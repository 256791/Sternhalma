package pwr.tp.sternhalma.client;

import java.awt.*;
import java.awt.geom.Ellipse2D;

public class Field {
    protected int x;
    protected int y;
    protected int player;
    protected boolean selected;

    private int xPos;
    private int yPos;
    private Ellipse2D select;
    private Ellipse2D pone;

    public Field(int x, int y, int xPos, int yPos){
        this.x = x;
        this.y = y;
        this.xPos = xPos;
        this.yPos = yPos;
        this.player = 0;
        select = new Ellipse2D.Float(xPos-12,yPos-12,24,24);
        pone = new Ellipse2D.Float(xPos-8,yPos-8,16,16);
    }

    public boolean contains (int x, int y){
        return select.contains(x, y);
    }

    public void draw(Graphics2D g2d){

        if(player!=0){
            Color color = switch (player) {
                case 1 -> new Color(242, 231, 66);
                case 2 -> new Color(25, 25, 234);
                case 3 -> new Color(231, 40, 53);
                case 4 -> new Color(255, 84, 152);
                case 5 -> new Color(186, 7, 199);
                case 6 -> new Color(50, 255, 50);
                default -> Color.BLACK;
            };

            g2d.setPaint(color);
            g2d.fill(pone);
            g2d.setPaint(Color.BLACK);
            g2d.setStroke(new BasicStroke(1));
            g2d.draw(pone);
        }
        if(selected){
            g2d.setPaint(Color.RED);
            g2d.setStroke(new BasicStroke(3));
            g2d.draw(select);
        }
    }
}
