package geometry;

public enum GeometricClasses {
	POINT("point"), LINE("line"), AREA("area"), VOLUME("volume");
	
	//public final static List<String> classes = new ArrayList<>(Arrays.asList("point", "area", "line", "volume"));
	
	private String type;
	
	GeometricClasses(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
	
}
