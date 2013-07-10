package mediaplayer;

import javax.media.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.io.*;

public class MediaPlayerFrame extends JFrame {

    private static final String FRAME_TITLE = "3280 extra formats player";

    private static final String CONTROL_PANEL_TITLE = "Control Panel";

    // location and size variables for the frame.
    private static final int LOC_X = 100;
    private static final int LOC_Y = 100;
    private static final int HEIGHT = 500;
    private static final int WIDTH = 500;

    private Player player = null;

    /**
     * The tabbed pane for displaying controls.
     */
    private JTabbedPane tabPane = null;

    /**
     * Create an instance of the media frame.  No data will be displayed in the
     * frame until a player is set.
     */
    public MediaPlayerFrame() {
        super(FRAME_TITLE);
        setLocation(LOC_X, LOC_Y);
        setSize(WIDTH, HEIGHT);

        tabPane = new JTabbedPane();
        getContentPane().add(tabPane);

        /* adds a window listener so that the player may be cleaned up before 
           the frame actually closes.
        */
        addWindowListener(new WindowAdapter() {
                              public void windowClosing(WindowEvent e) {
                                  closeCurrentPlayer();
                              }
                          });
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();

        mainPanel.setLayout(gbl);

        boolean visualComponentExists = false;

        if (player.getVisualComponent() != null) {
            visualComponentExists = true;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1;
            gbc.weighty = 1;
            gbc.fill = GridBagConstraints.BOTH;
            mainPanel.add(player.getVisualComponent(), gbc);
        }

        // if the gain control component exists, add it to the new panel.
        if ((player.getGainControl() != null) &&
            (player.getGainControl().getControlComponent() != null)) {
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.weightx = 0;
            gbc.weighty = 1;
            gbc.gridheight = 2;
            gbc.fill = GridBagConstraints.VERTICAL;
            mainPanel.add(player.getGainControl().getControlComponent(), gbc);
        }

        if (player.getControlPanelComponent() != null) {
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.weightx = 1;
            gbc.gridheight = 1;

            if (visualComponentExists) {
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weighty = 0;
            }
            else {
                gbc.fill = GridBagConstraints.BOTH;
                gbc.weighty = 1;
            }

            mainPanel.add(player.getControlPanelComponent(), gbc);
        }

        return mainPanel;
    }
    public Component getPlayerComponent(){
        return player.getVisualComponent();
    }
    
    public Component getControlPanelComponent(){
        return player.getControlPanelComponent();
    }

    /**
     * Sets the media locator.  Setting this to a new value effectively
     * discards any Player which may have already existed.
     * @param locator the new MediaLocator object.
     * @throws IOException indicates an IO error in opening the media.
     * @throws NoPlayerException indicates no player was found for the 
     * media type.
     * @throws CannotRealizeException indicates an error in realizing the 
     * media file or stream.
     */
    public void setMediaLocator(MediaLocator locator) throws IOException, 
        NoPlayerException, CannotRealizeException {

        // create a new player with the new locator.  This will effectively 
        // stop and discard any current player.
        setPlayer(Manager.createRealizedPlayer(locator));
    }

    public void setPlayer(Player newPlayer) {
        // close the current player
        closeCurrentPlayer();

        player = newPlayer;

        // refresh the tabbed pane.
        tabPane.removeAll();

        if (player == null) return;

        // add the new main panel
        tabPane.add(CONTROL_PANEL_TITLE, createMainPanel());

        // add any other controls which may exist in the player.  These 
        // controls should already contain a name which is used in the 
        // tabbed pane.
        Control[] controls = player.getControls();
        for (int i = 0; i < controls.length; i++)
            if (controls[i].getControlComponent() != null)
                tabPane.add(controls[i].getControlComponent());
    }

    private void closeCurrentPlayer() {
        if (player != null) {
            player.stop();
            player.close();
        }
    }
}
