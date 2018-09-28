package tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;



public class testJsonBinding {

	public static void main(String[] args) {
		
		
		HashMap<String, ArrayList<String>> bindingsMapping = new HashMap<>();
		HashMap<String, String> singletonsMapping = new HashMap<>();
		
		ArrayList<JSONObject> solutions = new ArrayList<>();
		ArrayList<JSONObject> variables = new ArrayList<>();
		ArrayList<JSONObject> bindings = new ArrayList<>();
		
		String json = "{\"@graph\":[{\"@id\":\"_:b0\",\"value\":\"Rule checking whether there are electric heaters in electric heaters free area\",\"variable\":\"message\"},{\"@id\":\"_:b1\",\"value\":{\"@id\":\"http://data.open.ac.uk/kmi/hans/fireExtinguisher\"},\"variable\":\"class\"},{\"@id\":\"_:b10\",\"binding\":[\"_:b0\",\"_:b11\",\"_:b12\"]},{\"@id\":\"_:b11\",\"value\":{\"@id\":\"http://data.open.ac.uk/hans/shacl#heaterFreeAreaRule\"},\"variable\":\"rule\"},{\"@id\":\"_:b12\",\"value\":{\"@id\":\"http://data.open.ac.uk/kmi/hans/HeaterFreeArea\"},\"variable\":\"class\"},{\"@id\":\"_:b2\",\"binding\":[\"_:b3\",\"_:b4\",\"_:b1\"]},{\"@id\":\"_:b3\",\"value\":\"Rule checking whether fire extinguishers are properly labelled\",\"variable\":\"message\"},{\"@id\":\"_:b4\",\"value\":{\"@id\":\"http://data.open.ac.uk/hans/shacl#fireExtinguisherLabelRule\"},\"variable\":\"rule\"},{\"@id\":\"_:b5\",\"binding\":[\"_:b6\",\"_:b7\",\"_:b8\"]},{\"@id\":\"_:b6\",\"value\":\"Rule checking whether fire exit points are provided with fire action notices\",\"variable\":\"message\"},{\"@id\":\"_:b7\",\"value\":{\"@id\":\"http://data.open.ac.uk/hans/shacl#fireActionNoticeRule\"},\"variable\":\"rule\"},{\"@id\":\"_:b8\",\"value\":{\"@id\":\"http://data.open.ac.uk/kmi/hans/FireExitPoint\"},\"variable\":\"class\"},{\"@id\":\"_:b9\",\"@type\":\"rs:ResultSet\",\"resultVariable\":[\"message\",\"rule\",\"class\"],\"size\":\"3\",\"solution\":[\"_:b10\",\"_:b5\",\"_:b2\"]}],\"@context\":{\"value\":{\"@id\":\"http://www.w3.org/2001/sw/DataAccess/tests/result-set#value\"},\"variable\":{\"@id\":\"http://www.w3.org/2001/sw/DataAccess/tests/result-set#variable\"},\"binding\":{\"@id\":\"http://www.w3.org/2001/sw/DataAccess/tests/result-set#binding\",\"@type\":\"@id\"},\"size\":{\"@id\":\"http://www.w3.org/2001/sw/DataAccess/tests/result-set#size\",\"@type\":\"http://www.w3.org/2001/XMLSchema#int\"},\"solution\":{\"@id\":\"http://www.w3.org/2001/sw/DataAccess/tests/result-set#solution\",\"@type\":\"@id\"},\"resultVariable\":{\"@id\":\"http://www.w3.org/2001/sw/DataAccess/tests/result-set#resultVariable\"},\"rs\":\"http://www.w3.org/2001/sw/DataAccess/tests/result-set#\",\"rdf\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\",\"xsd\":\"http://www.w3.org/2001/XMLSchema#\"}}";
		JSONObject o =  new JSONObject(json);
		
		System.out.println(json);
		
		assert o.get("@graph") instanceof JSONArray;
		
		JSONArray graph = (JSONArray) o.get("@graph");
		
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
			Object value = variable.get("value");
			String variableValue = "";
			
			if(value instanceof JSONObject) {
				variableValue = variable.getJSONObject("value").get("@id").toString();
			}
			else if(value instanceof String) {
				variableValue = (String)value;
			}
			
			singletonsMapping.put(variableId, variableValue);
		}
		
		for(JSONObject binding : bindings) {
			String bindingId = binding.getString("@id");
			JSONArray bindingArray = binding.getJSONArray("binding");
			ArrayList<String> bindingList = new ArrayList<>();
			
			for(int j = 0; j < bindingArray.length(); ++j) {
				bindingList.add((String)bindingArray.get(j));
			}
			
			bindingsMapping.put(bindingId, bindingList);
		}
		
		for(JSONObject binding : bindings) {
			String bindingId = binding.getString("@id");
			JSONArray bindingArray = binding.getJSONArray("binding");
			ArrayList<String> bindingList = new ArrayList<>();
			
			for(int j = 0; j < bindingArray.length(); ++j) {
				bindingList.add((String)bindingArray.get(j));
			}
			
			bindingsMapping.put(bindingId, bindingList);
		}
		
		JSONObject results = new JSONObject();
		results.put("results", new JSONArray());
		
		for(JSONObject solution : solutions) {
			JSONArray solutionsArray = solution.getJSONArray("solution");
			JSONArray variablesArray = solution.getJSONArray("resultVariable");
			
			for(int j = 0; j < solutionsArray.length(); ++j) {
				
				// current solution
				String solutionId = solutionsArray.getString(j);
				ArrayList<String> solutionBindings = bindingsMapping.get(solutionId);
				JSONObject curSolution = new JSONObject();
				assert solutionBindings.size() == variablesArray.length();
				
				for(int k = 0; k < variablesArray.length(); ++k) {
					String variableBindingId = solutionBindings.get(k);
					String variableName = variablesArray.getString(k);
					curSolution.put(variableName, singletonsMapping.get(variableBindingId));
				}
				
				results.getJSONArray("results").put(curSolution);
			}	
		}
		
		System.out.println(results);
		
		for(int i = 0; i < results.getJSONArray("results").length(); ++i) {
			System.out.println(results.getJSONArray("results").get(i));
		}
		
	}

}
