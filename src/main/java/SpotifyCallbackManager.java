// HTTP used to send and recieve requests to Spotify
// Handles network requests
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class SpotifyCallbackManager {

    private HttpServer server;
    private volatile  String code;

    // Start a local HTTP server (port 8888)
    public void startServer() throws IOException {

        this.server = HttpServer.create(new InetSocketAddress(8888), 0);
        server.createContext("/callback", new MyHandler());
        this.server.start();
    }

    // Register a custom handler for /callback path to get Spotify Authorization Code
    class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException { // Automatically runs when calls MyHandler
            
            String query = exchange.getRequestURI().getQuery(); // Parse redirect URL to get Spotify Auth code
            String[] params = query.split("=");
            String parse_code = params[1];
            params = parse_code.split("&");
            parse_code = params[0];
            SpotifyCallbackManager.this.code = parse_code; // Instancec Variable recieves this code to return to main program.

            String response = "Authorized! Your code is: " + parse_code;
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
    
    // Close HTTP Flow
    public void stopServer(){
        server.stop(0);
        server = null;
    }
    // Getter Method to Return Code
    public String getCode(){
        return code;
    }
}