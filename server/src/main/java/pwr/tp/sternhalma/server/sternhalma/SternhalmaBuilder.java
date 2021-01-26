package pwr.tp.sternhalma.server.sternhalma;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import pwr.tp.sternhalma.server.menager.Game;
import pwr.tp.sternhalma.server.menager.GameBuilder;
import pwr.tp.sternhalma.server.menager.Server;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of GameBuilder for Sternhalma
 */
public class SternhalmaBuilder implements GameBuilder {
    Sternhalma game;
    @Override
    public Game build(Server server, int id, JSONObject properties) throws JSONException {
        game = new Sternhalma(server, id);
        Board board = buildBoard(properties.getInt("board"));
        GameMaster gameMaster = buildGameMaster(board, properties.getJSONArray("rules"));
        board.setGameMaster(gameMaster);
        game.setBoard(board);
        game.setPlayerCount(properties.getInt("playerCount"));
        return game;
    }

    /**
     * Private method used to build board
     * @param type board id
     * @return generated board
     * @throws JSONException if cant create board by given id
     */
    private Board buildBoard(int type) throws JSONException {
        List<Field> fields = new ArrayList<Field>();
        List<Pone> pones = new ArrayList<Pone>();

        String filepath = "/board_"+ type +".json";
        InputStream stream = getClass().getResourceAsStream(filepath);
        if (stream == null) {
            throw new JSONException("no resource file for given board");
        }
        JSONObject boardConfig = new JSONObject(new JSONTokener(stream));
        JSONArray jFields = boardConfig.getJSONArray("fields");
        int length = jFields.length();
        for(int i=0; i<length; i++) {
            JSONObject jField = jFields.getJSONObject(i);
            Field field = new Field(jField.getInt("x"),
                    jField.getInt("y"), jField.getInt("player"));
            if(field.player!=0){
                Pone pone = new Pone(field.x, field.y, (field.player+2)%6 +1);
                pone.currentField = field;
                field.currentPone = pone;
                pones.add(pone);
            }
            fields.add(field);
        }

        JSONArray jConnections = boardConfig.getJSONArray("connections");
        length = jConnections.length();
        for(int i=0; i<length; i++) {
            JSONObject connection = jConnections.getJSONObject(i);
            Field field = fields.get(connection.getInt("field"));
            Field[] arr = new Field[6];
            for(int j=0; j<6; j++) {
                if (connection.has(Integer.toString(j))) {
                    arr[j] = fields.get(connection.getInt(Integer.toString(j)));
                }
            }
            field.adjacent=arr;
        }
        return new Board(type, pones, fields);
    }

    /**
     * Private method used to construct game master
     * @param board reference to board that game master will be attached to
     * @param rules JSONArray of rules to add
     * @return  created game master
     * @throws JSONException if wrong ruleset given
     */
    private GameMaster buildGameMaster(Board board, JSONArray rules) throws JSONException{
        GameMaster gameMaster = new GameMaster(board);
        int length = rules.length();
        for(int i=0; i<length; i++){
            JSONObject jRule = rules.getJSONObject(i);
            Rule rule = switch (jRule.getString("rule")){
                case "JumpMoveRule" -> new JumpMoveRule();
                case "BasicMoveRule" -> new BasicMoveRule();
                case "OnePerRoundRule" -> new OnePerRoundRule();
                case "LockedMoveRule" -> new LockedMoveRule();
                default -> null;
            };
            if (rule == null) throw new JSONException("No Rule Found");
            gameMaster.addRule(rule);
        }
        return gameMaster;
    }
}
