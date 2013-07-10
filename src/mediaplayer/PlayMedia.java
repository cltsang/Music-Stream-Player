package mediaplayer;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author MingOr
 */
public class PlayMedia extends Thread {

    private Decoder decoder;

    public PlayMedia(String name) {
        super(name);
    }

    public Decoder getDecoder() {
        return decoder;
    }
    
    @Override
    public void run() {
        try {
            String nextSong = this.getName();
            do {
                Controller.setPosition(0);
                this.setName(nextSong);
                decoder = new Decoder(this.getName());
                System.out.println(this.getName());
                decoder.play();                
            } while ((nextSong = Controller.nextSong()) != null);
        } catch (InterruptedException ex) {
            Logger.getLogger(PlayMedia.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
