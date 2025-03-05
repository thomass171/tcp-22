package de.yard.threed.core.platform;



/**
 * Created by thomass on 05.06.15.
 * 27.02.25: name no longer deprecated.
 * Has no 'MaterialDefinition getDefinition()' because it appears weird/inappropriate. How should a platform build it.
 */
public interface NativeMaterial {

    /**
     * 21.7.16
     * Ob das Ändern von Material wirklich der wahre Jakob ist (weil es die Platform vielleicht nicht kann), muss
     * sich noch zeigen. Mal klein und nach Bedarf anfangen.
     *  Das deaktivieren von transparancy in Unity scheint nicht zu gehen, wenn es mal enabled war.
     * 05.10.17: Vielleicht sollte bei Änderungen einfach das Material komplett neu gesetzt werden? Das kann aber wiederum sehr ineffizient sein.
     * Man kann es vielleicht einmalig ueber das Material setzen, und dann nur noch den Alpha Channel regeln.
     * Ist das mit dem Value nicht Quatshc? Kommt der Wert nicht aus dem Alpha Channel?
     * transparency ist ja auch n ein Beispiel. Alles mögliche mag sich zur Laufzeit ändern. Also, Matzerial neu setzen
     * ist auf jeden Fall der erste Weg.
     * 6.10.17:Ob das aber mit nativen Loadern (z.B. gltf) geht, ist fraglich. Der Weg, wie FG Effects baut, ist vielleicht schon sehr sepziell.
     * 10.10.17: jetzt boolean und erstmal nicht deprecated, solane es nutzbar ist und keine Probleme macht.
     * 10.8.24:Again deprecated. Cannot be set later. This is nonsense. Building new material is the way to go.
     */
    @Deprecated
    void setTransparency(boolean enabled);

    /**
     * 01.09.16: Not unique, so no identifier. More a label, helpful for logging.
     *
     */
    String getName();

    void setName(String name);

    //27.2.25 what's this??
    NativeTexture[] getMaps();

     NativeUniform getUniform(String name);
}
