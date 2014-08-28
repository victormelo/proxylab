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
	CacheHash cache;
	public ThreadHandler(Socket c, CacheHash cache) {
		this.client = c;
		this.cache = cache;
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

		/* Before send the request to the server, check cache */
		if(cache.existsRequest(request)) {
			try {
				response = cache.getResponse(request);
				DataOutputStream toClient = new DataOutputStream(client.getOutputStream());
				toClient.write(response.toString().getBytes());
				toClient.write(response.body);
				
				System.out.println("[FROM CACHE]" +response.log(request.URI, client.getLocalAddress()));
				
				client.close();
			}catch(IOException e) {
				System.out.println("Error writing response to client: " + e);
			}
		} else {
			try {
				/* Send request to server */
				/* Open socket and write request to socket */
				server = new Socket(request.getHost(), request.getPort());
				DataOutputStream toServer = new DataOutputStream(server.getOutputStream());
				toServer.writeBytes(request.toString());

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
				System.out.println(response.log(request.URI, client.getInetAddress()));
				
				client.close();
				server.close();

				// Add request and response on cache
				cache.add(request, response);
				System.out.println("[CACHE] Added stuff to cache, current cache size "+cache.getSizeInMB()+ " MB");
			} catch (IOException e) {
				System.out.println("Error writing response to client: " + e);
			}
		}
		System.out.println();

	}




}
