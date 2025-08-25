package App; // Make sure this matches your folder structure

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class Launcher {

    private static Map<String, String> loadUsers() {
        Map<String, String> users = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\family_2\\Pictures\\Real_Platformer\\users.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    users.put(parts[0].trim(), parts[1].trim()); // username : password
                }
            }

            // Debugging: print loaded users
            System.out.println("Loaded users:");
            for (String user : users.keySet()) {
                System.out.println(" - " + user);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }

    public static void main(String[] args) {
        Map<String, String> users = loadUsers();

        JFrame frame = new JFrame("Forgebound Launcher");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLayout(new GridLayout(3, 2));

        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField();

        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField();

        JButton loginButton = new JButton("Login");

        frame.add(userLabel);
        frame.add(userField);
        frame.add(passLabel);
        frame.add(passField);
        frame.add(new JLabel(""));
        frame.add(loginButton);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = userField.getText();
                String password = new String(passField.getPassword());

                if (users.containsKey(username) && users.get(username).equals(password)) {
                    JOptionPane.showMessageDialog(frame, "Login successful! Launching game...");

                    try {
                        String pythonPath = "python"; // Or full path to python.exe
                        String scriptPath = "C:\\Users\\family_2\\Pictures\\Real_Platformer\\combination.py";

                        ProcessBuilder pb = new ProcessBuilder(pythonPath, scriptPath);
                        pb.directory(new File("C:\\Users\\family_2\\Pictures\\Real_Platformer")); // Working directory
                        pb.inheritIO();

                        Process process = pb.start();

                        int exitCode = process.waitFor();
                        System.out.println("Python exited with code: " + exitCode);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(frame, "Error launching game!");
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid login!");
                }
            }
        });

        frame.setVisible(true);
    }
}
