package com.jwriter.core.web.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.jwriter.core.request.RequestException;
import com.jwriter.core.request.WriterRequest;
import com.jwriter.core.web.message.ErrorResponseMessage;
import com.jwriter.core.web.message.RequestMessage;
import com.jwriter.core.web.message.SuccessResponseMessage;

public class WriterRequestControllerServlet extends HttpServlet {

	private static final long serialVersionUID = -8424717944762349780L;
	private static final Logger log = Logger.getLogger(WriterRequestControllerServlet.class);
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ObjectInputStream ois = new ObjectInputStream ( new BufferedInputStream( req.getInputStream() ) );
		RequestMessage requestParameter = null;
		try {
			requestParameter = (RequestMessage) ois.readObject();
		} catch (ClassNotFoundException e) {
			sendError (resp, e);
			return;
		}
		
		HttpSession session = req.getSession();
		
		log.info("Looking for Request JNLP of id '"+requestParameter.getRequestID()+"' on session '"+session.getId()+"'");
		
		WriterRequest writerRequest = (WriterRequest) session.getAttribute( requestParameter.getRequestID() );
		
		if ( writerRequest==null ) {
			sendError(resp, "WriterRequest of ID '"+requestParameter.getRequestID()+"' not found on user session of ID '"+session.getId()+"'");
			return;
		}
		
		Object[] args = requestParameter.getArguments();
		
		Class<?>[] clazzes = getParameterTypes (args);
		Method method = null;
		try {
			method = writerRequest.getClass().getMethod( requestParameter.getMethodName() , clazzes);
		} catch (Exception e) {
			sendError(resp, e);
			return;
		}
		
		log.info("Invoking method '"+requestParameter.getMethodName()+"' by reflection");
		
		Object respObj = null;
		try {
			respObj = method.invoke(writerRequest, args);
			sendResponse (resp, new SuccessResponseMessage( respObj ));
		} catch (Exception e) {
			if ( e instanceof InvocationTargetException && e.getCause() instanceof RequestException ) {
				sendError(resp, e.getCause());
			} else {
				sendError(resp, e);
			}
		} 
		
	
	}

	private void sendResponse(HttpServletResponse resp, Object obj) throws IOException {
		resp.setContentType("application/binary");
		ObjectOutputStream oos = new ObjectOutputStream( new BufferedOutputStream( resp.getOutputStream() ) );
		oos.writeObject( obj );
		oos.close();
		
	}

	private Class<?>[] getParameterTypes(Object[] args) {
		Class<?>[] clazzes = new Class[args.length];
		for ( int i=0 ; i<args.length ; i++ ) {
			clazzes[i] = args[i].getClass();
		}
		return clazzes;
	}

	private void sendError(HttpServletResponse resp, Throwable e) throws IOException {
		e.printStackTrace();
		sendResponse(resp, new ErrorResponseMessage(e));
	}
	
	private void sendError(HttpServletResponse resp, String message) throws IOException {
		sendResponse(resp, new ErrorResponseMessage(message));
	}

}
