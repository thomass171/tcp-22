package de.yard.threed.platform.jme;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLocator;
import com.jme3.asset.AssetManager;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.platform.common.ShaderUtil;
import de.yard.threed.javacommon.FileReader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;


/**
 * 3.5.19: Allgemeine custom shader (z.B. Photoalbum) liegen in bundle core. Da ist das mit rootpath aeusserst fragwürdig?
 * Es koennte zufällig gehen, weil die Dateien auch im FS liegen. Und es gibt auch JME spezifische custom shader (MYLighting),
 * die liegen nur in JME im FS. Da ist etwas nicht rund?
 *
 * Created by thomass on 23.12.15.
 */
public class JmeShaderLocator implements AssetLocator {
    Log logger = Platform.getInstance().getLog(JmeShaderLocator.class);
    private String rootpath;

    public JmeShaderLocator() {

    }

    @Override
    public void setRootPath(String s) {
        rootpath = s;
    }

    /**
     * @param assetManager
     * @param assetKey
     * @return
     */
    @Override
    public AssetInfo locate(AssetManager assetManager, final AssetKey assetKey) {
        final String assetkey = assetKey.getName();
        logger.debug("locate: " + assetkey + " with root path " + rootpath);
        if (!assetkey.endsWith(".vert") && !assetkey.endsWith(".frag")) {
            // cannot be located by this locator
            return null;
        }

        AssetInfo ai = new AssetInfo(assetManager, assetKey) {
            @Override
            public java.io.InputStream openStream() {
                try {
                    String source;
                    String loc = /*17.3.16 rootpath + "/" +*/ assetkey.substring(JmeResourceManager.RESOURCEPREFIX.length()+1);
                    logger.debug("loading shader from " + loc);
                    InputStream is = FileReader.getFileStream(new BundleResource(loc));
                    byte[] bytebuf;
                    bytebuf = FileReader.readFully(is);
                    source = new String(bytebuf, "UTF-8");
                    //HashMap<String,String> translatemap = new HashMap<String, String>();
                    source = ShaderUtil.preprocess(source/*,translatemap*/);
                    if (assetkey.endsWith(".vert")) {
                        source = "uniform mat4 g_ProjectionMatrix;\n" +
                                "uniform mat4 g_WorldViewMatrix;\n" +
                                "attribute vec3 inPosition;\n" +
                                "attribute vec3 inNormal;\n" +
                                "attribute vec2 inTexCoord;\n" +
                                "uniform mat4 g_NormalMatrix;\n" +
                                source;
                        //source = source.replaceAll("MODELVIEWPROJECTIONMATRIX","g_WorldViewProjectionMatrix");
                        source = source.replaceAll("PROJECTIONMATRIX", "g_ProjectionMatrix");
                        source = source.replaceAll("MODELVIEWMATRIX", "g_WorldViewMatrix");
                        source = source.replaceAll("VERTEX", "inPosition");
                        source = source.replaceAll("MULTITEXCOORD0", "inTexCoord");
                        source = source.replaceAll("NORMALMATRIX", "mat3(g_NormalMatrix)");
                        source = source.replaceAll("NORMAL", "inNormal");
                        source = source.replaceAll("OUT", "varying");
                    }
                    if (assetkey.endsWith(".frag")) {
                        source = source.replaceAll("FRAGCOLOR", "gl_FragColor");
                        source = source.replaceAll("TEXTURE2D", "texture2D");
                        source = source.replaceAll("IN", "varying");
                    }
                    // Die ungeeignete JME Nomenklatur mit Material uniform prefix "m_" beachten
                    // 27.4.16: Das ist wirklich reichlich nervend.
                    source = source.replaceAll("isunshaded","m_isunshaded");
                    source = source.replaceAll("texture0","m_texture0");
                    source = source.replaceAll("texture1","m_texture1");

                    return new ByteArrayInputStream(source.getBytes("UTF-8"));
                } catch (Exception e) {
                    //TODO Fehlerhandling
                    e.printStackTrace();
                    return null;
                }
            }
        };
        return ai;
    }


}
