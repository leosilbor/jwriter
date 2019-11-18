package com.jwriter.core.web.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import com.jwriter.bootstrap.Main;
import com.jwriter.bootstrap.Mock;
import com.jwriter.bootstrap.argument.ArgumentsBuilder;
import com.jwriter.bootstrap.util.HashUtil;
import com.jwriter.bootstrap.util.IOUtil;
import com.jwriter.core.config.WriterConfig;
import com.jwriter.core.request.WriterRequest;

public class JnlpFactoryServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(JnlpFactoryServlet.class);

	private static final long serialVersionUID = 1838480286887714498L;
	
	private Template velocityTemplate; 
	private WriterConfig writerConfig;
	private Map<String, String> localHashJars;
	
	
	public void init(ServletConfig config) throws ServletException {
		log.info("Starting Velocity services");
		Velocity.setProperty( "resource.loader" , "class");
		Velocity.setProperty( "class.resource.loader.description" , "Velocity Classpath Resource Loader");
		Velocity.setProperty( "class.resource.loader.class" , "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		
		try {
			Velocity.init();
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException("Error trying to initialize JWriterServlet. Velocity error.", e);
		}
		
		try {
			this.velocityTemplate = Velocity.getTemplate("template.jnlp");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException("Error trying to create velocity template from 'template.jnlp'", e);
		}
		
		velocityTemplate.setEncoding("ISO-8859-1");
		
		InputStream isConfig = JnlpFactoryServlet.class.getClassLoader().getResourceAsStream("jwriter-config.xml");
		if ( isConfig==null ) {
			throw new ServletException("The file configuration of jWriter 'jwriter-config.xml' not found");
		}
		
		try {
			writerConfig = WriterConfig.init(isConfig);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException("Error trying to parse configuration file.", e);
		}
		
		String localJarsPath = config.getServletContext().getRealPath( writerConfig.getJarsPath() );
		
		try {
			localHashJars = HashUtil.generateJarsHash(localJarsPath, "MD5");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException("Error while trying to generate hash of jwriter jars dependecies", e);
		}
	}
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Object objRequestJnlp = (WriterRequest) req.getAttribute( WriterRequest.KEY_REQUEST_JNLP );
		WriterRequest requestJnlp = null;
		if ( objRequestJnlp instanceof WriterRequest ) {
			requestJnlp = (WriterRequest) objRequestJnlp;
		} else {
			throw new ServletException("The attribute in '"+WriterRequest.KEY_REQUEST_JNLP+"' is not a valid '"+WriterRequest.class.getCanonicalName()+"' instance");
		}
		
		String jarsPath = writerConfig.getJarsPath();
		Integer writerPort = writerConfig.getWriterPort();
		Integer officePort = writerConfig.getOfficePort();
		String bootstrapJar = writerConfig.getBootstrapJar();
		String officeBeanJar = writerConfig.getOfficeBeanJar();
		String language = writerConfig.getLanguage();
		String logPattern = writerConfig.getLogPattern();
		String title = writerConfig.getTitle();
		String writerRequestServletURLPattern = writerConfig.getWriterRequestServletURLPattern();
		
		String appURL = getAppURL(req);
		
		List<String> arguments = new ArrayList<String>();
		arguments.add("--writerPort");
		arguments.add(writerPort.toString());
		if ( officePort!=null ) {
			arguments.add("--officePort");
			arguments.add(officePort.toString());
		}
		arguments.add("--urlLib");
		arguments.add(appURL+jarsPath);
		arguments.add("--language");
		arguments.add(language);
		arguments.add("--logPattern");
		arguments.add(logPattern);
		arguments.add("--title");
		arguments.add(title);
		
		HttpSession session = req.getSession();
		
		arguments.add("--writerRequestServletURL");
		arguments.add(appURL+writerRequestServletURLPattern);
		arguments.add("--sessionID");
		arguments.add(session.getId());
		arguments.add("--writerRequestID");
		arguments.add(requestJnlp.getID());
		arguments.add("--docType");
		arguments.add(requestJnlp.getDocType());
		arguments.add("--docName");
		arguments.add(requestJnlp.getDocName());
		
		String classPlugin = requestJnlp.getClassPlugin();
		
		if ( classPlugin==null ) {
			throw new ServletException("The request JNLP does not contain a valid '"+classPlugin+"'");
		}
		
		List<String> oitList = requestJnlp.getTransformers();
		
		if ( oitList!=null && oitList.size()>0 ) {
			for ( String oit: oitList ) {
				arguments.add("--transformer");
				arguments.add( oit );
			}
		}
		
		List<String> dependencies = requestJnlp.getDependencies();
		
		if ( dependencies==null || dependencies.size()==0 ) {
			throw new ServletException("The request JNLP dependencies must contain the plugin jar at minimun");
		}
		
		for ( String jarDep: dependencies ) {
			arguments.add("--jar");
			jarDep = jarDep.replace(".jar", "");
			arguments.add( jarDep );
			String hashJar = getHashJar( jarDep );
			arguments.add( hashJar );
		}
		
		List<String> jnlpArgs = requestJnlp.getArguments();
		if ( jnlpArgs!=null ) {
			arguments.addAll( jnlpArgs );
		}
		
		arguments.add("--pluginClass");
		arguments.add(classPlugin);
		
		arguments.add("--jar");
		arguments.add( officeBeanJar );
		String hashJar = getHashJar( officeBeanJar );
		arguments.add( hashJar );
		
		String clientIP = req.getRemoteHost();
		Socket socketClient = null;
		resp.setContentType("application/x-java-jnlp-file");
		
		session.setAttribute( requestJnlp.getID() , requestJnlp);
		log.info("Request JNLP of id '"+requestJnlp.getID()+"' added on session '"+session.getId()+"'");
		
		try {
			log.info("Trying to connect to an instance of jWriter on client machine = "+clientIP+":"+writerPort);
			socketClient = new Socket( clientIP, writerPort );
			ObjectOutputStream oos = new ObjectOutputStream( socketClient.getOutputStream() );
			String[] args = toStringArray( arguments );
			Map<String, List<String[]>> argsMap = ArgumentsBuilder.parseArguments( args );
			oos.writeObject( argsMap );
			oos.close();
			
			fillJnlpTemplate(appURL, jarsPath, bootstrapJar, Mock.class.getCanonicalName(), null, resp.getOutputStream());
		} catch ( ConnectException ce ) {
			log.info("Not exists an instance running on client machine");
			
			fillJnlpTemplate(appURL, jarsPath, bootstrapJar, Main.class.getCanonicalName(), arguments, resp.getOutputStream());
		}
		
		resp.getOutputStream().close();
		resp.flushBuffer();
				
	}
	
	private String[] toStringArray (List<String> list) {
		String[] array = new String[list.size()];
		int i = 0;
		for ( String s: list ) {
			array[i++] = s;
		}
		return array;
	}
	
	private void emptyJnlp(ServletOutputStream outputStream) throws IOException {
		IOUtil.copy( JnlpFactoryServlet.class.getClassLoader().getResourceAsStream("mock.jnlp") , outputStream);
		
	}

	private String getHashJar(String jarName) throws IOException {
		String hash = localHashJars.get(jarName);
		if ( hash==null ) {
			throw new IOException("Hash of jar '"+jarName+"' not found on localHashJars map");
		}
		return hash;
	}

	private String getAppURL(HttpServletRequest req) {
//		return "http://" + req.getLocalAddr() + ":" + req.getLocalPort() +  req.getContextPath() + "/";
		return req.getRequestURL().substring( 0, req.getRequestURL().lastIndexOf("/")+1 );
	}

	private void fillJnlpTemplate (String appURL, String jarsPath, String mainJar, String mainClass, List<String> arguments, OutputStream os) throws ServletException {
		log.info("Starting jnlp fill with argments");
		
		VelocityContext context = new VelocityContext();
		context.put("appURL", appURL);
		context.put("jarsPath", jarsPath);
		context.put("mainJar", mainJar+".jar");
		context.put("mainClass", mainClass);
		context.put("arguments", arguments);
		
//		OutputStreamWriter osw = new OutputStreamWriter( os );
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter( baos );
		
		try {
			velocityTemplate.merge(context, osw);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException("Error trying to generate new jnlp with values: appURL="+appURL+", jarsPath="+jarsPath+", mainJar="+mainJar+", mainClass="+mainClass, e);
		}
		
		try {
			osw.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new ServletException("Erroy while trying to finalizy jnlp creation", e);
		}
		
		byte[] bJnlp = baos.toByteArray();
		
		log.debug("JNLP:");
		log.debug(new String(bJnlp));
		
		try {
			os.write( bJnlp );
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new ServletException("Erroy while trying to finalizy jnlp creation", e);
		}
		
		
		
	}

}
