package com.jwriter.editor.request;

import java.util.List;

import com.jwriter.core.request.RequestException;
import com.jwriter.core.request.WriterRequest;
import com.jwriter.editor.plugin.EditorWriter;

public abstract class EditorRequest extends WriterRequest {
	
	private static final long serialVersionUID = 4357635824216679414L;

	public EditorRequest(String docType, String docName) {
		super(docType, docName);
	}

	public String getClassPlugin() {
		return EditorWriter.class.getCanonicalName();
	}

	public List<String> getArguments () {
		return null;
	}
	
	public List<String> getTransformers() {
		return null;
	}
	
	public abstract void saveDocument (byte[] document) throws RequestException;
	
	public abstract byte[] getDocument () throws RequestException;
	
}
