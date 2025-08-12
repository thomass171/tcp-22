package de.yard.threed.core.platform;

import de.yard.threed.core.*;
import de.yard.threed.core.buffer.NativeByteBuffer;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourceLoader;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.resource.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Default implementation implementing nothing. Sometimes null is returned and sometimes an exception thrown. Not yet clear whats better.
 * <p>
 * 5.7.21
 */
public class DefaultPlatform extends Platform {
    @Override
    public NativeSceneNode buildModel() {
        return null;
    }

    @Override
    public NativeSceneNode buildModel(String name) {
        return null;
    }

    @Override
    public void buildNativeModelPlain(ResourceLoader resourceLoader, ResourcePath opttexturepath, ModelBuildDelegate modeldelegate, int options) {
    }

    /**
     * A node search by the platform. A custom search is in SceneNode.findNode[ByName]().
     */
    @Override
    public List<NativeSceneNode> findSceneNodeByName(String name) {
        return null;
    }

    @Override
    public NativeMesh buildNativeMesh(NativeGeometry nativeGeometry, NativeMaterial material, boolean castShadow, boolean receiveShadow) {
        return null;
    }

    @Override
    public NativeSceneNode buildLine(Vector3 from, Vector3 to, Color color) {
        return null;
    }

    @Override
    public void updateMesh(NativeMesh mesh, NativeGeometry nativeGeometry, NativeMaterial material) {

    }

    @Override
    public NativeCamera buildPerspectiveCamera(double fov, double aspect, double near, double far) {
        return null;
    }

    @Override
    public NativeMaterial buildMaterial(String name, HashMap<ColorType, Color> color, HashMap<String, NativeTexture> texture, HashMap<NumericType, NumericValue> parameters/*, Object effect*/) {
        return null;
    }

    @Override
    public NativeMaterial buildMaterial(NativeProgram program, boolean opaque) {
        // too early to call registerAndInitializeShaderMaterial() because defaults are not yet set
        return null;
    }

    @Override
    public NativeTexture buildNativeTexture(/*2.1.24BundleResource*/URL filename, HashMap<NumericType, NumericValue> parameters) {
        return null;
    }

    @Override
    public NativeTexture buildNativeTexture(ImageData imagedata, boolean fornormalmap) {
        return null;
    }

    @Override
    public NativeTexture buildNativeTexture(NativeCanvas imagedata) {
        throw new RuntimeException("NativeCanvas is not supported currently due to LWJGL3/AWT conflicts");
    }

    @Override
    public NativeLight buildPointLight(Color argb, double range) {
        return null;
    }

    @Override
    public NativeLight buildAmbientLight(Color argb) {
        return null;
    }

    @Override
    public NativeLight buildDirectionalLight(Color argb, Vector3 direction) {
        return null;
    }

    @Override
    public List<NativeLight> getLights() {
        return null;
    }

    @Override
    public NativeGeometry buildNativeGeometry(Vector3Array vertices, int[] indices, Vector2Array uvs, Vector3Array normals) {
        return null;
    }

    @Override
    public NativeDocument parseXml(String xmltext) throws XmlException {
        return null;
    }

    @Override
    public NativeContentProvider getContentProvider(char type, String location, TestPdfDoc docid) {
        return null;
    }

    @Override
    public NativeSplineInterpolationFunction buildNativeSplineInterpolationFunction(double[] x, double[] y) {
        return null;
    }

    @Override
    public NativeRay buildRay(Vector3 origin, Vector3 direction) {
        return null;
    }

    /*10.7.21 @Override
    public NativeSceneRunner getSceneRunner() {
        return null;
    }*/

    @Override
    public NativeCanvas buildNativeCanvas(int width, int height) {
        return null;
    }

    @Override
    public long currentTimeMillis() {
        return 0;
    }

    @Override
    public boolean getKeyDown(int keycode) {
        return false;
    }

    @Override
    public boolean getKeyUp(int keycode) {
        return false;
    }

    @Override
    public boolean getKey(int keycode) {
        return false;
    }

    @Override
    public Point getMouseMove() {
        return null;
    }

    @Override
    public Point getMouseUp() {
        return null;
    }

    @Override
    public Point getMouseDown() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Configuration getConfiguration() {
        return null;
    }

    @Override
    public NativeEventBus getEventBus() {
        return null;
    }

    @Override
    public void abort() {

    }

    @Override
    public NativeJsonValue parseJson(String jsonstring) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean isDevmode() {
        return false;
    }

    @Override
    public NativeVRController getVRController(int index) {
        return null;
    }

    @Override
    public NativeRenderProcessor buildSSAORenderProcessor() {
        return null;
    }

    @Override
    public void addRenderProcessor(NativeRenderProcessor renderProcessor) {

    }

    /*24.10.23 try without @Override
    protected Log getLog() {
        return null;
    }*/

    @Override
    public Log getLog(Class clazz) {
        return null;
    }

    @Override
    public NativeStringHelper buildStringHelper() {
        return null;
    }

    @Override
    public float getFloat(byte[] buf, int offset) {
        return 0;
    }

    @Override
    public void setFloat(byte[] buf, int offset, float f) {

    }

    @Override
    public double getDouble(byte[] buf, int offset) {
        return 0;
    }

    @Override
    public NativeByteBuffer buildByteBuffer(int size) {
        return null;
    }

    @Override
    public void httpGet(String url, List<Pair<String, String>> parameter, List<Pair<String, String>> header, AsyncJobDelegate<AsyncHttpResponse> asyncJobDelegate) {
        throw new RuntimeException(("not implemented"));
    }

    @Override
    public NativeScene getScene() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public NativeAudioClip buildNativeAudioClip(BundleResource filename) {
        logger.warn("NativeAudioClip not implemented");
        return null;
    }

    @Override
    public NativeAudio buildNativeAudio(NativeAudioClip audioClip) {
        logger.warn("NativeAudio not implemented");
        return null;
    }

    @Override
    public NativeBundleResourceLoader buildResourceLoader(String basedir, String location) {throw new RuntimeException("not implemented"); }

    /**
     * 20.8.24: Just a simple default implementation. The platform might do it more efficiently.
     */
    @Override
    public List<NativeSceneNode> findNodeByName(String name, NativeSceneNode startnode) {
        List<NativeSceneNode> nodelist = new ArrayList<NativeSceneNode>();
        // 3.1.18: Also check 'this'.
        if (name.equals(startnode.getName())) {
            nodelist.add(startnode);
        }
        for (NativeTransform child : startnode.getTransform().getChildren()) {
            if (child != null) {
                NativeSceneNode csn = child.getSceneNode();
                if (csn != null) {
                    nodelist.addAll(this.findNodeByName(name, csn));
                }
            }
        }
        return nodelist;
    }

    @Override
    public NativeProgram buildProgram(String name, BundleResource vertexShader, BundleResource fragmentShader) {
        return null;
    }

    /*@Override
    public void loadBundle(String bundlename, BundleLoadDelegate bundleLoadDelegate, boolean delayed) {
        throw new RuntimeException("no bundle loader implemented");
    }*/

    @Override
    public NativeSocket connectToServer(Server server) {
        throw new RuntimeException("not implemented");
    }
}
