package emsapp;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public class EMSApp extends JFrame {
    private JButton eventButton;
    private JButton attendeeButton;
    private JButton venueButton;
    private JLabel loginLabel;
    private JLabel onlineDot;

    public EMSApp(String userName) {
        setTitle("Event Management System");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Prevent default close operation
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Load background image
        ImageIcon backgroundImage = new ImageIcon("C:\\Users\\admin\\OneDrive\\Documents\\NetBeansProjects\\EMSApp\\markus-spiske-Vmn_WfR09tg-unsplash.jpg");
        JLabel backgroundLabel = new JLabel(backgroundImage);
        backgroundLabel.setLayout(new GridBagLayout());
        add(backgroundLabel);

        // Initialize buttons
        eventButton = new JButton("Event Planning");
        attendeeButton = new JButton("Register Attendees");
        venueButton = new JButton("Manage Venues");

        // Customize button appearance
        customizeButton(eventButton);
        customizeButton(attendeeButton);
        customizeButton(venueButton);

        // Add hover effect
        addHoverEffect(eventButton);
        addHoverEffect(attendeeButton);
        addHoverEffect(venueButton);

        // Create login label with online dot
        JPanel loginPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        loginPanel.setOpaque(false);

        loginLabel = new JLabel("Login as: " + userName);
        loginLabel.setFont(new Font("Arial", Font.BOLD, 16));
        loginLabel.setForeground(Color.BLACK);

        onlineDot = new JLabel("\u2022");
        onlineDot.setFont(new Font("Arial", Font.BOLD, 20));
        onlineDot.setForeground(new Color(0, 255, 0));

        loginPanel.add(loginLabel);
        loginPanel.add(onlineDot);

        add(loginPanel, BorderLayout.NORTH);

        // Set constraints for button placement
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Add buttons to the background labeljb
        backgroundLabel.add(eventButton, gbc);

        gbc.gridy = 1;
        backgroundLabel.add(attendeeButton, gbc);

        gbc.gridy = 2;
        backgroundLabel.add(venueButton, gbc);

        // Add action listeners to buttons
        eventButton.addActionListener(e -> openFrame(new EventManagementFrame()));
        attendeeButton.addActionListener(e -> openFrame(new AttendeeRegistrationFrame()));
        venueButton.addActionListener(e -> openFrame(new VenueManagementFrame()));

        // Handle window closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                int choice = JOptionPane.showConfirmDialog(null,
                        "Goodbye, " + userName + "! Do you want to exit the application?",
                        "Exit Confirmation",
                        JOptionPane.YES_NO_OPTION);

                if (choice == JOptionPane.YES_OPTION) {
                    // Dispose the current frame
                    EMSApp.this.dispose();

                    // Show the logging off loading screen
                    LoadingScreen loadingScreen = new LoadingScreen("Logging off...");
                    SwingUtilities.invokeLater(() -> {
                        loadingScreen.setVisible(true);

                        // Simulate logging off time
                        SwingUtilities.invokeLater(() -> {
                            try {
                                Thread.sleep(3000); // Simulated loading time
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            loadingScreen.stopAnimation();
                            loadingScreen.dispose(); // Close the loading screen
                            System.exit(0); // Exit the application
                        });
                    });
                }
            }
        });

        // Keep a reference to the main frame
        setVisible(true);
    }

    // Method to customize button appearance
    private void customizeButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(new Color(0, 120, 215));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
    }

    // Method to add hover effect to buttons
    private void addHoverEffect(JButton button) {
        Color originalColor = button.getBackground();
        Color hoverColor = new Color(0, 150, 250);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
                button.setFont(button.getFont().deriveFont(Font.BOLD, 18f));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalColor);
                button.setFont(button.getFont().deriveFont(Font.BOLD, 16f));
            }
        });
    }

    // Method to open a new frame and keep the current one visible
    private void openFrame(JFrame frame) {
        // Open the new frame
        frame.setVisible(true);
        // Hide the current frame
        this.setVisible(false);

        // Set up a window listener to handle closing of the new frame
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                // Show the main frame again when the new frame is closed
                EMSApp.this.setVisible(true);
                frame.dispose();
            }
        });
    }

    public static void main(String[] args) {
        // Show loading screen
        LoadingScreen loadingScreen = new LoadingScreen("Loading application...");
        SwingUtilities.invokeLater(() -> loadingScreen.setVisible(true));

        // Simulate loading time
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // After loading, close the loading screen and show the main application
        SwingUtilities.invokeLater(() -> {
            loadingScreen.stopAnimation();
            loadingScreen.dispose(); // Close the loading screen

            // Prompt for username
            String userName = JOptionPane.showInputDialog(null, "Welcome back! Please enter your name:", "Login", JOptionPane.PLAIN_MESSAGE);

            if (userName != null && !userName.trim().isEmpty()) {
                new EMSApp(userName);
            } else {
                JOptionPane.showMessageDialog(null, "Name cannot be empty. Exiting application.");
                System.exit(0);
            }
        });
    }
}
