package net;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class ThreadHandler extends Thread {

	Socket client = null;

	public ThreadHandler(Socket c) {
		this.client = c;

	}
	public void run() {
		Socket server = null;
		HttpRequest request = null;
		HttpResponse response = null;

		/* Process request. If there are any exceptions, then simply
		 * return and end this request. This unfortunately means the
		 * client will hang for a while, until it timeouts. */

		/* Read request */
		try {
			BufferedReader fromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
			request = new HttpRequest(fromClient);
			System.out.println(request.log());

		} catch (IOException e) {
			System.out.println("Error reading request from client: " + e);
			return;
		}
		/* Send request to server */
		try {
			/* Open socket and write request to socket */
			server = new Socket(request.getHost(), request.getPort());
			DataOutputStream toServer = new DataOutputStream(server.getOutputStream());
			toServer.writeBytes(request.toString());
			/* Fill in */
		} catch (UnknownHostException e) {
			System.out.println("Unknown host: " + request.getHost());
			System.out.println(e);
			return;
		} catch (IOException e) {
			System.out.println("Error writing request to server: " + e);
			return;
		}
		/* Read response and forward it to client */
		try {
			DataInputStream fromServer = new DataInputStream(server.getInputStream());
			response = new HttpResponse(fromServer);
			DataOutputStream toClient = new DataOutputStream(client.getOutputStream());
			toClient.write(response.toString().getBytes());
			toClient.write(response.body);
			/* Write response to client. First headers, then body */
			System.out.println(response.log(request.URI));
			
			client.close();
			server.close();
			/* Insert object into the cache */
			/* Fill in (optional exercise only) */
		} catch (IOException e) {
			System.out.println("Error writing response to client: " + e);
		}
	}


}
