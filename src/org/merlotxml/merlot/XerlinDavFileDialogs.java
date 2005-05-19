/*
 *  ======================================================================
 *  The Xerlin XML Editor is Copyright (c) 2002 SpeedLegal Holdings, Inc.
 *  and other contributors.  It includes software developed for the
 *  Merlot XML Editor which is Copyright (c) 1999-2000 ChannelPoint, Inc.
 *  All rights reserved.
 *  ======================================================================
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *  1. Redistribution of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *  2. Redistribution in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  3. All advertising materials mentioning features or use of this
 *  software must display the following acknowledgment:
 *  "This product includes software developed by the SpeedLegal Group for
 *  use in the Xerlin XML Editor www.xerlin.org and software developed by
 *  ChannelPoint, Inc. for use in the Merlot XML Editor www.merlotxml.org"
 *  4. Except for the acknowledgments required by these conditions, any
 *  names trademarked by SpeedLegal Holdings, Inc. must not be used to
 *  endorse or promote products derived from this software without prior
 *  written permission. For written permission, please contact
 *  info@speedlegal.com. Any names trademarked by ChannelPoint, Inc. must
 *  not be used to endorse or promote products derived from this software
 *  without prior written permission. For written permission, please
 *  contact legal@channelpoint.com.
 *  5. Except for the acknowledgment required by these conditions, Products
 *  derived from this software may not be called "Xerlin" nor may "Xerlin"
 *  appear in their names without prior written permission of SpeedLegal
 *  Holdings, Inc. Products derived from this software may not be called
 *  "Merlot" nor may "Merlot" appear in their names without prior written
 *  permission of ChannelPoint, Inc.
 *  6. Redistribution of any form whatsoever must retain the following
 *  acknowledgment:
 *  "This product includes software developed by the SpeedLegal Group for
 *  use in the Xerlin XML Editor www.xerlin.org and software developed by
 *  ChannelPoint, Inc. for use in the Merlot XML Editor www.merlotxml.org"
 *  7. Developers who choose to contribute code or documentation to Xerlin
 *  (which is encouraged but not required) acknowledge and agree that: (a) any
 *  such contributions accepted and included in Xerlin will be subject to this
 *  license; (b) SpeedLegal Holdings, Inc. or any successor that hosts the
 *  Xerlin project will always have the right to make those contributions
 *  available under this license or an equivalent open source license; and
 *  (c) all contributions are made with the full authority of their owner/s.
 *  THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 *  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 *  AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 *  SPEEDLEGAL HOLDINGS, INC. OR CHANNELPOINT, INC. OR ANY CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 *  THE POSSIBILITY OF SUCH DAMAGE.
 *  ======================================================================
 *  For more information on SPEEDLEGAL visit www.speedlegal.com
 *  For information on the XERLIN project visit www.xerlin.org
 */
package org.merlotxml.merlot;

import java.awt.FileDialog;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.util.HttpURL;

import com.speedlegal.webdav.DAVException;
import com.speedlegal.webdav.DAVServer;
import com.speedlegal.webdav.EditorFile;
import com.speedlegal.webdav.WebdavFileChooser;
import com.speedlegal.webdav.ui.LoginFrame;

/**
 *  This class provides file dialogs for loading and saving of XML files it can
 *  ge used to provide different dialogs for different filesystems
 *
 *@author     Justin Lipton
 *@created    16 March 2003
 *      justin Exp $
 */
public class XerlinDavFileDialogs extends XerlinFileDialogs {
    private static DAVServer _davserver = null;
    private static EditorFile _file_locator = null;


    /**
     *  Constructor for the XerlinDavFileDialogs object
     *
     *@param  frame  Description of the Parameter
     */
    public XerlinDavFileDialogs(XMLEditorFrame frame) {
        super(frame);
    }


    /**
     *  Gets the client service if it exists otherwise creates a new client
     *  returns null if filebased returns _davserver if it exists returns new
     *  _davserver if it doesn't exist
     *
     *@return    The client value
     */

    public static DAVServer getClient() {
        XMLEditorFrame frame = XMLEditorFrame.getSharedInstance();
        HttpURL url;
        DAVServer client = null;
        if (_davserver == null) {

            String warning = MerlotResource.getString(ERR, "warning");
            String generic =
                    MerlotResource.getString(ERR, "generic.network.error");
            try {
                String server =
                        XMLEditorSettings.getSharedInstance().getProperty(
                        "webdav.server");
                String path =
                        XMLEditorSettings.getSharedInstance().getProperty(
                        "webdav.path");
                String user =
                        XMLEditorSettings.getSharedInstance().getProperty(
                        "webdav.user");
                String proxy =
                        XMLEditorSettings.getSharedInstance().getProperty(
                        "webdav.proxyserver");
                boolean hasProxy =
                        XMLEditorSettings.getSharedInstance().getProperty(
                        "webdav.proxy").equals(
                        "true");
                int port;

                final LoginFrame loginFrame =
                        new LoginFrame(frame, server, path, user, "");
                loginFrame.showAtCentre();
                server = loginFrame.getServer();
                path = loginFrame.getRootPath();
                user = loginFrame.getUser();
                String passwd = loginFrame.getPassword();
                port = loginFrame.getPort();
                url = new HttpURL(user, passwd, server, port, path);
                if (user == null || user.length() == 0) {
                    user = System.getProperty("user.dir");
                }
                XMLEditorSettings.getSharedInstance().setProperty(
                        "webdav.user",
                        user);
                XMLEditorSettings.getSharedInstance().setProperty(
                        "webdav.password",
                        passwd);
                XMLEditorSettings.getSharedInstance().setProperty(
                        "webdav.server",
                        server + ":" + port);
                if (url == null) {
                    return null;
                    //getClient();
                }
                if (hasProxy) {
                    int colonIndex = proxy.indexOf(':');
                    int proxyPort =
                            Integer.parseInt(proxy.substring(colonIndex + 1));
                    String proxyServer = proxy.substring(0, colonIndex);

                    client =
                            DAVServer.getDAVServer(url, "", proxyServer, proxyPort);
                } else {
                    client = DAVServer.getDAVServer(url, "");
                }
                if (client == null) {
                    return null;
                    //getClient();
                }

                EditorFile rootnode = client.getNode("/");
                rootnode.lastModified();
            } catch (DAVException de) {
                // Bad username/password combo or bad path
                if (de.getStatusCode() == 401) {
                    String noauth =
                            MerlotResource.getString(ERR, "invalid.client");
                    JOptionPane.showMessageDialog(
                            frame.getDesktopPane(),
                            noauth,
                            warning,
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(
                            frame.getDesktopPane(),
                            generic,
                            warning,
                            JOptionPane.WARNING_MESSAGE);
                }
                return null;
                //getClient();
            } catch (java.net.ConnectException ce) {
                // Non existent port
                JOptionPane.showMessageDialog(
                        frame.getDesktopPane(),
                        generic,
                        warning,
                        JOptionPane.WARNING_MESSAGE);
                return null;
                //getClient();
            } catch (java.net.NoRouteToHostException nr) {
                // Non existent server or network
                JOptionPane.showMessageDialog(
                        frame.getDesktopPane(),
                        generic,
                        warning,
                        JOptionPane.WARNING_MESSAGE);
                return null;
                //getClient();
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(
                        frame.getDesktopPane(),
                        generic,
                        warning,
                        JOptionPane.WARNING_MESSAGE);
                return null;
                //getClient();
            } catch (NullPointerException npe) {
                // Ugly the Coonect and NoRout exceptions are not being thrown
                JOptionPane.showMessageDialog(
                        frame.getDesktopPane(),
                        generic,
                        warning,
                        JOptionPane.WARNING_MESSAGE);
                return null;
                //getClient();
            }
            _davserver = client;
        }

        if (_davserver != null) {
            //_davserver.getClient().setDebug(1000);
            return _davserver;
        }

        return null;
        //getClient();
    }


    /**
     *  Present a file chooser dialog for the user to select a file.
     *
     *@param  filter            the file filter for which files to show in the
     *      chooser
     *@param  selectedFileName  a file to select in the chooser if using the
     *      native AWT fileDialog
     *@param  readWrite         Description of the Parameter
     *@return                   the File the user selected or null if they
     *      didn't select one
     */
    protected File getFileToOpen(
            MerlotFileFilter filter,
            String selectedFileName,
            boolean readWrite) {
        // WebDav setup integration starts here...
        EditorFile rtn = null;
        JFileChooser chooser = null;
        //SPFileChooser chooser = null;
        DAVServer client = getClient();

        if (client == null) {
            return super.getFileToOpen(filter, selectedFileName, readWrite);
        }
        EditorFile current = getCurrentFile();
        // This is where the login dialog gets integrated...

        try {
            chooser = new WebdavFileChooser(client.getRootURL(), "", "");
            if (current != null) {
                chooser.setCurrentDirectory(current.getParentFile());
            }
            //FIXME use the MerlotFileFilter properly
            //FileFilter xmlfilter = ((WebdavFileChooser)chooser).
            //        addFileFilter("XML Documents (*.xml)");
            //((WebdavFileChooser)chooser).addFileFilter("All Files");
            chooser.setFileFilter(filter);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (chooser == null) {
            return super.getFileToOpen(filter, selectedFileName, readWrite);
        }

        // WebDav setup integration ends here
        // Bring up the chooser if we've got a chooser or we are filebased
        try {
            MerlotDebug.msg("Open File");

            // I'm not sure why but this uses a 'special' FileChooser for
            // Windows - I've modified it so everything is the same...
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);

            int open = chooser.showOpenDialog(_frame);
            if (open == JFileChooser.APPROVE_OPTION) {
                rtn = EditorFile.getFile(chooser.getSelectedFile());
                MerlotDebug.msg("Approved: file = " + rtn);
                // strip off the filename and set the current dir variable
                if (rtn != null && rtn.exists() && rtn.isFile()) {
                    setCurrentFile(rtn);
                } else {
                    String msg =
                            MerlotResource.getString(ERR, "xml.file.invalid");
                    JOptionPane.showMessageDialog(
                            _frame.getDesktopPane(),
                            msg,
                            "Warning",
                            JOptionPane.INFORMATION_MESSAGE);
                    rtn = null;
                }
            }

            // Save the references...
            if (rtn != null) {
                // Heres the locking stuff
                // If its locked open READ-ONLY
                // if (webDirService.isReadOnly(rtn))
                // Set file locked by other if appropriate
                if (readWrite && !XMLEditorFile.hasWriteAccess(rtn)) {
                    //Handle case where lock is owned by user
                    if (!XMLEditorFile.setLock(rtn)) {
                        String args[] = new String[1];
                        args[0] = rtn.getName();
                        String msg =
                                MessageFormat.format(
                                MerlotResource.getString(ERR, "file.read.only"),
                                args);
                        JOptionPane.showMessageDialog(
                                _frame.getDesktopPane(),
                                msg,
                                "Warning",
                                JOptionPane.WARNING_MESSAGE);
                        return null;
                    }
                } else if (readWrite) {
                    // Lock the file and set the time-out
                    XMLEditorFile.setLock(rtn);
                }
            }
            return rtn;
        } catch (Exception ex) {
            MerlotDebug.exception(ex);
            MerlotError.exception(
                    ex,
                    MerlotResource.getString(ERR, "xml.file.open.w"));
            return null;
        }
    }


    /**
     *  Creates an AWT native-peered file dialog. This is usefull on windows for
     *  accessing network shares and such.
     *
     *@param  dir     the Directory to start the file dialog in
     *@param  filter  a file filter for what files to show to the user in the
     *      dialog
     *@return         a FileDialog instance with the given properties
     */
    protected FileDialog getXMLFileDialog(
            String dir,
            java.io.FilenameFilter filter) {
        FileDialog chooser = new FileDialog(_frame, "Open File");
        chooser.setDirectory(dir);
        //	chooser.setFilenameFilter(filter);
        return chooser;
    }


    /**
     *  Creates a Swing JFileChooser with the given filter and starting the
     *  directory given
     *
     *@param  dir     the Directory to start the file chooser in
     *@param  filter  a file filter for what files to show to the user in the
     *      dialog
     *@return         a JFileChooser instance with the given properties
     */
    protected JFileChooser getXMLFileChooser(
            String dir,
            javax.swing.filechooser.FileFilter filter) {
        JFileChooser chooser = new JFileChooser(dir);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(filter);
        return chooser;
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    protected File openLibraryFile() {
        String libDir = _frame.getLibDir();
        if (XMLEditorSettings.getSharedInstance().useJFileChooser()) {
            JFileChooser chooser =
                    getXMLFileChooser(libDir, new MerlotLibFileFilter());
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
            int open = chooser.showOpenDialog(_frame);
            if (open == JFileChooser.APPROVE_OPTION) {
                return chooser.getSelectedFile();
            }
        } else {
            FileDialog dialog =
                    getXMLFileDialog(libDir, new MerlotLibFileFilter());
            dialog.setFile("*.xmllib");

            dialog.show();
            String f = dialog.getFile();
            String d = dialog.getDirectory();
            if (f != null) {
                return new File(d, f);
            }
        }
        return null;
    }


    /**
     *  Gets the fileToSave attribute of the XerlinFileDialogs object
     *
     *@return    The fileToSave value
     */
    protected File getFileToSave() {
        DAVServer client = getClient();
        if (client == null) {
            return super.getFileToSave();
        }
        JFileChooser chooser = null;
        EditorFile f = null;
        EditorFile current = getCurrentFile();
        try {
            XMLEditorFrame.setWaitCursor();
            chooser = new WebdavFileChooser(client.getRootURL(), "", "");
            if (current != null) {
                chooser.setCurrentDirectory(current.getParentFile());
            }

            ((WebdavFileChooser)chooser).addFileFilter(
                    "XML Documents (*.xml)");
            ((WebdavFileChooser)chooser).addFileFilter("All Files");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            XMLEditorFrame.setDefaultCursor();
        }

        if (chooser != null) {
            chooser.setDialogType(JFileChooser.SAVE_DIALOG);
            int state =
                    chooser.showSaveDialog(XMLEditorFrame.getSharedInstance());

            if (state == JFileChooser.APPROVE_OPTION) {
                f = EditorFile.getFile(chooser.getSelectedFile());
            }
        }

        if (f == null) {
            return null;
        }

        if (!f.getName().endsWith(".xml")) {
            f = client.getNode(f.getPath() + ".xml");
        }

        // This would be better but currently no feasible
        //XMLFile.setLock(f);
        if (f.exists()) {
            // What if it's already open?
            if (_frame._fileToFrameMap.get(f.getAbsolutePath()) != null) {
                String msg = MerlotResource.getString(ERR, "document.open");
                JOptionPane.showMessageDialog(
                        _frame.getDesktopPane(),
                        msg,
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
                return null;
            }
            // warn about existing files
            String[] quesargs = new String[1];
            quesargs[0] = f.getAbsolutePath();

            String ovwrques =
                    MessageFormat.format(
                    MerlotResource.getString(ERR, "document.save.overwrite.q"),
                    quesargs);

            int sure =
                    MerlotOptionPane.showInternalConfirmDialog(
                    _frame,
                    ovwrques,
                    MerlotResource.getString(ERR, "document.save.overwrite.t"),
                    JOptionPane.YES_NO_OPTION);
            if (sure == JOptionPane.NO_OPTION) {
                return null;
            }
        }

        if (f == null) {
            return null;
        }

        //Set lock
        if (XMLEditorFile.setLock(f)) {
            return f;
        }

        return null;
    }


    /**
     *  Setter and getter methods for user current clause and file directory
     *  locations
     *
     *@return    The currentFile value
     */
    public EditorFile getCurrentFile() {
        return _file_locator;
    }


    /**
     *  Sets the currentFile attribute of the XMLEditorFrame object
     *
     *@param  f  The new currentFile value
     */
    public void setCurrentFile(EditorFile f) {
        // Don't set to a version
        if (f.getRealPath().equals(f.getPath())) {
            _file_locator = f;
        }
    }

}

