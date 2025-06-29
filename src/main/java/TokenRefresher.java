// Handles logic behind Access Tokens


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Random;

import org.json.JSONObject;

public class TokenRefresher{
    
    // Variables to receive Spotify API (Hardcoded to my account)
    private String user_client_id = "8d67730388db4528ad3eae63a8627974"; // My personal spotify account
    private String redirect_uri = "http://127.0.0.1:8888/callback";

    private String access_token = "";
    private String refresh_token = "";

    public String getAccessToken(){
        return this.access_token;
    }
    
    public String getRefreshToken(){
        return this.refresh_token;
    }

    public void setAccessToken(String new_access_token){
        this.access_token = new_access_token;
    }
    
    public void setRefreshToken(String new_refresh_token){
        this.refresh_token = new_refresh_token;
    }
    // Refresh Token (AUthorization Code w/ PCKE Flow)

    // .... Code Verifier with Random String Code Between 43 and 123 ~
    public String generateCodeVerifier(){
        String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
        String code_verifier = "";
        Random random = new Random();
        int random_length = random.nextInt(129 - 43) + 43;
        while (code_verifier.length() < random_length){
            int index = (int) (random.nextFloat() * CHARS.length());
            code_verifier = code_verifier + CHARS.charAt(index);
        }
        return code_verifier;
    }

    // .... Hash the Code Verifier into a Code Challenge ~
    public String generateCodeChallenge(String code_verifier){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256"); // Prepares the hasher algorithm
            byte[] code = code_verifier.getBytes(StandardCharsets.UTF_8); // Converts String to Bytes to be hashed
            byte[] hash = md.digest(code); // Hash the Bytes using the SHA-256 Algorithm
            String base64 = Base64.getUrlEncoder().withoutPadding().encodeToString(hash); // Convert to String
            return base64;

        } catch (Exception NoSuchAlgorithmException) {
            return "Error";
        }
    }
    // .... Do a GET Request to authorize user using Code Verifier ~
    public String sendAuthRequest(String code_verifier, String code_challenge){
        try {
 
            String authURL = "https://accounts.spotify.com/authorize" // URL Link for Authorization
            +"?client_id=" + this.user_client_id
            +"&response_type=code"
            +"&redirect_uri=" + URLEncoder.encode(this.redirect_uri, "UTF-8")
            +"&code_challenge_method=S256"
            +"&code_challenge=" + code_challenge
            +"&scope=user-read-playback-state"; // <--- Specifies Type of Request
            return authURL;

        } catch (Exception e) {
            return "Error";        
        }
    }
    // .... Do a POST Exchange Using the Code from parseURL and Code_Verifier to Get Access Token
    public String exchangeCodeChallengeForToken(String redirect_url_code, String code_verifier){
        try {
            URL url = new URL("https://accounts.spotify.com/api/token"); // URL object
        
            HttpURLConnection connect_to_url = (HttpURLConnection) url.openConnection(); // Opens connection to URL
            connect_to_url.setRequestMethod("POST"); 
            connect_to_url.setDoOutput(true); // Allows POST Request to be made. (Shows that it sends the data to be sent to request body)
            connect_to_url.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); // Sets headers for ongoing request to Spotify API

            String post_request_body = "grant_type=authorization_code" // POST parameters in the HTTP Post Request to get Token (Think of Cmd Curl)
            +"&code=" + URLEncoder.encode(redirect_url_code, "UTF-8")
            +"&redirect_uri=" + URLEncoder.encode(this.redirect_uri, "UTF-8")
            +"&client_id=" + URLEncoder.encode(this.user_client_id, "UTF-8")
            +"&code_verifier=" + URLEncoder.encode(code_verifier, "UTF-8");
            
            try (OutputStream os = connect_to_url.getOutputStream()){ // Opens OutputStream that allows Java to send data to Spotify over HTTP connection.
                os.write(post_request_body.getBytes(StandardCharsets.UTF_8)); // Writes POST parameters as bytes to Spotify's API
            }

            int response_code = connect_to_url.getResponseCode(); // Reads HTTP Response from Spotify
            InputStream is;
            if (response_code == 200){ // If the Spotify Response is successful (200), it gets the response.
                is = connect_to_url.getInputStream();
            }
            else{
                is = connect_to_url.getErrorStream();
            }

            StringBuilder response = new StringBuilder(); // Object to Build String to Return Response from Spotify Line by line
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) { // BufferedReader reads Response from Spotify
                String line;
                while ((line = br.readLine()) != null){ // Reads response one line at a time
                    response.append(line.trim()); // Adds each line to the total response while deleting white spaces with trim
                }
            } 
        return response.toString(); // Returns complete response from Spotify as a String (Contains Access Token & Refresh Token)

        } catch (Exception e) {
            return "Error";
        }
    }
    // .... Parse JSON Response for Access Token using JSON Parser
    public String parseForAccessToken(String json_response){
        JSONObject json = new JSONObject(json_response);
        return json.getString("access_token");
    }

    // .... Parse JSON Response for Refresh Token using JSON Parser
    public String parseForRefreshToken(String json_response){
        JSONObject json = new JSONObject(json_response);
        return json.getString("refresh_token");
    }

    public String refreshAccessToken(String refresh_token){
        try {
            URL url = new URL("https://accounts.spotify.com/api/token"); // URL object
        
            HttpURLConnection connect_to_url = (HttpURLConnection) url.openConnection(); // Opens connection to URL
            connect_to_url.setRequestMethod("POST"); 
            connect_to_url.setDoOutput(true); // Allows POST Request to be made. (Shows that it itends the data to be sent to request body)
            connect_to_url.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); // Sets headers for ongoing request to Spotify API

            String post_request_body = "grant_type=refresh_token" // POST parameters in the HTTP Post Request to get Token (Think of Cmd Curl)
            +"&refresh_token=" + URLEncoder.encode(refresh_token, "UTF-8")
            +"&client_id=" + URLEncoder.encode(this.user_client_id, "UTF-8");

            try (OutputStream os = connect_to_url.getOutputStream()){ // Opens OutputStream that allows Java to send data to Spotify over HTTP connection.
                os.write(post_request_body.getBytes(StandardCharsets.UTF_8)); // Writes POST parameters as bytes to Spotify's API
            }

            int response_code = connect_to_url.getResponseCode(); // Reads HTTP Response from Spotify
            InputStream is;
            if (response_code == 200){ // If the Spotify Response is successful (200), it gets the response.
                is = connect_to_url.getInputStream();
            }
            else{
                is = connect_to_url.getErrorStream();
            }

            StringBuilder response = new StringBuilder(); // Object to Build String to Return Response from Spotify Line by line
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) { // BufferedReader reads Response from Spotify
                String line;
                while ((line = br.readLine()) != null){ // Reads response one line at a time
                    response.append(line.trim()); // Adds each line to the total response while deleting white spaces with trim
                }
            } 
        return response.toString(); // Returns complete response from Spotify as a String (Contains Access Token & Refresh Token)

        } catch (Exception e) {
            return "Error";
        }
    }

    
}