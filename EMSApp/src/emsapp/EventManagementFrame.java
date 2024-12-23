package emsapp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class EventManagementFrame extends JFrame {
    private JTextField nameField;
    private JTextField dateField;
    private JTextField timeField;
    private JTextArea descriptionArea;
    private JTextField organiserField;
    private JButton createButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JTable eventTable;
    private DefaultTableModel tableModel;

    public EventManagementFrame() {
        setTitle("Event Planning");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        add(formPanel, BorderLayout.NORTH);

        formPanel.add(new JLabel("Event Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        formPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        dateField = new JTextField();
        formPanel.add(dateField);

        formPanel.add(new JLabel("Time (HH:MM:SS):"));
        timeField = new JTextField();
        formPanel.add(timeField);

        formPanel.add(new JLabel("Description:"));
        descriptionArea = new JTextArea(3, 20);
        formPanel.add(new JScrollPane(descriptionArea));

        formPanel.add(new JLabel("Organiser Details:"));
        organiserField = new JTextField();
        formPanel.add(organiserField);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        createButton = new JButton("Create Event");
        updateButton = new JButton("Edit Event");
        deleteButton = new JButton("Delete Event");
        buttonPanel.add(createButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Table to Display Events
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Date", "Time", "Description", "Organiser"}, 0);
        eventTable = new JTable(tableModel);
        add(new JScrollPane(eventTable), BorderLayout.CENTER);

        // Load event details by default
        loadEventDetails();

        // Add Action Listeners
        createButton.addActionListener(e -> createEvent());
        updateButton.addActionListener(e -> updateEvent());
        deleteButton.addActionListener(e -> deleteEvent());
    }

    private void createEvent() {
    String name = nameField.getText();
    String date = dateField.getText();
    String time = timeField.getText();
    String description = descriptionArea.getText();
    String organiser = organiserField.getText();

    if (name.isEmpty() || date.isEmpty() || time.isEmpty() || description.isEmpty() || organiser.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    String sql = "INSERT INTO events (event_name, event_date, event_time, description, organiser_details) VALUES (?, ?, ?, ?, ?)";

    try (Connection conn = DatabaseUtils.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setString(1, name);
        pstmt.setString(2, date);
        pstmt.setString(3, time);
        pstmt.setString(4, description);
        pstmt.setString(5, organiser);

        pstmt.executeUpdate();
        JOptionPane.showMessageDialog(this, "Event created successfully!");
        loadEventDetails();

    } catch (SQLIntegrityConstraintViolationException ex) {
        // Handle the case where the event already exists
        JOptionPane.showMessageDialog(this, "An event with this name already exists. Please choose a different name.", "Duplicate Event", JOptionPane.ERROR_MESSAGE);
    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error creating event.");
    }
}

    private void updateEvent() {
        int selectedRow = eventTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an event to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (Integer) tableModel.getValueAt(selectedRow, 0);
        String name = nameField.getText();
        String date = dateField.getText();
        String time = timeField.getText();
        String description = descriptionArea.getText();
        String organiser = organiserField.getText();

        if (name.isEmpty() || date.isEmpty() || time.isEmpty() || description.isEmpty() || organiser.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "UPDATE events SET event_name = ?, event_date = ?, event_time = ?, description = ?, organiser_details = ? WHERE event_id = ?";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, date);
            pstmt.setString(3, time);
            pstmt.setString(4, description);
            pstmt.setString(5, organiser);
            pstmt.setInt(6, id);

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Event updated successfully!");
            loadEventDetails();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating event.");
        }
    }

    private void deleteEvent() {
        int selectedRow = eventTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an event to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (Integer) tableModel.getValueAt(selectedRow, 0);

        String sql = "DELETE FROM events WHERE event_id = ?";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Event deleted successfully!");
            loadEventDetails();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting event.");
        }
    }

    private void loadEventDetails() {
        String sql = "SELECT * FROM events";

        try (Connection conn = DatabaseUtils.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            tableModel.setRowCount(0); // Clear existing data

            while (rs.next()) {
                int id = rs.getInt("event_id");
                String name = rs.getString("event_name");
                String date = rs.getString("event_date");
                String time = rs.getString("event_time");
                String description = rs.getString("description");
                String organiser = rs.getString("organiser_details");

                tableModel.addRow(new Object[]{id, name, date, time, description, organiser});
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading event details.");
        }
    }
}
