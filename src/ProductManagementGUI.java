import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

public class ProductManagementGUI extends JFrame {
    private JTable productTable;
    private DefaultTableModel tableModel;
    private ProductDAO productDAO;

    public ProductManagementGUI() {
        setTitle("Product Management");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        productDAO = new ProductDAO();

        // Table setup
        String[] columnNames = {"ID", "Name", "Price", "Quantity", "Category", "Image"};
        tableModel = new DefaultTableModel(columnNames, 0);
        productTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(productTable);

        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton refreshBtn = new JButton("Refresh");
        JButton editBtn = new JButton("Edit Selected");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton backBtn = new JButton("Back to Dashboard");

        refreshBtn.addActionListener(e -> refreshProducts());
        editBtn.addActionListener(e -> editSelectedProduct());
        deleteBtn.addActionListener(e -> deleteSelectedProduct());
        backBtn.addActionListener(e -> dispose());

        buttonPanel.add(refreshBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(backBtn);

        // Add components to frame
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load initial data
        refreshProducts();

        setVisible(true);
    }

    public void refreshProducts() {
        try {
            List<Product> products = productDAO.getAllProducts();
            tableModel.setRowCount(0); // Clear existing data

            for (Product product : products) {
                Object[] rowData = {
                        product.getProductId(),
                        product.getName(),
                        product.getPrice(),
                        product.getQuantity(),
                        product.getCategory(),
                        product.getImage() != null ? "Yes" : "No"
                };
                tableModel.addRow(rowData);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void productUpdated() {
        refreshProducts();
    }

    private void editSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product to edit");
            return;
        }

        int productId = (int) tableModel.getValueAt(selectedRow, 0);
        try {
            Product product = productDAO.getProductById(productId);
            if (product != null) {
                new EditProductGUI(product, this);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading product: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete");
            return;
        }

        int productId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this product?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (productDAO.deleteProduct(productId)) {
                    JOptionPane.showMessageDialog(this, "Product deleted successfully");
                    refreshProducts();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error deleting product: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}