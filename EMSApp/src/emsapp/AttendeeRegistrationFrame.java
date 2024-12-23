package emsapp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.regex.Pattern;

public class AttendeeRegistrationFrame extends JFrame {
    private JTextField nameField;
    private JTextField emailField;
    private JTextField contactField;
    private JComboBox<String> eventComboBox;
    private JButton registerButton;
    private JButton viewButton;
    private JButton updateButton;
    private JButton removeButton;
    private JTable attendeeTable;
    private DefaultTableModel tableModel;
    private boolean isTableVisible = false;

    public AttendeeRegistrationFrame() {
        setTitle("Attendee Registration");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        add(formPanel, BorderLayout.NORTH);

        formPanel.add(new JLabel("Attendee Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        formPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        formPanel.add(emailField);

        formPanel.add(new JLabel("Contact Number:"));
        contactField = new JTextField();
        formPanel.add(contactField);

        formPanel.add(new JLabel("Event:"));
        eventComboBox = new JComboBox<>();
        formPanel.add(eventComboBox);

        // Button Panel for registration and actions
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        registerButton = new JButton("Register Attendee");
        viewButton = new JButton("View Attendees");
        updateButton = new JButton("Update Attendee");
        removeButton = new JButton("Remove Attendee");

        buttonPanel.add(registerButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(removeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Table to Display Attendees
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Contact", "Event"}, 0);
        attendeeTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(attendeeTable);
        add(scrollPane, BorderLayout.CENTER);

        // Load events into combo box
        loadEvents();

        // Add Action Listeners
        registerButton.addActionListener(e -> registerAttendee());
        viewButton.addActionListener(e -> viewAttendees());
        updateButton.addActionListener(e -> updateAttendee());
        removeButton.addActionListener(e -> removeAttendee());

        // Initialize the phone number validation
        initialize();
    }

    private void initialize() {
        // Add key listener to contact field to restrict non-numeric input
        addPhoneNumberValidation();
    }

    private void addPhoneNumberValidation() {
        contactField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                // Allow only digits
                if (!Character.isDigit(c)) {
                    evt.consume();  // Discard non-numeric input
                }
            }
        });
    }

    private void loadEvents() {
        String sql = "SELECT event_id, event_name FROM events";

        try (Connection conn = DatabaseUtils.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String eventName = rs.getString("event_name");
                eventComboBox.addItem(eventName);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading events.");
        }
    }

    private void registerAttendee() {
        String name = nameField.getText();
        String email = emailField.getText();
        String contact = contactField.getText();
        String event = (String) eventComboBox.getSelectedItem();

        // Check if any field is empty
        if (name.isEmpty() || email.isEmpty() || contact.isEmpty() || event == null) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate email and contact number formats
        if (!isValidEmail(email) || !isValidPhoneNumber(contact)) {
            JOptionPane.showMessageDialog(this, "Invalid email or phone number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if the attendee already exists with the same name and email
        if (isEmailExists(email)) {
            JOptionPane.showMessageDialog(this, "An attendee with this email already exists.", "Duplicate Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Get event ID
        int eventId = getEventId(event);
        if (eventId == -1) {
            JOptionPane.showMessageDialog(this, "Selected event is not valid.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // SQL query to insert a new attendee
        String sql = "INSERT INTO attendees (attendee_name, email, contact_number, registered_event_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, contact);
            pstmt.setInt(4, eventId);

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Attendee registered successfully!");
            loadAttendeeDetails();  // Reload attendees list after registration

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error registering attendee.");
        }
    }

    private boolean isEmailExists(String email) {
        String sql = "SELECT 1 FROM attendees WHERE email = ? LIMIT 1";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // If a row is returned, email exists
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    private int getEventId(String eventName) {
        String sql = "SELECT event_id FROM events WHERE event_name = ?";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, eventName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("event_id");
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    private void loadAttendeeDetails() {
        String sql = "SELECT a.attendee_id, a.attendee_name, a.email, a.contact_number, e.event_name " +
                     "FROM attendees a LEFT JOIN events e ON a.registered_event_id = e.event_id";

        try (Connection conn = DatabaseUtils.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            tableModel.setRowCount(0); // Clear existing data

            while (rs.next()) {
                int id = rs.getInt("attendee_id");
                String name = rs.getString("attendee_name");
                String email = rs.getString("email");
                String contactNumber = rs.getString("contact_number");
                String eventName = rs.getString("event_name");

                tableModel.addRow(new Object[]{id, name, email, contactNumber, eventName});
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading attendee details.");
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && Pattern.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", email);
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("\\d{10,15}"); // Ensures only digits
    }

    private void viewAttendees() {
        // Toggle the table visibility
        if (isTableVisible) {
            attendeeTable.setVisible(false);  // Hide the table
            isTableVisible = false;
            viewButton.setText("View Attendees");  // Change button text
        } else {
            loadAttendeeDetails();  // Load attendees data
            attendeeTable.setVisible(true);  // Show the table
            isTableVisible = true;
            viewButton.setText("Hide Attendees");  // Change button text
        }
    }

    private void updateAttendee() {
        int selectedRow = attendeeTable.getSelectedRow();
        if (selectedRow != -1) {
            // Get the attendee ID from the selected row
            int attendeeId = (int) tableModel.getValueAt(selectedRow, 0);

            // Get the new data from the input fields (name, email, phone)
            String name = nameField.getText();
            String email = emailField.getText();
            String contact = contactField.getText();

            // Validate the fields
            if (name.isEmpty() || email.isEmpty() || contact.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check if the email is valid
            if (!isValidEmail(email) || !isValidPhoneNumber(contact)) {
                JOptionPane.showMessageDialog(this, "Invalid email or phone number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Update query to modify the attendee details
            String sql = "UPDATE attendees SET attendee_name = ?, email = ?, contact_number = ? WHERE attendee_id = ?";

            try (Connection conn = DatabaseUtils.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, name);
                pstmt.setString(2, email);
                pstmt.setString(3, contact);
                pstmt.setInt(4, attendeeId);

                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Attendee details updated successfully!");
                loadAttendeeDetails();  // Reload the updated details

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating attendee.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an attendee to update.");
        }
    }

    private void removeAttendee() {
        int selectedRow = attendeeTable.getSelectedRow();
        if (selectedRow != -1) {
            int attendeeId = (int) tableModel.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this attendee?");
            if (confirm == JOptionPane.YES_OPTION) {
                // Delete query to remove the attendee
                String sql = "DELETE FROM attendees WHERE attendee_id = ?";

                try (Connection conn = DatabaseUtils.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {

                    pstmt.setInt(1, attendeeId);
                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Attendee removed successfully!");
                    loadAttendeeDetails();  // Reload attendees after removal

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error removing attendee.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an attendee to remove.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AttendeeRegistrationFrame().setVisible(true));
    }
}
