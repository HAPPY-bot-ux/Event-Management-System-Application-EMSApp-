package emsapp;

import java.awt.*;
import javax.swing.*;

public class LoadingScreen extends JDialog {
    private JProgressBar progressBar;

    public LoadingScreen(String message) {
        setTitle("Loading");
        setSize(300, 150);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Add message label
        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        add(messageLabel, BorderLayout.NORTH);

        // Add progress bar
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        add(progressBar, BorderLayout.CENTER);

        // Close on ESCAPE key press
        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    dispose();
                }
            }
        });
    }

    // Method to stop the animation
    public void stopAnimation() {
        progressBar.setIndeterminate(false);
        progressBar.setValue(100);
    }
}
