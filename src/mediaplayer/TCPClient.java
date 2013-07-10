/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mediaplayer;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author MingOr
 */
public class TCPClient extends Thread {

    public static final int portNum = 13280;
    private Playlist playlist;
    private static byte[] ipAddr = new byte[4];

    public TCPClient (byte[] ipAddr) {
        System.arraycopy(ipAddr, 0, this.ipAddr, 0, 4);
    }
    
    public void run() {
        Socket socket;

        System.out.println("[TCP Client]: client started");
        
        try {
            socket = new Socket(InetAddress.getByAddress(ipAddr), portNum);

            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            //ObjectOutputStream oos = new ObjectOutputStream(socket1.getOutputStream());

            //oos.writeObject(playlist);

            playlist = (Playlist) ois.readObject();
              
            ois.close();

            socket.close();
            
            Controller.getResource().add(new Resource(ipAddr, playlist));
            
        } catch (Exception e) {

                System.out.println("[TCP Client]: Socket Exception");

        }
    }

    public Playlist getPlaylist() {
        return playlist;
    }
 
}
