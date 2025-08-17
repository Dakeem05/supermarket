public class SaleItem {
    private int itemId;
    private int saleId;
    private int productId;
    private int quantity;
    private double price;

    // Constructor, Getters & Setters
    public SaleItem(int itemId, int saleId, int productId, int quantity, double price) {
        this.itemId = itemId;
        this.saleId = saleId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    // Getters & Setters
    public int getItemId() { return itemId; }
    public int getSaleId() { return saleId; }
    public int getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
}