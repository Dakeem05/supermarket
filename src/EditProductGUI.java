import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EditProductGUI extends JFrame {
    private Product product;
    private ProductManagementGUI parent;
    private ProductDAO productDAO;

    public EditProductGUI(Product product, ProductManagementGUI parent) {
        this.product = product;
        this.parent = parent;

        setTitle("Edit Product");
        setSize(400, 300);
        setLayout(new GridLayout(6, 2));

        // Form fields
        JTextField nameField = new JTextField(product.getName());
        JTextField priceField = new JTextField(String.valueOf(product.getPrice()));
        JTextField quantityField = new JTextField(String.valueOf(product.getQuantity()));
        JTextField categoryField = new JTextField(product.getCategory());

        add(new JLabel("Name:"));
        add(nameField);
        add(new JLabel("Price:"));
        add(priceField);
        add(new JLabel("Quantity:"));
        add(quantityField);
        add(new JLabel("Category:"));
        add(categoryField);

        // Save button
        JButton saveBtn = new JButton("Save Changes");
        saveBtn.addActionListener(e -> {
            try {
                product.setName(nameField.getText());
                product.setPrice(Double.parseDouble(priceField.getText()));
                product.setQuantity(Integer.parseInt(quantityField.getText()));
                product.setCategory(categoryField.getText());

                productDAO = new ProductDAO();

                if (productDAO.updateProduct(product)) {
                    JOptionPane.showMessageDialog(this, "Product updated successfully");
                    parent.refreshProducts();
                    dispose();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number format", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error updating product: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        add(new JLabel(""));
        add(saveBtn);

        setVisible(true);

        // After successful save:
        parent.productUpdated();
    }
}