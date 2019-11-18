package com.jwriter.bootstrap.service;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.jwriter.bootstrap.BootstrapException;
import com.jwriter.bootstrap.config.ConfigException;
import com.jwriter.bootstrap.config.UserConfig;
import com.jwriter.bootstrap.config.UserConfigManager;
import com.jwriter.bootstrap.dependency.DependencyManager;
import com.jwriter.bootstrap.dependency.DependencyManagerException;
import com.jwriter.bootstrap.loader.LoaderException;
import com.jwriter.bootstrap.loader.WriterLoader;
import com.jwriter.bootstrap.office.OfficeService;
import com.jwriter.bootstrap.util.MessagesUtil;


public class BootstrapContext {
	private static final Logger log = Logger.getLogger(BootstrapContext.class);
	
	private static BootstrapContext context;
	
	private Integer writerPort;
	private OfficeService officeService;
	private MessagesUtil messages;
	private String localWriterPath;
	private File logFile;
	private DependencyManager dependencyManager;
	private WriterLoader loader; 
	private UserConfig userConfig;
	private String title;
	
	private BootstrapContext (Integer writerPort, OfficeService officeService, MessagesUtil messages, PatternLayout logPattern, String title) throws BootstrapException, DependencyManagerException {
		this.writerPort = writerPort;
		this.officeService = officeService;
		this.messages = messages;
		this.title = title;
		this.localWriterPath = System.getProperty("user.home")+"/.jWriter/";
		log.info("Configurating log on local user folder file");
		this.logFile = configureFileLog(logPattern);
		log.info("Configurating dependecy management of dependencies jar");
		this.dependencyManager = configureDependecyManager();
		log.info("Configurating loca user file configuration");
		try {
			this.userConfig = readUserConfiguration();
		} catch (ConfigException e) {
			throw new BootstrapException("Error while trying to read user configuration file", e);
		}
		log.info("Configurating initial class loader with open office default jars");
		try {
			this.loader = configureInitialClassLoader( userConfig );
		} catch (LoaderException e) {
			throw new BootstrapException("Error while trying to configure initial class loader of jars", e);
		}
	}
	
	private UserConfig readUserConfiguration() throws ConfigException {
		return UserConfigManager.getUserConfig(messages, localWriterPath, title);
	}

	private DependencyManager configureDependecyManager() throws DependencyManagerException {
		return new DependencyManager(localWriterPath);
	}

	private WriterLoader configureInitialClassLoader(UserConfig userConfig) throws LoaderException {
		WriterLoader loader = new WriterLoader(userConfig.getPathJuh(), userConfig.getPathJurt(),
				userConfig.getPathRidl(), userConfig.getPathUnoil());
		return loader;
	}

	public static BootstrapContext init (Integer writerPort, OfficeService officeService, MessagesUtil messages, PatternLayout logPattern, String title) throws BootstrapException, DependencyManagerException {
		context = new BootstrapContext(writerPort, officeService, messages, logPattern, title);
		return context;
	}
	
	public static BootstrapContext getInstance() {
		return context;
	}
	
	private File configureFileLog(PatternLayout logPattern) throws BootstrapException {
		File writerLocalFileFolder = new File( localWriterPath );
		if ( !writerLocalFileFolder.exists() ) {
			log.info("Local folder does not exist, creating new");
			writerLocalFileFolder.mkdir();
		}
		File fileLog = new File(writerLocalFileFolder, "jwriter.log");
		String fileLogPath = fileLog.getAbsolutePath();
		try {
			Logger.getRootLogger().addAppender( new FileAppender( logPattern , fileLogPath, false) );
		} catch (IOException e) {
			throw new BootstrapException("Error trying to create new file appender to log on: "+fileLogPath, e);
		}
		log.info("Logger succefull configurated on: "+fileLogPath);
		return fileLog;
		
	}

	public Integer getWriterPort() {
		return writerPort;
	}

	public OfficeService getOfficeService() {
		return officeService;
	}

	public MessagesUtil getMessages() {
		return messages;
	}

	public String getLocalWriterPath() {
		return localWriterPath;
	}

	public File getLogFile() {
		return logFile;
	}

	public DependencyManager getDependencyManager() {
		return dependencyManager;
	}

	public WriterLoader getLoader() {
		return loader;
	}

	public UserConfig getUserConfig() {
		return userConfig;
	}

	public String getTitle() {
		return title;
	}
	
}
