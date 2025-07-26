// DatabaseConnection.java
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class DatabaseConnection {
    
    private static final String PROPERTIES_FILE = "application.properties";
    private Properties props;
    
    public static void main(String[] args) {
        DatabaseConnection app = new DatabaseConnection();
        app.startServer();
    }
    
    public void startServer() {
        props = loadProperties();
        
        if (props == null) {
            System.err.println("Failed to load application properties");
            return;
        }
        
        int port = Integer.parseInt(props.getProperty("server.port", "8080"));
        
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new RootHandler());
            server.createContext("/health", new HealthHandler());
            server.createContext("/db-test", new DatabaseTestHandler());
            server.setExecutor(null);
            
            System.out.println("Starting HTTP server on port " + port);
            System.out.println("Available endpoints:");
            System.out.println("  http://localhost:" + port + "/");
            System.out.println("  http://localhost:" + port + "/health");
            System.out.println("  http://localhost:" + port + "/db-test");
            
            server.start();
            System.out.println("Server started successfully!");
            
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public String connectAndQuery() {
        if (props == null) {
            return "Failed to load application properties";
        }
        
        String url = buildConnectionUrl(props);
        String username = props.getProperty("db.username");
        String password = props.getProperty("db.password");
        
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String result = executeSelectQuery(connection);
            return "Database connection successful!\n" + result;
            
        } catch (SQLException e) {
            return "Database connection failed: " + e.getMessage();
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
    
    private String executeSelectQuery(Connection connection) {
        String query = "SELECT 1 as result";
        
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            
            if (resultSet.next()) {
                int result = resultSet.getInt("result");
                return "Query 'SELECT 1' executed successfully! Result: " + result;
            } else {
                return "Query executed but no results returned";
            }
            
        } catch (SQLException e) {
            return "Error executing query: " + e.getMessage();
        }
    }
    
    // HTTP Handlers
    class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "MySQL Connection App\n\n" +
                            "Available endpoints:\n" +
                            "- /health - Server health check\n" +
                            "- /db-test - Test database connection\n";
            
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
    
    class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "Server is running OK";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
    
    class DatabaseTestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = connectAndQuery();
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
