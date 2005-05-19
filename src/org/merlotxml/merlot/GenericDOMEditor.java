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
http://www.merlotxml.org
*/

package org.merlotxml.merlot;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.merlotxml.util.xml.DTDElement;
import org.merlotxml.util.xml.GrammarComplexType;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Node;

/**
 * Editor of nodes and such
 *
 */

public class GenericDOMEditor implements MerlotDOMEditor, MerlotConstants
{
	private static final String[] _editableTypes = {"ALL"};
	
	protected static Hashtable _invalidCharHash;
	protected static Hashtable _manditoryFieldHash;

	public static final String SANITY_CHARS_PROP = "merlot.editor.generic.form.invalidchars";
	public static final String SANITY_MANDITORY_PROP = "merlot.editor.generic.form.manditoryfields";
	
	

	public GenericDOMEditor () 
	{
		
		
	}
		/**
	 * Returns a set of menu items for any special actions for 
	 * this particular editor
	 * that it wants in the popup menu. Standard stuff like cut, copy, paste
	 * is taken care of by other objects. If nothing needs added
	 */
	public  JMenuItem[] getMenuItems(MerlotDOMNode node) 
	{
		return null;
	}
	
	

	/**
	 * returns a panel for editing this type of component.
	 */
	public JPanel getEditPanel(MerlotDOMNode node) 
	{
		JPanel p  = null;
		
		Node nd = node.getRealNode();
	   
		int t = nd.getNodeType();
		switch (t) {
		case Node.TEXT_NODE:
			p = new TextEditPanel((MerlotDOMText)node);
			break;			
		case Node.COMMENT_NODE:
			p = new CommentEditPanel((MerlotDOMComment)node);
			break;
		case Node.PROCESSING_INSTRUCTION_NODE:
			p = new ProcInstructionEditPanel((MerlotDOMProcessingInstruction)node);
			break;
		case Node.DOCUMENT_NODE:
		case Node.DOCUMENT_TYPE_NODE:
		case Node.DOCUMENT_FRAGMENT_NODE:
		default:
			p = new GenericDOMEditPanel(node);
			break;			
		}		
		if (p != null)
			installListener(p,node);

		return p;
		
	}
	
	public void savePanel(JPanel p) 
		throws PropertyVetoException
	{
		if (p instanceof GenericDOMEditPanel) {
			((GenericDOMEditPanel)p).save();
		}
		else {
			MerlotDebug.msg("Cannot save! Panel is not the right type.");
		}
		
				
	}
	

	/**
	 * Returns the element types that this editor handles
	 */
	public String[] getEditableTypes() 
	{
		return _editableTypes;
	}
	
		
	
	/**
	 * returns true if this editor also edits it's children. If an editor
	 * says it handles it's children, then it must handle all it's children.<P>
	 */
	public boolean editsChildren() 
	{
		return false;
	}
	
	public void grabFocus(JPanel p) 
	{
		if (p instanceof GenericDOMEditPanel) {
			((GenericDOMEditPanel)p).grabFocus();
		}
	
		
	}
	
	/**
	 * Returns true if the component editor wants a particular node hidden
	 * from the user. If the editor wants to filter 
	 * what the user sees in their display, it should look at the
	 * given node, otherwise it should return false
	 */
	public boolean suppressNode(MerlotDOMNode node) 
	{
		// check this node out to see if it's a #text or #comment 
		// node, if so, hide it

		Node nd = node.getRealNode();
		//	MerlotDebug.msg("filter nd = "+nd+" nodename = "+node.getNodeName());
	   
		int t = nd.getNodeType();
		switch (t) {
		case Node.TEXT_NODE:
			
			if (nd instanceof CharacterData) {
			
			
				//			MerlotDebug.msg("#text = "+s);
				if (node instanceof MerlotDOMText && ((MerlotDOMText)node).isVisible()) {
					return false;
				}
				String s = ((CharacterData)nd).getData();	
				if (s != null) {
					s = s.trim();
					if ( s.equals("")) {
						return true;
					}
					return false;
				}
			}
			
			return true;
		case Node.COMMENT_NODE:
			return false;
		case Node.PROCESSING_INSTRUCTION_NODE:
			return false;
		case Node.DOCUMENT_NODE:
			return true;
		case Node.DOCUMENT_TYPE_NODE:
			return true;
		case Node.DOCUMENT_FRAGMENT_NODE:
			return true;
			
		}
		

		return false;
	}
	
	public boolean suppressAddType(DTDElement el) 
	{
		return false;
	}
    
    public boolean suppressAddType(GrammarComplexType el) {
        return false;
    }
	
	public void installListener(JPanel p, MerlotDOMNode node) 
	{
		if (p instanceof GenericDOMEditPanel) {
			// install us as a listener
			MerlotDebug.msg("installing listener for "+p);
			
			((GenericDOMEditPanel)p).addVetoableChangeListener(new GenericSanityCheckListener());
		}
	}

	protected static final int START=0;
	protected static final int NODENAME=1;
	protected static final int ATTRNAME=2;
	protected static final int EQUALS=3;
	
	protected static void parseInvalidCharsProp(String prop) 
	{
		int tokenstate = START;
		Vector names = null;
		Vector fields = null;
		StringBuffer chars = null;
		int err = 0;
		if (prop == null) {
			return;
		}
		
		StringTokenizer tok = new StringTokenizer(prop,"=., ",true);
		while (tok.hasMoreTokens()) {
			String s = tok.nextToken();
			if (s.length() > 0) {
				char c = s.charAt(0);
				switch (tokenstate) {
				case START:
					if (names != null && fields != null && chars != null) {
						for (int i=0;i<names.size(); i++) {
							for (int j=0;j<fields.size();j++) {
								String key = (String)names.elementAt(i) + "."+ (String)fields.elementAt(j);
								MerlotDebug.msg("GenericDOMEditor: putting key="+key+" chars='"+chars.toString()+"'");
								_invalidCharHash.put(key,chars.toString());
							}
						}
						names = null;
						fields = null;
						chars = null;
					}
					if (tok.hasMoreTokens()) {
						names = new Vector();
						fields = new Vector();
					}
					
					switch (c) {
					case ',':
					case '=':
					case '.':
					case ' ':
						err++;
						break;
					default:
						names.add(s);
						tokenstate=NODENAME;
						break;
					}
					break;
				case NODENAME:
					switch (c) {
					case '=':
						err++;
						break;
					case ',':
						break;
					case '.':
						tokenstate=ATTRNAME;
						break;
					case ' ':
						err++;
						break;
					default:
						names.add(s);
						break;
					}
					break;
				case ATTRNAME:
					switch (c) {
					case '=':
						chars = new StringBuffer();
						tokenstate=EQUALS;
						break;
					case ',':
						break;
					case '.':
					case ' ':
						err++;
						break;
					default:
						fields.add(s);
						break;
					}
					break;
				case EQUALS:
					switch (c) {
					case ' ':
						tokenstate=START;
						break;
					default:
						for (int i=0;i<s.length();i++) {
							if (s.charAt(i) == '{') {
								int x = s.indexOf('}');
								if (x > 0) {
									String word = s.substring(i+1,x);
									if (word.equals("space")) {
										chars.append(' ');
									}
									i+=x;
									continue;
								}
							}
							chars.append(s.charAt(i));
						}
					}
					break;
				}
			}
		}
		if (names != null && fields != null && chars != null) {
			for (int i=0;i<names.size(); i++) {
				for (int j=0;j<fields.size();j++) {
					String key = (String)names.elementAt(i) + "." + (String)fields.elementAt(j);
					MerlotDebug.msg("GenericDOMEditor: putting key="+key+" chars='"+chars.toString()+"'");
					_invalidCharHash.put(key,chars.toString());
				}
			}
			names = null;
			fields = null;
			chars = null;
		}
		
	}
	
	protected static void parseManditoryFieldsProp(String prop) 
	{
		int tokenstate = START;
		Vector names = null;
		Vector fields = null;
		int err = 0;
		if (prop == null) {
			return;
		}
		
		StringTokenizer tok = new StringTokenizer(prop,"=, ",true);
		while (tok.hasMoreTokens()) {
			String s = tok.nextToken();
			if (s.length() > 0) {
				char c = s.charAt(0);
				switch (tokenstate) {
				case START:
					if (names != null && fields != null) {
						for (int i=0;i<names.size(); i++) {
							for (int j=0;j<fields.size();j++) {
								String key = (String)names.elementAt(i) + "."+ (String)fields.elementAt(j);
								MerlotDebug.msg("GenericDOMEditor: putting manditory field key="+key);
								_manditoryFieldHash.put(key,"yes");
							}
						}
						names = null;
						fields = null;
					}
					if (tok.hasMoreTokens()) {
						names = new Vector();
						fields = new Vector();
					}
switch (c) {
					case ',':
					case '=':
					case '.':
					case ' ':
						err++;
						break;
					default:
						names.add(s);
						tokenstate=NODENAME;
						break;
					}
					break;
				case NODENAME:
					switch (c) {
					case '=':
						tokenstate=EQUALS;
						break;
					case ',':
						break;
					case '.':
						tokenstate=ATTRNAME;
						break;
					case ' ':
						err++;
						break;
					default:
						names.add(s);
						break;
					}
					break;
				case ATTRNAME:
					switch (c) {
					case '=':
						tokenstate=EQUALS;
						break;
					case ',':
						break;
					case '.':
					case ' ':
						err++;
						break;
					default:
						fields.add(s);
						break;
					}
					break;
				case EQUALS:
					switch (c) {
					case ' ':
						tokenstate=START;
						break;
					case ',':
						break;
						
					default:
						fields.add(s);
						break;
					}
					break;
				}
			}
		}
		if (names != null && fields != null) {
			for (int i=0;i<names.size(); i++) {
				for (int j=0;j<fields.size();j++) {
					String key = (String)names.elementAt(i) + "."+ (String)fields.elementAt(j);
					MerlotDebug.msg("GenericDOMEditor: putting manditory field key="+key);
					_manditoryFieldHash.put(key,"yes");
				}
			}
			names = null;
			fields = null;
		}
		
	}
	
	
	protected static String getInvalidChars(MerlotDOMNode node, String fieldname) 
	{
		if (_invalidCharHash == null) {
			_invalidCharHash = new Hashtable();
			// load this up from the properties
			String invalidcharsprop = XMLEditorSettings.getSharedInstance().getProperty(SANITY_CHARS_PROP);
			parseInvalidCharsProp(invalidcharsprop);
			
			
		}
		if (node != null) {
			String nodename = node.getNodeName();
			return (String)_invalidCharHash.get(nodename+"."+fieldname);
		}
		return null;
		
	}
	
	protected static String getManditoryFields(MerlotDOMNode node, String fieldname) 
	{
		if (_manditoryFieldHash == null) {
			_manditoryFieldHash = new Hashtable();
			String manditoryfields = XMLEditorSettings.getSharedInstance().getProperty(SANITY_MANDITORY_PROP);
			parseManditoryFieldsProp(manditoryfields);
			
		}
		if (node != null) {
			String nodename = node.getNodeName();
			return (String)_manditoryFieldHash.get(nodename+"."+fieldname);
		}
		return null;
						
	}
	

	public static class GenericSanityCheckListener implements VetoableChangeListener 
	{
		
		public GenericSanityCheckListener() 
		{
		}
		
	
		public void vetoableChange(PropertyChangeEvent evt) 
			throws PropertyVetoException
		{
			//	System.out.println("vetoableChange");
			
			
			// if the name has spaces or is blank, throw an exception
			String t;
			Object n = evt.getSource();
			if (!(n instanceof MerlotDOMNode)) {
				return;
			}
			MerlotDOMNode node = (MerlotDOMNode)n;
			
			String s = evt.getPropertyName();
			Object o = evt.getNewValue();
			if (o instanceof String) {
				t = ((String)o).trim();
			}
			else {
				t = null;
			}
			
			String invalidchars = GenericDOMEditor.getInvalidChars(node,s);
			if (t != null && invalidchars != null) {
				
				//	System.out.println("invalidchars = "+invalidchars);
				// check the field value for invalid chars
				for (int i=0;i<invalidchars.length();i++) {
					char c = invalidchars.charAt(i);
					if (t.indexOf(c) >= 0) {
						String[] err = new String[3];
						err[0] = node.getNodeName();
						err[1] = s;
						switch (c) {
						case ' ':
							err[2] = "space";
							break;
						case '.':
							err[2] = "period";
							break;
						case ',':
							err[2] = "comma";
							break;
						default:
							err[2] = "'" + c + "'";
							break;
							
						}
						
						throw new PropertyVetoException(MessageFormat.format(MerlotResource.getString(ERR,"illegal.value.attr.char"),err),evt);
					}
					
				}
			}
			
			String  manditory  = GenericDOMEditor.getManditoryFields(node,s);
			if (manditory != null && manditory.equals("yes")) {
				if (t == null || t.equals("")) {
					String err[] = new String[2];
					err[0] = node.getNodeName();
					err[1] = s;
					
					throw new PropertyVetoException(MessageFormat.format(MerlotResource.getString(ERR,"manditory.field"),err),evt);
				}
			}
		}
	}
	

    

}
