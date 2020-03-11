package twitterserver.twitterserver.handler;

import java.io.IOException;
import java.io.PrintWriter;

import twitterserver.twitterserver.SparkFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class WordCountHandler extends AbstractHandler {

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_OK);
		
		PrintWriter out = response.getWriter();
		out.println(SparkFactory.getInstance().getWordCount());
		
		baseRequest.setHandled(true);
	}

}
