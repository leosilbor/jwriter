package com.jwriter.core.model;

public enum DocumentType {
	RTF("rtf", "Rich Text Format"), ODT("odt", "writer8");
	
	private String extension;
	private String filter;
	
	private DocumentType(String extension, String filter) {
		this.extension = extension;
		this.filter = filter;
	}

	public String getExtension() {
		return extension;
	}

	public String getFilter() {
		return filter;
	}
	
	public static DocumentType valueOfExtension (String extension) {
		DocumentType docType = null;
		for ( DocumentType dt: values() ) {
			if ( dt.getExtension().equalsIgnoreCase( extension ) ) {
				docType = dt;
				break;
			}
		}
		return docType;
	}
}
