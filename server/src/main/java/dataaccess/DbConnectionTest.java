package dataaccess;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DbConnectionTest {

    static Connection getConnection() throws SQLException, IOException {
        Properties properties = new Properties();

        try (InputStream input =
                     DbConnectionTest.class.getResourceAsStream("/db.properties")) {
            if (input == null) {
                throw new IOException("Could not find db.properties on the classpath");
            }
            properties.load(input);
        }

        String url = "jdbc:mysql://localhost:3306/";

        return DriverManager.getConnection(
                url,
                properties.getProperty("db.user"),
                properties.getProperty("db.password")
        );
    }

    public static void main(String[] args) {
        try (Connection connection = getConnection()) {
            System.out.println("Connected to MySQL!");
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}