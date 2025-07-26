import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    
    private static final String PROPERTIES_FILE = "application.properties";
    
    public static void main(String[] args) {
        DatabaseConnection app = new DatabaseConnection();
        app.connectAndQuery();
    }
    
    public void connectAndQuery() {
        Properties props = loadProperties();
        
        if (props == null) {
            System.err.println("Failed to load application properties");
            return;
        }
        
        String url = buildConnectionUrl(props);
        String username = props.getProperty("db.username");
        String password = props.getProperty("db.password");
        
        System.out.println("Attempting to connect to MySQL database...");
        System.out.println("URL: " + url);
        System.out.println("Username: " + username);
        
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            System.out.println("Successfully connected to MySQL database!");
            
            // Execute SELECT 1 query
            executeSelectQuery(connection);
            
        } catch (SQLException e) {
            System.err.println("Database connection failed!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private Properties loadProperties() {
        Properties props = new Properties();
        
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                System.err.println("Unable to find " + PROPERTIES_FILE + " in classpath");
                return null;
            }
            
            props.load(input);
            System.out.println("Properties loaded successfully");
            return props;
            
        } catch (IOException e) {
            System.err.println("Error loading properties file: " + e.getMessage());
            return null;
        }
    }
    
    private String buildConnectionUrl(Properties props) {
        String host = props.getProperty("db.host", "localhost");
        String port = props.getProperty("db.port", "3306");
        String database = props.getProperty("db.database", "test");
        
        return String.format("jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", 
                           host, port, database);
    }
    
    private void executeSelectQuery(Connection connection) {
        String query = "SELECT 1 as result";
        
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            
            System.out.println("\nExecuting query: " + query);
            
            if (resultSet.next()) {
                int result = resultSet.getInt("result");
                System.out.println("Query executed successfully!");
                System.out.println("Result: " + result);
            }
            
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
