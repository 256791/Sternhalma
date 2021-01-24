package pwr.tp.sternhalma.server.sternhalma;

/**
 * Rule limiting movement to inside of destination area
 */
public class LockedMoveRule implements Rule {
    @Override
    public int isValid(Pone from, Field to, int player, int move) {
        if(from.currentField.player == player && to.player!= player) return -1;
        return 0;
    }
}
