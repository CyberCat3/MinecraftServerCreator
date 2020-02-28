package cybercat3.jaibel_server_creator.version_handlers;

import cybercat3.jaibel_server_creator.IOUtils;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

public class VanillaHandler {
    private static Set<String> versions;

    public static Set<String> getVersions() throws IOException {
        // If we have already found the versions
        if (versions != null && versions.size() != 0) {
            return versions; // return ´em
        }
        // We're using a set to remove prevent duplicates.
        versions = new LinkedHashSet<>();

        // Download the html from the Minecraft Version history Wiki-page.
        String html = IOUtils.downloadURLToString("https://minecraft.gamepedia.com/Java_Edition_version_history");

        // Skip to the actual versions.
        html = html.substring(html.indexOf("<span class=\"mw-headline\" id=\"Release\">Release</span>"));

        // Parse the html then fetch the versions.
        Pattern p = Pattern.compile("<a href=\"(/Java_Edition_(\\d+\\.\\d+(\\.\\d+)?))\" title=\".+?\">.*?</a>");
        Matcher m = p.matcher(html);

        while (m.find()) {
            if (ForgeHandler.mcVersionToNumber(m.group(2)) >= ForgeHandler.mcVersionToNumber("1.2.5")) {
                versions.add(m.group(2));
            }
        }

        return versions;
    }

    public static URL getURLFromVersion(String version) throws IOException {
        String link = "https://minecraft.gamepedia.com/Java_Edition_" + version;

        String html = IOUtils.downloadURLToString(link);

        Pattern p = Pattern.compile("<a target=\"_self\" rel=\"nofollow\" class=\"external text\" href=\"(https://launcher\\.mojang\\.com/v1/objects/[0-9a-f]+/server\\.jar)\">Server</a>");
        Matcher m = p.matcher(html);

        if (m.find()) {
            return new URL(m.group(1));
        }

        throw new FileNotFoundException("Couldn't find server.jar for MC " + version);
    }
}
