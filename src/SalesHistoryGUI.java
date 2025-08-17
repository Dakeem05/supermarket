import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

public class SalesHistoryGUI extends JFrame {
    private JTable salesTable;
    private DefaultTableModel tableModel;
    private SaleDAO saleDAO = new SaleDAO();

    public SalesHistoryGUI() {
        setTitle("Sales History");
        setSize(800, 600);
        setLayout(new BorderLayout());

        // Table setup
        String[] columns = {"Sale ID", "Cashier ID", "Total", "Date"};
        tableModel = new DefaultTableModel(columns, 0);
        salesTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(salesTable);

        // Refresh button
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadSales());

        add(scrollPane, BorderLayout.CENTER);
        add(refreshBtn, BorderLayout.SOUTH);

        loadSales();
        setVisible(true);
    }

    private void loadSales() {
        tableModel.setRowCount(0);
        try {
            List<Sale> sales = saleDAO.getSalesHistory();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            for (Sale sale : sales) {
                tableModel.addRow(new Object[]{
                        sale.getSaleId(),
                        sale.getAdminId(),
                        String.format("$%.2f", sale.getTotalAmount()),
                        dateFormat.format(sale.getSaleDate())
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading sales: " + e.getMessage());
        }
    }
}