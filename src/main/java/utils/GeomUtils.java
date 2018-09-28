package utils;

import org.apache.commons.math3.complex.Quaternion;
import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

public class GeomUtils {
	
	public static Point initialisePointFromROSMessage(JSONObject rosPointJson, GeometryFactory gf) {
		Coordinate coordinate = new Coordinate(rosPointJson.getDouble("x"), rosPointJson.getDouble("y"), rosPointJson.getDouble("z"));
		return gf.createPoint(coordinate);
	}
	
	// TODO check whether the conversion is correct
	public static Quaternion initialiseQuaternionFromROSMessage(JSONObject rosQuaternionJson) {
		return new Quaternion(rosQuaternionJson.getDouble("w"), rosQuaternionJson.getDouble("x"), rosQuaternionJson.getDouble("y"), rosQuaternionJson.getDouble("z"));
	}
	
	public static LineString initialiseLineFromROSMessage(JSONArray rosPointListJson, GeometryFactory gf) {
		Coordinate[] coordinates = new Coordinate[rosPointListJson.length()];
		
		for(int i = 0; i < rosPointListJson.length(); ++i) {
			JSONObject currentPoint = rosPointListJson.getJSONObject(i);
			Coordinate c = new Coordinate(currentPoint.getDouble("x"), currentPoint.getDouble("y"), currentPoint.getDouble("z"));
			coordinates[i] = c;
		}
		
		return gf.createLineString(coordinates);
	}
	
	public static Polygon initialisePolygonFromROSMessage(JSONArray rosPointListJson, GeometryFactory gf) {
		Coordinate[] coordinates = new Coordinate[rosPointListJson.length()];
		
		for(int i = 0; i < rosPointListJson.length(); ++i) {
			JSONObject currentPoint = rosPointListJson.getJSONObject(i);
			Coordinate c = new Coordinate(currentPoint.getDouble("x"), currentPoint.getDouble("y"), currentPoint.getDouble("z"));
			coordinates[i] = c;
		}
		
		return gf.createPolygon(coordinates);
	}
	
}
