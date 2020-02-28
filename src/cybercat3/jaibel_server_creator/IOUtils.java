package cybercat3.jaibel_server_creator;

import java.io.*;
import java.net.*;

public final class IOUtils {
    private IOUtils() {}

    public static URLConnection connectionFromURL(URL url) throws IOException {
        URLConnection conn = url.openConnection();
        conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.122 Safari/537.36");
        return conn;
    }

    public static InputStream inputStreamFromURL(URL url) throws IOException {
        return connectionFromURL(url).getInputStream();
    }

    public static void downloadURLToStream(String url, OutputStream os) throws IOException {
        downloadURLToStream(new URL(url), os);
    }
    public static void downloadURLToStream(URL url, OutputStream os) throws IOException {
        try (InputStream is = inputStreamFromURL(url)) {
            copyStream(is, os);
        }
    }

    public static String downloadURLToString(String url) throws IOException {
        return downloadURLToString(new URL(url));
    }
    public static String downloadURLToString(URL url) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        downloadURLToStream(url, baos);
        return baos.toString();
    }

    public static String streamToString(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copyStream(is, baos);
        return baos.toString();
    }

    public static void copyStream(InputStream is, OutputStream os) throws IOException {
        int bytesRead;
        byte[] buffer = new byte[1024];

        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
    }
}
