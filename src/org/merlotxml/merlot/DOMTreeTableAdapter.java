/*
====================================================================
Copyright (c) 1999-2001 ChannelPoint, Inc..  All rights reserved.
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

package org.merlotxml.merlot;

import java.awt.datatransfer.Transferable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.javax.swing.JTreeTable;
import com.sun.javax.swing.TreeTableModel;

/**
 * 
 * Adapts a DOM Document object into a TreeTableModel
 * 
 * @author Kelly A. Campbell
 *
 */
public class DOMTreeTableAdapter extends DNDJTreeTableModel
    implements MerlotNodeListener, MerlotConstants
{

    private static final int ELEMENT_COL = 0;
    private static final int NAME_COL    = 1;
    private static final int LABEL_COL   = 2;
	
    protected static final int NODE_INSERTED_EVENT = 10;
    protected static final int NODE_REMOVED_EVENT  = 11;
    protected static final int NODE_CHANGED_EVENT  = 12;
	
    public static final String ELEMENT = "ELEMENT_NAME";
	

    private String[] _column_attrs;
    private String[] _column_names;
	

    protected Hashtable _children_of_the_nodes;
    protected MerlotDOMNode   _root = null;
	
    protected UndoManager _undoManager = null;
    
    protected XMLFile _file;
    
    protected Vector _statusListeners = new Vector();
    

    /**
     * The tree which we need to have a ref to when inserting new nodes so we can select the new row
     */
    
    protected JTreeTable _table;

	public DOMTreeTableAdapter(XMLFile file) 
	{
		super(new MerlotDOMDocument(file.getDocument(), file));
		_file = file;
		_root = (MerlotDOMNode)super.root;
		if (_root != null) {
			_root.addMerlotNodeListener(this);
			addIDManagerAsNodeListener();
            // Starting validation at the top node
            MerlotDOMNode firstChild = (MerlotDOMNode)_root.getChildElements().get(0);
            MerlotDebug.msg("First child: " + firstChild);
            if (firstChild instanceof MerlotDOMElement) {
                MerlotDebug.msg("Starting validation...");
                ((MerlotDOMElement)firstChild).validate();
            } else {
                MerlotDebug.msg("Can't start validation, first child is not a MerlotDOMElement: " + firstChild.getClass());
			}
		}
		else {
			MerlotDebug.msg("DOMTreeTableAdapter root is null");
		}
		_children_of_the_nodes = new Hashtable();
	}
	
    public DOMTreeTableAdapter(MerlotDOMNode root) 
    {
	super(root);
	_root = root;
	if (root != null) {
	    _root.addMerlotNodeListener(this);
		addIDManagerAsNodeListener();
	}
	else {
	    MerlotDebug.msg("DOMTreeTableAdapter root is null");
	}
	
	_children_of_the_nodes = new Hashtable();
    }
	
	public void addIDManagerAsNodeListener()
	{
		IDManager idMan = getDocument().getIdManager();
		if ( idMan != null )
			_root.addMerlotNodeListener( idMan );
		else
			System.out.println( "No IDManager found." );
	}
	
    public MerlotDOMDocument getDocument() 
    {
	if (_root instanceof MerlotDOMDocument) {
	    return (MerlotDOMDocument)_root;
	}
	else {
	    return _root.getMerlotDOMDocument();
	}
	
    }
	
    public void setUndoManager(UndoManager m) 
    {
	_undoManager = m;
    }
    

    public int getColumnCount() 
    {
	return _column_attrs.length;
    }
	
    public Class getColumnClass(int column) 
    {
	if (column == 0) {
	    return TreeTableModel.class;
	}
	else {
	    return String.class;
	}
	
    }
	

    public String getColumnName(int column) 
    {
	return (_column_names[column]);
    }
	
    public void setColumns(String[] attr, String[] names)
    {
	_column_attrs = attr;
	_column_names = names;
	for ( int i = 0; i < attr.length; i++)
		MerlotDebug.msg( "Attr: " + attr[i] );
	for ( int i = 0; i < names.length; i++)
		MerlotDebug.msg( "Name: " + names[i] );
	
	
	if (attr.length != names.length) {
	    throw new IllegalArgumentException("attr and names must be arrays of the same length");
	}
    }
    
    
    public Object getValueAt(Object node, int column) 
    {
	String s = "";
	String col = _column_attrs[column];
	switch (column) {
	case ELEMENT_COL:
	    if (node instanceof MerlotDOMElement) {
		MerlotDOMElement nd = (MerlotDOMElement)node;
		s = nd.getElementName();
	    } else {
		s = ((MerlotDOMNode)node).getNodeName();
	    }
	    break;
		
	default:
	    if (node instanceof MerlotDOMElement) 
		{
			MerlotDOMElement nd = (MerlotDOMElement)node;
			if ( col.equals( "~" ) )
				s = nd.getDescriptiveText();
			else
				s = nd.getAttribute(col);
		// This hack is not necessary any more, because it is now customisable. 
				// hack to display the text with it's enclosing element
		//if (s == null || s.equals("")) {
		//    s = nd.getChildText(true);
		//}
	    } else if (node instanceof MerlotDOMText) {
		s = ((MerlotDOMText)node).getText();
	    } else if (node instanceof MerlotDOMComment) {
		s = ((MerlotDOMComment)node).getText();
	    } else if (node instanceof MerlotDOMProcessingInstruction) {
		s = ((MerlotDOMProcessingInstruction)node).getTarget();
	    }
	    break;
	}
	//	MerlotDebug.msg("DOMTreeTableAdapter.getValueAt(node="+node+", column="+column+") = "+s);
	return s;	

    }
	
    public Object getChild(Object node, int n) 
    {
	Object ret = null;
	if (node instanceof MerlotDOMNode) {
	    MerlotDOMNode nd = (MerlotDOMNode)node;
	    Vector v = filterChildNodes(nd);
	    ret = v.elementAt(n);
	    
	}

	//	MerlotDebug.msg("DOMTreeTableAdapter.getChild(node="+node+", n="+n+") = "+ret);
	return ret;
    }
	
    public int getChildCount(Object node) 
    {
	int ret = 0;
	
	if (node instanceof MerlotDOMNode) {
	    MerlotDOMNode nd = (MerlotDOMNode)node;
	    
	    // need to filter out "#text" nodes from xml4j -- these should be
	    // treated like attributes if they are allowed in an element
	    Vector v = filterChildNodes(nd);
	    if (v != null) {
		ret = v.size();
	    }
	}
	return ret;
		
    }
	
    private String getDebugStringForNode(Object node) 
    {
	String s = null;
	if (node instanceof MerlotDOMElement) {
	    s = ((MerlotDOMElement)node).getAttribute("name");
	    if (s.trim().equals("")) {
		s = null;
	    }
	    
	}
	if (node instanceof MerlotDOMNode && s == null) {
	    s = ((MerlotDOMNode)node).getNodeName();
	    if (s.trim().equals("")) {
		s = null;
	    }
	}
	if (s == null) {
	    s = node.toString();
	}
	return s;
	
    }
    
    
    /**
     * This gets the set of child nodes for a given node, and filters
     * out stuff we don't want to display in the table (like #text nodes)
     * @return a vector of Node's
     */
    protected Vector filterChildNodes(MerlotDOMNode nd) 
    {
	Vector nodes = null;
	
	// cache the filtered nodes
	
	if ((nodes = (Vector)_children_of_the_nodes.get(nd.getRealNode())) == null) {
	    boolean  filterNodes = 
		XMLEditorSettings.getSharedInstance().isFilteringNodes();
	    
	    MerlotDOMNode[] list = nd.getChildNodes();
	   	    
	    if (list != null) {
		int len = list.length;
		if (len > 0) {
		    nodes = new Vector();
		    for (int i =0; i< len; i++) {
			boolean suppress = false;
			MerlotDOMNode n = list[i];
			if (filterNodes) {
			    MerlotDOMEditor editor = n.getEditor();
			    if (editor != null) {
				if (editor.suppressNode(n)) {
				    suppress = true;
				}
			    }
							
			}
			if (n instanceof MerlotDOMDoctype) {
			    suppress = true;
			}
			
			if (!suppress) {
			    nodes.addElement(n);
			}

		    }
		    _children_of_the_nodes.put(nd.getRealNode(),nodes);
		}
	    }
	}
		
	return nodes;
    }

    /**
     * reset the cache for a given node
     */
    public void childrenChanged(MerlotDOMNode nd) 
    {
	if (nd != null) {
	    _children_of_the_nodes.remove(nd.getRealNode());
	}
	
    }
    
    protected void nodeEvent(int event, MerlotDOMNode parent, 
			     int[] childIndices, MerlotDOMNode[] children) 
    {
	MerlotDebug.msg("DOMTree: nodeEvent: "+event);
		
	if (event == NODE_INSERTED_EVENT) {
	    // kill the cache
	    childrenChanged(parent);
	}
		

	Object[] pathToParent = getTreePathForNode(parent);

	// rework the childIndices to allow for the filtering
	Vector filteredChildren = filterChildNodes(parent);
		
	int[] tmpChildIndices = new int[children.length];
	MerlotDOMNode[] tmpChildren = new MerlotDOMNode[children.length];
		
	int[] myChildIndices;
	MerlotDOMNode[] myChildren;
		
		
	if (filteredChildren != null) {
	    int j=0;
	    for (int i = 0; i < children.length; i++) {
		MerlotDOMNode nd = children[i];
		int ind = filteredChildren.indexOf(nd);
		if (ind >= 0) {
		    tmpChildIndices[j] = ind;
		    tmpChildren[j] = nd;
		    j++;
		}
	    }
	    myChildIndices = new int[j];
	    myChildren = new MerlotDOMNode[j];
			
	    for (int i=0;i<j;i++) {
		myChildIndices[i] = tmpChildIndices[i];
		myChildren[i] = tmpChildren[i];
				
	    }
	}
	else {
	    myChildIndices = childIndices;
	    myChildren = children;
						
	}
		
	if (event == NODE_REMOVED_EVENT) {
	    childrenChanged(parent);
	}
		

	switch (event) {
	case NODE_INSERTED_EVENT:
			
	    String cstr;
	    if (myChildren.length > 0) {
		cstr = myChildren[0].getNodeName();
				
		if (myChildren[0] instanceof MerlotDOMElement) {
		    cstr += " name = " +((MerlotDOMElement)myChildren[0]).getAttribute("name");
		}
			
				
		MerlotDebug.msg("fireTreeNodesInserted (childIndex: "+myChildIndices[0] +" child: "+cstr+")");
		MerlotDebug.msg("  path = "+new TreePath(pathToParent));
			
				
		fireTreeNodesInserted(this, pathToParent, myChildIndices,  myChildren);
				
		SwingUtilities.invokeLater(new DisplayInsertedNodeRunnable(parent));
	    }
	    break;
	case NODE_REMOVED_EVENT:
	    fireTreeNodesRemoved(this, pathToParent, myChildIndices,  myChildren);
	    break;
	case NODE_CHANGED_EVENT:
	    fireTreeNodesChanged(this,pathToParent, myChildIndices, myChildren);
	    break;
	}
    }
	
    /**
     * triggers the proper event to notify a node of a child being added
     * in the tree display
     */
    public void nodeInserted(MerlotDOMNode parent, int[] i, 
			     MerlotDOMNode[] children) 
    {
	nodeEvent(NODE_INSERTED_EVENT, parent,i,children);
    }
	
    public void nodeRemoved (MerlotDOMNode parent, int[] i,
			     MerlotDOMNode[] children) 
    {
	nodeEvent(NODE_REMOVED_EVENT, parent, i,children);
				
    }

    public void nodeChanged(MerlotDOMNode parent, int[] i,
			    MerlotDOMNode[] children)
    {
	nodeEvent(NODE_CHANGED_EVENT, parent, i,children);	
    }

	
    public void nodeDeleted(MerlotDOMNode nd) 
    {
    }
	

    public void cacheReset (MerlotDOMNode parent) 
    {
	childrenChanged(parent);
		
    }
	

    protected class DisplayInsertedNodeRunnable implements Runnable 
    {
	private MerlotDOMNode _child;
	public DisplayInsertedNodeRunnable(MerlotDOMNode nd) 
	{
	    _child = nd;
	}
				
	public void run() 
	{
	    // wrap this up and have it done later after we're sure the tree is all updated
	    Object[] pathToChild = getTreePathForNode(_child);	
	    TreePath pToChild = new TreePath(pathToChild);
	    JTree tree = _table.getTree();
		
	    tree.expandPath(pToChild);
	}
    }
    
    /**
	 * Refreshes the node in the tree
	 */
    public void refreshNode(MerlotDOMNode node) {
        int[] path = getLocationPathForNode(node);
        if (path.length > 0) {
            int loc = path[path.length-1];
            ListSelectionEvent event = new ListSelectionEvent(_table, loc, loc, false);
            _table.valueChanged(event);
            _table.repaint();
        }
    }
    
    /**
     * This gets the location of a node specified by the indices of the 
     * nodes in the path within their parent containers. This creates a 
     * snapshot of where a node was located at a certain time specifically
     * for undoing operations
     */
    public int[] getLocationPathForNode(MerlotDOMNode nd) 
    {
	Object[] path = getTreePathForNode(nd);
	int len = path.length;
	int[] loc = new int[len];
	loc[0] = 0; // root is always zero
	if (len > 1) {
	    for (int i=1; i < len; i++) {
		loc[i] = ((MerlotDOMNode)path[i-1]).getChildIndex((MerlotDOMNode)path[i]);
	    }
	}
	return loc;
    }
  
    /** 
     * builds the path from this object up to the root and then make an array with 
     * it in correct traversal order i.e. root down
     */
    
    public Object[] getTreePathForNode(MerlotDOMNode nd) 
    {
	Vector v = new Vector();
	Object root = getRoot();
	MerlotDOMNode node = nd;
	Object[] objs = null;
	
	while (node != null && node != root) {
	    v.addElement(node);
	    
	    String dbg_str = node.getNodeName();
	    if (node instanceof MerlotDOMElement) {
		dbg_str += " name="+((MerlotDOMElement)node).getAttribute("name");
	    }
	    dbg_str += " id="+node;
	    
	    //MerlotDebug.msg("addtopath: "+dbg_str);
	    node = node.getParentNode();
	}
	if (node == root) {
	    // add the root to the end of the vector
	    v.addElement(node);
	    
	    String dbg_str = node.getNodeName();
	    if (node instanceof MerlotDOMElement) {
		dbg_str += " name="+((MerlotDOMElement)node).getAttribute("name");
	    }
	    dbg_str += " id="+node;
	    
	    //MerlotDebug.msg("addtopath: "+dbg_str+"\n");
	    
	    // we completed the loop sucessfully
	    // now reverse the order of the path
	    int len = v.size();
	    objs = new Object[len];
	    for (int i=len-1,j=0;i>=0;i--,j++) {
		objs[j] = v.elementAt(i);
	    }
	}
	else {
	    objs = new Object[1];
	    objs[0] = root;
	}
	return objs;
    }
    	
    public void fireTreeStructureChanged(Object source, Object[] path) 
    {
	//	MerlotDebug.msg("DOMTreeTableAdapter.fireTreeStructureChanged(source="+source+", path="+path+")");
	
	// Guaranteed to return a non-null array
        Object[] listeners = super.listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeModelListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent(source, path);
                ((TreeModelListener)listeners[i+1]).treeStructureChanged(e);
            }          
        }
    }

    /**
     * Returns any PCDATA as a string if it exists as a "#text" child node of
     * the given node or null
     */
    public String getPCDATA(Node nd) 
    {
	Vector nodes = null;
	if ((nodes = (Vector)_children_of_the_nodes.get(nd)) != null) {
	    NodeList list = nd.getChildNodes();
	    int len = list.getLength();
	    for (int i = 0; i < len; i++) {
		Node n = list.item(i);
		if (n != null) {
		    String name = n.getNodeName();
		    if (name != null && name.equals("#text")) {
			//return it
			return n.getNodeValue();
		    }
		}
				
	    }
	}
	return null;
		
    }
	
    public void setJTreeTable(JTreeTable table) 
    {
	_table = table;
    }
	

    /**
     * called by DNDJTreeTable when an object is dropped somewhere.
     * Hands off the object to importFragment.
     */
    public boolean dropOnRow(int row, Object data, int where) 
    {

	if (data instanceof MerlotDOMFragment) {
	    UndoableEdit edit = importFragment(row, (MerlotDOMFragment)data, where, false);
	    if (edit != null) {
				// add it to an undo manager
		if (_undoManager != null) {
		    _undoManager.addEdit(edit);
            if (XMLEditorFrame.getSharedInstance()!=null)
		        XMLEditorFrame.getSharedInstance().resetUndoAction(_undoManager);
		}
		
		return true;
	    }
	}
	return false;
				
    }
	
    public MerlotDOMNode getNodeAtRow(int row) 
    {
	Object o = _table.getTree().getPathForRow(row).getLastPathComponent();
	if ( o instanceof MerlotDOMNode) {
	    return (MerlotDOMNode)o;
	}
	return null;
				
    }
	
    /**
     * Imports a fragment into the current document tree. If the fragment is from
     * another document, then it is copied. If it is from the current document,
     * and this is not a paste operation, then it is moved. 
     * (Actually imported, and the old one is deleted).
     * @return an undoable edit if successfull, null otherwise
     */

    public UndoableEdit importFragment(int row, MerlotDOMFragment frag, int where, boolean paste)
    {
	return importFragment( row,  frag,  where,  paste, true);
		
    }
	
    public UndoableEdit importFragment(int row, MerlotDOMFragment frag, int where, boolean paste, boolean pruneLibItems)
    {
    XMLEditorFrame xef = XMLEditorFrame.getSharedInstance();
    XMLEditorDoc doc; 
    XMLEditorDocUI xed = null;
    if (xef!=null) {
        doc  = xef.getCurrentDocument();
        if (doc == null && _file!=null) {
            doc = _file.getXMLEditorDoc();
        }
        if (doc!=null)
            xed = doc.getXMLEditorDocUI();
    } else {
        XerlinPanel xp = XMLEditor.getSharedInstance().getXerlinPanel();
        xed = xp.getXMLEditorDocUI();
    }
	
	boolean ret = false;
	CompoundEdit bigedit = new CompoundEdit();
	MerlotUndoableEdit edit;
	int undoType = MerlotUndoableEdit.INSERT;
	String undoDesc = "import";
	
	boolean moving = false;	// true if we're just moving a node within a document

	MerlotDOMNode newChild = null;
	Object o = _table.getTree().getPathForRow(row).getLastPathComponent();
	if ( o instanceof MerlotDOMNode) {
	    // see if the given data is allowable at the given row
	    //	MerlotDOMFragment frag = (MerlotDOMFragment)data;
	    MerlotDOMNode droppedon = (MerlotDOMNode)o;
	    MerlotDOMNode parent = null;
			
	    MerlotDOMNode[] children = frag.getChildNodes();
	    // see if they have the same parent document... if so we're doing a move
	    // unless this is a paste
	    if (!paste && frag.getDocument() == droppedon.getDocument()) {
		moving = true;
				//	undoType = MerlotUndoableEdit.MOVE;
		undoDesc = "move";
	    }
			
	    MerlotDOMNode child = null;
			
	    if (children.length > 0) {
		child = frag.getFirstChild();
				
		if (child instanceof MerlotLibraryItem && !moving && pruneLibItems) {
		    MerlotDebug.msg("pruning library item during import");
					
		    MerlotLibraryItem.pruneLibraryItems(frag);
		    children = frag.getChildNodes();
		    child = frag.getFirstChild();
		}
	    }
					
	    switch (where) {
	    case INTO:
		parent = droppedon;
		// see if this child is allowed here
		if (parent.isAllowableChild(child.getNodeName(), -1)) {
		    // import all children, we're assuming that if the first child is
		    // allowed, the rest are too
		    MerlotDebug.msg("importing frag");
            // Disable the IDManager to prevent Ids changing on move
            IDManager idm = parent.getIdManager();
            idm.setDisabled(true);
		    for (int i = 0; i < children.length; i++) {
			    newChild = parent.importChild(children[i], false);
			    int[] location = getLocationPathForNode(newChild);
			    edit = new MerlotUndoableEdit(undoDesc, undoType, this, newChild, location);
			    bigedit.addEdit(edit);
		    }
            idm.setDisabled(false);
		    MerlotDebug.msg("done importing frag");
		    ret = true;
		}
		else {
		    /* XXX causes deadlock in Drag and Drop
		       MerlotError.showMessageLater(MerlotResource.getString(ERR,"dom.insert.into.err"), 
		       MerlotResource.getString(ERR,"dom.insert.into.err.t"));
					
		    */
		    fireStatusChanged(new StatusEvent(MerlotResource.getString(ERR,"dom.insert.into.err")));
		    MerlotDebug.msg("Parent doesn't allow child. Parent="+parent+"["+parent.getNodeName()+"] child="+child+"["+child.getNodeName()+"]");
					
		}
		break;
	    case BEFORE:
		parent = droppedon.getParentNode();
		// see if this child is allowed here
		if (parent != null) {
		    int droppedonIndex = parent.getChildIndex(droppedon);
		    if (parent.isAllowableChild(child,droppedonIndex,!moving)) {
                // Disable the IDManager to prevent Ids changing on move
                IDManager idm = parent.getIdManager();
                idm.setDisabled(true);
			    for (int i =0; i < children.length; i++) {
			        newChild = droppedon.importChildBefore(children[i]);
			        int[] location = getLocationPathForNode(newChild);
			        edit = new MerlotUndoableEdit("import", undoType, this, newChild, location);
			        bigedit.addEdit(edit);
			    }
                idm.setDisabled(false);
		        ret = true;
		    }
		}
		if (!ret) {
		    /*
		      MerlotError.showMessageLater(MerlotResource.getString(ERR,"dom.insert.before.err"), 
		      MerlotResource.getString(ERR,"dom.insert.before.err.t"));
					
		    */
		    fireStatusChanged(new StatusEvent(MerlotResource.getString(ERR,"dom.insert.before.err")));
		    MerlotDebug.msg("Parent doesn't allow child. Parent="+parent+"["+parent.getNodeName()+"] child="+child+"["+child.getNodeName()+"]");	
		}
				
		break;
				
	    case AFTER:
		parent = droppedon.getParentNode();
		// see if this child is allowed here
		if (parent != null) {
		    int droppedonIndex = parent.getChildIndex(droppedon);
		    if (parent.isAllowableChild(child,droppedonIndex+1,!moving)) {
                // Disable the IDManager to prevent Ids changing on move
                IDManager idm = parent.getIdManager();
                idm.setDisabled(true);

			    for (int i=children.length-1;i>=0;i--) {
			        newChild = droppedon.importChildAfter(children[i]);
			        int[] location = getLocationPathForNode(newChild);
			        edit = new MerlotUndoableEdit("import", undoType, this, newChild, location);
			        bigedit.addEdit(edit);
			    }
                idm.setDisabled(false);
			    ret = true;
		    }
		}
		if (!ret) {
		    /*
		      MerlotError.showMessageLater(MerlotResource.getString(ERR,"dom.insert.after.err"), 
		      MerlotResource.getString(ERR,"dom.insert.after.err.t"));
					
		    */
		    fireStatusChanged(new StatusEvent(MerlotResource.getString(ERR,"dom.insert.after.err")));
		    MerlotDebug.msg("Parent doesn't allow child. Parent="+parent+"["+parent.getNodeName()+"] child="+child+"["+child.getNodeName()+"]");
		}				
		break;
	    default:
		MerlotError.showMessageLater(MerlotResource.getString(ERR,"dom.insert.default.err"), 
					   MerlotResource.getString(ERR,"dom.insert.default.err.t"));
		break;
				
	    }
	    // if we're moving the node, need to delete the old one
	    if (moving) {
				
		MerlotDOMNode[] orig = frag.getClonedFrom();
				// if the drop was successfull and there's an 
				// original node to take out
		if (ret && orig != null) {
		    for (int i=0;i<orig.length;i++) {
			int[] location = getLocationPathForNode(orig[i]);
            if (xed!=null)
                xed.deleteNode(orig[i]);
            else
                orig[i].delete();
			edit = new MerlotUndoableEdit(undoDesc, MerlotUndoableEdit.DELETE, this, orig[i], location);
			bigedit.addEdit(edit);
			MerlotDebug.msg("orig node deleted");
		    }
		    frag.setClonedFrom(null);
		}
	    }
	}
	else {
	    MerlotDebug.msg(" o.class = "+o.getClass());
	}
	if (ret) {
	    bigedit.end();
	    MerlotDebug.msg("importFragment returns: "+bigedit);
	    return bigedit;
	}
	else {
	    return null;
	}
    }

    /**
     * returns a MerlotDOMFragment containing the nodes in the paths 
     */
    public Transferable getTransferable(TreePath[] paths) 
    {
	MerlotDOMDocument d = getDocument();

	if (d != null && paths != null) {
	    MerlotDOMFragment frag = d.createDocumentFragment();
	    MerlotDOMNode[] clonedset = new MerlotDOMNode[paths.length]; // nodes we cloned from
	    MerlotDOMNode node;
	    for (int i=0; i < paths.length; i++) {
		Object n = paths[i].getLastPathComponent();
		if (n instanceof MerlotDOMNode) {
		    node = (MerlotDOMNode)n;
		    clonedset[i] = node;
		    frag.appendChild((MerlotDOMNode)node.clone());
		}
		else {
		    MerlotDebug.msg("unknown tree node object type: "+n);
		}
	    }
	    frag.setClonedFrom(clonedset);
	    return frag;
	}
	MerlotDebug.msg("Something is null: d = "+d+ " paths = "+paths);
	return null;
    }
	
    public void addStatusListener(StatusListener listener) 
    {
	_statusListeners.add(listener);
    }
	
    public void removeStatusListener(StatusListener listener) 
    {
	_statusListeners.remove(listener);
		
    }
	
    public void fireStatusChanged(StatusEvent evt) 
    {
	Enumeration e = _statusListeners.elements();
	while (e.hasMoreElements()) {
	    Object o = e.nextElement();
	    if (o instanceof StatusListener) {
		StatusListener l = (StatusListener)o;
		l.statusChanged(evt);
	    }
	}
    }
}
