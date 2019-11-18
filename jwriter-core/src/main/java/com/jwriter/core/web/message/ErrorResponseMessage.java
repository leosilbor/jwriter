package com.jwriter.core.web.message;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ErrorResponseMessage extends Exception implements ResponseMessage {
	
	private static final long serialVersionUID = -8914405255440456048L;
	
	private String stackTrace;
	
	public ErrorResponseMessage (Throwable e) {
		super(e.getMessage());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		e.printStackTrace( new PrintStream( baos ) );
		stackTrace = new String( baos.toByteArray() );
	}
	
	public ErrorResponseMessage (String message) {
		super(message);
	}
	
	public String getSuperStackTrace () {
		return stackTrace;
	}
	

	
}
