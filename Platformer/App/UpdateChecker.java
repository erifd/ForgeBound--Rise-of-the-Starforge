package App;

import java.awt.Desktop;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.*;

public class UpdateChecker {
    private static final String CURRENT_VERSION = "v0.1.0-omega";
    // Change this to your version file URL (GitHub raw, your server, etc.)
    private static final String VERSION_URL = "https://raw.githubusercontent.com/erifd/ForgeBound--Rise-of-the-Starforge/main/version.txt";
    private static final String DOWNLOAD_URL = "https://github.com/erifd/ForgeBound--Rise-of-the-Starforge/releases";

    public static void checkForUpdates() {
        new Thread(() -> {
            try {
                String latestVersion = fetchLatestVersion();
                
                if (latestVersion != null && isNewerVersion(latestVersion, CURRENT_VERSION)) {
                    int response = JOptionPane.showConfirmDialog(
                        null,
                        "A new version of Forgebound is available!\n\n" +
                        "Current: " + CURRENT_VERSION + "\n" +
                        "Latest: " + latestVersion + "\n\n" +
                        "Would you like to download the update?",
                        "Update Available",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    
                    if (response == JOptionPane.YES_OPTION) {
                        try {
                            // Open browser to download page
                            Desktop.getDesktop().browse(new java.net.URI(DOWNLOAD_URL));
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(null, 
                                "Visit: " + DOWNLOAD_URL, 
                                "Download Update", 
                                JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            } catch (Exception e) {
                // Silently fail - don't bother user if update check fails
                System.err.println("Update check failed: " + e.getMessage());
            }
        }).start();
    }

    private static String fetchLatestVersion() throws Exception {
        URL url = new URL(VERSION_URL);
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))) {
            String line = reader.readLine();
            return line != null ? line.trim() : null;
        }
    }

    private static boolean isNewerVersion(String latest, String current) {
    try {
        // Remove 'v' prefix and any suffix like '-omega', '-alpha', etc.
        String latestClean = latest.replaceAll("^v", "").split("-")[0];
        String currentClean = current.replaceAll("^v", "").split("-")[0];
        
        String[] latestParts = latestClean.split("\\.");
        String[] currentParts = currentClean.split("\\.");
        
        int length = Math.max(latestParts.length, currentParts.length);
        
        for (int i = 0; i < length; i++) {
            int latestNum = i < latestParts.length ? 
                Integer.parseInt(latestParts[i].trim()) : 0;
            int currentNum = i < currentParts.length ? 
                Integer.parseInt(currentParts[i].trim()) : 0;
            
            if (latestNum > currentNum) {
                return true;
            } else if (latestNum < currentNum) {
                return false;
            }
        }
    } catch (NumberFormatException e) {
        System.err.println("Version format error: " + e.getMessage());
    }
    return false;
}
}