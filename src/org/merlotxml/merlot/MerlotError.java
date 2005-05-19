/*
 *  ====================================================================
 *  Copyright (c) 1999-2000 ChannelPoint, Inc..  All rights reserved.
 *  ====================================================================
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *  1. Redistribution of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *  2. Redistribution in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  3. All advertising materials mentioning features or use of this
 *  software must display the following acknowledgment:  "This product
 *  includes software developed by ChannelPoint, Inc. for use in the
 *  Merlot XML Editor (http://www.channelpoint.com/merlot/)."
 *  4. Any names trademarked by ChannelPoint, Inc. must not be used to
 *  endorse or promote products derived from this software without prior
 *  written permission. For written permission, please contact
 *  legal@channelpoint.com.
 *  5.  Products derived from this software may not be called "Merlot"
 *  nor may "Merlot" appear in their names without prior written
 *  permission of ChannelPoint, Inc.
 *  6. Redistribution of any form whatsoever must retain the following
 *  acknowledgment:  "This product includes software developed by
 *  ChannelPoint, Inc. for use in the Merlot XML Editor
 *  (http://www.channelpoint.com/merlot/)."
 *  THIS SOFTWARE IS PROVIDED BY CHANNELPOINT, INC. "AS IS" AND ANY EXPRESSED OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO
 *  EVENT SHALL CHANNELPOINT, INC. OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 *  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  ====================================================================
 *  For more information on ChannelPoint, Inc. please see http://www.channelpoint.com.
 *  For information on the Merlot project, please see
 *  http://www.channelpoint.com/merlot.
 */

// Copyright 1999 ChannelPoint, Inc., All Rights Reserved.

package org.merlotxml.merlot;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import matthew.awt.StrutLayout;

/**
 *  Gui error reporter for the end-user
 *
 *@author     Administrator
 *@created    25 April 2003
 */

public class MerlotError implements MerlotConstants {
    /**
     *  Description of the Field
     */
    public static ShowMessageLaterRunnable _later = null;
    /**
     *  Description of the Field
     */
    public static Thread _laterThread = null;
    /**
     *  Description of the Field
     */
    public static boolean _showingLater = false;

    static {

    }


    /**
     *  init some neccessary error stuff
     */
    public MerlotError() {
        MerlotDebug.msg("MerlotError initializing");

        _later = new ShowMessageLaterRunnable();
        _laterThread = new Thread(_later);
        _laterThread.start();

    }


    /**
     *  Description of the Method
     */
    public void quit() {
        _later._die = true;
        _laterThread.interrupt();

    }


    /**
     *  Description of the Method
     *
     *@param  ex       Description of the Parameter
     *@param  usermsg  Description of the Parameter
     */
    public static void exception(Throwable ex, String usermsg) {
        Component c;
        if (XMLEditorFrame.getSharedInstance() != null) {
            c = XMLEditorFrame.getSharedInstance().getDesktopPane();
        } else {
            c = XMLEditor.getSharedInstance().getXerlinPanel();
        }
        String excp = null;
        String title = null;

        if (ex instanceof MerlotException) {
            Throwable t = ((MerlotException)ex).getWrappedException();
            if (t != null) {
                ex = t;
            }
            excp = MerlotResource.getString(ERR, "error.an");
            title = MerlotResource.getString(ERR, "error");

        }
        if (excp == null) {
            excp = ex.getClass().getName();
        }
        if (title == null) {
            title = excp;
        }

        int i = excp.lastIndexOf(".");
        if (i < 0) {
            i = 0;
        }
        excp = excp.substring(i + 1);

        StringBuffer msg = new StringBuffer(excp + " occurred " + usermsg);
        String s = ex.getMessage();
        if (s != null) {
            msg.append("\n" + s);

        }

        StrutLayout lay = new StrutLayout();

        JPanel l = MerlotUtils.createMultiLineLabel(msg.toString(), 80);
        l.setBorder(new EmptyBorder(5, 5, 15, 5));

        JPanel p = new JPanel(lay);

        JButton b = MerlotUtils.createButtonFromAction(new ShowErrorDetailsAction(ex, c));

        p.add(l);
        StrutLayout.StrutConstraint strut = new StrutLayout.StrutConstraint(l,
                StrutLayout.BOTTOM_RIGHT,
                StrutLayout.TOP_RIGHT,
                StrutLayout.SOUTH, 5);
        p.add(b, strut);
        // Try to force the exceptions to fit inside the desktop pane
        JComponent _desktop = (JComponent)c;
        Dimension desktopsize = _desktop.getSize();

        int maxwidth = (int)((double)desktopsize.getWidth() * 0.9);
        int maxheight = (int)((double)desktopsize.getHeight() * 0.9);
        Dimension maximumSize = new Dimension(maxwidth, maxheight);
        p.setMaximumSize(maximumSize);

        MerlotOptionPane.showInternalMessageDialog(c, p, title, JOptionPane.ERROR_MESSAGE);

    }


    /**
     *  Description of the Method
     *
     *@param  usermsg  Description of the Parameter
     */
    public static void msg(String usermsg) {
        msg(usermsg, MerlotResource.getString(ERR, "error"));
    }


    /**
     *  Description of the Method
     *
     *@param  usermsg  Description of the Parameter
     *@param  title    Description of the Parameter
     */
    public static void msg(String usermsg, String title) {
        XMLEditorFrame xef = XMLEditorFrame.getSharedInstance();

        Component c;
        if (xef == null) {
            c = XMLEditor.getSharedInstance().getXerlinPanel();
        } else {
            c = xef.getDesktopPane();
        }

        MerlotOptionPane.showInternalMessageDialog(c, usermsg, title, JOptionPane.ERROR_MESSAGE);

    }


    /**
     *  shows the stack trace of an error message
     *
     *@author     Administrator
     *@created    25 April 2003
     */
    protected static class ShowErrorDetailsAction extends AbstractAction {
        Throwable _error;
        Component _c;


        /**
         *  Constructor for the ShowErrorDetailsAction object
         *
         *@param  t  Description of the Parameter
         *@param  c  Description of the Parameter
         */
        public ShowErrorDetailsAction(Throwable t, Component c) {
            _error = t;
            _c = c;
            MerlotUtils.loadActionResources(this, ERR, "error.details");

        }


        /**
         *  Description of the Method
         *
         *@param  evt  Description of the Parameter
         */
        public void actionPerformed(ActionEvent evt) {
            // take the error and print the stack trace out to a string
            StringWriter sw = new StringWriter();
            PrintWriter w = new PrintWriter(sw);
            _error.printStackTrace(w);
            w.close();
            String s = MerlotUtils.wrapLines(sw.toString(), 150);
            JTextArea ta = new JTextArea(s);
            JScrollPane sp = new JScrollPane(ta);

            // Try to force the exceptions to fit inside the desktop pane
            JComponent _desktop;
            if (XMLEditorFrame.getSharedInstance() != null) {
                _desktop = XMLEditorFrame.getSharedInstance().getDesktopPane();
            } else {
                _desktop = XMLEditor.getSharedInstance().getXerlinPanel();
            }
            Dimension desktopsize = _desktop.getSize();
            int newwidth = (int)((double)desktopsize.getWidth() * 0.75);
            int newheight = (int)((double)desktopsize.getHeight() * 0.75);

            Dimension preferredSize = new Dimension(newwidth, newheight);

            int maxwidth = (int)((double)desktopsize.getWidth() * 0.9);
            int maxheight = (int)((double)desktopsize.getHeight() * 0.9);
            Dimension maximumSize = new Dimension(maxwidth, maxheight);
            sp.setPreferredSize(preferredSize);
            sp.setMaximumSize(maximumSize);

            JPanel p = new JPanel(new BorderLayout());
            p.add(sp, BorderLayout.CENTER);

            MerlotOptionPane.showInternalMessageDialog(_c, p);
        }

    }


    /**
     *  Special version of msg that displays messages in a different thread from
     *  the calling thread (due to some nasty drag and drop event handling bugs
     *  that cause deadlocks if certain gui operations are done at dnd drop
     *  event time)
     *
     *@param  usermsg  Description of the Parameter
     *@param  title    Description of the Parameter
     */

    public static void showMessageLater(String usermsg, String title) {

        if (_later != null) {
            if (_showingLater) {
                MerlotDebug.msg("DEADLOCK ERROR!! already showing a message later");
                return;
            }
            _showingLater = true;
            synchronized (_later) {
                _showingLater = true;

                _later._msg = usermsg;
                _later._title = title;
                MerlotDebug.msg("calling notify");
                _later.notifyAll();
            }
            _showingLater = false;
        }

    }


    /**
     *  Description of the Class
     *
     *@author     Administrator
     *@created    25 April 2003
     */
    public class ShowMessageLaterRunnable implements Runnable {

        /**
         *  Description of the Field
         */
        public String _msg = null;
        /**
         *  Description of the Field
         */
        public String _title = null;
        /**
         *  Description of the Field
         */
        public boolean _die = false;


        /**
         *  Main processing method for the ShowMessageLaterRunnable object
         */
        public void run() {
            while (!_die) {
                try {
                    synchronized (_later) {
                        MerlotDebug.msg("MerlotError waiting on _later");

                        _later.wait();
                        MerlotDebug.msg("MerlotError past wait");

                        if (_msg != null && _title != null) {
                            // Gotta luv stacks of hacks
                            //  a hack (SwingUtils.invokeLater())
                            //  on a hack (ShowMessageLaterRunnable)
                            //  on a hack (Java DND support)
                            // since this is still locking up due to
                            // native methods co-inciding, I'm doing the
                            // plan D attack... Thread.sleep()

                            Thread.sleep(500);
                            // hope half a second is enough

                            //SwingUtilities.invokeLater(new msgRunnable(_msg,_title));

                            msg(_msg, _title);
                            _msg = null;
                            _title = null;
                        }
                    }

                } catch (InterruptedException ex) {
                    MerlotDebug.exception(ex);
                }
            }

        }

    }


    /**
     *  Description of the Class
     *
     *@author     Administrator
     *@created    25 April 2003
     */
    public class msgRunnable implements Runnable {
        /**
         *  Description of the Field
         */
        protected String _msg = null;
        /**
         *  Description of the Field
         */
        protected String _title = null;


        /**
         *  Constructor for the msgRunnable object
         *
         *@param  msg    Description of the Parameter
         *@param  title  Description of the Parameter
         */
        public msgRunnable(String msg, String title) {
            _msg = msg;
            _title = title;
        }


        /**
         *  Main processing method for the msgRunnable object
         */
        public void run() {
            msg(_msg, _title);
        }

    }

    /*
     *  protected static void doMsg()
     *  {
     *  msg(_usermsg,_title);
     *  }
     *  static String _usermsg;
     *  static String _title;
     *  public static void showMessageLater(String usermsg, String title)
     *  {
     *  _usermsg = usermsg;
     *  _title   = title;
     *  SwingWorker	 worker = new SwingWorker() {
     *  public Object construct() {
     *  doMsg();
     *  return new Boolean(true);
     *  }
     *  public void finished() {
     *  }
     *  };
     *  Boolean b = (Boolean)worker.get();
     *  MerlotDebug.msg("SwingWorker.get = "+b);
     *  }
     */

}

