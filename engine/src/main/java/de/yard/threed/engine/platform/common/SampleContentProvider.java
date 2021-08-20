package de.yard.threed.engine.platform.common;


import de.yard.threed.core.Color;
import de.yard.threed.core.Dimension;
import de.yard.threed.core.ImageData;
import de.yard.threed.core.platform.NativeContentProvider;
import de.yard.threed.engine.gui.Text;

/**
 * Liefert fuer jede Seite ein generiertes Image mit der Seitenzahl drauf.
 * 8.3.16: Verschoben nach platform, um auch in WebGL genutzt werden zu koennen. Und weil die
 * Bereitstellung von Content ja nunmal platformabhängig ist.
 * 
 * <p/>
 * Created by thomass on 06.02.15.
 */
public class SampleContentProvider implements NativeContentProvider {
    int numberofpages;
    Color basecolor = Color.BLUE;

    public SampleContentProvider(int numberofpages) {
        this.numberofpages = numberofpages;
    }

    @Override
    public int getNumberOfPages() {
        return numberofpages;
    }

    @Override
    public ImageData getPage(int pageno) {
        ImageData image = Text/*ImageFactory*/.buildLabelImage((""+pageno), new Dimension(100, 300), basecolor);
        return image;
    }


}
