package de.yard.threed.javacommon;

import de.yard.threed.core.Util;
import de.yard.threed.core.resource.NativeResource;
import org.apache.commons.io.IOUtils;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * FileReader fuer alle Platformen ausser GWT. Auch Anroid? Eigentlich ja, denn dafuer ist ja extra die ziplist.
 * 6.4.16: TODO Aber das Konzept der ziplist koennte als Location o.ä. in den RessourceManager. 21.12.16: siehe DVK, jetzt ResourcePath
 * 6.4.16: TODO: Das muss zumindest zum Teil wie der StringHelper ueber Interface in die Platform.
 * 16.10.18: Es gibt doch Bundle. Da wird das hier doch nicht mehr gebraucht. verschoben nach  desktop.
 * <p/>
 * <p/>
 * Created by thomass on 08.02.16.
 */
public class FileReader {
    // Eine Liste von zip/jar files
    private static List<File> ziplist = new ArrayList<File>();

    FileReader() {
    }

    /*17.5.16 public static InputStream getFileStream(String file) throws IOException {
        return getFileStream(new File(file));
    }*/

    /**
     * Sucht die Datei in folgender Reihenfolge:
     * 1) im aktuellen Verzeichnis bzw. relativ dazu bzw. halt einfach mit einem File.exists().
     * 2) Wenn das scheitert, wird im Classpath gesucht.
     * 3) Wenn das auch nichts liefert, wird eine optionale Path/Zip File Liste durchgesucht.
     * <p/>
     * 17.5.16: Mit NativeResource gibt es jetzt klarere Vorgaben, wo gesucht werden soll.
     * 21.12.16: Jetzt gibt es noch den ResourcePAth. Wahrscheinlich muss die Suche in etwas wie PAthProvider.
     * 05.04.17: Liefert nie null.
     * 24.4.17: Seit Bundle ist das wohl deprecated. Eine Lösung fuer z.B. gz brauchts aber trotzdem.
     * 16.10.18: Jetzt in desktop und nicht mehr deprecated. zip gibt es aber nicht mehr.
     * @return
     * @throws IOException
     */
    
    public static InputStream getFileStream(NativeResource resource) throws IOException {
        try {
            boolean iscompressed = resource.getName().endsWith(".gz");
            java.io.InputStream is;
            Integer knownsize = null;
            if (!resource.isBundled()) {
                String filename = resource.getFullName();
                File file = new File(filename);
                is = new FileInputStream(file);
                knownsize = (int) file.length();
            } else {
                String pathtofile = resource.getFullName();//file.getPath();
                
                //isType = file.getClass().getResourceAsStream(pathtofile);
                //isType = ClassLoader.getSystemResourceAsStream(pathtofile);
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                is = classLoader.getResourceAsStream(pathtofile);
                if (is == null) {
                    // Ueber explizite Zip Fileliste suchen
                    for (File zip : ziplist) {
                        if ((is = readfromzip(zip, pathtofile)) != null) {
                            break;
                        }
                    }
                    if (is == null) {
                        throw new IOException("file not found: " + pathtofile);
                    }
                }
            }
            if (iscompressed) {
                Util.nomore();
                //16.10.18 return getInputStream(new GZIPInputStream(isType), null);
            }

            return is;//16.10.18 getInputStream(isType, knownsize);

        } catch (IOException e) {
            throw e;
        }
    }

    public static byte[] readFully(InputStream ins){
        if (ins==null){
            return null;
        }
        try {
            return IOUtils.toByteArray(ins);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        
    }
    
    /*16.10.18 public static InputStream getSystemResourceAsStream(String ressource) throws IOException {
        //  try
        if (ressource.endsWith(".gz")) {
            return getInputStream(new GZIPInputStream(ClassLoader.getSystemResourceAsStream(ressource)), null);
        }
        return getInputStream(ClassLoader.getSystemResourceAsStream(ressource), null);
    }*/

    public static void addArchive(File zipfile) {
        ziplist.add(zipfile);
    }
    /*public static byte[] getFile(String filename) throws IOException {
        return readfully(new File(filename));
    }*/

    /**
     * 24.4.17: jetzt auch deprecated
     * @return
     * @throws IOException
     */
    /*16.10.18 @Deprecated
    public static InputStream getInputStream(final java.io.InputStream ins, final Integer knownsize) throws IOException {
        //  final ObjectInputStream oi;
        //try {
        //     oi = new ObjectInputStream(ins);
        //} catch (IOException e) {
        //    throw new RuntimeException("io", e);
        //  }

        return new InputStream() {
            @Override
            public int read() {
                try {
                    return ins.read();
                } catch (IOException e) {
                    throw new RuntimeException("io", e);
                }
            }

            @Override
            public void read(byte[] buf, int size) {
                try {
                    int offset = 0;
                    // Der io.InputStream liest nicht unbedingt so viel wie ich moechte.
                    //TODO notaus
                    do {
                        int read = ins.read(buf, offset, size);
                        size -= read;
                        offset += read;
                    }
                    while (size > 0);
                } catch (IOException e) {
                    throw new RuntimeException("io", e);
                }

            }

            /**
             * TODO: das ist doch total ineffizient
             * @return
             * /
            @Override
            public byte[] readFully() {
                if (knownsize != null) {
                    byte[] data = new byte[knownsize];
                    read(data, knownsize);
                    return data;
                } else {
                    List<Byte> data = new ArrayList<Byte>();
                    int b;
                    while ((b = read()) != -1) {
                        data.add((byte) b);
                    }
                    byte[] buf = new byte[data.size()];
                    int i = 0;
                    for (Byte sb : data) {
                        buf[i++] = sb;
                    }
                    return buf;
                }
            }

        /*    @Override
            public int readInt() {
                try {
                    return oi.readInt();
                } catch (IOException e) {
                    throw new RuntimeException("io", e);
                }
            }

            @Override
            public float readFloat() {
                try {
                    return oi.readFloat();
                } catch (IOException e) {
                    throw new RuntimeException("io", e);
                }
            }

            @Override
            public String readString() {
                try {
                    int len = oi.readInt();
                    String s = "";
                    for (int i = 0; i < len; i++) {
                        s += oi.readChar();
                    }
                    return s;
                } catch (IOException e) {
                    throw new RuntimeException("io", e);
                }
            }

            @Override
            public void close() {
                try {
                    oi.close();
                } catch (IOException e) {
                    throw new RuntimeException("io", e);
                }
            }* /
        };
    }*/

    private static java.io.InputStream readfromzip(File zipfile, String entryname) {
        try {
            ZipFile zp = new ZipFile(zipfile);
            if (zp == null) {
                return null;
            }
            ZipEntry entry = zp.getEntry(entryname);
            if (entry == null) {
                return null;
            }
            return zp.getInputStream(entry);

        } catch (Exception e) {
        }
        return null;
    }

    public static java.io.InputStream getInputStream(final InputStream ins) {
        return new java.io.InputStream() {
            @Override
            public int read() throws IOException {
                return ins.read();
            }
        };
    }

    

}
