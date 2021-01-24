package pwr.tp.sternhalma.client;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Board {
    private final List<Field> fields;

    public Board(int type) throws Exception {
        fields = new ArrayList<Field>();

        String filepath = "/board_"+ type +".json";
        InputStream stream = getClass().getResourceAsStream(filepath);
        if (stream == null) {
            throw new Exception();
        }
        try {
            JSONObject boardConfig = new JSONObject(new JSONTokener(stream));
            JSONArray fieldArr = boardConfig.getJSONArray("fields");
            int length = fieldArr.length();
            for(int i=0; i<length; i++) {
                JSONObject field = fieldArr.getJSONObject(i);
                fields.add(new Field(
                        field.getInt("x"),
                        field.getInt("y"),
                        field.getInt("x_pos")-95,
                        field.getInt("y_pos")
                ));
            }
        } catch (JSONException e) {
            throw new Exception();
        }
    }

    public void update(int x, int y, int player){
        synchronized (this){
            for(Field field: fields){
                if(field.x == x && field.y == y){
                    field.player = player;
                    return;
                }
            }
        }
    }

    public Field contains(int x, int y) {
        synchronized (this) {
            for (Field field : fields) {
                if (field.contains(x, y)) {
                    return field;
                }
            }
        }
        return null;
    }

    public void draw(Graphics2D g2d){
        synchronized (this) {
            for(Field field: fields){
                field.draw(g2d);
            }
        }
    }
}
