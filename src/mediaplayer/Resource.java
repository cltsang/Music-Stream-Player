/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mediaplayer;

/**
 *
 * @author mingor
 */
public class Resource {
    public byte[] ipAddr = new byte[4];
    public Playlist playlist;
    
    public Resource(byte[] ipAddr, Playlist playlist) {
        System.arraycopy(ipAddr, 0, this.ipAddr, 0, 4);
        this.playlist = playlist;
    }
}
