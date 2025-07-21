import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHelper {
    // Ganti sesuai nama database dan port MySQL kamu
    private static final String DB_URL = "jdbc:mysql://localhost:3306/keuangan_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ MySQL JDBC Driver berhasil dimuat.");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL JDBC Driver tidak ditemukan!");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("✅ Koneksi ke database berhasil.");
            return conn;
        } catch (SQLException e) {
            System.err.println("❌ Gagal terhubung ke database:");
            System.err.println("URL: " + DB_URL);
            System.err.println("User: " + DB_USER);
            System.err.println("Error: " + e.getMessage());
            throw e;
        }
    }

    // Fungsi tambahan untuk test koneksi manual
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("❌ Test koneksi gagal: " + e.getMessage());
            return false;
        }
    }

    // Jalankan langsung untuk test dari VSCode
    public static void main(String[] args) {
        if (testConnection()) {
            System.out.println("🟢 Koneksi database berhasil!");
        } else {
            System.out.println("🔴 Gagal koneksi ke database.");
        }
    }
}
