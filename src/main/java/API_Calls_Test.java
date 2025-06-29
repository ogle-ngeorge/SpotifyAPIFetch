// Handles API Calls to Spotify

// Shows the current song playing on the Spotify account (WORKS)
// Shows the current song artist
// Shows the current song album

import java.awt.Desktop;
import java.net.URI;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlayingContext;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.player.GetInformationAboutUsersCurrentPlaybackRequest;


public class API_Calls_Test {
    
    // Make an API Call to get the Song Name
    public static String getCurrentSong(TokenRefresher tokenRefresher){

        // Create an API call to an account in Spotify

        SpotifyApi spotify_api = new SpotifyApi.Builder()
            .setAccessToken(tokenRefresher.getAccessToken())
            .build();
        
        // Create a GET Request to ask Spotify's servers the song playing.
            // Build Request Object
        GetInformationAboutUsersCurrentPlaybackRequest request_song_name = spotify_api.getInformationAboutUsersCurrentPlayback().build();
        // Error Catching
        try{
            // Execute and Recieve Response
            if (request_song_name != null){
                CurrentlyPlayingContext execute_song_name = request_song_name.execute();
                // Get song Title
                return execute_song_name.getItem().getName();
            }
            else{
                // Return Message when song IS NOT playing.
                return "No song currently playing";
            }
        }
        // When any other error arises (Will flesh this out later for better debugging)
        catch (Exception IException){
            return "Error fetching song.";
        }        
    }


    // Make an API Call to get the Artist Name
    public static String getCurrentArtist(TokenRefresher tokenRefresher){

        // Create an API call to an account in Spotify

        SpotifyApi spotify_api = new SpotifyApi.Builder()
            .setAccessToken(tokenRefresher.getAccessToken())
            .build();
        
        // Create a GET Request to ask Spotify's servers the song playing.
            // Build Request Object
        GetInformationAboutUsersCurrentPlaybackRequest request_artist_name = spotify_api.getInformationAboutUsersCurrentPlayback().build();
        // Error Catching
        try{
            // Execute and Recieve Response
            if (request_artist_name != null){
                CurrentlyPlayingContext execute_artist_name = request_artist_name.execute();
                // Get song Title w/ Casting
                Track return_song_title = (Track) execute_artist_name.getItem();
                return return_song_title.getArtists()[0].getName();
            }
            else{
                // Return Message when song IS NOT playing.
                return "No song currently playing";
            }
        }
        // When any other error arises (Will flesh this out later for better debugging)
        catch (Exception IException){
            return "Error fetching song.";
        }        
    }

        // Make an API Call to get the ALbum Art
    public static String getCurrentAlbumArt(TokenRefresher tokenRefresher){

        // Create an API call to an account in Spotify

        SpotifyApi spotify_api = new SpotifyApi.Builder()
            .setAccessToken(tokenRefresher.getAccessToken())
            .build();
        
        // Create a GET Request to ask Spotify's servers the song playing.
            // Build Request Object
        GetInformationAboutUsersCurrentPlaybackRequest request_art_name = spotify_api.getInformationAboutUsersCurrentPlayback().build();
        // Error Catching
        try{
            // Execute and Recieve Response
            if (request_art_name != null){
                CurrentlyPlayingContext execute_art_name = request_art_name.execute();
                // Get Art Title w/ Casting
                Track return_art = (Track) execute_art_name.getItem();
                return return_art.getAlbum().getImages()[0].getUrl();
            }
            else{
                // Return Message when song IS NOT playing.
                return "No song currently playing";
            }
        }
        // When any other error arises (Will flesh this out later for better debugging)
        catch (Exception IException){
            return "Error fetching song.";
        }        
    }

    public static void main(String[] args) {
        // Generate HTTP Callback Server
        SpotifyCallbackManager callbackManager = new SpotifyCallbackManager();
        try {
            callbackManager.startServer();
        } catch (Exception e) {
        }
        // Generate PKCE Values
        TokenRefresher tokenRefresher = new TokenRefresher();
        String code_verifier = tokenRefresher.generateCodeVerifier();
        String code_challenge = tokenRefresher.generateCodeChallenge(code_verifier);

        // Open URL Automatically to Browser and Recieve Spotify Code
        String auth_url = tokenRefresher.sendAuthRequest(code_verifier, code_challenge);
        System.out.println(auth_url);
        try {
            Desktop.getDesktop().browse(new URI(auth_url));
        } catch (Exception e){
            System.out.println("Could not open browser automatically, open URL manually:" + auth_url);
        }

        // Use Spotify Code to Recieve Access Token
        String spotify_auth_code = null;
        while ((spotify_auth_code = callbackManager.getCode()) == null) { // Waits for the user to authenticate and for the authorization code to be set
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("This is the code received from authentication: " + spotify_auth_code);

        callbackManager.stopServer();

        String json_response = tokenRefresher.exchangeCodeChallengeForToken(spotify_auth_code, code_verifier);
        String access_token = tokenRefresher.parseForAccessToken(json_response);
        String refresh_token = tokenRefresher.parseForRefreshToken(json_response);

        System.out.println("ACCESS TOKEN: " + access_token);
        
        tokenRefresher.setAccessToken(access_token);
        tokenRefresher.setRefreshToken(refresh_token);
        
        String current_playing_song = getCurrentSong(tokenRefresher);
        String current_playing_artist = getCurrentArtist(tokenRefresher);
        System.out.println(current_playing_song + " ~ " + current_playing_artist);
    }   

        // TIMER TO GET NEW TOKEN
        /* 
        Timer timer = new Timer(); // Timer by 55 minutes (in miliseconds).
        int time_begin = 0;
        int time_interval = 3300000;
        
        TimerTask get_new_refresh_token = new TimerTask() { // Fetch Refresh Token Task     
            @Override
            public void run(){
                String json_response_refresh_token = tokenRefresher.refreshAccessToken(refresh_token);
                String new_refresh_token = tokenRefresher.parseForRefreshToken(json_response_refresh_token);
                String new_access_token = tokenRefresher.parseForAccessToken(json_response_refresh_token);
                tokenRefresher.setAccessToken(new_access_token);
                tokenRefresher.setRefreshToken(new_refresh_token);
            }
        };
        timer.schedule(get_new_refresh_token, time_begin, time_interval);

        


        // API Calls

                */
            
        
        
        }
    

