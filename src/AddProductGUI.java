import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class AddProductGUI extends JFrame {
    private JTextField nameField, priceField, quantityField, categoryField;
    private JButton imageButton, submitButton;
    private File selectedImage;
    private ProductDAO productDAO;
    private InventoryGUI parentFrame;  // Changed from 'parent' to avoid conflict

    public AddProductGUI(InventoryGUI parent) {
        this.parentFrame = parent;  // Store reference to parent frame
        setTitle("Add New Product");
        setSize(500, 400);
        setLayout(new GridLayout(6, 2));

        productDAO = new ProductDAO();

        // Initialize form fields
        initializeFormFields();

        setLocationRelativeTo(parent);  // Center relative to parent
        setVisible(true);
    }

    private void initializeFormFields() {
        // Product Name
        add(new JLabel("Product Name:"));
        nameField = new JTextField();
        add(nameField);

        // Price
        add(new JLabel("Price:"));
        priceField = new JTextField();
        add(priceField);

        // Quantity
        add(new JLabel("Quantity:"));
        quantityField = new JTextField();
        add(quantityField);

        // Category
        add(new JLabel("Category:"));
        categoryField = new JTextField();
        add(categoryField);

        // Image selection
        add(new JLabel("Product Image:"));
        imageButton = new JButton("Select Image");
        imageButton.addActionListener(e -> selectImage());
        add(imageButton);

        // Submit button
        submitButton = new JButton("Add Product");
        submitButton.addActionListener(e -> addProduct());
        add(submitButton);
    }

    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedImage = fileChooser.getSelectedFile();
            imageButton.setText(selectedImage.getName());
        }
    }

    private void addProduct() {
        try {
            // Validate inputs
            String name = nameField.getText();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Product name cannot be empty");
            }

            double price = Double.parseDouble(priceField.getText());
            int quantity = Integer.parseInt(quantityField.getText());
            String category = categoryField.getText();
            byte[] imageBytes = null;

            if (selectedImage != null) {
                imageBytes = Files.readAllBytes(selectedImage.toPath());
            }

            // Create and add product
            Product product = new Product(0, name, price, quantity, category, imageBytes);
            if (productDAO.addProduct(product)) {
                JOptionPane.showMessageDialog(this, "Product added successfully!");
                parentFrame.refreshProducts();  // Refresh parent's table
                dispose();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter valid numbers for price and quantity",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error reading image file: " + e.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error adding product: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}