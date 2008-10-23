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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CDKModule {

    private static Map<String, CDKModule> modules = new HashMap<String, CDKModule>();
    
    private String root;
    
    private String name;
    private List<String> cdkDependencies;
    private List<String> dependencies;
    private List<String> packages;
    private List<String> javaFiles;
    
    private CDKModule(String name, String root) throws Exception {
        this.name = name;
        this.root = root;
        this.cdkDependencies = new ArrayList<String>();
        this.dependencies = new ArrayList<String>();
        this.packages = new ArrayList<String>();
        this.javaFiles = new ArrayList<String>();
        loadInfo();
    }
    
    private void loadInfo() throws Exception {
        loadCDKDependencies();
        loadDependencies();
        loadPackages();
    }
    private void loadCDKDependencies() throws Exception {
        File file = new File(
            root + File.separator + "src" + 
                   File.separator + "META-INF" +
                   File.separator + name + ".cdkdepends");
        if (file.exists() && file.canRead()) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null) {
                line = line.trim();
                if (line.length() == 0) {
                    // don't complain about empty lines
                } else if (line.startsWith("cdk-") && line.endsWith(".jar") && line.length() > 8) {
                    String dep = line.substring(4,line.indexOf('.'));
                    addCDKDependency(dep);
                } else {
                    throw new Exception("Incorrect CDK dependency: " + line);
                }
                line = reader.readLine();
            }
        }
    }
    private void loadDependencies() throws Exception {
        File file = new File(
            root + File.separator + "src" + 
                   File.separator + "META-INF" +
                   File.separator + name + ".libdepends");
        if (file.exists() && file.canRead()) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null) {
                line = line.trim();
                if (line.length() == 0) {
                    // don't complain about empty lines
                } else {
                    addDependency(line);
                }
                line = reader.readLine();
            }
        }
    }
    private void loadPackages() throws Exception {
        File file = new File(
            root + File.separator + "build" + 
                   File.separator + name + ".javafiles");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        while (line != null) {
            addJavaFile(line.trim());
            String pkg = line.substring(0,line.lastIndexOf('/'));
            pkg = pkg.replaceAll("\\/", ".");
            if (!hasPackage(pkg)) addPackage(pkg);
            line = reader.readLine();
        }
    }

    public static CDKModule getInstance(String name, String root) throws Exception {
        if (!modules.containsKey( name )) {
            CDKModule newModule = new CDKModule(name, root);
            modules.put( name, newModule );
            return newModule;
        }
        return modules.get( name );
    }
    
    public String getName() {
        return name;
    }
    
    public List<String> getCDKDependencies() {
        return cdkDependencies;
    }
    
    private void addCDKDependency(String module) {
        this.cdkDependencies.add( module );
    }
    
    public List<String> getDependencies() {
        return dependencies;
    }
    
    private void addDependency(String jar) throws Exception {
        this.dependencies.add(jar);
    }
    
    /**
     * Returns a {@link List} of package names this module depends on.
     */
    public List<String> getPackages() {
        return packages;
    }
    
    /**
     * Returns a {@link List} of java source files for this module.
     */
    public List<String> getJavaFiles() {
        return javaFiles;
    }

    private void addPackage(String packageName) {
        this.packages.add( packageName );
    }
    
    private void addJavaFile(String file) {
        this.javaFiles.add(file);
    }

    private boolean hasPackage(String packageName) {
        return getPackages().contains( packageName);
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Module: ").append(getName()).append('\n');
        buffer.append("  Dependencies: ").append('\n');
        for (String dep : getDependencies()) {
            buffer.append("    ").append(dep).append('\n');
        }
        buffer.append("  Packages: ").append('\n');
        for (String pkg : getPackages()) {
            buffer.append("    ").append(pkg).append('\n');
        }
        return buffer.toString();
    }
}
