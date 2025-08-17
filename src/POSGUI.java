import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class POSGUI extends JFrame {
    private JTable productTable, cartTable;
    private DefaultTableModel productModel, cartModel;
    private JLabel totalLabel;
    private List<Product> cart = new ArrayList<>();
    private Admin cashier;
    private ProductDAO productDAO = new ProductDAO();
    private SaleDAO saleDAO = new SaleDAO();

    public POSGUI(Admin cashier) {
        this.cashier = cashier;
        setTitle("Point of Sale - " + cashier.getUsername());
        setSize(1000, 600);
        setLayout(new BorderLayout());

        // Product selection panel (left)
        JPanel productPanel = new JPanel(new BorderLayout());
        productModel = new DefaultTableModel(new String[]{"ID", "Name", "Price", "Stock"}, 0);
        productTable = new JTable(productModel);
        loadProducts();
        productPanel.add(new JScrollPane(productTable), BorderLayout.CENTER);

        // Cart panel (right)
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartModel = new DefaultTableModel(new String[]{"ID", "Name", "Price", "Qty", "Subtotal"}, 0);
        cartTable = new JTable(cartModel);
        cartPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        // Bottom panel (total + checkout)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        totalLabel = new JLabel("Total: $0.00", JLabel.CENTER);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        bottomPanel.add(totalLabel, BorderLayout.CENTER);

        JButton checkoutBtn = new JButton("Checkout");
        checkoutBtn.addActionListener(e -> checkout());
        bottomPanel.add(checkoutBtn, BorderLayout.EAST);

        // Add to cart button
        JButton addToCartBtn = new JButton("Add to Cart");
        addToCartBtn.addActionListener(e -> addToCart());

        // Layout setup
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, productPanel, cartPanel);
        splitPane.setDividerLocation(600);

        add(splitPane, BorderLayout.CENTER);
        add(addToCartBtn, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void loadProducts() {
        productModel.setRowCount(0);
        try {
            List<Product> products = productDAO.getAllProducts();
            for (Product p : products) {
                productModel.addRow(new Object[]{p.getProductId(), p.getName(), p.getPrice(), p.getQuantity()});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage());
        }
    }

    private void addToCart() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product");
            return;
        }

        int productId = (int) productModel.getValueAt(selectedRow, 0);
        String name = (String) productModel.getValueAt(selectedRow, 1);
        double price = (double) productModel.getValueAt(selectedRow, 2);
        int stock = (int) productModel.getValueAt(selectedRow, 3);

        String qtyStr = JOptionPane.showInputDialog(this, "Enter quantity for " + name + ":");
        if (qtyStr == null || qtyStr.isEmpty()) return;

        try {
            int qty = Integer.parseInt(qtyStr);
            if (qty <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be positive");
                return;
            }
            if (qty > stock) {
                JOptionPane.showMessageDialog(this, "Not enough stock! Available: " + stock);
                return;
            }

            // Add to cart
            Product product = new Product(productId, name, price, qty, "");
            cart.add(product);
            updateCart();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity");
        }
    }

    private void updateCart() {
        cartModel.setRowCount(0);
        double total = 0;

        for (Product p : cart) {
            double subtotal = p.getPrice() * p.getQuantity();
            cartModel.addRow(new Object[]{
                    p.getProductId(),
                    p.getName(),
                    p.getPrice(),
                    p.getQuantity(),
                    String.format("$%.2f", subtotal)
            });
            total += subtotal;
        }

        totalLabel.setText(String.format("Total: $%.2f", total));
    }

    private void checkout() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Confirm purchase for " + String.format("$%.2f", getTotal()) + "?",
                "Confirm Checkout",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Record sale
                Sale sale = new Sale(0, cashier.getAdminId(), getTotal(), new Timestamp(System.currentTimeMillis()));
                int saleId = saleDAO.recordSale(sale, cart);

                // Update stock
                for (Product p : cart) {
                    productDAO.updateProductQuantity(p.getProductId(), -p.getQuantity());
                }

                JOptionPane.showMessageDialog(this, "Sale completed! Receipt #" + saleId);
                cart.clear();
                updateCart();
                loadProducts(); // Refresh stock
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Checkout failed: " + e.getMessage());
            }
        }
    }

    private double getTotal() {
        return cart.stream().mapToDouble(p -> p.getPrice() * p.getQuantity()).sum();
    }
}