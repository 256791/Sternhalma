package pwr.tp.sternhalma.server.sternhalma;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pwr.tp.sternhalma.server.menager.Game;
import pwr.tp.sternhalma.server.menager.Player;
import pwr.tp.sternhalma.server.menager.Server;

/**
 * Class represents implementation of Game sternhalma
 * it handles all incoming actions and option changes.
 * It passes game mechanics responsibility to board and
 * game master classes but handles player permissions
 * and non game state change requests.
 */
public class Sternhalma extends Game {
    private Board board;
    private int currentPlayerId;

    /**
     * Initializer setting default values and calling parent
     * initialization method
     * @param server reference to server
     * @param id unique id of the game
     */
    public Sternhalma(Server server, int id){
        super(server, id);
        playerCount = 2;
    }

    /**
     * Sets board that will be used in game
     * @param board board to be set
     */
    protected void setBoard(Board board) {
        this.board = board;
    }

    /**
     * Sets new player count
     * @param playerCount amount of players that will play the game
     * @return true if set correctly false if wrong value passed
     */
    protected boolean setPlayerCount(int playerCount) {
        if(players.size()>playerCount) return false;
        if(playerCount != 2 && playerCount != 3 && playerCount != 4 && playerCount != 6) return false;
        this.playerCount = playerCount;
        return true;
    }

    /**
     * Implementation of abstract method responsible for action handling
     * @param player reference to the Player that send the action
     * @param action JSONObject containing description of action to perform
     * @throws JSONException if JSONObject have wrong or missing values
     */
    @Override
    public void action(Player player, JSONObject action) throws JSONException {
        String type = action.getString("type");
        switch (type) {
            case "leave" ->
                leave(player);
            case "getGameInfo" ->
                player.respond(getGameInfo());
            case "getBoardStatus" ->
                player.respond(board.getBoardStatus());
            case "move" -> {
                if (currentPlayerId == player.getPlayerId()) {
                    synchronized (this) {
                        int sx = action.getInt("fromX");
                        int sy = action.getInt("fromY");
                        int dx = action.getInt("toX");
                        int dy = action.getInt("toY");
                        if (board.move(sx, sy, dx, dy, player.getPlayerId())) {
                            JSONObject change = new JSONObject();
                            change.put("type", "notify");
                            change.put("message", "boardStatus");
                            JSONArray board = new JSONArray();
                            JSONObject jsonField = new JSONObject();
                            jsonField.put("x", sx);
                            jsonField.put("y", sy);
                            jsonField.put("player", 0);
                            board.put(jsonField);
                            jsonField = new JSONObject();
                            jsonField.put("x", dx);
                            jsonField.put("y", dy);
                            jsonField.put("player", players.indexOf(player)+1);
                            board.put(jsonField);
                            change.put("board", board);
                            sendToAll(change);
                            player.respond(Player.ACCEPT);
                        } else {
                            player.respond(Player.WRONG_VAL);
                        }
                    }
                } else {
                player.respond(Player.NO_PERM);
            }
            }
            case "endTurn" -> {
                if (currentPlayerId == player.getPlayerId()) {
                    if (board.endTurn(player.getPlayerId())) {
                        JSONObject win = new JSONObject();
                        try {
                            win.put("type", "notify");
                            win.put("message", "winner");
                            win.put("id", player.getPlayerId());
                        } catch (JSONException ignore) {
                        }
                        sendToAll(win);
                        leave(player);
                    }
                    synchronized (this) {
                        if (currentPlayerId == player.getPlayerId()) {
                            int index = players.indexOf(player);
                            index = (index + 1) % playerCount;
                            Player tplayer = players.get(index);
                            currentPlayerId = tplayer.getPlayerId();
                            player.respond(Player.ACCEPT);
                            tplayer.respond(Player.TURN);
                        }
                        else player.respond(Player.NO_PERM);
                    }
                }
                else player.respond(Player.NO_PERM);
            }
            default ->
                player.respond(Player.WRONG_VAL);
        }
    }

    /**
     * Implementation of abstract method responsible for option change handling
     * @param player reference to the Player that send the change request
     * @param change JSONObject containing description of change to be done
     * @throws JSONException if JSONObject have wrong or missing values
     */
    @Override
    public void option(Player player, JSONObject change) throws JSONException {
        synchronized (this) {
            if (player.getPlayerId() == adminId) {
                String option = change.getString("option");
                Object value = change.get("value");
                switch (option) {
                    case "playerCount" -> {
                        if (value instanceof Integer) {
                            int val = (Integer) value;
                            if (setPlayerCount(val)) {
                                player.respond(Player.ACCEPT);
                            }
                            player.respond(Player.WRONG_VAL);
                        } else throw new JSONException("Type mismatch");
                    }
                    case "admin" -> {
                        if (value instanceof Integer) {
                            for (Player p : players) {
                                if (p.getPlayerId() == (Integer) value) {
                                    adminId = (Integer) value;
                                    player.respond(Player.ACCEPT);
                                    p.respond(Player.ADMIN);
                                    return;
                                }
                            }
                            player.respond(Player.WRONG_VAL);
                        } else throw new JSONException("Mismatch types");
                    }
                    case "start" -> {
                        if (!started) {
                            if (players.size() == playerCount) {
                                started = true;
                                player.respond(Player.ACCEPT);
                                board.startGame(players);
                                for(int i=0; i<players.size(); i++){
                                    JSONObject start = new JSONObject();
                                    start.put("type", "notify");
                                    start.put("message", "gameStarted");
                                    start.put("player", i+1);
                                    start.put("board", board.getType());
                                    players.get(i).respond(start);
                                }
                                sendToAll(board.getBoardStatus());
                                Player tplayer;
                                tplayer = players.get(0);
                                currentPlayerId = tplayer.getPlayerId();
                                tplayer.respond(Player.TURN);

                            } else player.respond(Player.WRONG_VAL);

                        } else player.respond(Player.WRONG_VAL);
                    }
                    default ->
                        player.respond(Player.WRONG_VAL);
                }
            } else {
                player.respond(Player.NO_PERM);
            }
        }
    }

    /**
     * Implementation of abstract method responsible for creating
     * JSON containing current game configuration
     * @return JSONObject containing information about current game
     */
    @Override
    protected JSONObject getGameInfo() {
        JSONObject info = new JSONObject();
        try{
            info.put("type","notify");
            info.put("message","gameInfo");
            info.put("game", "sternhalma");
            info.put("gameId", getId());
            info.put("started", started);
            info.put("playerCount", playerCount);
            info.put("board", board.getType());
        } catch (JSONException ignore) {}
        return info;
    }
}
