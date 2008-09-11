/* $Revision: 6707 $ $Author: egonw $ $Date: 2006-07-30 16:38:18 -0400 (Sun, 30 Jul 2006) $
 * 
 * Copyright (C) 2008  Egon Willighagen <egonw@users.sf.net>
 * 
 * Contact: cdk-devel@lists.sourceforge.net
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package net.sf.cdk.tools;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class CDKModuleTool {

    public static List<String> findModules(String root) {
        // construct a list of modules, assuming runDoclet has been run
        List<String> modules = new ArrayList<String>();
        File dir = new File(root + File.separator + "build");
        System.out.println("Root folder: " + dir.getPath());
        File[] files = dir.listFiles(
            new FileFilter() {
                public boolean accept(File file) {
                    return file.getName().endsWith(".javafiles");
                }
            }
        );
        for (int i=0; i<files.length; i++) {
            String name = files[i].getName();
            if (!name.startsWith("test")) {
                String module = name.substring(0, name.indexOf('.'));
                modules.add(module);
            }
        }
        return modules;
    }

}
