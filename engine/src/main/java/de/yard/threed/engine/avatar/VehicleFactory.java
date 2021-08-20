package de.yard.threed.engine.avatar;

import de.yard.threed.core.Degree;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.Vector3;
import de.yard.threed.engine.apps.WoodenToyFactory;


/**
 * Erzeugen von Vehicles als SceneModel Hierarchie.
 * 19.9.19: Eigentlich doch deprecated zugunsten von VehiclePmlFactory, weil man nicht speichern kann?
 * 06.03.21: Vor allem, weil man damit nicht nur GLTF speichern kann, sondern über PortableModelList
 * auch ein 3D Model.
 *
 * Created by thomass on 24.11.16.
 */
@Deprecated
public class VehicleFactory {
    // Die Spurweite; Abstand der Räder Center. Bezugsgröße für alles.
    public final static float trackwidth = 1;
    public final static float wheelwidth = 0.05f;

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
     * 28.12.18: Deprecated zugiunsten der GLTF Variante. Die Methode hier soll dann mal ganz weg.
     * @return
     */
    @Deprecated
    public static SceneNode buildLocomotive() {
        // Grundelement (Absenkung vorne fehlt noch.
        float baselen = trackwidth * 2.5f;
        float baselen2 = baselen / 2;
        float baseheight = 0.3f;
        float baseheight2 = baseheight / 2;
        // Der Kessel
        float boilerlength = trackwidth*1.5f;
        float boilerlength2 = boilerlength/2;
        float boilerradius = trackwidth / 2.6f;
        // Der freie Platz vorm Boiler
        float boileroffset = 0.2f;
        float boilerxpos = -baselen2 + boileroffset + boilerlength2 ;
        // Wheels. Das Wheelcenter liegt auf Höhe der Base Unterkante.
        int WHEELCNT = 6;
        float wheelradius = 0.15f;
        float wheelwidth2 = wheelwidth / 2;
        float basewidth = trackwidth - wheelwidth;
        float basewidth2 = basewidth / 2;
        //
        float chimneyheight = trackwidth/2;
        float chimneyheight2 = chimneyheight/2;

        WoodenToyFactory tbf = new WoodenToyFactory();

        SceneNode baseblock = tbf.buildBlock(baselen, baseheight, trackwidth);

        SceneNode boiler = tbf.buildCylinder(boilerradius, boilerlength);
        boiler.getTransform().setPosition(new Vector3(boilerxpos, trackwidth / 4f, 0));
        boiler.getTransform().rotateOnAxis(new Vector3(0, 1, 0), new Degree(90));
        baseblock.attach(boiler);
        
        // chimney auf den Boiler
        SceneNode chimney = tbf.buildChimney(chimneyheight,trackwidth*0.1f,trackwidth*0.2f);
        // Boiler ist rotiert, darum sind Achsen anders.
        chimney.getTransform().setPosition(new Vector3(0, boilerradius+chimneyheight2-0.05f, -boilerlength/4));
        chimney.getTransform().setParent(boiler.getTransform());

        SceneNode[] wheel = new SceneNode[WHEELCNT];
        for (int i = 0; i < WHEELCNT; i++) {
            // die geraden links, die ungeraden rechts, vorne beginnen.
            int achse = i / 2;
            float zoffset = basewidth2 + wheelwidth2;
            if (i % 2 == 1) {
                // rechts bzw. hinteres z
                zoffset = -zoffset;
            }
            wheel[i] = tbf.buildWheel(wheelradius, wheelwidth);
            wheel[i].getTransform().setPosition(new Vector3(-baselen2 + (achse + 1) * baselen / (WHEELCNT/2+1), -baseheight2, zoffset));
            wheel[i].getTransform().setParent(baseblock.getTransform());
            wheel[i].getTransform().rotateOnAxis(new Vector3(1, 0, 0), new Degree(90));
        }

        // back ist das Häuschen hinten. 29.10.17: Wegen VR Häuschen höher
        float backheight = trackwidth * 0.7f;
         backheight = trackwidth * 0.9f;
        float backheight2 = backheight/2;
        float backlen = trackwidth*0.9f - 0.1f;
        float backlen2 = backlen / 2;
        float backwidth = trackwidth*0.9f - 0.1f;
        float backwidth2 = backwidth / 2;
        float pfostenwidth = 0.1f;

        // Die Node back hat kein Mesh. Sie schliesst direkt an den Boiler an
        SceneNode back = new SceneNode();
        back.getTransform().setPosition(new Vector3(boilerxpos+boilerlength/2+backlen2, backheight2+baseheight2, 0));
        back.getTransform().setParent(baseblock.getTransform());

        // Die Pfosten liegen ein bischen nach innen in back rein.
        SceneNode[] pfosten = new SceneNode[4];
        for (int i = 0; i < 4; i++) {
            pfosten[i] = tbf.buildBlock(backheight, pfostenwidth, pfostenwidth);
            pfosten[i].getTransform().setPosition(new Vector3((i < 2) ? backlen2-pfostenwidth : -backlen2+pfostenwidth, 0, (i % 2 == 0) ? backwidth2-pfostenwidth : -backwidth2+pfostenwidth));
            pfosten[i].getTransform().setParent(back.getTransform());
            //aufrichten
            pfosten[i].getTransform().rotateOnAxis(new Vector3(0, 0, 1), new Degree(90));
        }
        float roofheight = 0.15f;
        SceneNode roof = tbf.buildBlock(backlen, roofheight, backwidth);
        roof.getTransform().setPosition(new Vector3(0, backheight2, 0));
        roof.getTransform().setParent(back.getTransform());

        SceneNode raiser = new SceneNode();
        baseblock.getTransform().setParent(raiser.getTransform());
        baseblock.getTransform().setPosition(new Vector3(0, baseheight2 + wheelradius, 0));
        raiser.setName("Locomotive");
        return raiser;
    }

    /**
     * Baut ein Platikauto.
     * <p>
     * Erstreckt sich ueber die x-Achse (hier als "len" bezeichnet) mit vorne im negativen, y geht nach oben, z in die Tiefe/Quer (hier als "width" bezeichnet).
     * Die Dimensionen brauchen nicht uebergeben werden. Denn
     * dafuer hat man ja den scale. Standardmass hier ist Meter.  
     * trackwidth auch für ein Auto als Basis.
     * y=0 ist die Radunterkante, also das, was später auf einem Grund aufgesetzt wird. D.h., der Aufrufer positioniert es mit y=0 und die Unterkant
     * der Reifen berührt die y=0 Ebene.
     *
     * 20.9.19: Siehe VehiclePmlFactory.buildMobi().
     *
     * @return
     */
    @Deprecated
    public static SceneNode buildCar() {
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
        float chimneyheight = trackwidth/2;
        float chimneyheight2 = chimneyheight/2;

        // TODO kein Holz
        WoodenToyFactory tbf = new WoodenToyFactory();

        SceneNode baseblock = tbf.buildBlock(baselen, baseheight, trackwidth);
        
        SceneNode[] wheel = new SceneNode[WHEELCNT];
        for (int i = 0; i < WHEELCNT; i++) {
            // die geraden links, die ungeraden rechts, vorne beginnen.
            int achse = i / 2;
            float zoffset = basewidth2 + wheelwidth2;
            if (i % 2 == 1) {
                // rechts bzw. hinteres z
                zoffset = -zoffset;
            }
            wheel[i] = tbf.buildWheel(wheelradius, wheelwidth);
            wheel[i].getTransform().setPosition(new Vector3(-baselen2 + (achse + 1) * baselen / (WHEELCNT/2+1), -baseheight2, zoffset));
            wheel[i].getTransform().setParent(baseblock.getTransform());
            wheel[i].getTransform().rotateOnAxis(new Vector3(1, 0, 0), new Degree(90));
        }

        // back ist die Fahrgastzelle
        float backheight = trackwidth * 0.3f;
        float backheight2 = backheight/2;
        float backlen = trackwidth*0.9f - 0.1f;
        float backlen2 = backlen / 2;
        float backwidth = trackwidth*0.9f - 0.1f;
        float backwidth2 = backwidth / 2;
        
       SceneNode drivercell = tbf.buildBlock(backlen, backheight, backwidth);
        drivercell.getTransform().setPosition(new Vector3(baselen2/3, backheight2+baseheight2, 0));
        drivercell.getTransform().setParent(baseblock.getTransform());

        SceneNode raiser = new SceneNode();
        baseblock.getTransform().setParent(raiser.getTransform());
        baseblock.getTransform().setPosition(new Vector3(0, baseheight2 + wheelradius, 0));
        raiser.setName("Car");
        return raiser;
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
        return new Vector3(trackwidth,trackwidth,0);
    }
}
