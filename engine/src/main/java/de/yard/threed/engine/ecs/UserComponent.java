package de.yard.threed.engine.ecs;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

/**
 * Only for real users that logged in. Not for bots.
 *
 * <p>
 * Created by thomass on 24.1.22.
 */
public class UserComponent extends EcsComponent {
    Log logger = Platform.getInstance().getLog(UserComponent.class);

    public static String TAG = "UserComponent";

    private String username;
    private String connectionId;

    public UserComponent(String username, String connectionId) {
        this.username = username;
        this.connectionId = connectionId;
    }

    public UserComponent(String username) {
        this.username = username;
        this.connectionId = null;
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

    /**
     * Will be null in local mode.
     */
    public String getConnectionId() {
        return connectionId;
    }
}
