package sparql.functions;

import org.apache.jena.query.QueryException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PropertyNotFoundException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.NodeValueBoolean;
import org.apache.jena.sparql.function.FunctionBase3;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

import semanticmap.Namespaces;
import utils.KBUtils;

public class IsWithinDistance extends FunctionBase3 {

	public static Model model;
	public static WKTReader wktReader;
	
	public IsWithinDistance() {
		super();
	}

	@Override
	public NodeValue exec(NodeValue v1, NodeValue v2, NodeValue v3) {
		Resource v1Resource = model.getResource(v1.asString());
		Resource v2Resource = model.getResource(v2.asString());
		Resource v3Resource = model.getResource(v3.asString());
					
		Geometry v1Geom = KBUtils.getGeometryFromResource(v1Resource, model, wktReader);
		Geometry v2Geom = KBUtils.getGeometryFromResource(v2Resource, model, wktReader);
		
		if(v1Geom == null || v2Geom == null) {
			throw new PropertyNotFoundException(model.getProperty(Namespaces.geosparqlPrefix + "asWKT"));
		}
		
		try {
			double distance = Double.parseDouble(v3Resource.toString());
			if(v1Geom.isWithinDistance(v2Geom, distance)) {
				return new NodeValueBoolean(true);
			}
			else {
				return new NodeValueBoolean(false);
			}
		}
		catch (NumberFormatException e) {
			throw new QueryException("Third argument of isWithingDistance has the wrong format");
		}			
	}

}
