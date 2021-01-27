package pwr.tp.sternhalma.server.menager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class representing Game object. It defines how Player will interact
 * with real instance of game.
 */
public abstract class Game {
    private final Server server;
    private final int id;

    protected int playerCount;
    protected List<Player> players;
    protected int adminId;
    protected boolean started;

    /**
     * Initializer of the Game class used to initialize some of the fields
     * especially final ones
     * @param server reference to Server class that owns this game
     * @param id unique id of the game
     */
    public Game(Server server, int id) {
        this.server = server;
        this.id = id;
        players = new ArrayList<Player>();
        this.started = false;
    }

    /**
     * Abstract method used to communicate with game
     * @param player reference to the Player that send the action
     * @param request JSONObject containing request to handle
     * @throws JSONException if error inside JSON (corrupted, missing values etc)
     */
    public abstract void handleRequest(Player player, JSONObject request) throws JSONException;

    /**
     * Get method for id field of Game
     * @return id of the game
     */
    public int getId(){
        return this.id;
    }

    /**
     * Method used to join Game. It handles response to Player.
     * @param player reference to Player that wants to join.
     * @return True if succesfuly joined. False if failed to join.
     */
    public synchronized boolean join(Player player) {
        if (!started) {
            if (playerCount > players.size()) {
                players.add(player);
                if (players.size() == 1) {
                    adminId = player.getPlayerId();
                }
                player.respond(getGameInfo(player.getPlayerId()==adminId));
                return true;
            }
        }
        player.respond(Player.GAME_CON_ERR);
        return false;
    }

    /**
     * Method used to leave from game.
     * @param player Reference to Player object that wants to leave the game
     */
    public synchronized void leave(Player player) {
        if(player.getPlayerId() == adminId){
            if(players.size() == 1){
                    started = true;
                    server.removeGame(this);
                return;
            }else {
                players.remove(player);
                adminId = players.get(0).getPlayerId();
                players.get(0).respond(getGameInfo(true));
            }
        }else {
            players.remove(player);
        }
        if(started){
            for(Player p: players){
                p.kick("Player left during the game");
            }
            server.removeGame(this);
        }
    }

    /**
     * Abstract method used to return JSONObject containing information's about game.
     * Its used to update client game info variables
     * @return JSONObject containing information about instance of game
     */
    protected abstract JSONObject getGameInfo(boolean admin);

    /**
     * Method used to send message to all Players connected to the game
     * @param message JSONObject containing message to be send
     */
    protected synchronized void sendToAll(JSONObject message) {
        for(Player player: players) {
            player.respond(message);
        }
    }

    /**
     * Method used to send message to all Players connected to the game
     * @param message String containing message to be send
     */
    protected synchronized void sendToAll(String message) {
        for(Player player: players) {
            player.respond(message);
        }
    }
}