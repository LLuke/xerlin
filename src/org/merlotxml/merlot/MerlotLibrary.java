// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
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
http://www.merlotxml.org/
*/


// Copyright 1999 ChannelPoint, Inc., All Rights Reserved.

package org.merlotxml.merlot;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreePath;

import matthew.awt.StrutLayout;

import org.merlotxml.awt.PercentLayout;
import org.merlotxml.util.xml.DTDCache;
import org.merlotxml.util.xml.DTDCacheEntry;

import com.sun.javax.swing.JTreeTable;
import com.sun.javax.swing.TreeTableModel;
/**
 * 
 * A library of XML components (and or other stuff at some point)
 * 
 * @author Kelly A. Campbell
 *
 */

public class MerlotLibrary extends JInternalFrame
    implements MerlotConstants, MouseListener, ClipboardOwner

{

    private XMLFile _libFile;
    private MerlotLibraryTreeTableModel _tableModel;
    private JTreeTable          _table;
    private MerlotDOMDocument _libdoc;
    private JViewport _viewport;
	

    private Action _newSectionAction;
    private Action _deleteSectionAction;
    private Action _editNodeAction;
    private Action _deleteNodeAction;
    private Action _copyLibItemAction;
    private Action _pasteLibItemAction;
    private Action _renameLibItemAction;
	
	
    private MerlotLibrary _merlotLib;
	
	
    private MerlotDOMElement _libraryRoot = null;
	

    private static final int LIB_BOUNDS_FUDGE_FACTOR = 15;
    private static String _systemID = null;
	
    static 
    {
        DTDCache cache = DTDCache.getSharedInstance();

		String publicid = MerlotResource.getString(XML,"library.doctype.public");
		String systemid = MerlotResource.getString(XML,"library.doctype.dtdfile");

        DTDCacheEntry entry = new DTDCacheEntry(publicid, systemid);
        try {
            java.net.URL u = MerlotLibrary.class.getClassLoader().getResource(systemid);                
            InputStream stream = MerlotLibrary.class.getClassLoader().getResourceAsStream(systemid);
            System.out.println("lib dtd stream = "+stream);
            
            if (stream != null) {
                _systemID = u.toString();
                entry.setSystemId(_systemID);
                cache.loadDTDIntoCache(stream, entry);
            }
        }
        catch (IOException ex) {
            MerlotDebug.exception(ex);
        }
        // ignore
    }
    
    public MerlotLibrary ( XMLFile file) 
	throws MerlotException
    {
	super("Library", true, true, true, true);
	_libFile = file;
	_libdoc = new MerlotDOMDocument(file.getDocument(),file);
	init();
		
    }

    public static File getNewLibraryFile(String name) 
    {
		
	String dir = XMLEditorFrame.getSharedInstance().getCurrentDir();
	File f = null;
		
	if (XMLEditorSettings.getSharedInstance().useJFileChooser()) {
	    JFileChooser chooser = new JFileChooser(dir);
	    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    chooser.setFileFilter(new MerlotLibFileFilter());
	    chooser.setDialogType(JFileChooser.SAVE_DIALOG);
	    int save = chooser.showSaveDialog(XMLEditorFrame._frame);
	    if (save == JFileChooser.APPROVE_OPTION) {
		f = chooser.getSelectedFile();
	    }
	}
	else {
	    // XXX begin addition for native file dialog		
			
	    FileDialog dialog = new FileDialog(XMLEditorFrame.getSharedInstance(),"Create new library file");
	    dialog.setMode(FileDialog.SAVE);
	    dialog.setFilenameFilter(new MerlotLibFileFilter());
	    dialog.setDirectory(dir);
	    dialog.setFile(".xmllib");
			
	    dialog.show();
	    String fl = dialog.getFile();
	    String d  = dialog.getDirectory();
	    if (fl != null) {
		f = new File(d,fl);
				// XXX end of addition for native file dialog
	    }
	}
	if (f != null) {
	    if (f.getName().indexOf('.') < 0) {
		f = new File(f.getAbsolutePath() + ".xmllib");
	    }
	    if (XMLEditorSettings.getSharedInstance().useJFileChooser()) {
		if (f.exists()) {
		    // warn about existing files
		    String[] quesargs = new String[1];
		    quesargs[0] = f.getAbsolutePath();
					
		    String ovwrques = MessageFormat.format(MerlotResource.getString(ERR, "document.save.overwrite.q"),quesargs);
					
		    int sure = MerlotOptionPane.showInternalConfirmDialog(XMLEditorFrame._frame._desktop,ovwrques,MerlotResource.getString(ERR,"document.save.overwrite.t"),JOptionPane.YES_NO_OPTION);
		    if (sure == JOptionPane.NO_OPTION) {
			return null;
		    }
		}
				
	    }
	    try {
		FileWriter fw = new FileWriter(f);
		String[] dtda = new String[3];
		dtda[0] = MerlotResource.getString(XML,"library.doctype");
		dtda[1] = MerlotResource.getString(XML,"library.doctype.public");
		dtda[2] = _systemID;
            //MerlotResource.getString(XML,"library.doctype.dtdfile");
				
				
		MessageFormat mf = new MessageFormat(MerlotResource.getString(XML,"doctype.declaration"));
				
		String doctype = mf.format(dtda);
				
		fw.write(MerlotResource.getString(XML,"xml.declaration") + "\n");
		fw.write(doctype+"\n");
		fw.write("<"+dtda[0]+" name=\""+name+"\"></"+dtda[0]+">\n");
		fw.close();
				
				
		return f;
	    }
	    catch (IOException ex) {
		MerlotDebug.exception(ex);
	    }
			
	}
	return null;
    }
	

    protected void init() 
	throws MerlotException
    {
		_merlotLib = this;
	//		MerlotDebug.msg("New MerlotLibary("+title+")");
	

	// make sure this is a library document
	MerlotDOMNode nd = getLibraryNode();
	if (nd == null) {
	    throw new MerlotException(MerlotResource.getString(ERR,"library.notlibrary"));
	}

	setupActions();
	setupFrame();
		
		
	Icon icon = XMLEditorSettings.getSharedInstance().getIcon("library", XMLEditorSettings.SMALL_ICON);
	if (icon != null) {
	    setFrameIcon(icon);	
	}
	String title = getFrameTitle();
	if (title != null) {
	    setTitle(title);
	}
		
    }
	

    protected void setupActions() 
    {
	_newSectionAction = new NewSectionAction();
	_deleteSectionAction = new DeleteSectionAction();
	_editNodeAction   = new EditNodeAction();
	_deleteNodeAction = new DeleteNodeAction();
	_copyLibItemAction = new CopyLibItemAction();
	_pasteLibItemAction = new PasteLibItemAction();
	_renameLibItemAction = new RenameLibItemAction();
		
    }
	

    protected void setupFrame () 
    {

	setupPanel();
	restoreBounds();
	restoreExpansionState();
		

	this.addInternalFrameListener(new InternalFrameAdapter() {
		public void internalFrameClosing(InternalFrameEvent evt) 
		{
		    saveLibrary();
		    _tableModel.quit();
				
		}
				
	    });
		
		
    }
	

    public void saveLibrary() 
    {
	saveBounds();
	saveExpansionState();
		
	try {
	    _libFile.save();
	}
	catch (MerlotException ex) {
	    MerlotError.exception(ex, MerlotResource.getString(ERR,"library.save.w"));
	}
		
				
		
    }
	

	
    protected String getFrameTitle() 
    {
	MerlotDOMElement node = getLibraryNode();
	if (node != null) {
	    String title = node.getAttribute("name");
	    if (title != null && !(title.trim().equals(""))) {
		return title;
	    }
	}
	return null;
    }
	
    /**
     * returns the "library" root element, or null
     */
    public MerlotDOMElement getLibraryNode() 
    {
	if (_libraryRoot == null) {
	    // get the "library" root element
	    //	MerlotDOMDocument doc = new_libFile.getDocument();
	    MerlotDOMNode[] nodes = _libdoc.getChildNodes();
			
	    for (int i=0;i<nodes.length;i++) {
		MerlotDOMNode node = nodes[i];
		if (node instanceof MerlotDOMElement) {
		    // see if it's a library
		    if (node.getNodeName().equals("library")) {
			_libraryRoot = (MerlotDOMElement)node;
		    }
		}
	    }
	}
	return _libraryRoot;
		
    }
	
    protected void setupPanel() 
    {

	PercentLayout lay = new PercentLayout();
	JPanel p = new JPanel(lay);
	    
	    
	    
	JPanel treepanel = setupTreeTable();
	    
	    
	p.add(treepanel,   new PercentLayout.Constraints(100,PercentLayout.BOTH));
	    
	this.getContentPane().add(p);
	    
    }


    // MouseListener implementation
    public void mouseClicked (MouseEvent e) 
    {
	if (e.isPopupTrigger()) {
	    return;
	}
	if (e.getClickCount() >= 2) {
	    // edit the library item
	}
		
    }
	
    public void mousePressed (MouseEvent e)
    {
	if (e.isPopupTrigger()) {
	    doPopup(e);
	}
		
    }
	
    public void mouseReleased (MouseEvent e) 
    {
	if (e.isPopupTrigger()) {
	    doPopup(e);
	}
    }
	
    public void mouseEntered (MouseEvent e) 
    {
    }
	
    public void mouseExited (MouseEvent e)
    {
    }
	

    protected void doPopup(MouseEvent e) 
    {
        MerlotDebug.msg("***** MerlotLibrary doPopup****");
	Point pt = e.getPoint();
	int row = _table.rowAtPoint(pt);
	JPopupMenu m;
	if (row >= 0) {
	    _table.setRowSelectionInterval(row, row);
	    MerlotDOMNode nd = getNodeForRow(row);
	    m = createPopupMenu(nd);

        // Guard against popup menu items going off the screen
        m.pack();
        Dimension menusize = m.getPreferredSize();
        Point screenpoint = (Point)pt.clone();
        javax.swing.SwingUtilities.convertPointToScreen(screenpoint,_table);
        Dimension screensize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        // System.out.println("Screen Point = " + screenpoint);
        
        Point endpoint = new Point();
        endpoint.x = screenpoint.x + menusize.width;
        endpoint.y = screenpoint.y + menusize.height;
        
        if (screenpoint.x < 0) {
            screenpoint.x = 0;
        }
        if (screenpoint.y < 0) {
            screenpoint.y = 0;
        }
        if (endpoint.x > (int)screensize.getWidth()) {
            screenpoint.x = (int)screensize.getWidth() - menusize.width;
        }
        if (endpoint.y > (int)screensize.getHeight()) {
            // Add 30 points for a possible Windows Taskbar
            // (i.e. - programmer couldn't figure out how to ask the system
            // if it was auto-hidden.) - empirically determined value 
            // if anybody has a better idea.....
            screenpoint.y = (int)screensize.getHeight() - menusize.height - 30;
        }
        
        Point componentpoint = (Point)screenpoint.clone();
        javax.swing.SwingUtilities.convertPointFromScreen(componentpoint,_table);
        pt = componentpoint; 
        
	    m.show(_table, pt.x, pt.y);
	}
	else {
	    m = createPopupMenu(null);
	    m.show(_viewport, pt.x, pt.y);
	}
		
				
    }
	
    protected JPopupMenu createPopupMenu(MerlotDOMNode node) 
    {
	JPopupMenu popup = new SizeableJPopupMenuByHeight();
	boolean isitem = false;

	if (node != null && node.getNodeName().equals("libitem")) {
	    isitem = true;
	}
		
	MerlotUtils.addActionToMenu(_newSectionAction, popup);
	MerlotUtils.addActionToMenu(_deleteSectionAction, popup);
		
	MerlotUtils.addActionToMenu(_renameLibItemAction, popup);
		
	
	popup.addSeparator();
		
	// XXX Not Implemented yet
	//		MerlotUtils.addActionToMenu(_editNodeAction, popup);
	MerlotUtils.addActionToMenu(_deleteNodeAction, popup);
	if (isitem) {
	    _editNodeAction.setEnabled(false);
	    _deleteNodeAction.setEnabled(true);
	    _deleteSectionAction.setEnabled(false);
	}
	else {
	    _editNodeAction.setEnabled(false);
	    _deleteNodeAction.setEnabled(false);
	    _deleteSectionAction.setEnabled(true);
			
			
	}
		
	popup.addSeparator();
		
	MerlotUtils.addActionToMenu(_copyLibItemAction, popup);
	MerlotUtils.addActionToMenu(_pasteLibItemAction, popup);

	_copyLibItemAction.setEnabled(false);
	_pasteLibItemAction.setEnabled(false);
		
	if (isitem) {
	    _copyLibItemAction.setEnabled(true);
	}
	else {
	    // if the clipboard has something, enable paste
	    Transferable t = XMLEditorFrame.getSharedInstance().getTreeClipboard().getContents(this);
	    if (t instanceof MerlotDOMFragment) {
		_pasteLibItemAction.setEnabled(true);
	    }
			
	}
		

	return popup;
		
		
    }
	
    /**
     * Gets the Node object that is the current selection in the 
     * document tree.
     */
    public MerlotDOMNode getSelectedNode() 
    {
	MerlotDOMNode node = null;
		
	int selected_row = _table.getSelectedRow();
	if (selected_row >= 0) {
	    node = getNodeForRow(selected_row);
	}
	else {
	    MerlotDebug.msg("No row is currently selected.");
	}
		
	return node;
		
    }

    public MerlotDOMNode getNodeForRow(int row) 
    {
	MerlotDOMNode node = null;
	// get the object
	javax.swing.tree.TreePath treePath = 
	    _table.getTree().getPathForRow(row);
	if (treePath != null) {
	    Object n = treePath.getLastPathComponent();
	    if (n instanceof MerlotDOMNode) {
		node = (MerlotDOMNode)n;
	    }
	    else {
		MerlotDebug.msg("unknown tree node object type: "+n);
	    }
	    return node;
	}
	else {
	    return null;
	}
		
    }

    public void newSection() 
    {
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
		
		
	int ok = MerlotOptionPane.showInternalConfirmDialog(this,
							    p,
							    MerlotResource.getString(UI,"library.section.new.nameit"),
							    JOptionPane.OK_CANCEL_OPTION);
		
			
	if (ok == JOptionPane.OK_OPTION) {
	    // create a new section
	    MerlotDOMNode libnode = getLibraryNode();
	    MerlotDOMNode newsection = libnode.newChild("libshelf");
	    if (newsection instanceof MerlotDOMElement) {
		((MerlotDOMElement)newsection).setAttribute("name",namefield.getText());
	    }

	}
			
	
	
		
    }
	

    public void rename() 
    {
	MerlotDOMNode nd = getSelectedNode();
	if (nd != null) {
	    JPanel p = new JPanel();
	    JLabel l = new JLabel(MerlotResource.getString(UI,"library.item.rename.label"));
	    JTextField namefield = new JTextField(20);
	    if (nd instanceof MerlotDOMElement) {
		namefield.setText(((MerlotDOMElement)nd).getAttribute("name"));
	    }

	    StrutLayout lay = new StrutLayout();
	    p.setLayout(lay);
	   
	    p.add(l);
	    p.add(namefield, new StrutLayout.StrutConstraint(l,
							 StrutLayout.MID_RIGHT,
							 StrutLayout.MID_LEFT,
							 StrutLayout.EAST,
							 5));
			
			
	    int ok = MerlotOptionPane.showInternalConfirmDialog(this,
								p,
								MerlotResource.getString(UI,"library.item.rename.t"),
								JOptionPane.OK_CANCEL_OPTION);
			
			
	    if (ok == JOptionPane.OK_OPTION) {
		if (nd instanceof MerlotDOMElement) {
		    ((MerlotDOMElement)nd).setAttribute("name",namefield.getText().trim());
		}
	    }
	}
    }
	
		


    /**
     * stores the bounds as  an attribute to the library node
     */

    protected void saveBounds() 
    {
	Rectangle r = this.getBounds();
	MerlotDOMElement libroot = getLibraryNode();
	String boundstr = r.x + "," + r.y + "," + r.width + "," + r.height;
	libroot.setAttribute("bounds",boundstr);
	MerlotDebug.msg("bounds = "+boundstr);
		
		
    }
	


    /** gets the saved bounds out of the library element if they're there
     * ands sets the frame bounds. If they're not there, does nothing
     */
    protected void restoreBounds() 
    {
	MerlotDOMElement libroot = getLibraryNode();
	String boundstr = libroot.getAttribute("bounds");
	if (boundstr != null && boundstr.length() > 7) {
	    // parse it up
	    StringTokenizer tok = new StringTokenizer(boundstr,", ");
	    int i=0;
	    int[] bounds = new int[4];
	    while (tok.hasMoreTokens() && i < 4) {
		String n = tok.nextToken();
		try {
		    bounds[i++] = Integer.parseInt(n);
		}
		catch (NumberFormatException ex) {
		    // bad data.. skiddadle
		    return;
		}
	    }
	    if (i == 4) {
				// we have a winner
				// next check and make sure the bounds are within the main frame's viewing 
				// area
		Rectangle r = XMLEditorFrame.getSharedInstance().getBounds();
		if ((r.width > bounds[0] + LIB_BOUNDS_FUDGE_FACTOR || bounds[0] < 0) && 
		    (r.height > bounds[1] + LIB_BOUNDS_FUDGE_FACTOR || bounds[1] < 0)) {
		    MerlotDebug.msg("Setting bounds to "+bounds[0]+", "+
				    bounds[1] +", "+ 
				    bounds[2] +", "+ bounds[3]);
					
		    this.setBounds(bounds[0], 
				   bounds[1], 
				   bounds[2], 
				   bounds[3]);
		}
		else {
		    MerlotDebug.msg("Library not within bounds of frame");
		    MerlotDebug.msg("r.x = " + r.x + "   bounds[0] = "+bounds[0]);
		    MerlotDebug.msg("r.y = " + r.y + "   bounds[1] = "+bounds[1]);
					
		}
				
	    }
			
				
	}
	else {
	    this.pack();
	}
		
		
		
    }

    protected void saveExpansionState() 
    {
		
		
    }
	

    /**
     * Restores the expansion state of the tree
     */
    protected void restoreExpansionState() 
    {
	MerlotDOMElement libroot = getLibraryNode();
	// traverse the libshelf items
	MerlotDOMNode[] shelves = libroot.getChildNodes();
	if (shelves != null) {
	    String expanded;
	    MerlotDOMElement shelf;
	    JTreeTable.TreeTableCellRenderer tree = 
		(JTreeTable.TreeTableCellRenderer)
		_table.getDefaultRenderer(TreeTableModel.class);
	    MerlotDebug.msg("restoring shelves");
			
	    for (int i=0;i<shelves.length;i++) {
		if (shelves[i] instanceof MerlotDOMElement) {
		    shelf = (MerlotDOMElement)shelves[i];
		    expanded = shelf.getAttribute("expanded");
		    MerlotDebug.msg("shelf: "+shelf+"   expanded: "+expanded);
					
		    if (expanded.equals("true")) {
			Object[] o = _tableModel.getTreePathForNode(shelf);
			if (o != null) {
			    TreePath tp = new TreePath(o);
			    MerlotDebug.msg("expanding path: "+tp);
			    tree.expandPath(tp);
			}
		    }
		}
	    }
			
			
	}
		
		
    }
	

    public void lostOwnership(Clipboard cb, Transferable t)
    {
		
    }
	

    protected JPanel setupTreeTable() 
    {
	JPanel p = new JPanel();

	PercentLayout lay = new PercentLayout();
	p.setLayout(lay);
		
	_tableModel = new MerlotLibraryTreeTableModel(_libFile,this);
	String attr[] = new String[1];
	String name[] = new String[1];
	attr[0] = "name";
	//	attr[1] = "type";

	name[0] = "Name";
	//	name[1] = "Type";
		
	_tableModel.setColumns(attr,name);
		
	_table = new DNDJTreeTable(_tableModel);
	_table.setTableHeader(null);
	//		_table.setDefaultRenderer(JLabel.class, new JLabelTableCellRenderer());
	/*
	  TableColumn col = _table.getColumn("Type");
	  if (col != null) {
	  col.setPreferredWidth(16);
	  }
	*/
	_tableModel.setJTreeTable(_table);
		
	MerlotDOMElement libroot = getLibraryNode();
	if (libroot == null) {
	    MerlotDebug.msg("Libroot is null");
	}
		
	_tableModel.setLibroot(libroot);
		
	//		_table.setRootVisible(true);
	JScrollPane sp = new JScrollPane(_table);
		
	JTreeTable.TreeTableCellRenderer tree = 
	    (JTreeTable.TreeTableCellRenderer)
	    _table.getDefaultRenderer(TreeTableModel.class);
	tree.setRootVisible(false);
	tree.setShowsRootHandles(true);
		
		
	// setup the tree to use Node.getName() instead of
	// toString() to render the Node
	tree.setCellRenderer(new MerlotNodeRenderer("name"));
	   
		
	_table.setShowGrid(true);
	//	_table.setIntercellSpacing(new Dimension(1,1));
	_table.setIntercellSpacing(new Dimension(1,0));
		
	// what was the reason to allow multiple selections in the library?
	_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		
	_table.addMouseListener(this);
	sp.getViewport().addMouseListener(this);
	_viewport = sp.getViewport();
		
	p.add(sp, new PercentLayout.Constraints(100,PercentLayout.BOTH));
	
		
	p.setPreferredSize(new Dimension(150,500));
	p.setMinimumSize(new Dimension(10,10));
	return p;
	
    }

    protected class JLabelTableCellRenderer implements TableCellRenderer
    {
	protected  JLabel _blank = new JLabel("");
		
	public Component getTableCellRendererComponent(JTable table,
						       Object value,
						       boolean isSelected,
						       boolean hasFocus,
						       int row,
						       int column)
	{
	    if (value instanceof JLabel) {
		MerlotDebug.msg("value = "+value);
				
		return (Component) value;
	    }
	    else return _blank;
			
	}
		
		
    }

    protected class NewSectionAction extends AbstractAction 
    {
	public NewSectionAction () 
	{
	    MerlotUtils.loadActionResources(this,UI,"library.section.new");
	}
		
	public void actionPerformed(ActionEvent evt) 
	{
	    newSection();
			
	}
    }
    protected class DeleteSectionAction extends AbstractAction 
    {
	public DeleteSectionAction () 
	{
	    MerlotUtils.loadActionResources(this,UI,"library.section.delete");
	}
		
	public void actionPerformed(ActionEvent evt) 
	{
	    // get the section to delete
	    MerlotDOMNode nd = getSelectedNode();
	    String s = MerlotResource.getString(UI, "library.section.delete.sure.q");
	    String t = MerlotResource.getString(UI, "library.section.delete.sure.t");

	    MessageFormat mf = new MessageFormat(s);
	    Object[] o = new Object[1];
	    if (nd instanceof MerlotDOMElement) {
		o[0] = ((MerlotDOMElement)nd).getAttribute("name");
	    }
	    else {
		o[0] = nd.getNodeName();
	    }
			
	    String msg = mf.format(o);
			
	    int ok = MerlotOptionPane.showInternalConfirmDialog(_merlotLib, msg,t,JOptionPane.YES_NO_OPTION);
	    if (ok == JOptionPane.YES_OPTION) {
		nd.delete();
	    }
			
			
	}
    }
    protected class EditNodeAction extends AbstractAction 
    {
	public EditNodeAction () 
	{
	    MerlotUtils.loadActionResources(this,UI,"library.item.edit");
	}
		
	public void actionPerformed(ActionEvent evt) 
	{
			
	}
    }
    protected class DeleteNodeAction extends AbstractAction 
    {
	public DeleteNodeAction () 
	{
	    MerlotUtils.loadActionResources(this,UI,"library.item.delete");
	}
		
	public void actionPerformed(ActionEvent evt) 
	{
	    MerlotDOMNode nd = getSelectedNode();
	    String s = MerlotResource.getString(UI, "library.item.delete.sure.q");
	    String t = MerlotResource.getString(UI, "library.item.delete.sure.t");
			
	    MessageFormat mf = new MessageFormat(s);
	    Object[] o = new Object[1];
	    if (nd instanceof MerlotDOMElement) {
		o[0] = ((MerlotDOMElement)nd).getAttribute("name");
	    }
	    else {
		o[0] = nd.getNodeName();
	    }

	    String msg = mf.format(o);
			
	    int ok = MerlotOptionPane.showInternalConfirmDialog(_merlotLib, msg,t,JOptionPane.YES_NO_OPTION);
	    if (ok == JOptionPane.YES_OPTION) {
		nd.delete();
	    }

			
	}
    }

    protected class CopyLibItemAction extends AbstractAction 
    {
	public CopyLibItemAction () 
	{
	    MerlotUtils.loadActionResources(this,UI,"library.item.copy");
	}
		
	public void actionPerformed(ActionEvent evt) 
	{
	    Transferable t = null;
	    TreePath[] selection = _table.getTree().getSelectionPaths();
	    if (selection != null) {
		t = _tableModel.getTransferable(selection);
	    }
	    Clipboard cb = XMLEditorFrame.getSharedInstance().getTreeClipboard();
	    cb.setContents(t,_merlotLib);
			
	}
    }
    protected class PasteLibItemAction extends AbstractAction 
    {
		
	public PasteLibItemAction () 
	{
	    MerlotUtils.loadActionResources(this,UI,"library.item.paste");
	}
		
	public void actionPerformed(ActionEvent evt) 
	{
	    int row = _table.getSelectedRow();
			
	    Transferable t = XMLEditorFrame.getSharedInstance().getTreeClipboard().getContents(this);
	    if (t instanceof MerlotDOMFragment) {	
		_tableModel.addItemLater(row, (MerlotDOMFragment)t,DNDJTreeTableModel.INTO, true);
				/*
				  _tableModel.importFragment(row,(MerlotDOMFragment)t,DNDJTreeTableModel.INTO, true);
				*/
	    }
			
	}
    }

    protected class RenameLibItemAction extends AbstractAction 
    {
	public RenameLibItemAction() 
	{
	    MerlotUtils.loadActionResources(this,UI,"library.item.rename");
	}
		
	public void actionPerformed(ActionEvent evt) 
	{
	    rename();
						
	}
		
    }
	

}	


