package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.typedarrays.client.JsUtils;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.typedarrays.shared.TypedArrays;
import com.google.gwt.typedarrays.shared.Uint8Array;
import com.google.gwt.xhr.client.ReadyStateChangeHandler;
import com.google.gwt.xhr.client.XMLHttpRequest;
import de.yard.threed.core.Pair;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeBundleLoader;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.resource.BundleLoadDelegate;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResolver;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.NativeResource;
import de.yard.threed.engine.platform.ResourceLoadingListener;

import java.util.ArrayList;
import java.util.List;

public class WebGlBundleLoader implements NativeBundleLoader {
    Log logger = Platform.getInstance().getLog(WebGlBundleLoader.class);

    private int preloadingcnt, preloaded, preloadedfailure;
    //loadingbundle sind die, die gerade geladen werden.
    List<LoadingBundle> loadingbundle = new ArrayList<LoadingBundle>();
    // currently active async loadings. Hier kommt nur der einfache Namen rein, weil die Resource teilweise "bundles" enthält. Das dürfte aber eciehn um paralleladen zu verhindern.
    public List<String> currentlyloading = new ArrayList<String>();
    private String preloadError = null;

    @Override
    public void asyncBundleLoad(String bundlename, BundleLoadDelegate delegate, boolean delayed) {
        /*WebGlResourceManager.getInstance().*/
        loadBundlePlatformInternal(bundlename, delegate, delayed);
    }

    @Override
    public List<Pair<BundleLoadDelegate, Bundle>> processAsync() {
        throw new RuntimeException("processAsync not needed");
    }

    @Override
    public void completeBundle(BundleResource file) {
        Bundle bundle = file.bundle;
        //ResourceManager rm = Platform.getInstance().getRessourceManager();

        //String bundlebasedir = BundleRegistry.getBundleBasedir(bundle.name, true);
        String bundlebasedir = BundleResolver.resolveBundle(bundle.name, Platform.getInstance().bundleResolver).getPath();
        //String resource = bundlebasedir + "/" + BundleRegistry.getDirectoryName(bundle.name, false);
        String filename = file.getFullName();
        //resource = bundlebasedir + "/" + filename;
        WebGlResource webglresource = new WebGlResource(bundlebasedir + "/" + filename);
        loadBundleData(bundle, webglresource, filename, false, bundlebasedir);

    }

    @Override
    public boolean isLoading(BundleResource file) {
        boolean isloading = currentlyloading.contains(file.getName());
        //10.10.18: sehr oft duplicate logs bei WebGL, darum nicht mehr immer loggen.
        if (BundleRegistry.bundledebuglog) {
            logger.debug("isLoading: " + file.getFullName() + ",loading.size=" + currentlyloading.size() + ",isloading=" + isloading);
        }
        if (currentlyloading.size() > 0) {
            //logger.debug("getFirst loading:" + currentlyloading.get(0));
        }
        return isloading;
    }

    public void preLoad(String[] bundles) {
        preloadingcnt = bundles.length;
        for (String bundlename : bundles) {
            boolean delayed = false;
            if (bundlename.endsWith("-delayed")) {
                //TODO das mit delay ist ne kruecke
                bundlename = bundlename.replaceAll("-delayed", "");
                delayed = true;
            }
            logger.debug("preloading bundle " + bundlename);
            loadBundlePlatformInternal(bundlename, (Bundle b) -> {
                if (b != null) {
                    preloaded++;
                } else {
                    //already logged
                    preloadedfailure++;
                }
            }, delayed);
        }
    }

    public String getPreloadError() {
        return preloadError;
    }

    /**
     * Pruefen, ob alle Preinit Bundle geladen sind.
     *
     * @return
     */
    public boolean preLoadCompleted() {
        return preloaded == preloadingcnt;
    }

    public boolean preLoadFailure() {
        return preloadedfailure > 0;
    }

    private void loadBundleData(Bundle bundle, WebGlResource resource, String filename, boolean delayed, String bundlebasedir) {
        ResourceLoadingListener listener =
                new ResourceLoadingListener() {
                    @Override
                    public void onLoad(BundleData bytebuf) {
                        //debug.log ist schon im loadResource()
                        if (bundle.contains(filename)) {
                            logger.error("duplicate directory entry " + filename + " or data already loaded: " + bundle.getResource(filename).getSize());
                        }
                        bundle.addResource(filename, bytebuf);
                    }

                    @Override
                    public void onError(int httperrorcode) {
                        // already logged
                        //14.6.17:  Wenn beim Lesen eines einzelnen Elements ein Fehler auftritt, wird das im Bundle vermerkt und der Eintrag bleibt halt leer. Es erfolgt aber kein Abbruch.
                        if (bundle.contains(filename)) {
                            logger.error("onError, but data exists for " + filename);
                        }
                        bundle.addFailure(filename, "" + httperrorcode);
                    }
                };
        //logger.debug("filename="+filename);
        char filetype = Bundle.filetype(filename);
        switch (filetype) {
            case 'T'://GLTF
                // C# conform fall through
            case 't':
                if (filetype == 'T' && delayed) {
                    bundle.addResource(filename, null);
                    break;
                }
                loadRessource(resource, listener, false);
                break;
            case 'B'://GLTF binary
                // C# conform fall through
            case 'b':
                if (filetype == 'B' && delayed) {
                    bundle.addResource(filename, null);
                    break;
                }
                WebGlResource res = resource;
                if (StringUtils.endsWith(filename, ".btg.gz")) {
                    // uncompressed lesen weil ein uncompress in js oder Browser offenbar nicht geht.
                    res = new WebGlResource(bundlebasedir + "/" + StringUtils.substringBeforeLast(filename, ".gz"));
                }
                loadRessource(res, listener, true);
                break;
            case 'i':
                //Image/Textur wird spaeter "intern" geladen.
                bundle.addResource(filename, null);
                break;
            case 'z':
                //zipped binary (btg.gz). Der unzip wird schon hier statt beim getRersource gemacht, weil dies hier
                //in der Platform ist.
                //19.8.23: zip download was never a real option in JS.
                throw new RuntimeException("no gz/zip download");
                //loadRessource(resource, listener, true, true);
                //break;
                                /*12.1.18 case 'g':
                                    //gltf wird - je nach dem - spaeter "intern" geladen.
                                    if (PlatformWebGl.customgltfloader) {
                                        loadRessource(resource, listener, false, false);
                                    } else {
                                        lb.bundle.addResource(filename, null);
                                    }
                                    break;*/
            default:
                //unknown
                logger.warn("unknown filetype " + filetype);
                bundle.addResource(filename, null);
                break;
        }
    }

    /**
     * Like a preload. First the directory, then the content.
     *
     * @param bundlename
     * @param loadlistener
     */
    public void loadBundlePlatformInternal(final String bundlename, BundleLoadDelegate loadlistener, boolean delayed) {
        //String bundlebasedir = BundleRegistry.getBundleBasedir(bundlename, true);
        String bundlebasedir = BundleResolver.resolveBundle(bundlename, Platform.getInstance().bundleResolver).getPath();
        String directoryresource = bundlebasedir + "/" + BundleRegistry.getDirectoryName(bundlename, true);

        // TODO race conditioin? Koennte das selbe Bundle mehrfach laden?
        LoadingBundle lb = new LoadingBundle(bundlename, loadlistener, delayed);
        loadingbundle.add(lb);

        WebGlResource directory = new WebGlResource(directoryresource);
        loadRessource(directory, new ResourceLoadingListener() {
                    @Override
                    public void onLoad(BundleData bytebuf) {
                        if (BundleRegistry.bundledebuglog) {
                            logger.debug("directory loaded");
                        }
                        lb.bundle = new Bundle(bundlename, bytebuf.getContentAsString(), delayed);
                        //loadlistenerlist.add(loadlistener);
                        for (String filename : lb.bundle.directory) {
                            WebGlResource resource = new WebGlResource(bundlebasedir + "/" + filename);
                            loadBundleData(lb.bundle, resource, filename, delayed, bundlebasedir);
                        }
                        lb.doneloading = Platform.getInstance().currentTimeMillis();
                    }


                    @Override
                    public void onError(int httperrorcode) {
                        // already logged. Directory failed.
                        lb.addFailure(directoryresource);
                    }
                }

        );

    }


    //@Override
    public void loadRessource(final NativeResource ressource, final ResourceLoadingListener loadlistener) {
        loadRessource(ressource, loadlistener, false);
    }

    /**
     * Wichtig zu wissen: Man kann hier nur (wegen Javascript) Text laden, keine Binaries. Alles andere läuft auf Krücken Friemeli hinaus.
     * Wegen Performance kein getBytes(). Per XMLHttpRequest gehen auch Binaries.
     * Fuer Vereinfachung immer XMLHttpRequest verwenden geht aber nicht, weil einfache Textdaten damit aus unklaren Gründen
     * nicht (Safari) oder nur sehr langsam (Firefox) konvertiert werden können.
     * Das ist aber wohl ein Prinzipproblem, weil Javascript keine byte arrays kennt.
     * Man bekommt aus einem Utin8Array einfach keinen String gemacht. Läuft auf Fehler oder ist langsam.
     * Siehe auch https://developer.mozilla.org/en-US/docs/Web/JavaScript/Data_structures für Speicherbedarf.
     * <p>
     * 19.8.23: This method appears somehow weird. But flag binary is really useful with XMLHttpRequest, eg. for loading GLTF binary models? Really?.
     * But 'zipped' is no valid option.
     *
     * @param ressource
     * @param loadlistener
     */
    public void loadRessource(final NativeResource ressource, final ResourceLoadingListener loadlistener, boolean binary) {
        if (BundleRegistry.bundledebuglog) {
            logger.debug("loadRessource:" + ressource.getFullName());
        }
        final String name = ressource.getFullName();
        //30.9.18: Mit base url prefix if no host specified
        final String url = StringUtils.startsWith(name, "http") ? name : GWT.getHostPageBaseURL() + name;
        String cachedisablequerystring = "?timestamp=" + Platform.getInstance().currentTimeMillis();
        String key;
        if (ressource.getName().contains("/")) {
            key = StringUtils.substringAfterLast(ressource.getName(), "/");
        } else {
            key = ressource.getName();
        }
        currentlyloading.add(key);
        if (binary) {
            XMLHttpRequest request = XMLHttpRequest.create();
            //request.open("GET", "http://127.0.0.1:8888/"+name);
            request.open("GET", url/*name*/);
            request.setResponseType(XMLHttpRequest.ResponseType.ArrayBuffer);
            /*6.8.21 if (!binary) {
                // 03.05.2018: Auch hier versuchen mit etxt/plain, um XML parse error in Firefox loszuwerden
                //11.10.18:Das wird man aber wohl gar nicht los.
                // trotzdem kommt aber bei Firefox  ContentTyp "application/xml"
                //request.setRequestHeader("Content-Type", "text/plain");
                //request.setRequestHeader("Mime-Type", "text/plain");
                //request.setRequestHeader("Accept", "text/plain");
                request.setResponseType("'text/plain");
            }*/
            request.setOnReadyStateChange(new ReadyStateChangeHandler() {

                @Override
                public void onReadyStateChange(XMLHttpRequest xhr) {
                    currentlyloading.remove(key);
                    if (xhr.getReadyState() == XMLHttpRequest.DONE) {

                        if (xhr.getStatus() == 200) {
                            ArrayBuffer buffer = xhr.getResponseArrayBuffer();
                            if (BundleRegistry.bundledebuglog) {
                                logger.debug("onReadyStateChange. size=" + buffer.byteLength());
                            }
                            loadlistener.onLoad(new BundleData(new WebGlByteBuffer(buffer), false));
                        } else {
                            logger.error("XHR Status code " + xhr.getStatus() + " for resource '" + ressource.getFullName() + "' with url " + url);
                            // Der onload und muss immer aufgerufen werden, sonst bleibt er im preload.
                            //21.4.17: Jetzt muss der Aufrufer Fehler nachhalten.
                            loadlistener.onError(xhr.getStatus());
                        }
                    }
                }
            });
            request.send();

        } else {

            // 19.9.17: das muss man offenbar selber so dranfriemeln. Wobei, eigentlich ist Cachenutzung ja gut bei Bundles.TODO schaltbar?
            final String resname = /*name*/url + cachedisablequerystring;
            RequestBuilder requestBuilder =
                    new RequestBuilder(RequestBuilder.GET, resname);
            // 21.4.17: Firefox hat wohl den Dewfault ContentTyp "application/xml"
            // setzen im Request duerfte aber witzlos sein.??
            // "The GWT RequestBuilder uses XMLHttpRequest internally"
            requestBuilder.setHeader("Content-Type", "text/plain");
            //not valid in CORS. Useless anyway? requestBuilder.setHeader("Mime-Type", "text/plain");
            requestBuilder.setHeader("Accept", "text/plain");
            try {
                requestBuilder.sendRequest(null, new RequestCallback() {
                    public void onError(Request request, Throwable exception) {
                        currentlyloading.remove(key);
                        logger.error("onError" + exception.getMessage());
                        //TODO Fehlerhandling
                    }

                    public void onResponseReceived(Request request, Response response) {
                        currentlyloading.remove(key);
                        String responsedata = response.getText();
                        if (response.getStatusCode() != 200) {
                            logger.error("Status code " + response.getStatusCode() + " for resource " + ressource.getFullName() + "with url " + resname);
                            // Der onload und muss immer aufgerufen werden, sonst bleibt er im preload.
                            //21.4.17: Jetzt muss der Aufrufer Fehler nachhalten.
                            loadlistener.onError(response.getStatusCode());

                        } else {
                            // hier kommt man auch hin, wenn der Server eine Fehlerseite schickt (z.b: Jetty im Dev mode)
                            //TODO  Das muss ueberdacht werden im Rahmen von allen Laufzeitfehlern .
                            if (BundleRegistry.bundledebuglog) {
                                logger.debug("onResponseReceived for " + ressource.getFullName() + ". size=" + responsedata.length());
                            }
                            //logger.debug(url);
                            loadlistener.onLoad(new BundleData(responsedata));
                        }

                    }
                });
            } catch (RequestException e) {
                e.printStackTrace();
                //TODO Fehlerhandling
                logger.error("loadRessource" + e.getMessage());
            }
        }
    }

    public void checkBundleCompleted() {
        int loadedindex = -1;

        // check for success
        for (int index = 0; index < loadingbundle.size(); index++) {
            LoadingBundle lb = loadingbundle.get(index);
            if (lb.isReady()) {
                BundleRegistry.registerBundle(lb.bundle.name, lb.bundle, lb.delayed);
                loadedindex = index;
                break;
            }
        }
        if (loadedindex != -1) {
            BundleLoadDelegate callback = loadingbundle.get(loadedindex).callback;
            Bundle b = loadingbundle.get(loadedindex).bundle;
            loadingbundle.remove(loadedindex);
            callback.bundleLoad(b);
            // testAufruf2();
        }

        // check for failure
        loadedindex = -1;
        for (int index = 0; index < loadingbundle.size(); index++) {
            LoadingBundle lb = loadingbundle.get(index);
            if (lb.failed()) {
                logger.error("Bundle failed: " + lb.bundlename);
                loadedindex = index;
                break;
            }
        }
        if (loadedindex != -1) {
            LoadingBundle lb = loadingbundle.get(loadedindex);
            if (preloadError == null) {
                preloadError = "Loading failed for (baseurl=" + GWT.getHostPageBaseURL() + "):";
            }
            for (String f : lb.failurelist) {
                preloadError += f + ",";
            }

            BundleLoadDelegate callback = lb.callback;
            loadingbundle.remove(loadedindex);
            callback.bundleLoad(null);
        }
    }

}
