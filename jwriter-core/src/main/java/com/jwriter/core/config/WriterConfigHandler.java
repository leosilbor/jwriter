package com.jwriter.core.config;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class WriterConfigHandler extends DefaultHandler {
	private WriterConfig config;
	
	public void startDocument() throws SAXException {
		this.config = new WriterConfig();
	}
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if ( qName.equals("initial-configuration") ) {
			String sJarsPath = attributes.getValue("jarsPath");
			if ( sJarsPath==null ) {
				throw new SAXException("Error while parsing config xml. The atribute 'jarsPath' must be especified");
			} else if ( sJarsPath.startsWith("/") ) {
				sJarsPath = sJarsPath.substring(1);
			}
			
			String bootstrapJar = attributes.getValue("bootstrapJar");
			if ( bootstrapJar==null ) {
				throw new SAXException("Error while parsing config xml. The atribute 'bootstrapJar' must be especified");
			}
			
			String officeBeanJar = attributes.getValue("officeBeanJar");
			if ( officeBeanJar==null ) {
				throw new SAXException("Error while parsing config xml. The atribute 'officeBeanJar' must be especified");
			}
			
			String language = attributes.getValue("language");
			if ( language==null ) {
				throw new SAXException("Error while parsing config xml. The atribute 'language' must be especified");
			}
			
			String logPattern = attributes.getValue("logPattern");
			if ( logPattern==null ) {
				throw new SAXException("Error while parsing config xml. The atribute 'logPattern' must be especified");
			}
			
			String title = attributes.getValue("title");
			if ( title==null ) {
				throw new SAXException("Error while parsing config xml. The atribute 'title' must be especified");
			}
			
			String writerRequestServletURLPattern = attributes.getValue("writerRequestServletURLPattern");
			if ( writerRequestServletURLPattern==null ) {
				throw new SAXException("Error while parsing config xml. The atribute 'writerRequestServletURLPattern' must be especified");
			}
			
			String sWriterPort = attributes.getValue("writerPort");
			if ( sWriterPort==null ) {
				throw new SAXException("Error while parsing config xml. The atribute 'writerPort' must be especified");
			}
			Integer iWriterPort = null;
			try {
				iWriterPort = new Integer(sWriterPort);
			} catch ( NumberFormatException e ) {
				throw new SAXException("The value of atribute 'writerPort' is invalid", e);
			}
			
			String sOfficePort = attributes.getValue("officePort");
			Integer iOfficePort = null;
			if ( sOfficePort!=null ) {
				try {
					iOfficePort = new Integer(sOfficePort);
				} catch ( NumberFormatException e ) {
					throw new SAXException("The value of atribute 'officePort' is invalid", e);
				}
			}
			
			
			config.setJarsPath(sJarsPath);
			config.setWriterPort(iWriterPort);
			config.setOfficePort(iOfficePort);
			config.setBootstrapJar(bootstrapJar);
			config.setOfficeBeanJar(officeBeanJar);
			config.setLanguage(language);
			config.setLogPattern(logPattern);
			config.setTitle(title);
			config.setWriterRequestServletURLPattern(writerRequestServletURLPattern);
			
		}
	}
	
	public void endDocument() throws SAXException {
		if ( config==null ) {
			throw new SAXException("The configuration can not be done. The configuration is empty.");
		}
		
		config.isValid();
	}
	
	public WriterConfig getConfig() {
		return config;
	}
}
