package emsapp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class VenueManagementFrame extends JFrame {
    private JTextField nameField;
    private JTextField addressField;
    
    private JTextField capacityField;
    private JCheckBox availabilityCheckBox;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JTable venueTable;
    private DefaultTableModel tableModel;

    public VenueManagementFrame() {
        setTitle("Venue Management");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        add(formPanel, BorderLayout.NORTH);

        formPanel.add(new JLabel("Venue Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        formPanel.add(new JLabel("Address:"));
        addressField = new JTextField();
        formPanel.add(addressField);

        formPanel.add(new JLabel("Capacity:"));
        capacityField = new JTextField();
        formPanel.add(capacityField);

        formPanel.add(new JLabel("Availability:"));
        availabilityCheckBox = new JCheckBox();
        formPanel.add(availabilityCheckBox);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        addButton = new JButton("Add Venue");
        updateButton = new JButton("Edit Venue");
        deleteButton = new JButton("Delete Venue");
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Table to Display Venues
        tableModel = new DefaultTableModel(new String[]{"Venue ID", "Venue Name", "Address", "Capacity", "Availability"}, 0) {
            // Override the getColumnClass method to ensure Availability column is displayed as a string
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4) {  // Availability column
                    return String.class;
                }
                return super.getColumnClass(columnIndex);
            }
        };
        venueTable = new JTable(tableModel);
        add(new JScrollPane(venueTable), BorderLayout.CENTER);

        // Load venue details by default
        loadVenueDetails();

        // Add Action Listeners
        addButton.addActionListener(e -> addVenue());
        updateButton.addActionListener(e -> updateVenue());
        deleteButton.addActionListener(e -> deleteVenue());
    }

    private void addVenue() {
        String name = nameField.getText();
        String address = addressField.getText();
        String capacityText = capacityField.getText();
        boolean availability = availabilityCheckBox.isSelected();

        if (name.isEmpty() || address.isEmpty() || capacityText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int capacity;
        try {
            capacity = Integer.parseInt(capacityText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Capacity must be a number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO venues (venue_name, address, capacity, is_available) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, address);
            pstmt.setInt(3, capacity);
            pstmt.setBoolean(4, availability);

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Venue added successfully!");
            loadVenueDetails();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding venue.");
        }
    }

    private void updateVenue() {
        int selectedRow = venueTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a venue to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int venueId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String name = nameField.getText();
        String address = addressField.getText();
        String capacityText = capacityField.getText();
        boolean availability = availabilityCheckBox.isSelected();

        if (name.isEmpty() || address.isEmpty() || capacityText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int capacity;
        try {
            capacity = Integer.parseInt(capacityText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Capacity must be a number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "UPDATE venues SET venue_name = ?, address = ?, capacity = ?, is_available = ? WHERE venue_id = ?";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, address);
            pstmt.setInt(3, capacity);
            pstmt.setBoolean(4, availability);
            pstmt.setInt(5, venueId);

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Venue updated successfully!");
            loadVenueDetails();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating venue.");
        }
    }

    private void deleteVenue() {
        int selectedRow = venueTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a venue to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int venueId = (Integer) tableModel.getValueAt(selectedRow, 0);

        String sql = "DELETE FROM venues WHERE venue_id = ?";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, venueId);

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Venue deleted successfully!");
            loadVenueDetails();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting venue.");
        }
    }

    private void loadVenueDetails() {
        String sql = "SELECT * FROM venues";

        try (Connection conn = DatabaseUtils.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            tableModel.setRowCount(0); // Clear existing data

            while (rs.next()) {
                int venueId = rs.getInt("venue_id");
                String name = rs.getString("venue_name");
                String address = rs.getString("address");
                int capacity = rs.getInt("capacity");
                boolean availability = rs.getBoolean("is_available");

                // Set availability as custom text "Available" or "Not Available"
                String availabilityText = availability ? "Available" : "Not Available";

                tableModel.addRow(new Object[]{venueId, name, address, capacity, availabilityText});
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading venue details.");
        }
    }
}
