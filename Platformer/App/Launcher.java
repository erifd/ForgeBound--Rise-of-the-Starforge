package App;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

public class Launcher {

    // Base URL of your backend API
    private static final String BASE_URL = "http://localhost:3000/api";
    
    // Debug mode flag
    private static boolean debugMode = false;
    
    // Debug console process
    private static Process debugConsoleProcess = null;
    
    // Backend server process
    private static Process backendProcess = null;
    
    // Get base directory for relative paths
    private static final File BASE_DIR = new File(System.getProperty("user.dir"));
    
    // Status label for displaying messages
    private static JLabel statusLabel;

    // Helper method to update status label
    private static void updateStatus(String message, Color color) {
        if (statusLabel != null) {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText(message);
                statusLabel.setForeground(color);
            });
        }
        // Always print to console - it will only be visible if debug mode spawns CMD
        System.out.println("[STATUS] " + message);
    }
    
    // Open a CMD window for debug output
    private static void openDebugConsole() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "cmd.exe", 
                "/k", 
                "echo Debug Console - Launcher Output && echo. && echo Waiting for messages..."
            );
            pb.inheritIO();
            debugConsoleProcess = pb.start();
            System.out.println("[DEBUG] Debug console opened");
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to open debug console: " + e.getMessage());
        }
    }
    
    // Close the debug console
    private static void closeDebugConsole() {
        if (debugConsoleProcess != null && debugConsoleProcess.isAlive()) {
            debugConsoleProcess.destroy();
            debugConsoleProcess = null;
            System.out.println("[DEBUG] Debug console closed");
        }
    }

    // Helper method: send POST request
    private static String sendPost(String endpoint, String jsonData) {
        try {
            URL url = new URL(BASE_URL + endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int status = conn.getResponseCode();
            InputStream is = (status < 400) ? conn.getInputStream() : conn.getErrorStream();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }
                return response.toString();
            }
        } catch (Exception e) {
            updateStatus("Network error: " + e.getMessage(), Color.RED);
            if (debugMode) {
                e.printStackTrace();
            }
            return "{\"error\":\"Request failed\"}";
        }
    }

    // ============ SEASONAL LOGO CHECKER ============
    
    /**
     * Determines the appropriate seasonal logo based on the current date
     * using astronomical season dates for 2025-2026.
     */
    private static String getCurrentSeasonLogo() {
        LocalDate today = LocalDate.now();
        return getSeasonLogoForDate(today);
    }
    
    /**
     * Determines the appropriate seasonal logo for a given date.
     */
    private static String getSeasonLogoForDate(LocalDate date) {
        int year = date.getYear();
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();
        
        // Check 2025 seasons
        if (year == 2025) {
            // Spring 2025: Mar 20 - Jun 19
            if ((month == 3 && day >= 20) || (month == 4) || (month == 5) || (month == 6 && day <= 19)) {
                return isEquinoxOrSolstice(date, year) ? "Logo Spring Equinox.png" : "Logo Spring.png";
            }
            // Summer 2025: Jun 20 - Sep 21
            else if ((month == 6 && day >= 20) || (month == 7) || (month == 8) || (month == 9 && day <= 21)) {
                return isEquinoxOrSolstice(date, year) ? "Logo Summer Solstice.png" : "Logo Summer.png";
            }
            // Fall 2025: Sep 22 - Dec 20
            else if ((month == 9 && day >= 22) || (month == 10) || (month == 11) || (month == 12 && day <= 20)) {
                return isEquinoxOrSolstice(date, year) ? "Logo Automnal Equinox.png" : "Logo Fall.png";
            }
            // Winter 2025: Dec 21 - end of year (continues into 2026)
            else if (month == 12 && day >= 21) {
                return "Logo Winter Solstice.png";
            }
            // Winter 2025 (beginning of year): Jan 1 - Mar 19
            else if ((month == 1) || (month == 2) || (month == 3 && day <= 19)) {
                return "Logo Winter.png";
            }
        }
        
        // Check 2026 seasons
        else if (year == 2026) {
            // Winter 2025-2026 (continuation): Jan 1 - Mar 19
            if ((month == 1) || (month == 2) || (month == 3 && day <= 19)) {
                return "Logo Winter.png";
            }
            // Spring 2026: Mar 20 - Jun 20
            else if ((month == 3 && day >= 20) || (month == 4) || (month == 5) || (month == 6 && day <= 20)) {
                return isEquinoxOrSolstice(date, year) ? "Logo Spring Equinox.png" : "Logo Spring.png";
            }
            // Summer 2026: Jun 21 - Sep 21
            else if ((month == 6 && day >= 21) || (month == 7) || (month == 8) || (month == 9 && day <= 21)) {
                return isEquinoxOrSolstice(date, year) ? "Logo Summer Solstice.png" : "Logo Summer.png";
            }
            // Fall 2026: Sep 22 - Dec 20
            else if ((month == 9 && day >= 22) || (month == 10) || (month == 11) || (month == 12 && day <= 20)) {
                return isEquinoxOrSolstice(date, year) ? "Logo Automnal Equinox.png" : "Logo Fall.png";
            }
            // Winter 2026: Dec 21 - end of year (continues into 2027)
            else if (month == 12 && day >= 21) {
                return "Logo Winter Solstice.png";
            }
        }
        
        // Default to a general seasonal approach for other years
        return getGeneralSeasonLogo(month, day);
    }
    
    /**
     * Checks if the date is an equinox or solstice day.
     */
    private static boolean isEquinoxOrSolstice(LocalDate date, int year) {
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();
        
        if (year == 2025) {
            if (month == 3 && day == 20) return true;  // Spring Equinox
            if (month == 6 && day == 20) return true;  // Summer Solstice
            if (month == 9 && day == 22) return true;  // Fall Equinox
            if (month == 12 && day == 21) return true; // Winter Solstice
        } else if (year == 2026) {
            if (month == 3 && day == 20) return true;  // Spring Equinox
            if (month == 6 && day == 21) return true;  // Summer Solstice
            if (month == 9 && day == 22) return true;  // Fall Equinox
            if (month == 12 && day == 21) return true; // Winter Solstice
        }
        
        return false;
    }
    
    /**
     * General season determination for years outside 2025-2026.
     */
    private static String getGeneralSeasonLogo(int month, int day) {
        if ((month == 3 && day >= 20) || (month == 4) || (month == 5) || (month == 6 && day <= 20)) {
            return "Logo Spring.png";
        }
        else if ((month == 6 && day >= 21) || (month == 7) || (month == 8) || (month == 9 && day <= 21)) {
            return "Logo Summer.png";
        }
        else if ((month == 9 && day >= 22) || (month == 10) || (month == 11) || (month == 12 && day <= 20)) {
            return "Logo Fall.png";
        }
        else {
            return "Logo Winter.png";
        }
    }
    
    /**
     * Gets the full path to the seasonal logo file.
     */
    private static String getSeasonalLogoPath() {
        String logoFile = getCurrentSeasonLogo();
        return "Assets/Logos/" + logoFile;
    }
    
    // ============ END SEASONAL LOGO CHECKER ============

    // Custom animated Play button
    static class AnimatedPlayButton extends JButton {
        private boolean hovered = false;
        private int grow = 0;
        private double angle = 0;

        public AnimatedPlayButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(Color.WHITE);

            Timer timer = new Timer(50, e -> {
                if (hovered) {
                    grow = Math.min(10, grow + 1);
                    angle += 0.1;
                } else {
                    grow = Math.max(0, grow - 1);
                    angle = 0;
                }
                repaint();
            });
            timer.start();

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 20 + grow;
            int pad = 5;

            g2.rotate(Math.sin(angle) * 0.1, w / 2.0, h / 2.0);

            g2.setColor(Color.GREEN.darker());
            g2.fillRoundRect(pad, pad, w - 2 * pad, h - 2 * pad, arc, arc);

            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            int textW = fm.stringWidth(getText());
            int textH = fm.getAscent();
            g2.drawString(getText(), (w - textW) / 2, (h + textH) / 2 - 4);

            g2.dispose();
        }
    }

    private static void launchGame(JFrame parentFrame, String username) {
        try {
            updateStatus("Launching game...", Color.BLUE);
            
            // Check for Python script - it's in the same directory since we're in Platformer
            File scriptFile = new File(BASE_DIR, "combination");
            if (!scriptFile.exists()) {
                scriptFile = new File(BASE_DIR, "combination.py");
                if (!scriptFile.exists()) {
                    updateStatus("Game script not found!", Color.RED);
                    JOptionPane.showMessageDialog(parentFrame, 
                        "Game script not found in: " + BASE_DIR.getAbsolutePath());
                    return;
                }
            }
            
            String pythonPath = "python";
            String scriptPath = scriptFile.getAbsolutePath();

            // Pass username as argument to the game
            ProcessBuilder pb;
            if (username != null && !username.isEmpty()) {
                pb = new ProcessBuilder(pythonPath, scriptPath, username);
            } else {
                pb = new ProcessBuilder(pythonPath, scriptPath, "guest");
            }
            
            pb.directory(BASE_DIR);
            
            // Only show console output if debug mode is enabled
            if (debugMode) {
                pb.inheritIO();
            }
            
            pb.start();

            // Stop backend server when game launches
            stopBackendServer();
            closeDebugConsole();
            parentFrame.dispose();
            System.exit(0);

        } catch (Exception ex) {
            updateStatus("Error launching game: " + ex.getMessage(), Color.RED);
            if (debugMode) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(parentFrame, "Error launching game: " + ex.getMessage());
        }
    }
    
    private static void startBackendServer() {
        try {
            updateStatus("Starting backend server...", Color.BLUE);
            
            // Determine backend location based on where we're running from
            File backendDir;
            
            // Check if we're in Platformer subfolder or project root
            if (BASE_DIR.getName().equals("Platformer")) {
                // Running from Platformer folder: go up one level, then into platformer-auth-backend
                backendDir = new File(BASE_DIR.getParentFile(), "platformer-auth-backend");
            } else {
                // Running from project root: backend is sibling folder
                backendDir = new File(BASE_DIR, "platformer-auth-backend");
            }
            
            System.err.println("[DEBUG] BASE_DIR: " + BASE_DIR.getAbsolutePath());
            System.err.println("[DEBUG] Looking for backend at: " + backendDir.getAbsolutePath());
            
            if (!backendDir.exists()) {
                updateStatus("Backend not found. Use 'Continue as Guest'.", Color.ORANGE);
                System.err.println("[ERROR] Backend directory does not exist!");
                return;
            }
            
            File serviceAccountKey = new File(backendDir, "serviceAccountKey.json");
            if (!serviceAccountKey.exists()) {
                updateStatus("Service key missing. Use 'Continue as Guest'.", Color.ORANGE);
                System.err.println("[ERROR] serviceAccountKey.json not found at: " + serviceAccountKey.getAbsolutePath());
                return;
            }
            
            String credentialsPath = serviceAccountKey.getAbsolutePath();
            String backendPath = backendDir.getAbsolutePath();
            
            String command = String.format(
                "cd '%s'; $env:GOOGLE_APPLICATION_CREDENTIALS='%s'; npm start",
                backendPath,
                credentialsPath
            );
            
            ProcessBuilder pb = new ProcessBuilder(
                "powershell.exe",
                "-NoProfile",
                "-WindowStyle", "Hidden",
                "-Command",
                command
            );
            
            pb.directory(backendDir);
            
            if (debugMode) {
                pb.inheritIO();
            } else {
                // Redirect output to null if not in debug mode
                if (System.getProperty("os.name").startsWith("Windows")) {
                    pb.redirectOutput(new File("NUL"));
                    pb.redirectError(new File("NUL"));
                } else {
                    pb.redirectOutput(new File("/dev/null"));
                    pb.redirectError(new File("/dev/null"));
                }
            }
            
            System.err.println("[DEBUG] Starting backend server...");
            backendProcess = pb.start();
            
            // Give the server a moment to start
            Thread.sleep(3000);
            
            updateStatus("Backend ready! Login/Signup enabled.", new Color(0, 150, 0));
            System.err.println("[DEBUG] Backend server started successfully");
            
        } catch (Exception e) {
            updateStatus("Backend failed. Use 'Continue as Guest'.", Color.ORANGE);
            System.err.println("[ERROR] Failed to start backend server: " + e.getMessage());
            if (debugMode) {
                e.printStackTrace();
            }
        }
    }
    
    private static void stopBackendServer() {
        if (backendProcess != null && backendProcess.isAlive()) {
            backendProcess.destroy();
            try {
                backendProcess.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                backendProcess.destroyForcibly();
            }
            if (debugMode) {
                System.out.println("[DEBUG] Backend server stopped");
            }
        }
    }

    public static void main(String[] args) {
        // Start the backend server first
        startBackendServer();
        
        // Add shutdown hook to stop server when launcher closes
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stopBackendServer();
        }));
        
        // Check for updates on startup (runs in background thread)
        UpdateChecker.checkForUpdates();

        JFrame frame = new JFrame("Forgebound Launcher");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 350);
        frame.setLayout(new BorderLayout());

        // Load and set the icon using seasonal logo
        try {
            String logoPath = getSeasonalLogoPath();
            File iconFile = new File(BASE_DIR, logoPath);
            
            // Fallback to default if seasonal logo not found
            if (!iconFile.exists()) {
                iconFile = new File(BASE_DIR, "Assets/Terrain/Solarite/lava_block.png");
            }
            
            ImageIcon icon = new ImageIcon(iconFile.getAbsolutePath());
            frame.setIconImage(icon.getImage());
            
            if (debugMode) {
                System.out.println("[DEBUG] Using logo: " + logoPath);
            }
        } catch (Exception e) {
            updateStatus("Could not load icon", Color.ORANGE);
            if (debugMode) {
                System.err.println("Could not load icon: " + e.getMessage());
            }
        }

        // Create logo panel at the top with seasonal logo
        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        try {
            String logoPath = getSeasonalLogoPath();
            File logoFile = new File(BASE_DIR, logoPath);
            
            // Fallback to default if seasonal logo not found
            if (!logoFile.exists()) {
                logoFile = new File(BASE_DIR, "Assets/Terrain/Solarite/lava_block.png");
            }
            
            ImageIcon logoIcon = new ImageIcon(logoFile.getAbsolutePath());
            Image scaledImage = logoIcon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));
            logoPanel.add(logoLabel);
            
            if (debugMode) {
                System.out.println("[DEBUG] Logo loaded: " + logoPath);
            }
        } catch (Exception e) {
            updateStatus("Could not load logo", Color.ORANGE);
            if (debugMode) {
                System.err.println("Could not load logo: " + e.getMessage());
            }
        }

        // Create form panel
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        
        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField();

        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField();

        JCheckBox showPassword = new JCheckBox("Show Password");
        JCheckBox debugCheckBox = new JCheckBox("Debug Mode");
        
        JButton loginButton = new JButton("Login");
        JButton signupButton = new JButton("Sign Up");

        formPanel.add(userLabel);
        formPanel.add(userField);
        formPanel.add(passLabel);
        formPanel.add(passField);
        formPanel.add(showPassword);
        formPanel.add(debugCheckBox);
        formPanel.add(loginButton);
        formPanel.add(signupButton);
        
        // Add status label in the empty space
        JLabel statusLabelText = new JLabel("Status:", JLabel.RIGHT);
        statusLabel = new JLabel("Initializing...", JLabel.LEFT);
        statusLabel.setForeground(Color.BLUE);
        formPanel.add(statusLabelText);
        formPanel.add(statusLabel);

        // Create guest button panel (centered)
        JPanel guestPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton guestButton = new JButton("Continue as Guest");
        guestButton.setPreferredSize(new Dimension(200, 30));
        guestPanel.add(guestButton);

        frame.add(logoPanel, BorderLayout.NORTH);
        frame.add(formPanel, BorderLayout.CENTER);
        frame.add(guestPanel, BorderLayout.SOUTH);

        // Debug mode checkbox
        debugCheckBox.addActionListener(e -> {
            debugMode = debugCheckBox.isSelected();
            if (debugMode) {
                updateStatus("Debug mode enabled", Color.BLUE);
                openDebugConsole();
                System.out.println("[DEBUG] Current seasonal logo: " + getCurrentSeasonLogo());
            } else {
                updateStatus("Debug mode disabled", Color.BLUE);
                closeDebugConsole();
            }
        });

        // Show/hide password
        showPassword.addActionListener(e -> {
            if (showPassword.isSelected()) {
                passField.setEchoChar((char) 0);
            } else {
                passField.setEchoChar('•');
            }
        });

        // Login button
        loginButton.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                updateStatus("Please enter username and password!", Color.RED);
                JOptionPane.showMessageDialog(frame, "Please enter username and password!");
                return;
            }

            updateStatus("Logging in...", Color.BLUE);
            String json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
            String response = sendPost("/login", json);
            
            System.out.println("[DEBUG] Login response: " + response);

            // Check if login was successful (no error in response)
            if (!response.contains("error")) {
                updateStatus("Login successful!", new Color(0, 150, 0));
                JOptionPane.showMessageDialog(frame, "Login successful!");

                // Open new window with Play button
                JFrame gameFrame = new JFrame("Game Launcher");
                gameFrame.setSize(500, 320);
                gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                // Set icon for game frame with seasonal logo
                try {
                    String logoPath = getSeasonalLogoPath();
                    File iconFile = new File(BASE_DIR, logoPath);
                    
                    if (!iconFile.exists()) {
                        iconFile = new File(BASE_DIR, "Assets/App/Logo/Logo Fall.png");
                    }
                    
                    ImageIcon icon = new ImageIcon(iconFile.getAbsolutePath());
                    gameFrame.setIconImage(icon.getImage());
                } catch (Exception ex) {
                    System.err.println("Could not load icon: " + ex.getMessage());
                }

                AnimatedPlayButton playButton = new AnimatedPlayButton("Play");
                playButton.setPreferredSize(new Dimension(120, 60));

                playButton.addActionListener(ev -> launchGame(gameFrame, username));

                gameFrame.setLayout(new GridBagLayout());
                gameFrame.add(playButton);
                gameFrame.setVisible(true);

                frame.dispose();
            } else {
                updateStatus("Login failed: " + response, Color.RED);
                JOptionPane.showMessageDialog(frame, "Invalid login credentials!\n" + response);
            }
        });

        // Signup button
        signupButton.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                updateStatus("Username/Password cannot be empty!", Color.RED);
                JOptionPane.showMessageDialog(frame, "Username/Password cannot be empty!");
                return;
            }

            updateStatus("Signing up...", Color.BLUE);
            String json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
            String response = sendPost("/signup", json);
            
            System.out.println("[DEBUG] Signup response: " + response);

            // Check response for success or specific errors
            if (!response.contains("error")) {
                updateStatus("User registered successfully!", new Color(0, 150, 0));
                JOptionPane.showMessageDialog(frame, "User registered successfully!");
            } else if (response.contains("User already exists")) {
                updateStatus("User already exists!", Color.ORANGE);
                JOptionPane.showMessageDialog(frame, "User already exists!");
            } else {
                updateStatus("Error: " + response, Color.RED);
                JOptionPane.showMessageDialog(frame, "Error signing up: " + response);
            }
        });

        // Guest button - launch directly to game
        guestButton.addActionListener(e -> {
            updateStatus("Launching as guest...", Color.BLUE);
            
            // Open game frame without authentication
            JFrame gameFrame = new JFrame("Game Launcher");
            gameFrame.setSize(500, 320);
            gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Set icon for game frame with seasonal logo
            try {
                String logoPath = getSeasonalLogoPath();
                File iconFile = new File(BASE_DIR, logoPath);
                
                if (!iconFile.exists()) {
                    iconFile = new File(BASE_DIR, "Assets/App/Logo/Logo Fall.png");
                }
                
                ImageIcon icon = new ImageIcon(iconFile.getAbsolutePath());
                gameFrame.setIconImage(icon.getImage());
            } catch (Exception ex) {
                System.err.println("Could not load icon: " + ex.getMessage());
            }

            AnimatedPlayButton playButton = new AnimatedPlayButton("Play");
            playButton.setPreferredSize(new Dimension(120, 60));

            playButton.addActionListener(ev -> launchGame(gameFrame, "guest"));

            gameFrame.setLayout(new GridBagLayout());
            gameFrame.add(playButton);
            gameFrame.setVisible(true);

            frame.dispose();
        });

        frame.setVisible(true);
    }
}