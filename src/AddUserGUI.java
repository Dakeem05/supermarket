import javax.swing.*;
        import java.awt.*;
        import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AddUserGUI extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private AdminDAO adminDAO;

    public AddUserGUI() {
        setTitle("Add New User");
        setSize(400, 300);
        setLayout(new GridLayout(4, 2));

        adminDAO = new AdminDAO();

        // Form fields
        add(new JLabel("Username:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        add(new JLabel("Role:"));
        String[] roles = {"admin", "attendant"};
        roleComboBox = new JComboBox<>(roles);
        add(roleComboBox);

        // Submit button
        JButton submitButton = new JButton("Add User");
        submitButton.addActionListener(e -> addUser());
        add(submitButton);

        setVisible(true);
    }

    private void addUser() {
        try {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String role = (String) roleComboBox.getSelectedItem();

            // Basic validation
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and password cannot be empty",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Admin newAdmin = new Admin(0, username, password, role);
            // You'll need to implement addAdmin() in AdminDAO
            if (adminDAO.addAdmin(newAdmin)) {
                JOptionPane.showMessageDialog(this, "User added successfully!");
                dispose();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding user: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}