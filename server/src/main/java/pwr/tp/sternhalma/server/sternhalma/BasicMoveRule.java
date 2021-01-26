package pwr.tp.sternhalma.server.sternhalma;

/**
 * Rule defining move 1 field away from source
 */
public class BasicMoveRule implements Rule {

    @Override
    public int isValid(Pone from, Field to, int player, int move) {
        if(move>0) return 0;
        if(to.currentPone!=null) return -1;
        for(int i=0; i<6; i++){
            if(from.currentField.adjacent[i] == to){
                return 1;
            }
        }
        return 0;
    }
}
