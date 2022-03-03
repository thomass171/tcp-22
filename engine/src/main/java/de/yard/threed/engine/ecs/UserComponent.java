package de.yard.threed.engine.ecs;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

/**
 * <p>
 * Created by thomass on 24.1.22.
 */
public class UserComponent extends EcsComponent {
    Log logger = Platform.getInstance().getLog(UserComponent.class);

    public static String TAG = "UserComponent";

    private String username;

    public UserComponent(String username) {
        this.username = username;
    }

    @Override
    public String getTag() {
        return "UserComponent";
    }

    public static UserComponent getUserComponent(EcsEntity e) {
        UserComponent uc = (UserComponent) e.getComponent(UserComponent.TAG);
        return uc;
    }

    public String getUsername() {
        return username;
    }
}
