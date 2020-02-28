package cybercat3.jaibel_server_creator;

import cybercat3.jaibel_server_creator.version_handlers.FabricHandler;
import cybercat3.jaibel_server_creator.version_handlers.ForgeHandler;
import cybercat3.jaibel_server_creator.version_handlers.PaperHandler;
import cybercat3.jaibel_server_creator.version_handlers.VanillaHandler;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;

public class ServerInstaller {
    public static final String MINECRAFT_SAVES_PATH_WINDOWS = System.getProperty("user.home") + "\\AppData\\Roaming\\.minecraft\\saves";
    public static final String MINERCAFT_SAVES_PATH_MAC_OS  = System.getProperty("user.home") + "/Library/Application Support/minecraft/saves";
    public static final String MINECRAFT_SAVES_PATH_LINUX   = System.getProperty("user.home") + "/.minecraft/saves";

    private static JTextArea console;
    private static JProgressBar progressBar;

    public static void setConsole(JTextArea console) {
        ServerInstaller.console = console;
    }

    public static void setProgressBar(JProgressBar progressBar) {
        ServerInstaller.progressBar = progressBar;
    }

    private static void addToConsole(String s) {
        SwingUtilities.invokeLater(() -> console.setText(console.getText() + "\n" + s));
    }

    public static void installServer(InstallationOrder order) {
        new Thread(() -> {
            try {
                String serverDirectory = order.getPathToTarget();

                // CREATE SERVER DIRECTORY
                if (!Files.exists(Paths.get(serverDirectory))) {
                    Files.createDirectory(Paths.get(serverDirectory));
                    addToConsole("Created Server Directory.");
                }

                // DOWNLOAD MINECRAFT SERVER IF TYPE IS VANILLA OR FABRIC
                if (order.getServerType() == ServerType.VANILLA || order.getServerType() == ServerType.FABRIC) {
                    try {
                        downloadServer(serverDirectory, order.getVersion());
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(Main.appFrame,
                                "The Minecraft Server could not be downloaded.\n\n" +
                                        "Perhaps you don't have an internet connection?"
                                ,"Couldn't download server!",
                                JOptionPane.ERROR_MESSAGE);
                        throw e;
                    }
                }

                // COPY WORLD OVER IF THE USER WANTS
                if (order.usePreexistingWorld()) {
                    try {
                        copyWorld(order.getPathToWorld(), serverDirectory);
                    } catch (IOException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(Main.appFrame,
                                "Couldn't copy your world.\n\n" +
                                        "Your server will still work, but the\n" +
                                        "world will be generated from scratch."
                                ,"Couldn't copy world!",
                                JOptionPane.WARNING_MESSAGE);
                    }
                }

                // CREATE LAUNCH SCRIPTS AND AGREE TO EULA
                try {
                    agreeToEula(serverDirectory);
                    createLaunchScripts(serverDirectory);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(Main.appFrame,
                            "Couldn't install server.\n\n" +
                                    "Perhaps you deleted it?"
                            ,"Couldn't install server!",
                            JOptionPane.ERROR_MESSAGE);
                    throw e;
                }

                // IF FORGE
                if (order.getServerType() == ServerType.FORGE) {
                    // DOWNLOAD FORGE
                    try {
                        downloadForge(serverDirectory, order.getVersion());
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(Main.appFrame,
                                "Couldn't download Forge.\n\n" +
                                        "Either I did something stupid, or\n" +
                                        "you don't have an internet connection."
                                ,"Couldn't download Forge!",
                                JOptionPane.ERROR_MESSAGE);
                        throw e;
                    }

                    // INSTALL FORGE
                    try {
                        runForgeInstaller(serverDirectory);
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(Main.appFrame,
                                "Couldn't install Forge.\n\n" +
                                        "Perhaps you don't have an internet connection?"
                                ,"Couldn't install Forge!",
                                JOptionPane.ERROR_MESSAGE);
                        throw e;
                    }

                    // FIX LAUNCHER SCRIPTS
                    try {
                        patchLaunchScriptsForForge(serverDirectory);
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(Main.appFrame,
                                "Couldn't patch Launcher Scripts.\n\n"
                                ,"Couldn't patch Launcher Scripts!",
                                JOptionPane.ERROR_MESSAGE);
                        throw e;
                    }

                    // REMOVE THE FORGE INSTALLER
                    removeForgeInstaller(serverDirectory);
                }

                // IF FABRIC
                if (order.getServerType() == ServerType.FABRIC) {
                    // DOWNLOAD FABRIC
                    try {
                        downloadFabric(serverDirectory);
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(Main.appFrame,
                                "Couldn't download Fabric.\n\n" +
                                        "Perhaps you don't have an Internet Connection?"
                                ,"Couldn't download Fabric!",
                                JOptionPane.ERROR_MESSAGE);
                        throw e;
                    }

                    // INSTALL FABRIC
                    try {
                        runFabricInstaller(serverDirectory, order.getVersion());
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(Main.appFrame,
                                "Couldn't install Fabric.\n\n" +
                                        "Perhaps you don't have an internet connection?"
                                ,"Couldn't install Fabric!",
                                JOptionPane.ERROR_MESSAGE);
                        throw e;
                    }

                    // FIX LAUNCHER SCRIPTS
                    try {
                        patchLaunchScriptsForFabric(serverDirectory);
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(Main.appFrame,
                                "Couldn't patch Launcher Scripts.\n\n"
                                ,"Couldn't patch Launcher Scripts!",
                                JOptionPane.ERROR_MESSAGE);
                        throw e;
                    }

                    // REMOVE THE FABRIC INSTALLER
                    removeFabricInstaller(serverDirectory);
                }

                // IF PAPER
                if (order.getServerType() == ServerType.PAPER) {
                    // DOWNLOAD PAPER
                    try {
                        downloadPaper(serverDirectory, order.getVersion());
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(Main.appFrame,
                                "Couldn't download Forge.\n\n" +
                                        "Perhaps you don't have an internet connection?"
                                ,"Couldn't download Forge!",
                                JOptionPane.ERROR_MESSAGE);
                        throw e;
                    }

                    // FIX LAUNCHER SCRIPTS
                    try {
                        patchLaunchScriptsForPaper(serverDirectory);
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(Main.appFrame,
                                "Couldn't patch Launcher Scripts.\n\n"
                                ,"Couldn't patch Launcher Scripts!",
                                JOptionPane.ERROR_MESSAGE);
                        throw e;
                    }
                }

                // INITIALISE MINECRAFT SERVER
                try {
                    runAndStopServer(serverDirectory);
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(Main.appFrame,
                            "Couldn't initialise server.\n\n" +
                                    "Your server will still work, unless\n" +
                                    "something fucked up real bad."
                            , "Couldn't initialise server!",
                            JOptionPane.WARNING_MESSAGE);
                }


                addToConsole("Finished!");
                progressBar.setValue(0);
                progressBar.setIndeterminate(false);
                progressBar.setString("FINISHED!");
                progressBar.setFont(new Font(progressBar.getFont().getName(), Font.BOLD, progressBar.getFont().getSize() + 20));
                Main.setTitleInfo("Finished!");
                JOptionPane.showMessageDialog(Main.appFrame,
                        "The Server has been successfully installed!",
                        "Finished!", JOptionPane.INFORMATION_MESSAGE);

                SwingUtilities.invokeLater(Main::switchToServerTypeSelector);

            } catch (IOException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(Main::switchToConfirmation);
            }
        }).start();
    }

    private static void downloadServer(String serverDirectory, String version) throws IOException {
        SwingUtilities.invokeLater(() -> {
            progressBar.setIndeterminate(true);
            progressBar.setString("DOWNLOADING SERVER...");
            progressBar.setValue(0);
        });

        URLConnection conn = IOUtils.connectionFromURL(VanillaHandler.getURLFromVersion(version));
        try (InputStream is = conn.getInputStream();
             OutputStream os = Files.newOutputStream(Paths.get(serverDirectory, "server.jar"))) {

            SwingUtilities.invokeLater(() -> progressBar.setIndeterminate(false));

            byte[] buffer = new byte[16384];

            long totalBytesToDownload = conn.getContentLengthLong();

            long totalBytesRead = 0;
            int prevPercentage = 0;

            int bytesRead;

            while ((bytesRead = is.read(buffer)) != -1) {

                totalBytesRead += bytesRead;

                int currPercentage = (int) (totalBytesRead * 100 / totalBytesToDownload);
                if (currPercentage != prevPercentage) {
                    SwingUtilities.invokeLater(() -> progressBar.setValue(currPercentage));
                }
                prevPercentage = currPercentage;

                os.write(buffer, 0, bytesRead);
            }
        }

        addToConsole("Downloaded Server.");
        System.out.println("Downloaded server");
    }

    private static void copyWorld(String world, String serverDirectory) throws IOException {
        SwingUtilities.invokeLater(() -> {
            progressBar.setIndeterminate(true);
            progressBar.setValue(0);
            progressBar.setString("Copying World...");
        });


        Path worldPath = Paths.get(world);
        Path serverWorldPath = Paths.get(serverDirectory, "world");

        Files.walkFileTree(worldPath, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path realPath = serverWorldPath.resolve(worldPath.relativize(dir));
                if (!Files.exists(realPath)) {
                    Files.createDirectory(realPath);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path copyPath = serverWorldPath.resolve(worldPath.relativize(file));
                Files.copy(file, copyPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Copied " + worldPath.relativize(file));
                return FileVisitResult.CONTINUE;
            }

            @Override public FileVisitResult visitFileFailed(Path file, IOException exc) { return FileVisitResult.CONTINUE; }
            @Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) { return FileVisitResult.CONTINUE; }
        });

        addToConsole("Copied World.");
    }

    private static void createLaunchScripts(String serverDirectory) throws IOException {
        Files.copy(ServerInstaller.class.getResourceAsStream("launch_scripts/launch.bat"), Paths.get(serverDirectory, "launch.bat"), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(ServerInstaller.class.getResourceAsStream("launch_scripts/launch.sh"), Paths.get(serverDirectory, "launch.sh"), StandardCopyOption.REPLACE_EXISTING);
        addToConsole("Created launch scripts.");
    }

    private static void agreeToEula(String serverDirectory) throws IOException {
        Files.write(Paths.get(serverDirectory, "eula.txt"), "eula=true".getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        addToConsole("Agreed to EULA.");
    }

    private static void runAndStopServer(String serverDirectory) throws IOException {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(0);
            progressBar.setString("INITIALISING SERVER...");
            progressBar.setIndeterminate(true);
        });
        boolean runsOnWindows = System.getProperty("os.name").startsWith("Windows");
        ProcessBuilder pb = new ProcessBuilder(
                Paths.get(serverDirectory,
                        "launch." + (runsOnWindows ? "bat" : "sh"))
                        .toAbsolutePath().toString());
        pb.directory(new File(serverDirectory));
        Process p = pb.start();
        InputStream is = p.getInputStream();
        InputStream err = p.getErrorStream();

        p.getOutputStream().write("stop\nstop\nstop\nstop\nstop\nstop\n".getBytes());
        p.getOutputStream().flush();

        new Thread(() -> {
            LinkedList<Character> lastFourCharacters = new LinkedList<>();
            LinkedList<Character> done = new LinkedList<>(Arrays.asList('D', 'o', 'n', 'e'));

            try {
                int currByte;

                while ( (currByte = err.read()) != -1 ) {
                    char byteChar = (char) currByte;
                    System.out.print(byteChar);
                    lastFourCharacters.add(byteChar);
                    if (lastFourCharacters.size() > 4) {
                        lastFourCharacters.removeFirst();
                    }
                    if (lastFourCharacters.equals(done)) {
                        SwingUtilities.invokeLater(() -> addToConsole("Initialised Server."));
                        SwingUtilities.invokeLater(() -> progressBar.setString("STOPPING SERVER..."));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        int currByte;

        LinkedList<Character> lastFourCharacters = new LinkedList<>();
        LinkedList<Character> done = new LinkedList<>(Arrays.asList('D', 'o', 'n', 'e'));

        while ( (currByte = is.read()) != -1 ) {
            char byteChar = (char) currByte;
            System.out.print(byteChar);
            lastFourCharacters.add(byteChar);
            if (lastFourCharacters.size() > 4) {
                lastFourCharacters.removeFirst();
            }
            if (lastFourCharacters.equals(done)) {
                SwingUtilities.invokeLater(() -> addToConsole("Initialised Server."));
                SwingUtilities.invokeLater(() -> progressBar.setString("STOPPING SERVER..."));
            }
        }
        addToConsole("Stopped Server.");
    }

    private static void downloadForge(String serverDirectory, String version) throws IOException {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(0);
            progressBar.setIndeterminate(true);
            progressBar.setString("DOWNLOADING FORGE...");
        });

        URLConnection conn = IOUtils.connectionFromURL(ForgeHandler.getURLFromVersion(version));
        try (InputStream is = conn.getInputStream();
             OutputStream os = Files.newOutputStream(Paths.get(serverDirectory, "forge-installer.jar"))) {

            SwingUtilities.invokeLater(() -> progressBar.setIndeterminate(false));

            byte[] buffer = new byte[16384];

            long totalBytesToDownload = conn.getContentLengthLong();

            long totalBytesRead = 0;
            int prevPercentage = 0;

            int bytesRead;

            while ((bytesRead = is.read(buffer)) != -1) {

                totalBytesRead += bytesRead;

                int currPercentage = (int) (totalBytesRead * 100 / totalBytesToDownload);
                if (currPercentage != prevPercentage) {
                    SwingUtilities.invokeLater(() -> progressBar.setValue(currPercentage));
                }
                prevPercentage = currPercentage;

                os.write(buffer, 0, bytesRead);
            }
        }

        addToConsole("Downloaded forge installer.");
        System.out.println("Downloaded forge installer.");
    }

    private static void runForgeInstaller(String serverDirectory) throws IOException {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(0);
            progressBar.setString("INSTALLING FORGE...");
            progressBar.setIndeterminate(true);
        });
        Process p = new ProcessBuilder("java",
                "-jar",
                Paths.get(serverDirectory, "forge-installer.jar").toAbsolutePath().toString(),
                "--installServer")
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .directory(new File(serverDirectory))
                .start();
        String forgeResult = IOUtils.streamToString(p.getInputStream());
        System.out.println(forgeResult);
        if (forgeResult.contains("There was an error during installation")) {
            throw new IOException("Couldn't install forge");
        }
        addToConsole("Installed Forge.");
    }

    private static void removeForgeInstaller(String serverDirectory) {
        try {
            Files.deleteIfExists(Paths.get(serverDirectory, "forge-installer.jar"));
            Files.deleteIfExists(Paths.get(serverDirectory, "forge-installer.jar.log"));
            addToConsole("Cleaned forge installer.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void patchLaunchScriptsForForge(String serverDirectory) throws IOException {
        Optional<String> forgeLauncher = Files.list(Paths.get(serverDirectory))
                .map(p -> p.getFileName().toString())
                .filter(p -> p.matches("forge-\\d+\\.\\d+(\\.\\d+)?.*?\\.jar"))
                .findFirst();

        if (forgeLauncher.isPresent()) {
            System.out.println(forgeLauncher.get());

            for (final String ls : new String[]{"launch.bat", "launch.sh"}) {
                Path p = Paths.get(serverDirectory, ls);
                String content = new String(Files.readAllBytes(p));
                content = content.replace("server.jar", forgeLauncher.get());
                Files.write(p, content.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
            }

        } else {
            throw new FileNotFoundException("Forge launcher doesn't exist");
        }

        addToConsole("Patched Launcher Scripts.");
    }

    private static void downloadPaper(String serverDirectory, String version) throws IOException {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(0);
            progressBar.setIndeterminate(true);
            progressBar.setString("DOWNLOADING PAPER...");
        });

        URLConnection conn = IOUtils.connectionFromURL(PaperHandler.getURLFromVersion(version));
        try (InputStream is = conn.getInputStream();
             OutputStream os = Files.newOutputStream(Paths.get(serverDirectory, "paper.jar"))) {

            SwingUtilities.invokeLater(() -> progressBar.setIndeterminate(false));

            byte[] buffer = new byte[16384];

            long totalBytesToDownload = conn.getContentLengthLong();

            long totalBytesRead = 0;
            int prevPercentage = 0;

            int bytesRead;

            while ((bytesRead = is.read(buffer)) != -1) {

                totalBytesRead += bytesRead;

                int currPercentage = (int) (totalBytesRead * 100 / totalBytesToDownload);
                if (currPercentage != prevPercentage) {
                    SwingUtilities.invokeLater(() -> progressBar.setValue(currPercentage));
                }
                prevPercentage = currPercentage;

                os.write(buffer, 0, bytesRead);
            }
        }

        addToConsole("Downloaded PaperMC.");
        System.out.println("Downloaded PaperMC.");
    }

    private static void patchLaunchScriptsForPaper(String serverDirectory) throws IOException {
        for (final String ls : new String[]{"launch.bat", "launch.sh"}) {
            Path p = Paths.get(serverDirectory, ls);
            String content = new String(Files.readAllBytes(p));
            content = content.replace("server.jar", "paper.jar");
            Files.write(p, content.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        }
        addToConsole("Patched launch scripts for PaperMC.");
    }

    private static void downloadFabric(String serverDirectory) throws IOException {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(0);
            progressBar.setIndeterminate(true);
            progressBar.setString("DOWNLOADING FABRIC...");
        });

        URLConnection conn = IOUtils.connectionFromURL(FabricHandler.getInstaller());
        try (InputStream is = conn.getInputStream();
             OutputStream os = Files.newOutputStream(Paths.get(serverDirectory, "fabric-installer.jar"))) {

            SwingUtilities.invokeLater(() -> progressBar.setIndeterminate(false));

            byte[] buffer = new byte[16384];

            long totalBytesToDownload = conn.getContentLengthLong();

            long totalBytesRead = 0;
            int prevPercentage = 0;

            int bytesRead;

            while ((bytesRead = is.read(buffer)) != -1) {

                totalBytesRead += bytesRead;

                int currPercentage = (int) (totalBytesRead * 100 / totalBytesToDownload);
                if (currPercentage != prevPercentage) {
                    SwingUtilities.invokeLater(() -> progressBar.setValue(currPercentage));
                }
                prevPercentage = currPercentage;

                os.write(buffer, 0, bytesRead);
            }
        }

        addToConsole("Downloaded Fabric Installer.");
        System.out.println("Downloaded Fabric Installer.");
    }

    private static void runFabricInstaller(String serverDirectory, String version) throws IOException {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(0);
            progressBar.setString("INSTALLING FABRIC...");
            progressBar.setIndeterminate(true);
        });
        Process p = new ProcessBuilder("java",
                "-jar",
                Paths.get(serverDirectory, "fabric-installer.jar").toAbsolutePath().toString(),
                "server",
                version)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .directory(new File(serverDirectory))
                .start();
        String fabricResult = IOUtils.streamToString(p.getInputStream());
        System.out.println(fabricResult);
        if (!fabricResult.contains("Done, start server")) {
            throw new IOException("Couldn't install fabric");
        }
        addToConsole("Installed Fabric.");
    }

    private static void removeFabricInstaller(String serverDirectory) {
        try {
            Files.deleteIfExists(Paths.get(serverDirectory, "fabric-installer.jar"));
            addToConsole("Cleaned Fabric Installer.");
            System.out.println("Cleaned Fabric Installer");
        } catch (IOException e) {
            System.err.println("Couldn't delete fabric-installer.jar");
        }
    }

    private static void patchLaunchScriptsForFabric(String serverDirectory) throws IOException {
        for (final String ls : new String[]{"launch.bat", "launch.sh"}) {
            Path p = Paths.get(serverDirectory, ls);
            String content = new String(Files.readAllBytes(p));
            content = content.replace("server.jar", "fabric-server-launch.jar");
            Files.write(p, content.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        }
        addToConsole("Patched launch scripts for Fabric.");
    }
}
