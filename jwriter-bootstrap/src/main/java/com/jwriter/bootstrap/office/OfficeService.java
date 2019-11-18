package com.jwriter.bootstrap.office;





public interface OfficeService {
	public void startOfficeService (String pathSoffice);
	
	public Object newOOoBeanInstance (String pathSoffice, ClassLoader classLoader) throws OfficeServiceException;
	
	public void stopOfficeService() ;

}
