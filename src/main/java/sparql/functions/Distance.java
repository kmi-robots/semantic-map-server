package sparql.functions;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PropertyNotFoundException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.NodeValueDouble;
import org.apache.jena.sparql.function.FunctionBase2;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

import semanticmap.Namespaces;
import utils.KBUtils;

public class Distance extends FunctionBase2 {

	public static Model model;
	public static WKTReader wktReader;
	
	public Distance() {
		super();
	}

	@Override
	public NodeValue exec(NodeValue v1, NodeValue v2) {
		Resource v1Resource = model.getResource(v1.asString());
		Resource v2Resource = model.getResource(v2.asString());
		Geometry v1Geom = KBUtils.getGeometryFromResource(v1Resource, model, wktReader);
		Geometry v2Geom = KBUtils.getGeometryFromResource(v2Resource, model, wktReader);
		
		
		
		if(v1Geom == null || v2Geom == null) {
			throw new PropertyNotFoundException(model.getProperty(Namespaces.geosparqlPrefix + "asWKT"));
		}
		
		double distance = v1Geom.distance(v2Geom);
		System.out.println(distance);
		return new NodeValueDouble(distance);

	}

}
