package com.jwriter.core.web.message;

public class SuccessResponseMessage implements ResponseMessage {

	private static final long serialVersionUID = 6888996749576936391L;
	
	private Object response;

	public SuccessResponseMessage(Object response) {
		super();
		this.response = response;
	}

	public Object getResponse() {
		return response;
	}
	
	

}
