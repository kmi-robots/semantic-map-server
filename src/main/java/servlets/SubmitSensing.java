package servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import semanticmap.KBInterface;

public class SubmitSensing extends HttpServlet {

	private static final long serialVersionUID = 1834194641234738454L;
	public static KBInterface kbInterface;
	
	public SubmitSensing() {
		super();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.getWriter().println("This API is accessible only via POST method.");
		resp.setStatus(HttpStatus.NOT_IMPLEMENTED_501);
		resp.getWriter().flush();
		resp.getWriter().close();
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String sensingData = req.getParameter("sensing");

		if (sensingData == null) {
			resp.setStatus(HttpStatus.BAD_REQUEST_400);
			resp.getWriter().write("Please specify the parameter 'sensing");
			resp.getWriter().flush();
			resp.getWriter().close();
		}
		else {
			try {
				JSONObject sensing = new JSONObject(sensingData);
				JSONArray res = kbInterface.updateWithSensing(sensing);
				
				if(res.length() >= sensing.getJSONArray("detections").length()) {
					resp.setStatus(HttpStatus.CONFLICT_409);
					resp.getWriter().write(res.toString());
					resp.getWriter().flush();
					resp.getWriter().close();
				}				
				else if(res.length() > 0) {
					resp.setStatus(HttpStatus.PARTIAL_CONTENT_206);
					resp.getWriter().write(res.toString());
					resp.getWriter().flush();
					resp.getWriter().close();
				}
				else resp.setStatus(HttpStatus.CREATED_201);
			}
			catch (Exception e) {
				e.printStackTrace();
				resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
				resp.getWriter().write(ExceptionUtils.getStackTrace(e));
				resp.getWriter().flush();
				resp.getWriter().close();
			}
		}
	}
	
}
