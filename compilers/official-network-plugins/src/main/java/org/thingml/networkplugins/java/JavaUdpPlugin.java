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
package org.thingml.networkplugins.java;

import org.apache.commons.io.IOUtils;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.sintef.thingml.*;
import org.sintef.thingml.helpers.AnnotatedElementHelper;
import org.thingml.compilers.Context;
import org.thingml.compilers.spi.NetworkPlugin;
import org.thingml.compilers.spi.SerializationPlugin;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JavaUdpPlugin extends NetworkPlugin {

    public JavaUdpPlugin() {
        super();
    }

    public String getPluginID() {
        return "JavaUdpPlugin";
    }

    public List<String> getSupportedProtocols() {
        List<String> res = new ArrayList<>();
        res.add("Udp");
        res.add("UdpPort");
        res.add("UdpPort_0");
        res.add("UdpPort_1");
        res.add("UdpPort_2");
        res.add("UdpPort_3");
        res.add("UdpPort_4");
        res.add("UdpPort_5");
        res.add("UdpPort_6");
        res.add("UdpPort_7");
        res.add("UdpPort_8");
        res.add("UdpPort_9");
        return res;
    }

    public List<String> getTargetedLanguages() {
        List<String> res = new ArrayList<>();
        res.add("java");
        return res;
    }

    final Set<Message> messages = new HashSet<Message>();

    private void clearMessages() {
        messages.clear();
    }

    private void addMessage(Message m) {
        boolean contains = false;
        for(Message msg : messages) {
            if (EcoreUtil.equals(msg, m)) {
                contains = true;
                break;
            }
        }
        if (!contains) {
            messages.add(m);
        }
    }

    public void generateNetworkLibrary(Configuration cfg, Context ctx, Set<Protocol> protocols) {

        StringBuilder builder = new StringBuilder();
        for (Protocol prot : protocols) {
            System.out.println("UdpPlugin found protocol <"+prot.getName()+">");
            
            String serializers = "";
            for (ThingPortMessage tpm : getMessagesSent(cfg, prot)) {
                addMessage(tpm.m);
            }
            SerializationPlugin sp = null;
            try {
                sp = ctx.getSerializationPlugin(prot);
            } catch (UnsupportedEncodingException uee) {
                System.err.println("Could not get serialization plugin... Expect some errors in the generated code");
                uee.printStackTrace();
                return;
            }
            
            for (Message m : messages) {
                StringBuilder temp = new StringBuilder();
                System.out.println("Generates serialization for message "+m.getName());
                serializers += sp.generateSerialization(temp, prot.getName() + "BinaryProtocol", m);
            }
            messages.clear();
            builder = new StringBuilder();
            for (ThingPortMessage tpm : getMessagesReceived(cfg, prot)) {
                addMessage(tpm.m);
            }
            sp.generateParserBody(builder, prot.getName() + "BinaryProtocol", null, messages, null);
            final String result = builder.toString().replace("/*$SERIALIZERS$*/", serializers);
            try {
                final File folder = new File(ctx.getOutputDirectory() + "/src/main/java/org/thingml/generated/network");
                folder.mkdir();
                final File f = new File(ctx.getOutputDirectory() + "/src/main/java/org/thingml/generated/network/" + prot.getName() + "BinaryProtocol.java");
                final OutputStream output = new FileOutputStream(f);
                IOUtils.write(result, output, Charset.forName("UTF-8"));
                IOUtils.closeQuietly(output);
            } catch (Exception e) {
                e.printStackTrace();
            }
            new SerialProtocol(ctx, prot, cfg).generate();
        }
    }

    private class SerialProtocol {
        Context ctx;
        Protocol prot;
        Configuration cfg;
        String instance;

        private List<Port> ports = new ArrayList<Port>();

        public SerialProtocol(Context ctx, Protocol prot, Configuration cfg) {
            this.ctx = ctx;
            this.prot = prot;
            this.cfg = cfg;
            this.instance = prot.getName();
        }

        private void addPort(Port p) {
            boolean contains = false;
            for (Port port : ports) {
                if (EcoreUtil.equals(port, p)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                ports.add(p);
            }
        }

        public void generate() {
            for (ThingPortMessage tpm : getMessagesSent(cfg, prot)) {
                addPort(tpm.p);
            }
            for (ThingPortMessage tpm : getMessagesReceived(cfg, prot)) {
                addPort(tpm.p);
            }
            String template = ctx.getTemplateByID("templates/JavaUdpPlugin.java");
            template = template.replace("/*$INSTANCE$*/", instance);
            template = template.replace("/*$SERIALIZER$*/", prot.getName() + "BinaryProtocol");
            StringBuilder parseBuilder = new StringBuilder();
            parseBuilder.append("final Event event = " + prot.getName() + "BinaryProtocol.instantiate(payload);\n");
            for(Port p : ports) {//FIXME
                parseBuilder.append("if (event != null) " + p.getName() + "_port.send(event);\n");
            };
            template = initPort(ctx, template);
            for (ExternalConnector conn : getExternalConnectors(cfg, prot)) {
                updateMain(ctx, cfg, conn);
            }
            template = template.replace("/*$PARSING CODE$*/", parseBuilder.toString());

            try {
                final File folder = new File(ctx.getOutputDirectory() + "/src/main/java/org/thingml/generated/network");
                folder.mkdir();
                final File f = new File(ctx.getOutputDirectory() + "/src/main/java/org/thingml/generated/network/UdpJava"+instance+".java");
                final OutputStream output = new FileOutputStream(f);
                IOUtils.write(template, output, Charset.forName("UTF-8"));
                IOUtils.closeQuietly(output);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String initPort(Context ctx, String template) {
            for (Port p : ports) {
                template = template.replace("/*$PORTS$*/", "/*$PORTS$*/\nprivate Port " + p.getName() + "_port;\npublic Port get" + ctx.firstToUpper(p.getName()) + "_port(){return " + p.getName() + "_port;}\n");
                String portType = "PortType.PROVIDED";
                if (p instanceof RequiredPort)
                    portType = "PortType.REQUIRED";

                template = template.replace("/*$INIT PORTS$*/", "/*$INIT PORTS$*/\n" + p.getName() + "_port = new Port(" + portType + ", \"" + p.getName() + "\", this);\n");
            }
            return template;
        }

        private void updateMain(Context ctx, Configuration cfg, ExternalConnector conn) {
            try {
                final InputStream input = new FileInputStream(ctx.getOutputDirectory() + "/src/main/java/org/thingml/generated/Main.java");
                final List<String> packLines = IOUtils.readLines(input);
                String main = "";
                for (String line : packLines) {
                    main += line + "\n";
                }
                input.close();
                //final String speed = AnnotatedElementHelper.hasAnnotation(conn.getProtocol(), "baudrate") ? AnnotatedElementHelper.annotation(conn.getProtocol(), "baudrate").get(0) : "9600";
                //final String port = AnnotatedElementHelper.hasAnnotation(conn.getProtocol(), "port") ? AnnotatedElementHelper.annotation(conn.getProtocol(), "port").get(0) : "/dev/ttyACM0";
                final String localPort = AnnotatedElementHelper.hasAnnotation(conn.getProtocol(), "udp_local_port") ? AnnotatedElementHelper.annotation(conn.getProtocol(), "udp_local_port").get(0) : "???";
                final String remoteAddress = AnnotatedElementHelper.hasAnnotation(conn.getProtocol(), "udp_remote_address") ? AnnotatedElementHelper.annotation(conn.getProtocol(), "udp_remote_address").get(0) : "???";
                final String remotePort = AnnotatedElementHelper.hasAnnotation(conn.getProtocol(), "udp_remote_port") ? AnnotatedElementHelper.annotation(conn.getProtocol(), "udp_remote_port").get(0) : "???";
                System.out.println("UdpPlugin found following annotations <"+localPort+"> <"+remoteAddress+"> <"+remotePort+">");
                main = main.replace("/*$NETWORK$*/", "/*$NETWORK$*/\nUdpJava"+instance+" " + conn.getName() + "_" + conn.getProtocol().getName() + " = (UdpJava"+instance+") new UdpJava"+instance+"(\"" + localPort + "\", \"" + remoteAddress + "\", \"" + remotePort + "\").buildBehavior(null, null);\n");

                StringBuilder connBuilder = new StringBuilder();
                connBuilder.append(conn.getName() + "_" + conn.getProtocol().getName() + ".get" + ctx.firstToUpper(conn.getPort().getName()) + "_port().addListener(");
                connBuilder.append(ctx.getInstanceName(conn.getInst().getInstance()) + ".get" + ctx.firstToUpper(conn.getPort().getName()) + "_port());\n");
                connBuilder.append(ctx.getInstanceName(conn.getInst().getInstance()) + ".get" + ctx.firstToUpper(conn.getPort().getName()) + "_port().addListener(");
                connBuilder.append(conn.getName() + "_" + conn.getProtocol().getName() + ".get" + ctx.firstToUpper(conn.getPort().getName()) + "_port());\n");
                main = main.replace("/*$EXT CONNECTORS$*/", "/*$EXT CONNECTORS$*/\n" + connBuilder.toString());

                main = main.replace("/*$START$*/", "/*$START$*/\n" + conn.getName() + "_" + conn.getProtocol().getName() + ".init().start();\n");
                main = main.replace("/*$STOP$*/", "/*$STOP$*/\n" + conn.getName() + "_" + conn.getProtocol().getName() + ".stop();\n");

                final File f = new File(ctx.getOutputDirectory() + "/src/main/java/org/thingml/generated/Main.java");
                final OutputStream output = new FileOutputStream(f);
                IOUtils.write(main, output, Charset.forName("UTF-8"));
                IOUtils.closeQuietly(output);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
