import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DashboardGUI extends JFrame {
    private Admin loggedInAdmin;

    public DashboardGUI(Admin admin) {
        this.loggedInAdmin = admin;
        setTitle("Dashboard - " + admin.getUsername());
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 1));

        // Buttons based on role
        JButton viewProductsBtn = new JButton("View/Edit Products");
        viewProductsBtn.addActionListener(e -> new ProductManagementGUI());

        JButton addProductBtn = new JButton("Add New Product");
        addProductBtn.addActionListener(e -> new AddProductGUI(new InventoryGUI()));

        JButton addUserBtn = new JButton("Add New User");
        addUserBtn.addActionListener(e -> new AddUserGUI());

        JButton posBtn = new JButton("Point of Sale (POS)");
        posBtn.addActionListener(e -> new POSGUI(loggedInAdmin));

        JButton salesHistoryBtn = new JButton("View Sales History");
        salesHistoryBtn.addActionListener(e -> new SalesHistoryGUI());

        JButton aboutBtn = new JButton("About Me");
        aboutBtn.addActionListener(e -> new AboutPage());

        // Only admin can add users
        if ("admin".equals(loggedInAdmin.getRole())) {
            add(addUserBtn);
        }

        add(viewProductsBtn);
        add(addProductBtn);
        add(posBtn);
        add(salesHistoryBtn);
        add(aboutBtn);

        setVisible(true);
    }
}