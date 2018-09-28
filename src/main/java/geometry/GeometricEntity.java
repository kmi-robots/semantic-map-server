package geometry;

import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

public class GeometricEntity {
	private String category;
	private String _class;
	private String id;
	private long timestamp;
	private Pose pose;
	private GeometricClasses geomType;
	private Geometry geometry;
 	
	public GeometricEntity(String category, String _class, long timestamp, GeometricClasses geomType) {
		this._class = _class;
		this.timestamp = timestamp;
		this.category = category;
		this.geomType = geomType;
	}
	
	public GeometricClasses getGeomType() {
		return geomType;
	}

	public void setGeomType(GeometricClasses geomType) {
		this.geomType = geomType;
	}

	public GeometricEntity(String category, String _class, long timestamp) {
		this._class = _class;
		this.timestamp = timestamp;
		this.category = category;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String get_class() {
		return _class;
	}

	public void set_class(String _class) {
		this._class = _class;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public Pose getPose() {
		return pose;
	}

	public void setPose(Pose pose) {
		this.pose = pose;
	}
	
	public void setGeometry(Geometry geometry) throws GeometryTypeException {
		if(geometry instanceof Point && !this.geomType.equals(GeometricClasses.POINT)) {
			throw new GeometryTypeException("Cannot set point for type " + this.geomType.toString());
		}
		if(geometry instanceof LineString && !this.geomType.equals(GeometricClasses.LINE)) {
			throw new GeometryTypeException("Cannot set line for type " + this.geomType.toString());
		}
		if(geometry instanceof Polygon && !this.geomType.equals(GeometricClasses.AREA)) {
			throw new GeometryTypeException("Cannot set line for type " + this.geomType.toString());
		}
		this.geometry = geometry;
	}

	public Geometry getGeometry() {
		return this.geometry;
	}

	// {\"category\":\"area\", \"class\":\"HeaterFreeArea\", \"id\":\"Playroom\", \"timestamp\":1535983132000, \"geometry\":{\"geom_class\":\"area\", \"points\":[{\"x\":-3.0, \"y\":-3.0, \"z\":0.0},{\"x\":3.0, \"y\":-3.0, \"z\":0.0},{\"x\":3.0, \"y\":3.0, \"z\":0.0},{\"x\":-3.0, \"y\":3.0, \"z\":0.0},{\"x\":-3.0, \"y\":-3.0, \"z\":0.0}]}}
	public JSONObject toJson() {
		
		JSONObject ret = new JSONObject();
			
		ret.put("category", this.category);
		ret.put("class", this._class);
		
		if(this.id != null && !this.id.isEmpty()) {
			ret.put("id", this.id);
		}
		
		ret.put("timestamp", this.timestamp);
		
		JSONObject geometry = new JSONObject();		
		geometry.put("geom_class", this.geomType.getType());
		
		if(this.pose != null) {
			geometry.put("pose", this.pose.toJson());
		}
		
		if(this.geomType.equals(GeometricClasses.LINE) || this.geomType.equals(GeometricClasses.AREA)) {
			JSONArray points = new JSONArray();
			
			for(Coordinate c : this.geometry.getCoordinates()) {
				JSONObject cJson = new JSONObject();
				cJson.put("x", c.x);
				cJson.put("y", c.y);
				cJson.put("z", c.z);
				points.put(cJson);
			}
			
			geometry.put("points", points);
		}
		
		ret.put("geometry", geometry);
		
		return ret;
	}
}
