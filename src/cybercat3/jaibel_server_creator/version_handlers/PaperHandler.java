package cybercat3.jaibel_server_creator.version_handlers;

import cybercat3.jaibel_server_creator.IOUtils;

import java.net.URL;
import java.util.*;
import java.io.*;
import java.util.regex.*;

public class PaperHandler {
    private static Set<String> versions;

    public static Set<String> getVersions() throws IOException {
        // If we have already found the versions
        if (versions != null && versions.size() != 0) {
            return versions; // return ´em
        }
        // We're using a set to remove prevent duplicates.
        versions = new LinkedHashSet<>();

        // Download the json from the PaperMC API.
        String json = IOUtils.downloadURLToString("https://papermc.io/api/v1/paper/");

        // Parse the html then fetch the versions.
        Pattern p = Pattern.compile("\"(\\d+\\.\\d+(\\.\\d+)?)\"");
        Matcher m = p.matcher(json);

        while (m.find()) {
            versions.add(m.group(1));
        }

        return versions;
    }

    public static URL getURLFromVersion(String version) throws IOException {
        String url = "https://papermc.io/api/v1/paper/" + version;

        String json = IOUtils.downloadURLToString(url);

        Pattern p = Pattern.compile("\"latest\":\"(\\d+)\"");
        Matcher m = p.matcher(json);

        if (m.find()) {
            return new URL(url + "/" + m.group(1) + "/download");
        }

        throw new IOException("Couldn't parse Paper API");
    }
}
