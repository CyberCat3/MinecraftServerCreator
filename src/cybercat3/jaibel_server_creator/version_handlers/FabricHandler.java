package cybercat3.jaibel_server_creator.version_handlers;

import cybercat3.jaibel_server_creator.IOUtils;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.net.*;

public class FabricHandler {

    private static Set<String> versions;

    public static List<String> getVersions() throws IOException {
        // If we have already found the versions
        if (versions != null && versions.size() != 0) {
            List<String> ls = new ArrayList<>(versions);
            Collections.reverse(ls);
            return ls; // return ´em
        }
        // We're using a set to remove prevent duplicates.
        versions = new LinkedHashSet<>();

        // Download the xml from fabric.
        String html = IOUtils.downloadURLToString("https://maven.fabricmc.net/net/fabricmc/yarn/maven-metadata.xml");

        // Parse the xml then fetch the versions.
        Pattern p = Pattern.compile("<version>(\\d+\\.\\d+(\\.\\d+)?)\\+");
        Matcher m = p.matcher(html);

        while (m.find()) {
            versions.add(m.group(1));
        }

        List<String> ls = new ArrayList<>(versions);
        Collections.reverse(ls);
        return ls;
    }

    public static URL getInstaller() throws IOException {
        // Get the xml from fabric.
        String html = IOUtils.downloadURLToString("https://maven.fabricmc.net/net/fabricmc/fabric-installer/maven-metadata.xml");

        // Parse the xml to get latest launcher version.
        Pattern p = Pattern.compile("<release>((\\d+\\.){3}\\d+)</release>");
        Matcher m = p.matcher(html);

        String version = m.find() ? m.group(1) : null;

        // Construct the download url.
        String url = String.format("https://maven.fabricmc.net/net/fabricmc/fabric-installer/%s/fabric-installer-%s.jar",
                version, version);

        return new URL(url);
    }
}
