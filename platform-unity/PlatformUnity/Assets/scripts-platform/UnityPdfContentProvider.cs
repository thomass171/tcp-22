using System;
using java.lang;
using UnityEngine;

namespace de.yard.threed.platform
{
    using de.yard.threed.core;
    using de.yard.threed.core.platform;
    

    /**
     *
     */
    public class UnityPdfContentProvider: NativeContentProvider
    {
        UnityPdfDocument doc;
        NativeTexture emptytexture;

        public UnityPdfContentProvider (TestPdfDoc docid)
        {
            /*doc = new JmePdfDocument("/Users/thomass/Dokumentation(Bücher)/MD-Diagrams.pdf");
        doc = new JmePdfDocument ("/Users/thomass/Dokumentation(Bücher)/svn-book.pdf");
        doc = new JmePdfDocument ("/Users/thomass/Dokumentation(Bücher)/ECURAL_MINI_Salbe.pdf");
        // Eine empty page wird auf jeden Fall gebraucht, sonst folgt irgendwann NPE
        ImageData image = ImageFactory.buildSingleColor(200, 100, Color.GRAY);
        emptytexture = PlatformFactory.getInstance().buildNativeTexture(image);*/
            doc = new UnityPdfDocument ("/Users/thomas/Dokumentation(Bücher)/Handbücher/iPad_User_Guide.pdf", docid);

        }

        public int getNumberOfPages ()
        {
            return doc.getNumberOfPages ();
        }

        public ImageData getPage (int pageno)
        {
            return doc.getPage (pageno);
        }

      /*  public NativeTexture getEmptyPage ()
        {
            return emptytexture;
        }*/
    }
}
