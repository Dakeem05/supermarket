import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

class DatabaseConnector {
    private static final String URL = "jdbc:postgresql://localhost:5432/supermarket_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Arua08141";

    static {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS admins (admin_id SERIAL PRIMARY KEY, username VARCHAR(50) UNIQUE NOT NULL, password VARCHAR(255) NOT NULL, role VARCHAR(20) NOT NULL)");
            stmt.execute("CREATE TABLE IF NOT EXISTS products (product_id SERIAL PRIMARY KEY, name VARCHAR(100) NOT NULL, price DECIMAL(10,2) NOT NULL, quantity INT NOT NULL, category VARCHAR(50), image BYTEA)");
            stmt.execute("CREATE TABLE IF NOT EXISTS sales (sale_id SERIAL PRIMARY KEY, admin_id INT REFERENCES admins(admin_id), total DECIMAL(10,2) NOT NULL, date TIMESTAMP NOT NULL)");
            stmt.execute("CREATE TABLE IF NOT EXISTS sale_items (item_id SERIAL PRIMARY KEY, sale_id INT REFERENCES sales(sale_id), product_id INT REFERENCES products(product_id), quantity INT NOT NULL, price DECIMAL(10,2) NOT NULL)");
            stmt.execute("INSERT INTO admins (username, password, role) VALUES ('Admin', 'Admin123', 'admin') ON CONFLICT (username) DO NOTHING");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

class Admin {
    private int adminId;
    private String username;
    private String password;
    private String role;

    public Admin(int adminId, String username, String password, String role) {
        this.adminId = adminId;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public int getAdminId() { return adminId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
}

class Product {
    private int productId;
    private String name;
    private double price;
    private int quantity;
    private String category;
    private byte[] image;

    public Product(int productId, String name, double price, int quantity, String category, byte[] image) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.image = image;
    }

    public int getProductId() { return productId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public byte[] getImage() { return image; }
    public void setImage(byte[] image) { this.image = image; }
}

class Sale {
    private int saleId;
    private int adminId;
    private double total;
    private Timestamp date;

    public Sale(int saleId, int adminId, double total, Timestamp date) {
        this.saleId = saleId;
        this.adminId = adminId;
        this.total = total;
        this.date = date;
    }

    public int getSaleId() { return saleId; }
    public int getAdminId() { return adminId; }
    public double getTotal() { return total; }
    public Timestamp getDate() { return date; }
}

class AdminDAO {
    public boolean addAdmin(Admin admin) throws SQLException {
        String sql = "INSERT INTO admins(username, password, role) VALUES(?,?,?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, admin.getUsername());
            pstmt.setString(2, admin.getPassword());
            pstmt.setString(3, admin.getRole());
            return pstmt.executeUpdate() > 0;
        }
    }

    public Admin authenticate(String username, String password) throws SQLException {
        String sql = "SELECT * FROM admins WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Admin(
                        rs.getInt("admin_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role")
                    );
                }
            }
        }
        return null;
    }
}

class ProductDAO {
    public boolean addProduct(Product product) throws SQLException {
        String sql = "INSERT INTO products(name, price, quantity, category, image) VALUES(?,?,?,?,?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.setInt(3, product.getQuantity());
            pstmt.setString(4, product.getCategory());
            if (product.getImage() != null) {
                pstmt.setBytes(5, product.getImage());
            } else {
                pstmt.setNull(5, Types.BINARY);
            }
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products";
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Product product = new Product(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"),
                        rs.getString("category"),
                        rs.getBytes("image")
                );
                products.add(product);
            }
        }
        return products;
    }

    public boolean updateProduct(Product product) throws SQLException {
        String sql = "UPDATE products SET name = ?, price = ?, quantity = ?, category = ?, image = ? WHERE product_id = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.setInt(3, product.getQuantity());
            pstmt.setString(4, product.getCategory());
            if (product.getImage() != null) {
                pstmt.setBytes(5, product.getImage());
            } else {
                pstmt.setNull(5, Types.BINARY);
            }
            pstmt.setInt(6, product.getProductId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteProduct(int productId) throws SQLException {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public Product getProductById(int productId) throws SQLException {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Product(
                            rs.getInt("product_id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getInt("quantity"),
                            rs.getString("category"),
                            rs.getBytes("image")
                    );
                }
            }
        }
        return null;
    }

    public boolean updateProductQuantity(int productId, int quantityChange) throws SQLException {
        String sql = "UPDATE products SET quantity = quantity + ? WHERE product_id = ? AND quantity + ? >= 0";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantityChange);
            pstmt.setInt(2, productId);
            pstmt.setInt(3, quantityChange);
            return pstmt.executeUpdate() > 0;
        }
    }
}

class SaleDAO {
    public int recordSale(Sale sale, List<Product> cart) throws SQLException {
        Connection conn = DatabaseConnector.getConnection();
        try {
            conn.setAutoCommit(false);
            String saleSql = "INSERT INTO sales(admin_id, total, date) VALUES(?,?,?) RETURNING sale_id";
            try (PreparedStatement salePstmt = conn.prepareStatement(saleSql)) {
                salePstmt.setInt(1, sale.getAdminId());
                salePstmt.setDouble(2, sale.getTotal());
                salePstmt.setTimestamp(3, sale.getDate());
                ResultSet rs = salePstmt.executeQuery();
                rs.next();
                int saleId = rs.getInt(1);
                String itemSql = "INSERT INTO sale_items(sale_id, product_id, quantity, price) VALUES(?,?,?,?)";
                try (PreparedStatement itemPstmt = conn.prepareStatement(itemSql)) {
                    for (Product p : cart) {
                        itemPstmt.setInt(1, saleId);
                        itemPstmt.setInt(2, p.getProductId());
                        itemPstmt.setInt(3, p.getQuantity());
                        itemPstmt.setDouble(4, p.getPrice());
                        itemPstmt.addBatch();
                    }
                    itemPstmt.executeBatch();
                }
                conn.commit();
                return saleId;
            }
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public List<Sale> getAllSales() throws SQLException {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales ORDER BY date DESC";
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                sales.add(new Sale(rs.getInt("sale_id"), rs.getInt("admin_id"), rs.getDouble("total"), rs.getTimestamp("date")));
            }
        }
        return sales;
    }
}

class Super extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private AdminDAO adminDAO = new AdminDAO();

    public Super() {
        setTitle("Supermarket Login");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel with gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x701F0F), 0, getHeight(), new Color(0x290C05));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Form panel for inputs
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        formPanel.setOpaque(false); // Transparent to show gradient background
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Username
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setForeground(Color.WHITE);
        formPanel.add(usernameLabel);
        usernameField = new JTextField(15); // Smaller width
        usernameField.setPreferredSize(new Dimension(150, 25)); // Smaller height
        formPanel.add(usernameField);

        // Password
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(Color.WHITE);
        formPanel.add(passwordLabel);
        passwordField = new JPasswordField(15); // Smaller width
        passwordField.setPreferredSize(new Dimension(150, 25)); // Smaller height
        formPanel.add(passwordField);

        // Login Button
        formPanel.add(new JLabel(""));
        JButton loginButton = new JButton("Login") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xAB6554));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 12));
        loginButton.setFocusPainted(false);
        loginButton.setContentAreaFilled(false);
        loginButton.setOpaque(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        loginButton.setPreferredSize(new Dimension(100, 30));
        loginButton.addActionListener(e -> {
            try {
                Admin admin = adminDAO.authenticate(
                        usernameField.getText(),
                        new String(passwordField.getPassword())
                );
                if (admin != null) {
                    new DashboardGUI(admin);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid credentials!", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        formPanel.add(loginButton);

        // Center the form panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(formPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Super::new);
    }
}

class AddProductGUI extends JDialog {
    private JTextField nameField, priceField, quantityField, categoryField;
    private JButton imageButton, submitButton;
    private File selectedImage;
    private ProductDAO productDAO = new ProductDAO();
    private Runnable refreshCallback;

    public AddProductGUI(Frame owner, Runnable refreshCallback) {
        super(owner, "Add New Product", true);
        this.refreshCallback = refreshCallback;
        setSize(450, 350);
        setLocationRelativeTo(owner);

        // Main panel with gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x701F0F), 0, getHeight(), new Color(0x290C05));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Color labelColor = Color.WHITE;
        Dimension fieldSize = new Dimension(200, 25);

        JLabel nameLabel = new JLabel("Product Name:");
        nameLabel.setForeground(labelColor);
        formPanel.add(nameLabel);
        nameField = new JTextField();
        nameField.setPreferredSize(fieldSize);
        formPanel.add(nameField);

        JLabel priceLabel = new JLabel("Price:");
        priceLabel.setForeground(labelColor);
        formPanel.add(priceLabel);
        priceField = new JTextField();
        priceField.setPreferredSize(fieldSize);
        formPanel.add(priceField);

        JLabel quantityLabel = new JLabel("Quantity:");
        quantityLabel.setForeground(labelColor);
        formPanel.add(quantityLabel);
        quantityField = new JTextField();
        quantityField.setPreferredSize(fieldSize);
        formPanel.add(quantityField);

        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setForeground(labelColor);
        formPanel.add(categoryLabel);
        categoryField = new JTextField();
        categoryField.setPreferredSize(fieldSize);
        formPanel.add(categoryField);

        JLabel imageLabel = new JLabel("Product Image:");
        imageLabel.setForeground(labelColor);
        formPanel.add(imageLabel);
        imageButton = createStyledButton("Select Image");
        imageButton.addActionListener(e -> selectImage());
        formPanel.add(imageButton);

        formPanel.add(new JLabel(""));
        submitButton = createStyledButton("Add Product");
        submitButton.addActionListener(e -> addProduct());
        formPanel.add(submitButton);

        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(formPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedImage = fileChooser.getSelectedFile();
            imageButton.setText(selectedImage.getName());
        }
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xAB6554));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return button;
    }

    private void addProduct() {
        try {
            String name = nameField.getText();
            if (name.trim().isEmpty()) {
                throw new IllegalArgumentException("Product name cannot be empty");
            }
            double price = Double.parseDouble(priceField.getText());
            int quantity = Integer.parseInt(quantityField.getText());
            String category = categoryField.getText();
            byte[] imageBytes = null;
            if (selectedImage != null) {
                imageBytes = Files.readAllBytes(selectedImage.toPath());
            }
            Product product = new Product(0, name, price, quantity, category, imageBytes);
            if (productDAO.addProduct(product)) {
                JOptionPane.showMessageDialog(this, "Product added successfully!");
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add product.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for price and quantity", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading image file: " + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding product: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

class ProductManagementGUI extends JFrame {
    private JTable productTable;
    private DefaultTableModel tableModel;
    private ProductDAO productDAO = new ProductDAO();

    public ProductManagementGUI() {
        setTitle("Product Management");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x701F0F), 0, getHeight(), new Color(0x290C05));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        setContentPane(mainPanel);

        String[] columnNames = {"ID", "Name", "Price", "Quantity", "Category", "Image"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(tableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(productTable);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        JButton refreshBtn = createStyledButton("Refresh");
        JButton addBtn = createStyledButton("Add Product");
        JButton editBtn = createStyledButton("Edit Selected");
        JButton deleteBtn = createStyledButton("Delete Selected");
        JButton returnBtn = createStyledButton("Return to Dashboard");

        refreshBtn.addActionListener(e -> refreshProducts());
        addBtn.addActionListener(e -> new AddProductGUI(this, this::refreshProducts));
        editBtn.addActionListener(e -> editSelectedProduct());
        deleteBtn.addActionListener(e -> deleteSelectedProduct());
        returnBtn.addActionListener(e -> dispose());


        buttonPanel.add(refreshBtn);
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(returnBtn);

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        refreshProducts();
        setVisible(true);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xAB6554));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return button;
    }

    public void refreshProducts() {
        try {
            List<Product> products = productDAO.getAllProducts();
            tableModel.setRowCount(0);
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
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int productId = (int) tableModel.getValueAt(selectedRow, 0);
        try {
            Product product = productDAO.getProductById(productId);
            if (product != null) {
                new EditProductDialog(this, product);
                refreshProducts();
            } else {
                JOptionPane.showMessageDialog(this, "Product not found.", "Error", JOptionPane.ERROR_MESSAGE);
                refreshProducts();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading product: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int productId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this product?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (productDAO.deleteProduct(productId)) {
                    JOptionPane.showMessageDialog(this, "Product deleted successfully.");
                    refreshProducts();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete product.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting product: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

class DashboardGUI extends JFrame {
    private Admin loggedInAdmin;

    public DashboardGUI(Admin admin) {
        this.loggedInAdmin = admin;
        setTitle("Dashboard - " + admin.getUsername());
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create main panel with gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x701F0F), 0, getHeight(), new Color(0x290C05));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new GridBagLayout());

        // Button styling
        Font buttonFont = new Font("Arial", Font.BOLD, 12); // Smaller font
        Color buttonColor = new Color(0xAB6554);
        Color textColor = Color.WHITE;

        // Buttons based on role
        JButton addUserBtn = createStyledButton("Add User");
        addUserBtn.addActionListener(e -> new AddUserGUI());

        JButton viewProductsBtn = createStyledButton("View Products");
        viewProductsBtn.addActionListener(e -> new ProductManagementGUI());

        JButton addProductBtn = createStyledButton("Add Product");
        addProductBtn.addActionListener(e -> new AddProductGUI(this, null));

        JButton posBtn = createStyledButton("POS");
        posBtn.addActionListener(e -> new POSGUI(loggedInAdmin));

        JButton salesHistoryBtn = createStyledButton("Sales History");
        salesHistoryBtn.addActionListener(e -> new SalesHistoryGUI());

        JButton aboutBtn = createStyledButton("About");
        aboutBtn.addActionListener(e -> new AboutPage());

        JButton logoutBtn = createStyledButton("Logout");
        logoutBtn.addActionListener(e -> {
            new Super();
            dispose();
        });

        // Panel to hold buttons in a grid, which will then be centered
        JPanel buttonGridPanel = new JPanel();
        buttonGridPanel.setOpaque(false);
        buttonGridPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Add buttons to panel (square layout)
        if ("admin".equalsIgnoreCase(loggedInAdmin.getRole())) {
            buttonGridPanel.setLayout(new GridLayout(3, 3, 15, 15));
            buttonGridPanel.add(addUserBtn);
        } else {
            buttonGridPanel.setLayout(new GridLayout(2, 3, 15, 15));
        }
        buttonGridPanel.add(viewProductsBtn);
        buttonGridPanel.add(addProductBtn);
        buttonGridPanel.add(posBtn);
        buttonGridPanel.add(salesHistoryBtn);
        buttonGridPanel.add(aboutBtn);
        buttonGridPanel.add(logoutBtn);

        mainPanel.add(buttonGridPanel, new GridBagConstraints());
        setContentPane(mainPanel);
        setVisible(true);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xAB6554));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Reduced padding
        button.setPreferredSize(new Dimension(130, 50)); // Smaller buttons
        return button;
    }
}

class AboutPage extends JFrame {
    public AboutPage() {
        setTitle("About Supermarket Inventory System");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(new Color(0, 120, 215));
        JLabel titleLabel = new JLabel("Supermarket Inventory System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

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
                        "• Sales tracking and reporting\n" +
                        "• Multi-user access control\n\n" +
                        "Developed using Java Swing and a relational database for reliable\n" +
                        "data storage and management."
        );
        aboutText.setBackground(contentPanel.getBackground());

        JPanel devPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        devPanel.setBorder(BorderFactory.createTitledBorder("Developer Information"));
        JLabel devLabel = new JLabel(
                "<html>" +
                        "<b>Name:</b> Edidiong Samuel<br>" +
                        "<b>Reg No:</b> 22/SC/SC/1169<br>" +
                        "<b>Email:</b> edidongsamuel14@gmail.com<br>" +
                        "<b>GitHub:</b> github.com/dakeem05<br>" +
                        "<b>Date:</b> " + LocalDate.now() +
                        "</html>"
        );
        devPanel.add(devLabel);

        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton closeButton = new JButton("Close") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xAB6554));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        closeButton.setForeground(Color.WHITE);
        closeButton.setFont(new Font("Arial", Font.BOLD, 12));
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setOpaque(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        closeButton.setPreferredSize(new Dimension(100, 30));
        closeButton.addActionListener(e -> dispose());
        closePanel.add(closeButton);
        
        contentPanel.add(aboutText);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(devPanel);

        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(closePanel, BorderLayout.SOUTH);

        setVisible(true);
    }
}

class AddUserGUI extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private AdminDAO adminDAO = new AdminDAO();

    public AddUserGUI() {
        setTitle("Add New User");
        setSize(400, 250);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel with gradient background from login panel
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x701F0F), 0, getHeight(), new Color(0x290C05));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Form panel for inputs
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        formPanel.setOpaque(false); // Transparent to show gradient background
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Username
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setForeground(Color.WHITE);
        formPanel.add(usernameLabel);
        usernameField = new JTextField(15);
        usernameField.setPreferredSize(new Dimension(150, 25));
        formPanel.add(usernameField);

        // Password
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(Color.WHITE);
        formPanel.add(passwordLabel);
        passwordField = new JPasswordField(15);
        passwordField.setPreferredSize(new Dimension(150, 25));
        formPanel.add(passwordField);

        // Role
        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setForeground(Color.WHITE);
        formPanel.add(roleLabel);
        String[] roles = {"admin", "attendant"};
        roleComboBox = new JComboBox<>(roles);
        roleComboBox.setPreferredSize(new Dimension(150, 25));
        formPanel.add(roleComboBox);

        // Submit Button
        formPanel.add(new JLabel(""));
        JButton submitButton = new JButton("Add User") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xAB6554));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        submitButton.setForeground(Color.WHITE);
        submitButton.setFont(new Font("Arial", Font.BOLD, 12));
        submitButton.setFocusPainted(false);
        submitButton.setContentAreaFilled(false);
        submitButton.setOpaque(false);
        submitButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        submitButton.setPreferredSize(new Dimension(100, 30));
        submitButton.addActionListener(e -> addUser());
        formPanel.add(submitButton);

        // Center the form panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(formPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private void addUser() {
        try {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String role = (String) roleComboBox.getSelectedItem();
            if (username.trim().isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and password cannot be empty", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Admin newAdmin = new Admin(0, username, password, role);
            if (adminDAO.addAdmin(newAdmin)) {
                JOptionPane.showMessageDialog(this, "User added successfully!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add user.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding user: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

class POSGUI extends JDialog {
    private JTable productTable, cartTable;
    private DefaultTableModel productModel, cartModel;
    private JLabel totalLabel;
    private List<Product> cart = new ArrayList<>();
    private Admin cashier;
    private ProductDAO productDAO = new ProductDAO();
    private SaleDAO saleDAO = new SaleDAO();

    public POSGUI(Admin cashier) {
        super((Frame) null, "Point of Sale - " + cashier.getUsername(), true);
        this.cashier = cashier;
        setSize(1000, 700);
        setLocationRelativeTo(null);

        JPanel productPanel = new JPanel(new BorderLayout());
        productPanel.setBorder(BorderFactory.createTitledBorder("Available Products"));
        productModel = new DefaultTableModel(new String[]{"ID", "Name", "Price", "Stock"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(productModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        loadProducts();
        productPanel.add(new JScrollPane(productTable), BorderLayout.CENTER);

        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.setBorder(BorderFactory.createTitledBorder("Shopping Cart"));
        cartModel = new DefaultTableModel(new String[]{"ID", "Name", "Price", "Qty", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        cartTable = new JTable(cartModel);
        cartPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        totalLabel = new JLabel("Total: $0.00", JLabel.CENTER);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 24));
        totalLabel.setForeground(Color.WHITE);
        bottomPanel.add(totalLabel, BorderLayout.CENTER);

        JPanel buttonActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonActionPanel.setOpaque(false);
        JButton checkoutBtn = createStyledButton("Checkout");
        checkoutBtn.addActionListener(e -> checkout());
        JButton returnBtn = createStyledButton("Return to Dashboard");
        returnBtn.addActionListener(e -> dispose());
        buttonActionPanel.add(checkoutBtn);
        buttonActionPanel.add(returnBtn);
        bottomPanel.add(buttonActionPanel, BorderLayout.EAST);
        bottomPanel.setOpaque(false);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setOpaque(false);
        JButton addToCartBtn = createStyledButton("Add Selected to Cart");
        addToCartBtn.addActionListener(e -> addToCart());
        JButton refreshBtn = createStyledButton("Refresh Products");
        refreshBtn.addActionListener(e -> loadProducts());
        topPanel.add(addToCartBtn);
        topPanel.add(refreshBtn);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, productPanel, cartPanel);
        splitPane.setDividerLocation(500);
        splitPane.setOpaque(false);

        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x701F0F), 0, getHeight(), new Color(0x290C05));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(mainPanel);
        setVisible(true);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xAB6554));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return button;
    }

    private void loadProducts() {
        productModel.setRowCount(0);
        try {
            List<Product> products = productDAO.getAllProducts();
            for (Product p : products) {
                if (p.getQuantity() > 0) {
                    productModel.addRow(new Object[]{p.getProductId(), p.getName(), p.getPrice(), p.getQuantity()});
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addToCart() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to add to the cart.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int productId = (int) productModel.getValueAt(selectedRow, 0);
        String name = (String) productModel.getValueAt(selectedRow, 1);
        double price = (double) productModel.getValueAt(selectedRow, 2);
        int stock = (int) productModel.getValueAt(selectedRow, 3);
        String qtyStr = JOptionPane.showInputDialog(this, "Enter quantity for " + name + ":", "1");
        if (qtyStr == null || qtyStr.isEmpty()) return;
        try {
            int qty = Integer.parseInt(qtyStr);
            if (qty <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be a positive number.", "Invalid Quantity", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (qty > stock) {
                JOptionPane.showMessageDialog(this, "Not enough stock! Available: " + stock, "Stock Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            for (Product p : cart) {
                if (p.getProductId() == productId) {
                    if (p.getQuantity() + qty > stock) {
                        JOptionPane.showMessageDialog(this, "Not enough stock for additional quantity!", "Stock Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    p.setQuantity(p.getQuantity() + qty);
                    updateCart();
                    return;
                }
            }
            Product product = new Product(productId, name, price, qty, "", null);
            cart.add(product);
            updateCart();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity entered.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCart() {
        cartModel.setRowCount(0);
        double total = 0;
        for (Product p : cart) {
            double subtotal = p.getPrice() * p.getQuantity();
            cartModel.addRow(new Object[]{p.getProductId(), p.getName(), p.getPrice(), p.getQuantity(), String.format("%.2f", subtotal)});
            total += subtotal;
        }
        totalLabel.setText(String.format("Total: $%.2f", total));
    }

    private void checkout() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty!", "Checkout", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        double total = cart.stream().mapToDouble(p -> p.getPrice() * p.getQuantity()).sum();
        int confirm = JOptionPane.showConfirmDialog(this, "Confirm purchase for " + String.format("$%.2f", total) + "?", "Confirm Checkout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Sale sale = new Sale(0, cashier.getAdminId(), total, new Timestamp(System.currentTimeMillis()));
                int saleId = saleDAO.recordSale(sale, cart);
                for (Product p : cart) {
                    productDAO.updateProductQuantity(p.getProductId(), -p.getQuantity());
                }
                JOptionPane.showMessageDialog(this, "Sale completed successfully! Receipt #" + saleId, "Success", JOptionPane.INFORMATION_MESSAGE);
                cart.clear();
                updateCart();
                loadProducts();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Checkout failed: " + e.getMessage(), "Checkout Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

class EditProductDialog extends JDialog {
    private JTextField nameField, priceField, quantityField, categoryField;
    private JButton imageButton;
    private File selectedImage;
    private Product product;
    private ProductDAO productDAO;

    public EditProductDialog(Frame owner, Product product) {
        super(owner, "Edit Product", true);
        this.product = product;
        this.productDAO = new ProductDAO();
        setSize(500, 400);
        setLocationRelativeTo(owner);
        
        // Main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x701F0F), 0, getHeight(), new Color(0x290C05));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        setContentPane(mainPanel);

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Color labelColor = Color.WHITE;

        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setForeground(labelColor);
        formPanel.add(nameLabel);
        nameField = new JTextField(product.getName());
        formPanel.add(nameField);

        JLabel priceLabel = new JLabel("Price:");
        priceLabel.setForeground(labelColor);
        formPanel.add(priceLabel);
        priceField = new JTextField(String.valueOf(product.getPrice()));
        formPanel.add(priceField);

        JLabel quantityLabel = new JLabel("Quantity:");
        quantityLabel.setForeground(labelColor);
        formPanel.add(quantityLabel);
        quantityField = new JTextField(String.valueOf(product.getQuantity()));
        formPanel.add(quantityField);

        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setForeground(labelColor);
        formPanel.add(categoryLabel);
        categoryField = new JTextField(product.getCategory());
        formPanel.add(categoryField);

        JLabel imageLabel = new JLabel("Product Image:");
        imageLabel.setForeground(labelColor);
        formPanel.add(imageLabel);
        imageButton = createStyledButton(product.getImage() != null ? "Image Loaded" : "Select Image");
        imageButton.addActionListener(e -> selectImage());
        formPanel.add(imageButton);

        JButton cancelButton = createStyledButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        formPanel.add(cancelButton);

        JButton saveButton = createStyledButton("Save Changes");
        saveButton.addActionListener(e -> saveChanges());
        formPanel.add(saveButton);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xAB6554));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return button;
    }
    
    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedImage = fileChooser.getSelectedFile();
            imageButton.setText(selectedImage.getName());
        }
    }

    private void saveChanges() {
        try {
            product.setName(nameField.getText());
            product.setPrice(Double.parseDouble(priceField.getText()));
            product.setQuantity(Integer.parseInt(quantityField.getText()));
            product.setCategory(categoryField.getText());
            if (selectedImage != null) {
                product.setImage(Files.readAllBytes(selectedImage.toPath()));
            }
            if (productDAO.updateProduct(product)) {
                JOptionPane.showMessageDialog(this, "Product updated successfully");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update product.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number format for price or quantity.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading image file: " + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating product: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

class SalesHistoryGUI extends JFrame {
    private JTable salesTable;
    private DefaultTableModel tableModel;
    private SaleDAO saleDAO = new SaleDAO();

    public SalesHistoryGUI() {
        setTitle("Sales History");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x701F0F), 0, getHeight(), new Color(0x290C05));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        setContentPane(mainPanel);

        String[] columns = {"Sale ID", "Admin ID", "Total", "Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        salesTable = new JTable(tableModel);
        mainPanel.add(new JScrollPane(salesTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        JButton refreshBtn = createStyledButton("Refresh");
        refreshBtn.addActionListener(e -> loadSales());
        bottomPanel.add(refreshBtn);

        JButton returnBtn = createStyledButton("Return to Dashboard");
        returnBtn.addActionListener(e -> dispose());
        bottomPanel.add(returnBtn);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        loadSales();
        setVisible(true);
    }

    private void loadSales() {
        tableModel.setRowCount(0);
        try {
            List<Sale> sales = saleDAO.getAllSales();
            for (Sale s : sales) {
                tableModel.addRow(new Object[]{s.getSaleId(), s.getAdminId(), s.getTotal(), s.getDate()});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading sales: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xAB6554));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return button;
    }
}