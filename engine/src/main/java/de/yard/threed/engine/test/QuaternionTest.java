package de.yard.threed.engine.test;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.testutil.TestUtil;



/**
 * Date: 27.08.14
 */
public class QuaternionTest {
    public void testNormalize() {
        Quaternion q = new Quaternion(1, 2, 3,4);
        q = q.normalize();
        double norm = q.getX()*q.getX()+q.getY()*q.getY()+q.getZ()*q.getZ()+q.getW()*q.getW();
        TestUtil.assertEquals("norm", 1, norm);
        //Referenzwerte aus JME
        TestUtil.assertEquals("x", 0.182574f, q.getX());
        TestUtil.assertEquals("y", 0.36514837f, q.getY());
        TestUtil.assertEquals("z", 0.5477226f, q.getZ());
        TestUtil.assertEquals("w", 0.73029673f, q.getW());
    }

    public void testAngles() {
        Degree pitch = new Degree(0);
        Degree yaw = new Degree(0);
        Degree roll = new Degree(0);
        Quaternion q =  Quaternion.buildFromAngles(pitch, yaw, roll);
        TestUtil.assertEquals("x1", 0, q.getX());
        TestUtil.assertEquals("y1", 0, q.getY());
        TestUtil.assertEquals("z1", 0, q.getZ());
        TestUtil.assertEquals("w1", 1, q.getW());
        double[] angles = new double[3];
        q.toAngles(angles);
        TestUtil.assertEquals("pitch1", 0, (float) Degree.buildFromRadians(angles[0]).getDegree());
        TestUtil.assertEquals("yaw1", 0, (float) Degree.buildFromRadians(angles[1]).getDegree());
        TestUtil.assertEquals("roll1", 0, (float) Degree.buildFromRadians(angles[2]).getDegree());

        pitch = new Degree(30);
        yaw = new Degree(45);
        roll = new Degree(60);
        q =  Quaternion.buildFromAngles(pitch, yaw, roll);
        /*Zur Ermittlung der Referenzwerte.
        com.jme3.math.Quaternion q1 = new com.jme3.math.Quaternion().fromAngles(pitch.toRad(), yaw.toRad(), roll.toRad());
        TestUtil.assertEquals("x1", 0.3919f, q1.getX(), TestUtil.floattesttolerance);
        TestUtil.assertEquals("y1", 0.439679f, q1.getY(), TestUtil.floattesttolerance);
        TestUtil.assertEquals("z1", 0.3604234f, q1.getZ(), TestUtil.floattesttolerance);
        TestUtil.assertEquals("w1", 0.72331f, q1.getW(), TestUtil.floattesttolerance);
        In Unity ergeben sich andere Werte, evtl eil Unity left handed ist. Unity hat die ORder ZXY:
        q.x=0.3919038
        q.y=0.2005621
        q.z=0.3604234
        q.w=0.8223631
        */
        TestUtil.assertEquals("x1", 0.3919f, q.getX());
        TestUtil.assertEquals("y1", 0.439679f, q.getY());
        TestUtil.assertEquals("z1", 0.3604234f, q.getZ());
        TestUtil.assertEquals("w1", 0.72331f, q.getW());
        angles = new double[3];
        q.toAngles(angles);
        TestUtil.assertEquals("pitch2", 30, (float) Degree.buildFromRadians(angles[0]).getDegree());
        TestUtil.assertEquals("yaw2", 45, (float) Degree.buildFromRadians(angles[1]).getDegree());
        TestUtil.assertEquals("roll2", 60, (float) Degree.buildFromRadians(angles[2]).getDegree());

    }

    public void testVectorRotation() {
        for (Vector3 from : new Vector3[]{new Vector3(1, 0, 0), new Vector3(1, 1, 0)}) {
            for (Vector3 to : new Vector3[]{new Vector3(0, 0, 1), new Vector3(1, 0, 1)}) {
                Quaternion q = Quaternion.buildQuaternion(from, to);
                Vector3 result = from.rotate(q).normalize();
                // die normalisierten verglechen, sonst ist das nicht zuverlaessig
                Vector3 tonorm = to.clone().normalize();
                //8.8.15 TODO assertVector3("Rotation " + from + "to " + tonorm, tonorm, result);
                //29.11.16: In MathTest gibt es auchnen Test dazu.
            }
        }

    }
    
    /**
     * Die ..., um den Punkt (1,0,0) um 45 Grad auf (0.70710677f,0,-0.70710677f)
     * zu verschieben und entsprechend zu rotieren.
     * Sowas ist Teil der Shape Extrusion.
     */
    public void testVectorRotationForExtrusion() {
        Vector3 extrudedirection = new Vector3(0, 0, -1);        
        Vector3 pathtangent = new Vector3(-1, 0, -1);
       // System.out.println("pathtangent=" + pathtangent.dump("\n"));
        Quaternion pathrotation = Vector3.getRotation(extrudedirection, pathtangent);
       // System.out.println("pathtangent=" + pathtangent.dump("\n") + ",pathrotation=" + pathrotation.dump("\n"));
        // Die Rotation muss 45 Grad um y sein
        double[] angles = new double[3];
        pathrotation.toAngles(angles);
        TestUtil.assertEquals("x-rot", 0, (float) Degree.buildFromRadians(angles[0]).getDegree());
        TestUtil.assertEquals("y-rot", 45, (float) Degree.buildFromRadians(angles[1]).getDegree());
        TestUtil.assertEquals("z-rot", 0, (float) Degree.buildFromRadians(angles[2]).getDegree());
        // Zweite Probe ueber anderen Weg
        Quaternion rot45y =  Quaternion.buildFromAngles(new Degree(0),new Degree(45),new Degree(0));
        rot45y.toAngles(angles);
        TestUtil.assertEquals("x-rot", 0, (float) Degree.buildFromRadians(angles[0]).getDegree());
        TestUtil.assertEquals("y-rot", 45, (float) Degree.buildFromRadians(angles[1]).getDegree());
        TestUtil.assertEquals("z-rot", 0, (float) Degree.buildFromRadians(angles[2]).getDegree());
     //   System.out.println("rot45y=" + rot45y.dump("\n"));
        //28.8.15: Der direkte Vergleich zwischen den Qs scheitert, obwohl beide
        //anscheinend denselben Winkel repraesentieren. Sehr strange.
        //assertQuaternion("transformedsource", rot45y, pathrotation);
    }

}
