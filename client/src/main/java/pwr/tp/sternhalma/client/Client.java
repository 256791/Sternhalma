package pwr.tp.sternhalma.client;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client extends Thread{
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private volatile boolean running;

    private Sternhalma game;
    private ClientWindow lobby;


    public static void main(String[] args) {
        Client c = new Client();
    }

    public Client(){
        this.lobby = new ClientWindow(this);
        this.socket = null;
    }

    public void run(){
        running = true;
        String buffer;
        while(running) {
            try {
                buffer = in.readLine();
                if (buffer == null) throw new IOException();
                handleRequest(buffer);
            } catch (IOException e) {
                if(game != null) game.dispose();
                if(lobby != null) lobby.dispose();
                running = false;
            }
        }
    }

    public void send(JSONObject data){
        String msg = data.toString();
        msg = msg.replace('\n', ' ');
        out.println(msg);
    }

    public void send(String data){
        out.println(data);
    }

    public boolean connect(int port){
        if(running) return true;
        try {
            socket = new Socket("localhost",port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            socket = null;
            return false;
        }

        start();
        return true;
    }

    public void leave(){
        game.dispose();
        game = null;
        lobby = new ClientWindow(this);
    }

    private void handleRequest(String data) {
        System.out.println(data);
        try {
            JSONObject json = new JSONObject(data);
            switch (json.getString("type")) {
                case "boardData" -> {
                    if (game != null) {
                        game.updateBoard(json.getJSONArray("board"));
                    }
                }
                case "gameData" -> {
                    if(lobby != null) lobby.gameInfo(json);
                }
                case "turn" -> {
                    if (game != null) game.turn();
                }
                case "start" ->{
                    if (game == null) {
                        if (lobby != null) {
                            lobby.dispose();
                            lobby = null;
                            game = new Sternhalma(
                                    this,
                                    json.getInt("board"),
                                    json.getInt("player")
                            );
                        }
                    }
                }
                case "kick" ->{
                    if (game != null) game.endGame(json.getString("reason"));
                }
                case "winner"->{
                    if(game != null) game.endGame("Player "+json.getString("id")+" won");
                }
                case "accept" ->{
                    if (game != null) game.accept();
                }
                case "reject" ->{
                    if(lobby != null) lobby.reject(json.getString("reason"));
                    if(game != null) game.reject();
                }
                case "error" -> {
                    if(game != null) game.dispose();
                    if(lobby != null) lobby.dispose();
                    running = false;
                }
                case "ping" -> {
                    //ignore
                }
                default -> {
                    if(lobby != null) lobby.dispose();
                    if(game != null) game.dispose();
                    running = false;
                }
            }

        } catch (JSONException e) {
            if(game != null) game.dispose();
            if(lobby != null) lobby.dispose();
            running = false;
        }
    }
}
