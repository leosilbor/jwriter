package com.jwriter.core.plugin;

import com.sun.star.awt.XWindow;
import com.sun.star.beans.PropertyValue;
import com.sun.star.comp.beans.OOoBean;
import com.sun.star.frame.XDispatchHelper;
import com.sun.star.frame.XDispatchProvider;
import com.sun.star.frame.XFrame;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

public class Macro {
	
	public enum CursorDirection {
		UP(".uno:GoUp"), DOWN(".uno:GoDown"), LEFT(".uno:GoLeft"), RIGHT(".uno:GoRight");
		private String uno;
		private CursorDirection (String uno) {
			this.uno = uno;
		}
		public String getUno () {
			return this.uno;
		}
	}
	
	private OOoBean bean;
	private XDispatchHelper xDh;
	private XDispatchProvider xDispatchProvider;
	private XWindow xWindow;
	
	
	public Macro (OOoBean bean) {
		this.bean = bean;
	}

	public void insertSection (String nome, boolean protect) throws Exception {
		PropertyValue[] args1 = getProperties(7);
		
		args1[0].Name = "RegionName";
		args1[0].Value = nome;
		args1[1].Name = "RegionCondition";
		args1[1].Value = "";
		args1[2].Name = "RegionHidden";
		args1[2].Value = false;
		args1[3].Name = "RegionProtect";
		args1[3].Value = protect;
		args1[4].Name = "LinkName";
		args1[4].Value = "";
		args1[5].Name = "FilterName";
		args1[5].Value = "";
		args1[6].Name = "SubRegion";
		args1[6].Value = "";

		executeDispatch( ".uno:InsertSection", args1);
	}

	public void walkCursor (CursorDirection direction, boolean select) throws Exception {
		PropertyValue[] args2 = getProperties(2);
		
		args2[0].Name = "Count";
		args2[0].Value = 1;
		args2[1].Name = "Select";
		args2[1].Value = select;

		executeDispatch(direction.getUno(), args2);
	}
	
	private PropertyValue[] getProperties(int tam) {
		PropertyValue[] props = new PropertyValue[tam];
		for ( int i=0 ; i<tam ; i++ ) {
			props[i] = new PropertyValue();
		}
		return props;
	}
	
	private void executeDispatch(String uno, PropertyValue[] props) throws Exception {
		if (props == null) {
			props = getProperties(0);
		}
		
		init();
		
		// focus on broffice, necessary!
		xWindow.setFocus();
		xDh.executeDispatch(xDispatchProvider, uno, "", 0, props);
	}

	private void init() throws Exception {
		if ( xDh==null || xDispatchProvider==null || xWindow==null ) {
			XComponentContext xCc = bean.getOOoConnection().getComponentContext();
			XFrame xFrame = bean.getDocument().getCurrentController().getFrame();
			Object dispatchHelperObject = xCc.getServiceManager().createInstanceWithContext("com.sun.star.frame.DispatchHelper", xCc);
			xDh = (XDispatchHelper) UnoRuntime.queryInterface(XDispatchHelper.class, dispatchHelperObject);
			xDispatchProvider = (XDispatchProvider) UnoRuntime.queryInterface(XDispatchProvider.class, xFrame);		
			xWindow = xFrame.getComponentWindow();
		}
		
	}
	
	
}
