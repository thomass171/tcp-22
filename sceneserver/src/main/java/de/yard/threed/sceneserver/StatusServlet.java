package de.yard.threed.sceneserver;

import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.javanative.JsonUtil;
import de.yard.threed.sceneserver.jsonmodel.Entity;
import de.yard.threed.sceneserver.jsonmodel.Status;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.HttpHeaders;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class StatusServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Status status = new Status();

        ClientListener clientListener = ClientListener.getInstance();
        if (clientListener != null) {
            status.setClients(clientListener.getClients());
        } else {
            status.setClients(new ArrayList<>());
        }
        status.setCpuload(getProcessCpuLoad());
        if (SystemManager.isinited){
            //  SystemManager is MT safe.
            List<EcsEntity> el = SystemManager.findEntities(null);
            status.setEntities(new ArrayList<>());
            for (EcsEntity e:el){
                status.getEntities().add(Entity.fromEcs(e));
            }
        }

        response.setContentType("application/json");
        // There is no preflight for GET, so this header should be sufficient?
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,"*");
        response.setStatus(HttpServletResponse.SC_OK);
        //response.getWriter().println("{ \"status\": \"ok\"}");
        response.getWriter().println(JsonUtil.toJson(status));
    }

    /**
     * Own(!) processes load.
     * <p>
     * From https://stackoverflow.com/questions/37985719/spring-boot-actuator-to-give-cpu-usage
     */
    public Double getProcessCpuLoad() {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        AttributeList list;
        try {
            ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
            list = mbs.getAttributes(name, new String[]{"ProcessCpuLoad"});
        } catch (Exception e) {
            log.error("getProcessCpuLoad() failed", e);
            return null;
        }

        if (list.isEmpty()) {
            log.error("getProcessCpuLoad(): no list");
            return null;
        }

        Attribute att = (Attribute) list.get(0);
        Double value = (Double) att.getValue();

        // usually takes a couple of seconds before we get real values
        //?if (value == -1.0)      return Double.NaN;
        // returns a percentage value with 1 decimal point precision
        log.debug("raw cpu load: {}", value);
        return ((int) (value * 1000) / 10.0);
    }
}
