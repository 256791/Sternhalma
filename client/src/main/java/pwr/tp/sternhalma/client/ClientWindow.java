package pwr.tp.sternhalma.client;

import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ClientWindow extends JFrame {
    private Client client;
    private boolean admin;

    private JFormattedTextField portField;
    private JFormattedTextField gameIdField;
    private JButton connectButton;
    private JButton joinButton;
    private JButton newGameButton;
    private JComboBox<String> playerSelect;
    private int playerSelectComboIndex;
    private JButton startButton;
    private JComboBox<String> boardSelect;

    public ClientWindow(Client client){
        this.client = client;
        this.admin = false;
        buildGUI();
    }

    public void gameInfo(JSONObject info) throws JSONException{
        gameIdField.setEnabled(false);
        newGameButton.setEnabled(false);
        joinButton.setEnabled(false);
        gameIdField.setValue(info.getInt("gameId"));
        playerSelect.setSelectedIndex(
                switch (info.getInt("playerCount")){
                    case 2 -> 0;
                    case 3 -> 1;
                    case 4 -> 2;
                    case 6 -> 3;
                    default -> throw new JSONException("Wrong value");
                }
        );
        admin = info.getBoolean("admin");
        if(admin) {
            playerSelect.setEnabled(true);
            boardSelect.setEnabled(true);
            startButton.setEnabled(true);
        }
    }

    public void reject(String reason) {
        JOptionPane.showMessageDialog(this, reason, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    private void connect(){
        int port = ((Number)portField.getValue()).intValue();
        System.out.println(port);
        if(client.connect(port)){
            gameIdField.setEnabled(true);
            joinButton.setEnabled(true);
            newGameButton.setEnabled(true);
            portField.setEnabled(false);
            connectButton.setEnabled(false);
        }
    }

    private void join(){
        int gameId = ((Number)gameIdField.getValue()).intValue();
        client.send("{\"type\": \"join\", \"id\":"+gameId+"}");
    }

    private void newGame(){
        client.send("{\"type\": \"create\",\"properties\":{" +
                "\"game\": \"sternhalma\",\"default\": false," +
                "\"playerCount\": 2,\"board\": 0,\"rules\":[" +
                "{\"rule\": \"OnePerRoundRule\"},{\"rule\": \"BasicMoveRule\"}," +
                "{\"rule\": \"JumpMoveRule\"},{\"rule\": \"LockedMoveRule\"}]}}");
        gameIdField.setEnabled(false);
        newGameButton.setEnabled(false);
        joinButton.setEnabled(false);
    }

    private void changePlayerCount(ActionEvent option){
        if(admin) {
            if (playerSelectComboIndex != playerSelect.getSelectedIndex()) {
                playerSelectComboIndex = playerSelect.getSelectedIndex();
                client.send("{\"type\": \"gameData\", \"change\": \"playerCount\"," +
                        "\"value\": " +
                        switch (playerSelectComboIndex) {
                            case 0 -> 2;
                            case 1 -> 3;
                            case 2 -> 4;
                            case 3 -> 6;
                            default -> 0;
                        }
                        + "}");
            }
        }
    }

    private void startGame() {
        if(admin) client.send("{\"type\": \"start\"}");
    }

    private void buildGUI(){
        setTitle("Sternhalma");
        setResizable(false);
        setLocation(100,100);
        getContentPane().setPreferredSize(new Dimension(320, 240));
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);



        JLabel portLabel = new JLabel("Port");
        portLabel.setBounds(10,10,80,20);
        add(portLabel);

        portField = new JFormattedTextField();
        portField.setValue(8080);
        portField.setBounds(10,30,80,30);
        add(portField);

        connectButton = new JButton("Connect");
        connectButton.addActionListener(e->connect());
        connectButton.setBounds(100, 30, 100, 30);
        add(connectButton);

        JLabel gameIdLabel = new JLabel("Game ID");
        gameIdLabel.setBounds(10,70,80,20);
        add(gameIdLabel);
        gameIdField = new JFormattedTextField(new JLabel("GameID"));
        gameIdField.setValue(1);
        gameIdField.setBounds(10, 90, 40, 30);
        gameIdField.setEnabled(false);
        add(gameIdField);

        joinButton = new JButton("Join");
        joinButton.addActionListener(e->join());
        joinButton.setBounds(100,90,100,30);
        joinButton.setEnabled(false);
        add(joinButton);

        newGameButton = new JButton("New Game");
        newGameButton.addActionListener(e->newGame());
        newGameButton.setBounds(210,90,100,30);
        newGameButton.setEnabled(false);
        add(newGameButton);

        String[] playerComboList = {"2 Players", "3 Players", "4 Players", "6 Players"};
        playerSelect = new JComboBox<>(playerComboList);
        playerSelect.setBounds(10,140,80,30);
        playerSelect.setSelectedIndex(0);
        playerSelectComboIndex = 0;
        playerSelect.setEnabled(false);
        playerSelect.addActionListener(this::changePlayerCount);
        add(playerSelect);

        String[] boardComboList = {"Clasic Board"};
        boardSelect = new JComboBox<>(boardComboList);
        boardSelect.setBounds(100,140,100,30);
        boardSelect.setSelectedIndex(0);
        boardSelect.setEnabled(false);
        add(boardSelect);

        startButton = new JButton("Start");
        startButton.addActionListener(e->startGame());
        startButton.setBounds(210,140,100,30);
        startButton.setEnabled(false);
        add(startButton);

        setVisible(true);
    }
}
