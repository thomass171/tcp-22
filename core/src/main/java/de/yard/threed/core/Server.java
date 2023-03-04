package de.yard.threed.core;

public class Server {

    public static final int DEFAULT_BASE_PORT = 5890;

    private String host;
    private int port;

    public Server(String server) {
        if (StringUtils.contains(server, ":")) {
            this.host = StringUtils.substringBefore(server, ":");
            this.port = Util.atoi(StringUtils.substringAfterLast(server, ":"));
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
}
