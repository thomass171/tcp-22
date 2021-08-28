package de.yard.threed.platform.jme;

import de.yard.threed.core.ImageData;
import de.yard.threed.core.platform.TestPdfDoc;
import de.yard.threed.javacommon.ImageUtil;

/**
 * Created by thomass on 06.02.15.
 */
public class JmePdfContentProvider extends JmeContentProvider {
    JmePdfDocument doc;
    //NativeTexture emptytexture;

    public JmePdfContentProvider(TestPdfDoc docid) {
        /*doc = new JmePdfDocument("/Users/thomass/Dokumentation(Bücher)/MD-Diagrams.pdf");
        doc = new JmePdfDocument ("/Users/thomass/Dokumentation(Bücher)/svn-book.pdf");
        doc = new JmePdfDocument ("/Users/thomass/Dokumentation(Bücher)/ECURAL_MINI_Salbe.pdf");
        // Eine empty page wird auf jeden Fall gebraucht, sonst folgt irgendwann NPE
        ImageData image = ImageFactory.buildSingleColor(200, 100, Color.GRAY);
        emptytexture = PlatformFactory.getInstance().buildNativeTexture(image);*/
        doc = new JmePdfDocument ("/Users/thomas/Bibliothek/Handbücher/iPad_User_Guide.pdf",docid);

    }

    @Override
    public int getNumberOfPages() {
        return doc.getNumberOfPages();
    }

    @Override
    public ImageData getPage(int pageno) {
        return ImageUtil.buildImageData(doc.getPage(pageno));
    }

  /*  @Override
    public ImageData getEmptyPage() {
        return emptytexture;
    }*/
}
