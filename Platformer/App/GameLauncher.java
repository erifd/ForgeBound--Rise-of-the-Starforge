package App;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class GameLauncher extends JFrame {
    
    private static String username = "Guest";
    private static String password = "";
    private static final File BASE_DIR = new File(System.getProperty("user.dir"));
    private static final File SAVES_DIR = new File(BASE_DIR, "Saves");
    
    // Sign image
    private static Image signImage = null;
    
    // UI Components
    private JPanel mainPanel;
    private CardLayout cardLayout;
    
    // Sign animation - drops at 45 degree angle
    private int signYLeft = -400;
    private int signYRight = -400;
    private int targetSignY = 20; // Moved up higher to avoid buttons
    private javax.swing.Timer signDropTimer;
    private boolean leftSideDropped = false;
    
    // Sign rotation for 45 degree tilt (constant) - tilted to the left
    private double signRotation = Math.PI / 4; // +45 degrees (tilted to the left)
    
    // Rotating play button
    private double playButtonRotation = 0;
    
    public GameLauncher() {
        setTitle("Forgebound: Rise of the Starforge");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Load the sign image
        loadSignImage();
        
        // Create saves directory if it doesn't exist
        if (!SAVES_DIR.exists()) {
            SAVES_DIR.mkdirs();
        }
        
        // Setup card layout for switching screens
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Create screens
        JPanel mainMenuPanel = createMainMenuPanel();
        JPanel saveSelectionPanel = createSaveSelectionPanel();
        
        mainPanel.add(mainMenuPanel, "MAIN_MENU");
        mainPanel.add(saveSelectionPanel, "SAVE_SELECTION");
        
        add(mainPanel);
        
        // Start sign drop animation
        startSignAnimation();
    }
    
    // Load the sign image
    private void loadSignImage() {
        try {
            // Try different possible locations for the sign
            File signFile = new File(BASE_DIR, "Assets/App/Forgebound Sign.png");
            
            if (!signFile.exists() && BASE_DIR.getName().equals("App")) {
                // Running from App directory, go up to Platformer
                File platformerDir = BASE_DIR.getParentFile();
                signFile = new File(platformerDir, "Assets/App/Forgebound Sign.png");
            }
            
            if (!signFile.exists()) {
                // Try from Platformer root
                signFile = new File(BASE_DIR, "Platformer/Assets/App/Forgebound Sign.png");
            }
            
            if (signFile.exists()) {
                ImageIcon icon = new ImageIcon(signFile.getAbsolutePath());
                signImage = icon.getImage();
                System.out.println("Sign image loaded: " + signFile.getAbsolutePath());
            } else {
                System.err.println("Sign image not found. Checked: " + signFile.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Error loading sign image: " + e.getMessage());
        }
    }
    
    // ============ MAIN MENU PANEL ============
    private JPanel createMainMenuPanel() {
        JPanel panel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw background (placeholder - will be replaced)
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(20, 20, 40),
                    0, getHeight(), new Color(60, 40, 80)
                );
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                // Draw hanging sign
                drawHangingSign(g2);
            }
        };
        
        // Create rotating play button
        RotatingPlayButton playButton = new RotatingPlayButton();
        playButton.setBounds(300, 400, 200, 80); // Moved down to avoid sign
        playButton.addActionListener(e -> showSaveSelection());
        
        // Create leaderboard button
        JButton leaderboardButton = new JButton("Leaderboards");
        leaderboardButton.setBounds(325, 500, 150, 40); // Moved down
        styleButton(leaderboardButton);
        leaderboardButton.addActionListener(e -> showLeaderboards());
        
        panel.add(playButton);
        panel.add(leaderboardButton);
        
        // Animation timer for sign drop
        javax.swing.Timer repaintTimer = new javax.swing.Timer(16, e -> panel.repaint());
        repaintTimer.start();
        
        return panel;
    }
    
    // Draw the hanging sign with game title (scaled down, 45 degree angle)
    private void drawHangingSign(Graphics2D g2) {
        // Scaled down size: 350x350
        int signWidth = 350;
        int signHeight = 350;
        
        int signX = (800 - signWidth) / 2;
        
        // Calculate center Y position (average of both sides)
        int centerY = (signYLeft + signYRight) / 2;
        
        // Draw left chain/rope
        g2.setColor(new Color(80, 60, 40));
        g2.setStroke(new BasicStroke(4));
        g2.drawLine(signX + 80, 0, signX + 80, signYLeft);
        
        // Draw right chain/rope
        g2.drawLine(signX + signWidth - 80, 0, signX + signWidth - 80, signYRight);
        
        // Save original transform
        java.awt.geom.AffineTransform originalTransform = g2.getTransform();
        
        // Apply transformations: translate to center, rotate 45 degrees, translate back
        int centerX = signX + signWidth / 2;
        int centerYPos = centerY + signHeight / 2;
        
        g2.translate(centerX, centerYPos);
        g2.rotate(signRotation); // Apply 45 degree rotation
        g2.translate(-signWidth / 2, -signHeight / 2);
        
        if (signImage != null) {
            // Draw the actual sign image at 350x350
            g2.drawImage(signImage, 0, 0, signWidth, signHeight, null);
        } else {
            // Fallback: Draw wooden sign background if image not found
            g2.setColor(new Color(101, 67, 33)); // Wood color
            RoundRectangle2D sign = new RoundRectangle2D.Double(0, 0, signWidth, signHeight, 20, 20);
            g2.fill(sign);
            
            // Draw sign border
            g2.setColor(new Color(60, 40, 20));
            g2.setStroke(new BasicStroke(3));
            g2.draw(sign);
            
            // Draw decorative corners
            g2.fillOval(-5, -5, 15, 15);
            g2.fillOval(signWidth - 10, -5, 15, 15);
            g2.fillOval(-5, signHeight - 10, 15, 15);
            g2.fillOval(signWidth - 10, signHeight - 10, 15, 15);
            
            // Draw text on sign (fallback)
            g2.setColor(new Color(255, 215, 0)); // Gold color
            Font titleFont = new Font("Serif", Font.BOLD, 28);
            g2.setFont(titleFont);
            
            String title1 = "FORGEBOUND:";
            String title2 = "Rise of the Starforge";
            
            FontMetrics fm = g2.getFontMetrics();
            int text1Width = fm.stringWidth(title1);
            
            g2.drawString(title1, (signWidth - text1Width) / 2, 165);
            
            Font subtitle = new Font("Serif", Font.ITALIC, 20);
            g2.setFont(subtitle);
            fm = g2.getFontMetrics();
            int text2Width = fm.stringWidth(title2);
            g2.drawString(title2, (signWidth - text2Width) / 2, 195);
        }
        
        // Restore original transform
        g2.setTransform(originalTransform);
    }
    
    // Start sign drop animation - both sides drop together at 45 degrees (faster)
    private void startSignAnimation() {
        signDropTimer = new javax.swing.Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Drop both sides together to maintain 45 degree angle (faster speed)
                if (signYLeft < targetSignY) {
                    signYLeft += 18; // Faster drop speed
                    signYRight += 18;
                    
                    if (signYLeft >= targetSignY) {
                        signYLeft = targetSignY;
                        signYRight = targetSignY;
                        
                        // Add bounce effect
                        createBounceEffect();
                        signDropTimer.stop();
                    }
                }
            }
        });
        signDropTimer.start();
    }
    
    // Create bounce effect for the sign (faster)
    private void createBounceEffect() {
        javax.swing.Timer bounceTimer = new javax.swing.Timer(20, new ActionListener() {
            int bounceCount = 0;
            int bounceHeight = 25;
            boolean goingUp = false;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (goingUp) {
                    signYLeft -= 6; // Faster bounce
                    signYRight -= 6;
                    
                    if (signYLeft <= targetSignY - bounceHeight) {
                        goingUp = false;
                        bounceCount++;
                        bounceHeight /= 2;
                    }
                } else {
                    signYLeft += 6; // Faster bounce
                    signYRight += 6;
                    
                    if (signYLeft >= targetSignY) {
                        signYLeft = targetSignY;
                        signYRight = targetSignY;
                        
                        if (bounceCount >= 3) {
                            ((javax.swing.Timer) e.getSource()).stop();
                        } else {
                            goingUp = true;
                        }
                    }
                }
            }
        });
        bounceTimer.start();
    }
    
    // ============ ROTATING PLAY BUTTON ============
    class RotatingPlayButton extends JButton {
        private javax.swing.Timer rotationTimer;
        private boolean hovered = false;
        
        public RotatingPlayButton() {
            super("PLAY");
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("Arial", Font.BOLD, 32));
            
            rotationTimer = new javax.swing.Timer(16, e -> {
                if (hovered) {
                    playButtonRotation += 0.02;
                }
                repaint();
            });
            rotationTimer.start();
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    playButtonRotation = 0;
                }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int w = getWidth();
            int h = getHeight();
            
            // Rotate around center
            g2.rotate(Math.sin(playButtonRotation) * 0.1, w / 2.0, h / 2.0);
            
            // Draw button background
            GradientPaint gradient = new GradientPaint(
                0, 0, new Color(34, 139, 34),
                0, h, new Color(0, 100, 0)
            );
            g2.setPaint(gradient);
            g2.fillRoundRect(10, 10, w - 20, h - 20, 30, 30);
            
            // Draw border
            g2.setColor(new Color(255, 215, 0));
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(10, 10, w - 20, h - 20, 30, 30);
            
            // Draw text
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            int textW = fm.stringWidth("PLAY");
            int textH = fm.getAscent();
            g2.drawString("PLAY", (w - textW) / 2, (h + textH) / 2 - 4);
            
            g2.dispose();
        }
    }
    
    // ============ SAVE SELECTION PANEL ============
    private JPanel createSaveSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(20, 20, 40),
                    0, getHeight(), new Color(60, 40, 80)
                );
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        // Title
        JLabel titleLabel = new JLabel("Select Save File", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 36));
        titleLabel.setForeground(new Color(255, 215, 0));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Save files list
        JPanel savesPanel = new JPanel();
        savesPanel.setLayout(new BoxLayout(savesPanel, BoxLayout.Y_AXIS));
        savesPanel.setOpaque(false);
        
        JScrollPane scrollPane = new JScrollPane(savesPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));
        
        updateSavesList(savesPanel);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Bottom buttons
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.setOpaque(false);
        
        JButton newGameButton = new JButton("New Game");
        JButton backButton = new JButton("Back");
        
        styleButton(newGameButton);
        styleButton(backButton);
        
        newGameButton.addActionListener(e -> createNewGame());
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "MAIN_MENU"));
        
        bottomPanel.add(newGameButton);
        bottomPanel.add(backButton);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // Update the list of save files
    private void updateSavesList(JPanel savesPanel) {
        savesPanel.removeAll();
        
        File[] saveFiles = SAVES_DIR.listFiles((dir, name) -> name.endsWith(".forgeboundwrld"));
        
        if (saveFiles == null || saveFiles.length == 0) {
            JLabel noSavesLabel = new JLabel("No save files found. Click 'New Game' to start!");
            noSavesLabel.setForeground(Color.WHITE);
            noSavesLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            noSavesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            savesPanel.add(Box.createVerticalStrut(20));
            savesPanel.add(noSavesLabel);
        } else {
            Arrays.sort(saveFiles, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
            
            for (File saveFile : saveFiles) {
                JPanel saveEntry = createSaveEntry(saveFile);
                savesPanel.add(saveEntry);
                savesPanel.add(Box.createVerticalStrut(10));
            }
        }
        
        savesPanel.revalidate();
        savesPanel.repaint();
    }
    
    // Create a single save entry
    private JPanel createSaveEntry(File saveFile) {
        JPanel entry = new JPanel(new BorderLayout());
        entry.setMaximumSize(new Dimension(700, 80));
        entry.setPreferredSize(new Dimension(700, 80));
        entry.setBackground(new Color(40, 40, 60, 200));
        entry.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Save name (from filename without extension)
        String saveName = saveFile.getName().replace(".forgeboundwrld", "");
        JLabel nameLabel = new JLabel(saveName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        nameLabel.setForeground(Color.WHITE);
        
        // Last modified date
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm");
        String lastModified = sdf.format(new Date(saveFile.lastModified()));
        JLabel dateLabel = new JLabel("Last played: " + lastModified);
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        dateLabel.setForeground(new Color(200, 200, 200));
        
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);
        infoPanel.add(nameLabel, BorderLayout.NORTH);
        infoPanel.add(dateLabel, BorderLayout.CENTER);
        
        // Load button
        JButton loadButton = new JButton("Continue");
        loadButton.setPreferredSize(new Dimension(100, 50));
        styleButton(loadButton);
        loadButton.addActionListener(e -> loadGame(saveFile));
        
        // Delete button
        JButton deleteButton = new JButton("Delete");
        deleteButton.setPreferredSize(new Dimension(100, 50));
        deleteButton.setBackground(new Color(139, 0, 0));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);
        deleteButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete '" + saveName + "'?",
                "Delete Save",
                JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                saveFile.delete();
                updateSavesList((JPanel) entry.getParent());
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(loadButton);
        buttonPanel.add(deleteButton);
        
        entry.add(infoPanel, BorderLayout.CENTER);
        entry.add(buttonPanel, BorderLayout.EAST);
        
        return entry;
    }
    
    // Style a button
    private void styleButton(JButton button) {
        button.setBackground(new Color(34, 139, 34));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
    }
    
    // Show save selection screen
    private void showSaveSelection() {
        // Update the saves list before showing
        JPanel savesPanel = (JPanel) ((JScrollPane) ((JPanel) mainPanel.getComponent(1))
            .getComponent(1)).getViewport().getView();
        updateSavesList(savesPanel);
        
        cardLayout.show(mainPanel, "SAVE_SELECTION");
    }
    
    // Create a new game
    private void createNewGame() {
        String saveName = JOptionPane.showInputDialog(this, "Enter a name for your new game:");
        if (saveName != null && !saveName.trim().isEmpty()) {
            saveName = saveName.trim();
            File saveFile = new File(SAVES_DIR, saveName + ".forgeboundwrld");
            
            if (saveFile.exists()) {
                JOptionPane.showMessageDialog(this, "A save with this name already exists!");
                return;
            }
            
            // Create a new save file (empty for now)
            try {
                saveFile.createNewFile();
                loadGame(saveFile);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error creating save file: " + e.getMessage());
            }
        }
    }
    
    // Load a game
    private void loadGame(File saveFile) {
        try {
            // Launch the actual game with the save file
            String pythonPath = "python";
            
            // Determine the correct path to the game script
            // GameLauncher is in Platformer/App/ and game is in Platformer/Game/
            File gameScript;
            
            // Check if we're running from the App directory
            if (BASE_DIR.getName().equals("App")) {
                // Go up one level to Platformer, then into Game
                File platformerDir = BASE_DIR.getParentFile();
                gameScript = new File(platformerDir, "Game/combination");
            } else if (BASE_DIR.getName().equals("Platformer")) {
                // Running from Platformer root
                gameScript = new File(BASE_DIR, "Game/combination");
            } else {
                // Running from project root (ForgeBound--Rise-of-the-Starforge)
                gameScript = new File(BASE_DIR, "Platformer/Game/combination");
            }
            
            if (!gameScript.exists()) {
                JOptionPane.showMessageDialog(this, 
                    "Game script not found!\n" +
                    "Looking for: " + gameScript.getAbsolutePath() + "\n" +
                    "Current directory: " + BASE_DIR.getAbsolutePath());
                return;
            }
            
            ProcessBuilder pb = new ProcessBuilder(
                pythonPath,
                gameScript.getAbsolutePath(),
                username,
                password,
                saveFile.getAbsolutePath()
            );
            
            // Set working directory to Platformer root
            File workingDir;
            if (BASE_DIR.getName().equals("App")) {
                workingDir = BASE_DIR.getParentFile(); // Platformer
            } else if (BASE_DIR.getName().equals("Platformer")) {
                workingDir = BASE_DIR;
            } else {
                workingDir = new File(BASE_DIR, "Platformer");
            }
            pb.directory(workingDir);
            
            // Start the game process
            Process gameProcess = pb.start();
            
            // Optional: Wait a moment to see if the game starts successfully
            Thread.sleep(500);
            
            // Check if the game process is still running
            if (!gameProcess.isAlive()) {
                int exitCode = gameProcess.exitValue();
                JOptionPane.showMessageDialog(this, 
                    "Game failed to start!\n" +
                    "Exit code: " + exitCode + "\n" +
                    "Game script: " + gameScript.getAbsolutePath() + "\n" +
                    "Working directory: " + workingDir.getAbsolutePath());
                return;
            }
            
            // Close the launcher only if game started successfully
            System.exit(0);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error launching game: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Show leaderboards
    private void showLeaderboards() {
        JOptionPane.showMessageDialog(this, 
            "Leaderboards feature coming soon!\n\n" +
            "This will show:\n" +
            "- Top players by score\n" +
            "- Fastest completion times\n" +
            "- Achievement rankings",
            "Leaderboards",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void main(String[] args) {
        // Accept username and password from command line
        if (args.length >= 2) {
            username = args[0];
            password = args[1];
        }
        
        SwingUtilities.invokeLater(() -> {
            GameLauncher launcher = new GameLauncher();
            launcher.setVisible(true);
        });
    }
}