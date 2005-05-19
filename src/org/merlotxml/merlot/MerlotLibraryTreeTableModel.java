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

import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import matthew.awt.StrutLayout;

/**
 * 
 * Tree model for the library. A library document consists of sections (shelves)
 * which contain library elements.
 *
 * 
 * 
 * @author Kelly A. Campbell
 *
 */
public class MerlotLibraryTreeTableModel  extends DOMTreeTableAdapter

{

	private MerlotDOMElement _libroot = null;
	private MerlotLibrary _library;
	
	public static AddNodeLaterRunnable _later = null;
	public static Thread               _laterThread = null;
	

	protected static final int TYPE_COL = 1;
	

	
	
	public MerlotLibraryTreeTableModel (XMLFile file, MerlotLibrary lib) 
	{
		super(lib.getLibraryNode());
		//super(file);
		
		_library = lib;

		MerlotDOMDocument doc = _root.getMerlotDOMDocument();

		_later = new AddNodeLaterRunnable();
		_laterThread = new Thread(_later);
		_laterThread.start();
		
	}

	/**
	 * 
	 */	
	
	public void setLibroot(MerlotDOMElement el) 
	{
		_libroot = el;
	}
	
		
	public void quit() 
	{
		_later._die = true;
		_laterThread.interrupt();
		
	}
		

	/**
	 * Overloads the default drop on row for the library panel.
	 * When a drop occurs, we just add the docfrag as a libitem 
	 * in the location nearest the drop.
	 */
	
	public boolean dropOnRow(int row, Object data, int where) 
	{
		MerlotDebug.msg("Library dropOnRow  row = "+row);
		/*
		if (data instanceof MerlotLibraryItem) {
			
			
		}
		*/
		if (data instanceof MerlotDOMFragment) {
			
			addItemLater(row, (MerlotDOMFragment)data, where, false);
			return true;
			
		}
				
		return false;
				
	}

	/**
	 * returns a MerlotDOMFragment containing the nodes in the paths. This does basically
	 * the opposite of newLibItem in that it prunes out the libitem element
	 */
	/*
	public Transferable getTransferable(TreePath[] paths) 
	{
		//	MerlotDebug.msg("MerlotLibModel.getTransferable("+paths+")");
		
		if (paths == null) {
			MerlotError.msg(MerlotResource.getString(ERR, "library.copy.nullselection"),
						  MerlotResource.getString(ERR, "library.copy.nullselection.t"));
			
		}
		
		else if (paths.length > 1) {
			MerlotError.msg(MerlotResource.getString(ERR, "library.copy.toomany"),
						  MerlotResource.getString(ERR, "library.copy.toomany.t"));
		}
		else {
			Transferable t = super.getTransferable(paths);
			// now prune out the libitem element
			if (t instanceof MerlotDOMFragment) {
				MerlotDOMFragment frag = (MerlotDOMFragment)t;
				MerlotDOMNode[] children = frag.getChildNodes();
				for (int i=0; i < children.length; i++) {
					if (children[i].getNodeName().equals("libitem")) {
						frag.removeChild(children[i]);
						MerlotDOMNode[] subchildren = children[i].getChildNodes();
						for (int j=0; j<subchildren.length; j++) {
							frag.appendChild(subchildren[j]);
						}
					}
				}
			}	
			//	MerlotDebug.msg(" t = "+t);
		

			return t;
		}
		return null;
		
	}
	*/

	/**
	 * This adds a new item to the library where the user dropped it, or pasted it
	 * It asks the user what they want to name the item first.
	 */
	protected void newLibItem(int row, MerlotDOMFragment frag, int where, boolean paste) 
	{
		MerlotDOMNode item = null;


		if (_table == null) {
			MerlotDebug.msg("_table is null");
			return ;
		}
		
		JTree tree = _table.getTree();
		if (tree == null) {
			MerlotDebug.msg("tree is null");
			return ;
		}
		TreePath path = tree.getPathForRow(row);
		if (path == null) {
			MerlotDebug.msg("path is null");
			return ;
		}
		Object o = path.getLastPathComponent();
		if (o instanceof MerlotDOMNode) {
			MerlotDOMNode droppedon = (MerlotDOMNode)o;
			MerlotDOMNode child = frag.getFirstChild();
			
			if (frag.getDocument() == droppedon.getDocument() || 
				child instanceof MerlotLibraryItem) {
				// we're moving the item around in the library...
				MerlotDebug.msg("moving item within library");
				
				importFragment(row,frag,where,paste,false);
				return;
			}
								
			
			// get the name of the item from the user
			
			
			
			JPanel p = new JPanel();
			JLabel l = new JLabel("Library item name:");
			JTextField namefield = new JTextField(20);
		
			StrutLayout lay = new StrutLayout();
			p.setLayout(lay);
		
			p.add(l);
			p.add(namefield, new StrutLayout.StrutConstraint(l,
									 StrutLayout.MID_RIGHT,
									 StrutLayout.MID_LEFT,
									 StrutLayout.EAST,
									 5));
			
			
			
			int ok = MerlotOptionPane.showInternalConfirmDialog(_library,
									    p,
									    MerlotResource.getString(UI,"library.item.new.nameit"),
									    JOptionPane.OK_CANCEL_OPTION);
			
			
			if (ok == JOptionPane.OK_OPTION) {
				// create a new item from the fragment
			
				MerlotDOMNode nd = null;
				// get the first child of the frag
				MerlotDOMNode[] nodes = frag.getChildNodes();
				if (nodes.length > 0) {
					nd = nodes[0];
				}
				String nodeName = namefield.getText().trim();
				if (nodeName == null || nodeName.equals("")) {
					nodeName = nd.getNodeName();
				}
				item = frag.newChild("libitem");
				((MerlotDOMElement)item).setAttribute("name",nodeName);
				if (nd != null) {
					
					
					// now we need to put together the fragment for passing to importFragment
		
					
					for (int i=0;i<nodes.length;i++) {
						frag.removeChild(nodes[i]);
						item.appendChild(nodes[i]);
					}
					MerlotDebug.msg("importing fragment "+frag+" to library");
					frag.printNode();
					
					
					importFragment(row, frag, where, paste,false);
					//	_table.getTree().treeDidChange();
					
					
				}
				
				
			}
		
		
		}
	}
	
	
	/**
	 * Special method that handles the drop in a different thread from the
	 * calling thread (due to some nasty drag and drop event handling bugs that cause
	 * deadlocks if certain gui operations are done at dnd drop event time)
	 */
	public  void addItemLater(int row, MerlotDOMFragment frag, int where, boolean paste) 
	{

		synchronized(_later) {
			if (_later != null) {
				_later._row = row;
				_later._frag = frag;
				_later._where = where;
				_later._paste = paste;
				
				_later._model = this;
				
				_later.notifyAll();
			}
			
			
		}
		
		
	}
	
	/**
	 * This is a nasty little hackish class used add the nodes that are dropped on the
	 * library panel so we can ask the user a question in a GUI window without having the
	 * DND handler lockup the JVM
	 */
	private class AddNodeLaterRunnable implements Runnable {
		
		// this is a sick C-like way of doing this, but since we only have one static instance
		// of this class per vm, it seemed like the easer way.

		protected MerlotDOMFragment _frag  = null;
		protected int             _row   = -1;
		protected int             _where = -1;
		protected boolean         _paste = false;
		
		protected MerlotLibraryTreeTableModel _model = null;
		

		public  boolean _die = false;
		
		public  AddNodeLaterRunnable() 
		{

		}
		
		public void run () 
		{
			while (!_die) {
				try {
					synchronized (_later) {
						MerlotDebug.msg("MerlotLibraryTreeTableModel waiting on _later");
						
						_later.wait();
						MerlotDebug.msg("MerlotLibraryTreeTableModel past wait");
						Thread.sleep(500);
						
						if (_frag != null && _model != null && _row >= 0) {
							SwingUtilities.invokeLater(new newLibItemRunnable(_model,_row, _frag, _where, _paste));
							
							//							_model.newLibItem(_row, _frag, _where, _paste);
							_frag = null;
							_model = null;
							_row  = -1;
							_where = -1;
						}
						
					}
					
				}
				catch (InterruptedException ex) {
					MerlotDebug.exception(ex);
				}
			}
			
			
		}
		
		
	}

	public class newLibItemRunnable extends Thread 
	{
		protected int _row;
		protected MerlotDOMFragment _frag;
		protected int _where;
		protected boolean _paste;
		protected MerlotLibraryTreeTableModel _model = null;

		public newLibItemRunnable (MerlotLibraryTreeTableModel model, int row, MerlotDOMFragment frag, int where, boolean paste) 
		{
			_model = model;
			
			_row = row;
			_frag = frag;
			_where = where;
			_paste = paste;
		}
		
		public void run () 
		{
			_model.newLibItem(_row, _frag, _where, _paste);
		}
		
		
	}
	


	/**
	 * Filter out nodes we don't want displayed in the tree view. 
	 * For the library all we want to display
	 * is the shelves, and the libitems under them.
	 */
	
	protected Vector filterChildNodes(MerlotDOMNode nd) 
	{
		Vector nodes = null;
		if (nd.getNodeName().equals("libitem")) {
			return null;
		}
		else {
            return super.filterChildNodes(nd);
		}
	}
}
