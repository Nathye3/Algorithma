import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class RegisterUI extends JFrame {
    private JTextField tfUsername;
    private JPasswordField pfPassword;

    public RegisterUI() {
        setTitle("Registrasi Akun");
        setSize(350, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(4, 2, 10, 10));

        add(new JLabel("Username:"));
        tfUsername = new JTextField();
        add(tfUsername);

        add(new JLabel("Password:"));
        pfPassword = new JPasswordField();
        add(pfPassword);

        JButton btnDaftar = new JButton("Daftar");
        btnDaftar.addActionListener(e -> prosesRegistrasi());
        add(new JLabel()); // kosong
        add(btnDaftar);
    }

    private void prosesRegistrasi() {
        String username = tfUsername.getText().trim();
        String password = new String(pfPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi.");
            return;
        }

        try (Connection conn = DatabaseHelper.getConnection()) {
            String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Registrasi berhasil. Silakan login.");
            dispose();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Registrasi gagal: " + e.getMessage());
        }
    }
}
