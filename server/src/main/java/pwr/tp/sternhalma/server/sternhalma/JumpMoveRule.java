package pwr.tp.sternhalma.server.sternhalma;

/**
 * Rule defining move 3 fields in straight line trough another pone
 */
public class JumpMoveRule implements Rule {
    @Override
    public int isValid(Pone from, Field to, int player, int move) {
        for(int i=0; i<6; i++) {
            if(to.adjacent[i].adjacent[i].currentPone==from){
                if(to.adjacent[i].player!=0) return 1;
                else return -1;
            }
        }
        return 0;
    }
}
