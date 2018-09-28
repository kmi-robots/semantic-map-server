package tests;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

public class WTKtoJTSTest {

	public static void main(String[] args) throws ParseException {
		String wkt1 = "POINT (4 4)";
		String wkt2 = "POLYGON((0 0, 30 0, 30 30, 0 30, 0 0))";
		WKTReader reader = new WKTReader();
		Geometry g1 = reader.read(wkt1);
		Geometry g2 = reader.read(wkt2);
		Point p1 = (Point)g1;
		Polygon p2 = (Polygon)g2;
		
		System.out.println(p2.contains(p1));

		
	}

}
