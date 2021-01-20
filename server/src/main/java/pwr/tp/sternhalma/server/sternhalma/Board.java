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
    private int type;
    private int playerCount;
    private int currentPlayer;
    private GameMaster gameMaster;
    protected List<Pone> pones;
    protected List<Field> fields;

    private JSONObject boardStarus;
    private boolean statusFlag;

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
        this.statusFlag = false;
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
        playerCount = players.size();
        int[] arr = new int[playerCount];
        for(int i=0;i<playerCount; i++){
            arr[i]=players.get(i).getPlayerId();
        }
        synchronized (this){
            Iterator<Pone> iter = pones.iterator();
            while(iter.hasNext()){
                Pone pone = iter.next();
                if(pone.player > playerCount) iter.remove();
                else pone.player = arr[pone.player - 1];
            }
            for(Field field: fields){
                if(field.player  > playerCount) field.player = 0;
                else if(field.player!=0) field.player = arr[field.player - 1];
            }
        }
    }

    /**
     * Method used to move pone from field to field. Check if move is possible and
     * moves pone to corresponding field
     * @param fromX x coordinate of pone
     * @param fromY y coordinate of pone
     * @param toX x coordinate of destination field
     * @param toY y coordinate of destination field
     * @param player id of player that moves the pone
     * @return true if move was successful false if rule violated
     */
    public boolean move(int fromX, int fromY, int toX, int toY, int player) {
        currentPlayer=player;
        Pone from = null;
        Field to = null;
        synchronized (this){
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
        }
        if (from == null || to == null) return false;

        if(gameMaster.isValid(from, to, player)){
            from.changeField(to);
            statusFlag = false;
            return true;
        }
        return false;
    }

    /**
     * Method used to end turn. Method also checks if player won in this turn.
     * @return True if player has won in this turn
     */
    public boolean endTurn() {
        return gameMaster.hasWon(currentPlayer);
    }

    /**
     * Method used to get information about current board state.
     * If something changed updates json before send.
     * @return JSONObject containing information about current board status
     */
    public synchronized JSONObject getBoardStatus() {
        if(!statusFlag) {
            boardStarus = new JSONObject();
            JSONArray board = new JSONArray();
            try {
                boardStarus.append("type", "notify");
                boardStarus.append("message", "boardStatus");
                boardStarus.append("players", playerCount);
                for (Pone pone : pones){
                    JSONObject jsonPone = new JSONObject();
                    jsonPone.append("x", pone.x);
                    jsonPone.append("y", pone.y);
                    jsonPone.append("player", pone.player);
                    board.put(jsonPone);
                }
                boardStarus.append("board", board);
            } catch (JSONException ignore) {}
            statusFlag = true;
        }
        return boardStarus;
    }

    /**
     * Method used to get board type information.
     * @return Int id of board type 0 is default
     */
    public int getType() {
        return type;
    }
}
