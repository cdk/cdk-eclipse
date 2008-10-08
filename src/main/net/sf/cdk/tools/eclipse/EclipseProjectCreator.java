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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.cdk.tools.CDKModule;
import net.sf.cdk.tools.CDKModuleTool;

public class EclipseProjectCreator {

    private static Map<String, String> jarToProjectMap;
    
    static {
        jarToProjectMap = new HashMap<String, String>();
        jarToProjectMap.put("vecmath1.2-1.14.jar", "javax.vecmath");
        jarToProjectMap.put("jama-1.0.2.jar", "org.jama");
        jarToProjectMap.put("xom-1.1.jar", "net.bioclipse.xom");
        jarToProjectMap.put("jumbo-5.4.2-b2.jar", "net.bioclipse.cml");
        jarToProjectMap.put("jumbo-with-fix-by-jonalv.jar", "net.bioclipse.cml");
        jarToProjectMap.put("log4j.jar", "org.apache.log4j");
        jarToProjectMap.put("jgrapht-0.6.0.jar", "org.3pq.jgrapht");
        jarToProjectMap.put("xpp3-1.1.4c.jar", "org.xmlpull.xpp3");
        jarToProjectMap.put("sjava-0.68.jar", "org.omegahat.sjava");
        jarToProjectMap.put("JRI.jar", "org.rosuda.jri");
        jarToProjectMap.put("jniinchi-0.4.jar", "net.sf.jniinchi");
    }
    
    private final String ROOTARG = "--root=";
    private final String TAGARG = "--tag=";
    
    private String root;
    private String tag;

    private String outputPath = "exports" + File.separator + (tag == null ? "" : (tag + File.separator));
    private String version = "1.1.0.20080907";

    private List<CDKModule> modules = new ArrayList<CDKModule>();
    
    private EclipseProjectCreator() {
        root = "../../cdk";
        tag = null;
    }

    private void findModules() {
        modules = CDKModuleTool.findModules(root + (tag == null ? "" : (File.separator + tag)));
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
                System.out.println("Set root to: " + root);
            } else if (arg.startsWith(TAGARG)) {
                tag = arg.substring(TAGARG.length());
                System.out.println("Set tag to: " + tag);
            }
        }
    }

    private void createEclipseProjects() throws IOException {
        for (CDKModule module : modules) {
            System.out.println("Processing " + module.getName() + "...");
            String projectName = "org.openscience.cdk." + module.getName();
            File projectFolder = createProjectFolder(projectName);
            if (projectFolder.exists()) {
                createDotProjectFile(projectName, projectFolder);
                createDotClasspathFile(projectName, module, projectFolder);
                createManifestFile(projectName, module, projectFolder);
                createBuildProperties(projectName, module, projectFolder);
                copyCDKJar(module, projectFolder);
            } else {
                System.out.println("Creation of folder failed...");
            }
        }
    }

    private void copyCDKJar(CDKModule module, File projectFolder) {
        File input = new File(
            root + 
            (tag == null ? "" : (File.separator + tag)) +
            File.separator + "dist" +
            File.separator + "jar" +
            File.separator + "cdk-" + module.getName() + ".jar"
        );
        if (input.exists() && input.canRead()) {
            File output = new File(
                                   projectFolder.getPath() + 
                                   File.separator + "jar"+ 
                                   File.separator + "cdk-" + module.getName() + ".jar"
            );
            try {
                copyFile(input, output);
            } catch ( Exception e ) {
                System.out.println("Could not copy module jar: " + module.getName());
                e.printStackTrace();
            }
        }
    }

    private void copyFile(File input, File output) throws Exception {
        FileInputStream iStream = new FileInputStream(input);
        FileOutputStream oStream = new FileOutputStream(output);
        FileChannel iChannel = iStream.getChannel();
        FileChannel oChannel = oStream.getChannel();
        iChannel.transferTo(0, iChannel.size(), oChannel);
        iStream.close();
        oStream.close();
    }

    private void createDotProjectFile(String projectName, File projectFolder) throws IOException {
        File dotProjectFile = new File(projectFolder.getPath()+ "/.project");
        PrintWriter writer = new PrintWriter(new FileWriter(dotProjectFile));
        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.println("<projectDescription>");
        writer.println("  <name>" +  projectName + "</name>");
        writer.println("  <comment></comment>");
        writer.println("  <projects>");
        writer.println("  </projects>");
        writer.println("  <buildSpec>");
        writer.println("    <buildCommand>");
        writer.println("      <name>org.eclipse.jdt.core.javabuilder</name>");
        writer.println("    </buildCommand>");
        writer.println("    <buildCommand>");
        writer.println("      <name>org.eclipse.pde.ManifestBuilder</name>");
        writer.println("    </buildCommand>");
        writer.println("    <buildCommand>");
        writer.println("      <name>org.eclipse.pde.SchemaBuilder</name>");
        writer.println("    </buildCommand>");
        writer.println("  </buildSpec>");
        writer.println("  <natures>");
        writer.println("    <nature>org.eclipse.jdt.core.javanature</nature>");
        writer.println("    <nature>org.eclipse.pde.PluginNature</nature>");
        writer.println("  </natures>");
        writer.println("</projectDescription>");
        writer.close();
    }

    private void createDotClasspathFile(String projectName, CDKModule module, File projectFolder) throws IOException {
        File dotProjectFile = new File(projectFolder.getPath()+ "/.classpath");
        PrintWriter writer = new PrintWriter(new FileWriter(dotProjectFile));
        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.println("<classpath>");
        writer.println("  <classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>");
        writer.println("  <classpathentry kind=\"con\" path=\"org.eclipse.pde.core.requiredPlugins\"/>");
        writer.println("  <classpathentry exported=\"true\" kind=\"lib\" path=\"jar/cdk-" + module.getName() + ".jar\"/>");
        writer.println("  <classpathentry kind=\"output\" path=\"bin\"/>");
        writer.println("</classpath>");
        writer.close();
    }
    
    private void createBuildProperties(String projectName, CDKModule module, File projectFolder) throws IOException {
        File buildPropertiesFile = new File(projectFolder.getPath()+ "/build.properties");
        PrintWriter writer = new PrintWriter(new FileWriter(buildPropertiesFile));
        writer.println("bin.includes = META-INF/,\\");
        Iterator<String> otherDeps = module.getDependencies().iterator();
        while (otherDeps.hasNext()) {
            writer.print("               jar/" + otherDeps.next());
            if (otherDeps.hasNext()) {
                writer.print(",\\");
            }
            writer.println();
        }            		
        writer.close();
    }
    
    private void createManifestFile(String projectName, CDKModule module, File projectFolder) throws IOException {
        File dotProjectFile = new File(projectFolder.getPath()+ "/META-INF/MANIFEST.MF");
        PrintWriter writer = new PrintWriter(new FileWriter(dotProjectFile));
        writer.println("Manifest-Version: 1.0");
        writer.println("Bundle-ManifestVersion: 2");
        writer.println("Bundle-Name: CDK plugin");
        writer.println("Bundle-SymbolicName: " + projectName + ";singleton:=true");
        writer.println("Bundle-Version: " + version + "");
        writer.println("Bundle-Vendor: The Chemistry Development Kit Project");
        writer.println("Bundle-ActivationPolicy: lazy");
        writer.println("Bundle-Classpath: jar/cdk-" + module.getName() + ".jar");
        writer.println("Bundle-RequiredExecutionEnvironment: J2SE-1.5");

        Iterator<String> cdkDeps = module.getCDKDependencies().iterator();
        Iterator<String> otherDeps = module.getDependencies().iterator();
        if (cdkDeps.hasNext() || otherDeps.hasNext()) {
            writer.print("Require-Bundle:");
            while (cdkDeps.hasNext()) {
                writer.print(" org.openscience.cdk." + cdkDeps.next());
                if (cdkDeps.hasNext() || otherDeps.hasNext()) {
                    writer.print(',');
                }
                writer.println();
            }
            while (otherDeps.hasNext()) {
                String jar = otherDeps.next();
                if (!jarToProjectMap.containsKey(jar)) {
                    System.out.println("Don't know which Eclipse project this jar maps to: " + jar);
                } else {
                    writer.print(" " + jarToProjectMap.get(jar));
                    if (otherDeps.hasNext()) {
                        writer.print(',');
                    }
                    writer.println();
                }
            }
        }

        Iterator<String> pkgs = module.getPackages().iterator();
        if (pkgs.hasNext()) {
            writer.print("Export-Package:");
            while (pkgs.hasNext()) {
                writer.print(" " + pkgs.next());
                if (pkgs.hasNext()) {
                    writer.print(',');
                }
                writer.println();
            }
        }
        
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
