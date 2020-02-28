package cybercat3.jaibel_server_creator;

import cybercat3.jaibel_server_creator.version_handlers.FabricHandler;
import cybercat3.jaibel_server_creator.version_handlers.ForgeHandler;
import cybercat3.jaibel_server_creator.version_handlers.PaperHandler;
import cybercat3.jaibel_server_creator.version_handlers.VanillaHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.util.stream.Stream;

import static cybercat3.jaibel_server_creator.GUIConsts.*;

public class Main {
    private static Runnable onEscape;

    public static JFrame appFrame;

    private static InstallationOrder installationOrder = new InstallationOrder();

    public static Set<String> launchArgs;

    public static void main(String[] args) {
        launchArgs = new HashSet<>(Arrays.asList(args));

        new Thread(() -> {
            try {
                System.out.println("VanillaVersions: " + VanillaHandler.getVersions());
            } catch (IOException ignored) {}
        }).start();
        new Thread(() -> {
            try {
                System.out.println("ForgeVersions: " + ForgeHandler.getVersions());
            } catch (IOException ignored) {}
        }).start();
        new Thread(() -> {
            try {
                System.out.println("FabricVersions: " + FabricHandler.getVersions());
            } catch (IOException ignored) {}
        }).start();
        new Thread(() -> {
            try {
                System.out.println("PaperVersions: " + PaperHandler.getVersions());
            } catch (IOException ignored) {}
        }).start();

        if (!launchArgs.contains("dev-mode")) {
            Updater.run();
        }

        appFrame = getApp();
        appFrame.setVisible(true);
        switchToServerTypeSelector();

        appFrame.getRootPane().registerKeyboardAction(
                e -> onEscape.run(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private static JFrame getApp() {
        JFrame jframe = new JFrame();
        jframe.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);

        Dimension screenDimensions = Toolkit.getDefaultToolkit().getScreenSize();
        jframe.setLocation(screenDimensions.width / 2 - WINDOW_WIDTH / 2,
                           screenDimensions.height / 3 - WINDOW_HEIGHT / 3);
        jframe.setResizable(false);
        jframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        return jframe;
    }

    public static void setTitleInfo(String s) {
        String title = "Jaibel Server Creator";
        if (s != null && s.length() > 0) {
            appFrame.setTitle(title + " - " + s);
        } else {
            appFrame.setTitle(title);
        }
    }

    public static void switchToServerTypeSelector() {
        appFrame.getContentPane().removeAll();

        appFrame.setLayout(new GridLayout(1, 3));

        JButton jb1 = new JButton("VANILLA");
        JButton jb2 = new JButton("FORGE");
        JButton jb3 = new JButton("PAPER");
        JButton jb4 = new JButton("FABRIC");

        jb1.setBackground(VANILLA_COLOR);
        jb2.setBackground(FORGE_COLOR);
        jb3.setBackground(PAPER_COLOR);
        jb4.setBackground(FABRIC_COLOR);

        jb1.addActionListener(e -> {
            installationOrder.setServerType(ServerType.VANILLA);
            switchToVersionSelector(ServerType.VANILLA);
        });
        jb2.addActionListener(e -> {
            installationOrder.setServerType(ServerType.FORGE);
            switchToVersionSelector(ServerType.FORGE);
        });
        jb3.addActionListener(e -> {
            installationOrder.setServerType(ServerType.PAPER);
            switchToVersionSelector(ServerType.PAPER);
        });
        jb4.addActionListener(e -> {
            installationOrder.setServerType(ServerType.FABRIC);
            switchToVersionSelector(ServerType.FABRIC);
        });

        Stream.of(jb1, jb2, jb3, jb4)
                .peek(btn -> {
                    btn.setFont(new Font("Verdana", Font.BOLD, 25));
                    btn.setForeground(Color.WHITE);
                    btn.setFocusPainted(false);
                })
                .forEach(appFrame::add);

        onEscape = () -> System.exit(0);
        setTitleInfo("Select Server Type");

        appFrame.revalidate();
        appFrame.repaint();
        System.out.println("Showing ServerTypeSelector");
    }

    public static void switchToVersionSelector(ServerType serverType) {
        appFrame.getContentPane().removeAll();

        appFrame.setLayout(new FlowLayout());

        JPanel pnl = new JPanel();
        pnl.setLayout(new FlowLayout());

        JScrollPane jsp = new JScrollPane(pnl);
        jsp.setPreferredSize(new Dimension(WINDOW_WIDTH - 10, WINDOW_HEIGHT - 40));
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        appFrame.add(jsp);
        jsp.getVerticalScrollBar().setUnitIncrement(jsp.getVerticalScrollBar().getUnitIncrement() + 2);

        new Thread(() -> {
            try {
                Collection<String> versions =
                        serverType == ServerType.VANILLA ? VanillaHandler.getVersions() :
                        serverType == ServerType.FORGE   ? ForgeHandler.getVersions() :
                        serverType == ServerType.PAPER   ? PaperHandler.getVersions() :
                        serverType == ServerType.FABRIC  ? FabricHandler.getVersions() :
                        new ArrayList<>();

                SwingUtilities.invokeLater(() -> {
                    pnl.setPreferredSize(new Dimension(WINDOW_WIDTH - 20, versions.size() / 5 * 120 + 200));

                    versions.forEach(v -> {
                        JButton btn = new JButton(v);
                        btn.setPreferredSize(new Dimension(120, 120));
                        pnl.add(btn);
                        btn.setForeground(Color.WHITE);
                        switch (serverType) {
                            case VANILLA: btn.setBackground(VANILLA_COLOR);  break;
                            case FORGE:   btn.setBackground(FORGE_COLOR);    break;
                            case PAPER:   btn.setBackground(PAPER_COLOR);    break;
                            case FABRIC:  btn.setBackground(FABRIC_COLOR);   break;
                        }
                        btn.setFont(new Font("Verdana", Font.BOLD, 20));
                        btn.setFocusPainted(false);
                        btn.setBorder(BorderFactory.createLineBorder(new Color(0,0,0,100), 3, true));
                        btn.addActionListener(e -> {
                            installationOrder.setVersion(v);
                            switchToPreexistingWorldSelector();
                        });
                    });
                });
            } catch (IOException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(Main.appFrame,
                            "Couldn't get versions.\n\n" +
                                    "This is probably because you don't\n" +
                                    "have an Internet connection.\n" +
                                    "It could also be my fault, if it is,\n" +
                                    "feel free to post an issue on Github."
                            ,"Couldn't get versions!",
                            JOptionPane.ERROR_MESSAGE);
                    switchToServerTypeSelector();
                });
            }
        }).start();

        onEscape = Main::switchToServerTypeSelector;
        setTitleInfo("Select Minecraft Version");

        appFrame.revalidate();
        appFrame.repaint();
        System.out.println("Showing VersionSelector");
    }

    public static void switchToPreexistingWorldSelector() {
        appFrame.getContentPane().removeAll();

        appFrame.setLayout(new BorderLayout());

        JButton title = new JButton("Use a preexisting world?");
        JButton yesBtn = new JButton("YES");
        JButton noBtn = new JButton("NO");

        title.setBackground(FORGE_COLOR);
        yesBtn.setBackground(VANILLA_COLOR);
        noBtn.setBackground(PAPER_COLOR);

        title.setFocusPainted(false);
        title.setBorderPainted(false);
        title.setForeground(Color.WHITE);
        title.setMargin(new Insets(10,0,10,0));
        title.setFont(new Font("Verdana", Font.PLAIN, 30));

        Stream.of(yesBtn, noBtn).forEach(btn -> {
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Verdana", Font.BOLD, 35));
            btn.setFocusPainted(false);
        });

        appFrame.add(title, BorderLayout.PAGE_START);
        JPanel pnl = new JPanel(new GridLayout(1, 2));
        pnl.add(yesBtn);
        pnl.add(noBtn);
        appFrame.add(pnl, BorderLayout.CENTER);

        noBtn.addActionListener(e -> {
            installationOrder.usePreexistingWorld(false);
            switchToServerDestinationSelector();
        });

        yesBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select world");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);

            for (String path : new String[]{
                    ServerInstaller.MINECRAFT_SAVES_PATH_LINUX,
                    ServerInstaller.MINECRAFT_SAVES_PATH_WINDOWS,
                    ServerInstaller.MINERCAFT_SAVES_PATH_MAC_OS}) {
                File f = new File(path);
                if (f.exists()) {
                    chooser.setCurrentDirectory(f);
                    break;
                }

            }
            int result = chooser.showOpenDialog(appFrame);
            if (result == JFileChooser.APPROVE_OPTION) {
                installationOrder.usePreexistingWorld(true);
                installationOrder.setPathToWorld(chooser.getSelectedFile().getAbsolutePath());
                switchToServerDestinationSelector();
            }
        });

        onEscape = () -> switchToVersionSelector(installationOrder.getServerType());
        setTitleInfo("Use a Preexisting World?");

        appFrame.revalidate();
        appFrame.repaint();
        System.out.println("Showing Use a Preexisting World Selector");
    }

    public static void switchToServerDestinationSelector() {
        appFrame.getContentPane().removeAll();

        appFrame.setLayout(new BorderLayout());

        JButton title = new JButton("Where should the server be made?");
        JButton btn = new JButton("SELECT...");

        btn.setBackground(VANILLA_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Verdana", Font.BOLD, 35));
        btn.setFocusPainted(false);

        title.setBackground(FORGE_COLOR);
        title.setForeground(Color.WHITE);
        title.setFocusPainted(false);
        title.setBorderPainted(false);
        title.setMargin(new Insets(10,0,10,0));
        title.setFont(new Font("Verdana", Font.PLAIN, 30));

        appFrame.add(title, BorderLayout.PAGE_START);
        appFrame.add(btn, BorderLayout.CENTER);

        btn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select destination");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);

            chooser.setCurrentDirectory(new File(System.getProperty("user.home"), "Desktop"));

            int result = chooser.showOpenDialog(appFrame);
            if (result == JFileChooser.APPROVE_OPTION) {
                installationOrder.setPathToTarget(chooser.getSelectedFile().getAbsolutePath());
                switchToConfirmation();
            }
        });

        onEscape = Main::switchToPreexistingWorldSelector;
        setTitleInfo("Select Server Location");

        appFrame.revalidate();
        appFrame.repaint();
        System.out.println("Showing ServerDestinationSelector");
    }

    public static void switchToConfirmation() {
        appFrame.getContentPane().removeAll();

        appFrame.setLayout(new BorderLayout());

        JButton title = new JButton("Confirm Details");
        title.setBackground(FORGE_COLOR);
        title.setForeground(Color.WHITE);
        title.setFocusPainted(false);
        title.setBorderPainted(false);
        title.setMargin(new Insets(10,0,10,0));
        title.setFont(new Font("Verdana", Font.PLAIN, 30));
        appFrame.add(title, BorderLayout.PAGE_START);

        JButton confirmBtn = new JButton("CREATE SERVER");
        confirmBtn.addActionListener(e -> {
            System.out.printf("Creating a %s %s Server%n", installationOrder.getServerType(), installationOrder.getVersion());
            switchToLoadingScreen();
            SwingUtilities.invokeLater(() -> ServerInstaller.installServer(installationOrder));
        });
        confirmBtn.setBackground(VANILLA_COLOR);
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setFocusPainted(false);
        confirmBtn.setMargin(new Insets(10, 0, 10, 0));
        confirmBtn.setFont(new Font("Verdana", Font.BOLD, 35));
        appFrame.add(confirmBtn, BorderLayout.PAGE_END);

        JPanel jpl = new JPanel(new GridLayout(4, 2));
        String[] texts = {
                "ServerType:", installationOrder.getServerType().name(),
                "Minecraft Version:", installationOrder.getVersion(),
                "World:", installationOrder.usePreexistingWorld() ? limitString(installationOrder.getPathToWorld(), 30) : "GENERATE",
                "Destination:", limitString(installationOrder.getPathToTarget(), 30)};
        for (String text : texts) {
            JLabel label = new JLabel(text, SwingConstants.CENTER);
            label.setFont(new Font("Verdana", Font.BOLD, 18));
            jpl.add(label);
        }

        appFrame.add(jpl, BorderLayout.CENTER);

        onEscape = Main::switchToServerDestinationSelector;
        setTitleInfo("Confirm Details");

        appFrame.revalidate();
        appFrame.repaint();
        System.out.println("Showing Confirmation");
    }

    public static void switchToLoadingScreen() {
        appFrame.getContentPane().removeAll();

        appFrame.setLayout(new BorderLayout());

        JTextArea console = new JTextArea(10, 30);
        console.setEditable(false);
        console.setFont(new Font("Verdana", Font.PLAIN, 20));
        console.setText("Started.");

        JScrollPane scrollPane = new JScrollPane(console, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        appFrame.add(scrollPane, BorderLayout.CENTER);


        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("Verdana", Font.PLAIN, 32));
        progressBar.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT / 3));

        appFrame.add(progressBar, BorderLayout.PAGE_END);

        onEscape = () -> {};
        setTitleInfo("Installing...");

        appFrame.revalidate();
        appFrame.repaint();
        ServerInstaller.setConsole(console);
        ServerInstaller.setProgressBar(progressBar);
        System.out.println("Showing Loading Screen");
    }

    private static String limitString(String s, int maxLength) {
        return s.length() > maxLength ?
                "..." + s.substring(
                        s.length() - maxLength + 3
                ) : s;
    }

}
