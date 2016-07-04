package org.thingml.generated.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
//import java.net.SocketTimeoutException;
import org.thingml.generated.messages.*;
import org.thingml.java.*;
import org.thingml.java.ext.*;

import java.util.Arrays;

public class UdpJava/*$INSTANCE$*/ extends Component {

    private int localPort = 0;
    private int remotePort = 0;
    private InetAddress remoteIpAddress = null;
    
    private DatagramSocket udpSocket = null;

    private final byte[] START_SEQUENCE = {0x12, 0x14, 0x16, 0x18};
    
    private final String instName = "UdpJava/*$INSTANCE$*/: ";

    /*$MESSAGE TYPES$*/

    /*$PORTS$*/

    public UdpJava/*$INSTANCE$*/(final String localPortString, final String remoteIpAddressString, final String remotePortString) {
        
        try {
            this.localPort = Integer.decode(localPortString);
            this.remotePort = Integer.decode(remotePortString);
            this.remoteIpAddress = InetAddress.getByName(remoteIpAddressString);

            this.udpSocket = new DatagramSocket(localPort);
           
            new Thread(new UdpPortReader()).start();
           
            System.out.println(instName +"Open socket from "+this.localPort+" to addr "+this.remoteIpAddress+":"+this.remotePort);
            
        } catch (Exception ex) {
            System.out.println(instName +"Got exception: "+ex);
            ex.printStackTrace();
        }
    }

    private void send(final byte[] payload) {
        try {
           byte[] sendData = new byte[START_SEQUENCE.length+payload.length];
           for (int i = 0; i < START_SEQUENCE.length; i++) sendData[i] = START_SEQUENCE[i];
           for (int i = 0; i < payload.length; i++) sendData[i + START_SEQUENCE.length] = payload[i];
           
           System.out.println(instName +"Sending packet len "+sendData.length+" to addr "+this.remoteIpAddress+":"+this.remotePort);
           System.out.print("[");
           for (int i = 0; i < sendData.length; i++) System.out.printf("%02X ", sendData[i]);
           System.out.println("]");

           DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, this.remoteIpAddress, this.remotePort);
           this.udpSocket.send(sendPacket);

        } catch (Exception ex) {
            System.out.println(instName +"Got exception: "+ex);
            ex.printStackTrace();
        }
    }

    private void parse(final byte[] payload) {
        /*$PARSING CODE$*/
    }

    @Override
    public void run() {
        while (active) {
            try {
                final Event e = queue.take();//should block if queue is empty, waiting for a message
                System.out.println(instName +"Got evet: "+e);
                final byte[] payload = /*$SERIALIZER$*/.toBytes(e);
                if (payload != null)
                    send(payload);
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }
    }

    @Override
    public Component buildBehavior(String id, Component root) {
        /*$INIT PORTS$*/
        return this;
    }

    class UdpPortReader implements Runnable{

        @Override
        public void run() {
            while (active) {
                // Wait for rx data
                try {
                    byte[] buf = new byte[256];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    //this.udpSocket.setSoTimeout(1000);
                    udpSocket.receive(packet);
                    byte[] rxBufWithStart = packet.getData();
                    int rxLenWithStart = packet.getLength();

                    System.out.println(instName +"Receiving packet len "+packet.getLength()+" from addr "+packet.getAddress()+":"+packet.getPort());
                    System.out.print("[");
                    for (int i = 0; i < packet.getLength(); i++) System.out.printf("%02X ", rxBufWithStart[i]);
                    System.out.println("]");

                    if (rxLenWithStart > START_SEQUENCE.length) {
                        boolean matchStart = true;
                        for (int i = 0; i < START_SEQUENCE.length; i++) {
                            if(rxBufWithStart[i] != START_SEQUENCE[i]) matchStart = false;
                        }
                        if (matchStart == true) {
                            byte[] rxBuf = new byte[rxLenWithStart - START_SEQUENCE.length];
                            for (int i = 0; i < rxBuf.length; i++) {
                                rxBuf[i] = rxBufWithStart[i + START_SEQUENCE.length];
                            }
                            parse(rxBuf);
                        } else {
                            System.out.println(instName +"UDP: Receiver mismatch in start sequence.");
                        }
                    }
                } catch (IOException ex) {
                    System.out.println(instName +"Got exception: "+ex);
                    ex.printStackTrace();
                }
            } 
            System.out.println(instName +"UDP: Receiver thread stopped.");
        }
    }
}