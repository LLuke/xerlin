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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.merlotxml.merlot.plugin.PluginConfig;
import org.merlotxml.merlot.plugin.dtd.DTDPluginConfig;

/**
 * Factory singleton to get editors for particular types of nodes and elements.
 * This is the class that gets editor panels from plugins 
 * for whatever element type they want to handle. otherwise, this produces a
 * generic Xml component editor
 *
 */

public class MerlotEditorFactory 
{
	/**
	 * Singleton instance
	 */
	protected static MerlotEditorFactory _instance = null;

	/**
	 * Global editors as defined in the main application properties file
	 * with the key merlot.editor.classes
	 */
	protected Hashtable _globalEditors;
	
	/**
	 * The global default editor 
	 */
	protected MerlotDOMEditor _default = null;
	
        /**
         * The defalt schema editor 
         */
        protected MerlotDOMEditor _defaultSchemaEditor = null;
	
	
	private MerlotEditorFactory()
	{
		// initialize the global default editor (what we use when nothing else is found that
		// is more specific
		String defaultEditorName =  XMLEditorSettings.getSharedInstance().getDefaultEditor();
		try {
			if (defaultEditorName != null) {
				Class c = Class.forName(defaultEditorName);
				Object o = c.newInstance();
				if (o instanceof MerlotDOMEditor) {
					_default = (MerlotDOMEditor)o;
				}
			}
		}
		catch (Exception ex) {
			MerlotDebug.exception(ex);
		}
		String defaultSchemaEditorName =  XMLEditorSettings.getSharedInstance().getDefaultSchemaEditor();

        try {
            if (defaultSchemaEditorName != null) {
                Class c = Class.forName(defaultSchemaEditorName);
                Object o = c.newInstance();
                if (o instanceof MerlotDOMEditor) {
                    _defaultSchemaEditor = (MerlotDOMEditor)o;
                 }
            } 
        } catch (Exception ex) {
            MerlotDebug.exception(ex);
        }
		
		// we absolutely must have a default editor... 
		if (_default == null) {
		    _default = new GenericDOMEditor();
		}

        if (_defaultSchemaEditor == null) {
            _defaultSchemaEditor = _default; 
        }

		// initialize the global editors 
		_globalEditors = new Hashtable();
		String eds = XMLEditorSettings.getSharedInstance().getEditors();

		List list = getEditorClasses(eds,null);
		if (list != null) {
			Iterator it = list.iterator();
			while (it.hasNext()) {
				MerlotDOMEditor editor = (MerlotDOMEditor)it.next();
				
				/*String[] edtypes = editor.getEditableTypes();
				if (edtypes != null) {
					for (int j=0; j<edtypes.length; j++) {
						_globalEditors.put(edtypes[j],editor);
						MerlotDebug.msg("Added editor "+editor+" for type '"+edtypes[j]+"'");
						
					}
				}*/
			}
			
		}
		
		
	}
	/**
	 * returns a list of editor panel classes loaded from the given classloader
	 * @return a List of MerlotDOMEditor instances or null
	 */
	public static List getEditorClasses (String classes, ClassLoader loader) 
	{
		MerlotDOMEditor editor = null;
		Class edclass;
		ArrayList list = new ArrayList();
		
		
		if (classes != null) {
			StringTokenizer tokize = new StringTokenizer(classes,", ");
			while (tokize.hasMoreTokens()) {
				try {
					
					String classname = tokize.nextToken();
					//	System.out.println("CLASSNAME = " + classname);
					edclass = Class.forName(classname,true,loader);
					Object o = edclass.newInstance();
					if (o instanceof MerlotDOMEditor) {
						editor = (MerlotDOMEditor)o;
						list.add(editor);
					}
					else {
						MerlotDebug.msg("Object "+edclass+" is not a MerlotDOMEditor");
					}
				}
				catch (ClassNotFoundException cnf) {
					MerlotDebug.exception(cnf);
				}
				catch (InstantiationException ex) {
					MerlotDebug.exception(ex);
				}
				catch (IllegalAccessException ia) {
					MerlotDebug.exception(ia);
				}
				
			}
			return list;
		}	
		return null;
		
		
	}
	
	
	public static MerlotEditorFactory getInstance() 
	{
		if (_instance == null) {
			_instance = new MerlotEditorFactory();
		}
		return _instance;
	}
	
	/**
	 * This method gets an editor panel for a particular node.
	 * This checks for a custom editor provided by a dtd plugin,
	 * a custom global editor, a default editor provided by the
	 * dtd plugin in that order. Failing any of those, the system
	 * default editor is returned.
	 * @param nodeName the name of the node (should not be null)
	 * @param node the node that's going to be edited (can be null)
	 * @param plugin optional plugin (can be null)
	 * @return a panel for editing this particular node or node type
	 */
        public MerlotDOMEditor getEditor(String nodeName, DTDPluginConfig config)
        throws InstantiationException, IllegalAccessException {
            return getEditor(nodeName, config, false);
        }
	
	public MerlotDOMEditor getEditor(String nodeName, DTDPluginConfig config, boolean useSchema)
	throws InstantiationException, IllegalAccessException
	{
		
		Class editorClass;
		//	DTDPluginConfig config;
		Iterator iter;
		MerlotDOMEditor rtn = null;
		Object globalEditor;
		PluginConfig nextConfig;
	
		/* we only look for a special plugin editor in the dtd plugin associated with a 
		 * file
		 */
		//MerlotDebug.msg("Plugin config = " + config);
		if (config != null) {
		    //MerlotDebug.msg("editorClass = " + config.getEditorClassFor(nodeName));
		    if ( (editorClass = config.getEditorClassFor(nodeName)) != null) {
			Object o = editorClass.newInstance();
			if (o instanceof MerlotDOMEditor){
			    rtn = (MerlotDOMEditor)o;
			}
		    }
		}
		
		if (rtn == null) {
		    // if the plugin didn't return us an editor, try the global table
		    if ( (globalEditor = _globalEditors.get(nodeName)) instanceof MerlotDOMEditor) {
			rtn = (MerlotDOMEditor) globalEditor;
		    }
		    
		}
		
		//Finally, use the global default
		if (rtn == null) {
            if (useSchema)
                rtn = _defaultSchemaEditor;
             else
		        rtn = _default;
		}
		return rtn;
		
	}
	
}
