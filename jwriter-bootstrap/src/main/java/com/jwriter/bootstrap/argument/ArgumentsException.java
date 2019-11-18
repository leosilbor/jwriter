package com.jwriter.bootstrap.argument;

public class ArgumentsException extends Exception {
	
	private static final long serialVersionUID = 7371322422498526622L;

	public ArgumentsException(String message, Throwable cause) {
		super(message, cause);
	}

	public ArgumentsException(String message) {
		super(message);
	}

}
