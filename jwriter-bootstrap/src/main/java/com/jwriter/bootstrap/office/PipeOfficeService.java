package com.jwriter.bootstrap.office;

import java.io.IOException;
import java.net.URLEncoder;

import org.apache.log4j.Logger;



public class PipeOfficeService extends AbstractOfficeService {
	private static final Logger log = Logger.getLogger(PipeOfficeService.class);
	
	private String pipeName;
	
	public PipeOfficeService () {
		this.pipeName = getPipeName();
	}
	
	public Process startOfficeServiceProcess(String pathSoffice) {
		log.info("Trying start office pipe connection '"+pipeName+"'");
		Process officeProcess = null;
		try {
			String cmd[] = new String[5];
			cmd[0] = pathSoffice;
			cmd[1] = "-nologo";
			cmd[2] = "-nodefault";
			cmd[3] = "-accept=pipe,name="+pipeName+";urp;StarOffice.ServiceManager";
			cmd[4] = "-norestore";
			officeProcess = Runtime.getRuntime().exec(cmd);
		} catch (IOException io) {
			log.error("Error trying to execute process to start a new office service", io);
		}
		return officeProcess;
	}

	private String replaceAll(String aString, String aSearch, String aReplace ) {
        StringBuffer aBuffer = new StringBuffer(aString);

        int nPos = aString.length();
        int nOfs = aSearch.length();
        
        while ( ( nPos = aString.lastIndexOf( aSearch, nPos - 1 ) ) > -1 )
            aBuffer.replace( nPos, nPos+nOfs, aReplace );

        return aBuffer.toString();
	}

	public Object getOOoBeanParameterValue() {
		return pipeName;
	}
	
	private String getPipeName () {
		String aPipeName = System.getProperty("user.name") + "_Office";
		aPipeName = replaceAll( aPipeName, "_", "%B7" );
		return replaceAll( replaceAll( URLEncoder.encode(aPipeName), "+", "%20" ), "%", "_" );
	}



}
