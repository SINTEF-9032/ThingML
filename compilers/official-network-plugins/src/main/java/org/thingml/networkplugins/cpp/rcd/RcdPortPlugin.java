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
package org.thingml.networkplugins.cpp.rcd;

import java.io.UnsupportedEncodingException;
import org.sintef.thingml.*;
import org.thingml.compilers.Context;
import org.thingml.compilers.c.CCompilerContext;
import org.thingml.compilers.spi.NetworkPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.sintef.thingml.helpers.AnnotatedElementHelper;
import org.thingml.compilers.spi.SerializationPlugin;

/**
 *
 * @author steffend
 */
public class RcdPortPlugin extends NetworkPlugin {

    CCompilerContext ctx;
    Set<RcdPort> rcdPorts;

    public String getPluginID() {
        return "RcdPortPlugin";
    }

    public String getCppNameScope() {
        return "/*CFG_CPPNAME_SCOPE*/";
    }
        
    public List<String> getSupportedProtocols() {
        List<String> res = new ArrayList<>();
        res.add("RcdPort_0");
        res.add("RcdPort_1");
        res.add("RcdPort_2");
        res.add("RcdPort_3");
        res.add("RcdPort_4");
        res.add("RcdPort_5");
        res.add("RcdPort_6");
        res.add("RcdPort_7");
        res.add("RcdPort_8");
        res.add("RcdPort_9");
        res.add("RcdPortTunnel_0");
        res.add("RcdPortTunnel_1");
        res.add("RcdPortTunnel_2");
        res.add("RcdPortTunnel_3");
        res.add("RcdPortTunnel_4");
        res.add("RcdPortTunnel_5");
        res.add("RcdPortTunnel_6");
        res.add("RcdPortTunnel_7");
        res.add("RcdPortTunnel_8");
        res.add("RcdPortTunnel_9");
        return res;
    }

    public String getTargetedLanguage() {
        return "sintefboard";
    }

    public void generateNetworkLibrary(Configuration cfg, Context ctx, Set<Protocol> protocols) {
        this.ctx = (CCompilerContext) ctx;
        rcdPorts = new HashSet<RcdPort>();
        
        //System.out.println("RcdPortPlugin.generateNetworkLibrary() " + protocols);
        if (!protocols.isEmpty()) {
            StringBuilder builder = new StringBuilder();

            ctx.getBuilder("hashdefines").append("#define RCDPORT_IN_USE\n");

            //************ Generate method for receiving messages from rcdports

            //This header is part of the "sintefboard_main_header.h" template file
            //headerbuilder.append("// Receive forwarding of messages from ports\n");
            //headerbuilder.append("void " + "rcdport_receive_forward(msgc_t *msg_in_ptr, int16_t from_port)");
            //headerbuilder.append(";\n");
            builder.append("// Receive forwarding of messages from ports\n");
            builder.append("void " + getCppNameScope() + "rcd_port_receive_forward(msgc_t *msg_in_ptr, int16_t from_port)");
            builder.append("{\n");
            builder.append("switch (from_port) {\n");

            for (Protocol prot : protocols) {
                RcdPort port = new RcdPort();
                rcdPorts.add(port);
                port.protocol = prot;
                port.cfg = cfg;
                //System.out.println("Protocol " + prot.getName() + " => "+ prot.getAnnotations());
                try {
                    port.sp = ctx.getSerializationPlugin(prot);
                } catch (UnsupportedEncodingException uee) {
                    System.err.println("Could not get serialization plugin... Expect some errors in the generated code");
                    uee.printStackTrace();
                    return;
                }
                for (ExternalConnector eco : this.getExternalConnectors(cfg, prot)) {
                  port.ecos.add(eco);
                  eco.setName(eco.getProtocol().getName());
                }
                port.generateNetworkLibrary(this.ctx);

                String portnum = port.getPortNumber();
                String portName = prot.getName();
                builder.append("// "+portName+" portnum is() " + portnum + "\n");
                builder.append("case " + portnum + ":\n");
                builder.append(portName + "_parser(msg_in_ptr);\n");
                builder.append("break;\n");

            }
            builder.append("} // switch from port\n");
            builder.append("}\n");

            ctx.getBuilder("RcdPortPlugin" + ".c").append(builder.toString());

            generatePortInfo();
        }
        
    }

    @Override
    public List<String> getTargetedLanguages() {

        List<String> res = new ArrayList<>();
        res.add("sintefboard");
        return res;
    }
    
    private void generatePortInfo() {
        StringBuilder rcdportinfoBuilder = new StringBuilder();
        
        rcdportinfoBuilder.append("#define NUM_OF_PORTS_IN_TASK "+rcdPorts.size()+"\n\n");  
        rcdportinfoBuilder.append("const static port_info_entry_t _class_port_info[] = {\n");  

        for (RcdPort port : rcdPorts) {
            rcdportinfoBuilder.append("{"+port.getPortNumber()+", "+port.getPortRole()+", \""+port.getPortName()+"\"},\n");  
        }
        rcdportinfoBuilder.append("{-1, PORT_END, \"\"},\n");
        rcdportinfoBuilder.append("};\n");  

        ctx.getBuilder("rcdportinfo").append(rcdportinfoBuilder.toString());
    }

    private class RcdPort {
        Configuration cfg;
        Set<ExternalConnector> ecos;
        Protocol protocol;
        SerializationPlugin sp;

        RcdPort() {
            ecos = new HashSet<>();
        }
        
        public String getPortNumber() {
            return AnnotatedElementHelper.annotation(protocol, "rcdport_number").iterator().next();
        }

        public String getPortRole() {
            String ret = "";
            Port p = getPorts(cfg, protocol).iterator().next();
            if (p instanceof RequiredPort) {
                ret = "PORT_REQUIRED";
            }
            if (p instanceof ProvidedPort) {
                ret = "PORT_PROVIDED";
            }
            return ret;
        }
        
        public String getPortName() {
            String ret = "";
            Port p = getPorts(cfg, protocol).iterator().next();
            Thing t = getThings(cfg, protocol).iterator().next();
            ret = t.getName()+":"+p.getName();
            
            if (AnnotatedElementHelper.hasAnnotation(protocol, "rcdport_name")) {
                ret = AnnotatedElementHelper.annotation(protocol, "rcdport_name").iterator().next();
            }

            return ret;
        }
        
        public String getPortTunnelMsgidName() {
            String ret = AnnotatedElementHelper.annotation(protocol, "rcdport_tunnel_msgid").iterator().next();
            return ret;
        }
        
        public String getShimName() {
            String ret = "none";
            
            if (AnnotatedElementHelper.hasAnnotation(protocol, "shim_name")) {
                ret = AnnotatedElementHelper.annotation(protocol, "shim_name").iterator().next();
            }
            ret = ret.toLowerCase();
            return ret;
        }
        
        public boolean noSerializing() {
            List<String> formats = sp.getSupportedFormat();
            boolean ret = formats.get(0).contentEquals("None");
            
            return ret;
        }
        
        public boolean useShim() {
            boolean ret = false;
            
            if (getShimName().contentEquals("none")) {
                ret = false;
            } else {
                ret = true;
            }
            return ret;
        }
        
        public void printPortInfo(int i, Port p) {
            System.out.print("RcdPort("+i+") "+ protocol.getName() +" is connected to port "+ p.getName()+ " ");
            if (p instanceof RequiredPort) {
                System.out.print("ProvidedPort ");
            }
            if (p instanceof ProvidedPort) {
                System.out.print("RequiredPort ");
            }
            System.out.println();
        }
        
        public void generateMessageForwarders(StringBuilder builder, StringBuilder headerbuilder) {
            for (ThingPortMessage tpm : getMessagesSent(cfg, protocol)) {
                Thing t = tpm.t;
                Port p = tpm.p;
                Message m = tpm.m;
                

                headerbuilder.append("// Forwarding of messages " + protocol.getName() + "::" + t.getName() + "::" + p.getName() + "::" + m.getName() + "\n");
                headerbuilder.append("void " + "forward_" + protocol.getName() + "_" + ctx.getSenderName(t, p, m));
                ctx.appendFormalParameters(t, headerbuilder, m);
                headerbuilder.append(";\n");

                builder.append("// Forwarding of messages " + protocol.getName() + "::" + t.getName() + "::" + p.getName() + "::" + m.getName() + "\n");
                builder.append("void " + getCppNameScope() + "forward_" + protocol.getName() + "_" + ctx.getSenderName(t, p, m));
                ctx.appendFormalParameters(t, builder, m);
                builder.append("{\n");

                String portnum = getPortNumber();

                if (noSerializing()) {
                    // No serializing, just map ThingML message to Rcd message
                    String msgid = AnnotatedElementHelper.annotation(m, "rcdport_msgid").iterator().next();

                    builder.append("msgc_t   msg_out;      // Outgoing message\n");
                    builder.append("if( Ports_ptr->IsConnected(" + portnum + ") ) {\n");
                    builder.append("APP_MSGC_comp_" + msgid + "(&msg_out" + ctx.getActualParametersSection(m) + ");\n");
                    builder.append("Ports_ptr->SendMsgc(" + portnum + ", &msg_out);\n");
                    builder.append("}\n");
                    
                } else {
                    // Serializing specified, create a rcd tunnel
                    String i = sp.generateSerialization(builder, "forward_buf", m);
                    if(useShim()) {
                        builder.append("to_shim_" + protocol.getName() + "(forward_buf, " + i + ");\n");
                    } else {
                        // Bypass the shim section
                        builder.append("to_transport_" + protocol.getName() + "(forward_buf, " + i + ");\n");
                    }
                }
                builder.append("}\n\n");
            }
        }

        public void generatePortDeserializer(StringBuilder builder, StringBuilder headerbuilder) {
            
            String portName = protocol.getName();
            
            headerbuilder.append("void " + portName + "_parser(msgc_t *msg_in_ptr);\n");

            builder.append("void " + getCppNameScope() + " " + portName + "_parser(msgc_t *msg_in_ptr) {\n");
            builder.append("switch (msg_in_ptr->MsgId) {\n");
            
            if (noSerializing()) {
                // No serializing, just map ThingML message to Rcd message
                
                for (ThingPortMessage tpm : getMessagesReceived(cfg, protocol)) {
                    Thing t = tpm.t;
                    Port p = tpm.p;
                    Message m = tpm.m;

                    Set<String> ignoreList = new HashSet<String>();
                    String msgid = AnnotatedElementHelper.annotation(m, "rcdport_msgid").iterator().next();
                    builder.append("//m.annotation(rcdport_msgid) is " +  msgid + "\n");
                    //String decompproto = m.annotation("rcdport_decompproto").iterator().next();
                    //parserBuilder.append("//m.annotation(rcdport_decompproto) is " +  decompproto + "\n");
                    builder.append("case " + msgid + ":\n");
                    builder.append("{\n");
                    ctx.appendFormalParameterDeclarations(builder, m);
                    builder.append("APP_MSGC_decomp_" + msgid + "(msg_in_ptr" + ctx.getActualPtrParametersSection(m) + ");\n");
                    builder.append("{\n");
                    ctx.generateSerializationForForwarder(m, builder, ctx.getHandlerCode(cfg, m), ignoreList);
                    builder.append("externalMessageEnqueue(forward_buf, " + (ctx.getMessageSerializationSize(m) - 2) + ", " + portName + "_instance.listener_id);\n");
                    builder.append("}\n");
                    builder.append("}\n");
                    builder.append("break;\n");
                }
            } else {
                // Serializing specified, create a rcd tunnel
                
                String tunnel_msgid = getPortTunnelMsgidName();
                builder.append("case " + tunnel_msgid + ":\n");
                builder.append("{\n");
                builder.append("uint8_t *rcv_buf_ptr;\n");
                builder.append("uint8_t rcv_len;\n");
                builder.append("APP_MSGC_decomp_" + tunnel_msgid + "(msg_in_ptr, &rcv_buf_ptr, &rcv_len);\n");

                if(useShim()) {
                    builder.append("from_transport_" + protocol.getName() + "(rcv_buf_ptr, rcv_len);\n");
                } else {
                    // Bypass the shim section
                    builder.append("from_shim_" + protocol.getName() + "(rcv_buf_ptr, rcv_len);\n");
                }
                
                builder.append("}\n");
                builder.append("break;\n");
                
            }
            builder.append("} // switch MsgId \n");
            builder.append("}\n\n");
        }
        
        public void generateToTransportFunction(StringBuilder builder, StringBuilder headerbuilder) {
            
            String portName = protocol.getName();

            if (noSerializing()) {
                // No serializing, no need for to_transport function
            } else {
                // Serializing specified, create to_transport function
                String portnum = getPortNumber();
                String tunnel_msgid = getPortTunnelMsgidName();
            
                headerbuilder.append("void to_transport_" + portName + "( uint8_t *buf_ptr, uint8_t buf_len);\n");

                builder.append("void " + getCppNameScope() + "to_transport_" + portName + "( uint8_t *buf_ptr, uint8_t buf_len) {\n");
                builder.append("msgc_t   msg_out;      // Outgoing message\n");
                builder.append("if( Ports_ptr->IsConnected(" + portnum + ") ) {\n");
                builder.append("APP_MSGC_comp_" + tunnel_msgid + "(&msg_out, buf_ptr, buf_len);\n");
                builder.append("Ports_ptr->SendMsgc(" + portnum + ", &msg_out);\n");
                builder.append("}\n");
                builder.append("}\n\n");
            }
        }
        
        public void generateFromShimFunction(StringBuilder builder, StringBuilder headerbuilder) {
            
            String portName = protocol.getName();

            if (noSerializing()) {
                // No serializing, no need for from_shim function
            } else {
                // Serializing specified, create from_shim function
                Set<Message> rcvMessages = new HashSet();
                for (ThingPortMessage tpm : getMessagesReceived(cfg, protocol)) {
                    Message m = tpm.m;
                    rcvMessages.add(m);
                }                
            
                headerbuilder.append("void from_shim_" + portName + "( uint8_t *buf_ptr, uint8_t buf_len);\n");

                builder.append("void " + getCppNameScope() + "from_shim_" + portName + "( uint8_t *buf_ptr, uint8_t buf_len) {\n");
                sp.generateParserBody(builder, "buf_ptr", "buf_len", rcvMessages, portName + "_instance.listener_id");
                builder.append("}\n\n");
            }
        }
        
        public String getSection(String source_string, String start_string, String end_string) {
            int startIdx = source_string.indexOf(start_string);
            int endIdx = source_string.lastIndexOf(end_string) + end_string.length();
            String ret = source_string.substring(startIdx, endIdx);
            return ret;
        }
        
        void generateNetworkLibrary(CCompilerContext ctx) {
            if (!ecos.isEmpty()) {
                String ctemplate = ctx.getTemplateByID("templates/RcdPortForward.c");
                String htemplate = ctx.getTemplateByID("templates/RcdPortForward.h");

                String portName = protocol.getName();
                
                if (useShim()) {
                    String ctemplateTransport = ctx.getTemplateByID("templates/RcdPortShimNone.c");
                    String htemplateTransport = ctx.getTemplateByID("templates/RcdPortShimNone.h");

                    String shimName = getShimName();
                    System.out.println("Protocol " + portName + " using shim(" + shimName + ")");
                    if( shimName.contentEquals("ack")) {
                        ctemplateTransport = ctx.getTemplateByID("templates/RcdPortShimAck.c");
                        htemplateTransport = ctx.getTemplateByID("templates/RcdPortShimAck.h");
                    }
                    
                    // Merge transport into base template
                    ctemplate = ctemplate.replace("/*SHIM_FUNCTIONS*/", ctemplateTransport);
                    
                    htemplate = htemplate.replace("/*SHIM_INFORMATION*/", getSection(htemplateTransport, "/*SHIM_INFO_START*/", "/*SHIM_INFO_END*/"));
                    htemplate = htemplate.replace("/*SHIM_PROTOTYPES*/", getSection(htemplateTransport, "/*SHIM_PROTO_START*/", "/*SHIM_PROTO_END*/"));
                    
                    String txBufSize = AnnotatedElementHelper.hasAnnotation(protocol, "shim_tx_buf_size") ? AnnotatedElementHelper.annotation(protocol, "shim_tx_buf_size").get(0) : "40";
                    htemplate = htemplate.replace("/*TX_BUF_SIZE*/", txBufSize);
                    System.out.println("Protocol " + portName + " using annontation shim_tx_buf_size(" + txBufSize + ")");
                    String mtuSize = AnnotatedElementHelper.hasAnnotation(protocol, "shim_mtu_size") ? AnnotatedElementHelper.annotation(protocol, "shim_mtu_size").get(0) : "14";
                    htemplate = htemplate.replace("/*MTU_SIZE*/", mtuSize);
                    System.out.println("Protocol " + portName + " using annontation shim_mtu_size(" + mtuSize + ")");
                    String timeoutMs = AnnotatedElementHelper.hasAnnotation(protocol, "shim_timeout_ms") ? AnnotatedElementHelper.annotation(protocol, "shim_timeout_ms").get(0) : "2000";
                    htemplate = htemplate.replace("/*TIMEOUT_MS*/", timeoutMs);
                    System.out.println("Protocol " + portName + " using annontation shim_timeout_ms(" + timeoutMs + ")");
                } else {
                    if (!noSerializing()) {
                        System.out.println("Protocol " + portName + " no shim specified");
                    }
                }

                ctemplate = ctemplate.replace("/*PORT_NAME*/", portName);
                htemplate = htemplate.replace("/*PORT_NAME*/", portName);

                StringBuilder b = new StringBuilder();
                StringBuilder h = new StringBuilder();
                
                generatePortDeserializer(b, h);

                generateToTransportFunction(b, h);
                
                generateFromShimFunction(b, h);
                
                generateMessageForwarders(b, h);

                ctemplate += "\n" + b;
                htemplate += "\n" + h;

                ctx.getBuilder("|_LAST_"+protocol.getName() + ".c").append(ctemplate);
                ctx.getBuilder("|_LAST_"+protocol.getName() + ".h").append(htemplate);

                ctx.addToInitCode("\n" + portName + "_instance.listener_id = add_instance(&" + portName + "_instance);\n");

                int i = 0;
                for (Port p : getPorts(cfg, protocol)) {
                    printPortInfo(++i, p);
                }
            }
        }
    }
}
