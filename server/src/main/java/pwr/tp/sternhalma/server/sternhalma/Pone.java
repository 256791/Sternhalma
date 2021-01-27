package pwr.tp.sternhalma.server.sternhalma;

public class Pone {
    protected int x;
    protected int y;
    protected int player;
    protected int playerN;
    protected Field currentField;

    public Pone(int x, int y, int player){
        this.x = x;
        this.y = y;
        this.player = player;
        this.playerN = player;
        currentField = null;
    }

    public void changeField(Field destinaton){
        currentField.currentPone = null;
        currentField = destinaton;
        destinaton.currentPone = this;
        x = destinaton.x;
        y = destinaton.y;
    }
}
