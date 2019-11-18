package com.jwriter.bootstrap.office;

import java.io.IOException;
import java.lang.reflect.Constructor;

import org.apache.log4j.Logger;

public abstract class AbstractOfficeService implements OfficeService {
	private static final Logger log = Logger.getLogger(AbstractOfficeService.class);
	
	private Process officeProcess;
	
	public Object newOOoBeanInstance(String pathSoffice, ClassLoader classLoader) throws OfficeServiceException {
		log.info("Creating new instance of OOoBean on '"+pathSoffice+"'");
		Class<?> classBean = null;
		try {
			classBean = classLoader.loadClass("com.sun.star.comp.beans.OOoBean");
		} catch ( Throwable e ) {
			throw new OfficeServiceException("Error while trying to load OOoBean Class", e);
		}
		
		Object beanParameterValue = getOOoBeanParameterValue();
		Class<?> beanParameterType = beanParameterValue.getClass();
		
		Constructor<?> consBean = null;
		try {
			consBean = classBean.getConstructor(String.class, beanParameterType);
		} catch ( Throwable e ) {
			throw new OfficeServiceException("Error while trying to get constructor of OOoBean (String, "+beanParameterType.getName()+")", e);
		}
		
		try {
			return consBean.newInstance(pathSoffice, beanParameterValue);
		} catch ( Throwable e ) {
			throw new OfficeServiceException("Error while trying to instantiate new OOoBean instance ("+pathSoffice+", "+beanParameterValue+")", e);
		}
	}
	
	public void stopOfficeService() {
		log.info("Trying to kill office process");
		try {
			Runtime.getRuntime().exec("taskkill /IM soffice* /F");
		} catch (IOException io) {
			log.error("Error trying to execute command to finish office: tskill soffice", io);
		}
//		if ( officeProcess!=null ) {
//			officeProcess.destroy();
//		}
	}
	
	public void startOfficeService (String pathSoffice) {
		this.officeProcess = startOfficeServiceProcess (pathSoffice);
	}
	
	public abstract Process startOfficeServiceProcess (String pathSoffice);

	public abstract Object getOOoBeanParameterValue() ;

}
