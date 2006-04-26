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
            if (_running && !element.isInValidationQueue()) {
                //MerlotDebug.msg("Validation queue: " + _queue.size() + "; Adding " + element.getNodeName());
                _queue.add(element);

                final int qsize = _queue.size();
				element.setValidationQueueIndex(qsize-1);

                removeParent(element);
            }
        }
    }
    
    void removeParent(MerlotDOMNode node) {
        MerlotDOMNode parent = node.getParentNode();
    	if (parent != null && parent.isInValidationQueue()) {
    		_queue.setElementAt(null, parent.getValidationQueueIndex());
            _queue.add(parent);
    		final int qsize = _queue.size();
			parent.setValidationQueueIndex(qsize-1);
        }
    	if (parent != null) {
            removeParent(parent);
    	}
    }
    
    public void run() {
    	long timeStart = -1;
    	int frontNodeIndex = 0;

    	while (_running) {
            try {
                long timeNow = System.currentTimeMillis();
                if (timeNow - _lastStatusUpdate > 300) {
                    if (_queue.isEmpty()) {
                        displayStatus("");

                        if(timeStart != -1) {
                        	MerlotDebug.msg("Time = "
                        			+ (System.currentTimeMillis()-timeStart));
                        	timeStart = -1;
                        }
                    }
                    else {

                    	if(timeStart == -1) {
                    		timeStart = System.currentTimeMillis();
                    		MerlotDebug.msg("Time start = "
									+ new java.util.Date(timeStart) + " ("
									+ timeStart + ")");
                    	}

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
                	// As the queue is processed first in first out, the element
                	// is nulled out. Find the first non-null element
                	// this allows maintaining the indices the same in the
                	// nodes so they can be cached and we don't end up with
                	// too many calls to equals() to find the node (on a large
                	// file I measured 1.1 trillion calls to equals)
                	while(frontNodeIndex < _queue.size()) {
                		next = (MerlotDOMElement) _queue.get(frontNodeIndex++);
                		if(next != null) {
                			_queue.setElementAt(null, frontNodeIndex-1);
                			next.setValidationQueueIndex(-1);
                			break;
                		}
                	}

                	// Nodes are always added to the end, so if the end is
                	// reached, empty the queue
                	if(frontNodeIndex == _queue.size()) {
                		_queue.clear();
                		frontNodeIndex = 0;
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
        MerlotDebug.msg("Validation thread exiting: "
        		+ Thread.currentThread().toString());
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
