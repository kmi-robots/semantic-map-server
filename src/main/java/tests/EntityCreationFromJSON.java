package tests;

import org.json.JSONException;
import org.json.JSONObject;
import org.locationtech.jts.geom.GeometryFactory;

import geometry.GeometryTypeException;

public class EntityCreationFromJSON {

	public static void main(String[] args) {
		//String entityPointJsonString = "{\"category\":\"object\", \"class\":\"Heater\", \"timestamp\":1535983132000, \"geometry\":{\"geom_class\":\"point\", \"pose\":{\"position\":{\"x\":3.0, \"y\":1.0, \"z\":0.0}, \"orientation\":{\"x\":0.0, \"y\":0.0, \"z\":0.0, \"w\":1.0}}}}";
		String entityAreaJsonString = "{\"category\":\"area\", \"class\":\"Room\", \"ID\":\"Playroom\", \"timestamp\":1535983132000, \"geometry\":{\"geom_class\":\"area\", \"points\":[{\"x\":0.0, \"y\":0.0, \"z\":0.0},{\"x\":4.0, \"y\":0.0, \"z\":0.0},{\"x\":4.0, \"y\":7.0, \"z\":0.0},{\"x\":0.0, \"y\":7.0, \"z\":0.0},{\"x\":0.0, \"y\":0.0, \"z\":0.0}]}}";
	
		//JSONObject entityPointJson = new JSONObject(entityPointJsonString);
		JSONObject entityAreaJson = new JSONObject(entityAreaJsonString);
		entityAreaJson.getString("napallo");
		GeometryFactory gf = new GeometryFactory();
		try {
			createEntityPoint(entityAreaJson, gf);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (GeometryTypeException e) {
			e.printStackTrace();
		}
	}
	
	public static void createEntityPoint(JSONObject o, GeometryFactory gf) throws JSONException, GeometryTypeException {
		
		
	}

}
