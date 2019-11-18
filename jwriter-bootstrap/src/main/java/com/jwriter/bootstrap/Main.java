package com.jwriter.bootstrap;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.Map;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.jwriter.bootstrap.argument.ArgumentsBuilder;
import com.jwriter.bootstrap.argument.ArgumentsException;
import com.jwriter.bootstrap.argument.MainArguments;
import com.jwriter.bootstrap.dependency.DependencyManagerException;
import com.jwriter.bootstrap.office.OfficeService;
import com.jwriter.bootstrap.office.OfficeServiceException;
import com.jwriter.bootstrap.service.WriterService;
import com.jwriter.bootstrap.util.MessagesUtil;


/**
 * Main class that is executed since the JWS starts
 * Is responsible for creating the log file, program context and pass the execution
 * to the plugin
 * @author leonardo.borges
 *
 */
public class Main {
	private static final Logger log = Logger.getLogger(Main.class);
	private static final String KEY_LOG_PATTERN = "logPattern";
	
	public static void main(String[] args) throws BootstrapException, ArgumentsException, DependencyManagerException, OfficeServiceException {
		for ( String arg: args ) {
			System.out.println("\""+arg+"\"");
		}
		System.setSecurityManager(null);
		Map<String, List<String[]>> argsMap = ArgumentsBuilder.parseArguments(args);
		List<String[]> logPatternListValues = argsMap.get(KEY_LOG_PATTERN);
		if ( logPatternListValues==null || logPatternListValues.size()>1 || logPatternListValues.get(0)==null || logPatternListValues.get(0).length>1 ) {
			throw new ArgumentsException("Argument '"+KEY_LOG_PATTERN+"' not found or is invalid");
		}
		PatternLayout pattern = new PatternLayout( logPatternListValues.get(0)[0] );
		Logger.getRootLogger().addAppender( new ConsoleAppender( pattern ) );
		log.info("Console log succefful configured");
		boolean firstInstance = initializeExecution (argsMap);
		if ( !firstInstance ) {
			log.info("Shutting down current application");
			System.exit(0);
		}
	}

	private static boolean initializeExecution(Map<String, List<String[]>> argsMap) throws BootstrapException, ArgumentsException, DependencyManagerException, OfficeServiceException {
		log.info("Parsing main arguments");
		MainArguments mainArgs = (MainArguments) ArgumentsBuilder.getArguments(argsMap, MainArguments.class);
		Integer writerPort = mainArgs.getWriterPort();
		OfficeService officeService = mainArgs.getOfficeService();
		MessagesUtil messages = mainArgs.getMessages();
		PatternLayout logPattern = mainArgs.getLogPattern();
		String title = mainArgs.getTitle();
		log.info("JWriter will run on port: "+writerPort);
		log.info("Office will be executed by: "+officeService);
		log.info("Veryfing if an instance of jWriter is already running on local machine");
		Socket socket = null;		
		boolean firstInstance = false;		
		try {
			socket = new Socket(InetAddress.getLocalHost(), writerPort);
			log.info("Connection succefull estabilished on port "+writerPort);
		} catch (ConnectException ce) {
			log.info("The connection could not be estabilished");
			log.info("Starting a new instance of jWriter on local machine");
			WriterService service = new WriterService(writerPort, officeService, messages, logPattern, title);
			service.start();
			firstInstance = true;
		} catch ( IOException io ) {
			throw new BootstrapException("Error trying to conect on local jWriter instance on port "+writerPort, io);
		}
		log.info("Transfering execution to local jWriter instance");
		transferExecution(socket, writerPort, argsMap);
		log.info("Tranfer finished");
		return firstInstance;
	}
	
	private static void transferExecution(Socket socket, Integer writerPort, Map<String, List<String[]>> argsMap) throws BootstrapException {
		if ( socket==null ) { 
			try {
				socket = new Socket( InetAddress.getLocalHost(), writerPort );
			} catch (Exception e) {
				throw new BootstrapException("Error trying to estabilish connection on local jWriter instance on port "+writerPort, e);
			}
		}
		try {
			ObjectOutputStream oos = new ObjectOutputStream( socket.getOutputStream() );
			oos.writeObject( argsMap );
			oos.close();
		} catch ( IOException e ) {
			throw new BootstrapException("Error trying to transfer arguments to running jWriter", e);
		}
	}
}
