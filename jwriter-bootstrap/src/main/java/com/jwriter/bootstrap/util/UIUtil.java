package com.jwriter.bootstrap.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Label;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

public class UIUtil {
	private static final String ICON_PATH = "icon.png";
	
	public static void setSystemLookAndFeel () throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	}
	public static void showError (JFrame window, String title, String msg) {
		showMsg( window, title, msg, JOptionPane.ERROR_MESSAGE);
	}
	public static void showInfo(JFrame window, String title, String msg) {
		showMsg( window, title, msg, JOptionPane.INFORMATION_MESSAGE);
	}
	private static void showMsg (JFrame window, String title, String msg, int type) {
		JOptionPane.showMessageDialog(window, msg, title, type);
	}
	public static int showDialog(JFrame window, String title, String msg) {
		return JOptionPane.showConfirmDialog(window, msg, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
	}
	public static void createTrayIcon(final String title, final String closeMsg, final String exitMsg) {
		new Thread(new Runnable() {
			public void run() {
				final Display display = new Display();
				final Shell shell = new Shell (display);
				final Image image = new Image (display, UIUtil.class.getClassLoader().getResourceAsStream(ICON_PATH));
				final Tray tray = display.getSystemTray ();
				final TrayItem item = new TrayItem (tray, SWT.NONE);
				item.setToolTipText(title);
				final Menu menu = new Menu (shell, SWT.POP_UP);
				MenuItem mi = new MenuItem (menu, SWT.PUSH);
				mi.setText (closeMsg);
				mi.addListener (SWT.Selection, new Listener () {
					public void handleEvent (Event event) {
						MessageBox dialog = new MessageBox(shell, SWT.OK | SWT.CANCEL | SWT.ICON_QUESTION);
						dialog.setText(title);
						dialog.setMessage(exitMsg);
						if (event.type == SWT.Close) event.doit = false;
						if (dialog.open() != SWT.OK) return;
						display.dispose();
						System.exit(0);
					}
				});
				item.addListener (SWT.MenuDetect, new Listener () {
					public void handleEvent (Event event) {
						menu.setVisible (true);
					}
				});
				item.setImage (image);
				while (!shell.isDisposed ()) {
					if (!display.readAndDispatch ()) display.sleep ();
				}
				image.dispose ();
				
			}
		}).start();
	}
	public static void setIcon(JFrame window) throws IOException {
		byte[] image = IOUtil.toByteArray( UIUtil.class.getClassLoader().getResourceAsStream(ICON_PATH) );
		window.setIconImage( Toolkit.getDefaultToolkit().createImage( image ) );
	}
	public static void setCenterLocation(Window window) {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		window.setLocation( dim.width/2 - window.getWidth()/2 , dim.height/2 - window.getHeight()/2);
	}
	public static void setCenterLocation(Window window, JFrame parent) {
		Point parentLocation = parent.getLocation();
		Dimension parentDim = parent.getSize();
		Dimension windowDim = window.getSize();
		window.setLocation( parentLocation.x + parentDim.width/2 - windowDim.width/2 , parentLocation.y + parentDim.height/2 - windowDim.height/2  );
	}
	public static void main(String[] args) {
		JFrame parent = new JFrame();
		parent.setSize(500,500);
		parent.setLocation(500,500);
		parent.setVisible(true);
		UIUtil.showModalPane(parent, "salcando documento");
	}
	public static Window showModalPane (JFrame parent, String msg) {
		Window w = new Window(parent);
		w.setLayout(new BorderLayout());
		w.setBackground( new Color(212,208,200) );
		
		Label lMsg = new Label(msg);
		lMsg.setFont( new Font(null,Font.BOLD, 17) );
		w.add( lMsg, BorderLayout.CENTER );
		w.pack();
		
		if ( parent!=null ) {
			setCenterLocation(w, parent);
			parent.setEnabled(false);
		} else {
			setCenterLocation(w);
		}
		
		w.setVisible(true);
		return w;
	}
	public static void hideModalPane (JFrame parent, Window modal) {
		modal.dispose();
		if ( parent!=null ) {
			parent.setEnabled(true);
		}
	}
}
