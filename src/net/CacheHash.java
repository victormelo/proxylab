package net;

import java.util.HashMap;

public class CacheHash {
	private HashMap<String, HttpResponse> cache;
	private double size=0;
	
	public CacheHash() {
		cache = new HashMap<String, HttpResponse>();
	}
	
	public boolean existsRequest(HttpRequest request) {
		return this.cache.get(request.getURIRequestHost()) != null;

	}
	
	public HttpResponse getResponse(HttpRequest request) {
		return this.cache.get(request.getURIRequestHost());

	}

	public void add(HttpRequest request, HttpResponse response) {
		this.cache.put(request.getURIRequestHost(), response);
		this.size+=response.getContentStored();
	}
	
	public String getSizeInMB() {
		return String.format("%.4f", this.size / 1000000);
	}
	
}