package org.merlotxml.merlot;

import java.util.Vector;

/**
 * @author everth
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ValidationThread extends Thread {
    
    XMLEditorDoc _xmlEditorDoc = null;
    public static boolean _running = true;
    volatile Vector _queue = new Vector();
    long _lastStatusUpdate = 0;
    short _lastStatusMessageNumber = 0;
    
    public ValidationThread(XMLEditorDoc xmlEditorDoc) {
        _running = true;
        _xmlEditorDoc = xmlEditorDoc;
        this.setPriority(Thread.MIN_PRIORITY);
        this.start();
    }
    
    public void addElementToValidationQueue(MerlotDOMElement element) {
        synchronized (_queue) {
            if (_running && !_queue.contains(element)) {
                //MerlotDebug.msg("Validation queue: " + _queue.size() + "; Adding " + element.getNodeName());
                _queue.add(element);
                removeParent(element);
            }
        }
    }
    
    void removeParent(MerlotDOMNode node) {
        MerlotDOMNode parent = node.getParentNode();
        if (parent != null && _queue.contains(parent)) {
            _queue.remove(parent);
            _queue.add(parent);
        }
        if (parent != null)
            removeParent(parent);
    }
    
    public void run() {
        while (_running) {
            try {
                long timeNow = System.currentTimeMillis();
                if (timeNow - _lastStatusUpdate > 300) {
                    if (_queue.isEmpty())
                        displayStatus("");
                    else {
                        if (_lastStatusMessageNumber == 0) {
                            displayStatus("Validating elements");
                            _lastStatusMessageNumber = 1;
                        } else if (_lastStatusMessageNumber == 1) {
                            displayStatus("Validating elements.");
                            _lastStatusMessageNumber = 2;
                        } else if (_lastStatusMessageNumber == 2) {
                            displayStatus("Validating elements..");
                            _lastStatusMessageNumber = 3;
                        } else if (_lastStatusMessageNumber == 3) {
                            displayStatus("Validating elements...");
                            _lastStatusMessageNumber = 0;
                        }
                    }
                    _lastStatusUpdate = timeNow;
                }
                MerlotDOMElement next = null;
                synchronized (_queue) {
                    if (!_queue.isEmpty()) {
                        next = (MerlotDOMElement) _queue.remove(0);
                        //MerlotDebug.msg("Validation queue: " + _queue.size() + "; Now processing " + next.getNodeName());
                    }
                }
                if (next != null) {
                    next.validateNow();
                } else
                    sleep(100);
            } catch (Throwable t) {
                MerlotDebug.msg("Exception during validation: " + t);
                t.printStackTrace();
            }
        }
    }
    
    public void waitForValidationToFinish() {
        while (_running && !_queue.isEmpty()) {
            try {
                sleep(10);
            } catch (InterruptedException e) {
            }
        }
    }
    
    void displayStatus(String status) {
        XMLEditorDocUI ui = _xmlEditorDoc.getXMLEditorDocUI();
        if (ui == null)
            return;
        ui.setStatus(status);
        //ui.invalidate();
        //ui.revalidate();
        //ui.repaint();
    }
}
