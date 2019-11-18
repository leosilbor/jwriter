package com.jwriter.bootstrap.service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.jwriter.bootstrap.BootstrapException;
import com.jwriter.bootstrap.dependency.DependencyManagerException;
import com.jwriter.bootstrap.office.OfficeService;
import com.jwriter.bootstrap.office.OfficeServiceException;
import com.jwriter.bootstrap.office.SocketOfficeService;
import com.jwriter.bootstrap.util.MessagesUtil;
import com.jwriter.bootstrap.util.UIUtil;


public class WriterService extends Thread {
	private static final Logger log = Logger.getLogger(WriterService.class);
	
	private ServerSocket writerServiceSocket;
	
	public static void main(String[] args) throws BootstrapException, DependencyManagerException, OfficeServiceException {
		PatternLayout pattern = new PatternLayout("%d{ABSOLUTE} %5p %c{1}:%L - %m%n");
		Logger.getRootLogger().addAppender( new ConsoleAppender( pattern ) );
		MessagesUtil messages = new MessagesUtil( new Locale("pt") );
		WriterService service = new WriterService(8854, new SocketOfficeService(8855), messages, pattern, "jWriter 1.0");
		service.start();
	}
	
	public WriterService (Integer writerPort, OfficeService officeService, MessagesUtil messages, PatternLayout logPattern, String title) throws BootstrapException, DependencyManagerException {
		log.info("Configuring user interface");
		configureUI(messages, title, officeService);
		log.info("Creating initial context");
		BootstrapContext.init(writerPort, officeService, messages, logPattern, title);
		log.info("User inteface configured");
		log.info("Initial context configured");
		log.info("Initialiazing writer socket");
		this.writerServiceSocket = configureServerSocket(writerPort);
		log.info("Pre-Initialiazing office service");
		String pathBinSoffice = BootstrapContext.getInstance().getUserConfig().getPathSoffice();
		officeService.startOfficeService(pathBinSoffice);
	}
	
	private void configureUI(final MessagesUtil messages, final String title, final OfficeService officeService) throws BootstrapException {
		log.info("Defining system default look and feel on application");
		try {
			UIUtil.setSystemLookAndFeel();
		} catch (Exception e) {
			throw new BootstrapException("Error trying to define system look and feel", e);
		}
		
		log.info("Creating tray icon");
		
		UIUtil.createTrayIcon(title, messages.get("tray.close.label"), messages.get("confirm.close.application"));
		
	}

	private ServerSocket configureServerSocket(Integer port) throws BootstrapException {
		log.info("Start listening on port: "+port);
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			throw new BootstrapException("Error trying to create a service for jWriter on port "+port, e);
		}
		return serverSocket;
	}
	
	public void run() {
		while ( true ) {
			Socket socketNewArgs = null;
			try {
				log.info("Wainting for new connections...");
				socketNewArgs = writerServiceSocket.accept();
				log.info("New connection received, routing...");
			} catch (IOException e) {
				BootstrapContext bootstrapContext = BootstrapContext.getInstance();
				String msg = bootstrapContext.getMessages().get("error.wait.connection");
				log.fatal(msg, e);
				UIUtil.showError( null, bootstrapContext.getTitle(), msg );
				bootstrapContext.getOfficeService().stopOfficeService();
				System.exit(0);
			}
			
			PluginInitializer pluginExe = new PluginInitializer(socketNewArgs);
			pluginExe.start();
			
		}
		
	}
	
}
