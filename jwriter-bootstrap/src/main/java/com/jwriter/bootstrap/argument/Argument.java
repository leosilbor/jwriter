package com.jwriter.bootstrap.argument;

import java.io.Serializable;

public class Argument implements Serializable {
	private static final long serialVersionUID = 1606512750360354339L;
	
	private String key;
	private String[] values;
	
	public Argument(String key, String[] values) {
		super();
		this.key = key;
		this.values = values;
	}
	
	public String getKey() {
		return key;
	}
	public String[] getValues() {
		return values;
	}
}
