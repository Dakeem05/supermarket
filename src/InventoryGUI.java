import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class InventoryGUI extends JFrame {
    public ProductDAO productDAO;
    private JTable productTable;
    private DefaultTableModel tableModel;

    public InventoryGUI() {
        setTitle("Inventory Management");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        productDAO = new ProductDAO();

        // Initialize components
        initializeTable();
        JPanel buttonPanel = createButtonPanel();

        // Add components to frame
        add(new JScrollPane(productTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load initial data
        refreshProducts();

        setVisible(true);
    }

    private void initializeTable() {
        String[] columnNames = {"ID", "Name", "Price", "Quantity", "Category"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        productTable = new JTable(tableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshProducts());

        JButton addBtn = new JButton("Add Product");
        addBtn.addActionListener(e -> new AddProductGUI(this));

        JButton editBtn = new JButton("Edit Selected");
        editBtn.addActionListener(e -> editSelectedProduct());

        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.addActionListener(e -> deleteSelectedProduct());

        panel.add(refreshBtn);
        panel.add(addBtn);
        panel.add(editBtn);
        panel.add(deleteBtn);

        return panel;
    }

    public void refreshProducts() {
        try {
            List<Product> products = productDAO.getAllProducts();
            tableModel.setRowCount(0); // Clear table

            for (Product product : products) {
                Object[] row = {
                        product.getProductId(),
                        product.getName(),
                        product.getPrice(),
                        product.getQuantity(),
                        product.getCategory()
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading products: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a product to edit",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int productId = (int) tableModel.getValueAt(selectedRow, 0);
        try {
            Product product = productDAO.getProductById(productId);
            if (product != null) {
                new EditProductDialog(product, this);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading product: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a product to delete",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int productId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this product?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (productDAO.deleteProduct(productId)) {
                    JOptionPane.showMessageDialog(this,
                            "Product deleted successfully",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    refreshProducts();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error deleting product: " + e.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void productUpdated() {
        refreshProducts();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InventoryGUI());
    }
}