package tests;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

public class JTSPolygonCreationTest {

	public static void main(String[] args) {
		GeometryFactory gf = new GeometryFactory();
		Coordinate c1 = new Coordinate(0, 0);
		Coordinate c2 = new Coordinate(10, 0);
		Coordinate c3 = new Coordinate(10, 10);
		Coordinate c4 = new Coordinate(0, 10);
		Coordinate c5 = new Coordinate(0, 0);		
		Coordinate[] coordinates = {c1,c2,c3,c4,c5};		
		Polygon pol1 = gf.createPolygon(coordinates);
		
		//Coordinate c6 = new Coordinate(3,3);
		//Point point = gf.createPoint(c6);
			
		Coordinate c12 = new Coordinate(-1, -1);
		Coordinate c22 = new Coordinate(8, 1);
		Coordinate c32 = new Coordinate(8, 8);
		Coordinate c42 = new Coordinate(1, 8);
		Coordinate c52 = new Coordinate(-1, -1);
		Coordinate[] coordinates2 = {c12,c22,c32,c42,c52};
		Polygon pol2 = gf.createPolygon(coordinates2);
		
		System.out.println("Polygon 1 contains Polygon 2: " + pol1.contains(pol2));
		System.out.println("Polygon 2 contains Polygon 1: " + pol2.contains(pol1));
		System.out.println("Polygon 1 intersect Polygon 2: " + pol1.intersects(pol2));
		System.out.println("Polygon 1 contains Polygon 2: " + pol1.contains(pol2));
		
		Coordinate c13 = new Coordinate(1, 1);
		Coordinate c23 = new Coordinate(7, 1);
		Coordinate c33 = new Coordinate(7, 6);
		Coordinate c43 = new Coordinate(1, 7);
		Coordinate c53 = new Coordinate(1, 1);
		Coordinate[] coordinates3 = {c13,c23,c33,c43,c53};
		Polygon pol3 = gf.createPolygon(coordinates3);
		
		System.out.println("----");
		System.out.println("Polygon 2 contains Polygon 3: " + pol2.contains(pol3));
		System.out.println("Polygon 3 contains Polygon 2: " + pol3.contains(pol2));
		System.out.println("Polygon 2 intersect Polygon 3: " + pol2.intersects(pol3));
		
		
	}

}
