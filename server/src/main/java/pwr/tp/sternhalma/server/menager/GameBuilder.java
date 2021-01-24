package pwr.tp.sternhalma.server.menager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Interface defining method to implement for building a game
 */
public interface GameBuilder {
    /**
     * Method that builds game using given parameters
     * @param server reference to server that owns the game
     * @param id if of game to be created
     * @param properties JSONObject defining game properties
     * @return Built game
     * @throws JSONException if properties have missing fields or wrong values
     */
    Game build(Server server, int id, JSONObject properties) throws JSONException;
}
