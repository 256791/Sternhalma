package pwr.tp.sternhalma.server.menager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class PlayerTest {
    private static final int PORT = 8080;
    private Server server;
    private PrintWriter out;
    private BufferedReader in;
    private String response;

    @Before
    public void init(){
        server = new Server(PORT);
        server.start();
        response = "";
        try{
            Socket client = new Socket("localhost", PORT);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (IOException e) {
            fail("Could not connect to server");
        }
    }

    @After
    public void cleanup(){
        server.close();
    }

    @Test(timeout = 1000)
    public void testBadJSON() {
        out.println("Not A JSON }}");

        try {
            response = in.readLine();
        } catch (IOException e) {
            fail("Could not connect to server");
        }
        assertEquals(Player.JSON_ERR, response);

        out.println("{\"type\": \"someRandomType\"}");

        try {
            response = in.readLine();
        } catch (IOException e) {
            fail("Could not connect to server");
        }
        assertEquals(Player.JSON_ERR, response);
    }

    @Test(timeout = 1000)
    public void testPing() {
        out.println(Player.PING);

        try {
            response = in.readLine();
        } catch (IOException e) {
            fail("");
        }
        assertEquals(Player.PING, response);
    }

    @Test(timeout = 1000)
    public void testCreateJoin(){
        out.println("{\"type\": \"create\", \"properties\": " +
                "{\"game\": \"sternhalma\", \"players\": 2}, \"board\": \"default\"}}");

        try { response = in.readLine(); } catch (IOException e) {
            fail("Could not connect to server");
        }
        if(response.equals(Player.GAME_CON_ERR)) fail("Cant create game");
        if(response.equals(Player.JSON_ERR)) fail("Cant create game");

        try{
            Socket client2 = new Socket("localhost", PORT);
            PrintWriter out2 = new PrintWriter(client2.getOutputStream(), true);
            BufferedReader in2 = new BufferedReader(new InputStreamReader(client2.getInputStream()));
            out2.println("{\"type\": \"join\", \"id\": \"1\"}");
            response = in2.readLine();
        } catch (IOException e) {
            fail("Could not connect to server");
        }

        if(response.equals(Player.GAME_CON_ERR)) fail("Cant create game");
        if(response.equals(Player.JSON_ERR)) fail("Cant create game");

        out.println("{\"type\": \"join\", \"id\": \"22\"}");

        try { response = in.readLine(); } catch (IOException e) {
            fail("Could not connect to server");
        }

        assertEquals(response, Player.GAME_CON_ERR);
    }

    @Test(timeout = 1000)
    public void testAction(){
        out.println("{\"type\": \"action\"}");

        try {
            response = in.readLine();
        } catch (IOException e) {
            fail("Could not connect to server");
        }

        assertEquals(response, Player.JSON_ERR);
    }

    @Test(timeout = 1000)
    public void testOption(){
        out.println("{\"type\": \"option\"}");

        try {
            response = in.readLine();
        } catch (IOException e) {
            fail("Could not connect to server");
        }

        assertEquals(response, Player.JSON_ERR);
    }
}