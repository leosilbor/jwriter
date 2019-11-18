package com.jwriter.editor.plugin;

import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.jwriter.bootstrap.plugin.PluginException;
import com.jwriter.bootstrap.service.BootstrapContext;
import com.jwriter.bootstrap.util.IOUtil;
import com.jwriter.bootstrap.util.UIUtil;
import com.jwriter.core.argument.BasicArguments;
import com.jwriter.core.model.DocumentType;
import com.jwriter.core.plugin.AbstractWriter;
import com.sun.star.beans.PropertyValue;
import com.sun.star.comp.beans.NoConnectionException;
import com.sun.star.comp.beans.OfficeDocument;

public class EditorWriter extends AbstractWriter<BasicArguments> implements WindowListener, ActionListener {
	private JFrame window;

	public EditorWriter(String pathSoffice) {
		super(pathSoffice, BasicArguments.class);
	}
	
	protected void start() throws PluginException {
		byte[] doc = null;
		try {
			doc = getDocument();
		} catch (Exception e) {
			throw new PluginException(BootstrapContext.getInstance().getMessages().get("error.document.msg"), e);
		}
		
		if ( doc==null ) {
			throw new PluginException(BootstrapContext.getInstance().getMessages().get("error.documentempty.msg"));
		}
		
		try {
			createUI();
		} catch (IOException e) {
			throw new PluginException(BootstrapContext.getInstance().getMessages().get("error.create.window"), e);
		}
		
		try {
			openDocument(doc);
		} catch (Exception e) {
			throw new PluginException(BootstrapContext.getInstance().getMessages().get("error.open.document"), e);
		}
		
	}

	protected File openDocument(byte[] doc) throws PluginException {
		DocumentType docType = getArguments().getDocType();
		File fDoc = null;
		try {
			fDoc = File.createTempFile("writer_temp_doc_", "."+docType.getExtension());
		} catch ( IOException e ) {
			throw new PluginException("Error while trying to create temporary file", e);
		}
		fDoc.deleteOnExit();
		try {
			IOUtil.copy(new ByteArrayInputStream(doc), new FileOutputStream(fDoc));
		} catch (IOException e) {
			throw new PluginException("Error while trying to copy document data ("+doc.length+") to local file ("+fDoc.getAbsolutePath()+")", e);
		}
		PropertyValue[] propsLoad = new PropertyValue[1];
		propsLoad[0] = new PropertyValue();
		propsLoad[0].Name = "Filter";
		propsLoad[0].Value = docType.getFilter();
		String fPath = fDoc.getAbsolutePath().replace("\\", "/");
		try {
			getOOoBean().loadFromURL( "file:///"+fPath , propsLoad);
			getOOoBean().aquireSystemWindow();
			getOOoBean().requestFocus();
		} catch ( Exception e ) {
			throw new PluginException("Error while trying to open documento. Temporary file on '"+fPath+"'", e);
		}
		return fDoc;
	}

	private void createUI() throws IOException {
		window = new JFrame( BootstrapContext.getInstance().getTitle()+" - "+getArguments().getDocName() );
		UIUtil.setIcon(window);
		getOOoBean().setAllBarsVisible(false);
		getOOoBean().setStatusBarVisible(true);
		MenuBar menuBar = createMenuBar();
		window.setMenuBar(menuBar);
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		UIUtil.setCenterLocation(window);
		window.setExtendedState(JFrame.MAXIMIZED_BOTH);
		window.addWindowListener(this);
		window.add(getOOoBean());
		window.setVisible(true);
	}

	private MenuBar createMenuBar() {
		MenuBar menu = new MenuBar();
		Menu fileMenu = new Menu(BootstrapContext.getInstance().getMessages().get("menu.label.file"));
		MenuItem saveItem = new MenuItem(BootstrapContext.getInstance().getMessages().get("menu.label.save"));
		saveItem.addActionListener( this );
		MenuItem closeItem = new MenuItem(BootstrapContext.getInstance().getMessages().get("menu.label.close"));
		closeItem.addActionListener( this );
		fileMenu.add( saveItem );
		fileMenu.add( closeItem );
		menu.add( fileMenu );
		return menu;
	}

	private byte[] getDocument() throws Exception {
		return (byte[]) sendRequestMessage("getDocument");
	}
	
	private void closeDocument() {
		if ( isDocumentModified() ) {
			suggestSave();
		} else {
			stop();
		}
		
	}

	private void suggestSave() {
		String[] option = new String[] { BootstrapContext.getInstance().getMessages().get("dialog.label.save"), BootstrapContext.getInstance().getMessages().get("dialog.label.discart"), BootstrapContext.getInstance().getMessages().get("dialog.label.cancel") };

		int op = JOptionPane.showOptionDialog(window,
				BootstrapContext.getInstance().getMessages().get("dialog.question.msg"),
				BootstrapContext.getInstance().getTitle(), JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, option, BootstrapContext.getInstance().getMessages().get("dialog.label.save"));

		switch (op) {
			case JOptionPane.YES_OPTION: {
				try {
					saveDocument();
				} catch (Exception e) {
					e.printStackTrace();
					UIUtil.showError(window, BootstrapContext.getInstance().getTitle(), e.getMessage());
					return;
				}
				stop();
				break;
			}
			case JOptionPane.NO_OPTION: {
				stop();
				break;
			}
			case JOptionPane.CANCEL_OPTION: {
				break;
			}
		}
		
	}
	
	private void saveDocument() throws Exception {
		byte[] newDoc = saveReturnDocument();
		sendRequestMessage("saveDocument", newDoc);
		
	}

	private byte[] saveReturnDocument() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		getOOoBean().storeToStream( baos , null);
		return baos.toByteArray();
	}

	private boolean isDocumentModified() {
		OfficeDocument doc = null;
		try {
			doc = getOOoBean().getDocument();
		} catch (NoConnectionException e) {
			return false;
		}
		try {
			return doc.isModified();
		} catch (Throwable t) {
			return false;
		}
	}
	
	public void actionPerformed(ActionEvent evt) {
		
		String com = evt.getActionCommand();
		
		if ( BootstrapContext.getInstance().getMessages().get("menu.label.close").equals( com ) ) {
			closeDocument();
		} else if ( BootstrapContext.getInstance().getMessages().get("menu.label.save").equals( com ) ) {
			try {
				saveDocument();
				UIUtil.showInfo(window, BootstrapContext.getInstance().getTitle(), BootstrapContext.getInstance().getMessages().get("save.success.msg"));
			} catch (Exception e) {
				e.printStackTrace();
				UIUtil.showError(window, BootstrapContext.getInstance().getTitle(), e.getMessage());
				return;
			}
		}
		
		
	}


	public void windowOpened(WindowEvent evt) {
	}
	public void windowClosing(WindowEvent evt) {
		closeDocument();
	}
	public void windowClosed(WindowEvent evt) {
	}
	public void windowIconified(WindowEvent evt) {
	}
	public void windowDeiconified(WindowEvent evt) {
	}
	public void windowActivated(WindowEvent evt) {
	}
	public void windowDeactivated(WindowEvent evt) {
	}

	public void stop() {
		try { getOOoBean().stopOOoConnection(); } catch ( Throwable e ) { e.printStackTrace(); }
		if ( window!=null ) { try { window.dispose(); } catch ( Throwable e ) { e.printStackTrace(); } }
		
	}

}
