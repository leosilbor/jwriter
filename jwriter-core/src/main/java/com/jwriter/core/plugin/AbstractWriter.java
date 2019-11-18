package com.jwriter.core.plugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.jwriter.bootstrap.argument.ArgumentsBuilder;
import com.jwriter.bootstrap.argument.ArgumentsException;
import com.jwriter.bootstrap.office.OfficeServiceException;
import com.jwriter.bootstrap.plugin.PluginException;
import com.jwriter.bootstrap.plugin.Writer;
import com.jwriter.bootstrap.service.BootstrapContext;
import com.jwriter.core.argument.BasicArguments;
import com.jwriter.core.web.message.ErrorResponseMessage;
import com.jwriter.core.web.message.RequestMessage;
import com.jwriter.core.web.message.ResponseMessage;
import com.jwriter.core.web.message.SuccessResponseMessage;
import com.sun.star.comp.beans.OOoBean;

public abstract class AbstractWriter<T extends BasicArguments> extends Writer {
	private static final Logger log = Logger.getLogger(AbstractWriter.class);
	
	private T arguments;
	private Class<T> clazzArguments;
	private OOoBean bean;
	private Macro macro;

	public AbstractWriter(String pathSoffice, Class<T> clazzArguments) {
		super(pathSoffice);
		this.clazzArguments = clazzArguments;
	}

	public void start(Map<String, List<String[]>> argsMap) throws PluginException {
		try {
			log.info("Parsing writer plugin arguments");
			this.arguments = (T) ArgumentsBuilder.getArguments(argsMap, clazzArguments);
		} catch (ArgumentsException e) {
			throw new PluginException("List of arguments of the plugin is invalid", e);
		}
		
		try {
			log.info("Creating new instance of office bean");
			this.bean = (OOoBean) BootstrapContext.getInstance().getOfficeService().newOOoBeanInstance( getPathSoffice() , this.getClass().getClassLoader());
		} catch (OfficeServiceException e) {
			throw new PluginException("Error trying to create new instance of OOoBean", e);
		}
		
		this.macro = new Macro(bean);
		
		start();
		
	}
	
	protected abstract void start () throws PluginException;
	
	protected Object sendRequestMessage (String methodName, Object... args) throws PluginException {
		log.info("Starting communication on server process: methodName="+methodName);
		URL url = null;
		try {
			url = new URL( arguments.getWriterRequestServletURL() );
		} catch (MalformedURLException e) {
			throw new PluginException("The URL especified to send request is invalid: "+arguments.getWriterRequestServletURL(), e);
		}
		log.info("Server url is '"+url+"'");
		HttpURLConnection con = null;;
		try {
			con = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			throw new PluginException("Error while trying to open connection to server on '"+arguments.getWriterRequestServletURL()+"'", e);
		}
		con.setDoInput(true);
		con.setDoOutput(true);
		try {
			con.setRequestMethod("POST");
		} catch (ProtocolException e) {
			throw new PluginException("Error while setting POST comunication type", e);
		}
		con.setRequestProperty("Cookie", "JSESSIONID="+arguments.getSessionID());  
		con.setRequestProperty("Content-type", "application/binary");
		log.info("Writing content to be sent");
		try {
			ObjectOutputStream oos = new ObjectOutputStream( con.getOutputStream() );
			oos.writeObject( new RequestMessage(arguments.getWriterRequestID(), methodName, args) );
			oos.close();
		} catch ( IOException e ) {
			throw new PluginException("Error while trying to send request message", e);
		}
		log.info("Connectin on server");
		try {
			con.connect();
		} catch (IOException e) {
			throw new PluginException("Error while trying to connect to server", e);
		}
		log.info("Wainting for response");
		ResponseMessage responseMessage = null;
		try {
			ObjectInputStream ois = new ObjectInputStream( con.getInputStream() );
			responseMessage = (ResponseMessage) ois.readObject();
			ois.close();
		} catch ( Exception e ) {
			throw new PluginException("Error while trying to read server response", e);
		}
		log.info("Response is '"+responseMessage+"'");
		if ( responseMessage instanceof  ErrorResponseMessage ) {
			ErrorResponseMessage erm = (ErrorResponseMessage) responseMessage;
			log.error( erm.getSuperStackTrace() );
			throw new PluginException(erm.getMessage(), erm);
		}
		
		con.disconnect();
		
		return ((SuccessResponseMessage)responseMessage).getResponse();
	}
	
	public T getArguments() {
		return this.arguments;
	}
	
	public OOoBean getOOoBean () {
		return bean;
	}
	
	public Macro getMacro () {
		return macro;
	}


}
