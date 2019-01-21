package com.alperez.geekbooks.crowler.utils;

import java.io.*;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class FileUtils {

    /**
     * Creates a directory and puts a .nomedia file in it
     *
     * @param dir
     * @return new dir
     * @throws IOException
     */
    public static File createDirIfNeeded(File dir) throws IOException {
        if ((dir != null) && !dir.exists()) {
            if (!dir.mkdirs() && !dir.isDirectory()) {
                throw new IOException("error create directory");
            }
            File noMediaFile = new File(dir, ".nomedia");
            noMediaFile.createNewFile();
        }
        return dir;
    }

    public static int clearFolder(File folder) {
        int numDeleted = 0;
        if ((folder == null) || !folder.isDirectory()) {
            return numDeleted;
        }
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    numDeleted += clearFolder(f);
                } else {
                    if (f.delete()) {
                        numDeleted ++;
                    }
                }
            }
        }
        return numDeleted;
    }


    /**
     *
     * @param sourceFile
     * @param destFile
     * @return Number of bytes copied
     * @throws IOException
     */
    public static long copyFileToFile(File sourceFile, File destFile) throws IOException {
        if (sourceFile == null) return 0;
        if(!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;
        FileInputStream fis = null;
        FileOutputStream fos = null;

        try {
            fis = new FileInputStream(sourceFile);
            fos = new FileOutputStream(destFile);
            source = fis.getChannel();
            destination = fos.getChannel();
            long count = 0;
            long size = source.size();
            while((count += destination.transferFrom(source, count, size - count)) < size);
            return count;
        } finally {
            if(source != null) {
                source.close();
            }
            if (fis != null) {
                fis.close();
            }
            if(destination != null) {
                destination.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    public static String copyFileWithHash(File src, File dst) throws NoSuchAlgorithmException, IOException {
        try (FileInputStream fIs = new FileInputStream(src); FileOutputStream fOs = new FileOutputStream(dst)) {
            InputStream is = new BufferedInputStream(fIs);
            OutputStream os = new BufferedOutputStream(fOs);

            byte data[] = new byte[8192];
            int count;

            MessageDigest digester = MessageDigest.getInstance("SHA-256");
            while ((count = is.read(data)) != -1) {
                os.write(data, 0, count);
                digester.update(data, 0, count);
            }

            os.flush();
            return new String(encodeBytesToHex(digester.digest()));
        }
    }

    private static char[] encodeBytesToHex(byte[] data) {
        char[] ret = new char[data.length * 2];
        for (int i=0, j=0; i<data.length; i++, j+=2) {
            int c = (char)((data[i] >> 4) & 0x0F);
            if (c > 9) c += 7;
            ret[j] = (char)(c+0x30);


            c = (char)(data[i] & 0x0F);
            if (c > 9) c += 7;
            ret[j+1] = (char)(c + 0x30);

        }
        return ret;
    }





    private FileUtils() { }
}
