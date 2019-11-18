package com.jwriter.core.web.message;


public class RequestMessage implements Message {
	private static final long serialVersionUID = -4768662995197195179L;
	private String requestID;
	private String methodName;
	private Object[] arguments;
	
	public RequestMessage(String requestID, String methodName,
			Object[] arguments) {
		super();
		this.requestID = requestID;
		this.methodName = methodName;
		this.arguments = arguments;
	}
	
	public String getRequestID() {
		return requestID;
	}
	public String getMethodName() {
		return methodName;
	}
	public Object[] getArguments() {
		return arguments;
	}
}
