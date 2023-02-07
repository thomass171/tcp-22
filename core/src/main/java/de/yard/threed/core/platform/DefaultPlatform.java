package de.yard.threed.core.platform;

import de.yard.threed.core.*;
import de.yard.threed.core.buffer.NativeByteBuffer;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.resource.BundleLoadDelegate;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourcePath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Default implementation implementing nothing. Sometimes null is returned and sometimes an exception thrown. Not yet clear whats better.
 *
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
    public void buildNativeModelPlain(BundleResource filename, ResourcePath opttexturepath, ModelBuildDelegate modeldelegate, int options) {

    }

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
    public NativeMaterial buildMaterial(String name, HashMap<ColorType, Color> color, HashMap<String, NativeTexture> texture, HashMap<NumericType, NumericValue> parameters, Object effect) {
        return null;
    }

    @Override
    public NativeTexture buildNativeTexture(BundleResource filename, HashMap<NumericType, NumericValue> parameters) {
        return null;
    }

    @Override
    public NativeTexture buildNativeTexture(ImageData imagedata, boolean fornormalmap) {
        return null;
    }

    @Override
    public NativeTexture buildNativeTexture(NativeCanvas imagedata) {
        return null;
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
    public boolean GetKeyDown(int keycode) {
        return false;
    }

    @Override
    public boolean GetKey(int keycode) {
        return false;
    }

    @Override
    public Point getMouseMove() {
        return null;
    }

    @Override
    public Point getMouseClick() {
        return null;
    }

    @Override
    public Point getMousePress() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Configuration getConfiguration() { return null; };

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

    @Override
    protected Log getLog() {
        return null;
    }

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

    /*MA36 ueber runner public void sendHttpRequest(String url, String method, String[] header, AsyncJobDelegate<AsyncHttpResponse> asyncJobDelegate ){
        throw new RuntimeException(("not implemented"));
    }*/

    @Override
    public  NativeScene getScene() {
        throw new RuntimeException("not implemented");
    }

    /*@Override
    public void loadBundle(String bundlename, BundleLoadDelegate bundleLoadDelegate, boolean delayed) {
        throw new RuntimeException("no bundle loader implemented");
    }*/

    @Override
    public  NativeSocket connectToServer(String server, int port) {
        throw new RuntimeException("not implemented");
    }
}
