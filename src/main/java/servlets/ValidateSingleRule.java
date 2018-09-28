package servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONObject;

import semanticmap.KBInterface;
import utils.JsonUtils;

public class ValidateSingleRule extends HttpServlet {

	private static final long serialVersionUID = -7551721416877187698L;
	public static KBInterface kbInterface;
	
	public ValidateSingleRule() {
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
		
		String ruleName = req.getParameter("rule");

		if (ruleName == null) {
			resp.setStatus(HttpStatus.BAD_REQUEST_400);
			resp.getWriter().write("Please specify the parameter 'rule'");
			resp.getWriter().flush();
			resp.getWriter().close();
		}
		else {
			// TODO check on whether the rule exists
			try {
				JSONObject violationReport = kbInterface.validateSingleRule(ruleName);
				JSONObject violationReportFormatted = JsonUtils.bindJsonLDValidationResults(violationReport);
				resp.setStatus(HttpStatus.OK_200);
				resp.getWriter().write(violationReportFormatted.toString());
				resp.getWriter().flush();
				resp.getWriter().close();
			}
			catch (DatatypeFormatException e) {
				e.printStackTrace();
				resp.setStatus(HttpStatus.BAD_REQUEST_400);
				resp.getWriter().write(ExceptionUtils.getStackTrace(e));
				resp.getWriter().flush();
				resp.getWriter().close();
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
