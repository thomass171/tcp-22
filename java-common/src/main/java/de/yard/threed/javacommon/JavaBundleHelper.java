package de.yard.threed.javacommon;

import de.yard.threed.core.Pair;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.buffer.NativeByteBuffer;
import de.yard.threed.core.platform.AsyncHttpResponse;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeFuture;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.URL;
import de.yard.threed.outofbrowser.FileSystemResource;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

public class JavaBundleHelper {

    /**
     * Load a texture defined in a bundle, maybe cached or via HTTP and wait for
     * load completion. For platforms that are not prepared to do
     * an async texture load like ThreeJs.
     * Non http part extrcated from PlatformJme.
     * <p>
     * Expects bundle to be set in BundleResource.
     * <p>
     */
    public static BufferedImage loadBundleTexture(/*2.1.24BundleResource*/URL textureresource) {
        // bundle traditionally was expected to be set
        /*if (textureresource.bundle == null) {
            getLogger().error("bundle not set for file " + textureresource.getFullName());
            return null;
        }

        Bundle bundle = textureresource.bundle;

        if (StringUtils.startsWith(bundle.getBasePath(), "http")) {*/
        if (textureresource.isHttp()) {
            // HttpBundleResourceLoader cannot be used because it needs platform/scenerunner for futures.
            // Since we are waiting here, there should be no MT problem with the future.

            List<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
            List<Pair<String, String>> headers = new ArrayList<Pair<String, String>>();
            //NativeFuture<AsyncHttpResponse> future = new JavaWebClient().httpGet(bundle.getBasePath() + "/" + textureresource.getFullName(), parameters, headers);
            NativeFuture<AsyncHttpResponse> future = new JavaWebClient().httpGet(textureresource.getUrl(), parameters, headers);

            while (!future.isDone()) {
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            AsyncHttpResponse response = future.get();
            if (response.getStatus() == 200) {
                NativeByteBuffer buffer = response.getContent();
                return ImageUtils.loadImageFromFile(getLogger(), new ByteArrayInputStream(buffer.getBuffer()), textureresource.getName());
            } else {
                getLogger().error("response with fail status " + response.getStatus());
                return null;
            }
        } else {
            // 28.12.23 since bundle knows its basepath meanwhile, decouple from resolver
            //String bundlebasedir = BundleResolver.resolveBundle(textureresource.bundle.name, Platform.getInstance().bundleResolver).getPath();
            //2.1.24 String bundlebasedir = bundle.getBasePath();
            //2.1.24 FileSystemResource resource = FileSystemResource.buildFromFullString(bundlebasedir + "/" + textureresource.getFullName());
            FileSystemResource resource = FileSystemResource.buildFromFullString(textureresource.getUrl());
            return ImageUtils.loadAndCacheImage(resource);
        }
    }

    private static Log getLogger() {
        return Platform.getInstance().getLog(JavaBundleHelper.class);
    }
}
