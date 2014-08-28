package net;
/**
 * HttpRequest - HTTP request container and parser
 *
 * $Id: HttpRequest.java,v 1.2 2003/11/26 18:11:53 kangasha Exp $
 *
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class HttpRequest {
	/** Help variables */
	final static String CRLF = "\r\n";
	final static int HTTP_PORT = 80;
	/** Store the request parameters */
	String method;
	String URI;
	String version;
	String headers = "";
	/** Server and port */
	private String host;
	private int port;

	/** Create HttpRequest by reading it from the client socket */
	public HttpRequest(BufferedReader from) {
		String firstLine = "";
		String[] tmp = null;
		try {
			firstLine = from.readLine();
		} catch (IOException e) {
			System.out.println("Error reading request line: " + e);
		}
		
		try {
			tmp = firstLine.split(" ");
			method = tmp[0];
			URI = tmp[1];
			version = tmp[2];
			if(URI.startsWith("http://")) {
				String[] tmpURI = URI.split("http://");
				URI = tmpURI[1].substring(tmpURI[1].indexOf("/"));
			}
			if (!(method.equals("GET") || method.equals("POST") || method.equals("HEAD"))) {
				System.out.println("Error: Method not GET, POST or HEAD");
			}
		} catch(Exception e) {
			System.out.println("Error: first line null;");
		}

		try {
			String line = from.readLine();
			while (line.length() != 0) {
				headers += line + CRLF;
				/* We need to find host header to know which server to
				 * contact in case the request URI is not complete. */
				if (line.startsWith("Host:")) {
					tmp = line.split(" ");
					if (tmp[1].indexOf(':') > 0) {
						String[] tmp2 = tmp[1].split(":");
						host = tmp2[0];
						
						port = Integer.parseInt(tmp2[1]);
					} else {
						host = tmp[1];
						
						port = HTTP_PORT;
					}
				}
				line = from.readLine();
			}
		} catch (IOException e) {
			System.out.println("Error reading from socket: " + e);
			return;
		}
//		System.out.println("Host to contact is: " + host + " at port " + port);
	}

	/** Return host for which this request is intended */
	public String getHost() {
		return host;
	}

	/** Return port for server */
	public int getPort() {
		return port;
	}

	/**
	 * Convert request into a string for easy re-sending.
	 */
	public String toString() {
		String req = "";

		req = method + " " + URI + " " + version + CRLF;
		req += headers;
		/* This proxy does not support persistent connections */
		req += "Connection: close" + CRLF;
		req += CRLF;

		return req;
	}
	
	public String getURIRequestHost() {
		return method + " " + URI + " " + version + CRLF + "Host: " + getHost();
	}
	
	public String log() {
		return "[REQUEST] URI requested is: " + URI + " method is: " + method;
	}
}