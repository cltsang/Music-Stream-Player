/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mediaplayer;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author MingOr
 */
public class TCPServer extends Thread {

    public static final int portNum = 13280;
    private Playlist playlist;

    public TCPServer(Playlist playlist) {
        this.playlist = playlist;
    }

    public void run() {

        ServerSocket servSocket;
        Socket fromClientSocket;

        while (true) {
            try {
                servSocket = new ServerSocket(portNum);
                System.out.println("Waiting for a connection on " + portNum);

                fromClientSocket = servSocket.accept();

                ObjectOutputStream oos = new ObjectOutputStream(fromClientSocket.getOutputStream());
                                
                oos.writeObject(playlist);

                oos.close();

                fromClientSocket.close();

            } catch (Exception e) {

                    System.out.println("[TCP Server]: Socket Exception");

            }
        }
    }
}
