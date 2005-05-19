/*
====================================================================
Copyright (c) 1999-2000 ChannelPoint, Inc..  All rights reserved.
====================================================================

Redistribution and use in source and binary forms, with or without 
modification, are permitted provided that the following conditions 
are met:

1. Redistribution of source code must retain the above copyright 
notice, this list of conditions and the following disclaimer. 

2. Redistribution in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the 
documentation and/or other materials provided with the distribution.

3. All advertising materials mentioning features or use of this 
software must display the following acknowledgment:  "This product 
includes software developed by ChannelPoint, Inc. for use in the 
Merlot XML Editor (http://www.channelpoint.com/merlot/)."
 
4. Any names trademarked by ChannelPoint, Inc. must not be used to 
endorse or promote products derived from this software without prior
written permission. For written permission, please contact
legal@channelpoint.com.

5.  Products derived from this software may not be called "Merlot"
nor may "Merlot" appear in their names without prior written
permission of ChannelPoint, Inc.

6. Redistribution of any form whatsoever must retain the following
acknowledgment:  "This product includes software developed by 
ChannelPoint, Inc. for use in the Merlot XML Editor 
(http://www.channelpoint.com/merlot/)."

THIS SOFTWARE IS PROVIDED BY CHANNELPOINT, INC. "AS IS" AND ANY EXPRESSED OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO 
EVENT SHALL CHANNELPOINT, INC. OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND 
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF 
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ====================================================================

For more information on ChannelPoint, Inc. please see http://www.channelpoint.com.  
For information on the Merlot project, please see 
http://www.channelpoint.com/merlot.
*/

// Copyright 1999 ChannelPoint, Inc., All Rights Reserved.

package org.merlotxml.merlot;

import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.MissingResourceException;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;

import matthew.awt.StrutLayout;

/**
 * 
 * Utilities for Merlot
 * 
 * @author Kelly A. Campbell
 *
 */
public class MerlotUtils implements MerlotConstants {

	/**
	 * Add an action to a toolbar
	 */

	public static void addActionToToolBar(Action a, JToolBar toolbar) {
		addActionToToolBar(a, toolbar, toolbar.getComponentCount());
	}

	/**
	 * Add an action to a toolbar at a specific location
	 * if the action is null a separator is added
	 */

	public static void addActionToToolBar(
		Action a,
		JToolBar toolbar,
		int position) {
		JButton b = null;
		// If there's an Action add it otherwise assume a Separator is required
		if (a != null) {
			b = createButtonFromAction(a);

			// setAction was added in JDK 1.3
			//  b.setAction(a);
			b.setText(null);
			b.setMargin(new Insets(3, 3, 3, 3));
			b.setBorderPainted(false);
			toolbar.add(b, position);
		} else {
			JToolBar.Separator js = new JToolBar.Separator();
			toolbar.add(js, position);
		}
		toolbar.revalidate();
	}

	/**
	 * add an action to a menu. Swing sucks
	 */

	public static void addActionToMenu(Action a, MenuElement m) {
		JMenuItem mi = createActionMenuItem(a);
		//		MerlotDebug.msg("addActionToMenu("+a+", "+m+")");
		if (m instanceof JMenu) {
			((JMenu) m).add(mi);
		} else if (m instanceof JPopupMenu) {
			((JPopupMenu) m).add(mi);
		}
	}

	/**
	 * Add an action to a menu at a specific position
	 */
	public static void addActionToMenu(Action a, MenuElement m, int position) {
		JMenuItem mi = createActionMenuItem(a);
		//      MerlotDebug.msg("addActionToMenu("+a+", "+m+")");
		if (m instanceof JMenu) {
			((JMenu) m).add(mi, position);
		} else if (m instanceof JPopupMenu) {
			((JPopupMenu) m).insert(mi, position);
		}
	}

	public static final String MNEMONIC_KEY = "MnemonicKey";

	public static JMenuItem createActionMenuItem(Action a) {
		JMenuItem mi = new JMenuItem();
		if (a != null) {
			mi.setText((String) a.getValue(Action.NAME));
			mi.setIcon((Icon) a.getValue(Action.SMALL_ICON));
			mi.setEnabled(a.isEnabled());
			mi.setToolTipText((String) a.getValue(Action.SHORT_DESCRIPTION));
			mi.setIcon((ImageIcon) a.getValue(ACTION_MENU_ICON));
			mi.setAccelerator((KeyStroke) a.getValue(ACTION_MENU_ACCELERATOR));

			Integer i = (Integer) a.getValue(MNEMONIC_KEY);
			if (i != null) {
				mi.setMnemonic(i.intValue());
			}

			mi.addActionListener(a);
			a.addPropertyChangeListener(
				new ButtonActionPropertyChangeListener(mi));
		}
		return mi;

	}

	/**
	 * loads up the resources for an action
	 */
	public static void loadActionResources(
		Action a,
		String bundle,
		String keyprefix) {
		// name
		try {
			a.putValue(
				ACTION_NAME,
				MerlotResource.getString(bundle, keyprefix));
		} catch (MissingResourceException ex) {
		}

		// icon
		try {
			a.putValue(
				ACTION_SMALL_ICON,
				MerlotResource.getImage(bundle, keyprefix + ".icon"));
		} catch (MissingResourceException ex) {
		}

		// tooltip text
		try {
			a.putValue(
				ACTION_SHORT_DESCRIPTION,
				MerlotResource.getString(bundle, keyprefix + ".tt"));
		} catch (MissingResourceException ex) {
		}

		try {
			a.putValue(
				ACTION_MENU_ICON,
				MerlotResource.getImage(bundle, keyprefix + ".micon"));
		} catch (MissingResourceException ex) {
		}

		try {
			a.putValue(
				ACTION_MENU_ACCELERATOR,
				MerlotResource.getKeyStroke(bundle, keyprefix + ".accel"));
		} catch (MissingResourceException ex) {
		}

	}

	/**
	 * copy a file to a different file
	 */
	public static void copyFile(File from, File to) throws IOException {
		byte[] buffer = new byte[256];
		FileInputStream is = new FileInputStream(from);
		FileOutputStream os = new FileOutputStream(to);
		int i;

		while ((i = is.read(buffer)) >= 0) {
			os.write(buffer, 0, i);

		}
		is.close();
		os.close();

	}

	public static class ToolbarButtonMouseListener extends MouseAdapter {
		protected JButton _button;

		public ToolbarButtonMouseListener(JButton b) {
			_button = b;
		}

		public void mouseEntered(MouseEvent evt) {
			if (_button.isEnabled()) {
				_button.setBorderPainted(true);
			}

		}

		public void mouseExited(MouseEvent evt) {
			_button.setBorderPainted(false);
		}

	}

	public static JButton createButtonFromAction(Action a) {
		JButton b =
			new JButton(
				(String) a.getValue(Action.NAME),
				(Icon) a.getValue(Action.SMALL_ICON));

		String s = (String) a.getValue(Action.SHORT_DESCRIPTION);
		if (s != null) {
			b.setToolTipText(s);
		}

		b.setHorizontalTextPosition(JButton.CENTER);
		b.setVerticalTextPosition(JButton.BOTTOM);
		b.setEnabled(a.isEnabled());
		b.addActionListener(a);
		b.addMouseListener(new ToolbarButtonMouseListener(b));
		a.addPropertyChangeListener(new ButtonActionPropertyChangeListener(b));
		return b;

	}

	/**
	 * Wraps lines at the given number of columns
	 */
	public static String wrapLines(String s, int cols) {
		//StringBuffer sb = new StringBuffer();
		char[] c = s.toCharArray();
		char[] d = new char[c.length];

		int i = 0;
		int j = 0;
		int lastspace = -1;
		while (i < c.length) {
			if (c[i] == '\n') {
				j = 0;
			}
			if (j > cols && lastspace > 0) {
				d[lastspace] = '\n';
				j = i - lastspace;
				lastspace = -1;
			}
			if (c[i] == ' ') {
				lastspace = i;
			}
			d[i] = c[i];
			i++;
			j++;
		}
		String ret = new String(d);
		return ret;
	}

	/**
	 * creates a wrapped mulit-line label from several labels
	 */
	public static JPanel createMultiLineLabel(String s, int cols) {
		boolean done = false;
		String msg = wrapLines(s, cols);
		char[] c = msg.toCharArray();
		int i = 0;
		StringBuffer sb = new StringBuffer();

		StrutLayout lay = new StrutLayout();
		JPanel p = new JPanel(lay);
		JLabel l, oldl = null;
		StrutLayout.StrutConstraint strut;

		while (!done) {
			if (i >= c.length || c[i] == '\n') {
				l = new JLabel(sb.toString());
				sb = new StringBuffer();

				if (oldl == null) {
					strut = null;
					p.add(l);
				} else {
					strut =
						new StrutLayout.StrutConstraint(
							oldl,
							StrutLayout.BOTTOM_LEFT,
							StrutLayout.TOP_LEFT,
							StrutLayout.SOUTH,
							1);
					p.add(l, strut);
				}
				oldl = l;
				if (i >= c.length) {
					done = true;
				}

			} else {
				sb.append(c[i]);
			}
			i++;

		}
		return p;

	}

	private static class ButtonActionPropertyChangeListener
		implements PropertyChangeListener {
		AbstractButton _button;

		ButtonActionPropertyChangeListener(AbstractButton b) {
			_button = b;
		}

		public void propertyChange(PropertyChangeEvent e) {
			String propertyName = e.getPropertyName();
			if (e.getPropertyName().equals(Action.NAME)) {
				String text = (String) e.getNewValue();
				_button.setText(text);
				_button.repaint();
			} else if (e.getPropertyName().equals(Action.SHORT_DESCRIPTION)) {
				String text = (String) e.getNewValue();
				_button.setToolTipText(text);
			} else if (propertyName.equals("enabled")) {
				Boolean enabledState = (Boolean) e.getNewValue();
				_button.setEnabled(enabledState.booleanValue());
				_button.repaint();
			} else if (e.getPropertyName().equals(Action.SMALL_ICON)) {
				Icon icon = (Icon) e.getNewValue();
				_button.setIcon(icon);
				_button.invalidate();
				_button.repaint();
			} else if (e.getPropertyName().equals(MNEMONIC_KEY)) {
				Integer mn = (Integer) e.getNewValue();
				_button.setMnemonic(mn.intValue());
				_button.invalidate();
				_button.repaint();
			}
		}
	}

}
