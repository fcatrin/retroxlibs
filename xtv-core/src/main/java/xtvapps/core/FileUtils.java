package xtvapps.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class FileUtils {
    private static final int BUF_SIZE = 65536;

    private FileUtils(){}

    public static String loadString(File f) throws IOException {
        return loadString(f, "UTF-8");
    }

    public static List<String> loadLines(File f) throws IOException  {
        return loadLines(f, "UTF-8");
    }

    public static String loadString(File f, String encoding) throws IOException {
        return CoreUtils.loadString(new FileInputStream(f), encoding);
    }

    public static List<String> loadLines(File f, String encoding) throws IOException {
        return CoreUtils.loadLines(new FileInputStream(f), encoding);
    }

    public static byte[] loadBytes(File f) throws IOException {
        try (FileInputStream is = new FileInputStream(f)) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] buf = new byte[BUF_SIZE];
            while (true) {
                int rc = is.read(buf);
                if (rc <= 0)
                    break;
                else
                    bout.write(buf, 0, rc);
            }
            return bout.toByteArray();
        }
    }

    public static void saveString(File f, String s) throws IOException {
        saveBytes(f, s.getBytes());
    }

    public static void saveBytes(File f, byte[] content) throws IOException {
        try (FileOutputStream os = new FileOutputStream(f); ByteArrayInputStream bais = new ByteArrayInputStream(content)) {
            byte[] buf = new byte[BUF_SIZE];
            while (true) {
                int rc = bais.read(buf);
                if (rc <= 0)
                    break;
                else
                    os.write(buf, 0, rc);
            }
        }
    }

    private static void buildTreeList(List<File> allFiles, File srcDir, File dstDir) {
        File[] files = srcDir.listFiles();
        if (files == null) return;

        for(File file : files) {
            allFiles.add(file);
            if (file.isDirectory() && !file.equals(dstDir)) {
                buildTreeList(allFiles, file, dstDir);
            }
        }
    }

    public static List<File> moveTree(File srcDir, File dstDir) throws IOException {
        List<File> files = new ArrayList<>();
        buildTreeList(files, srcDir, dstDir);

        dstDir.mkdirs();

        String base = srcDir.getCanonicalPath();
        for(File file : files) {
            String relative = file.getCanonicalPath().substring(base.length()+1);
            File dstFile = new File(dstDir, relative);
            if (file.isDirectory()) {
                dstFile.mkdirs();
            } else {
                file.renameTo(dstFile);
            }
        }
        return files;
    }

    public static long getTreeSize(File root) {
        long size = 0;
        File[] files = root.listFiles();

        if (files == null) return size;

        for(File file: files) {
            if (file.isDirectory()) size += getTreeSize(file);
            else size += file.length();
        }
        return size;
    }

    public static int getTreeCount(File root) {
        int count = 0;
        File[] files = root.listFiles();

        if (files == null) return count;

        for(File file: files) {
            if (file.isDirectory()) count += getTreeCount(file);
            else count ++;
        }
        return count;
    }

    public static boolean canWrite(File f) {
        if (!f.isDirectory()) return false;

        File testDir = new File(f, ".xtvapps_test");
        if (testDir.exists()) {
            return testDir.delete();
        }

        boolean canWrite = testDir.mkdir();
        testDir.delete();
        return canWrite;
    }

    public static boolean copyTree(File src, File dst) {
        dst.mkdirs();

        File[] files = src.listFiles();
        if (files == null) return true;

        for(File file : files) {
            File dstFile = new File(dst, file.getName());
            if (file.isDirectory()) {
                boolean success = copyTree(file, dstFile);
                if (!success) return false;
            } else {
                try {
                    copyFile(file, dstFile);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return true;
    }

    public static void copyFile(File src, File dst) throws IOException {
        copyFile(src, dst, null);
    }

    public static void copyFile(File src, File dst, ProgressListener progressListener) throws IOException {
        FileInputStream is = new FileInputStream(src);
        FileOutputStream os = new FileOutputStream(dst);
        CoreUtils.copy(is, os, progressListener, src.length());
    }

}
