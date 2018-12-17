package com.alperez.geekbooks.crowler.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlPageLoader {
    private final URL url;

    public static final int EOF = -1;

    public HtmlPageLoader(URL link) {
        this.url = link;
    }


    public String load(int maxSizeBytes) throws IOException {
        HttpURLConnection conn = HttpUtils.makeConnection(url);
        conn.setRequestMethod("GET");
        conn.setDoOutput(false);
        conn.connect();
        try {
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                int cLen = conn.getContentLength();
                if (cLen > maxSizeBytes) {
                    throw new IOException(String.format("Image content to large - %d bytes. Available max size - %d bites", cLen, maxSizeBytes));
                } else if (cLen == 0) {
                    throw new IOException("Content-Length is 0");
                } else {
                    String contType = conn.getContentType();
                    Matcher m = Pattern.compile("text/html;\\s+charset=([^\\s]+)\\s*").matcher(contType);
                    Charset charset = Charset.forName(m.matches() ? m.group(1) : "ISO-8859-1");
                    return is2string(conn.getInputStream(), charset);
                }
            } else {
                throw new IOException(String.format("Server returned HTTP Error: %d - %s", conn.getResponseCode(), conn.getResponseMessage()));
            }
        } finally {
            conn.disconnect();
        }
    }

    private String is2string(InputStream is, Charset charset) throws IOException {
        try (StringBuilderWriter sw = new StringBuilderWriter()) {
            copy(new InputStreamReader(is, charset), sw, 512);
            return sw.toString();
        }
    }


    private int copy(Reader input, Writer output, int buffSize) throws IOException {
        char buffer[] = new char[buffSize];
        int count = 0;
        int n;
        while ((n = input.read(buffer)) != EOF) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}
