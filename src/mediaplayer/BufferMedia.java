/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mediaplayer;

import java.io.DataInputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ming Or
 */
public class BufferMedia extends Thread {

    private static final int BLOCK_SIZE = 4096;
    private Decoder decoder;

    public BufferMedia(Decoder decoder) {
        this.decoder = decoder;
    }

    @Override
    public void run() {
        if (decoder.isLocal()) {
            int i = 0;
            while (i < (Math.ceil(decoder.getSubChunk2Size() / BLOCK_SIZE))) {
                try {
                    decoder.getInputStream().read(decoder.getFileBuf(), i * BLOCK_SIZE, BLOCK_SIZE);
                    decoder.getBitMap()[i] = true;
                    //System.out.println("[BufferMedia]: block " + i + "buffered");
                    i++;
                } catch (Exception e) {
                    return;
                }
            }
        } else {
            // network resource
            int i = 0, j = 0, k = 0;
            System.out.println("[BufferMedia]: " + decoder.getInputFile());
            boolean[] available = new boolean[Controller.getResource().size()];
            int[] mediaIndex = new int[Controller.getResource().size()];
            for (j = 0; j < Controller.getResource().size(); j++) {
                available[j] = Controller.getResource().get(j).playlist.isMusicExist(decoder.getInputFile());
                //System.out.println("[BufferMedia]: is Available: " + available[j]);
                if (available[j]) {
                    k++;
                    mediaIndex[j] = Controller.getResource().get(j).playlist.getIndex(decoder.getInputFile());
                    //System.out.println("[BufferMedia]: Available: " + Controller.getResource().get(j).ipAddr[0] + "." + Controller.getResource().get(j).ipAddr[1] + "." + Controller.getResource().get(j).ipAddr[2] + "." + Controller.getResource().get(j).ipAddr[3]);
                }
            }
            System.out.println("[BufferMedia]: Total num of blocks = " + decoder.getBitMap().length);
            UDPClient[] udpclient = new UDPClient[k];
            while (true) {
                for (j = 0; j < Controller.getResource().size(); j++) {
                    int cNum = 0;
                    if (available[j]) {                        
                        //try {
                            if (!decoder.getBitMap()[i]) {
                                System.out.println("[BufferMedia]: Request for block " + i);
                                udpclient[cNum] = new UDPClient(Controller.getResource().get(j).ipAddr, decoder.getFileBuf(), decoder.getBitMap(), mediaIndex[j], i);
                                udpclient[cNum].start();
                                cNum++;
                            }
                            i++;
                            /*
                            Thread.sleep(100);
                            if (!decoder.getBitMap()[i]) {
                                i--;
                            }
                             * 
                             */
                            if (i >= (Math.ceil(decoder.getSubChunk2Size() / BLOCK_SIZE))) {
                                 return;
                            }                            
                        //} catch (InterruptedException ex) {
                            //Logger.getLogger(BufferMedia.class.getName()).log(Level.SEVERE, null, ex);
                        //}
                    }
                    int a;
                    for (a = 0; a < k; a++) {
                        try {
                            udpclient[a].join();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(BufferMedia.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }
    }
}
