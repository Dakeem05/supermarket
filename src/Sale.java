import java.sql.Timestamp;
import java.util.List;

public class Sale {
    private int saleId;
    private int adminId;
    private double totalAmount;
    private Timestamp saleDate;
    private List<SaleItem> items;

    // Constructor, Getters & Setters
    public Sale(int saleId, int adminId, double totalAmount, Timestamp saleDate) {
        this.saleId = saleId;
        this.adminId = adminId;
        this.totalAmount = totalAmount;
        this.saleDate = saleDate;
    }

    // Getters & Setters
    public int getSaleId() { return saleId; }
    public int getAdminId() { return adminId; }
    public double getTotalAmount() { return totalAmount; }
    public Timestamp getSaleDate() { return saleDate; }
    public List<SaleItem> getItems() { return items; }
    public void setItems(List<SaleItem> items) { this.items = items; }
}