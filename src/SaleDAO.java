import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SaleDAO {
    public int recordSale(Sale sale, List<Product> items) throws SQLException {
        Connection conn = null;
        int saleId = 0;

        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Insert sale
            String saleSql = "INSERT INTO sales(admin_id, total_amount, sale_date) VALUES(?, ?, ?) RETURNING sale_id";
            try (PreparedStatement pstmt = conn.prepareStatement(saleSql)) {
                pstmt.setInt(1, sale.getAdminId());
                pstmt.setDouble(2, sale.getTotalAmount());
                pstmt.setTimestamp(3, sale.getSaleDate());

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    saleId = rs.getInt(1);
                }
            }

            // Insert sale items
            String itemsSql = "INSERT INTO sale_items(sale_id, product_id, quantity, price) VALUES(?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(itemsSql)) {
                for (Product p : items) {
                    pstmt.setInt(1, saleId);
                    pstmt.setInt(2, p.getProductId());
                    pstmt.setInt(3, p.getQuantity());
                    pstmt.setDouble(4, p.getPrice());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            conn.commit(); // Commit transaction
            return saleId;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.setAutoCommit(true);
        }
    }

    // Add method to fetch sales history
    public List<Sale> getSalesHistory() throws SQLException {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales ORDER BY sale_date DESC";

        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                sales.add(new Sale(
                        rs.getInt("sale_id"),
                        rs.getInt("admin_id"),
                        rs.getDouble("total_amount"),
                        rs.getTimestamp("sale_date")
                ));
            }
        }
        return sales;
    }
}