package pwr.tp.sternhalma.server.menager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Player class represent client connected to server. Its responsibility is to
 * handle incoming requests and respond to them. Part of the request will be
 * passed to its assigned Game class methods.
 */
public class Player extends Thread{
    private final int id;
    private Game game;

    private volatile boolean running;

    private final Server server;
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;


    public static final String PING;
    public static final String ACCEPT;
    //rejections
    public static final String NO_PERM;
    public static final String WRONG_VAL;
    public static final String GAME_CON_ERR;
    //errors
    public static final String JSON_ERR;

    static {
        PING = "{\"type\": \"ping\"}";
        ACCEPT = "{\"type\": \"accept\"}";

        NO_PERM = "{\"type\": \"reject\", \"reason\": \"noPermissions\"}";
        WRONG_VAL = "{\"type\": \"reject\", \"reason\": \"wrongValue\"}";
        GAME_CON_ERR = "{\"type\": \"reject\", \"reason\": \"CantConnectToGame\"}";

        JSON_ERR = "{\"type\": \"error\", \"error\": \"JSONException\"}";
    }


    /**
     * Initializer of Player class.
     * @param server reference to Server class that is the owner of initialized Player class.
     * @param id unique player Id used to recognize players
     * @param socket socket that player will use to read from and write to
     * @throws IOException if cant open socket in/out streams
     */
    public Player(Server server, int id, Socket socket) throws IOException {
        this.server = server;
        this.id = id;
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    /**
     * Get for unique Player id
     * @return id of the player
     */
    public int getPlayerId(){
        return id;
    }

    /**
     * Method used to close connection and stop thread
     * @throws IOException if IOException occurred during attempt to close the socket
     */
    public void close() throws IOException {
        running = false;
        socket.close();
    }

    /**
     * Thread inherited run method. Should not be called. Use start()
     * method instead for non blocking behaviour
     */
    public void run() {
        running = true;
        String buffer;
        JSONObject request;

        while(this.running) {
            try {
                buffer = in.readLine();
                if(buffer == null) throw new IOException();

                request = new JSONObject(buffer);
                handleRequest(request);
            } catch (IOException e) {
                if(this.running) {
                    if(server.isRunning()) server.removePlayer(this);
                    running = false;
                }
            } catch (JSONException e) {
                respond(JSON_ERR);
            }
        }
        if(game != null){
            game.leave(this);
        }
    }

    /**
     * Method used to send the message to client.
     * @param message JSONObject message to be sent.
     */
    public void respond(JSONObject message){
        String respond = message.toString();
        respond = respond.replace('\n', ' ');
        out.println(respond);
    }

    /**
     * Method used to send the message to client.
     * @param message String message to be sent.
     */
    public void respond(String message) {
        out.println(message);
    }

    /**
     * Private method that handles request received from client.
     * @param request JSONObject received from client
     * @throws JSONException if JSON have missing keys or wrong values
     */
    private void handleRequest(JSONObject request) throws JSONException{
        String type = request.getString("type");
        switch (type) {
            case "ping" ->
                respond(PING);
            case "join" -> {
                int id = request.getInt("id");
                Game game = server.findGame(id);
                joinGame(game);
            }
            case "create" -> {
                JSONObject properties = request.getJSONObject("properties");
                Game game = server.newGame(properties);
                joinGame(game);
            }
            default -> {
                if (game == null) throw new JSONException("No game assigned to player");
                game.handleRequest(this, request);
            }
        }
    }

    /**
     * Private method used to disconnect from last game and connect to new one.
     * @param game Game to be connected to
     */
    public void joinGame(Game game) {
        if(this.game != null) {
            this.game.leave(this);
            this.game = null;
        }
        if(game != null) {
            if(game.join(this)) {
                this.game = game;
                return;
            }
        }
        respond(GAME_CON_ERR);
    }

    /**
     * Method used to kick players from game. It sets player game reference to null
     * and sends information to client containing kick reason.
     */
    public void kick(String message){
        try {
            JSONObject kick = new JSONObject("{\"type\": \"kick\", " +
                    "\"reason\": \"" + message + "\"}");
            respond(kick);
        } catch (JSONException ignore) {}
        game = null;
    }
}