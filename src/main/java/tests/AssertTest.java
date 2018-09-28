package tests;

import org.apache.commons.lang3.EnumUtils;

import geometry.GeometricClasses;

public class AssertTest {

	public static void main(String[] args) {
		String a = "howwi";
		
		assert a.equals("howwi");
		//double p = Double.parseDouble(a);
		//System.out.println(p);
		
		System.out.println(GeometricClasses.AREA.toString());
		System.out.println(GeometricClasses.AREA.name());
		System.out.println(GeometricClasses.AREA.getType());
		System.out.println(GeometricClasses.AREA.compareTo(GeometricClasses.AREA));
		
		for (GeometricClasses c : GeometricClasses.values()) {
			System.out.println(c.name());
		}
		
		System.out.println(EnumUtils.isValidEnum(GeometricClasses.class, "point".toUpperCase()));
	}

}
