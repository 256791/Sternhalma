package pwr.tp.sternhalma.server.menager;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Server class contains all of the games and players. Its responsibility is to accept new connections
 * and menage existing games and players (add/remove/find). It writes log to System.out.
 */
public class Server extends Thread {
    private final int port;
    private volatile boolean running;
    private ServerSocket socketServer;

    private final List<Player> players;
    private final List<Game> games;
    private int currentPlayerId;
    private int currentGameId;


    public static void main(String[] args) {
        int port = 8080;
        if(args.length == 1){
            try{
                port = Integer.parseInt(args[0]);
            }catch(NumberFormatException ignored){}
        }
        Server server = new Server(port);
        server.start();
    }

    /**
     * Class constructor sets all of the variables needed to launch the server.
     * @param port port that server will be running at
     */
    public Server(int port) {
        this.port = port;
        this.running = false;
        this.players = new ArrayList<>();
        this.games = new ArrayList<>();
        this.currentPlayerId = 1;
        this.currentGameId = 1;
    }

    /**
     * Get for running variable.
     * @return state of server
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Method used to close the server. Closes all player threads and socket server.
     */
    public synchronized void close() {
        if(running) {
            running = false;
            try {
                socketServer.close();
            } catch (IOException e) {
                System.out.println("Error occurred during socket server closing");
            }
            for (Player player : players) {
                try {
                    player.close();
                } catch (IOException e) {
                    System.out.println("Error occurred during socket closing");
                }
            }
        }
    }

    /**
     * Thread inherited run method. Should not be called.
     */
    public void run() {
        initServer();

        while (running) {
            try {
                Socket socket = socketServer.accept();
                newPlayer(socket);
            } catch (IOException e) {
                if (running) {
                    System.out.println("Error occurred during " +
                            "acceptation of incoming connection");
                }
            }
        }

        System.out.println("Server stopped");
    }

    /**
     * Method used to create and add new instance of game to the server.
     * @param properties JSONObject containing description of the game to be created.
     * @return New instance of Game. Null if cant create
     * @throws JSONException if JSONObject is corrupted, have missing values, or wrong values.
     */
    public synchronized Game newGame(JSONObject properties) throws JSONException {
        GameBuilderDirector director = new GameBuilderDirector();
        Game game = director.build(currentGameId, properties);
        games.add(game);
        currentGameId++;
        return game;
    }

    /**
     * Method used to find game by given id.
     * @param id id of the game to be found.
     * @return instance of Game. Null if cant find matching id.
     */
    public synchronized Game findGame(int id) {
        for (Game game : games){
            if (game.getId() == id) return game;
        }
        return null;
    }

    /**
     * Method used to remove a Game from the stored games in the server.
     * @param game instance of Game to be removed
     */
    public synchronized void removeGame(Game game) {
        games.remove(game);
    }

    /**
     * Method used to remove a Player from the stored players in the server.
     * @param player instance of Player to be removed
     */
    public synchronized void removePlayer(Player player) {
        players.remove(player);
    }

    /**
     * Private method that handles Player creation.
     * @param socket socket that new Player will listen to
     * @throws IOException if cant open input output streams. In that circumstance
     * Player is not added to the collection of players.
     */
    private synchronized void newPlayer(Socket socket) throws IOException {
        Player player = new Player(this, currentPlayerId, socket);
        player.start();
        players.add(player);

        System.out.print("Connected to ");
        System.out.print(socket.getRemoteSocketAddress().toString());
        System.out.print(" player ID: ");
        System.out.println(currentPlayerId);

        currentPlayerId++;
    }

    /**
     * Private method used to initialize the server.
     */
    private synchronized void initServer(){
        try {
            socketServer = new ServerSocket(port);
            System.out.print("Server created at port ");
            System.out.println(port);
        } catch (IOException e) {
            System.out.print("Can not create server at port ");
            System.out.println(port);
            return;
        }
        running = true;
    }
}