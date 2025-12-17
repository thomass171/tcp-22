package de.yard.threed.javacommon;

import de.yard.threed.core.Pair;
import de.yard.threed.core.buffer.NativeByteBuffer;
import de.yard.threed.core.platform.AsyncHttpResponse;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeFuture;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.URL;
import de.yard.threed.outofbrowser.FileSystemResource;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JavaBundleHelper {

    /**
     * Load a texture defined in a bundle, maybe cached or via HTTP and wait for
     * load completion. For platforms that are not prepared to do
     * an async texture load like ThreeJs.
     * Non http part extrcated from PlatformJme.
     * <p>
     * 19.2.24: Decoupled from bundle.
     * <p>
     */
    public static BufferedImage loadBundleTexture(/*2.1.24BundleResource*/URL textureresource) {
        if (textureresource.isHttp()) {
            NativeByteBuffer buffer  = loadViaHttp(textureresource);

            if (buffer!=null) {
                return ImageUtils.loadImageFromFile(getLogger(), new ByteArrayInputStream(buffer.getBuffer()), textureresource.getAsString());
            } else {
                // already logged
                return null;
            }
        } else {
            FileSystemResource resource = FileSystemResource.buildFromFullString(textureresource.getAsString());
            return ImageUtils.loadAndCacheImage(resource);
        }
    }

    /**
     * Extracted from above to be used for textures and audio
     */
    private static NativeByteBuffer loadViaHttp(URL textureresource){
        // HttpBundleResourceLoader cannot be used because it needs platform/scenerunner for futures.
        // Since we are waiting here, there should be no MT problem with the future.

        List<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
        List<Pair<String, String>> headers = new ArrayList<Pair<String, String>>();
        //NativeFuture<AsyncHttpResponse> future = new JavaWebClient().httpGet(bundle.getBasePath() + "/" + textureresource.getFullName(), parameters, headers);
        NativeFuture<AsyncHttpResponse> future = new JavaWebClient().httpGet(textureresource.getAsString(), parameters, headers);

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
            return buffer;
        } else {
            getLogger().error("response with fail status " + response.getStatus());
            return null;
        }
    }

    public static byte[] loadResource(URL textureresource) {
        if (textureresource.isHttp()) {
            NativeByteBuffer buffer  = loadViaHttp(textureresource);

            if (buffer!=null) {
                return buffer.getBuffer();
            } else {
                // already logged
                return null;
            }
        } else {
            FileSystemResource resource = FileSystemResource.buildFromFullString(textureresource.getAsString());
            try {
                return FileReader.readFully(FileReader.getFileStream(resource));
            } catch (IOException e) {
                getLogger().error("loadResource: ImageIO.read failed for " + resource.getFullName());
                return null;
            }
        }
    }


    private static Log getLogger() {
        return Platform.getInstance().getLog(JavaBundleHelper.class);
    }
}
