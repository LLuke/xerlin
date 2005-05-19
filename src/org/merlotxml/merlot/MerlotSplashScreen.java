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

/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 2 -*-
 *
 * $Id: MerlotSplashScreen.java,v 1.2 2000/03/07 04:46:14 camk Exp $
 *
 */

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;




public class MerlotSplashScreen
{
  JLabel _statusBar;
  
    static final int SLEEP_TIME = 1000;	// 1 sec
    static final int MIN_SPLASH_TIME = 4 * 1000; // 5 seconds
    
 
  Icon _splash;
  JWindow _external = null;
    long _timer;
    
    //  JPanel _internal = null;
  
  /**
   * Create a splash screen
   *
   */
  public MerlotSplashScreen (Icon pic) 
  {
   
      if (pic != null) {
        _splash = pic;
        
    	setupExternal();
	_timer = System.currentTimeMillis();
	
      }
    }
    
 
  


  public void close()
  {
    try {
	long currentTime = System.currentTimeMillis();
    while ((currentTime - _timer) < MIN_SPLASH_TIME) {
	Thread.sleep(SLEEP_TIME);
	 currentTime = System.currentTimeMillis();
    }
    
      SwingUtilities.invokeLater(new CloseSplashScreen());
    }
    catch (Exception ex) {
      MerlotDebug.exception(ex);
    }
          
  }

  public void showStatus(String status) 
  {
    try {
      SwingUtilities.invokeLater(new UpdateStatus(status));
    }
    catch (Exception ex) {
      MerlotDebug.exception(ex);
    }
      }
 
  
  protected void setupExternal() 
  {
    _external = new JWindow();
    _external.getContentPane().add(getPanel());
    _external.pack();
    Dimension windowSize = _external.getSize();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    _external.setBounds((screenSize.width - windowSize.width) / 2,
                        (screenSize.height - windowSize.height) / 2,
                        windowSize.width,windowSize.height);
    _external.setVisible(true);
    
  }
  
  
  protected JPanel getPanel() 
  {
    JPanel p = new JPanel(new BorderLayout());
    _statusBar = new JLabel("...",SwingConstants.CENTER);
    p.add(new JLabel(_splash), BorderLayout.CENTER);
    p.add(_statusBar, BorderLayout.SOUTH);
    p.setBorder(new BevelBorder(BevelBorder.RAISED));
        
    return p;
    
  }
  
  class UpdateStatus implements Runnable 
  {
    String new_status;
    public UpdateStatus(String status) 
    {
      new_status = status;
    }
    public void run() 
    {
      _statusBar.setText(new_status);
    }
  }
  class CloseSplashScreen implements Runnable
  {
    public void run() 
    {
      // close the internal or external here
      if (_external != null) {
        _external.setVisible(false);
        _external.dispose();
        _external = null;
        
      }
     
      
    }
    
  }
  




	
}
