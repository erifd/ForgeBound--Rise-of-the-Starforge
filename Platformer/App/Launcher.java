package App;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Launcher {

    // Base URL of your backend API
    private static final String BASE_URL = "http://localhost:3000/api";

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
            e.printStackTrace();
            return "{\"error\":\"Request failed\"}";
        }
    }

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
            int arc = 20 + grow; // oval corners
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

    public static void main(String[] args) {
        JFrame frame = new JFrame("Forgebound Launcher");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        // Load and set the icon
        try {
            ImageIcon icon = new ImageIcon("Assets/Terrain/Solarite/lava_block.png");
            frame.setIconImage(icon.getImage());
        } catch (Exception e) {
            System.err.println("Could not load icon: " + e.getMessage());
        }

        // Create logo panel at the top
        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        try {
            ImageIcon logoIcon = new ImageIcon("Assets/Terrain/Solarite/lava_block.png");
            Image scaledImage = logoIcon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));
            logoPanel.add(logoLabel);
        } catch (Exception e) {
            System.err.println("Could not load logo: " + e.getMessage());
        }

        // Create form panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2));
        
        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField();

        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField();

        JCheckBox showPassword = new JCheckBox("Show Password");
        JButton loginButton = new JButton("Login");
        JButton signupButton = new JButton("Sign Up");

        formPanel.add(userLabel);
        formPanel.add(userField);
        formPanel.add(passLabel);
        formPanel.add(passField);
        formPanel.add(showPassword);
        formPanel.add(new JLabel(""));
        formPanel.add(loginButton);
        formPanel.add(signupButton);

        frame.add(logoPanel, BorderLayout.NORTH);
        frame.add(formPanel, BorderLayout.CENTER);

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
                JOptionPane.showMessageDialog(frame, "Please enter username and password!");
                return;
            }

            String json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
            String response = sendPost("/login", json);

            // Check if login was successful (no error in response)
            if (!response.contains("error")) {
                JOptionPane.showMessageDialog(frame, "Login successful!");

                // Open new window with Play button
                JFrame gameFrame = new JFrame("Game Launcher");
                gameFrame.setSize(500, 320);
                gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                // Set icon for game frame too
                try {
                    ImageIcon icon = new ImageIcon("Assets/Terrain/Solarite/lava_block.png");
                    gameFrame.setIconImage(icon.getImage());
                } catch (Exception ex) {
                    System.err.println("Could not load icon: " + ex.getMessage());
                }

                AnimatedPlayButton playButton = new AnimatedPlayButton("Play");
                playButton.setPreferredSize(new Dimension(120, 60));

                playButton.addActionListener(ev -> {
                    try {
                        String pythonPath = "python"; // adjust if needed
                        String scriptPath = "combination"; // relative path to the script

                        // Get the current working directory
                        File currentDir = new File(System.getProperty("user.dir"));

                        ProcessBuilder pb = new ProcessBuilder(pythonPath, scriptPath);
                        pb.directory(currentDir); // use current directory
                        pb.inheritIO();
                        pb.start();

                        gameFrame.dispose();
                        System.exit(0);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(gameFrame, "Error launching game!");
                    }
                });

                gameFrame.setLayout(new GridBagLayout());
                gameFrame.add(playButton);
                gameFrame.setVisible(true);

                frame.dispose();
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid login credentials!");
            }
        });

        // Signup button
        signupButton.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Username/Password cannot be empty!");
                return;
            }

            String json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
            String response = sendPost("/signup", json);

            // Check response for success or specific errors
            if (!response.contains("error")) {
                JOptionPane.showMessageDialog(frame, "User registered successfully!");
            } else if (response.contains("User already exists")) {
                JOptionPane.showMessageDialog(frame, "User already exists!");
            } else {
                JOptionPane.showMessageDialog(frame, "Error signing up!");
            }
        });

        frame.setVisible(true);
    }
}