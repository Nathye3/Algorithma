import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DashboardUI extends JFrame {
    private JTable transaksiTable;
    private JLabel saldoLabel;
    private JButton addTransactionButton;

    public DashboardUI() {
        setTitle("Aplikasi Perencanaan Keuangan Harian");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Tabel transaksi
        transaksiTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(transaksiTable);
        add(scrollPane, BorderLayout.CENTER);

        // Panel bawah
        JPanel bottomPanel = new JPanel(new BorderLayout());
        saldoLabel = new JLabel("Saldo: Rp 0");
        saldoLabel.setFont(new Font("Arial", Font.BOLD, 16));
        bottomPanel.add(saldoLabel, BorderLayout.WEST);

        addTransactionButton = new JButton("Tambah Transaksi");
        bottomPanel.add(addTransactionButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // Event tambah transaksi
        addTransactionButton.addActionListener(ignored -> {
            new TambahTransaksiUI(); // buka form
            new Timer(500, ignoredTimer -> loadData()).start(); // reload data
        });

        // Klik kanan (edit / hapus)
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem editItem = new JMenuItem("Edit");
        JMenuItem deleteItem = new JMenuItem("Hapus");
        popupMenu.add(editItem);
        popupMenu.add(deleteItem);
        transaksiTable.setComponentPopupMenu(popupMenu);

        transaksiTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int row = transaksiTable.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    transaksiTable.setRowSelectionInterval(row, row);
                }
            }
        });

        // Edit
        editItem.addActionListener(ignored -> {
            int selectedRow = transaksiTable.getSelectedRow();
            if (selectedRow >= 0) {
                int id = (int) transaksiTable.getValueAt(selectedRow, 0);
                String keterangan = transaksiTable.getValueAt(selectedRow, 1).toString();
                String jumlahStr = transaksiTable.getValueAt(selectedRow, 2).toString();
                new EditTransaksiUI(id, keterangan, jumlahStr);
                new Timer(500, ignoredTimer -> loadData()).start();
            }
        });

        // Hapus
        deleteItem.addActionListener(ignored -> {
            int selectedRow = transaksiTable.getSelectedRow();
            if (selectedRow >= 0) {
                int id = (int) transaksiTable.getValueAt(selectedRow, 0);
                int confirm = JOptionPane.showConfirmDialog(this, "Yakin hapus transaksi?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try (Connection conn = DatabaseHelper.getConnection()) {
                        String sql = "DELETE FROM transaksi WHERE id = ?";
                        PreparedStatement stmt = conn.prepareStatement(sql);
                        stmt.setInt(1, id);
                        stmt.executeUpdate();
                        JOptionPane.showMessageDialog(this, "Transaksi berhasil dihapus.");
                        loadData();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Gagal menghapus transaksi.");
                    }
                }
            }
        });

        loadData(); // tampilkan data pertama kali
        setVisible(true);
    }

    private void loadData() {
        try (Connection conn = DatabaseHelper.getConnection()) {
            String sql = "SELECT * FROM transaksi ORDER BY tanggal DESC, id DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Keterangan", "Jumlah (Rp)", "Tanggal"}, 0);
            double totalSaldo = 0;

            while (rs.next()) {
                int id = rs.getInt("id");
                String keterangan = rs.getString("keterangan");
                double jumlah = rs.getDouble("jumlah");
                java.sql.Date tanggal = rs.getDate("tanggal");

                model.addRow(new Object[]{id, keterangan, jumlah, tanggal});
                totalSaldo += jumlah;
            }

            transaksiTable.setModel(model);
            saldoLabel.setText(String.format("Saldo: Rp %, .2f", totalSaldo)); // format ribuan
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal mengambil data transaksi.");
        }
    }
}
