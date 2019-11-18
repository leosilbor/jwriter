package com.jwriter.bootstrap.dependency;

import java.io.Serializable;

public class Jar implements Serializable {
	private static final long serialVersionUID = 3498523705249963352L;
	
	private String name;
	private String hash;
	
	public String getName() {
		return name;
	}
	public String getHash() {
		return hash;
	}
	public Jar(String name, String hash) {
		super();
		this.name = name;
		this.hash = hash;
	}
	
	
}
