package de.yard.threed.engine;

import de.yard.threed.core.Pair;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.AsyncHttpResponse;
import de.yard.threed.core.platform.AsyncJobDelegate;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeBundleLoader;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.resource.BundleLoadDelegate;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.LoadingBundle;
import de.yard.threed.core.resource.NativeResource;

import java.util.ArrayList;
import java.util.List;

/**
 * Load a bundle via HTTP from a server. But it is not added to BundleRegistry here.
 * Derived from WebGlBundleLoader. Located in 'engine' to have SimpleHeadlessPlatform for testing.
 * <p>
 * <p>
 */
public class HttpBundleLoader implements NativeBundleLoader {
    Log logger = Platform.getInstance().getLog(HttpBundleLoader.class);

    String url;
    LoadingBundle lb;
    // currently active async loadings. Hier kommt nur der einfache Namen rein, weil die Resource teilweise "bundles" enthält. Das dürfte aber eciehn um paralleladen zu verhindern.
    public List<String> currentlyloading = new ArrayList<String>();

    public HttpBundleLoader() {
        this.url = url;
    }

    /**
     * First the directory, then the content.
     * 'bundlename' must be a full qualified name (URL)
     */
    @Override
    public void asyncBundleLoad(String bundlename, BundleLoadDelegate bundleLoadDelegate, boolean delayed) {
        if (!StringUtils.startsWith(bundlename, "http")) {
            logger.error("no http url:" + bundlename);
            // no info? well, the bundle wasn't loaded bundleLoadDelegate.bundleLoad(null);
            return;
        }
        String url = bundlename;
        bundlename = StringUtils.substringAfterLast(bundlename, "/");
        logger.debug("Loading effective bundle " + bundlename);

        //String bundlebasedir = BundleResolver.resolveBundle(bundlename, Platform.getInstance().bundleResolver).getPath();
        String directoryUrl = url + "/" + BundleRegistry.getDirectoryName(bundlename, true);

        //logger.debug("bundlebasedir=" + bundlebasedir);
        logger.debug("directoryUrl=" + directoryUrl);
        // TODO race conditioin? Koennte das selbe Bundle mehrfach laden?
        lb = new LoadingBundle(bundlename, bundleLoadDelegate, delayed);
        //loadingbundle.add(lb);

        //WebGlResource directory = new WebGlResource(directoryresource);

        loadRessource(new BundleResource(directoryUrl), new AsyncJobDelegate<AsyncHttpResponse>() {
            @Override
            public void completed(AsyncHttpResponse response) {
                logger.debug("Got http response " + response);
                if (response.getStatus() == 200) {

                    logger.debug("directory loaded");

                    lb.bundle = new Bundle(lb.bundlename, response.getContentAsString(), delayed);

                    for (String filename : lb.bundle.directory) {
                        String resource = url + "/" + filename;
                        loadBundleData(lb.bundle, BundleResource.buildFromFullString(resource), filename, delayed/*, bundlebasedir*/);
                    }

                } else {
                    logger.error("Unexpected response " + response);
                }
            }
        }, false);

    }

    /**
     * Not needed here because HTTP GET is async on its own?
     */
    @Override
    public List<Pair<BundleLoadDelegate, Bundle>> processAsync() {

        //Sicherheitshalber mal prefen. Wird aber offenbar auch in webgl verwendet.Siehe header.
        if (true || Platform.getInstance().hasOwnAsync()) {
            throw new RuntimeException("invalid usage of AsyncHelper");
        }

        List<Pair<BundleLoadDelegate, Bundle>> result = new ArrayList<Pair<BundleLoadDelegate, Bundle>>();

        return result;
    }

    @Override
    public void completeBundle(BundleResource file) {
        //??
        Util.notyet();
    }

    /**
     * Nicht relevant, weil sync geladen wird.
     *
     * @param file
     * @return
     */
    @Override
    public boolean isLoading(BundleResource file) {
        return false;
    }

    private void loadBundleData(Bundle bundle, BundleResource resource, String filename, boolean delayed/*, String bundlebasedir*/) {

        AsyncJobDelegate listener = new AsyncJobDelegate<AsyncHttpResponse>() {
            @Override
            public void completed(AsyncHttpResponse response) {
                logger.debug("Got http response " + response);
                if (response.getStatus() == 200) {

                    logger.debug(" loaded");

                    BundleData bundleData;
                    if (Bundle.isBinary(filename)) {
                        bundleData = new BundleData(response.getContent(), false);
                    } else {
                        bundleData = new BundleData(response.getContentAsString());
                    }
                    if (bundle.contains(filename)) {
                        logger.error("duplicate directory entry " + filename + " or data already loaded: " + bundle.getResource(filename).getSize() + " bytes");
                    }
                    bundle.addResource(filename, bundleData);


                } else {
                    logger.error("HTTP response with status " + response.getStatus());
                    if (bundle.contains(filename)) {
                        logger.error("onError, but data exists for " + filename);
                    }
                    bundle.addFailure(filename, "" + response.getStatus());
                }
                if (lb.isReady()) {
                    lb.callback.bundleLoad(bundle);
                }
            }
        };
        /*ResourceLoadingListener listener =
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
                };*/
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
                BundleResource res = resource;
                /*if (StringUtils.endsWith(filename, ".btg.gz")) {
                    // uncompressed lesen weil ein uncompress in js oder Browser offenbar nicht geht.
                    res = new BundleResource(bundlebasedir + "/" + StringUtils.substringBeforeLast(filename, ".gz"));
                }*/
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

    public void loadRessource(final NativeResource ressource, final AsyncJobDelegate loadlistener, boolean binary) {
        logger.debug("loadRessource:" + ressource.getFullName());
        final String name = ressource.getFullName();
        //30.9.18: Mit base url prefix if no host specified
        final String url = name;//StringUtils.startsWith(name, "http") ? name : GWT.getHostPageBaseURL() + name;
        String cachedisablequerystring = "?timestamp=" + Platform.getInstance().currentTimeMillis();
        String key;
        if (ressource.getName().contains("/")) {
            key = StringUtils.substringAfterLast(ressource.getName(), "/");
        } else {
            key = ressource.getName();
        }
        currentlyloading.add(key);
        Platform.getInstance().httpGet(url, null, null, loadlistener);
       /* if (binary) {
            XMLHttpRequest request = XMLHttpRequest.create();
            request.open("GET", url);
            request.setResponseType(XMLHttpRequest.ResponseType.ArrayBuffer);
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
            final String resname = url + cachedisablequerystring;
            RequestBuilder requestBuilder =  new RequestBuilder(RequestBuilder.GET, resname);
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
                            logger.error("Status code " + response.getStatusCode() + " for resource '" + ressource.getFullName() + "' with url " + resname);
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
        }*/
    }


}

