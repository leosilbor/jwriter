package com.jwriter.bootstrap.config;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import com.jwriter.bootstrap.util.MessagesUtil;
import com.jwriter.bootstrap.util.UIUtil;

public class UserConfigManager {
	private static final Logger log = Logger.getLogger(UserConfigManager.class);
	private static final int MAX_RECURSIVE_DEPH_LOOK = 5;
	
	public static UserConfig getUserConfig(MessagesUtil messages, String pathWriter, String title) throws ConfigException {
		String pathConfig = pathWriter+"/jwriter.data";
		
		File fileConfig = new File(pathConfig);
		
		log.info("Starting user configuration on: "+pathConfig);
		
		UserConfig userConfig = null;
		
		if ( fileConfig.exists() ) {
			log.info("The file configuration already existis, parsing values to memory");
			
			InputStream isConfig = null;
			try {
				isConfig = new FileInputStream(fileConfig);
			} catch (FileNotFoundException e) {
				throw new ConfigException("Error trying to open stream to read user configuration on '"+pathConfig+"'", e);
			}
			
			userConfig = UserConfig.fromStream(isConfig);
		}
		
		if ( userConfig==null || !userConfig.hasAllPaths() ) {
			log.info("The local file configuration does not exists or is invalid");
			log.info("Creating new file configuration");
			userConfig = loadInitialConfigurations(messages, fileConfig, title);
		}
		
		return userConfig;
	}

	private static UserConfig loadInitialConfigurations(MessagesUtil messages, File fileConfig, String title) throws ConfigException {
		UserConfig userConfig = new UserConfig();
		
		log.info("Looking for open office instalation path");
		askForOpenOfficeInstalation(messages, userConfig, title);
		log.info("Saving new configuration file");
		userConfig.toStream(fileConfig);
		
		return userConfig;
		
	}

	private static void askForOpenOfficeInstalation(final MessagesUtil messages, UserConfig userConfig, final String title) throws ConfigException {
		final JFrame chooserWindow = new JFrame( messages.get("ooi.chooser.title") );
		JFileChooser chooser = new JFileChooser();
		
		UIManager.put("FileChooser.lookInLabelText", messages.get("ooi.chooser.lookin"));
		UIManager.put("FileChooser.openButtonText", messages.get("ooi.chooser.open"));
		UIManager.put("FileChooser.cancelButtonText", messages.get("ooi.chooser.cancel"));
		UIManager.put("FileChooser.fileNameLabelText", messages.get("ooi.chooser.filename"));
		UIManager.put("FileChooser.filesOfTypeLabelText", messages.get("ooi.chooser.filetype"));
		SwingUtilities.updateComponentTreeUI(chooser);
		
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.addActionListener( new ChooserActionListener(chooserWindow, messages, userConfig, title) );
		chooserWindow.add( chooser );
		chooserWindow.pack();
		
		chooserWindow.addWindowListener(new WindowListener() {
			public void windowOpened(WindowEvent e) {
			}
			public void windowIconified(WindowEvent e) {
			}
			public void windowDeiconified(WindowEvent e) {
			}
			public void windowDeactivated(WindowEvent e) {
			}
			public void windowClosing(WindowEvent e) {
				log.info("Cancel option selected");
				if ( UIUtil.showDialog(chooserWindow, title, messages.get("confirm.cancel") )==JOptionPane.YES_OPTION ) {
					System.exit(0);
				}
			}
			public void windowClosed(WindowEvent e) {
			}
			public void windowActivated(WindowEvent e) {
			}
		});
		
		log.info("Showing chooser dialog");
		chooserWindow.setVisible(true);
		
		UIUtil.showInfo(chooserWindow, title, messages.get("ooi.chooser.title"));
		
		synchronized (chooserWindow) {
			try {
				chooserWindow.wait();
			} catch (InterruptedException e) {
				throw new ConfigException("Error while waiting for user selection", e);
			}
		}
	}
	
	private static class ChooserActionListener implements ActionListener {
		private final Logger log = Logger.getLogger(ChooserActionListener.class);
		private UserConfig userConfig;
		private JFrame window;
		private MessagesUtil messages;
		private String title;
		
		public ChooserActionListener (JFrame window, MessagesUtil messages, UserConfig userConfig, String title) {
			this.userConfig = userConfig;
			this.window = window;
			this.messages = messages;
			this.title = title;
		}

		public void actionPerformed(ActionEvent evt) {
			if ( JFileChooser.APPROVE_SELECTION.equals( evt.getActionCommand() ) ) {
				Window modal = UIUtil.showModalPane(window, messages.get("looking.jar.msg"));
				JFileChooser chooser = (JFileChooser) evt.getSource();
				File selectedFolder = chooser.getSelectedFile();
				log.info("Folder selected: "+selectedFolder.getAbsolutePath());
				log.info("Looking for open office jars");
				try {
					searchFiles(0, selectedFolder, userConfig);
				} catch (ConfigException e) {
					log.error("Error while searching for jar files", e);
				}
				UIUtil.hideModalPane(window, modal);
				if ( !userConfig.hasAllPaths() ) {
					log.info("The jars were not found on especified folder");
					UIUtil.showError(window, "Error", messages.get("invalid.instalation.path"));
				} else {
					log.info("Jars were found, closing window");
					synchronized (window) {
						window.notify();
					}
					window.dispose();
				}
			} else if ( JFileChooser.CANCEL_SELECTION.equals( evt.getActionCommand() ) ) {
				log.info("Cancel option selected");
				if ( UIUtil.showDialog(window, title, messages.get("confirm.cancel") )==JOptionPane.YES_OPTION ) {
					System.exit(0);
				}
			}
		}

		private void searchFiles(int high, File actualFile, UserConfig userConfig) throws ConfigException {
			if ( actualFile.isDirectory() && high<MAX_RECURSIVE_DEPH_LOOK ) {
				File[] subFiles = actualFile.listFiles();
				high++;
				for ( File file: subFiles ) {
					searchFiles(high, file, userConfig);
				}
			} else if ( actualFile.isFile() ) {
				String actualFileName = actualFile.getName();
				if ( actualFileName.equalsIgnoreCase( UserConfig.SOFFICE_NAME ) || actualFileName.equalsIgnoreCase( UserConfig.SOFFICE_NAME+".exe" ) ) {
					userConfig.setPathSoffice( actualFile.getAbsolutePath() );
					log.info("Soffice path '"+UserConfig.SOFFICE_NAME+"' found on folder '"+actualFile.getAbsolutePath()+"'");
					return;
				}
				for ( String jarName: UserConfig.JARS_NAMES ) {
					if ( actualFileName.equalsIgnoreCase( jarName ) ) {
						log.info("Jar '"+jarName+"' found on folder '"+actualFile.getAbsolutePath()+"'");
						userConfig.setJarPath(jarName, actualFile);
						return;
					}
				}
			}
			
		}
		
	}
	
}
