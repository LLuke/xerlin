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

import java.io.File;

import java.text.MessageFormat;

import java.awt.FileDialog;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * This class provides file dialogs for loading and saving of XML files
 * it can ge used to provide different dialogs for different filesystems
 *   
 *@author     iJustin Lipton
 *@created    16 March 2003
 */
public class XerlinFileDialogs implements MerlotConstants {
    protected XMLEditorFrame _frame;

    public XerlinFileDialogs(XMLEditorFrame frame) {
        _frame = frame;
    }

    /**
     *  Present a file chooser dialog for the user to select a file.
     *
     *@param  filter            the file filter for which files to show in the
     *      chooser
     *@param  selectedFileName  a file to select in the chooser if using the
     *      native AWT fileDialog
     *@return                   the File the user selected or null if they
     *      didn't select one
     */
    protected File getFileToOpen(
        MerlotFileFilter filter,
        String selectedFileName) {
        return getFileToOpen(filter, selectedFileName, true);
    }

    protected File getFileToOpen(
        MerlotFileFilter filter,
        String selectedFileName,
        boolean readWrite) {

        File rtn = null;
        JFileChooser chooser;
        String dir;

        try {
            MerlotDebug.msg("Open File");

            dir = _frame.getCurrentDir();

            if (XMLEditorSettings.getSharedInstance().useJFileChooser()) {

                chooser = getXMLFileChooser(dir, filter);
                chooser.setDialogType(JFileChooser.OPEN_DIALOG);
                int open = chooser.showOpenDialog(_frame);
                if (open == JFileChooser.APPROVE_OPTION) {
                    XMLEditorFrame.setWaitCursor();
                    rtn = chooser.getSelectedFile();
                    MerlotDebug.msg("Approved: file = " + rtn);
                    // strip off the filename and set the current dir variable
                    _frame.setCurrentDir(rtn.getParent());
                }
                return rtn;
            } else {

                FileDialog dialog = getXMLFileDialog(dir, filter);
                //dialog.setFile(selectedFileName);
                dialog.show();
                String f = dialog.getFile();
                String d = dialog.getDirectory();
                if (f != null) {
                    rtn = new File(d, f);
                    XMLEditorFrame.setWaitCursor();
                    MerlotDebug.msg("Approved: file = " + f);
                    _frame.setCurrentDir(rtn.getParent());
                }
                return rtn;
            }

        } catch (Exception ex) {
            XMLEditorFrame.setDefaultCursor();
            MerlotDebug.exception(ex);
            MerlotError.exception(
                ex,
                MerlotResource.getString(ERR, "xml.file.open.w"));
            return null;
        } finally {
            XMLEditorFrame.setDefaultCursor();
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
        chooser.setFilenameFilter(filter);
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
        String dir = XMLEditorFrame.getSharedInstance().getCurrentDir();
        File f = null;

        if (XMLEditorSettings.getSharedInstance().useJFileChooser()) {
            JFileChooser chooser = new JFileChooser(dir);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setFileFilter(new XMLFileFilter());
            chooser.setDialogType(JFileChooser.SAVE_DIALOG);
            int save =
                chooser.showSaveDialog(XMLEditorFrame.getSharedInstance());
            if (save == JFileChooser.APPROVE_OPTION) {
                f = chooser.getSelectedFile();
            }
        } else {

            // XXX begin addition for native file dialog
            FileDialog dialog =
                new FileDialog(
                    XMLEditorFrame.getSharedInstance(),
                    "Save as...");
            dialog.setMode(FileDialog.SAVE);
            dialog.setFilenameFilter(new XMLFileFilter());
            dialog.setDirectory(dir);
            dialog.setFile(".xml");

            dialog.show();
            String fl = dialog.getFile();
            String d = dialog.getDirectory();
            if (fl != null) {
                f = new File(d, fl);
            }
        }

        // XXX end of addition for native file dialog
        if (f != null) {

            if (f.getName().indexOf('.') < 0) {
                f = new File(f.getAbsolutePath() + ".xml");
            }
            // XXX removed because native file dialog handles this
            if (XMLEditorSettings.getSharedInstance().useJFileChooser()) {
                if (f.exists()) {
                    // warn about existing files
                    String[] quesargs = new String[1];
                    quesargs[0] = f.getAbsolutePath();

                    String ovwrques =
                        MessageFormat.format(
                            MerlotResource.getString(
                                ERR,
                                "document.save.overwrite.q"),
                            quesargs);

                    int sure =
                        MerlotOptionPane.showInternalConfirmDialog(
                            XMLEditorFrame.getSharedInstance(),
                            ovwrques,
                            MerlotResource.getString(
                                ERR,
                                "document.save.overwrite.t"),
                            JOptionPane.YES_NO_OPTION);
                    if (sure == JOptionPane.NO_OPTION) {
                        return null;
                    }
                }
            }
        }
        return f;
    }
}
