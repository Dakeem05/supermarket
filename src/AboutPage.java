import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AboutPage extends JFrame {
    public AboutPage() {
        setTitle("About Supermarket Inventory System");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0, 120, 215));
        JLabel titleLabel = new JLabel("Supermarket Inventory System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // Content Panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // System Description
        JTextArea aboutText = new JTextArea();
        aboutText.setEditable(false);
        aboutText.setLineWrap(true);
        aboutText.setWrapStyleWord(true);
        aboutText.setFont(new Font("Arial", Font.PLAIN, 14));
        aboutText.setText(
                "Supermarket Inventory System v1.0\n\n" +
                        "This application provides comprehensive inventory management\n" +
                        "and point-of-sale functionality for retail supermarkets.\n\n" +
                        "Features:\n" +
                        "• Product inventory management\n" +
                        "• Barcode/QR code support (future update)\n" +
                        "• Sales tracking and reporting\n" +
                        "• Multi-user access control\n\n" +
                        "Developed using Java Swing and PostgreSQL for reliable\n" +
                        "data storage and management."
        );

        // Developer Info
        JPanel devPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        devPanel.setBorder(BorderFactory.createTitledBorder("Developer Information"));
        JLabel devLabel = new JLabel(
                "<html>" +
                        "<b>Name:</b> Edidiong Samuel<br>" +
                        "<b>Reg No:</b> 22/SC/SC/1169<br>" +
                        "<b>Email:</b> edidongsamuel14@gmail.com<br>" +
                        "<b>GitHub:</b> github.com/dakeem05<br>" +
                        "<b>Date:</b> " + java.time.LocalDate.now() +
                        "</html>"
        );
        devPanel.add(devLabel);

        // Close Button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());

        // Add components
        contentPanel.add(aboutText);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(devPanel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(closeButton);

        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    // Add this to your DashboardGUI:
    public static void addAboutButton(JPanel panel) {
        JButton aboutButton = new JButton("About");
        aboutButton.addActionListener(e -> new AboutPage());
        panel.add(aboutButton);
    }
}