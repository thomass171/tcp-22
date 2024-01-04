package de.yard.threed.outofbrowser;

import de.yard.threed.core.buffer.SimpleByteBuffer;
import de.yard.threed.core.platform.AsyncHttpResponse;
import de.yard.threed.core.platform.AsyncJobDelegate;
import de.yard.threed.core.platform.NativeFuture;
import de.yard.threed.core.platform.NativeBundleResourceLoader;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.ResourceNotFoundException;
import de.yard.threed.core.resource.ResourcePath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileSystemBundleResourceLoader implements NativeBundleResourceLoader {

    ResourcePath basedir;

    public FileSystemBundleResourceLoader(ResourcePath basedir) {
        this.basedir = basedir;
    }

    @Override
    public void loadFile(String resource, AsyncJobDelegate<AsyncHttpResponse> asyncJobDelegate) {
        byte[] bytebuf = null;
        try {
            bytebuf = Files.readAllBytes(Paths.get(basedir.getPath() + "/" + resource));
            // code 200 to be HTTP compatible. Not nice.
            asyncJobDelegate.completed(new AsyncHttpResponse(200, null, new SimpleByteBuffer(bytebuf)));
        } catch (IOException e) {
            Platform.getInstance().getLog(FileSystemBundleResourceLoader.class).error("Failed to read " + resource + "," + e.getMessage());
            // code 400 to be HTTP compatible. Not nice. Well, -1 isn't really better.
            asyncJobDelegate.completed(new AsyncHttpResponse(400, null, null));
        }

    }

    @Override
    public String getBasePath() {
        return basedir.path;
    }
}
