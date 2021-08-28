package de.yard.threed.platform.jme;


import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.TestPdfDoc;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.util.PDFImageWriter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Date: 11.07.14
 */
public class JmePdfDocument /*29.721 implements NativePdfDocument*/ {
    static Log logger = Platform.getInstance().getLog(JmePdfDocument.class);
    private final PDDocumentInformation docinfo;
    String name;
    PDDocument doc;
    boolean cacheenabled = false;
    TestPdfDoc docid;

    /**
     * Name muss mit Suffix pdf sein.
     *
     * @param name
     */
    public JmePdfDocument(String name, TestPdfDoc docid) {
        this.name = name;
        this.docid = docid;
        if (!name.endsWith(".pdf"))
            throw new RuntimeException("suffix must be .pdf");
        try {
            doc = PDDocument.load(new File(name));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        docinfo = doc.getDocumentInformation();
    }

    public int getNumberOfPages() {
        return doc.getNumberOfPages();
    }

    public BufferedImage getPage(int page) {
        try {
            if (docid == TestPdfDoc.IPADUSERGUIDE) {
                String filename = "../cache/iPad_User_Guide " + page + ".jpeg";
                return getImage(filename);

            }
            logger.debug("getPage " + page);
            BufferedImage image = null;
            // Bei disabled cahce wird immr neu gelesen, dann aber trotzdem aus dem Cache gelesen.
            if (cacheenabled) {
                getPageFromCache(page);
            }
            if (image == null) {
                readPdf(page);
                image = getPageFromCache(page);

            }
            if (image == null) {
                logger.warn("image still null");
            }
            return image;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void readPdf(int page) throws IOException {
        String color = "rgb";


        int imageType = 24;
        if ("bilevel".equalsIgnoreCase(color)) {
            imageType = BufferedImage.TYPE_BYTE_BINARY;
        } else if ("indexed".equalsIgnoreCase(color)) {
            imageType = BufferedImage.TYPE_BYTE_INDEXED;
        } else if ("gray".equalsIgnoreCase(color)) {
            imageType = BufferedImage.TYPE_BYTE_GRAY;
        } else if ("rgb".equalsIgnoreCase(color)) {
            imageType = BufferedImage.TYPE_INT_RGB;
        } else if ("rgba".equalsIgnoreCase(color)) {
            imageType = BufferedImage.TYPE_INT_ARGB;
        } else {
            System.err.println("Error: the number of bits per pixel must be 1, 8 or 24.");
            System.exit(2);
        }

        int resolution;
        try {
            resolution = Toolkit.getDefaultToolkit().getScreenResolution();
        } catch (HeadlessException e) {
            resolution = 96;
        }
        logger.debug("resolution=" + resolution);
        /*List<PDPage> list = doc.getDocumentCatalog().getAllPages();

        int pageNumber = 1;
        for (PDPage page : list) {
            image = page.convertToImage();
            //File outputfile = new File(destinationDir + fileName +"_"+ pageNumber+".png");
            //ImageIO.write(image, "png", outputfile);
            pageNumber++;
        } */

        String cname = getCacheName();
        PDFImageWriter pdfimagewriter = new PDFImageWriter();
        // Durchnumerieren macht writeImage selbst
        //TODO: catch um writeImage und dann bei Exception das Image wieder loeschen. Sonst weiss man nicht, ob es brauchbar ist
        // Der Imagewriter beginnt mit page1!
        //boolean rc = pdfimagewriter.writeImage(doc, "png", null, page, page, getCacheName() + "-"/*,imageType,resolution*/);
        boolean rc = pdfimagewriter.writeImage(doc, "jpg", null, page, page, getCacheName() + "-"/*,imageType,resolution*/);
        logger.debug("rc=" + rc);
        //doc.close();
        // PDFImageWriter.write(doc, "png", null, 0, 0, "picture");
    }

    private String getCacheName() {
        String cname = name.replaceAll("/", ".");
        cname = cname.substring(0, cname.length() - 4);
        if (cname.startsWith("."))
            cname = cname.substring(1);
        return "../cache/" + cname;
    }

    private String getPageCacheName(int page) {
        return getCacheName() + "-" + page;
    }

    private BufferedImage getPageFromCache(int page) throws IOException {
        String filename = getPageCacheName(page)+".jpeg";
      //  filename = "cache/iPad_User_Guide " + page + ".jpeg";
        return getImage(filename);
    }

    private BufferedImage getImage(String filename) throws IOException {

        File file = new File(filename);
        if (file.exists()) {
            return ImageIO.read(file);
        }
        
        return null;
    }
}