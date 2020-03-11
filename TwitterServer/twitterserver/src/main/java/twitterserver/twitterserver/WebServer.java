package twitterserver.twitterserver;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import twitterserver.twitterserver.handler.*;
import twitterserver.twitterserver.Config;

public class WebServer {
	
	public Server createServer() {
		Server server = new Server();
		
		ServerConnector http = new ServerConnector(server);
		http.setHost(Config.host);
		http.setPort(Config.port);
		http.setIdleTimeout(Config.timeout);		
		server.addConnector(http);
		server.setHandler(new WordCountHandler());
		
		return server;
	}
}
