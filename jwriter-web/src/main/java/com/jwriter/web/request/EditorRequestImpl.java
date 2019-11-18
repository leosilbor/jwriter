package com.jwriter.web.request;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.jwriter.bootstrap.util.IOUtil;
import com.jwriter.core.request.RequestException;
import com.jwriter.editor.request.EditorRequest;

public class EditorRequestImpl extends EditorRequest {
	private static final long serialVersionUID = -5187965386399543345L;
	
	private File document;

	public EditorRequestImpl(String docType, String docName, File document) {
		super(docType, docName);
		this.document = document;
	}

	public void saveDocument(byte[] document) throws RequestException {
		try {
			FileOutputStream fos = new FileOutputStream(this.document);
			fos.write( document );
			fos.close();
		} catch ( IOException e ) {
			throw new RequestException("Error while saving document data", e);
		}
		
	}

	public String getID() {
		return toString();
	}

	public byte[] getDocument() throws RequestException {
		try {
			return IOUtil.toByteArray(new FileInputStream(document));
		} catch (IOException e) {
			e.printStackTrace();
			throw new RequestException("Error while trying to read document", e);
		}
	}

	public List<String> getDependencies() {
		return null;
	}

}
