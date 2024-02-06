package de.yard.threed.engine.platform;

import de.yard.threed.core.CharsetException;
import de.yard.threed.core.Pair;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.platform.AsyncHttpResponse;
import de.yard.threed.core.platform.AsyncJobDelegate;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeBundleResourceLoader;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.resource.BundleLoadDelegate;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResolver;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.LoadingBundle;
import de.yard.threed.core.resource.NativeResource;
import de.yard.threed.core.resource.ResourcePath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Load a bundle from somewhere via plaform (also for preinit). But it is not added to BundleRegistry here.
 * <p>
 * Keeps track of currently loaded bundles to avoid multiple concurrent load.
 * For all platforms. Uses platform for loading single files.
 * <p>
 * Not for user code usage to avoid MT effects. Should be called via Scenerunner.
 * 'delayed' not implemented
 * Derived from WebGlBundleLoader and HttpBundleLoader.
 * Located in 'engine' to have SimpleHeadlessPlatform for testing.
 * Bundle resolution and full qualified bundle handling is done outside and capsuled in NativeResourceLoader.
 */
public class PlatformBundleLoader {
    Log logger = Platform.getInstance().getLog(PlatformBundleLoader.class);

    String url;
    // bundles currently being loaded
    private Map<String, LoadingBundle> loadingbundles = new HashMap<String, LoadingBundle>();
    // From WebGlBundleLoader: currently active async loadings. Hier kommt nur der einfache Namen rein, weil die Resource teilweise "bundles" enthält. Das dürfte aber eciehn um paralleladen zu verhindern.
    public List<String> currentlyloading = new ArrayList<String>();

    public PlatformBundleLoader() {
        this.url = url;
    }

    /**
     * First the directory, then the content.
     * 'bundlename' must be a full qualified name (URL)
     */
    public void loadBundle(String bundlename, BundleLoadDelegate bundleLoadDelegate, NativeBundleResourceLoader resourceLoader) {

        logger.debug("Loading effective bundle " + bundlename);

        if (resourceLoader == null) {
            logger.error("No resourceLoader. Bundle '" + bundlename + "' not resolvable?");
            bundleLoadDelegate.bundleLoad(null);
            return;
        }
        String directoryUrl = /*13.12.23 url + "/" +*/ BundleRegistry.getDirectoryName(bundlename, true);

        //logger.debug("bundlebasedir=" + bundlebasedir);
        logger.debug("directoryUrl=" + directoryUrl);
        // avoid concurrent loading
        LoadingBundle clb = loadingbundles.get(bundlename);
        if (clb == null) {
            clb = new LoadingBundle(bundlename, bundleLoadDelegate, false);
            loadingbundles.put(bundlename, clb);
        } else {
            // load already running. Hook into that.
            clb.addDelegate(bundleLoadDelegate);
            return;
        }
        LoadingBundle lb = clb;
        //loadingbundle.add(lb);
        //WebGlResource directory = new WebGlResource(directoryresource);

        //loadRessource(new BundleResource(directoryUrl), new AsyncJobDelegate<AsyncHttpResponse>() {
        resourceLoader.loadFile(directoryUrl, new AsyncJobDelegate<AsyncHttpResponse>() {
            @Override
            public void completed(AsyncHttpResponse response) {
                logger.debug("Got http response " + response);
                if (response.getStatus() == 200) {

                    logger.debug("directory loaded for bundle " + bundlename);
                    String d;
                    try {
                        d = response.getContentAsString();
                    } catch (CharsetException e) {
                        // TODO improved eror handling
                        throw new RuntimeException(e);
                    }

                    lb.bundle = new Bundle(lb.bundlename, d, false, resourceLoader.getBasePath());

                    for (String filename : lb.bundle.directory) {
                        String resource = /*13.12.23 url + "/" +*/ filename;
                        loadBundleData(lb.bundle, BundleResource.buildFromFullString(resource), filename, false, lb/*, bundlebasedir*/, resourceLoader);
                    }

                } else {
                    logger.error("Unexpected response " + response);
                    // Assume bundle doesn't exist. Anyway we need to avoid an endless wait, so inform requester.
                    logger.error("No directory loaded. Bundle '" + bundlename + "' not existing?");
                    bundleLoadDelegate.bundleLoad(null);
                }
            }
        }/*, false*/);

    }

    public ArrayList<String> getLoadingbundles() {
        return new ArrayList<String>(loadingbundles.keySet());
    }

    /**
     * Not needed here because it is async on its own?
     */
    public List<Pair<BundleLoadDelegate, Bundle>> processAsync() {

        //Sicherheitshalber mal prefen. Wird aber offenbar auch in webgl verwendet.Siehe header.
        if (true || Platform.getInstance().hasOwnAsync()) {
            throw new RuntimeException("invalid usage of AsyncHelper");
        }

        List<Pair<BundleLoadDelegate, Bundle>> result = new ArrayList<Pair<BundleLoadDelegate, Bundle>>();

        return result;
    }


    /**
     * Nicht relevant, weil sync geladen wird.
     *
     * @param file
     * @return
     */
    public boolean isLoading(BundleResource file) {
        return false;
    }

    private void loadBundleData(Bundle bundle, BundleResource resource, String filename, boolean delayed, LoadingBundle lb/*, String bundlebasedir*/,
                                NativeBundleResourceLoader resourceLoader) {

        AsyncJobDelegate listener = new AsyncJobDelegate<AsyncHttpResponse>() {
            @Override
            public void completed(AsyncHttpResponse response) {
                if (response.getStatus() == 200) {
                    logger.trace(resource.getFullName() + " loaded with response " + response);

                    BundleData bundleData;
                    if (Bundle.isBinary(filename)) {
                        bundleData = new BundleData(response.getContent(), false);
                    } else {
                        bundleData = new BundleData(response.getContent(), true);
                    }
                    if (bundle.contains(filename)) {
                        logger.error("duplicate directory entry " + filename + " or data already loaded: " + bundle.getResource(filename).getSize() + " bytes");
                    }
                    bundle.addResource(filename, bundleData);
                } else {
                    logger.error(resource.getFullName() + " failed with response " + response);
                    if (bundle.contains(filename)) {
                        logger.error("onError, but data exists for " + filename);
                    }
                    bundle.addFailure(filename, "" + response.getStatus());
                }
                checkCompleted(lb, bundle);

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
                loadRessource(resource, listener, false, resourceLoader);
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
                loadRessource(res, listener, true, resourceLoader);
                break;
            case 'i':
                //Image/Texture will be loaded later on demand internally by platform itself. Nevertheless have an entry in the bundle to
                //be more consistent.
                bundle.addResource(filename, null);
                // important if image is the last entry
                checkCompleted(lb, bundle);
                break;
            case 's':
                //sound will be loaded later on demand internally by platform itself. Nevertheless have an entry in the bundle to
                //be more consistent.
                bundle.addResource(filename, null);
                // important if image is the last entry
                checkCompleted(lb, bundle);
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

    private void checkCompleted(LoadingBundle lb, Bundle bundle) {
        if (lb.isReady()) {
            logger.info("bundle " + bundle.name + " load complete(" + (bundle.getSizeInBytes() / 1000000) + " MB," + bundle.getSize() + " files,took " +
                    (Platform.getInstance().currentTimeMillis() - lb.started) + " ms)");

            bundle.complete();
            for (BundleLoadDelegate delegate : lb.callbacks) {
                delegate.bundleLoad(bundle);
            }
        }
        loadingbundles.remove(bundle.name);
    }

    public void loadRessource(final NativeResource ressource, final AsyncJobDelegate loadlistener, boolean binary, NativeBundleResourceLoader resourceLoader) {
        //logger.debug("loadRessource:" + ressource.getFullName());

        resourceLoader.loadFile(ressource.getFullName(), loadlistener);

    }


}


