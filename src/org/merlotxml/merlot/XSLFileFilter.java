package org.merlotxml.merlot;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * File Filter for XSL stylesheets
 *
 * @author Trang Nguyen
 */
public class XSLFileFilter extends FileFilter {

    // Accept xsl files or directories
    public boolean accept(File f) {

        if(f != null) {
            if(f.isDirectory()) {
                return true;
            }
            String name = f.getName();
            if (name.endsWith(".xsl"))
                return true;
        }
        return false;
    }

    // The description of this filter
    public String getDescription() {
        return "*.xsl";
    }
}

