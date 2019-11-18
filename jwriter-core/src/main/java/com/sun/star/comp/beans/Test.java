package com.sun.star.comp.beans;


public class Test {
	public static void main(String[] args) throws Exception {
//		String cmd[] = new String[4];
//		cmd[0] = "C:\\Arquivos de programas\\BrOffice.org 2.4\\program\\soffice.exe";
//		cmd[1] = "-nologo";
//		cmd[2] = "-nodefault";
//		cmd[3] = "-accept=socket,port="+8855+";urp";
//		Process officebeanProcess = Runtime.getRuntime().exec(cmd);
		
		OOoBean bean = new OOoBean("C:\\Arquivos de programas\\BrOffice.org 2.4\\program\\soffice.exe", "pipe_do_leo");
		bean.getOOoConnection();
		System.out.println( bean.getOOoDesktop().terminate() );
		bean.stopOOoConnection();
		bean.getOOoConnection();
//		bean.stopOOoConnection();
//	      
//	      // open another connection to the server, in order to call terminate it.
//	      // use of OOoBean.getOOoDesktop ().terminate () doesn't work so we have to do like this actually
//	      // in fact, OOoBean doesn't support termination of desktop (a bug of OOoBean)
//	      XComponentContext xRemoteContext = null;
//
//	      // create a local context then create a XUnoUrlResolver
//	      XComponentContext xLocalContext = com.sun.star.comp.helper.Bootstrap.createInitialComponentContext(null);
//	      XMultiComponentFactory xLocalServiceManager = xLocalContext.getServiceManager();
//	          Object urlResolver  = xLocalServiceManager.createInstanceWithContext("com.sun.star.bridge.UnoUrlResolver", xLocalContext );       
//	      XUnoUrlResolver xUnoUrlResolver = (XUnoUrlResolver) UnoRuntime.queryInterface(XUnoUrlResolver.class, urlResolver);
//	   
//	      // use XUnoUrlResolver to create the remote default context
//	      Object initialObject = xUnoUrlResolver.resolve(null);
//	      XPropertySet xPropertySet = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, initialObject);
//	      Object context = xPropertySet.getPropertyValue("DefaultContext");           
//	      xRemoteContext = (XComponentContext)UnoRuntime.queryInterface(XComponentContext.class, context);
//	   
//	      // get the remote desktop and call terminate () on it to terminate OOo process
//	      Object desk = xRemoteContext.getServiceManager().createInstanceWithContext("com.sun.star.frame.Desktop", xRemoteContext);
//	      XDesktop xDesk = (XDesktop)UnoRuntime.queryInterface(XDesktop.class, desk);
//	      
//	      xDesk.terminate();
		
	}
}
