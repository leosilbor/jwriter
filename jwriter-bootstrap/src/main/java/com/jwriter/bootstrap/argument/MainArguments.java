package com.jwriter.bootstrap.argument;

import java.util.Locale;
import java.util.MissingResourceException;

import org.apache.log4j.PatternLayout;

import com.jwriter.bootstrap.office.OfficeService;
import com.jwriter.bootstrap.office.PipeOfficeService;
import com.jwriter.bootstrap.office.SocketOfficeService;
import com.jwriter.bootstrap.util.MessagesUtil;



/**
 * Class that represents a structured view of the argument passed do the Main application by JWS
 * @author leonardo.borges
 *
 */
public class MainArguments implements Arguments {
	private static final long serialVersionUID = -4636930068976725251L;
	
	private Integer writerPort;
	private MessagesUtil messages;
	private PatternLayout logPattern;
	private String title;	
	private OfficeService officeService;
	
	public String getTitle() {
		return title;
	}
	public PatternLayout getLogPattern() {
		return logPattern;
	}
	public MessagesUtil getMessages() {
		return messages;
	}
	public Integer getWriterPort() {
		return writerPort;
	}
	public OfficeService getOfficeService() {
		if ( officeService==null ) {
			officeService = new PipeOfficeService();
		}
		return officeService;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	public void setLogPattern(String logPattern) {
		this.logPattern = new PatternLayout(logPattern);
	}
	public void setLanguage(String language) throws ArgumentsException {
		Locale locale = new Locale(language);
		try {
			this.messages = new MessagesUtil(locale);
		} catch ( MissingResourceException e ) {
			throw new ArgumentsException("Invalid language: "+language, e);
		}
	}
	public void setWriterPort(String writerPort) throws ArgumentsException {
		try {
			this.writerPort = new Integer(writerPort);
		} catch ( NumberFormatException e ) {
			throw new ArgumentsException("Invalid writer port: "+writerPort, e);
		}
		
	}
	public void setOfficePort(String officePort) throws ArgumentsException {
		Integer iOfficePort = null;
		try {
			iOfficePort = new Integer(officePort);
		} catch ( NumberFormatException e ) {
			throw new ArgumentsException("Invalid office port: "+officePort, e);
		}
		
		this.officeService = new SocketOfficeService(iOfficePort);
		
	}
	public void validate() throws ArgumentsException {
		if ( getWriterPort()==null ) {
			throw new ArgumentsException("Argument 'writerPort' not found");
		}
		
		if ( getMessages()==null ) {
			throw new ArgumentsException("Argument 'language' not found");
		}
		
		if ( getLogPattern()==null ) {
			throw new ArgumentsException("Argument 'logPattern' not found");
		}
		
		if ( getTitle()==null ) {
			throw new ArgumentsException("Argument 'title' not found");
		}
		
	}
	
}
