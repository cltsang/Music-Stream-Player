/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mediaplayer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author MingOr
 */
// 
public class UDPServer extends Thread {

    private static final int BLOCK_SIZE = 4096;
    // byte 0 - 3: media index
    // byte 4 - 7: block num
    private static final int HEADER_SIZE = 8;
    private Playlist playlist;
    private Object[] bufferArr;
    private Thread readBuf;

    public UDPServer(Playlist playlist) {
        this.playlist = playlist;
        bufferArr = new Object[playlist.getMusics().size()];
    }

    @Override
    public void run() {
        DatagramSocket serverSocket;

        try {
            serverSocket = new DatagramSocket(23280);
            System.out.println("[UDP Server]: UDP Server started");

            byte[] receiveData = new byte[BLOCK_SIZE + HEADER_SIZE];
            byte[] sendData = new byte[BLOCK_SIZE + HEADER_SIZE];
            while (true) {
                // receive packet
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                // analyze packet
                System.out.println("[UDP Server]: Request from IP[" + receivePacket.getAddress().toString() + ":" + receivePacket.getPort() + "]");


                System.arraycopy(receivePacket.getData(), 0, sendData, 0, HEADER_SIZE);
                byte[] headerMedia = new byte[4];
                byte[] headerBlock = new byte[4];
                System.arraycopy(receivePacket.getData(), 0, headerMedia, 0, 4);
                System.arraycopy(receivePacket.getData(), 4, headerBlock, 0, 4);


                
                
                int mediaIndex = //ByteBuffer.allocate(4).put(headerMedia).getInt();
                        TypeConverter.byteArrayToInt(headerMedia);
                int blockNum = //ByteBuffer.allocate(4).put(headerBlock).getInt();
                        TypeConverter.byteArrayToInt(headerBlock);

                System.out.println("[UDP Server]: Request for media " + mediaIndex);
                System.out.println("[UDP Server]: Request for block " + blockNum);

                if (mediaIndex < playlist.getMusics().size()) {
                    // file exists
                    if (bufferArr[(int) mediaIndex] != null) {
                        // is buffered
                        System.arraycopy((byte[]) bufferArr[(int) mediaIndex], (int) blockNum * BLOCK_SIZE, sendData, 8, BLOCK_SIZE);
                    } else {
                        // not buffered, buffer the data from file
                        Music sendMedia = playlist.getMusics().get((int) mediaIndex);
                        System.out.println("[UDP Server]: File: " + playlist.getMusics().get((int) mediaIndex).getTitle());


                        Decoder d = new Decoder(sendMedia.getTitle());
                        d.checkInputFormat();
                        bufferArr[(int) mediaIndex] = new byte[(int) d.getSubChunk2Size()];
                        //BufferMedia bufferMedia = new BufferMedia(d);
                        //bufferMedia.start();
                        //bufferMedia.join();
                        readBuffer(d, (int) mediaIndex);
                        
                        readBuf.join();
                        
                        System.out.println("[UDP Server]: joint");
                        
                        System.arraycopy((byte[]) bufferArr[(int) mediaIndex], (int) blockNum * BLOCK_SIZE, sendData, HEADER_SIZE, BLOCK_SIZE);
                        System.out.println("[UDP Server]: Buffered!");
                    }
                }
                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();

                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                serverSocket.send(sendPacket);

                System.out.println("[UDP Server]: media " + mediaIndex + ", block " + blockNum + " sent to " + sendPacket.getAddress().toString());
            }

        } catch (Exception e) {

                System.out.println("[UDP Server]: Socket Exception");


            
        }
    }

    public void readBuffer(final Decoder d, final int mediaIndex) {

        readBuf = new Thread() {

            public void run() {
                try {
                    d.getInputStream().readFully((byte[]) bufferArr[(int) mediaIndex]);
                } catch (IOException ex) {
                    Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        readBuf.start();
        try {
            readBuf.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
