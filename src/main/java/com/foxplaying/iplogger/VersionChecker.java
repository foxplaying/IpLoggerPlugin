// VersionChecker.java
package com.foxplaying.iplogger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class VersionChecker {
    private static final String GITHUB_API = "https://api.github.com/repos/foxplaying/IpLoggerPlugin/releases/latest";

    public static String fetchLatestVersion() {
        try {
            URL url = new URL(GITHUB_API);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "IpLoggerPlugin");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                json.append(line);
            }
            in.close();

            String result = json.toString();
            int tagStart = result.indexOf("\"tag_name\"");
            if (tagStart == -1) return null;

            int colon = result.indexOf(":", tagStart);
            int quoteStart = result.indexOf("\"", colon + 1);
            int quoteEnd = result.indexOf("\"", quoteStart + 1);

            return result.substring(quoteStart + 1, quoteEnd);
        } catch (Exception e) {
            return null;
        }
    }
}
