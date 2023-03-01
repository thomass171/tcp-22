package de.yard.threed.sceneserver;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.ServerContainer;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

@Slf4j
public class JettyServer {

    /**
     * Exceptions while starting the server should be handled by main.
     */
    public static Server startJettyServer(int port) throws Exception {
        log.info("Starting jetty on port {}", port);

        Server server = new Server();

        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.setConnectors(new Connector[]{connector});

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        // other handler have a addServletWithMapping() method
        context.addServlet(StatusServlet.class, "/status");

        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(new Handler[]{context, new DefaultHandler()});
        server.setHandler(handlers);

        // Initialize javax.websocket layer
        ServerContainer wscontainer = WebSocketServerContainerInitializer.configureContext(context);
        wscontainer.addEndpoint(ToUpperWebsocket.class);


        server.start();
        // join blocks until server terminates
        //server.join();

        return server;
    }
}

