import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class EditTransaksiUI extends JFrame {
    private JTextField namaTransaksiField;
    private JTextField jumlahField;
    private JComboBox<String> jenisCombo;
    private JButton simpanButton, batalButton;
    private int transaksiId;

    public EditTransaksiUI(int id, String namaTransaksi, String jumlahStr) {
        this.transaksiId = id;

        setTitle("✏️ Edit Transaksi");  
        setSize(400, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new GridBagLayout());

        Font labelFont = new Font("SansSerif", Font.PLAIN, 14);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 12, 10, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nama Transaksi
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Nama Transaksi:"), gbc);
        gbc.gridx = 1;
        namaTransaksiField = new JTextField(namaTransaksi);
        add(namaTransaksiField, gbc);

        // Jumlah
        gbc.gridx = 0;
        gbc.gridy++;
        add(new JLabel("Jumlah (Rp):"), gbc);
        gbc.gridx = 1;
        String displayJumlah = jumlahStr.replace("-", ""); // Hapus tanda minus jika ada
        jumlahField = new JTextField(displayJumlah);
        add(jumlahField, gbc);

        // Jenis Transaksi
        gbc.gridx = 0;
        gbc.gridy++;
        add(new JLabel("Jenis:"), gbc);
        gbc.gridx = 1;
        jenisCombo = new JComboBox<>(new String[]{"Pemasukan", "Pengeluaran"});
        jenisCombo.setSelectedItem(jumlahStr.startsWith("-") ? "Pengeluaran" : "Pemasukan");
        add(jenisCombo, gbc);

        // Tombol Simpan
        gbc.gridx = 0;
        gbc.gridy++;
        simpanButton = new JButton("✔ Simpan");
        simpanButton.setBackground(new Color(46, 204, 113));
        simpanButton.setForeground(Color.WHITE);
        simpanButton.setFont(labelFont);
        simpanButton.setFocusPainted(false);
        simpanButton.addActionListener(ignored -> updateTransaksi());
        add(simpanButton, gbc);

        // Tombol Batal
        gbc.gridx = 1;
        batalButton = new JButton("✖ Batal");
        batalButton.setBackground(new Color(231, 76, 60));
        batalButton.setForeground(Color.WHITE);
        batalButton.setFont(labelFont);
        batalButton.setFocusPainted(false);
        batalButton.addActionListener(ignored -> dispose());
        add(batalButton, gbc);

        setVisible(true);
    }

    private void updateTransaksi() {
        String namaTransaksi = namaTransaksiField.getText().trim();
        String jumlahText = jumlahField.getText().trim();
        String jenis = jenisCombo.getSelectedItem().toString();

        if (namaTransaksi.isEmpty() || jumlahText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field wajib diisi!");
            return;
        }

        try {
            double jumlah = Double.parseDouble(jumlahText);

            // Pastikan pengeluaran bernilai negatif
            if (jenis.equals("Pengeluaran")) {
                jumlah = -Math.abs(jumlah);
            } else {
                jumlah = Math.abs(jumlah);
            }

            try (Connection conn = DatabaseHelper.getConnection()) {
                String sql = "UPDATE transaksi SET nama_transaksi = ?, jumlah = ?, jenis = ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, namaTransaksi);
                stmt.setDouble(2, jumlah);
                stmt.setString(3, jenis);
                stmt.setInt(4, transaksiId);

                int result = stmt.executeUpdate();

                if (result > 0) {
                    JOptionPane.showMessageDialog(this, "✅ Transaksi berhasil diubah!");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "❌ Transaksi tidak ditemukan!");
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Jumlah harus berupa angka yang valid!");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal mengubah transaksi: " + e.getMessage());
        }
    }
}
