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
package net.sf.cdk.tools.eclipse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import net.sf.cdk.tools.CDKModuleTool;

public class EclipseProjectCreator {

    private final String ROOTARG = "--root=";
    private final String TAGARG = "--tag=";
    
    private String outputPath = "build/eclipse/trunk/";
    private String version = "1.1.0.20080907";

    private List<String> modules = new ArrayList<String>();
    
    private String root;
    private String tag;

    private EclipseProjectCreator() {
        root = "../../cdk/branches";
        tag = "1.2.0";
    }

    private void findModules() {
        modules = CDKModuleTool.findModules(root + File.separator + tag);
        System.out.println("Number of modules found: " + modules.size());
    }

    public static void main(String[] args) throws IOException {
        EclipseProjectCreator checker = new EclipseProjectCreator();
        checker.processArguments(args);
        checker.findModules();
        checker.createEclipseProjects();
    }

    private void processArguments( String[] args ) {
        for (String arg : args) {
            if (arg.startsWith(ROOTARG)) {
                root = arg.substring(ROOTARG.length());
            }
        }
    }

    private void createEclipseProjects() throws IOException {
        for (String module : modules) {
            System.out.println("Processing " + module + "...");
            String projectName = "org.openscience.cdk." + module;
            File projectFolder = createProjectFolder(projectName);
            if (projectFolder.exists()) {
                createDotProjectFile(projectName, projectFolder);
                createDotClasspathFile(projectName, projectFolder);
                createManifestFile(projectName, projectFolder);
            } else {
                System.out.println("Creation of folder failed...");
            }
        }
    }

    private void createDotProjectFile(String projectName, File projectFolder) throws IOException {
        File dotProjectFile = new File(projectFolder.getPath()+ "/.project");
        PrintWriter writer = new PrintWriter(new FileWriter(dotProjectFile));
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        writer.write("<projectDescription>\n");
        writer.write("  <name>" +  projectName + "</name>\n");
        writer.write("  <buildSpec>\n");
        writer.write("    <buildCommand>\n");
        writer.write("      <name>org.eclipse.jdt.core.javabuilder</name>\n");
        writer.write("    </buildCommand>\n");
        writer.write("    <buildCommand>\n");
        writer.write("      <name>org.eclipse.pde.ManifestBuilder</name>\n");
        writer.write("    </buildCommand>\n");
        writer.write("  </buildSpec>\n");
        writer.write("  <natures>\n");
        writer.write("    <nature>org.eclipse.jdt.core.javanature</nature>\n");
        writer.write("    <nature>org.eclipse.pde.PluginNature</nature>\n");
        writer.write("  </natures>\n");
        writer.write("</projectDescription>\n");
        writer.close();
    }

    private void createDotClasspathFile(String projectName, File projectFolder) throws IOException {
        File dotProjectFile = new File(projectFolder.getPath()+ "/.classpath");
        PrintWriter writer = new PrintWriter(new FileWriter(dotProjectFile));
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        writer.write("<classpath>\n");
        writer.write("  <classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>\n");
        writer.write("  <classpathentry kind=\"con\" path=\"org.eclipse.pde.core.requiredPlugins\"/>\n");
        // <classpathentry exported="true" kind="lib" path="jar/jgrapht-0.6.0.jar" sourcepath="cdksrc.zip"/>
        writer.write("</classpath>\n");
        writer.close();
    }
    
    private void createManifestFile(String projectName, File projectFolder) throws IOException {
        File dotProjectFile = new File(projectFolder.getPath()+ "/META-INF/MANIFEST.MF");
        PrintWriter writer = new PrintWriter(new FileWriter(dotProjectFile));
        writer.write("Manifest-Version: 1.0\n");
        writer.write("Bundle-ManifestVersion: 2\n");
        writer.write("Bundle-Name: CDK plugin\n");
        writer.write("Bundle-SymbolicName: " + projectName + ";singleton:=true\n");
        writer.write("Bundle-Version: " + version + "\n");
        writer.write("Bundle-Vendor: The Chemistry Development Kit Project\n");
        writer.write("Bundle-ActivationPolicy: lazy\n");
        writer.write("Export-Package: org.openscience.cdk\n");
        writer.write("Bundle-Classpath: .\n");
        writer.close();
    }

    private File createProjectFolder(String projectName) {
        
        File projectDir = new File(outputPath + projectName);
        projectDir.mkdirs();
        File metainfDir = new File(projectDir.getPath() + "/META-INF");
        metainfDir.mkdirs();
        File jarDir = new File(projectDir.getPath() + "/jar");
        jarDir.mkdirs();
        return projectDir;
    }

}
