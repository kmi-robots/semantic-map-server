package geometry;

import org.apache.commons.math3.complex.Quaternion;
import org.json.JSONObject;
import org.locationtech.jts.geom.Point;

public class Pose {
	// Vertex is used here because it allow access to the Z coordinate,
	// which is fundamental for quaternions
	// TODO when a point is loaded from a Coordinate, it can have z set
	private Point position;
	private Quaternion orientation;
	
	public Pose(Point position, Quaternion quaternion) {
		this.position = position;
		this.orientation = quaternion;
	}

	public Point getPosition() {
		return position;
	}

	public Quaternion getOrientation() {
		return orientation;
	}

	public JSONObject toJson() {
		JSONObject ret = new JSONObject();
		
		JSONObject position = new JSONObject();
		position.put("x", this.position.getCoordinate().x);
		position.put("y", this.position.getCoordinate().y);
		position.put("z", this.position.getCoordinate().z);
		
		JSONObject orientation = new JSONObject();
		orientation.put("x", this.orientation.getQ1());
		orientation.put("y", this.orientation.getQ2());
		orientation.put("z", this.orientation.getQ3());
		orientation.put("w", this.orientation.getQ0());
		
		ret.put("position", position);
		ret.put("orientation", orientation);
		
		return ret;
	}
	
}
