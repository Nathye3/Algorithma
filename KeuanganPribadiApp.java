import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class KeuanganPribadiApp extends JFrame {
    private JTextField tfNama, tfJumlah, tfTanggal;
    private JComboBox<String> cbJenis;
    private JLabel lblSaldo;
    private DefaultTableModel tableModel;
    private double saldo = 0;
    private final NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    private final ArrayList<Transaksi> daftarTransaksi = new ArrayList<>();

    public KeuanganPribadiApp() {
        setTitle("Aplikasi Keuangan Pribadi");
        setSize(800, 550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // ===== Panel Input =====
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(new EmptyBorder(15, 15, 5, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        tfNama = new JTextField();
        tfJumlah = new JTextField();
        tfTanggal = new JTextField(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
        cbJenis = new JComboBox<>(new String[]{"Pemasukan", "Pengeluaran"});
        JButton btnTambah = new JButton("Tambah");
        JButton btnSimpanDB = new JButton("Simpan ke Database");

        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Nama Transaksi:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        inputPanel.add(tfNama, gbc);

        gbc.gridx = 3; gbc.gridwidth = 1;
        inputPanel.add(new JLabel("Jumlah (Rp):"), gbc);
        gbc.gridx = 4; gbc.gridwidth = 2;
        inputPanel.add(tfJumlah, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Tanggal:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        inputPanel.add(tfTanggal, gbc);

        gbc.gridx = 3; gbc.gridwidth = 1;
        inputPanel.add(new JLabel("Jenis Transaksi:"), gbc);
        gbc.gridx = 4; gbc.gridwidth = 2;
        inputPanel.add(cbJenis, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2;
        inputPanel.add(btnTambah, gbc);
        gbc.gridx = 4;
        inputPanel.add(btnSimpanDB, gbc);

        // ===== Tabel =====
        tableModel = new DefaultTableModel(new String[]{"Tanggal", "Nama", "Jumlah", "Jenis"}, 0);
        JTable table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(0, 15, 0, 15));

        // ===== Panel Bawah =====
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(new Color(244, 67, 54));
        btnLogout.setForeground(Color.WHITE);

        lblSaldo = new JLabel("Saldo: Rp 0");
        lblSaldo.setHorizontalAlignment(SwingConstants.RIGHT);
        lblSaldo.setFont(new Font("SansSerif", Font.BOLD, 18));

        bottomPanel.add(btnLogout, BorderLayout.WEST);
        bottomPanel.add(lblSaldo, BorderLayout.EAST);

        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // ===== Event Listener =====
        btnTambah.addActionListener(e -> tambahTransaksi());
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Yakin logout?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new LoginUI().setVisible(true);
            }
        });
        btnSimpanDB.addActionListener(e -> simpanKeDatabase());

        // ===== Load saldo awal =====
        hitungSaldoDariDatabase();
    }

    private void tambahTransaksi() {
        String nama = tfNama.getText().trim();
        String jumlahStr = tfJumlah.getText().trim();
        String tanggal = tfTanggal.getText().trim();
        String jenis = cbJenis.getSelectedItem().toString();

        if (nama.isEmpty() || jumlahStr.isEmpty() || tanggal.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Isi semua field!");
            return;
        }

        try {
            double jumlah = Double.parseDouble(jumlahStr);
            if (jumlah < 0) {
                JOptionPane.showMessageDialog(this, "Jumlah tidak boleh negatif!");
                return;
            }

            Transaksi transaksi = new Transaksi(nama, jumlah, jenis, tanggal);
            daftarTransaksi.add(transaksi);

            if (jenis.equals("Pengeluaran")) saldo -= jumlah;
            else saldo += jumlah;

            updateTabel();
            lblSaldo.setText("Saldo: " + formatRupiah.format(saldo));

            tfNama.setText("");
            tfJumlah.setText("");
            tfTanggal.setText(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
            tfNama.requestFocus();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Jumlah harus angka!");
        }
    }

    private void updateTabel() {
        tableModel.setRowCount(0);
        for (Transaksi t : daftarTransaksi) {
            tableModel.addRow(new Object[]{t.tanggal, t.nama, formatRupiah.format(t.jumlah), t.jenis});
        }
    }

    private void simpanKeDatabase() {
        if (daftarTransaksi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tidak ada transaksi untuk disimpan.");
            return;
        }

        try (Connection conn = DatabaseHelper.getConnection()) {
            String sql = "INSERT INTO transaksi (nama_transaksi, jumlah, jenis, tanggal, user_id) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);

            for (Transaksi t : daftarTransaksi) {
                stmt.setString(1, t.nama);
                stmt.setDouble(2, t.jenis.equals("Pengeluaran") ? -Math.abs(t.jumlah) : t.jumlah);
                stmt.setString(3, t.jenis);
                stmt.setDate(4, new java.sql.Date(new SimpleDateFormat("dd-MM-yyyy").parse(t.tanggal).getTime()));
                stmt.setInt(5, 1);
                stmt.addBatch();
            }

            int[] hasil = stmt.executeBatch();

            JOptionPane.showMessageDialog(this, hasil.length + " transaksi berhasil disimpan.");
            daftarTransaksi.clear();
            tableModel.setRowCount(0);

            // Ambil saldo terbaru dari database
            hitungSaldoDariDatabase();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal menyimpan transaksi: " + e.getMessage());
        }
    }

    private void hitungSaldoDariDatabase() {
        try (Connection conn = DatabaseHelper.getConnection()) {
            String sql = "SELECT SUM(jumlah) AS total_saldo FROM transaksi";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                saldo = rs.getDouble("total_saldo");
                lblSaldo.setText("Saldo: " + formatRupiah.format(saldo));
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal mengambil saldo dari database.");
        }
    }

    static class Transaksi {
        String nama;
        double jumlah;
        String jenis;
        String tanggal;

        Transaksi(String nama, double jumlah, String jenis, String tanggal) {
            this.nama = nama;
            this.jumlah = jumlah;
            this.jenis = jenis;
            this.tanggal = tanggal;
        }
    }
}
