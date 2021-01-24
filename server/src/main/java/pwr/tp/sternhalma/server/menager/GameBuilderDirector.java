package pwr.tp.sternhalma.server.menager;

import org.json.JSONException;
import org.json.JSONObject;
import pwr.tp.sternhalma.server.sternhalma.SternhalmaBuilder;

/**
 * Class used to build games with given properties.
 * It sets builder that is proper for given game
 */
public class GameBuilderDirector{
    private GameBuilder builder;

    /**
     * Method resposible for setting proper builder and building the game
     * @param server reference to server that owns the game
     * @param id if of game to be created
     * @param properties JSONObject defining game properties
     * @return Built game
     * @throws JSONException if properties have missing fields or wrong values
     */
    public Game build(Server server, int id, JSONObject properties) throws JSONException {
        changeBuilder(properties);
        if(builder == null) throw new JSONException("unexpected value");
        return builder.build(server, id, properties);
    }

    /**
     * Sets builder for given game
     * @param properties JSONObject containing game key
     * @throws JSONException if key is missing or have wrong value
     */
    private void changeBuilder(JSONObject properties) throws JSONException{
        builder = switch (properties.getString("game")){
            case "sternhalma" -> new SternhalmaBuilder();
            default -> null;
        };
    }
}