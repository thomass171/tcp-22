package de.yard.threed.core;

public class Server {

    public static final int DEFAULT_BASE_PORT = 5890;

    private String host;
    private int port;
    // path might be useful for websockets hehind reverse proxies. Might be null to use default "/connect"
    private String path = null;

    public Server(String server) {
        if (StringUtils.contains(server, ":")) {
            String[] parts = StringUtils.split(server, ":");
            this.host = parts[0];
            this.port = Util.atoi(parts[1]);
            if (parts.length > 2) {
                this.path = parts[2];
            }
        } else {
            this.host = server;
            this.port = DEFAULT_BASE_PORT;
        }
    }

    public Server(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPath() {
        return path;
    }
}
