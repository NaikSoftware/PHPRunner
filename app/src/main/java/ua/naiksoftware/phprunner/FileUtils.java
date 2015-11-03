package ua.naiksoftware.phprunner;

import java.io.*;

public class FileUtils {

    public static String readFile(String path) {
        StringBuilder sb = new StringBuilder();
        try {
            FileReader fr = new FileReader(path);
            char buff[] = new char[1024];
            int c;
            while ((c = fr.read(buff)) != -1) {
                sb.append(buff, 0, c);
            }
        } catch (FileNotFoundException e) {
            return "File not found (TODO)";
        } catch (IOException ioe) {
            return "IOException (TODO)";
        }
        return sb.toString();
    }

    public static void saveCode(String code, String charset, String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file), charset);
        osw.append(code).flush();
        osw.close();
    }
}
