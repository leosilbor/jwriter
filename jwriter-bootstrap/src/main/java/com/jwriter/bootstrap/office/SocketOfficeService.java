package com.jwriter.bootstrap.office;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.log4j.Logger;

public class SocketOfficeService extends AbstractOfficeService {
	private static final Logger log = Logger.getLogger(SocketOfficeService.class);
	
	private Integer port;

	public SocketOfficeService(Integer port) {
		this.port = port;
	}

	public Process startOfficeServiceProcess(String pathSoffice) {
		log.info("Trying to connect on port '"+port+"'");
		Process officeProcess = null;
		try {
			new Socket(InetAddress.getLocalHost(), port);
			log.info("Connection succefull estabilished on port "+port+", open office is already running");
		} catch (ConnectException ce) {
			log.info("The connection could not be estabilished");
			log.info("Starting a new instance of office on local machine on port '"+port+"'");
			try {
				String cmd[] = new String[5];
				cmd[0] = pathSoffice;
				cmd[1] = "-nologo";
				cmd[2] = "-nodefault";
				cmd[3] = "-accept=socket,port="+port+";urp";
				cmd[4] = "-norestore";
				officeProcess = Runtime.getRuntime().exec(cmd);
			} catch (IOException io) {
				log.error("Error trying to execute process to start a new office service", io);
			}
		} catch ( IOException io ) {
			log.error("Error trying to conect on local office instance on port "+port, io);
		}
		
		return officeProcess;
		
	}

	public Object getOOoBeanParameterValue() {
		return port;
	}
	
}
