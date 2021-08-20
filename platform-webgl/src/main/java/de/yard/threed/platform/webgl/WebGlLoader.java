package de.yard.threed.platform.webgl;


import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.platform.Log;

/**
 * Loader currently for gltf. Either by builtin loader or custom loader.
 * 11.1.18: Duerfte ausser fuer den internen deprecated sein
 * 28.3.17: Nur noch fuer den ThreeJS internen.
 * 
 * Created by thomass on 19.09.2017
 */
public class WebGlLoader {
    // Direkt auf die Plattform, um Abhaengigkeit auf engine zu vermeiden
    static Log logger = new WebGlLog(WebGlLoader.class.getName());
    
    public static void loadGLTFbyThreeJS(BundleResource res, int delegateid, String basename){
        
        /*if (PlatformWebGl.customgltfloader){
            logger.debug("deprecated using custom gltf loader for "+res.getFullName());
            BundleData gltfjson = res.bundle.getResource(res);
            //logger.debug(" got json" + gltfjson.s);
            NativeJsonValue value =  WebGlJsonValue.buildJsonValue(JSONParser.parseStrict(gltfjson.s));
            //logger.debug(""+gltf.getNodeCount()+" nodes");
            //if (true) return;
            // Das gltf liegt schon im Bundle, das bin muss noch geladen werden.
            BundleResource binres = LoaderGLTF.getBinResource(res);
            WebGlResourceManager.getInstance().loadRessource(binres, new ResourceLoadingListener(){
                @Override
                public void onLoad(BundleData data) {
                    logger.debug("got gltf binary with size "+data.b.getSize());
                    try {
                        LoaderGLTF lf = new LoaderGLTF(value.isString().stringValue(),data.b,null,"");
                    } catch (InvalidDataException e) {
                        logger.error("Loading gltf failed: "+e.getMessage());
                        //TODO Fehlerbehandlung fuer ungenutzte delegateid
                    }
                }

                @Override
                public void onError(int errorcode) {
                    logger.error("failed gltf binary");
                }
            }, true, false);
        }else*/ {
            buildGLTFLoader(res.getFullName(), delegateid, basename);
        }
    }
    /**
     * From threejs:webl_loader_gltf
     * 
     */
    private static native void buildGLTFLoader(String url, int delegateid,String basename)  /*-{
        var loader = new $wnd.THREE.GLTFLoader();
        //for (var i = 0; i < extensionSelect.children.length; i++) {
		//    var child = extensionSelect.children[i];
		//	child.disabled = sceneInfo.extensions.indexOf(child.value) === -1;
		//	if (child.disabled && child.selected) {
		//		extensionSelect.value = extension = 'glTF';
		//   }
		//}

		//		var url = sceneInfo.url;
		//		var r = eval("/" + '\%s' + "/g");
		//		url = url.replace(r, extension);

		//		if (extension === 'glTF-Binary') {
		//			url = url.replace('.gltf', '.glb');
		//		}

		//		var loadStartTime = performance.now();
		//		var status = document.getElementById("status");
		//		status.innerHTML = "Loading...";

		loader.load( url, function(data) {
		    $wnd.logger.debug("gltf load completed");
		    data.delegateid = delegateid;
		    data.scene.name = basename;
		    $wnd.loadedmodel.push(data);
        });
        
    }-*/;
}
