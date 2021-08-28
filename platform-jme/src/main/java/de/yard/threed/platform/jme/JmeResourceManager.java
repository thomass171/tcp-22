package de.yard.threed.platform.jme;

import com.jme3.asset.AssetManager;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.javacommon.DefaultResourceReader;

/**
 * Ein Singleton wie der AssetManager bei JME.
 * <p/>
 * Created by thomass on 06.06.15.
 */
public class JmeResourceManager extends DefaultResourceReader/*5.8.21 JAResourceManager*/ {
    // Der Prefix ist erforderlich, damit JME die Resource nicht findet und einen Custom Loader aufruft.
    // 2.5.19: Statt "xxx" (wofuer das auch war), ein vernuenftiges "customJME".
    public static final String RESOURCEPREFIX = "customJME";//"xxx";//19.3.16 "src/main/resources";
    Log logger = Platform.getInstance().getLog(JmeResourceManager.class);

    // 11.12.15: Der am kann jetzt auch null sein, um z.B. normale Dateien oder auch autotests
    // machen zu koennen. Die startke Fokussierung auf den AM ist eh nicht so gut.
    public static AssetManager am;

    public JmeResourceManager(AssetManager am) {
        this.am = am;
        if (am != null) {
            //2.5.19: Erst irgendwo suchen und nur wenn das nichts findet,einen Effect bauen. Sonst scheitert er vorher schon im Effect bauen.
            //Nee, braucht nicht. JmeFileLocator nutzen.
            am.registerLocator(RESOURCEPREFIX, JmeFileLocator.class);
            am.registerLocator(RESOURCEPREFIX, JmeEffectLocator.class);
            am.registerLocator(RESOURCEPREFIX, JmeShaderLocator.class);
            // wenn die ersten zwei Locator nichts finden, mit dem JME Standard
            // FileLocator versuchen. Dürfte dann z.B. für Images  greifen.
            //19.3.16 brahcts den?am.registerLocator(null, JmeFileLocator.class);

        }
    }

    /**
     * Spezieller JME init fuer den Assetmanager
     *
     * @param am
     */
   /*5.8.21 public static JmeResourceManager init(AssetManager am) {
        JAResourceManager.instance = new JmeResourceManager(am);
        return (JmeResourceManager) JAResourceManager.instance;
    }*/


}
