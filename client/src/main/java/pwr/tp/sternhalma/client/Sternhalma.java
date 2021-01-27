package pwr.tp.sternhalma.client;

import org.json.JSONArray;
import org.json.JSONException;

import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;



public class Sternhalma extends JFrame {
    private final Client client;
    private Board board;
    private BufferedImage boardImage;
    private JTextArea info;
    private Field source;
    private Field destination;
    private volatile boolean lock;
    private final int player;

    public static void main(String[] args) {
        Sternhalma s = new Sternhalma(null, 0, 1);
    }

    public Sternhalma(Client client, int boardType, int player) {
        this.client = client;
        this.lock = true;
        this.player = player;
        buildGUI(boardType);
        setVisible(true);
    }

    public void turn(){
        info.setText("Your turn");
        lock = false;
    }

    public void accept(){
        if(source!=null && destination!=null) {
            info.setText("OK");
            source.selected = false;
            source = destination;
            destination = null;
            repaint();
            lock = false;
        }
    }

    public void reject(){
        info.setText("Incorrect move!");
        source.selected = false;
        destination.selected = false;
        source = null;
        destination = null;
        repaint();
        lock = false;
    }

    public void endGame(String message){
        lock = true;
        JOptionPane.showMessageDialog(this, message, "Info",
                JOptionPane.INFORMATION_MESSAGE);
        client.leave();
    }

    public void exit(boolean absolute){
        lock = true;
        if(absolute) System.exit(0);
        else {
            client.leave();
            dispose();
        }
    }

    public void updateBoard(JSONArray fields) throws JSONException {
        int length = fields.length();
        for(int i=0; i<length; i++) {
            JSONObject change = fields.getJSONObject(i);
            board.update(change.getInt("x"), change.getInt("y"),
                    change.getInt("player"));
        }
    }

    private void move() {
        if(!lock) {
            lock = true;
            if (source != null && destination != null) {
                info.setText(" Wait...");
                JSONObject message = new JSONObject();
                try {
                    message.put("type", "move");
                    message.put("fromX", source.x);
                    message.put("fromY", source.y);
                    message.put("toX", destination.x);
                    message.put("toY", destination.y);
                } catch (JSONException ignore) {
                }
                client.send(message);
            } else {
                info.setText(" Select fields!");
                lock = false;
            }
            repaint();
        }
    }

    private void endTurn() {
        if (!lock) {
            lock = true;
            info.setText("Opponents turn");
            if (source != null) {
                source.selected = false;
                source = null;
            }
            if (destination != null) {
                destination.selected = false;
                destination = null;
            }
            repaint();
            client.send("{\"type\":\"turn\"}");
        }
    }

    private void press(MouseEvent mouse){
        if(!lock) {
            int x = mouse.getX();
            int y = mouse.getY();
            Field field = board.contains(x, y);
            if(field!=null) {
                if (field.player == player) {
                    if (source != null) source.selected = false;
                    source = field;
                    field.selected = true;
                } else if (field.player == 0) {
                    if (destination != null) destination.selected = false;
                    destination = field;
                    field.selected = true;
                }
            }
            repaint();
        }
    }

    private void buildGUI(int boardType){
        setTitle("Sternhalma");
        setResizable(false);
        setLocation(100,100);
        getContentPane().setPreferredSize(new Dimension(640, 480));
        pack();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {exit(true);}
        });

        JPanel panel = new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.drawImage(boardImage, 0, 0, null);
                board.draw(g2d);
            }
        };
        panel.setLayout(null);

        panel.addMouseListener(new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent e) {press(e);}
        });
        add(panel);

        String filepath = "/board_"+ boardType +".png";
        try {
            boardImage = ImageIO.read(Sternhalma.class.getResource(filepath));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Resource file corrupted!", "Error",
                    JOptionPane.ERROR_MESSAGE);
            exit(true);
            return;
        }

        try{
            this.board = new Board(boardType);
        }
        catch(Exception e){
            JOptionPane.showMessageDialog(this, "Error occurred while loading the board", "Error",
                    JOptionPane.ERROR_MESSAGE);
            exit(true);
        }

        JButton move = new JButton("Move");
        move.setBounds(485, 100,140,50);
        move.addActionListener(e -> move());
        panel.add(move);

        JButton endTurn = new JButton("End Turn");
        endTurn.setBounds(485, 200,140,50);
        endTurn.addActionListener(e -> endTurn());
        panel.add(endTurn);

        String color = switch (player) {
            case 1 -> "Yellow";
            case 2 -> "Blue";
            case 3 -> "Red";
            case 4 -> "Pink";
            case 5 -> "Purple";
            case 6 -> "Green";
            default -> "ERROR";
        };
        info = new JTextArea(" You are " + color);
        info.setEditable(true);
        info.setHighlighter(null);
        info.setBackground(new Color(239, 255, 254));
        info.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        info.setFont(new Font(Font.SERIF,Font.PLAIN,20));
        info.setBounds(485, 10,140, 30);
        panel.add(info);
    }
}
