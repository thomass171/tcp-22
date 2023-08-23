package de.yard.threed.java2cs.testfiles;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Class comment
 */
public class Test1 extends Object {
    Logger logger = LoggerFactory.getLogger(Test1.class);
    public static Object lock1 = null; // static, null -> Swift 1.2 static class variable, nil
    public int x = 5;
    protected float f = 5.0f; // float literal
    public int a = 1, b = 2;
    float[] floatArray;
    Object[] objArray;
    volatile int testVolatile;
    final int finalgibtsincsnicht = 0;
    final static int constfuerswitch1 = 9;
    static final int constfuerswitch2 = 19;

    // Constructor (to convenience constructor)
    public Test1() {
        // this is the constructor body
        this(5.0, "foo"); // explicit constructor invocation
    }

    // Constructor with two args
    public Test1(double a, Object b) {
        a = Float.parseFloat("0.54");

        synchronized (b) {
            List<Node> oldlist = null;
        }
    }

    protected void run() {
        // this is the run body
        int a = 42; // local var
        this.a = b; // this reference
        new Test1(); // naked new instance creation
        Test1 test = new Test1(); // new instance creation
        float foo = (float) 42.0;
        float[] floatArray = new float[2];
        String s = null;
        Object[] objArray;
        Map<String, String> stringMap = new HashMap<String, String>();
        List<String> stringList = new ArrayList<String>();
        List<Float> floatList = new ArrayList<Float>();
        boolean[] ba = new boolean[3];

        for (int i = 0; i < 42; i++) {
            System.out.println("foo");
        }
        for (int i = 0; i < 42; i++) System.out.println("foo"); // missing braces
        int i = 42 + ba.length;
        while (i > 0) {
            System.out.println(i--);
        }
        if (a == 7685476) {
            throw new RuntimeException("foo"); // throw exception
        }
        List<Object> objectList = new ArrayList<Object>();
        for (Object obj : objectList) {
            System.out.println(obj);
        }

        String foo1 = null;
        if (foo1 instanceof String) {
            if (foo1.endsWith(".suffix")) {

            }
            int l = foo1.length();
        }
        switch (a) {
            case constfuerswitch1:
                a = testMethod2(32, new String[2]).length;
                break;
            case constfuerswitch2:
                a = floatArray.length;
                floatList.add(new Float(9f));
                break;
        }
       /* engine.Container c = new engine.Container();

        loop(image, width, height, new Handler() {
            public void handlePixel(BufferedImage image, int x, int y) {
                // Default ist medium gray
                int argb = (0xFF << 24) + (128 << 16) + (128 << 8) + 128;
                float d = distance(x, cx, y, cy);
                float radius = 40;
                if (d < radius) {
                    int c = (int) Math.round(127f * Math.cos((Math.PI / 2) * d / radius));
                    if (c > 127) {
                        c = 127;
                    }
                    System.out.println(d + ":" + c);
                    argb += (c << 16) + (c << 8) + c;

                }
                image.setRGB(x, y, argb);
            }
        });*/
    }

    static float testMethod1(int a, Object b) // static func two args
    {
        Class clazz = Test1.class;
        return 42.0f;
    }

    float[] testMethod2(final int a, String[] fa) {
        return new float[]{42.0f};
    }

    public void setName(String name) {
    }

    public String getName() {
        return "";
    }

    @Override
    public String toString() {
        return "zumstringgemacht";
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    class InnerClass {

    }
}

@Deprecated
class Foo implements MyInterface, Gee {
    @Override
    public void run1() {

    }

    @Override
    public void run() {
        Test1 t1 = new Test1();
    }

    /**
     * Es gibt keine throws declaration in C# (there are no checked exceptions in C#)
     */
    private void thrower() throws NichtGefundenException {
        throw new NichtGefundenException("");
    }

    public <Integer,String> void addFuture(List<Integer> future, AsyncJobDelegate<String> asyncJobDelegate) {
    }
}

interface MyInterface {
    public void run();
}

interface Gee {
    void run1();
    <T,D> void addFuture (List<T> future, AsyncJobDelegate<D> asyncJobDelegate);
}

interface Gee2 extends Gee {
    void run2();
}

final class fc {

}

enum NumericType {
    SHININESS,
    // Java beendet enums auch nicht mit ';'. Das ist dann ein leerer Body
    UNSHADED
}

class NichtGefundenException extends Exception {
    NichtGefundenException(String msg) {
        super(msg);
    }
}

class MaterialPool extends HashMap<String, Gee> {
    String[] sarr;
}

class Test1a extends Test1 implements MyInterface, Gee {
    List<MaterialPool>[] panelperslot;

    Test1a() {
        panelperslot = new List/*Unity <MaterialPool> Unity*/[3];
    }

    @Override
    public void run() {
    }

    @Override
    public void run1() {
    }

    @Override
    float[] testMethod2(final int a, String[] fa) {
        return new float[]{42.0f};
    }

    @Override
    public <Integer,String> void addFuture(List<Integer> future, AsyncJobDelegate<String> asyncJobDelegate) {
    }
}

abstract class AbstractClass {
    List<int[]> faces = new ArrayList<int[]>();

    protected abstract List<Test1a> find();

    abstract de.yard.threed.java2cs.testfiles.Test1a getLog(Class clazz);

    public abstract void asyncContentLoad(PlatformAsyncCallback callback, Node contentprovider, int page);
}

class ImageHelper {
    int[][] arr = new int[9][7];
    public static void loop(String image, int width, int height, PixelHandler handler) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                handler.handlePixel(image, x, y);
            }
        }
    }

    static public synchronized void initSystems() {

    }
}

class Node extends Test1 {
    @Override
    public void setName(String name) {
        super.setName(name);
        String s = super.getName();
        if (getName().equals("br")) {
            ImageHelper.loop("", 22, 33, (String image, int x, int y) -> {
                MaterialPool p = new MaterialPool();
            });
        }
        try {
        }
        catch (Exception se) {
        }
    }

    public void testMethoda(AsyncJobDelegate<String> asyncJobDelegate) {
        asyncJobDelegate.completed("rt");
    }
}

interface PlatformAsyncCallback<T> {
    void onSuccess(T result);

    void onFailure(T result);
}
