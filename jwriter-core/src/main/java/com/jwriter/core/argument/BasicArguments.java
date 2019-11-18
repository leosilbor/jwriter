package com.jwriter.core.argument;

import com.jwriter.bootstrap.argument.Arguments;
import com.jwriter.bootstrap.argument.ArgumentsException;
import com.jwriter.core.model.DocumentType;

public class BasicArguments implements Arguments {

	private static final long serialVersionUID = 6033628763322587062L;
	
	private String writerRequestServletURL;
	private String sessionID;
	private String writerRequestID;
	private DocumentType docType;
	private String docName;
	
	public String getWriterRequestServletURL() {
		return writerRequestServletURL;
	}
	public String getSessionID() {
		return sessionID;
	}
	public String getWriterRequestID() {
		return writerRequestID;
	}
	public DocumentType getDocType() {
		return docType;
	}
	public String getDocName () {
		return docName;
	}
	
	public void setWriterRequestServletURL(String writerRequestServletURL) {
		this.writerRequestServletURL = writerRequestServletURL;
	}
	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}
	public void setWriterRequestID(String writerRequestID) {
		this.writerRequestID = writerRequestID;
	}
	public void setDocType(String docType) throws ArgumentsException {
		this.docType = DocumentType.valueOfExtension( docType );
		if ( this.docType==null ) {
			throw new ArgumentsException("Invalid docType: "+docType);
		}
	}
	public void setDocName(String docName) {
		this.docName = docName;
	}
	
	public void validate() throws ArgumentsException {
		if ( getWriterRequestServletURL()==null ) {
			throw new ArgumentsException("Argument 'writerRequestServletURL' not found");
		}
		
		if ( getSessionID()==null ) {
			throw new ArgumentsException("Argument 'sessionID' not found");
		}
		
		if ( getWriterRequestID()==null ) {
			throw new ArgumentsException("Argument 'writerRequestID' not found");
		}
		
		if ( getDocType()==null ) {
			throw new ArgumentsException("Argument 'docType' not found");
		}
		
		if ( getDocName()==null ) {
			throw new ArgumentsException("Argument 'docName' not found");
		}
		
	}
	
	
	
	
	
	

}
