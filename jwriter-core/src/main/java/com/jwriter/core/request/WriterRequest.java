package com.jwriter.core.request;

import java.io.Serializable;
import java.util.List;

public abstract class WriterRequest implements Serializable {
	
	private static final long serialVersionUID = -1858855558929947974L;

	public static final String KEY_REQUEST_JNLP = "keyRequestJnlp";
	
	private String docType;
	private String docName;
	
	public WriterRequest (String docType, String docName) {
		this.docType = docType;
		this.docName = docName;
	}
	public String getDocType () {
		return this.docType;
	}
	public String getDocName () {
		return this.docName;
	}
	
	public abstract String getClassPlugin() ;
	public abstract List<String> getDependencies() ;
	public abstract List<String> getArguments();
	public abstract String getID ();
	public abstract List<String> getTransformers() ;
	
	
	
}
