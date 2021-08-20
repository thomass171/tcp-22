package de.yard.threed.platform.webgl;

/*public class WebGlParsedGltf   implements NativeParsedGltf {
    JavaScriptObject json;
    
    // Overlay types always have protected, zero-arg ctors
    protected WebGlParsedGltf(String jsonStr) { json = JsonUtils.safeEval(jsonStr);}

    private static <T extends JavaScriptObject> T parseJson(String jsonStr)    {
        return JsonUtils.safeEval(jsonStr);
    }
    
    /*public static WebGlParsedGltf buidParsedGltf(String jsonStr)    {
        return parseJson(jsonStr);
    }* /

    @Override
    public int getNodeCount() {
        return getNodeCount(json);
    }

    @Override
    public String getNodeName(int index) {
        return getNodeName(json,index);
    }

    @Override
    public Vector3Array getVec3Buffer(int index){
        return null;
    }
    
    public final native String getName() /*-{
    return this.name;
  }-* /;

    private final native int getNodeCount(JavaScriptObject jso) / *-{
        return jso.nodes.length;
    }-* /;

    private final native String getNodeName(JavaScriptObject jso,int i) / *-{
        return jso.nodes[i].name;
    }-* /;
}*/
