import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

public class TambahTransaksiUI extends JFrame {
    private JTextField namaField;
    private JTextField jumlahField;
    private JComboBox<String> jenisCombo;
    private JButton simpanButton, batalButton;

    public TambahTransaksiUI() {
        setTitle("Tambah Transaksi");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(5, 2, 10, 10));

        // Komponen Input
        add(new JLabel("Nama Transaksi:"));
        namaField = new JTextField();
        add(namaField);

        add(new JLabel("Jumlah (Rp):"));
        jumlahField = new JTextField();
        add(jumlahField);

        add(new JLabel("Jenis:"));
        jenisCombo = new JComboBox<>(new String[]{"Pemasukan", "Pengeluaran"});
        add(jenisCombo);

        // Tombol
        simpanButton = new JButton("💾 Simpan");
        simpanButton.setBackground(new Color(0, 153, 0));
        simpanButton.setForeground(Color.WHITE);

        batalButton = new JButton("❌ Batal");
        batalButton.setBackground(new Color(204, 0, 0));
        batalButton.setForeground(Color.WHITE);

        add(batalButton);
        add(simpanButton);

        // Event
        simpanButton.addActionListener(ignored -> simpanTransaksi());
        batalButton.addActionListener(ignored -> dispose());

        setVisible(true);
    }

    private void simpanTransaksi() {
        String nama = namaField.getText().trim();
        String jumlahStr = jumlahField.getText().trim();
        String jenis = jenisCombo.getSelectedItem().toString();

        if (nama.isEmpty() || jumlahStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama dan Jumlah harus diisi!");
            return;
        }

        try {
            double jumlah = Double.parseDouble(jumlahStr);
            if (jenis.equals("Pengeluaran")) {
                jumlah = -Math.abs(jumlah);
            }

            try (Connection conn = DatabaseHelper.getConnection()) {
                String sql = "INSERT INTO transaksi (nama_transaksi, jumlah, jenis, tanggal, user_id) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, nama);
                    stmt.setDouble(2, jumlah);
                    stmt.setString(3, jenis);
                    stmt.setDate(4, java.sql.Date.valueOf(LocalDate.now()));
                    stmt.setInt(5, 1); // user_id default

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this, "✅ Transaksi berhasil disimpan!");
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "❌ Gagal menyimpan transaksi.");
                    }
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Jumlah harus berupa angka!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Error DB: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> new TambahTransaksiUI().setVisible(true));
    }
}
