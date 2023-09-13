package de.yard.threed.engine.avatar;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;

import de.yard.threed.engine.apps.WoodenToyPmlFactory;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.loader.PortableModelDefinition;
import de.yard.threed.core.loader.PortableModelList;
import de.yard.threed.core.Color;


/**
 * Erzeugen von Vehicles als PortableModel Hierarchie.
 * Nachfolger von VehicleFactory, damit man speichern kann.
 *
 * 06.03.21: Vor allem, weil man damit nicht nur GLTF speichern kann, sondern über PortableModelList
 * auch ein 3D Model. Darum mal umbenannt: VehicleGltfFactory->VehiclePmlFactory
 *
 * Created by thomass on 24.12.18.
 */
public class VehiclePmlFactory {
    // Die Spurweite; Abstand der Räder Center. Bezugsgröße für alles.
    public final static float trackwidth = 1;

    /**
     * Baut eine Holzlokomotive. Alternativ vielleicht irgendwann mal Metall.
     * <p>
     * Erstreckt sich ueber die x-Achse (hier als "len" bezeichnet,negativ ist vorne), y geht nach oben, z in die Tiefe/Quer (hier als "width" bezeichnet).
     * Die Dimensionen brauchen nicht uebergeben werden. Denn
     * dafuer hat man ja den scale. Standardmass hier ist Meter.  HO ist dann der entsprechende Massstab.
     * y=0 ist die Wheelunterkante, also das, was später auf einem Gleis aufgesetzt wird. D.h., der Aufrufer positioniert es mit y=0 und die Unterkante
     * der Räder berührt die y=0 Ebene.
     * 29.10.2017: Die Dimensionierung soll auch VR geeignet sein. Da ist ein m Breite ganz gut, dann kann man sich links und rechts rauslehnen. Das Dach muss aber etwas höher,
     * damit man drin stehen kann. Aber 2m hoch geht nicht, das ist viel zu unmassig.
     *
     * @return
     */
    public static PortableModelList buildLocomotive() {
        // Grundelement (Absenkung vorne fehlt noch.
         float wheelwidth = 0.05f;

        float baselen = trackwidth * 2.5f;
        float baselen2 = baselen / 2;
        float baseheight = 0.3f;
        float baseheight2 = baseheight / 2;
        // Der Kessel
        float boilerlength = trackwidth * 1.5f;
        float boilerlength2 = boilerlength / 2;
        float boilerradius = trackwidth / 2.6f;
        // Der freie Platz vorm Boiler
        float boileroffset = 0.2f;
        float boilerxpos = -baselen2 + boileroffset + boilerlength2;
        // Wheels. Das Wheelcenter liegt auf Höhe der Base Unterkante.
        int WHEELCNT = 6;
        float wheelradius = 0.15f;
        float wheelwidth2 = wheelwidth / 2;
        float basewidth = trackwidth - wheelwidth;
        float basewidth2 = basewidth / 2;
        //
        float chimneyheight = trackwidth / 2;
        float chimneyheight2 = chimneyheight / 2;

        PortableMaterial woodmaterial = new PortableMaterial("BucheHell", "data:textures/gimp/wood/BucheHell.png");
        //TODO soll Phong sein,oder?
        PortableMaterial wheelmaterial = new PortableMaterial("wheelred", Color.RED);
        WoodenToyPmlFactory tbf = new WoodenToyPmlFactory();

        PortableModelDefinition baseblock = tbf.buildBlock(baselen, baseheight, trackwidth, woodmaterial.name);

        PortableModelDefinition boiler = tbf.buildCylinder(boilerradius, boilerlength, woodmaterial.name);
        boiler.setPosition(new Vector3(boilerxpos, trackwidth / 4f, 0));
        boiler.setRotation(Quaternion.buildQuaternionFromAngleAxis((new Degree(90)).toRad(), new Vector3(0, 1, 0)));
        baseblock.attach(boiler);

        // chimney auf den Boiler
        PortableModelDefinition chimney = tbf.buildChimney(chimneyheight, trackwidth * 0.1f, trackwidth * 0.2f, woodmaterial.name);
        // Boiler ist rotiert, darum sind Achsen anders.
        chimney.setPosition(new Vector3(0, boilerradius + chimneyheight2 - 0.05f, -boilerlength / 4));
        //chimney.getTransform().setParent(boiler.getTransform());
        boiler.attach(chimney);

        PortableModelDefinition[] wheel = new PortableModelDefinition[WHEELCNT];
        for (int i = 0; i < WHEELCNT; i++) {
            // die geraden links, die ungeraden rechts, vorne beginnen.
            int achse = i / 2;
            float zoffset = basewidth2 + wheelwidth2;
            if (i % 2 == 1) {
                // rechts bzw. hinteres z
                zoffset = -zoffset;
            }
            wheel[i] = tbf.buildWheel(wheelradius, wheelwidth, wheelmaterial.name);
            wheel[i].setPosition(new Vector3(-baselen2 + (achse + 1) * baselen / (WHEELCNT / 2 + 1), -baseheight2, zoffset));
            //wheel[i].getTransform().setParent(baseblock.getTransform());
            baseblock.attach(wheel[i]);
            wheel[i].setRotation(Quaternion.buildQuaternionFromAngleAxis((new Degree(90)).toRad(), new Vector3(1, 0, 0)));
        }

        // back ist das Häuschen hinten. 29.10.17: Wegen VR Häuschen höher
        float backheight = trackwidth * 0.7f;
        backheight = trackwidth * 0.9f;
        float backheight2 = backheight / 2;
        float backlen = trackwidth * 0.9f - 0.1f;
        float backlen2 = backlen / 2;
        float backwidth = trackwidth * 0.9f - 0.1f;
        float backwidth2 = backwidth / 2;
        float pfostenwidth = 0.1f;

        // Die Node back hat kein Mesh. Sie schliesst direkt an den Boiler an
        PortableModelDefinition back = new PortableModelDefinition();
        back.setPosition(new Vector3(boilerxpos + boilerlength / 2 + backlen2, backheight2 + baseheight2, 0));
        //back.getTransform().setParent(baseblock.getTransform());
        baseblock.attach(back);

        // Die Pfosten liegen ein bischen nach innen in back rein.
        PortableModelDefinition[] pfosten = new PortableModelDefinition[4];
        for (int i = 0; i < 4; i++) {
            pfosten[i] = tbf.buildBlock(backheight, pfostenwidth, pfostenwidth, woodmaterial.name);
            pfosten[i].setPosition(new Vector3((i < 2) ? backlen2 - pfostenwidth : -backlen2 + pfostenwidth, 0, (i % 2 == 0) ? backwidth2 - pfostenwidth : -backwidth2 + pfostenwidth));
            //pfosten[i].getTransform().setParent(back.getTransform());
            back.attach(pfosten[i]);
            //aufrichten
            pfosten[i].setRotation(Quaternion.buildQuaternionFromAngleAxis((new Degree(90)).toRad(), new Vector3(0, 0, 1)));
        }
        float roofheight = 0.15f;
        PortableModelDefinition roof = tbf.buildBlock(backlen, roofheight, backwidth, woodmaterial.name);
        roof.setPosition(new Vector3(0, backheight2, 0));
        //roof.getTransform().setParent(back.getTransform());
        back.attach(roof);

        PortableModelDefinition raiser = new PortableModelDefinition();
        //baseblock.getTransform().setParent(raiser.getTransform());
        raiser.attach(baseblock);
        baseblock.setPosition(new Vector3(0, baseheight2 + wheelradius, 0));
        raiser.setName("Locomotive");

        PortableModelList pml = new PortableModelList(null);
        pml.addModel(raiser);
        pml.addMaterial(woodmaterial);
        pml.addMaterial(wheelmaterial);
        return pml;
    }

    /**
     * einfach erstmal ein Cube.
     * 21.10.19
     *
     * @return
     */
    public static PortableModelList buildLocomotiveLowres() {
        float baselen = trackwidth;
        PortableMaterial woodmaterial = new PortableMaterial("BucheHell", "data:textures/gimp/wood/BucheHell.png");
        WoodenToyPmlFactory tbf = new WoodenToyPmlFactory();
        PortableModelDefinition baseblock = tbf.buildBlock(baselen, baselen, baselen, woodmaterial.name);
        PortableModelList pml = new PortableModelList(null);
        pml.addModel(baseblock);
        pml.addMaterial(woodmaterial);
        return pml;
    }

    /**
     * Baut ein Einsitzer Universalfahrzeug, eine Art FliWaTüt (Plastik oder Holz?). Etwa 2m lang und 1m breit. Als Strassenfahrzeug mit Rädern, kann aber auch fliegen wie ein Ufo
     * und sich auch im Maze bewegen.
     * <p>
     * Erstreckt sich ueber die x-Achse (hier als "len" bezeichnet) mit vorne im negativen, y geht nach oben, z in die Tiefe/Quer (hier als "width" bezeichnet).
     * Die Dimensionen brauchen nicht uebergeben werden. Denn
     * dafuer hat man ja den scale. Standardmass hier ist Meter.
     * trackwidth auch für ein Auto als Basis.
     * y=0 ist die Radunterkante, also das, was später auf einem Grund aufgesetzt wird. D.h., der Aufrufer positioniert es mit y=0 und die Unterkant
     * der Reifen berührt die y=0 Ebene.
     *
     * @return
     */
    public static PortableModelList buildMobi() {
         float wheelwidth = 0.05f;

        // Grundelement 
        float baselen = trackwidth * 2.5f;
        float baselen2 = baselen / 2;
        float baseheight = 0.3f;
        float baseheight2 = baseheight / 2;
        // Wheels. Das Wheelcenter liegt auf Höhe der Base Unterkante.
        int WHEELCNT = 4;
        float wheelradius = 0.15f;
        float wheelwidth2 = wheelwidth / 2;
        float basewidth = trackwidth - wheelwidth;
        float basewidth2 = basewidth / 2;
        //
        float chimneyheight = trackwidth / 2;
        float chimneyheight2 = chimneyheight / 2;

        PortableMaterial woodmaterial = new PortableMaterial("BucheHell", "data:textures/gimp/wood/BucheHell.png");
        //TODO soll Phong sein,oder?
        PortableMaterial wheelmaterial = new PortableMaterial("wheelred", Color.RED);
        // TODO kein Holz
        WoodenToyPmlFactory tbf = new WoodenToyPmlFactory();

        PortableModelDefinition baseblock = tbf.buildBlock(baselen, baseheight, trackwidth, woodmaterial.name);

        PortableModelDefinition[] wheel = new PortableModelDefinition[WHEELCNT];
        for (int i = 0; i < WHEELCNT; i++) {
            // die geraden links, die ungeraden rechts, vorne beginnen.
            int achse = i / 2;
            float zoffset = basewidth2 + wheelwidth2;
            if (i % 2 == 1) {
                // rechts bzw. hinteres z
                zoffset = -zoffset;
            }
            wheel[i] = tbf.buildWheel(wheelradius, wheelwidth,wheelmaterial.name);
            wheel[i].setPosition(new Vector3(-baselen2 + (achse + 1) * baselen / (WHEELCNT / 2 + 1), -baseheight2, zoffset));
            //wheel[i].getTransform().setParent(baseblock.getTransform());
            baseblock.attach(wheel[i]);
            wheel[i].setRotation(Quaternion.buildQuaternionFromAngleAxis(new Degree(90).toRad(), new Vector3(1, 0, 0)));
        }

        // back ist die Fahrgastzelle
        float backheight = trackwidth * 0.3f;
        float backheight2 = backheight / 2;
        float backlen = trackwidth * 0.9f - 0.1f;
        float backlen2 = backlen / 2;
        float backwidth = trackwidth * 0.9f - 0.1f;
        float backwidth2 = backwidth / 2;

        PortableModelDefinition drivercell = tbf.buildBlock(backlen, backheight, backwidth,woodmaterial.name);
        drivercell.setPosition(new Vector3(baselen2 / 3, backheight2 + baseheight2, 0));
        //drivercell.getTransform().setParent(baseblock.getTransform());
        baseblock.attach(drivercell);

        PortableModelDefinition raiser = new PortableModelDefinition();
        //baseblock.getTransform().setParent(raiser.getTransform());
        raiser.attach(baseblock);
        baseblock.setPosition(new Vector3(0, baseheight2 + wheelradius, 0));
        raiser.setName("Mobi");

        PortableModelList pml = new PortableModelList(null);
        pml.addModel(raiser);
        pml.addMaterial(woodmaterial);
        pml.addMaterial(wheelmaterial);
        return pml;
    }

    /**
     * Erstreckt sich ueber die x-Achse (hier als "len" bezeichnet,negativ ist vorne), y geht nach oben, z in die Tiefe/Quer (hier als "width" bezeichnet).
     * Die Dimensionen brauchen nicht uebergeben werden. Denn
     * dafuer hat man ja den scale. Standardmass hier ist Meter.
     * y=0 ist die Radunterkante, also das, was später auf Boden aufgesetzt wird. D.h., der Aufrufer positioniert es mit y=0 und die Unterkante
     * der Räder berührt die y=0 Ebene.
     * Ich leg x=0,y=0 mal in die Nabe.
     *
     * Naja, schön ist es nicht. Und der Sattel fehlt noch.
     * 16.5.19
     *
     * @return
     */
    public static PortableModelList buildBike() {
        float wheelwidth = 0.04f;
        int WHEELCNT = 2;
        float wheelradius = 0.30f;
        float thinbeamradius = 0.01f;
        float thickbeamradius = 0.02f;
        float gabelwidth = wheelwidth + thinbeamradius + 0.02f;
        float mainbeamlen = 0.5f;
        float wheeloffset = wheelradius + 0.1f;

        Degree baseangle = new Degree(60);
        float saddleheight = (float) (Math.sin(baseangle.toRad())*wheeloffset);

        PortableMaterial woodmaterial = new PortableMaterial("BucheHell", "data:textures/gimp/wood/BucheHell.png");
        //TODO soll Phong sein,oder?
        PortableMaterial wheelmaterial = new PortableMaterial("wheelred", Color.RED);
        WoodenToyPmlFactory tbf = new WoodenToyPmlFactory();

        // baseblock ist die Nabe
        PortableModelDefinition baseblock = tbf.buildBlock(0.03f, 0.03f, 0.1f, woodmaterial.name);

        PortableModelDefinition gabelback = tbf.buildGabel(wheeloffset, thinbeamradius, gabelwidth, woodmaterial.name);
        baseblock.attach(gabelback);

        PortableModelDefinition mainbeam = tbf.buildBeam(mainbeamlen, thickbeamradius,  woodmaterial.name);
        mainbeam.setRotation( Quaternion.buildRotationZ(new Degree(45)));
        baseblock.attach(mainbeam);

        // Der muss mit den beiden Gabeln ein gleichschenkliges Dreieck bilden
        PortableModelDefinition mainbeamback = tbf.buildBeam(wheeloffset, thickbeamradius,  woodmaterial.name);
        mainbeamback.setRotation( Quaternion.buildRotationZ(new Degree(120)));
        baseblock.attach(mainbeamback);

        PortableModelDefinition gabelback2 = tbf.buildGabel(wheeloffset, thinbeamradius, gabelwidth, woodmaterial.name);
        gabelback2.setPosition(new Vector3(-wheeloffset/2, saddleheight, 0));
        gabelback2.setRotation( Quaternion.buildRotationZ(baseangle));
        baseblock.attach(gabelback2);

        //der ganze Frontteil mit Rad, Gabel und Lenker, die alle miteinander drehbar sind.
        PortableModelDefinition front = new PortableModelDefinition();
        PortableModelDefinition gabelfront = tbf.buildGabel(wheeloffset, thinbeamradius, gabelwidth, woodmaterial.name);
        //gabelback2.setPosition(new Vector3(-wheeloffset/2, saddleheight, 0));
        gabelfront.setRotation( Quaternion.buildRotationZ(new Degree(180)));
        front.attach(gabelfront);
        PortableModelDefinition frontnode = tbf.buildBeam(0.1f, thickbeamradius,  woodmaterial.name);
        frontnode.setPosition(new Vector3(-0.08f, 0, 0));
        front.attach(frontnode);
        PortableModelDefinition handlebar = tbf.buildBeam(0.5f, thinbeamradius,  woodmaterial.name);
        handlebar.setRotation( Quaternion.buildRotationY(new Degree(90)));
        handlebar.setPosition(new Vector3(-0.1f, 0, 0.25f));
        front.attach(handlebar);

        // front ans Ende des mainbeam
        front.setPosition(new Vector3(mainbeamlen, 0, 0));
        front.setRotation( Quaternion.buildRotationZ(new Degree(-110)));
        mainbeam.attach(front);

        for (int i = 0; i < WHEELCNT; i++) {
            PortableModelDefinition wheel;
            wheel = tbf.buildWheel(wheelradius, wheelwidth, wheelmaterial.name);
            wheel.setPosition(new Vector3(wheeloffset * ((i == 0) ? -1 : 1), 0, 0));
            wheel.setRotation(Quaternion.buildQuaternionFromAngleAxis((new Degree(90)).toRad(), new Vector3(1, 0, 0)));
            //wheel[i].getTransform().setParent(baseblock.getTransform());
            if (i==0) {
                //hinten
                baseblock.attach(wheel);
            }else{
                //vorne
                front.attach(wheel);
            }

        }

        PortableModelDefinition raiser = new PortableModelDefinition();
        raiser.attach(baseblock);
        // baseblock.setPosition(new Vector3(0, baseheight2 + wheelradius, 0));
        raiser.setName("Bike");

        PortableModelList pml = new PortableModelList(null);
        pml.addModel(raiser);
        pml.addMaterial(woodmaterial);
        pml.addMaterial(wheelmaterial);
        return pml;
    }

    public static float getTrackwidth() {
        return trackwidth;
    }

    /**
     * Erstmal nur so ungefaehr.
     *
     * @return
     */
    public static Vector3 getPilotPosition() {
        return new Vector3(trackwidth, trackwidth, 0);
    }
}
