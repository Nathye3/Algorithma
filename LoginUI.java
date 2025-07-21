import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginUI extends JFrame {
    private JTextField tfUsername;
    private JPasswordField pfPassword;

    public LoginUI() {
        setTitle("Selamat Datang di Aplikasi Keuangan Pribadi");
        setSize(350, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 2, 10, 10));
        setResizable(false);

        add(new JLabel("Username:"));
        tfUsername = new JTextField();
        add(tfUsername);

        add(new JLabel("Password:"));
        pfPassword = new JPasswordField();
        add(pfPassword);

        JButton btnLogin = new JButton("Login");
        JButton btnDaftar = new JButton("Daftar");
        add(btnLogin);
        add(btnDaftar);

        btnLogin.addActionListener(e -> prosesLogin());
        btnDaftar.addActionListener(e -> new RegisterUI().setVisible(true));
        getRootPane().setDefaultButton(btnLogin);
    }

    private void prosesLogin() {
        String username = tfUsername.getText().trim();
        String password = new String(pfPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan Password harus diisi!");
            return;
        }

        try (Connection conn = DatabaseHelper.getConnection()) {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "✅ Login berhasil!");
                new KeuanganPribadiApp().setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "❌ Username atau password salah!", "Login Gagal", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal login: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginUI().setVisible(true));
    }               
}
