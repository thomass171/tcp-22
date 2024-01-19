package de.yard.threed.tools;

import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.buffer.SimpleByteBuffer;
import de.yard.threed.core.platform.*;
import de.yard.threed.core.buffer.NativeByteBuffer;

import java.io.StringReader;
import de.yard.threed.core.XmlException;
import de.yard.threed.javacommon.DefaultJavaStringHelper;
import de.yard.threed.javacommon.JALog;
import de.yard.threed.core.JavaStringHelper;
import de.yard.threed.javacommon.JavaXmlDocument;
import de.yard.threed.javacommon.SimpleJsonObject;
import de.yard.threed.javacommon.Util;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.HashMap;

/**
 * 10.9.21:TODO check: Why not SimpleHeadless?
 * 18.9.23: deprecated in favor of SimpleHeadlessPlatformFactory
 * Created on 10.12.18.
 */
@Deprecated
public class ToolsPlatform extends DefaultPlatform {
    NativeLogFactory logfactory;
    HashMap<String, String> properties = new HashMap<String, String>();


    private ToolsPlatform(/*ResourceManager resourceManager,*/ NativeLogFactory logfactory) {
        //this.resourcemanager = resourceManager;
        this.logfactory = logfactory;
        StringUtils.init(buildStringHelper());
        //matlib braucht bundles

        String hostdir = System.getProperty("HOSTDIR");
        if (hostdir == null) {
            hostdir = System.getenv("HOSTDIR");
            if (hostdir == null) {
                throw new RuntimeException("HOSTDIR not set");
            }
        }
    }

    public static Platform init(/*, HashMap<String, String> properties*/) {
        //System.out.println("PlatformOpenGL.init");
        //if (instance == null || !(instance instanceof PlatformOpenGL)) {
        /*for (String key : properties.keySet()) {
            //System.out.println("transfer of propery "+key+" to system");
            System.setProperty(key, properties.get(key));
        }*/

      
        instance = new ToolsPlatform(/*resourceManager, */new NativeLogFactory() {
            @Override
            public Log getLog(Class clazz) {
                return new JALog(clazz);
            }
        });
        //defaulttexture wird spaeter "irgendwo" geladen
        return instance;
    }

    @Override
    public Log getLog(Class clazz) {
        return logfactory.getLog(clazz);

    }

    @Override
    public NativeByteBuffer buildByteBuffer(int size) {
        SimpleByteBuffer buf = new SimpleByteBuffer(new byte[size]);
        return buf;
    }

    @Override
    public NativeJsonValue parseJson(String jsonstring) {
        GsonBuilder builder = new GsonBuilder();
        LinkedTreeMap map = (LinkedTreeMap) builder.create().fromJson(jsonstring, Object.class);
        return new SimpleJsonObject(map);
    }

    @Override
    public NativeDocument parseXml(String xmltext) throws XmlException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(xmltext)));
            return new JavaXmlDocument(doc);
        } catch (Exception e) {
            throw new XmlException(e);
        }
    }

    @Override
    public float getFloat(byte[] buf, int offset) {
        return Util.getFloat(buf, offset);
    }

    @Override
    public void setFloat(byte[] buf, int offset, float f) {
        Util.setFloat(buf,offset,f);
    }

    @Override
    public double getDouble(byte[] buf, int offset) {
        return Util.getDouble(buf, offset);
    }

    @Override
    public NativeStringHelper buildStringHelper() {
        return new DefaultJavaStringHelper();
    }
}

    
