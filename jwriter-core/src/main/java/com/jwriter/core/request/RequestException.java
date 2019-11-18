package com.jwriter.core.request;

public class RequestException extends Exception {

	private static final long serialVersionUID = -2136680441399402016L;

	public RequestException(String message, Throwable cause) {
		super(message, cause);
	}

	public RequestException(String message) {
		super(message);
	}

	

}
