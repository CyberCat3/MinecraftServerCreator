package cybercat3.jaibel_server_creator;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class Updater {
    public static void run() {
        new Thread(() -> {
            try {
                System.out.println("getActualChecksum() = " + getActualChecksum());
                System.out.println("getIntendedChecksum() = " + getIntendedChecksum());
                if (!Objects.equals(getIntendedChecksum(), getActualChecksum())) {
                    System.out.println("Hashes didn't match, updating...");
                    update();
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("[Updater] Couldn't check for updates nor update.");
            }
        }).start();
    }

    private static String getIntendedChecksum() throws IOException {
        String checksumsFile = IOUtils.downloadURLToString("https://jaibel.ddns.net/files/jaibel-server-creator/checksums.sha256");
        return checksumsFile.substring(0, 64);
    }

    private static String getActualChecksum() throws IOException {
        return computeHash(pathToThisJar());
    }

    private static void update() throws IOException {
        Path pathToUpdate = Files.createTempFile("jaibel_server_creator_update_", null);
        try (OutputStream os = Files.newOutputStream(pathToUpdate)) {
            IOUtils.downloadURLToStream(
                    "https://Jaibel.ddns.net/files/jaibel-server-creator/JaibelServerCreator.jar",
                    os
            );
        }
        System.out.println("Downloaded update.");


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                // This can write to current jar without causing an exception
                Files.write(pathToThisJar(), Files.readAllBytes(pathToUpdate));
                System.out.println("Update installed.");

                Files.deleteIfExists(pathToUpdate);
                System.out.println("Cleaned update.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        JOptionPane.showMessageDialog(Main.appFrame,
                "An update has been successfully download!\n" +
                       "It will be installed the next time you\n" +
                       "start this program!",
                "Update successfully Downloaded!",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private static Path pathToThisJar() {
        try {
            return Paths.get(new File(Updater.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static String computeHash(Path path) throws IOException {
        try {
            MessageDigest ms = MessageDigest.getInstance("sha-256");

            try (InputStream is = Files.newInputStream(path)) {
                int bytesRead;
                byte[] buffer = new byte[16384];

                while ((bytesRead = is.read(buffer)) != -1) {
                    ms.update(buffer, 0, bytesRead);
                }
            }

            return bytesToHex(ms.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        } catch (NoSuchFileException e) {
            return null;
        }
    }

    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; ++i) {
            int input = bytes[i]; //+ 128; // It feels better with plus, but Ubuntu's sha256sum doesn't do it.
            int h1 = (input >> 4) & 0b00001111;
            int h2 =        input & 0b00001111;
            hexChars[i * 2]     = HEX_CHARS[h1];
            hexChars[i * 2 + 1] = HEX_CHARS[h2];
        }
        return new String(hexChars);
    }
}
