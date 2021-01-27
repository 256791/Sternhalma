package pwr.tp.sternhalma.server.sternhalma;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pwr.tp.sternhalma.server.menager.Player;

import java.util.Iterator;
import java.util.List;

/**
 * Class representing board it contains pones and fields.
 * It also contain GameMaster class because this class
 * implements movement controller. All rule checking is done
 * by game master.
 */
public class Board {
    private final int type;
    private int playerCount;
    private int moveCount;
    private GameMaster gameMaster;
    protected List<Pone> pones;
    protected List<Field> fields;

    /**
     * Initializer of class Board. Lists need to be already build. Board construction
     * responsibility is passed to SternhalmaBuilder class.
     * @param type type of board 0 is default
     * @param pones List containing initialized pones
     * @param fields List containing initialized fields
     */
    public Board(int type, List<Pone> pones, List<Field> fields){
        this.type = type;
        this.pones = pones;
        this.fields = fields;
    }

    /**
     * Method used to set GameMaster for rule checking
     * @param gameMaster GameMaster that will be used by board
     */
    public void setGameMaster(GameMaster gameMaster) {
        this.gameMaster = gameMaster;
    }

    /**
     * Method used to start game. It assigns pones to players and prepare
     * board for game.
     * @param players List of players that will play the game
     */
    public void startGame(List<Player> players) {
        //todo replace this method with proper one
        playerCount = players.size();
        int[] arr = new int[playerCount];
        for(int i=0;i<playerCount; i++){
            arr[i]=players.get(i).getPlayerId();
        }
        Iterator<Pone> iter = pones.iterator();
        while(iter.hasNext()){
            Pone pone = iter.next();
            if(playerCount == 2){
                if(pone.player == 1) {pone.player = arr[0]; pone.playerN=1;}
                else if(pone.player == 4) {pone.player = arr[1]; pone.playerN=2;}
                else iter.remove();
            } else if(playerCount == 3){
                if(pone.player == 1) {pone.player = arr[0]; pone.playerN=1;}
                else if(pone.player == 3) {pone.player = arr[1]; pone.playerN=2;}
                else if(pone.player == 5) {pone.player = arr[2]; pone.playerN=3;}
                else iter.remove();
            } else if(playerCount == 4){
                if(pone.player == 1) {pone.player = arr[0]; pone.playerN=1;}
                else if(pone.player == 2) {pone.player = arr[1]; pone.playerN=2;}
                else if(pone.player == 4) {pone.player = arr[2]; pone.playerN=3;}
                else if(pone.player == 5) {pone.player = arr[3]; pone.playerN=4;}
                else iter.remove();
            } else if (playerCount == 6){
                pone.player = arr[pone.player-1];
            }
        }
        for(Field field: fields){
            if(playerCount == 2){
                if(field.player == 1) field.player = arr[1];
                else if(field.player == 4) field.player = arr[0];
                else field.player = 0;
            } else if(playerCount == 3){
                if(field.player == 2) field.player = arr[2];
                else if(field.player == 4) field.player = arr[0];
                else if(field.player == 6) field.player = arr[1];
                else field.player = 0;
            } else if(playerCount == 4){
                if(field.player == 1) field.player = arr[2];
                else if(field.player == 2) field.player = arr[3];
                else if(field.player == 4) field.player = arr[0];
                else if(field.player == 5) field.player = arr[1];
                else field.player = 0;
            } else if (playerCount == 6){
                if(field.player != 0) field.player = arr[(field.player+2)%6];
            }
        }
    }

    /**
     * Method used to move pone from field to field. Check if move is possible and
     * moves pone to corresponding field. After success send change to players.
     * @param fromX x coordinate of pone
     * @param fromY y coordinate of pone
     * @param toX x coordinate of destination field
     * @param toY y coordinate of destination field
     * @param player id of player that moves the pone
     * @return true if move was successful false if rule violated
     */
    public boolean move(int fromX, int fromY, int toX, int toY, int player) {
        Pone from = null;
        Field to = null;
        for(Pone pone: pones){
            if(pone.x == fromX && pone.y == fromY){
                from = pone;
                break;
            }
        }

        for(Field field: fields){
            if(field.x == toX && field.y == toY){
                to = field;
                break;
            }
        }
        if (from == null || to == null) return false;

        if(from.player!=player) return false;

        if(gameMaster.isValid(from, to, player, moveCount)){
            from.changeField(to);
            moveCount++;
            return true;
        }
        return false;
    }

    /**
     * Method used to end turn. Method also checks if player won in this turn.
     * @return True if player has won in this turn
     */
    public boolean endTurn(int player) {
        moveCount = 0;
        return gameMaster.hasWon(player);
    }

    /**
     * Method used to get information about current board state.
     * @return JSONObject containing information about current board status
     */
    public synchronized JSONObject getBoardStatus() {
        JSONObject boardStatus = new JSONObject();
        JSONArray board = new JSONArray();
        try {
            boardStatus.put("type", "boardData");
            for (Pone pone : pones){
                JSONObject jsonPone = new JSONObject();
                jsonPone.put("x", pone.x);
                jsonPone.put("y", pone.y);
                jsonPone.put("player", pone.playerN);
                board.put(jsonPone);
            }
            boardStatus.put("board", board);
        } catch (JSONException ignore) {}
        return boardStatus;
    }

    /**
     * Method used to get board type information.
     * @return Int id of board type 0 is default
     */
    public int getType() {
        return type;
    }
}
