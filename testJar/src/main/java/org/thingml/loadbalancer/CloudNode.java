/**
 * Copyright (C) 2014 SINTEF <franck.fleurey@sintef.no>
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingml.loadbalancer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author sintef
 */
public class CloudNode {
    
        public String name;
        public String ip;
        public int port;
        public int httpPort;
        public int weight;
        public Set<File> tests;
        public Set<String> languages;
        
        public CloudNode(String name) {
            this.name = name;
            this.tests = new HashSet<>();
            this.languages = new HashSet<>();
        }
        
        public void makeTestDir(File workingDir, File ressourcesDir, File compiler, File testJar, File testSrc) throws IOException {
            File testDir = new File(workingDir, name + "_testDir");
            if(testDir.exists()) {
                testDir.delete();
            }
            
            
            testDir.mkdir();
            File cDir = new File(testDir, "compilers");
            cDir.mkdir();
            File regDir = new File(cDir, "registry");
            regDir.mkdir();
            File tarDir = new File(regDir, "target");
            tarDir.mkdir();
            File compLink = new File(tarDir, "compilers.registry-0.7.0-SNAPSHOT-jar-with-dependencies.jar");
            Files.createSymbolicLink(compLink.toPath(), compiler.toPath());
            
            File tjDir = new File(testDir, "testJar");
            tjDir.mkdir();
            File tjtarDir = new File(tjDir, "target");
            tjtarDir.mkdir();
            File tJarLink = new File(tjtarDir, "testJar-0.7.0-SNAPSHOT-jar-with-dependencies.jar");
            Files.createSymbolicLink(tJarLink.toPath(), testJar.toPath());
            writeConfigFile(tjDir);
            
            File srcDir = new File(tjDir, "src");
            srcDir.mkdir();
            File mainDir = new File(srcDir, "main");
            mainDir.mkdir();
            File resourcesDir = new File(mainDir, "resources");
            resourcesDir.mkdir();
            File testSrcDir = new File(resourcesDir, "tests");
            testSrcDir.mkdir();
            
            
            for(File f : testSrc.listFiles()) {
                if(f.isDirectory() && (f.getName().compareTo("core") != 0)) {
                    File dirCopy = new File(testSrcDir, f.getName());
                    dirCopy.mkdir();
                }
            }
            
            File importLink =  new File(testSrcDir, "Import/import");
            File importDir =  new File(testSrc, "Import/import");
            Files.createSymbolicLink(importLink.toPath(), importDir.toPath());
            
            boolean found = false;
            for(String l : languages) {
                if (l.compareToIgnoreCase("arduino") == 0) {
                    found = true;
                    break;
                }
            }
            if(found) {
                File testArduinoLink = new File(resourcesDir, "testArduino.sh");
                File testArduino = new File(ressourcesDir, "testArduino.sh");
                Files.createSymbolicLink(testArduinoLink.toPath(), testArduino.toPath());
            }
            
            for(File t : tests) {
                File tLink =  new File(testSrcDir, t.getParentFile().getName() + "/" + t.getName());
                Files.createSymbolicLink(tLink.toPath(), t.toPath());
            }
            
            File coreLink =  new File(testSrcDir, "core");
            File coreDir =  new File(testSrc, "core");
            Files.createSymbolicLink(coreLink.toPath(), coreDir.toPath());
            
            File dtLink =  new File(testSrcDir, "datatypes.thingml");
            File dt =  new File(testSrc, "datatypes.thingml");
            Files.createSymbolicLink(dtLink.toPath(), dt.toPath());
            
            File tmlLink =  new File(testSrcDir, "thingml.thingml");
            File tml =  new File(testSrc, "thingml.thingml");
            Files.createSymbolicLink(tmlLink.toPath(), tml.toPath());
            
        }
        
        public void writeConfigFile(File workingDir) {
            StringBuilder res = new StringBuilder();
            //    webLink = prop.getProperty("webLink");
            //    myIP = prop.getProperty("myIP");
            //    myHTTPServerPort = prop.getProperty("myHTTPServerPort");
            
            res.append("#############################################\n");
            res.append("#          Config for " + name + "\n");
            res.append("#############################################\n\n");
            
            res.append("#Node Properties\n");
            res.append("webLink=True\n");
            res.append("myIP=" + ip + "\n");
            res.append("myHTTPServerPort=" + httpPort + "\n\n");
            
            res.append("#Languages Selection\n");
            res.append("languageList=");
            boolean isFirst = true;
            for(String l : languages) {
                if(isFirst) isFirst = false;
                else res.append(", ");
                res.append(l);
            }
            res.append("\n\n");
            
            res.append("#Mode Selection\n");
            res.append("useBlackList=False\n\n");
            
            res.append("#Test Selection\n");
            res.append("testList=");
            isFirst = true;
            for(File t : tests) {
                if(isFirst) isFirst = false;
                else res.append(", ");
                res.append(t.getName().split("\\.")[0]);
            }
            res.append("\n\n");
            
            
            File configFile = new File(workingDir, "config.properties");
            try {
                PrintWriter w = new PrintWriter(configFile);
                w.print(res.toString());
                w.close();
            } catch (Exception ex) {
                System.err.println("Problem writing log");
                ex.printStackTrace();
            }
        }
}
