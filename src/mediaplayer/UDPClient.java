/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mediaplayer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * onlineMedia[] - title, single, album, size, md5, resource[]
 * resource[] - (IP, mediaIndex)
 */

/**
 *
 * @author mingor
 */
public class UDPClient extends Thread {

    private static final int BLOCK_SIZE = 4096;
    private static final int HEADER_SIZE = 8;
    private static byte[] ipAddr = new byte[4];
    private int mediaIndex;
    private int blockNum;
    private byte[] fileBuf;
    private boolean[] bitMap;
    
    
    public UDPClient (byte[] ipAddr, byte[] fileBuf, boolean[] bitMap, int mediaIndex, int blockNum) {
        System.arraycopy(ipAddr, 0, UDPClient.ipAddr, 0, 4);
        this.fileBuf = fileBuf;
        this.mediaIndex = mediaIndex;
        this.blockNum = blockNum;
        this.bitMap = bitMap;
    }
    
    @Override
    public void run() {
        try {

            DatagramSocket clientSocket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByAddress(ipAddr);
            byte[] sendData = new byte[BLOCK_SIZE + HEADER_SIZE];
            byte[] receiveData = new byte[BLOCK_SIZE + HEADER_SIZE];
            
            byte[] mediaHeader = ByteBuffer.allocate(4).putInt(mediaIndex).array();
            byte[] blcokHeader = ByteBuffer.allocate(4).putInt(blockNum).array();
            
            System.arraycopy(mediaHeader, 0, sendData, 0, 4);
            System.arraycopy(blcokHeader, 0, sendData, 4, 4);
            
            System.out.println("[UDP Client]: " + sendData[0] + sendData[1] + sendData[2] + sendData[3]);
            
            System.out.println("[UDP Client]: UDP Packet init");
            
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 23280);
            clientSocket.send(sendPacket);
            
            System.out.println("[UDP Client]: UDP Packet sent to " + sendPacket.getAddress().toString());
            System.out.println("[UDP Client]: Request for: media = " + mediaIndex);
            System.out.println("[UDP Client]: Request for: block = " + blockNum);
            
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);
            
            System.out.println("[UDP Client]: UDP Packet received from " + receivePacket.getAddress().toString() + ": media " + mediaIndex + ", block " + blockNum);
            
            System.arraycopy(receivePacket.getData(), 8, fileBuf, blockNum * BLOCK_SIZE, BLOCK_SIZE);
            bitMap[blockNum] = true;
            
            clientSocket.close();
            
        } catch (Exception e) {

                System.out.println("[UDP Client]: Socket Exception");

        }
    }
}
