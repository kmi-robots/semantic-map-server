package utils;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.resultset.RDFOutput;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtils {
	
	public static JSONObject bindJsonLDResultSet(JSONObject resultSetJsonLD) {
		//assert resultSetJsonLD.get("@graph") instanceof JSONArray;
		
		// TODO: add some type checking on the values
		// TODO: when the returned field in the result row is a URI, it should be surrounded by <...>
		HashMap<String, ArrayList<String>> bindingsMapping = new HashMap<>();
		HashMap<String, String> singletonsMapping = new HashMap<>();
		
		ArrayList<JSONObject> solutions = new ArrayList<>();
		ArrayList<JSONObject> variables = new ArrayList<>();
		ArrayList<JSONObject> bindings = new ArrayList<>();
		
		JSONObject results = new JSONObject();
		results.put("results", new JSONArray());
		
		if(resultSetJsonLD.keySet().contains("@graph")) {
		
			JSONArray graph = (JSONArray) resultSetJsonLD.get("@graph");
			
			for(int i = 0; i < graph.length(); ++i) {
				JSONObject currentJson = graph.getJSONObject(i);
				List<String> keys = Arrays.asList(JSONObject.getNames(currentJson));
				
				if(keys.contains("solution")) {
					solutions.add(currentJson);
				}
				else if(keys.contains("binding")) {
					bindings.add(currentJson);
				}
				else if(keys.contains("variable")){
					variables.add(currentJson);
				}
			}
			
			for(JSONObject variable : variables) {

				String variableId = variable.getString("@id");
				Object value = null;
				
				// horrible, but very efficient code
				try {
					value = variable.get("value");
				}
				catch (JSONException e) {
					value = variable.get("rs:value");
				}
	
				String variableValue = "";
				
				// TODO to check with other data type more than strings
				if(value instanceof JSONObject) {
					if(((JSONObject) value).keySet().contains("@value"))
						variableValue = ((JSONObject)value).get("@value").toString();
					else if(((JSONObject) value).keySet().contains("@id")) {
						variableValue = ((JSONObject)value).get("@id").toString();
					}
				}
				else if(value instanceof String) {
					variableValue = (String)value;
				}
				
				singletonsMapping.put(variableId, variableValue);
			}
					
			for(JSONObject binding : bindings) {
				String bindingId = binding.getString("@id");
				Object bindingField = binding.get("binding");
				JSONArray bindingArray = new JSONArray();
				
				if(bindingField instanceof JSONArray) {
					bindingArray = (JSONArray)bindingField;
				}
				else if(bindingField instanceof String) {
					bindingArray.put(bindingField);
				}
				
				ArrayList<String> bindingList = new ArrayList<>();
				
				for(int j = 0; j < bindingArray.length(); ++j) {
					bindingList.add((String)bindingArray.get(j));
				}
				
				bindingsMapping.put(bindingId, bindingList);
			}
				
			for(JSONObject solution : solutions) {
				
				// all this is necessary, because whoever programmed
				// JSON-LD wasn't able to provide a single format for solution.
				// So if len(solution) == 1, the value is a String,
				// Otherwise is an array. ADVICE to JSONLD people: make it always an Array
				// same for bindings and resultVariable
				Object solutionField = solution.get("solution");
				Object variablesField = solution.get("resultVariable");
				JSONArray solutionsArray = new JSONArray();
				JSONArray variablesArray = new JSONArray();
				
				if(solutionField instanceof JSONArray) {
					solutionsArray =  (JSONArray)solutionField;
				}
				else if(solutionField instanceof String) {
					solutionsArray.put(solutionField);
				}
				
				if(variablesField instanceof JSONArray) {
					variablesArray =  (JSONArray)variablesField;
				}
				else if(variablesField instanceof String) {
					variablesArray.put(variablesField);
				}
				
				for(int j = 0; j < solutionsArray.length(); ++j) {
					
					// current solution
					String solutionId = solutionsArray.getString(j);
					ArrayList<String> solutionBindings = bindingsMapping.get(solutionId);
					JSONObject curSolution = new JSONObject();
					//assert solutionBindings.size() == variablesArray.length();
					
					for(int k = 0; k < variablesArray.length(); ++k) {
						String variableBindingId = solutionBindings.get(k);
						String variableName = variablesArray.getString(k);
						curSolution.put(variableName, singletonsMapping.get(variableBindingId));
					}
					
					results.getJSONArray("results").put(curSolution);
				}	
			}
		}

		return results;
	}
	
	public static JSONObject bindJsonLDValidationResults(JSONObject validationReportJsonLD) {
		assert validationReportJsonLD.get("@graph") instanceof JSONArray;
		JSONObject ret = new JSONObject();
		
		if(validationReportJsonLD.keySet().contains("@graph")) {
		
			JSONArray graphArray = validationReportJsonLD.getJSONArray("@graph");
			ret.put("violations", new JSONArray());
			
			for(int i = 0; i < graphArray.length(); ++i) {
				JSONObject item = graphArray.getJSONObject(i);
							
				if(item.getString("@type").equals("sh:ValidationResult")) {
					ret.getJSONArray("violations").put(item);
				}
				if(item.getString("@type").equals("sh:ValidationReport")) {
					ret.put("sh:conforms", item.getBoolean("sh:conforms"));
				}
			}
		}
		else {
			ret.put("sh:conforms", validationReportJsonLD.getBoolean("sh:conforms"));
		}
		return ret;
	}
	
	public static JSONObject resultSetToJsonLD(ResultSet resultSet) throws JSONException, UnsupportedEncodingException {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		RDFOutput a = new RDFOutput();
		Model aResSet = a.asModel(resultSet);
		RDFDataMgr.write(baos, aResSet, RDFFormat.JSONLD);
  
		return new JSONObject(baos.toString("UTF-8"));
	}
	
	public static JSONObject validationSetToJsonLD(Resource shaclValidationReport) throws JSONException, UnsupportedEncodingException {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		RDFDataMgr.write(baos, shaclValidationReport.getModel(), RDFFormat.JSONLD);
  
		return new JSONObject(baos.toString("UTF-8"));
	}

	public static JSONObject selectSingleRuleFromValidation(JSONObject validationReport, String rule) {
		JSONObject ret = new JSONObject();
		
		if(validationReport.keySet().contains("@graph")) {
			ret.put("@graph", new JSONArray());
			
			JSONArray graphArray = validationReport.getJSONArray("@graph");
			
			for (int i = 0; i < graphArray.length(); i++) {
				JSONObject curItem = graphArray.getJSONObject(i);
				
				if(curItem.keySet().contains("sourceShape")) {
					String sourceShape = curItem.getString("sourceShape");
					
					if(sourceShape.equals(rule)) {
						ret.getJSONArray("@graph").put(curItem);
					}
				}
				else if(curItem.keySet().contains("sh:conforms")) {
					ret.getJSONArray("@graph").put(curItem);
				}
			}
			
			// changing the value of sh:conforms to true if the graph array as only 1 item 
			// (which should correspond only to the report, at this point
			// because other rules might be false, but we're not interested in them
			if(ret.getJSONArray("@graph").length() == 1) {
				ret.remove("@graph");
				ret.put("sh:conforms", true);
				ret.put("@type", "sh:ValidationReport");
				ret.put("@id", "_:b0");
			}
		}
		else {
			ret = validationReport;	
		}
		
		return ret;
	}
}
