package com.jwriter.web.servlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jwriter.core.request.WriterRequest;
import com.jwriter.editor.request.EditorRequest;
import com.jwriter.web.request.EditorRequestImpl;

public class OpenDocumentServlet extends HttpServlet {

	private static final long serialVersionUID = -5820357204017493201L;
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		File document = new File("c:/document.odt");
		
		EditorRequest editorRequest = new EditorRequestImpl("odt", "Test Document", document);
		
		req.setAttribute( WriterRequest.KEY_REQUEST_JNLP , editorRequest);
		
		req.getRequestDispatcher("./jnlpFactoryServlet").forward(req, resp);
	}

}
