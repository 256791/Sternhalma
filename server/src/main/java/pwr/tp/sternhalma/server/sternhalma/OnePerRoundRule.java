package pwr.tp.sternhalma.server.sternhalma;

/**
 * Rule limiting to 1 pone per round and one type of move per round(basic/jump)
 */
public class OnePerRoundRule implements Rule {
    private Pone lastPone;
    private boolean basic;
    @Override
    public int isValid(Pone from, Field to, int player, int move) {
        if(move == 0) {
            lastPone = from;
            basic = false;
            for(int i=0; i<6; i++){
                if(from.currentField.adjacent[i] == to){
                    basic = true;
                    break;
                }
            }
            return 0;
        }else{
            if(lastPone!=from) return -1;
            for (int i = 0; i < 6; i++) {
                if (from.currentField.adjacent[i] == to) {
                    if(basic) return 0;
                    else return -1;
                }
            }
            if(basic) return -1;
            else return 0;
        }
    }
}
