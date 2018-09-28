package servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jetty.http.HttpStatus;

import semanticmap.KBInterface;

public class DeleteEntity extends HttpServlet {
	
	private static final long serialVersionUID = 8558027686207176195L;
	public static KBInterface kbInterface;
	
	public DeleteEntity() {
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
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException ,IOException {
		
		String entityURI = req.getParameter("entity_uri");
		
		if (entityURI == null) {
			resp.setStatus(HttpStatus.BAD_REQUEST_400);
			resp.getWriter().write("Please specify the parameter 'entity_uri' (must be a URI)");
			resp.getWriter().flush();
			resp.getWriter().close();
		}
		else {
			try {
				kbInterface.deleteEntity(entityURI);
				resp.setStatus(HttpStatus.OK_200);
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
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.getWriter().println("This API is accessible only via DELETE method.");
		resp.setStatus(HttpStatus.NOT_IMPLEMENTED_501);
		resp.getWriter().flush();
		resp.getWriter().close();
	}

}
