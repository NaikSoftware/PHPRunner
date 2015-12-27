package ua.naiksoftware.phprunner;

import java.io.*;
import java.util.zip.*;
import ua.naiksoftware.phprunner.log.*;

public class Unzip {

    private static final String tag = Unzip.class.getName();

    private static final long STOCK_LOCAL_MEMORY = 1024000;//1000 Kb про запас

    /*
     * Щитает размер архива, если его разархивировать
     */
    public static long calcUnzipped(InputStream is) {
        ZipInputStream zip = new ZipInputStream(is);
        ZipEntry ze;
        long realSize = 0;
        try {
            while ((ze = zip.getNextEntry()) != null) {
                realSize += ze.getSize();
                zip.closeEntry();
            }
        } catch (IOException e) {
        }
        //L.write(tag, "calcUnzipped return = " + (int)realSize);
        return realSize;
    }

    public static boolean unzip(InputStream is, File folderToUnzip, Installer inst, boolean setRights) {
        //L.write(tag, "method unzip started");
        ZipInputStream zip = new ZipInputStream(new BufferedInputStream(is));
        FileOutputStream fos = null;
        String fileName = null;
        ZipEntry zipEntry;
        try {
            while ((zipEntry = zip.getNextEntry()) != null) {
                long free = folderToUnzip.getFreeSpace();
                fileName = zipEntry.getName();
                final File outputFile = new File(folderToUnzip, fileName);
                outputFile.getParentFile().mkdirs();
                //L.write("Unzip", "Zip entry: " + fileName + ", extract to: " + outputFile.getPath());
                if (fileName.endsWith("/")) {
                    //Log.i("Unzip", fileName+ " is directory");
                    outputFile.mkdirs();
                    if (setRights) {
                        outputFile.setExecutable(true);
                    }
                    continue;
                } else {
                    outputFile.createNewFile();
                    if (zipEntry.getSize() == outputFile.length()) {
                        continue;
                    }
                    inst.update((int) outputFile.length());
                    free = free - zipEntry.getSize() + outputFile.length();
                    if (free < STOCK_LOCAL_MEMORY) {
                        inst.setErr(R.string.out_of_memory_local);
                        return false;
                    }
                    fos = new FileOutputStream(outputFile, false);
                    byte[] bytes = new byte[2048];
                    int c;
                    try {
                        while ((c = zip.read(bytes)) != -1) {
                            if (inst.isCancelled()) {
                                L.write(tag, "in zip.read(bytes) task was cancelled");
                                return false;
                            }
                            inst.update(c);
                            fos.write(bytes, 0, c);
                        }
                        fos.flush();
                        fos.close();
                    } catch (IOException e) {
                        L.write(tag, "IOErr in readFromStream (zip.read(bytes)): " + e);
                    }
                }
                zip.closeEntry();
                if (setRights) {
                    if (fileName.equals("lighttpd") || fileName.equals("mysqld") || fileName.equals(ServerUtils.PHP_BINARY)) {
                        Runtime.getRuntime().exec("chmod 777 " + outputFile.getAbsolutePath());
                    } else {
                        Runtime.getRuntime().exec("chmod 600 " + outputFile.getAbsolutePath());
                    }
                    /*if (fileName.equals("my.ini")) {
                     Runtime.getRuntime().exec("chmod 644 " + outputFile.getAbsolutePath());
                     } else {
                     Runtime.getRuntime().exec("chmod 777 " + outputFile.getAbsolutePath());
                     }*/
                }
            }
            //Runtime.getRuntime().exec("chmod 644 " + "/data/data/" + Const.MY_PACKAGE_NAME + "/my.ini");
        } catch (IOException ioe) {
            L.write(tag, "IOErr in unzip (nextEntry, closeEntry or other): " + ioe);
            inst.setErr(ioe.getMessage());
            return false;
        } finally {
            try {
                zip.close();
            } catch (IOException e) {
            }
        }
        return true;
    }
}
