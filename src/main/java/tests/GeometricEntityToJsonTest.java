package tests;

import java.util.Calendar;

import org.apache.commons.math3.complex.Quaternion;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;

import geometry.GeometricClasses;
import geometry.GeometricEntity;
import geometry.GeometryTypeException;
import geometry.Pose;

public class GeometricEntityToJsonTest {

	public static void main(String[] args) throws ParseException, GeometryTypeException {
		WKTReader reader = new WKTReader();
		//Geometry g = reader.read("POLYGON((20 -5.3 0, 21.2 -5.3 0, 21.2 -3 0, 20 -3 0, 20 -5.3 0))");
		Geometry p = reader.read("POINT(20 -5.3 3)");
		
		WKTWriter writer = new WKTWriter(3);
		System.out.println(writer.write(p));
		
		GeometryFactory gf = new GeometryFactory();
		
		GeometricEntity heater = new GeometricEntity("object", "Playroom", Calendar.getInstance().getTimeInMillis(), GeometricClasses.POINT);
		Quaternion feOrientation = new Quaternion(1, 0, 0, 0);
		Point fePosition = gf.createPoint(new Coordinate(20.1, -3.8, 0.4));
		Pose fePose = new Pose(fePosition, feOrientation);
		heater.setPose(fePose);
		
		heater.setGeometry(p);
		
		System.out.println(heater.toJson().toString());
	}

}
