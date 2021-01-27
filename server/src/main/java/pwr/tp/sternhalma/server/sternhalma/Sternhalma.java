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
     * Implementation of abstract method responsible for request handling
     * @param player reference to the Player that send the action
     * @param request JSONObject containing description of request to handle
     * @throws JSONException if JSONObject have wrong or missing values
     */
    @Override
    public void handleRequest(Player player, JSONObject request) throws JSONException {
        String type = request.getString("type");
        switch (type) {
            case "leave" ->
                leave(player);
            case "getGameData" ->
                player.respond(getGameInfo(player.getPlayerId()==adminId));
            case "getBoardData" ->
                player.respond(board.getBoardStatus());
            case "move" -> {
                if (currentPlayerId == player.getPlayerId()) {
                    synchronized (this) {
                        int sx = request.getInt("fromX");
                        int sy = request.getInt("fromY");
                        int dx = request.getInt("toX");
                        int dy = request.getInt("toY");
                        if (board.move(sx, sy, dx, dy, player.getPlayerId())) {
                            JSONObject change = new JSONObject();
                            change.put("type", "boardData");
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
                } else player.respond(Player.NO_PERM);
            }
            case "turn" -> {
                if (currentPlayerId == player.getPlayerId()) {
                    if (board.endTurn(player.getPlayerId())) {
                        JSONObject win = new JSONObject();
                        try {
                            win.put("type", "winner");
                            win.put("id", players.indexOf(player)+1);
                        } catch (JSONException ignore) {
                        }
                        sendToAll(win);
                        leave(player);
                    }
                    else synchronized (this) {
                        if (currentPlayerId == player.getPlayerId()) {
                            int index = players.indexOf(player);
                            index = (index + 1) % playerCount;
                            Player tplayer = players.get(index);
                            currentPlayerId = tplayer.getPlayerId();
                            tplayer.respond("{\"type\": \"turn\"}");
                        }
                        else player.respond(Player.NO_PERM);
                    }
                }
                else player.respond(Player.NO_PERM);
            }
            case "start" -> {
                if(player.getPlayerId() == adminId) {
                    if (!started) {
                        if (players.size() == playerCount) {
                            started = true;
                            player.respond(Player.ACCEPT);
                            board.startGame(players);
                            for (int i = 0; i < players.size(); i++) {
                                JSONObject start = new JSONObject();
                                start.put("type", "start");
                                start.put("player", i + 1);
                                start.put("board", board.getType());
                                players.get(i).respond(start);
                            }
                            sendToAll(board.getBoardStatus());
                            Player tplayer;
                            tplayer = players.get(0);
                            currentPlayerId = tplayer.getPlayerId();
                            tplayer.respond("{\"type\": \"turn\"}");

                        } else player.respond(Player.WRONG_VAL);

                    } else player.respond(Player.WRONG_VAL);
                } else player.respond(Player.NO_PERM);
            }
            case "gameData" -> {
                if (player.getPlayerId() == adminId) {
                    switch (request.getString("change")) {
                        case "playerCount" -> {
                            int val = request.getInt("value");
                            if (setPlayerCount(val)) {
                                synchronized (this) {
                                    for(Player p:players) {
                                        p.respond(getGameInfo(p.getPlayerId()==adminId));
                                    }
                                }
                            } else player.respond(Player.WRONG_VAL);
                        }
                        case "admin" ->{
                            //ignore not implemented in client
                        }
                    }
                } else player.respond(Player.NO_PERM);
            }
            default ->
                player.respond(Player.WRONG_VAL);
        }
    }

    /**
     * Implementation of abstract method responsible for creating
     * JSON containing current game configuration
     * @return JSONObject containing information about current game
     */
    @Override
    protected JSONObject getGameInfo(boolean admin) {
        JSONObject info = new JSONObject();
        try{
            info.put("type","gameData");
            info.put("game", "sternhalma");
            info.put("gameId", getId());
            info.put("started", started);
            info.put("playerCount", playerCount);
            info.put("board", board.getType());
            info.put("admin", admin);
        } catch (JSONException ignore) {}
        return info;
    }
}
