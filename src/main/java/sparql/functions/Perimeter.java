package sparql.functions;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PropertyNotFoundException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.NodeValueDouble;
import org.apache.jena.sparql.function.FunctionBase1;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

import semanticmap.Namespaces;
import utils.KBUtils;

public class Perimeter extends FunctionBase1 {

	public static Model model;
	public static WKTReader wktReader;
	
	public Perimeter() {
		super();
	}

	@Override
	public NodeValue exec(NodeValue v) {
		Resource vResource = model.getResource(v.asString());	
		Geometry vGeom = KBUtils.getGeometryFromResource(vResource, model, wktReader);
		
		if(vGeom == null) {
			throw new PropertyNotFoundException(model.getProperty(Namespaces.geosparqlPrefix + "asWKT"));
		}
		
		double perimeter = vGeom.getLength();
		return new NodeValueDouble(perimeter);
	}

}
