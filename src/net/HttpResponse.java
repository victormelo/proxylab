package net;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;

/**
 * HttpResponse - Handle HTTP replies
 *
 * $Id: HttpResponse.java,v 1.2 2003/11/26 18:12:42 kangasha Exp $
 *
 */

public class HttpResponse {
	final static String CRLF = "\r\n";
	/** How big is the buffer used for reading the object */
	final static int BUF_SIZE = 10000;
	/** Maximum size of objects that this proxy can handle. For the
	 * moment set to 100 KB. You can adjust this as needed. */
	final static int MAX_OBJECT_SIZE = 100000;
	final static String STATUS_NOT_FOUND = "HTTP/1.1 404 Not Found";
	final static String STATUS_OK = "HTTP/1.1 200 OK";
	/** Reply status and headers */
	String version;
	int status;
	String statusLine = "";
	String lastModified ="";
	String etag ="";
	String headers = "";
	boolean notFound= false;
	/* Body of reply */
	byte[] body;

	/** Read response from server. */
	public HttpResponse(DataInputStream fromServer) {
		/* Length of the object */
		int length = -1;
		boolean gotStatusLine = false;

		/* First read status line and response headers */
		try {
			String line = fromServer.readLine();
			boolean ler = true;
			while (ler) {
				if (!gotStatusLine) {
					statusLine = line;
					if(statusLine.equals(STATUS_NOT_FOUND)) {
						statusLine = STATUS_OK;
						notFound = true;
						break;
					}
					System.out.println(statusLine);
					gotStatusLine = true;
				} else {
					headers += line + CRLF;
				}

				/* Get length of content as indicated by
				 * Content-Length header. Unfortunately this is not
				 * present in every response. Some servers return the
				 * header "Content-Length", others return
				 * "Content-length". You need to check for both
				 * here. */
				if (line.startsWith("Content-Length") ||
						line.startsWith("Content-length")) {
					String[] tmp = line.split(" ");
					length = Integer.parseInt(tmp[1]);
				}
				
				if (line.startsWith("Last-Modified:")) {

					lastModified = line.split("Last-Modified: ")[1];
				}
				
				if(line.startsWith("ETag:")) {
					etag = line.split("ETag: ")[1];
				}
				
				line = fromServer.readLine();
				
				if(line.length() == 0 ) {
					ler = false;
				}
			}
		} catch (IOException e) {
			System.out.println("Error reading headers from server: " + e);
			return;
		}
		try {
			int bytesRead = 0;
			
			// If not defined Content Length, set the minimum size of body to be 100 KB
			body = new byte[length > 0 ? length : MAX_OBJECT_SIZE];
			byte buf[] = new byte[BUF_SIZE];
			boolean loop = false;

			/* If we didn't get Content-Length header, just loop until
			 * the connection is closed. */
			if (length == -1) {
				loop = true;
			}

			/* Read the body in chunks of BUF_SIZE and copy the chunk
			 * into body. Usually replies come back in smaller chunks
			 * than BUF_SIZE. The while-loop ends when either we have
			 * read Content-Length bytes or when the connection is
			 * closed (when there is no Connection-Length in the
			 * response. */
			while (bytesRead < length || loop) {
				int res = fromServer.read(buf);
				if (res == -1) {
					break;
				}
				/* Copy the bytes into body. Make sure we don't exceed
				 * the maximum object size. */
				for (int i = 0; 
						i < res && (i + bytesRead) < body.length; 
						i++) {
					body[i + bytesRead] = buf[i];
//					System.out.print((char)body[i]);
					
					
				}
				bytesRead += res;
			}

		} catch (Exception e) {
			System.out.println("Error reading response body: " + e);
			e.printStackTrace();
			return;
		}


	}

	/**
	 * Convert response into a string for easy re-sending. Only
	 * converts the response headers, body is not converted to a
	 * string.
	 */
	public String toString() {
		String res = "";

		res = statusLine + CRLF;
		res += headers;
		res += CRLF;

		return res;
	}
	
	public double getContentStored() {
		return (float)this.body.length;
	}
	
	public String log(String uri, InetAddress inetAddress) {
		return String.format("[RESPONSE] to client %s URI %s Last modified in: %s ETag: %s Content stored: %.2f KB", inetAddress.getHostAddress(), uri, lastModified == ""? "not determined" :  lastModified,etag == ""? "not determined" : etag, getContentStored()/1000);

	}
	
}