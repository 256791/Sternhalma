package pwr.tp.sternhalma.server.menager;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class ServerTest {
    private final static int PORT = 8080;
    private Server server;

    @Before
    public void init() {
        server = new Server(PORT);
        server.start();
    }

    @After
    public void cleanup() {
        server.close();
    }

    @Test
    public void testPortOccupied() {
        String[] args = {String.valueOf(PORT)};
        Server.main(args);
    }

    @Test
    public void testConnect() {

        try {
            Socket client = new Socket("localhost", PORT);
            PrintWriter out = new PrintWriter(client.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            client.close();
            Thread.sleep(100);
            Socket client2 = new Socket("localhost", PORT);
            PrintWriter out2 = new PrintWriter(client2.getOutputStream());
            BufferedReader in2 = new BufferedReader(new InputStreamReader(client2.getInputStream()));

            Socket client3 = new Socket("localhost", PORT);
            PrintWriter out3 = new PrintWriter(client3.getOutputStream());
            BufferedReader in3 = new BufferedReader(new InputStreamReader(client3.getInputStream()));
        } catch (IOException e) {
            fail("Could not connect to server");
        } catch (InterruptedException e) {
            fail("Sleep interrupted");
        }
    }

    @Test
    public void testGame() {
        JSONObject properties;

        try {
            properties = new JSONObject("{" +
                    "\"game\": \"sternhalma\"," +
                    "\"default\": false," +
                    "\"playerCount\": 2," +
                    "\"board\": 0," +
                    "\"rules\":[" +
                    "{\"rule\": \"OnePerRoundRule\"}," +
                    "{\"rule\": \"BasicMoveRule\"}," +
                    "{\"rule\": \"JumpMoveRule\"}," +
                    "{\"rule\": \"LockedMoveRule\"}" +
                    "]" +
                    "}");
            server.newGame(properties);
            server.newGame(properties);
            server.newGame(properties);
        } catch (JSONException e) {
            fail("JSON problem occurred");
        }

        Game game = server.findGame(1);
        if (game == null) fail("Could not find created game");

        server.removeGame(game);

        game = server.findGame(2);
        if (game == null) fail("Could not find created game");

        game = server.findGame(1);

        assertNull("Removed game still exists", game);
    }
}