/*generated*/
using System;
using java.lang;
namespace de.yard.threed.java2cs.testfiles {

using org.apache.log4j;// import org.apache.log4j.Logger;
using org.junit;// import org.junit.Test;

using java.util;// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;


/**
 * Class comment
 */
public class Test1  :  Object {
    public Logger logger = Logger.getLogger(typeof(Test1));
    public static Object lock1 = null; // static, null -> Swift 1.2 static class variable, nil
    public int x = 5;
    protected float f = 5.0f; // float literal
    public int a = 1, b = 2;
    public float[] floatArray;
    public Object[] objArray;
    public volatile int testVolatile;
    public const int finalgibtsincsnicht = 0;
    public const  int constfuerswitch1 = 9;
    public  const int constfuerswitch2 = 19;

    // Constructor (to convenience constructor)
    public Test1() :
        // this is the constructor body
        this(5.0, "foo") { // explicit constructor invocation
    }

    // Constructor with two args
    public Test1(double a, Object b) {
        a = Float.parseFloat("0.54");

        lock (b) {
            List<Node> oldlist = null;
        }
    }

    virtual protected void run() {
        // this is the run body
        int a = 42; // local var
        this.a = b; // this reference
        new Test1(); // naked new instance creation
        Test1 test = new Test1(); // new instance creation
        float foo = (float) 42.0;
        float[] floatArray = new float[2];
        string s = null;
        Object[] objArray;
        Map<string, string> stringMap = new HashMap<string, string>();
        List<string> stringList = new ArrayList<string>();
        List<Nullable<Single>> floatList = new ArrayList<Nullable<Single>>();
        bool[] ba = new bool[3];

        for (int i = 0; i < 42; i++) {
            System.Console.WriteLine("foo");
        }
        for (int i = 0; i < 42; i++) System.Console.WriteLine("foo"); // missing braces
        int i = 42 + ba.Length;
        while (i > 0) {
            System.Console.WriteLine(i--);
        }
        if (a == 7685476) {
            throw new RuntimeException("foo"); // throw exception
        }
        List<Object> objectList = new ArrayList<Object>();
         foreach  (Object obj  in  objectList) {
            System.Console.WriteLine(obj);
        }

        string foo1 = null;
        if (foo1 is string) {
            if (foo1.endsWith(".suffix")) {

            }
            int l = foo1.length();
        }
        switch (a) {
            case constfuerswitch1:
                a = testMethod2(32, new String[2]).Length;
                break;
            case constfuerswitch2:
                a = floatArray.Length;
                floatList.add(new Nullable<Single>(9f));
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

    public static float testMethod1(int a, Object b) // static func two args
    {
        System.Type clazz = typeof(Test1);
        return 42.0f;
    }

    public float[] testMethod2( int a, string[] fa) {
        return new float[]{42.0f};
    }

    virtual public void setName(string name) {
    }

    virtual public string getName() {
        return "";
    }

    override
    public string ToString() {
        return "zumstringgemacht";
    }

    override
    public bool Equals(Object o) {
        return false;
    }

    virtual public  T parseJsonToModel<T>(string jsonstring, System.Type<T> clazz) {
        T model = null;
        return model;
    }

    virtual public  string modelToJson<T>(T model) {return "";}

     public class InnerClass {

    }
}

 public 
class Foo  :  MyInterface, Gee {
    virtual 
    public void run1() {

    }

    virtual 
    public void run() {
        Test1 t1 = new Test1();
        string s = t1.modelToJson<Foo>(new Foo());
    }

    /**
     * Es gibt keine throws declaration in C# (there are no checked exceptions in C#)
     */
    private void thrower()  {
        throw new NichtGefundenException("");
    }
}

 public interface MyInterface {
     void run();
}

 public interface Gee {
    void run1();
}

 public interface Gee2  :  Gee {
    void run2();
}

 public  class fc {

}

enum NumericType {
    SHININESS,
    // Java beendet enums auch nicht mit ';'. Das ist dann ein leerer Body
    UNSHADED
}

 public class NichtGefundenException  :  Exception {
     public NichtGefundenException(string msg) :
        base(msg) {
    }
}

 public class MaterialPool  :  HashMap<string, Gee> {
    public string[] sarr;
}

 public class Test1a  :  Test1  ,  MyInterface, Gee {
    public List<MaterialPool>[] panelperslot;

     public Test1a() {
        panelperslot = new List <MaterialPool> [3];
    }

    virtual 
    public void run() {
    }

    virtual 
    public void run1() {
    }

    public override
    float[] testMethod2( int a, string[] fa) {
        return new float[]{42.0f};
    }
}

 public abstract class AbstractClass {
    public List<int[]> faces = new ArrayList<int[]>();

    protected abstract List<Test1a> find();

    public abstract de.yard.threed.java2cs.testfiles.Test1a getLog(System.Type clazz);

    public abstract void asyncContentLoad<T>(PlatformAsyncCallback<T> callback, Node contentprovider, int page);
}

 public class ImageHelper {
    public int[][] arr = new int[9][7];
    public static void loop(string image, int width, int height, PixelHandler handler) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                handler(image, x, y);
            }
        }
    }
}

 public class Node  :  Test1 {
    override
    public void setName(string name) {
        base.setName(name);
        string s = base.getName();
        if (getName().Equals("br")) {
            ImageHelper.loop("", 22, 33, (string image, int x, int y) => {
                MaterialPool p = new MaterialPool();
            });
        }
        try {
        }
        catch (System.Exception se) {
        }
    }

    virtual 
    public void testMethoda(AsyncJobDelegate<string> asyncJobDelegate) {
        asyncJobDelegate("rt");
    }
}

 public interface PlatformAsyncCallback<T> {
    void onSuccess(T result);

    void onFailure(T result);
}
}