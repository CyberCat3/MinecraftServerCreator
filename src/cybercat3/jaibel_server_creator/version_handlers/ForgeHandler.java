package cybercat3.jaibel_server_creator.version_handlers;

import cybercat3.jaibel_server_creator.IOUtils;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

public class ForgeHandler {
    private static Set<String> versions;

    public static Set<String> getVersions() throws IOException {
        // If we have already found the versions
        if (versions != null && versions.size() != 0) {
            return versions; // return ´em
        }
        // We're using a set to remove prevent duplicates.
        versions = new LinkedHashSet<>();

        // Download the html from files.minecraftforge.net.
        String html = IOUtils.downloadURLToString("https://files.minecraftforge.net/");

        // The first version is different HTML
        Pattern p = Pattern.compile("<li class=\"elem-active\">(\\d+\\.\\d+(\\.\\d+)?)</li>");
        Matcher m = p.matcher(html);

        if (m.find()) {
            versions.add(m.group(1));
        }

        // Parse the html then fetch the versions.
        p = Pattern.compile("<li>\\s+<a href=\".*?\">(\\d+\\.\\d+(\\.\\d+)?)");
        m = p.matcher(html);

        while (m.find()) {
            if (mcVersionToNumber(m.group(1)) >= mcVersionToNumber("1.5.2")) {
                versions.add(m.group(1));
            }
        }

        return versions;
    }

    public static URL getURLFromVersion(String version) throws IOException {

        String html = IOUtils.downloadURLToString("https://files.minecraftforge.net/maven/net/minecraftforge/forge/index_"+version+".html");

        Pattern p = Pattern.compile("Download Latest<br>\\s+<small>(\\d+\\.\\d+(\\.\\d+)?) - (\\d+\\.\\d+\\.\\d+(\\.\\d+)?)");
        Matcher m = p.matcher(html);


        if (m.find()) {
            String mcVersion = m.group(1);
            String forgeVersion = m.group(3);

            String url = String.format("https://files.minecraftforge.net/maven/net/minecraftforge/forge/%s-%s/forge-%s-%s-installer.jar",
                    mcVersion, forgeVersion, mcVersion, forgeVersion);

            // Forge had a weird link format for 1.7.10
            if (mcVersionToNumber(mcVersion) == 1007010) {
                url = String.format("https://files.minecraftforge.net/maven/net/minecraftforge/forge/%s-%s-%s/forge-%s-%s-%s-installer.jar",
                        mcVersion, forgeVersion, mcVersion, mcVersion, forgeVersion, mcVersion);
            }

            return new URL(url);
        }

        throw new IOException("Couldn't get link URL from Forge Website");
    }

    public static int mcVersionToNumber(String mcVersion) {
        String[] segments = mcVersion.trim().split("\\.");
        StringBuilder nb = new StringBuilder();

        for (String segment : segments) {
            for (int i = 0; i < 3 - segment.length(); ++i) {
                nb.append("0");
            }
            nb.append(segment);
        }

        if (segments.length == 2) {
            nb.append("000");
        }

        return Integer.parseInt(nb.toString());
    }
}
