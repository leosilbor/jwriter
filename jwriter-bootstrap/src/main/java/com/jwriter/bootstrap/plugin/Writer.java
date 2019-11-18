package com.jwriter.bootstrap.plugin;

import java.util.List;
import java.util.Map;



public abstract class Writer {
	private String pathSoffice;
	
	public Writer (String pathSoffice) {
		this.pathSoffice = pathSoffice;
	}
	
	public String getPathSoffice () {
		return pathSoffice;
	}
	
	public abstract void start (Map<String, List<String[]>> argsMap) throws PluginException;
	
	public abstract void stop ();
	
}
