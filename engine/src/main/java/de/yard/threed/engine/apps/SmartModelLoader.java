package de.yard.threed.engine.apps;

import de.yard.threed.core.*;
import de.yard.threed.core.loader.InvalidDataException;
import de.yard.threed.core.loader.LoaderAC;
import de.yard.threed.core.loader.PortableModel;
import de.yard.threed.core.loader.StringReader;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.ModelFactory;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.avatar.AvatarPmlFactory;
import de.yard.threed.engine.avatar.VehiclePmlFactory;
import de.yard.threed.engine.loader.PortableModelBuilder;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.platform.ResourceLoaderFromBundle;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * Load a model from a definition like "xxx:name;scale=...".
 * <p>
 * Doesn't use destinationNode but delegate to be more flexible.
 * <p>
 * Intended for testing and analyzing.
 */
public abstract class SmartModelLoader {
    static private Log logger = Platform.getInstance().getLog(SmartModelLoader.class);
    static Map<String, SmartModelLoader> registry = new HashMap<>();
    public static SmartModelLoader defaultSmartModelLoader;
    //a regular bundle load of plain GLTF (no XML) including bundle loading
    public static SmartModelLoader simpleSmartModelLoader;
    private static Map<String, GeneralFunction<PortableModel, String>> creatorClass = new HashMap<>();

    /**
     * Load errors might be forwarded via delegate. In that case no node should be passed
     * to delegate.
     * <p>
     * bundleUrl is used for external HTTP bundle, otherwise null
     */
    public abstract void loadModelBySource(String prefix, String modelname, String bundleUrl, ModelBuildDelegate delegate);

    public static void init() {
        registry = new HashMap<>();
        register("ac", new SmartModelLoader() {
            @Override
            public void loadModelBySource(String prefix, String modelname, String bundleUrl, ModelBuildDelegate delegate) {
                buildhardCodedAC(StringUtils.substringAfterLast(modelname, ":"), delegate);
            }
        });
        register("pcm", new SmartModelLoader() {
            @Override
            public void loadModelBySource(String prefix, String modelname, String bundleUrl, ModelBuildDelegate delegate) {
                // Pseudo Bundle
                //logger.debug("Building pcm for model " + modelname);
                PortableModel pml;
                if (creatorClass.containsKey(modelname)) {
                    pml = creatorClass.get(modelname).handle(null);
                } else if (modelname.equals("loc")) {
                    pml = VehiclePmlFactory.buildLocomotive();
                } else if (modelname.equals("bike")) {
                    pml = VehiclePmlFactory.buildBike();
                } else if (modelname.equals("mobi")) {
                    pml = VehiclePmlFactory.buildMobi();
                } else if (modelname.equals("avatarA")) {
                    pml = AvatarPmlFactory.buildAvatarA("red");
                } else {
                    throw new RuntimeException("unknown pcm model " + modelname);
                }
                SceneNode node = PortableModelBuilder.buildModel(pml, null);

                BuildResult result = new BuildResult(node.nativescenenode);
                delegate.modelBuilt(result);
            }
        });

        // Default is a regular bundle load of plain GLTF (no XML) including bundle loading
        defaultSmartModelLoader = simpleSmartModelLoader = new SmartModelLoader() {
            @Override
            public void loadModelBySource(String bundlename, String modelname, String bundleUrl, ModelBuildDelegate delegate) {
                Bundle bundle = BundleRegistry.getBundle(bundlename);
                if (bundle == null) {
                    AbstractSceneRunner.instance.loadBundle(bundlename, (Bundle b) -> {
                        addSimpleModelFromBundle(b, modelname, delegate);
                    });
                } else {
                    addSimpleModelFromBundle(bundle, modelname, delegate);
                }
            }
        };
    }

    public static void register(String prefix, SmartModelLoader smartModelLoader) {
        registry.put(prefix, smartModelLoader);
    }

    public static void loadAndScaleModelByDefinitions(String modelDefinition, ModelBuildDelegate delegate) {
        String[] parts = StringUtils.split(modelDefinition, ";");
        if (parts.length == 0) {
            logger.warn("Invalid modelDefinition:" + modelDefinition);
            return;
        }
        String modelpart = parts[0];
        double scale = Double.valueOf(parse(parts, "scale", "1.0"));
        Vector3 offset = Util.parseVector3(parse(parts, "offset", "0.0,0.0,0.0"));

        // Consider external http bundle
        String bundleUrl = parse(parts, "bundleUrl", null);

        String prefix = StringUtils.substringBefore(modelpart, ":");
        String modelname = StringUtils.substringAfter(modelpart, ":");

        SmartModelLoader smartModelLoader = registry.get(prefix);
        if (smartModelLoader == null) {
            // use default(load from bundle)
            smartModelLoader = defaultSmartModelLoader;
        }

        smartModelLoader.loadModelBySource(prefix, modelname, bundleUrl, result -> {
            if (result.getNode() != null) {
                result.getNode().setName(modelname);
                // 5.9.25 scale needs extra node. But outer around offset. Otherwise small deviations inside offset
                // will also scale (like in digital-clock model)
                result.getNode().getTransform().setPosition((offset));
                // decouple model node to keep scale etc. Needed 'twice' because getNode() returns native
                SceneNode translateNode = new SceneNode(new SceneNode(result.getNode()));
                translateNode.getTransform().setScale(new Vector3(scale, scale, scale));
                result = new BuildResult(translateNode.nativescenenode);
            }
            delegate.modelBuilt(result);
        });
    }

    private static String parse(String[] parts, String property, String defaultValue) {
        for (String s : parts) {
            if (StringUtils.startsWith(s, property + "=")) {
                return StringUtils.substringAfterLast(s, "=");
            }
        }
        return defaultValue;
    }

    /**
     * Add a simple/plain GLTF model
     */
    private static void addSimpleModelFromBundle(Bundle bundle, String modelname, ModelBuildDelegate delegate) {
        BundleResource br = BundleResource.buildFromFullString(modelname);
        br.bundle = bundle;
        Platform.getInstance().buildNativeModelPlain(new ResourceLoaderFromBundle(br), null, delegate, EngineHelper.LOADER_USEGLTF);
    }

    /**
     * ac files should be processed to GLTF and packeded in bundle for using it. Because there is no other workflow (eg. no
     * file access), for testing
     * we need to add these hardcoded. "sceneextension" (parameter) might also be an option.
     * No async here.
     */
    private static void buildhardCodedAC(String model, ModelBuildDelegate delegate) {

        String acSource = LoaderAC.sampleac;

        if (model.equals("hard-coded")) {
            acSource = "";
        }
        LoaderAC ac;
        try {
            ac = new LoaderAC(new StringReader(acSource), false);
        } catch (InvalidDataException e) {
            throw new RuntimeException(e);
        }

        String specificObject = null;// "Tunnel1Rotunda";
        if (specificObject != null) {
            // remove other
            while (ac.loadedfile.object.kids.size() > 1) {
                if (ac.loadedfile.object.kids.get(0).name.equals(specificObject)) {
                    ac.loadedfile.object.kids.remove(1);
                } else {
                    ac.loadedfile.object.kids.remove(0);
                }
            }
        }
        PortableModel portableModel = ac.buildPortableModel();
        SceneNode node = PortableModelBuilder.buildModel(portableModel, null);
        delegate.modelBuilt(new BuildResult(node.nativescenenode));
    }

    public static void addCreatorClass(String name, GeneralFunction<PortableModel, String> creator) {
        creatorClass.put(name, creator);
    }
}
