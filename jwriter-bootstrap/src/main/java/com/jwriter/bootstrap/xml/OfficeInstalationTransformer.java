package com.jwriter.bootstrap.xml;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public abstract class OfficeInstalationTransformer {
	private Map<String, List<String[]>> argsMap;
	
	public OfficeInstalationTransformer (Map<String, List<String[]>> argsMap) {
		this.argsMap = argsMap;
	}
	
	public abstract String getFileName ();
	public abstract boolean transform (InputStream is, OutputStream os) throws TransformerException;
	
	public String getArgument(String key) throws TransformerException {
		List<String[]> values = argsMap.get(key);
		if ( values==null ) {
			throw new TransformerException("Argument not found for key '"+key+"'");
		} else if ( values.size()>1 || values.get(0).length==0 || values.get(0).length>1 ) {
			throw new TransformerException("Invalid number of values ("+values.size()+") for argument of key '"+key+"'");
		} else {
			return values.get(0)[0];
		}
	}
}
