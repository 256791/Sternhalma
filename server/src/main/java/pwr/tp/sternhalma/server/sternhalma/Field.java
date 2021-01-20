package pwr.tp.sternhalma.server.sternhalma;

public class Field {
    protected int x;
    protected int y;
    protected int player;
    protected Field[] adjacent;
    protected Pone currentPone;

    public Field(int x, int y, int player){
        this.x = x;
        this.y = y;
        this.player = player;
        adjacent = null;
        currentPone = null;
    }

    public void setAdjacent(Field[] adjacent){
        this.adjacent = adjacent;
    }
}
