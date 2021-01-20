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
     * Abstract method used to make in game action (move end turn or else)
     * @param player reference to the Player that send the action
     * @param action JSONObject containing description of action to perform
     * @throws JSONException if error inside JSON (corrupted, missing values etc)
     */
    public abstract void action(Player player, JSONObject action) throws JSONException;

    /**
     * Abstract method used to change Game options
     * (change admin, change player count or else)
     * @param player reference to the Player that send the change request
     * @param change JSONObject containing description of change to be done
     * @throws JSONException if error inside JSON (corrupted, missing values etc)
     */
    public abstract void option(Player player, JSONObject change) throws JSONException;

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
                    player.respond(Player.ADMIN);
                }
                player.respond(Player.ACCEPT);
                player.respond(getGameInfo());
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
    public void leave(Player player) {
        if(player.getPlayerId() == adminId){
            if(players.size() == 1){
                    server.removeGame(this);
                return;
            }else {
                synchronized (this) {
                    players.remove(player);
                    adminId = players.get(0).getPlayerId();
                }
                players.get(0).respond(Player.ADMIN);
            }
        }else {
            players.remove(player);
        }
        if(started){
            sendToAll("{\"type\": \"notify\", \"message\": \"gameEnded\"}");
            synchronized (this){
                for(Player p: players){
                    p.joinGame(null);
                }
            }
            server.removeGame(this);
        }
    }

    /**
     * Abstract method used to return JSONObject containing informations about game.
     * Its used to update client game info variables
     * @return JSONObject containing information about instance of game
     */
    protected abstract JSONObject getGameInfo();

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