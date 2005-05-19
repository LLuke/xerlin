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
Merlot XML Editor (http://www.merlotxml.org/)."
 
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
(http://www.merlotxml.org/)."

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
http://www.merlotxml.org.
*/


// Copyright 1999 ChannelPoint, Inc., All Rights Reserved.

	package org.merlotxml.merlot;


import java.io.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import java.net.*; 
import javax.help.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

import javax.swing.event.*;

import org.merlotxml.merlot.plugin.PluginClassLoader;
import org.merlotxml.merlot.plugin.action.ActionConfig;
import org.merlotxml.merlot.plugin.action.ActionPluginConfig;
import org.merlotxml.merlot.plugin.nodeAction.NodeActionConfig;
import org.merlotxml.merlot.plugin.nodeAction.NodeActionPluginConfig;

/**
 * 
 * Actions used by the xml editor UI
 * 
 * @author Kelly A. Campbell
 *
 * @version $Id: XMLEditorActions.java,v 1.16 2001/10/09 22:19:01 justin Exp $
 *
 */
public class XMLEditorActions 
    implements MerlotConstants
{
    private XMLEditorFrame _frame;
    
    private static XMLEditorActions _instance = null;
    
    protected QuitAction     _quitAction;
    
    protected SaveFileAction _saveFileAction;
    protected SaveAsAction   _saveAsAction;
    protected CloseFileAction    _closeFileAction;
    protected RevertFileAction   _revertFileAction;
    
    
    protected NewFileAction  _newFileAction;
    protected OpenFileAction _openFileAction;
    protected OpenLibraryAction _openLibraryAction;
    protected NewLibraryAction _newLibraryAction;
	
    
    protected Action _undoAction;
    protected Action _cutAction;
    protected Action _copyAction;
    protected Action _pasteAction;
    protected Action _prefsAction;
    protected Action _backAction;
    protected Action _cancelAction;
	
    protected WindowCascadeAction _windowCascadeAction;
    protected WindowTileHorizontalAction _windowTileHorizontalAction;
    protected WindowTileVerticalAction _windowTileVerticalAction;

    protected PluginLoadAction _pluginLoadAction;
    

    protected Action _aboutAction;
    protected Action _helpAction;
	

    protected Action _systemLFAction;
    protected Action _metalLFAction;
	

    public XMLEditorActions(XMLEditorFrame f) 
    {
		_frame = f;
		initActions();
		_instance = this;
    }

    public static XMLEditorActions getSharedInstance() 
    {
		return _instance;
    }
	
	
    private void initActions() 
    {
		_quitAction     = new QuitAction();

		_saveFileAction = new SaveFileAction();
		_saveAsAction   = new SaveAsAction();
		_revertFileAction = new RevertFileAction();
		_closeFileAction    = new CloseFileAction();
	
		_saveFileAction.setEnabled(false);
		_saveAsAction.setEnabled(false);
		_revertFileAction.setEnabled(false);
		_closeFileAction.setEnabled(false);
	

		_newFileAction  = new NewFileAction();
		_openFileAction = new OpenFileAction();
		_openLibraryAction = new OpenLibraryAction();
		_newLibraryAction = new NewLibraryAction();
		//	_newLibraryAction.setEnabled(false); // XXX not implemented
		
		
		_undoAction     = new UndoAction();
		_cutAction      = new CutAction();
		_copyAction     = new CopyAction();
		_pasteAction    = new PasteAction();
        _backAction     = new BackAction();
        _cancelAction   = new CancelAction();

		

		_pasteAction.setEnabled(false);
		_undoAction.setEnabled(false);
        _backAction.setEnabled(false);
        _cancelAction.setEnabled(false);
		
		
		_prefsAction    = new EditPrefsAction();
		//_prefsAction.setEnabled(false);
		
		_systemLFAction    = new DefaultLFAction();
		_metalLFAction  = new MetalLFAction();
		
		_windowCascadeAction = new WindowCascadeAction();
		_windowTileHorizontalAction = new WindowTileHorizontalAction();
		_windowTileVerticalAction = new WindowTileVerticalAction();

		_windowCascadeAction.setEnabled(false);
		_windowTileHorizontalAction.setEnabled(false);
		_windowTileVerticalAction.setEnabled(false);

		_pluginLoadAction = new PluginLoadAction();
		

		_aboutAction    = new AboutAction();
		
		_helpAction	= new HelpAction();
		
				
    }
	
    protected class QuitAction extends AbstractAction 
    {
	
		public QuitAction () 
		{
			MerlotUtils.loadActionResources(this,UI,"file.quit");
		}
		
		public void actionPerformed (ActionEvent evt) 
		{
			_frame.quit();
		}
    }

    protected class SaveFileAction extends AbstractAction 
    {
		
		public SaveFileAction () 
		{
			MerlotUtils.loadActionResources(this,UI,"file.save");

		}
		
		public void actionPerformed (ActionEvent evt) 
		{
			_frame.save();
		}
    }

    protected class SaveAsAction extends AbstractAction 
    {
		
		public SaveAsAction () 
		{
			MerlotUtils.loadActionResources(this,UI,"file.saveas");

		}
		
		public void actionPerformed (ActionEvent evt) 
		{
			_frame.saveAs();
		}
    }
    protected class CloseFileAction extends AbstractAction 
    {
		
		public CloseFileAction () 
		{
			MerlotUtils.loadActionResources(this,UI,"file.close");

		}
		
		public void actionPerformed (ActionEvent evt) 
		{
			_frame.close();
		}
    }

    protected class RevertFileAction extends AbstractAction 
    {
		
		public RevertFileAction () 
		{
			MerlotUtils.loadActionResources(this,UI,"file.revert");

		}
		
		public void actionPerformed (ActionEvent evt) 
		{
			_frame.revert();
		}
    }

    protected class NewFileAction extends AbstractAction 
    {
		
		public NewFileAction () 
		{
			MerlotUtils.loadActionResources(this,UI,"file.new");

		}
		
		public void actionPerformed (ActionEvent evt) 
		{
			_frame.newFile();
		}
    }

    protected class OpenFileAction extends AbstractAction 
    {
		public OpenFileAction () 
		{
			MerlotUtils.loadActionResources(this,UI,"file.open");

		}
		
		public void actionPerformed (ActionEvent evt) 
		{
			_frame.openFile();
		}
    }

    protected class OpenLibraryAction extends AbstractAction 
    {
		public OpenLibraryAction () 
		{
			MerlotUtils.loadActionResources(this,UI,"library.open");
		}
		
		public void actionPerformed(ActionEvent evt) 
		{
			_frame.openLibrary();
		}
				
    }
    protected class NewLibraryAction extends AbstractAction 
    {
		public NewLibraryAction () 
		{
			MerlotUtils.loadActionResources(this,UI,"library.new");
		}
		
		public void actionPerformed(ActionEvent evt) 
		{
			_frame.newLibrary();
		}
				
    }	
    protected class UndoAction extends AbstractAction 
    {
		public UndoAction () 
		{
			MerlotUtils.loadActionResources(this,UI,"edit.undo");
		
		}
		
		public void actionPerformed (ActionEvent evt) 
		{
			
			_frame.undo(evt);
		}
    }

    protected class BackAction extends AbstractAction
    {
        public BackAction ()
        {
            MerlotUtils.loadActionResources(this,UI,"go.back");

        }

        public void actionPerformed (ActionEvent evt)
        {
            XMLEditorDoc doc = _frame.getCurrentDocument();
            XMLEditorDocUI ui;
            if (doc!=null) {
                ui = doc.getXMLEditorDocUI();

                Vector history = ui.getNodeHistory();
                if (history.size()>1) {
                    MerlotDOMNode toEdit = (MerlotDOMNode)history.elementAt(history.size()-2);
                    history.removeElementAt(history.size()-1);
                    history.removeElementAt(history.size()-1);
                    ui.editNode(toEdit, false);

                    // Failure to save
                    if (!ui._currentNode.equals(toEdit)) {
                        history.add(toEdit);
                        history.add(ui._currentNode);
                    }
                }
            }

        }
    }

    protected class CancelAction extends AbstractAction
    {
        public CancelAction ()
        {
            MerlotUtils.loadActionResources(this,UI,"cancel.panel");

        }

        public void actionPerformed (ActionEvent evt)
        {
            XMLEditorDoc doc = _frame.getCurrentDocument();
            XMLEditorDocUI ui;
            if (doc!=null) {
                ui = doc.getXMLEditorDocUI();
            
                Vector history = ui.getNodeHistory();
                ui._currentNode = null;
                
                if (history.size()>1) {
                    MerlotDOMNode toEdit = (MerlotDOMNode)history.elementAt(history.size()-2);
                    history.removeElementAt(history.size()-1);
                    history.removeElementAt(history.size()-1);
                    ui.editNode(toEdit, false);
                }
            }
        }
    }



    protected class CutAction extends TextAction 
    {
		public CutAction () 
		{
			super("cut");
			MerlotUtils.loadActionResources(this,UI,"edit.cut");


		}
		
		public void actionPerformed (ActionEvent evt) 
		{
            JTextComponent target = getTextComponent(evt);
            if (target != null) {
                target.cut();
				_pasteAction.setEnabled(true);
            }
			else {
				_frame.cut(evt);
			}
			
		}
    }
    protected class CopyAction extends TextAction 
    {
		public CopyAction () 
		{
			super("copy");
			MerlotUtils.loadActionResources(this,UI,"edit.copy");
		}
		
		public void actionPerformed (ActionEvent evt) 
		{
            JTextComponent target = getTextComponent(evt);
            if (target != null) {
                target.copy();
				_pasteAction.setEnabled(true);
			}
			else {
				_frame.copy(evt);
			}
			
		}
    }
    protected class PasteAction extends TextAction 
    {
		public PasteAction () 
		{
			super("paste");
			
			MerlotUtils.loadActionResources(this,UI,"edit.paste");

		}
		
		public void actionPerformed (ActionEvent evt) 
		{
            JTextComponent target = getTextComponent(evt);
            if (target != null) {
                target.paste();
            }
			else {
				_frame.paste(evt);
			}
			
		}
    }
    protected class EditPrefsAction extends AbstractAction 
    {
		public EditPrefsAction () 
		{
			MerlotUtils.loadActionResources(this,UI,"edit.prefs");

		}
		
		public void actionPerformed (ActionEvent evt) 
		{
            JDialog dialog  = _frame._preferenceDialog;
            if (dialog  == null){
                dialog  = new MerlotPreferenceDialog(_frame);
                dialog.pack();

                // figure out where to put this to center it
                Dimension d = _frame.getSize();
                Dimension e = dialog.getSize();
                int x =(int)_frame.getLocation().getX() +  (d.width - e.width )/ 2;
                int y =(int)_frame.getLocation().getY() + (d.height- e.height )/ 2  ;
                dialog.setLocation(x,y);
                dialog.show();
                _frame._preferenceDialog = dialog;
            } else {
                dialog.setVisible(true);
            }
		}
    }

    // Look and feel actions
       
    /**
     * Platform default Look & Feel
     */
    protected class DefaultLFAction extends AbstractAction 
    {
		public DefaultLFAction () 
		{
			putValue(NAME,"Default");
		}
		
		public void actionPerformed (ActionEvent evt) 
		{
			try {
                            String classname = UIManager.getSystemLookAndFeelClassName();
				UIManager.setLookAndFeel(classname);
				SwingUtilities.updateComponentTreeUI(XMLEditorFrame.getSharedInstance());
			}
			catch (Exception ex) {
				MerlotDebug.exception(ex);
			}
			
		}
    }
    
    /**
     * Swing cross platform look and feel
     */
    protected class MetalLFAction extends AbstractAction 
    {
		public MetalLFAction () 
		{
			putValue(NAME,"Metal");
		}
		
		public void actionPerformed (ActionEvent evt) 
		{
			try {
				UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
				SwingUtilities.updateComponentTreeUI(XMLEditorFrame.getSharedInstance());
				
			}
			catch (Exception ex) {
				MerlotDebug.exception(ex);
			}
		}
		
    }
	
    protected class AboutAction extends AbstractAction 
	
    {
	
		public AboutAction () 
		{
			MerlotUtils.loadActionResources(this,UI,"help.about");
			putValue(NAME,MerlotResource.getString(UI,"help.about")+" "+XMLEditorSettings.getSharedInstance().getFrameTitle());
		}
		
		public void actionPerformed (ActionEvent evt) 
		{
			MerlotAbout a = new MerlotAbout(_frame);
			a.run();
			
				    
		}
    }
    
    protected class HelpAction extends AbstractAction 
	
    {
	
		public HelpAction () 
		{
			MerlotUtils.loadActionResources(this,UI,"help.user");
		}
		
		public void actionPerformed (ActionEvent evt) 
		{
			int HELP_WIDTH = 700; 
            int HELP_HEIGHT = 500; 
            String USER_PATH = System.getProperty("user.dir");
            String SEP = System.getProperty("file.separator");
            String HELP_LOC = USER_PATH + SEP + XMLEditorSettings.getSharedInstance().
	        					                            getProperty("merlot.help.file");
            String HELP_DIR = HELP_LOC.substring(0, HELP_LOC.lastIndexOf(SEP));
            String HELP_FILE = HELP_LOC.substring(HELP_LOC.lastIndexOf(SEP)+1, HELP_LOC.length()); 
            String HELP_JAR = XMLEditorSettings.getSharedInstance().getProperty("merlot.help.jar");
                
            PluginClassLoader pcl = null;
            URL hsURL = null;
           
            try { 
            // Load from jar or from directory? 
                if (HELP_JAR!=null) {
                    pcl = new PluginClassLoader(new File(HELP_JAR)); 
                    hsURL = pcl.findResource(HELP_FILE);
                }
                else {
                    pcl = new PluginClassLoader(new File(HELP_DIR));
                    hsURL = HelpSet.findHelpSet(pcl, HELP_FILE);
                }
            } catch (Exception ex)  {
               MerlotDebug.exception(ex);
            }

           try {
                HelpSet hs = new HelpSet(pcl, hsURL);
                    
                Dimension screenSize = 
                Toolkit.getDefaultToolkit().getScreenSize(); 

                int locX = (screenSize.width - HELP_WIDTH) / 2; 
                int locY = (screenSize.height - HELP_HEIGHT) / 2; 

                JFrame frame = new JFrame(); 
                frame.setTitle(MerlotResource.getString(UI,"help.user")); 
                frame.getContentPane().setLayout(new BorderLayout()); 
                frame.getContentPane().add(new JHelp(hs),"Center"); 
                frame.setBounds(locX, locY, HELP_WIDTH, HELP_HEIGHT); 
                frame.setVisible(true); 

    	        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			    
            } catch (Exception ex) { 
            	MerlotDebug.exception(ex); 
            } 
		}
    }    

    protected class WindowTileHorizontalAction extends AbstractAction
    {
		public WindowTileHorizontalAction()
		{
			MerlotUtils.loadActionResources(this,UI,"window.tilehorizontal");
			putValue(NAME,MerlotResource.getString(UI,"window.tilehorizontal"));
		}
	
		public void actionPerformed(ActionEvent evt) 
		{

			JDesktopPane _desktop = _frame.getDesktopPane();
			JInternalFrame[] frames = _desktop.getAllFrames();
			JInternalFrame f = null;
	    
			if (frames.length == 0) {
				// no frames to tile
				// error condition - action should
				// be disabled anyway.
				return;
			}
	    
	    
			int newheight = (int)(_desktop.getSize().getHeight() / ((double)frames.length));
			int newwidth = (int)_desktop.getSize().getWidth();
			int newx = 0;
			int newy = 0;
	    
			for (int i = 0; i < frames.length; i++) {
				f = frames[i];
		
				if ((f.isClosed() == false) && (f.isIcon() == true)) {
					try {
						f.setIcon(false);
					}
					catch (java.beans.PropertyVetoException e) {
						// Ignore exceptions, none should be thrown, besides,
						// no real value in trying to do something with it.
					}
				}
				f.pack();
				_desktop.getDesktopManager().resizeFrame(f, 
														 newx, newy,
														 newwidth, newheight );
				newy += newheight;
			}
	    
		}
    }
    
    protected class WindowTileVerticalAction extends AbstractAction
    {
		public WindowTileVerticalAction()
		{
			MerlotUtils.loadActionResources(this,UI,"window.tilevertical");
			putValue(NAME,MerlotResource.getString(UI,"window.tilevertical"));
		}
	
		public void actionPerformed(ActionEvent evt) 
		{
			JDesktopPane _desktop = _frame.getDesktopPane();
			JInternalFrame[] frames = _desktop.getAllFrames();
			JInternalFrame f = null;
	    
			if (frames.length == 0) {
				// no frames to tile
				// error condition - action should
				// be disabled anyway.
				return;
			}
	    
	    
			int newheight = (int)(_desktop.getSize().getHeight());
			int newwidth = (int)(_desktop.getSize().getWidth() / ((double)frames.length));
			int newx = 0;
			int newy = 0;
	    
			for (int i = 0; i < frames.length; i++) {
				f = frames[i];
		
				if ((f.isClosed() == false) && (f.isIcon() == true)) {
					try {
						f.setIcon(false);
					}
					catch (java.beans.PropertyVetoException e) {
						// Ignore exceptions, none should be thrown, besides,
						// no real value in trying to do something with it.
					}
				}
				f.pack();
				_desktop.getDesktopManager().resizeFrame(f, 
														 newx, newy,
														 newwidth, newheight );
				newx += newwidth;
			}
		}
    }
    
    protected class WindowCascadeAction extends AbstractAction
    {
		public WindowCascadeAction()
		{
			MerlotUtils.loadActionResources(this,UI,"window.cascade");
			putValue(NAME,MerlotResource.getString(UI,"window.cascade"));
		}
	
		public void actionPerformed(ActionEvent evt)
		{
	    
			JDesktopPane _desktop = _frame.getDesktopPane();
			JInternalFrame[] frames = _desktop.getAllFrames();
			JInternalFrame f = null;
	    
			int STAGGER_X = 25;
			int STAGGER_Y = 25;
	    
			// maximum amounts of staggering before cycling back to the start
			int MAX_STAGGER_X = 200;
			int MAX_STAGGER_Y = 150;
	    
			// amount to offset staggers when recycling
			int STAGGER_OFFSET = 10;
	    
			// current values of staggering
			int _stagger_x = 0;
			int _stagger_y = 0;
			int _stagger_x_start = 0;
			int _stagger_y_start = 0;
	    
			for (int i = 0; i < frames.length; i++) {
				f = frames[i];
		
				if ((f.isClosed() == false) && (f.isIcon() == true)) {
					try {
						f.setIcon(false);
					}
					catch (java.beans.PropertyVetoException e) {
						// Ignore exceptions, none should be thrown, besides,
						// no real value in trying to do something with it.
					}
				}
				f.pack();
				_stagger_x += STAGGER_X;
				_stagger_y += STAGGER_Y;
				if (_stagger_x > MAX_STAGGER_X) {
					_stagger_x = (_stagger_x_start += STAGGER_OFFSET);
				}
				if (_stagger_y > MAX_STAGGER_Y) {
					_stagger_y = (_stagger_y_start += STAGGER_OFFSET);
				}
				f.setLocation(new Point(_stagger_x, _stagger_y));
		
				// Ensure the internal frame is smaller than the desktop
				Dimension desktopsize = _desktop.getSize();
				int newwidth = (int)((double)desktopsize.getWidth() * 0.75);
				int newheight = (int)((double)desktopsize.getHeight() * 0.75); 
		
				// sanity check
				if (newwidth < 0) { 
					newwidth = 100;
				}
				if (newheight < 0) {
					newheight = 100;
				}
				_desktop.getDesktopManager().resizeFrame(f, 
														 _stagger_x, _stagger_y,
														 newwidth, newheight );
			}
		}
    }
    
    public class PluginLoadAction extends AbstractAction 
    {
	public PluginLoadAction()
	{
	    MerlotUtils.loadActionResources(this,UI,"plugin.load");
	    putValue(NAME,MerlotResource.getString(UI,"plugin.load"));
	    // XXX disabled until implemented
	    this.setEnabled(false);
	    
	}
	
	public void actionPerformed (ActionEvent evt ) 
	{
	    
	    
	}
	
    }
	

	public class PluginFireAction implements ActionListener {
		
    protected ActionConfig _config;
		
    public PluginFireAction(ActionConfig config) {
			_config = config;
		}
		
			
    public void actionPerformed(ActionEvent event) {
				try {
	_config.getAction().performAction(_frame.getCurrentDocument().getDocument());
				}
      catch (NullPointerException e) {
	System.err.println("Ohhh, NullPointerException. Do you have some file opened?");
			}
		}
	}
	
	public class PluginFireNodeAction implements ActionListener 
	{
		protected NodeActionConfig _config;
		protected MerlotDOMNode _node;
		
		public PluginFireNodeAction( NodeActionConfig config, MerlotDOMNode node )
		{
			_config = config;
			_node = node;
		}
					
		public void actionPerformed(ActionEvent event)
		{
			_config.getNodeAction().performAction( _node );
		}
	}
}
