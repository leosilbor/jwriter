package com.jwriter.core.config;

import java.io.InputStream;
import java.io.Serializable;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

public class WriterConfig implements Serializable {
	
	private static final long serialVersionUID = 93154177829552856L;
	
	private String jarsPath;
	private Integer writerPort;
	private Integer officePort;
	private String bootstrapJar;
	private String officeBeanJar;
	
	private String language;
	private String logPattern;
	private String title;
	private String writerRequestServletURLPattern;
	
	public String getWriterRequestServletURLPattern() {
		return writerRequestServletURLPattern;
	}
	public void setWriterRequestServletURLPattern(
			String writerRequestServletURLPattern) {
		this.writerRequestServletURLPattern = writerRequestServletURLPattern;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getBootstrapJar() {
		return bootstrapJar;
	}
	public void setBootstrapJar(String bootstrapJar) {
		this.bootstrapJar = bootstrapJar.replace(".jar", "");
	}
	public String getOfficeBeanJar() {
		return officeBeanJar;
	}
	public void setOfficeBeanJar(String officeBeanJar) {
		this.officeBeanJar = officeBeanJar.replace(".jar", "");
	}
	public String getJarsPath() {
		return jarsPath;
	}
	public void setJarsPath(String jarsPath) {
		this.jarsPath = jarsPath;
	}
	public Integer getWriterPort() {
		return writerPort;
	}
	public void setWriterPort(Integer writerPort) {
		this.writerPort = writerPort;
	}
	public Integer getOfficePort() {
		return officePort;
	}
	public void setOfficePort(Integer officePort) {
		this.officePort = officePort;
	}
	public String getLogPattern() {
		return logPattern;
	}	
	public void setLogPattern(String logPattern) {
		this.logPattern = logPattern;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void isValid() throws SAXException {
		if ( jarsPath==null ) {
			throw new SAXException("The atribute 'jarsPath' is null");
		}
		if ( writerPort==null ) {
			throw new SAXException("The atribute 'writerPort' is null");
		}
		if ( bootstrapJar==null ) {
			throw new SAXException("The atribute 'bootstrapJar' is null");
		}
		if ( officeBeanJar==null ) {
			throw new SAXException("The atribute 'officeBeanJar' is null");
		}
		if ( language==null ) {
			throw new SAXException("The atribute 'language' is null");
		}
		if ( logPattern==null ) {
			throw new SAXException("The atribute 'logPattern' is null");
		}
		if ( title==null ) {
			throw new SAXException("The atribute 'title' is null");
		}
		if ( writerRequestServletURLPattern==null ) {
			throw new SAXException("The atribute 'writerRequestServletURLPattern' is null");
		}
		
	}
	public static WriterConfig init (InputStream is) throws Exception {
		WriterConfigHandler handler = new WriterConfigHandler();
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(is, handler);		
		return handler.getConfig();
	}
	
	
}
