package pwr.tp.sternhalma.server.sternhalma;

import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for move validity checking and win detection
 */
public class GameMaster {
    private final List<Rule> ruleset;
    private final Board board;
    private int move;
    private int lastPlayer;

    /**
     * Constructor. After construction need to call addRule to make ruleset
     * @param board reference to board that GameMaster will be attached to
     */
    public GameMaster(Board board){
        this.board = board;
        this.ruleset = new ArrayList<Rule>();
        this.lastPlayer = -1;
        this.move = 0;
    }

    /**
     * method used to add rule to ruleset
     * @param rule rule to be added
     */
    public synchronized void addRule(Rule rule){
        ruleset.add(rule);
    }

    /**
     * Method used to check if player won the game
     * @param player id of player to check
     * @return true if player won
     */
    public boolean hasWon(int player) {
        synchronized (board){
            for(Pone pone: board.pones){
                if(pone.player == player){
                    if(pone.currentField.player != player) return false;
                }
            }
        }
        return true;
    }

    /**
     * Method used to check move validity
     * @param from Pone that player wants to move
     * @param to Field that player wants to move to
     * @param player id of player performing move action
     * @return true if move is possible
     */
    public synchronized boolean isValid(Pone from, Field to, int player) {
        if(lastPlayer != player) {
            lastPlayer = player;
            move = 0;
        }
        boolean flag = false;
        for(Rule rule: ruleset){
            int result = rule.isValid(from, to, player, move);
            if(result == -1) return false;
            if(result == 1) flag = true;
        }
        if(flag) {
            move++;
            return true;
        }
        return false;
    }
}
