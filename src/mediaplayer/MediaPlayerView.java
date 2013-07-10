/*
 * MidiaPlayerView.java
 */
package mediaplayer;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.*;
import javax.media.*;
import java.io.File;
import javax.media.protocol.DataSource;

/**
 * The application's main frame.
 */
public class MediaPlayerView extends FrameView {

    private Thread updateSlider;
    private Thread updateMedia;
    private TCPServer tcpServer;
    private TCPClient tcpClient1;
    private TCPClient tcpClient2;
    private TCPClient tcpClient3;
    public int origin = 0;
    public int repeatStatus = 0;
    public int randomStatus = 0;
    public Configuration config;
    public ResourceMap resourceMap;
    private MediaTransmitter transmitter;

    
    private void playExtra(String filePath) {
        //get ip from text box, construct
        try {
            MediaPlayerFrame mpf = new MediaPlayerFrame();
            Manager.setHint(Manager.LIGHTWEIGHT_RENDERER, true);
            URL url = new File("media/" + filePath).toURL();
            mpf.setMediaLocator(new MediaLocator(url));
            mpf.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void startSubscribe(){
        try{
            MediaPlayerFrame mpf = new MediaPlayerFrame();
            String ip = "192.168.101.125";
            mpf.setMediaLocator(new MediaLocator("rtp://" + ip + ":44300/audio"));
            mpf.show();
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    private void startBroadcast(){
        try{
            URL url = new File("media/2.wav").toURL();
            String ip = "137.189.204.59";
            MediaLocator locator = new MediaLocator("rtp://" + ip + ":44300/audio");
            transmitter = new MediaTransmitter(locator);
            DataSource source = Manager.createDataSource(new MediaLocator(url));
            transmitter.setDataSource(source);
            transmitter.startTransmitting();
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }
    private void endBroadcast(){
        try {
            transmitter.stopTransmitting();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    

    public MediaPlayerView(SingleFrameApplication app) {
        super(app);
        config = Configuration.loadConfiguration();
        Controller.setMasterList(config.getMasterList());

        //playExtra();
        //startBroadcast();
        //startSubscribe();

        tcpServer = new TCPServer(config.getMasterList());
        tcpServer.start();
        byte[] host1 = new byte[4];
        byte[] host2 = new byte[4];
        byte[] host3 = new byte[4];
        host1[0] = (byte) 192;
        host1[1] = (byte) 168;
        host1[2] = (byte) 110;
        host1[3] = (byte) 125;
        host2[0] = (byte) 192;
        host2[1] = (byte) 168;
        host2[2] = (byte) 110;
        host2[3] = (byte) 122;
        host3[0] = (byte) 192;
        host3[1] = (byte) 168;
        host3[2] = (byte) 110;
        host3[3] = (byte) 120;

        tcpClient1 = new TCPClient(host1);

        tcpClient1.start();
        try {
            tcpClient1.join();
            if (Controller.getResource().size() > 0) {
                System.out.println("IP = " + Controller.getResource().get(0).ipAddr[3]);
                System.out.println("Playlist name = " + Controller.getResource().get(0).playlist.getListName());
                System.out.println("Playlist size = " + Controller.getResource().get(0).playlist.size());
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(MediaPlayerView.class.getName()).log(Level.SEVERE, null, ex);
        }

        tcpClient2 = new TCPClient(host2);

        tcpClient2.start();
        try {
            tcpClient2.join();
            if (Controller.getResource().size() > 1) {
                System.out.println("IP = " + Controller.getResource().get(1).ipAddr[3]);
                System.out.println("Playlist name = " + Controller.getResource().get(1).playlist.getListName());
                System.out.println("Playlist size = " + Controller.getResource().get(1).playlist.size());
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(MediaPlayerView.class.getName()).log(Level.SEVERE, null, ex);
        }

        UDPServer udpServer = new UDPServer(config.getMasterList());
        udpServer.start();

        tcpClient3 = new TCPClient(host3);

        tcpClient3.start();
        try {
            tcpClient3.join();
            if (Controller.getResource().size() > 1) {
                System.out.println("IP = " + Controller.getResource().get(2).ipAddr[3]);
                System.out.println("Playlist name = " + Controller.getResource().get(1).playlist.getListName());
                System.out.println("Playlist size = " + Controller.getResource().get(1).playlist.size());
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(MediaPlayerView.class.getName()).log(Level.SEVERE, null, ex);
        }

        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;

            }
        });



        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {

                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }

                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();


                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());

                }
            }
        });
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = MediaPlayerApp.getApplication().getMainFrame();
            aboutBox = new MediaPlayerAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        MediaPlayerApp.getApplication().show(aboutBox);

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        main_frame = new javax.swing.JFrame();
        mainPanel = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanelMusic = new javax.swing.JPanel();
        music_name_text = new javax.swing.JLabel();
        music_album_text = new javax.swing.JLabel();
        music_artist_text = new javax.swing.JLabel();
        music_time_text = new javax.swing.JLabel();
        music_name_Separator = new javax.swing.JSeparator();
        music_name = new javax.swing.JLabel();
        music_album = new javax.swing.JLabel();
        music_artist = new javax.swing.JLabel();
        music_time = new javax.swing.JLabel();
        music_album_Separator = new javax.swing.JSeparator();
        music_artist_Separator = new javax.swing.JSeparator();
        music_time_Separator = new javax.swing.JSeparator();
        jPanelList = new javax.swing.JPanel();
        play_list_bar = new javax.swing.JToolBar();
        add_playList_button = new javax.swing.JButton();
        play_list = new javax.swing.JLabel();
        open_play_list_cb = new javax.swing.JComboBox();
        space = new javax.swing.JLabel();
        remove_label = new javax.swing.JLabel();
        remove_play_list_cb = new javax.swing.JComboBox();
        trash_button = new javax.swing.JButton();
        list_content = new javax.swing.JScrollPane();
        playList_table = new javax.swing.JTable();
        playList_TF = new javax.swing.JTextField();
        song_TF = new javax.swing.JTextField();
        playList_label = new javax.swing.JLabel();
        song_label = new javax.swing.JLabel();
        save_button = new javax.swing.JButton();
        jPanelLIBRAR = new javax.swing.JPanel();
        library_bar = new javax.swing.JToolBar();
        search_label = new javax.swing.JLabel();
        space1 = new javax.swing.JLabel();
        option = new javax.swing.JComboBox();
        space2 = new javax.swing.JLabel();
        input_field = new javax.swing.JTextField();
        enter_button = new javax.swing.JButton();
        library_display_panel = new javax.swing.JScrollPane();
        search_result_table = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jPanelNework = new javax.swing.JPanel();
        work_in_progress = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanelInfo = new javax.swing.JPanel();
        course = new javax.swing.JLabel();
        phase1 = new javax.swing.JLabel();
        info_panal = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        statusPanel = new javax.swing.JPanel();
        jLabelSound = new javax.swing.JLabel();
        soundBar_v = new javax.swing.JSlider();
        soundBar_H = new javax.swing.JSlider();
        play_button = new javax.swing.JButton();
        stop_button = new javax.swing.JButton();
        volume_label = new javax.swing.JLabel();
        next_button = new javax.swing.JButton();
        repeat_button = new javax.swing.JButton();
        random_button = new javax.swing.JButton();
        show_time_label = new javax.swing.JLabel();
        mute_box = new javax.swing.JCheckBox();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(mediaplayer.MediaPlayerApp.class).getContext().getResourceMap(MediaPlayerView.class);
        main_frame.setBackground(resourceMap.getColor("main_frame.background")); // NOI18N
        main_frame.setForeground(resourceMap.getColor("main_frame.foreground")); // NOI18N
        main_frame.setName("main_frame"); // NOI18N

        mainPanel.setForeground(resourceMap.getColor("mainPanel.foreground")); // NOI18N
        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setPreferredSize(new java.awt.Dimension(881, 400));
        mainPanel.setLayout(null);

        jTabbedPane1.setBackground(resourceMap.getColor("jTabbedPane1.background")); // NOI18N
        jTabbedPane1.setForeground(resourceMap.getColor("jTabbedPane1.foreground")); // NOI18N
        jTabbedPane1.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        jTabbedPane1.setFont(resourceMap.getFont("jTabbedPane1.font")); // NOI18N
        jTabbedPane1.setMinimumSize(new java.awt.Dimension(881, 410));
        jTabbedPane1.setName("jTabbedPane1"); // NOI18N
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(881, 410));

        jPanelMusic.setFont(resourceMap.getFont("jPanelMusic.font")); // NOI18N
        jPanelMusic.setMinimumSize(new java.awt.Dimension(881, 410));
        jPanelMusic.setName("jPanelMusic"); // NOI18N
        jPanelMusic.setPreferredSize(new java.awt.Dimension(881, 410));

        music_name_text.setFont(resourceMap.getFont("jLabel12.font")); // NOI18N
        music_name_text.setText(resourceMap.getString("music_name_text.text")); // NOI18N
        music_name_text.setName("music_name_text"); // NOI18N

        music_album_text.setFont(resourceMap.getFont("jLabel12.font")); // NOI18N
        music_album_text.setText(resourceMap.getString("music_album_text.text")); // NOI18N
        music_album_text.setName("music_album_text"); // NOI18N

        music_artist_text.setFont(resourceMap.getFont("jLabel12.font")); // NOI18N
        music_artist_text.setText(resourceMap.getString("music_artist_text.text")); // NOI18N
        music_artist_text.setName("music_artist_text"); // NOI18N

        music_time_text.setFont(resourceMap.getFont("music_time_text.font")); // NOI18N
        music_time_text.setText(resourceMap.getString("music_time_text.text")); // NOI18N
        music_time_text.setName("music_time_text"); // NOI18N

        music_name_Separator.setName("music_name_Separator"); // NOI18N

        music_name.setFont(resourceMap.getFont("music_name.font")); // NOI18N
        music_name.setText(resourceMap.getString("music_name.text")); // NOI18N
        music_name.setName("music_name"); // NOI18N

        music_album.setFont(resourceMap.getFont("music_name.font")); // NOI18N
        music_album.setText(resourceMap.getString("music_album.text")); // NOI18N
        music_album.setName("music_album"); // NOI18N

        music_artist.setFont(resourceMap.getFont("music_name.font")); // NOI18N
        music_artist.setText(resourceMap.getString("music_artist.text")); // NOI18N
        music_artist.setName("music_artist"); // NOI18N

        music_time.setFont(resourceMap.getFont("music_name.font")); // NOI18N
        music_time.setText(resourceMap.getString("music_time.text")); // NOI18N
        music_time.setName("music_time"); // NOI18N

        music_album_Separator.setName("music_album_Separator"); // NOI18N

        music_artist_Separator.setName("music_artist_Separator"); // NOI18N

        music_time_Separator.setName("music_time_Separator"); // NOI18N

        javax.swing.GroupLayout jPanelMusicLayout = new javax.swing.GroupLayout(jPanelMusic);
        jPanelMusic.setLayout(jPanelMusicLayout);
        jPanelMusicLayout.setHorizontalGroup(
            jPanelMusicLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelMusicLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(music_name_Separator, javax.swing.GroupLayout.DEFAULT_SIZE, 722, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanelMusicLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelMusicLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(music_album_Separator, javax.swing.GroupLayout.DEFAULT_SIZE, 722, Short.MAX_VALUE)
                    .addGroup(jPanelMusicLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(jPanelMusicLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(music_album, javax.swing.GroupLayout.PREFERRED_SIZE, 704, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(music_artist_text, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(8, 8, 8)))
                .addContainerGap())
            .addGroup(jPanelMusicLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(music_name_text, javax.swing.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
                .addGap(626, 626, 626))
            .addGroup(jPanelMusicLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(music_artist_Separator, javax.swing.GroupLayout.DEFAULT_SIZE, 722, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelMusicLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(music_time_Separator, javax.swing.GroupLayout.DEFAULT_SIZE, 722, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanelMusicLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanelMusicLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(music_time_text)
                    .addComponent(music_artist, javax.swing.GroupLayout.DEFAULT_SIZE, 712, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelMusicLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(music_time, javax.swing.GroupLayout.DEFAULT_SIZE, 712, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanelMusicLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanelMusicLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(music_name, javax.swing.GroupLayout.DEFAULT_SIZE, 712, Short.MAX_VALUE)
                    .addComponent(music_album_text, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanelMusicLayout.setVerticalGroup(
            jPanelMusicLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelMusicLayout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addComponent(music_name_text, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(music_name_Separator, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(music_name, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addComponent(music_album_text, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(music_album_Separator, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(music_album, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(music_artist_text, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(music_artist_Separator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(music_artist, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(music_time_text)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(music_time_Separator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(music_time, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(117, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanelMusic.TabConstraints.tabTitle"), resourceMap.getIcon("jPanelMusic.TabConstraints.tabIcon"), jPanelMusic); // NOI18N

        jPanelList.setMinimumSize(new java.awt.Dimension(881, 410));
        jPanelList.setName("jPanelList"); // NOI18N
        jPanelList.setPreferredSize(new java.awt.Dimension(881, 410));

        play_list_bar.setRollover(true);
        play_list_bar.setName("play_list_bar"); // NOI18N

        add_playList_button.setIcon(resourceMap.getIcon("add_playList_button.icon")); // NOI18N
        add_playList_button.setText(resourceMap.getString("add_playList_button.text")); // NOI18N
        add_playList_button.setFocusable(false);
        add_playList_button.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        add_playList_button.setName("add_playList_button"); // NOI18N
        add_playList_button.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        play_list_bar.add(add_playList_button);

        play_list.setFont(resourceMap.getFont("play_list.font")); // NOI18N
        play_list.setIcon(resourceMap.getIcon("play_list.icon")); // NOI18N
        play_list.setText(resourceMap.getString("play_list.text")); // NOI18N
        play_list.setName("play_list"); // NOI18N
        play_list_bar.add(play_list);

        open_play_list_cb.setFont(resourceMap.getFont("remove_play_list_cb.font")); // NOI18N
        open_play_list_cb.setName("open_play_list_cb"); // NOI18N
        open_play_list_cb.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                open_play_list_cbMouseClicked(evt);
            }
        });
        open_play_list_cb.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                open_play_list_cbItemStateChanged(evt);
            }
        });
        play_list_bar.add(open_play_list_cb);

        space.setText(resourceMap.getString("space.text")); // NOI18N
        space.setName("space"); // NOI18N
        play_list_bar.add(space);

        remove_label.setFont(resourceMap.getFont("play_list.font")); // NOI18N
        remove_label.setIcon(resourceMap.getIcon("remove_label.icon")); // NOI18N
        remove_label.setText(resourceMap.getString("remove_label.text")); // NOI18N
        remove_label.setName("remove_label"); // NOI18N
        play_list_bar.add(remove_label);

        remove_play_list_cb.setFont(resourceMap.getFont("remove_play_list_cb.font")); // NOI18N
        remove_play_list_cb.setName("remove_play_list_cb"); // NOI18N
        remove_play_list_cb.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                remove_play_list_cbMouseClicked(evt);
            }
        });
        play_list_bar.add(remove_play_list_cb);

        trash_button.setIcon(resourceMap.getIcon("trash_button.icon")); // NOI18N
        trash_button.setText(resourceMap.getString("trash_button.text")); // NOI18N
        trash_button.setFocusable(false);
        trash_button.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        trash_button.setName("trash_button"); // NOI18N
        trash_button.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        trash_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trash_buttonActionPerformed(evt);
            }
        });
        play_list_bar.add(trash_button);

        list_content.setBackground(resourceMap.getColor("list_content.background")); // NOI18N
        list_content.setBorder(javax.swing.BorderFactory.createLineBorder(resourceMap.getColor("list_content.border.lineColor"), 3)); // NOI18N
        list_content.setFont(resourceMap.getFont("list_content.font")); // NOI18N
        list_content.setName("list_content"); // NOI18N

        playList_table.setFont(resourceMap.getFont("playList_table.font")); // NOI18N
        playList_table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "MUSIC TITLE", "ALBUM", "ARTIST", "DURATION"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        playList_table.setColumnSelectionAllowed(true);
        playList_table.setEditingColumn(0);
        playList_table.setEditingRow(0);
        playList_table.setName("playList_table"); // NOI18N
        playList_table.getTableHeader().setReorderingAllowed(false);
        list_content.setViewportView(playList_table);
        playList_table.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        playList_table.getColumnModel().getColumn(0).setPreferredWidth(100);
        playList_table.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("playList_table.columnModel.title0")); // NOI18N
        playList_table.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("playList_table.columnModel.title1")); // NOI18N
        playList_table.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("playList_table.columnModel.title2")); // NOI18N
        playList_table.getColumnModel().getColumn(3).setPreferredWidth(5);
        playList_table.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("playList_table.columnModel.title3")); // NOI18N

        playList_TF.setFont(resourceMap.getFont("jTextField2.font")); // NOI18N
        playList_TF.setText(resourceMap.getString("playList_TF.text")); // NOI18N
        playList_TF.setName("playList_TF"); // NOI18N

        song_TF.setFont(resourceMap.getFont("song_TF.font")); // NOI18N
        song_TF.setText(resourceMap.getString("song_TF.text")); // NOI18N
        song_TF.setName("song_TF"); // NOI18N

        playList_label.setFont(resourceMap.getFont("jTextField2.font")); // NOI18N
        playList_label.setText(resourceMap.getString("playList_label.text")); // NOI18N
        playList_label.setName("playList_label"); // NOI18N

        song_label.setFont(resourceMap.getFont("jTextField2.font")); // NOI18N
        song_label.setText(resourceMap.getString("song_label.text")); // NOI18N
        song_label.setName("song_label"); // NOI18N

        save_button.setFont(resourceMap.getFont("save_button.font")); // NOI18N
        save_button.setIcon(resourceMap.getIcon("save_button.icon")); // NOI18N
        save_button.setText(resourceMap.getString("save_button.text")); // NOI18N
        save_button.setName("save_button"); // NOI18N
        save_button.setRolloverIcon(resourceMap.getIcon("save_button.rolloverIcon")); // NOI18N
        save_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                save_buttonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelListLayout = new javax.swing.GroupLayout(jPanelList);
        jPanelList.setLayout(jPanelListLayout);
        jPanelListLayout.setHorizontalGroup(
            jPanelListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelListLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelListLayout.createSequentialGroup()
                        .addComponent(play_list_bar, javax.swing.GroupLayout.DEFAULT_SIZE, 722, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanelListLayout.createSequentialGroup()
                        .addGroup(jPanelListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(song_label)
                            .addComponent(playList_label))
                        .addGap(18, 18, 18)
                        .addGroup(jPanelListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(song_TF, javax.swing.GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE)
                            .addComponent(playList_TF, javax.swing.GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE))
                        .addGap(16, 16, 16)
                        .addComponent(save_button)
                        .addGap(30, 30, 30))
                    .addGroup(jPanelListLayout.createSequentialGroup()
                        .addComponent(list_content, javax.swing.GroupLayout.DEFAULT_SIZE, 722, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        jPanelListLayout.setVerticalGroup(
            jPanelListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelListLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(play_list_bar, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(list_content, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addGroup(jPanelListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelListLayout.createSequentialGroup()
                        .addGroup(jPanelListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(playList_label, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(playList_TF, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanelListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanelListLayout.createSequentialGroup()
                                .addGap(11, 11, 11)
                                .addComponent(song_label, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE))
                            .addGroup(jPanelListLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(song_TF, javax.swing.GroupLayout.DEFAULT_SIZE, 68, Short.MAX_VALUE))))
                    .addComponent(save_button))
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanelList.TabConstraints.tabTitle"), resourceMap.getIcon("jPanelList.TabConstraints.tabIcon"), jPanelList); // NOI18N

        jPanelLIBRAR.setMinimumSize(new java.awt.Dimension(881, 410));
        jPanelLIBRAR.setName("jPanelLIBRAR"); // NOI18N
        jPanelLIBRAR.setPreferredSize(new java.awt.Dimension(881, 410));

        library_bar.setRollover(true);
        library_bar.setName("library_bar"); // NOI18N

        search_label.setFont(resourceMap.getFont("title.font")); // NOI18N
        search_label.setText(resourceMap.getString("search_label.text")); // NOI18N
        search_label.setName("search_label"); // NOI18N
        library_bar.add(search_label);

        space1.setText(resourceMap.getString("space1.text")); // NOI18N
        space1.setName("space1"); // NOI18N
        library_bar.add(space1);

        option.setFont(resourceMap.getFont("option.font")); // NOI18N
        option.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "MUSIC TITLE", "ALBUM", "SINGER", "FILE PATH", "ONLINE" }));
        option.setName("option"); // NOI18N
        option.setPreferredSize(new java.awt.Dimension(100, 15));
        library_bar.add(option);

        space2.setText(resourceMap.getString("space2.text")); // NOI18N
        space2.setName("space2"); // NOI18N
        library_bar.add(space2);

        input_field.setFont(resourceMap.getFont("input_field.font")); // NOI18N
        input_field.setText(resourceMap.getString("input_field.text")); // NOI18N
        input_field.setName("input_field"); // NOI18N
        library_bar.add(input_field);

        enter_button.setIcon(resourceMap.getIcon("enter_button.icon")); // NOI18N
        enter_button.setText(resourceMap.getString("enter_button.text")); // NOI18N
        enter_button.setFocusable(false);
        enter_button.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        enter_button.setName("enter_button"); // NOI18N
        enter_button.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        enter_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enter_buttonActionPerformed(evt);
            }
        });
        library_bar.add(enter_button);

        library_display_panel.setBorder(javax.swing.BorderFactory.createLineBorder(resourceMap.getColor("library_display_panel.border.lineColor"), 3)); // NOI18N
        library_display_panel.setFont(resourceMap.getFont("library_display_panel.font")); // NOI18N
        library_display_panel.setName("library_display_panel"); // NOI18N

        search_result_table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "MUSIC TITLE", "ARTIST", "ALBUM", "DURATION"
            }
        ));
        search_result_table.setColumnSelectionAllowed(true);
        search_result_table.setName("search_result_table"); // NOI18N
        library_display_panel.setViewportView(search_result_table);
        search_result_table.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        search_result_table.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("search_result_table.columnModel.title0")); // NOI18N
        search_result_table.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("search_result_table.columnModel.title1")); // NOI18N
        search_result_table.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("search_result_table.columnModel.title2")); // NOI18N
        search_result_table.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("search_result_table.columnModel.title3")); // NOI18N

        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanelLIBRARLayout = new javax.swing.GroupLayout(jPanelLIBRAR);
        jPanelLIBRAR.setLayout(jPanelLIBRARLayout);
        jPanelLIBRARLayout.setHorizontalGroup(
            jPanelLIBRARLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLIBRARLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelLIBRARLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelLIBRARLayout.createSequentialGroup()
                        .addComponent(library_display_panel, javax.swing.GroupLayout.DEFAULT_SIZE, 722, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanelLIBRARLayout.createSequentialGroup()
                        .addComponent(library_bar, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 213, Short.MAX_VALUE)
                        .addComponent(jButton1)
                        .addGap(71, 71, 71))))
        );
        jPanelLIBRARLayout.setVerticalGroup(
            jPanelLIBRARLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLIBRARLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelLIBRARLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(library_bar, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(library_display_panel, javax.swing.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanelLIBRAR.TabConstraints.tabTitle"), resourceMap.getIcon("jPanelLIBRAR.TabConstraints.tabIcon"), jPanelLIBRAR); // NOI18N

        jPanelNework.setMinimumSize(new java.awt.Dimension(881, 410));
        jPanelNework.setName("jPanelNework"); // NOI18N
        jPanelNework.setPreferredSize(new java.awt.Dimension(881, 410));

        work_in_progress.setName("work_in_progress"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        javax.swing.GroupLayout jPanelNeworkLayout = new javax.swing.GroupLayout(jPanelNework);
        jPanelNework.setLayout(jPanelNeworkLayout);
        jPanelNeworkLayout.setHorizontalGroup(
            jPanelNeworkLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelNeworkLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(work_in_progress, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
                .addGap(151, 151, 151))
        );
        jPanelNeworkLayout.setVerticalGroup(
            jPanelNeworkLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelNeworkLayout.createSequentialGroup()
                .addGap(131, 131, 131)
                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 16, Short.MAX_VALUE)
                .addGap(338, 338, 338))
            .addGroup(jPanelNeworkLayout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addComponent(work_in_progress, javax.swing.GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE)
                .addGap(108, 108, 108))
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanelNework.TabConstraints.tabTitle"), resourceMap.getIcon("jPanelNework.TabConstraints.tabIcon"), jPanelNework); // NOI18N

        jPanelInfo.setMinimumSize(new java.awt.Dimension(881, 410));
        jPanelInfo.setName("jPanelInfo"); // NOI18N
        jPanelInfo.setPreferredSize(new java.awt.Dimension(881, 410));

        course.setFont(resourceMap.getFont("course.font")); // NOI18N
        course.setText(resourceMap.getString("course.text")); // NOI18N
        course.setName("course"); // NOI18N

        phase1.setFont(resourceMap.getFont("phase1.font")); // NOI18N
        phase1.setText(resourceMap.getString("phase1.text")); // NOI18N
        phase1.setName("phase1"); // NOI18N

        info_panal.setBackground(resourceMap.getColor("info_panal.background")); // NOI18N
        info_panal.setForeground(resourceMap.getColor("info_panal.foreground")); // NOI18N
        info_panal.setName("info_panal"); // NOI18N

        jLabel4.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jLabel5.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jLabel6.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jLabel7.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        jLabel8.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        jLabel9.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        jLabel10.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N

        jLabel1.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        javax.swing.GroupLayout info_panalLayout = new javax.swing.GroupLayout(info_panal);
        info_panal.setLayout(info_panalLayout);
        info_panalLayout.setHorizontalGroup(
            info_panalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(info_panalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(info_panalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(info_panalLayout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
                        .addGap(20, 20, 20))
                    .addGroup(info_panalLayout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
                        .addGap(55, 55, 55))
                    .addGroup(info_panalLayout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                        .addGap(49, 49, 49))
                    .addGroup(info_panalLayout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                        .addGap(57, 57, 57))
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                    .addGroup(info_panalLayout.createSequentialGroup()
                        .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
                        .addGap(51, 51, 51))
                    .addGroup(info_panalLayout.createSequentialGroup()
                        .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                        .addGap(25, 25, 25))
                    .addGroup(info_panalLayout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
                        .addGap(55, 55, 55)))
                .addGap(345, 345, 345))
        );
        info_panalLayout.setVerticalGroup(
            info_panalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(info_panalLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(26, 26, 26))
        );

        javax.swing.GroupLayout jPanelInfoLayout = new javax.swing.GroupLayout(jPanelInfo);
        jPanelInfo.setLayout(jPanelInfoLayout);
        jPanelInfoLayout.setHorizontalGroup(
            jPanelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelInfoLayout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(jPanelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(info_panal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(course, javax.swing.GroupLayout.DEFAULT_SIZE, 615, Short.MAX_VALUE)
                    .addComponent(phase1, javax.swing.GroupLayout.PREFERRED_SIZE, 615, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(233, 233, 233))
        );
        jPanelInfoLayout.setVerticalGroup(
            jPanelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(course, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(phase1, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(info_panal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(78, 78, 78))
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanelInfo.TabConstraints.tabTitle"), resourceMap.getIcon("jPanelInfo.TabConstraints.tabIcon"), jPanelInfo); // NOI18N

        mainPanel.add(jTabbedPane1);
        jTabbedPane1.setBounds(0, 5, 881, 490);

        statusPanel.setMinimumSize(new java.awt.Dimension(881, 0));
        statusPanel.setName("statusPanel"); // NOI18N
        statusPanel.setPreferredSize(new java.awt.Dimension(881, 120));

        jLabelSound.setIcon(resourceMap.getIcon("jLabelSound.icon")); // NOI18N
        jLabelSound.setText(resourceMap.getString("jLabelSound.text")); // NOI18N
        jLabelSound.setName("jLabelSound"); // NOI18N

        soundBar_v.setOrientation(javax.swing.JSlider.VERTICAL);
        soundBar_v.setAlignmentX(0.0F);
        soundBar_v.setName("soundBar_v"); // NOI18N
        soundBar_v.setPreferredSize(new java.awt.Dimension(10, 1500));
        soundBar_v.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                soundBar_vMouseClicked(evt);
            }
        });
        soundBar_v.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                soundBar_vMouseDragged(evt);
            }
        });

        soundBar_H.setValue(0);
        soundBar_H.setName("soundBar_H"); // NOI18N
        soundBar_H.setValueIsAdjusting(true);
        soundBar_H.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                soundBar_HMouseDragged(evt);
            }
        });

        play_button.setBackground(resourceMap.getColor("play_button.background")); // NOI18N
        play_button.setIcon(resourceMap.getIcon("play_button.icon")); // NOI18N
        play_button.setText(resourceMap.getString("play_button.text")); // NOI18N
        play_button.setName("play_button"); // NOI18N
        play_button.setOpaque(false);
        play_button.setPressedIcon(resourceMap.getIcon("play_button.pressedIcon")); // NOI18N
        play_button.setRolloverIcon(resourceMap.getIcon("play_button.rolloverIcon")); // NOI18N
        play_button.setRolloverSelectedIcon(resourceMap.getIcon("play_button.rolloverSelectedIcon")); // NOI18N
        play_button.setSelectedIcon(resourceMap.getIcon("play_button.selectedIcon")); // NOI18N
        play_button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                play_buttonMouseClicked(evt);
            }
        });
        play_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                play_buttonActionPerformed(evt);
            }
        });

        stop_button.setIcon(resourceMap.getIcon("stop_button.icon")); // NOI18N
        stop_button.setText(resourceMap.getString("stop_button.text")); // NOI18N
        stop_button.setName("stop_button"); // NOI18N
        stop_button.setRolloverIcon(resourceMap.getIcon("stop_button.rolloverIcon")); // NOI18N
        stop_button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                stop_buttonMouseClicked(evt);
            }
        });
        stop_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stop_buttonActionPerformed(evt);
            }
        });

        volume_label.setFont(resourceMap.getFont("volume_label.font")); // NOI18N
        volume_label.setName("volume_label"); // NOI18N

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, soundBar_v, org.jdesktop.beansbinding.ELProperty.create("${value}"), volume_label, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        next_button.setIcon(resourceMap.getIcon("next_button.icon")); // NOI18N
        next_button.setText(resourceMap.getString("next_button.text")); // NOI18N
        next_button.setName("next_button"); // NOI18N
        next_button.setRolloverIcon(resourceMap.getIcon("next_button.rolloverIcon")); // NOI18N
        next_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                next_buttonActionPerformed(evt);
            }
        });

        repeat_button.setBackground(resourceMap.getColor("repeat_button.background")); // NOI18N
        repeat_button.setIcon(resourceMap.getIcon("repeat_button.icon")); // NOI18N
        repeat_button.setText(resourceMap.getString("repeat_button.text")); // NOI18N
        repeat_button.setName("repeat_button"); // NOI18N
        repeat_button.setRolloverIcon(resourceMap.getIcon("repeat_button.rolloverIcon")); // NOI18N
        repeat_button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                repeat_buttonMouseClicked(evt);
            }
        });
        repeat_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                repeat_buttonActionPerformed(evt);
            }
        });

        random_button.setIcon(resourceMap.getIcon("random_button.icon")); // NOI18N
        random_button.setText(resourceMap.getString("random_button.text")); // NOI18N
        random_button.setName("random_button"); // NOI18N
        random_button.setRolloverIcon(resourceMap.getIcon("random_button.rolloverIcon")); // NOI18N
        random_button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                random_buttonMouseClicked(evt);
            }
        });
        random_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                random_buttonActionPerformed(evt);
            }
        });

        show_time_label.setFont(resourceMap.getFont("show_time_label.font")); // NOI18N
        show_time_label.setName("show_time_label"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, soundBar_H, org.jdesktop.beansbinding.ELProperty.create("${value}"), show_time_label, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        mute_box.setFont(resourceMap.getFont("mute_box.font")); // NOI18N
        mute_box.setText(resourceMap.getString("mute_box.text")); // NOI18N
        mute_box.setBorder(null);
        mute_box.setName("mute_box"); // NOI18N
        mute_box.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mute_boxMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addComponent(random_button, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(repeat_button, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(statusPanelLayout.createSequentialGroup()
                        .addGap(232, 232, 232)
                        .addComponent(play_button, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(stop_button, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(14, 14, 14)
                        .addComponent(next_button, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(soundBar_H, javax.swing.GroupLayout.PREFERRED_SIZE, 605, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(5, 5, 5)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(statusPanelLayout.createSequentialGroup()
                        .addComponent(show_time_label, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(35, 35, 35))
                    .addGroup(statusPanelLayout.createSequentialGroup()
                        .addComponent(jLabelSound, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(soundBar_v, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(mute_box, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(volume_label, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41))
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(statusPanelLayout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(random_button, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(statusPanelLayout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(repeat_button, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(statusPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                                .addComponent(soundBar_H, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(10, 10, 10)
                                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(play_button, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, statusPanelLayout.createSequentialGroup()
                                        .addGap(4, 4, 4)
                                        .addComponent(next_button, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(stop_button, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 76, Short.MAX_VALUE)))
                            .addGroup(statusPanelLayout.createSequentialGroup()
                                .addComponent(show_time_label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabelSound, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(45, 45, 45))
                            .addGroup(statusPanelLayout.createSequentialGroup()
                                .addComponent(soundBar_v, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(mute_box)
                                    .addComponent(volume_label))))))
                .addGap(5, 5, 5))
        );

        javax.swing.GroupLayout main_frameLayout = new javax.swing.GroupLayout(main_frame.getContentPane());
        main_frame.getContentPane().setLayout(main_frameLayout);
        main_frameLayout.setHorizontalGroup(
            main_frameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        main_frameLayout.setVerticalGroup(
            main_frameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(main_frameLayout.createSequentialGroup()
                .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 505, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(statusPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        statusPanel.getAccessibleContext().setAccessibleParent(mainPanel);

        setComponent(mainPanel);
        setStatusBar(statusPanel);

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents

    private void play_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_play_buttonActionPerformed
        // TODO add your handling code here:
        ArrayList<Music> result = null;
        ArrayList<Music> nameList = null;
        ArrayList<ArrayList<Music>> resultList = null;
        boolean found = false;
        if (Controller.getPlayingMedia() == null) {
            try {
                // At Play List tab
                if (jTabbedPane1.getSelectedIndex() == 2) {
                    if (search_result_table.getSelectedRow() != 0) {
                        for (int j = 0; j < Controller.getResource().size(); j++) {
                            result = resultList.get(j);
                            for (int i = 1; i <= result.size(); i++) {
                                found = false;
                                if (nameList != null) {
                                    for (int k = 0; k < nameList.size(); k++) {
                                        if (result.get(i - 1).getTitle().compareTo(nameList.get(k).getTitle()) == 0) {
                                            found = true;
                                            break;
                                        } else {
                                            nameList.add(result.get(i - 1));
                                        }
                                    }
                                } else {
                                    if (i == 1) {
                                        nameList.add(result.get(i - 1));
                                    }
                                }
                            }
                        }
                        for (int j = 0; j < nameList.size(); j++) {
                            if (search_result_table.getSelectedRow() == j) {
                                Music music = nameList.get(j);
                                //Controller.setCurrentPlayList(selectedList);
                                Controller.play(music);
                                updateSliderBar();
                                updateInfo();
                            }
                        }
                    }
                }
                if (jTabbedPane1.getSelectedIndex() == 1) {
                    if (open_play_list_cb.getSelectedItem() != null) {
                        Playlist selectedList = (Playlist) open_play_list_cb.getSelectedItem();
                        if (playList_table.getSelectedRow() >= 0 && playList_table.getSelectedRow() < selectedList.size()) {
                            Music music = selectedList.getMusics().get(playList_table.getSelectedRow());
                            Controller.setCurrentPlayList(selectedList);
                            Controller.play(music);
                            updateSliderBar();
                            updateInfo();
                        }
                    }
                    // At Library tab
                } else {
                    Controller.setCurrentPlayList(Controller.getResource().get(0).playlist);
                    Controller.setOnlineMedia(Controller.getResource().get(0).playlist.getMusics().get(1));
                    Controller.play(Controller.getResource().get(0).playlist.getMusics().get(1));
                    updateSliderBar();
                    updateInfo();

                }
                /*else if (jTabbedPane1.getSelectedIndex() == 2) {
                Playlist selectedList = config.getMasterList();
                if (search_result_table.getSelectedRow() >= 0 && search_result_table.getSelectedRow() < selectedList.size()) {
                Music music = searchResult.get(search_result_table.getSelectedRow());
                Controller.setCurrentPlayList(selectedList);
                Controller.play(music);
                updateSliderBar();
                music_name.setText(Controller.getCurrentMusic().getTitle());
                music_artist.setText(Controller.getCurrentMusic().getSinger());
                music_album.setText(Controller.getCurrentMusic().getAlbumName());
                music_time.setText((Controller.getCurrentMusic().getDurationInSecond() / 60) + ":" + (Controller.getCurrentMusic().getDurationInSecond() % 60));
                }
                }*/
            } catch (InterruptedException ex) {
                Logger.getLogger(MediaPlayerView.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            Controller.pauseResume();
        }
    }//GEN-LAST:event_play_buttonActionPerformed

    private void stop_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stop_buttonActionPerformed
        // TODO add your handling code here:
        if (Controller.getPlayingMedia() != null) {
            updateMedia.stop();
            music_name.setText(null);
            music_artist.setText(null);
            music_album.setText(null);
            music_time.setText(null);
            Controller.stop();
            updateSlider.stop();
            soundBar_H.setValue(0);
            show_time_label.setText("0");
        }
    }//GEN-LAST:event_stop_buttonActionPerformed

    private void soundBar_vMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_soundBar_vMouseDragged
        // TODO add your handling code here:
        Controller.setVolume(this.soundBar_v.getValue());
    }//GEN-LAST:event_soundBar_vMouseDragged

    private void next_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_next_buttonActionPerformed
        // TODO add your handling code here:
        Controller.setMode(0);
    }//GEN-LAST:event_next_buttonActionPerformed

    private void repeat_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_repeat_buttonActionPerformed
        // TODO add your handling code here:
        Controller.setMode(1);
    }//GEN-LAST:event_repeat_buttonActionPerformed

    private void save_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_save_buttonActionPerformed
        // TODO add your handling code here:
        boolean found = false;
        int i = 0;
        ArrayList<Playlist> play_list_arr = config.getPlaylists();

        if (playList_TF.getText().isEmpty() || song_TF.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Play List Name or Song cannot be empty!");
            return;
        }
        for (i = 0; i < play_list_arr.size(); i++) {
            if ((playList_TF.getText().compareToIgnoreCase(play_list_arr.get(i).getListName())) == 0) {
                found = true;
                break;
            }
        }
        if (found) {
            Music music = Importer.singleImport(song_TF.getText());
            play_list_arr.get(i).addMusic(music);

        } else {
            ArrayList<String> paths = new ArrayList<String>();
            paths.add(song_TF.getText());

            Playlist list = new Playlist(playList_TF.getText(), Importer.bulkImport(paths));
            config.addPlayList(list);
            config.saveConfiguration(config);
        }
        playList_TF.setText(null);
        song_TF.setText(null);
    }//GEN-LAST:event_save_buttonActionPerformed

    private void enter_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enter_buttonActionPerformed
        // TODO add your handling code here:
        ArrayList<Music> result = new ArrayList();
        ArrayList<String> nameList = new ArrayList();
        //ArrayList<ArrayList<Music>> resultList = null;
        boolean found = false;
        //input_field.setText(null);
        if (input_field.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "PLEASE INPUT THE SEARCHING CRITERIA!");
            return;
        }
        for (int i = 0; i < config.getMasterList().size(); i++) {
            if (option.getSelectedIndex() == 0 || option.getSelectedIndex() == -1) {
                result = config.getMasterList().searchTitle(input_field.getText());
            } else if (option.getSelectedIndex() == 1) {
                result = config.getMasterList().searchAlbumName(input_field.getText());
            } else if (option.getSelectedIndex() == 2) {
                result = config.getMasterList().searchSinger(input_field.getText());
            } else if (option.getSelectedIndex() == 3) {
                result = config.getMasterList().searchFilePath(input_field.getText());
            } else {
                JOptionPane.showMessageDialog(null, "ERROR!!PLEASE TRY AGAIN!");
            }
        }
        if (option.getSelectedIndex() == 4) {
            System.out.println("num of playlist = " + Controller.getResource().size());
            for (int j = 0; j < Controller.getResource().size(); j++) {
                int k, a;
                for (k = 0; k < Controller.getResource().get(j).playlist.getMusics().size(); k++) {
                    for (a = 0; a < result.size(); a++) {
                        if (result.get(a).getTitle().equals(Controller.getResource().get(j).playlist.getMusics().get(k).getTitle())) {
                            found = true;
                        }
                    }
                    if (!found) {
                        result.add(Controller.getResource().get(j).playlist.getMusics().get(k));
                    }
                }
            }
            String obj[][] = new String[result.size()][4];
            System.out.println("num of results = " + result.size());
            //search_result_table.setModel(new javax.swing.table.DefaultTableModel(new Object [][] {null, null, null, null}, new String[]{"Title", "Singer", "Album", "Path"});
            for (int m = 0; m < result.size(); m++) {
                obj[m][0] = result.get(m).getTitle();
                obj[m][1] = result.get(m).getSinger();
                obj[m][2] = result.get(m).getAlbumName();
                obj[m][3] = result.get(m).getFilePath();
                System.out.println(obj[m][0] + " " + obj[m][1] + " " + obj[m][2] + " " + obj[m][3]);
            }
            search_result_table.setModel(new javax.swing.table.DefaultTableModel(obj, new String[]{"Title", "Singer", "Album", "Path"}));



            /*
            } else {
            if (result != null) {
            for (int i = 1; i <= result.size(); i++) {
            if (result.isEmpty()) {
            JOptionPane.showMessageDialog(null, "NO MATCHING RESULT!");
            } else {
            search_result_table.setModel(new javax.swing.table.DefaultTableModel(new Object[][]{}, new String[]{result.get(i - 1).getTitle(), result.get(i - 1).getSinger(), result.get(i - 1).getAlbumName(), result.get(i - 1).getFilePath()}));
            }
            }
            }*/
        }
        // System.out.println(option.getActionCommand());
    }//GEN-LAST:event_enter_buttonActionPerformed

private void soundBar_vMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_soundBar_vMouseClicked
    // TODO add your handling code here:
    Controller.setVolume(this.soundBar_v.getValue());
    }//GEN-LAST:event_soundBar_vMouseClicked

    private void open_play_list_cbMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_open_play_list_cbMouseClicked
        // TODO add your handling code here:
        open_play_list_cb.removeAllItems();
        ArrayList<Playlist> playlist = config.getPlaylists();
        for (int i = 0; i < playlist.size(); i++) {
            open_play_list_cb.addItem(playlist.get(i));
        }
    }//GEN-LAST:event_open_play_list_cbMouseClicked

    private void remove_play_list_cbMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_remove_play_list_cbMouseClicked
        // TODO add your handling code here:
        remove_play_list_cb.removeAllItems();
        ArrayList<Playlist> playlist = config.getPlaylists();
        for (int i = 0; i < playlist.size(); i++) {
            remove_play_list_cb.addItem(playlist.get(i));
        }
    }//GEN-LAST:event_remove_play_list_cbMouseClicked

    private void trash_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trash_buttonActionPerformed
        // TODO add your handling code here:
        if (remove_play_list_cb.getItemAt(0).toString() == null) {
            JOptionPane.showMessageDialog(null, " NO PLAY LIST EXIST!");
            return;
        }
        //JOptionPane.showMessageDialog(null, remove_play_list_cb.getSelectedItem());
        Playlist list = (Playlist) (remove_play_list_cb.getSelectedItem());

        try {
            if (config.removePlaylist(list)) {
                JOptionPane.showMessageDialog(null, list.getListName() + " REMOVED!");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, " THE PLAY LIST " + list.getListName() + " CANNOT BE REMOVED! TRY AGAIN!");
            e.printStackTrace();

        }
    }//GEN-LAST:event_trash_buttonActionPerformed

private void mute_boxMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mute_boxMouseClicked
// TODO add your handling code here:
    Controller.mute();
    if (mute_box.isSelected()) {
        origin = soundBar_v.getValue();
        soundBar_v.setValue(0);
    } else {
        soundBar_v.setValue(origin);
    }
}//GEN-LAST:event_mute_boxMouseClicked

private void play_buttonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_play_buttonMouseClicked
// TODO add your handling code here:
    if (Controller.isPlaying()) {
        String fileName = resourceMap.getResourcesDir() + "/button_icons/blue_pause72.png";
        URL url = resourceMap.getClassLoader().getResource(fileName);
        ImageIcon icon = new ImageIcon(url);
        play_button.setIcon(icon);

        fileName = resourceMap.getResourcesDir() + "/button_icons/play.png";
        url = resourceMap.getClassLoader().getResource(fileName);
        icon = new ImageIcon(url);
        play_button.setRolloverIcon(icon);
    } else {
        play_button.setIcon(resourceMap.getIcon("play_button.icon"));
        String fileName = resourceMap.getResourcesDir() + "/button_icons/pause_72.png";
        URL url = resourceMap.getClassLoader().getResource(fileName);
        ImageIcon icon = new ImageIcon(url);
        play_button.setRolloverIcon(icon);
    }
}//GEN-LAST:event_play_buttonMouseClicked

private void repeat_buttonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_repeat_buttonMouseClicked
// TODO add your handling code here:
    if (repeatStatus == 0) {
        repeat_button.setIcon(resourceMap.getIcon("repeat_button.rolloverIcon"));
        repeat_button.setRolloverIcon(resourceMap.getIcon("repeat_button.icon"));
        repeatStatus = 1;
    } else {
        repeat_button.setIcon(resourceMap.getIcon("repeat_button.icon"));
        repeat_button.setRolloverIcon(resourceMap.getIcon("repeat_button.rolloverIcon"));
        repeatStatus = 0;
    }
}//GEN-LAST:event_repeat_buttonMouseClicked

private void random_buttonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_random_buttonMouseClicked
// TODO add your handling code here:
    if (randomStatus == 0) {
        random_button.setIcon(resourceMap.getIcon("random_button.rolloverIcon"));
        random_button.setRolloverIcon(resourceMap.getIcon("random_button.icon"));
        randomStatus = 1;
    } else {
        random_button.setIcon(resourceMap.getIcon("random_button.icon"));
        random_button.setRolloverIcon(resourceMap.getIcon("random_button.rolloverIcon"));
        randomStatus = 0;
    }
}//GEN-LAST:event_random_buttonMouseClicked

    private void random_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_random_buttonActionPerformed
        // TODO add your handling code here:
        Controller.setMode(2);
    }//GEN-LAST:event_random_buttonActionPerformed

    private void open_play_list_cbItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_open_play_list_cbItemStateChanged
        // TODO add your handling code here:
        ArrayList<Music> list;

        int j = open_play_list_cb.getSelectedIndex();
        if (j >= 0 && j < config.getPlaylists().size()) {
            list = config.getPlaylists().get(j).getMusics();

            Controller.setCurrentPlayList(config.getPlaylists().get(j));

            String obj[][] = new String[list.size()][4];
            for (int i = 0; i < list.size(); i++) {
                obj[i][0] = list.get(i).getTitle();
                obj[i][1] = list.get(i).getSinger();
                obj[i][2] = list.get(i).getAlbumName();
                obj[i][3] = list.get(i).getFilePath();
            }
            playList_table.setModel(new javax.swing.table.DefaultTableModel(obj, new String[]{"Title", "Singer", "Album", "Path"}));
        }
    }//GEN-LAST:event_open_play_list_cbItemStateChanged

    private void stop_buttonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_stop_buttonMouseClicked
        // TODO add your handling code here:
        play_button.setIcon(resourceMap.getIcon("play_button.icon"));
        String fileName = resourceMap.getResourcesDir() + "/button_icons/pause_72.png";
        URL url = resourceMap.getClassLoader().getResource(fileName);
        ImageIcon icon = new ImageIcon(url);
        play_button.setRolloverIcon(icon);
    }//GEN-LAST:event_stop_buttonMouseClicked

private void soundBar_HMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_soundBar_HMouseDragged
// TODO add your handling code here:
    Controller.setChanged(true);
    Controller.setPosition((int) ((float) soundBar_H.getValue() / (float) soundBar_H.getMaximum() * 100));
}//GEN-LAST:event_soundBar_HMouseDragged

private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
    playExtra(input_field.getText());
}//GEN-LAST:event_jButton1MouseClicked

    public void updateSliderBar() {
        updateSlider = new Thread() {

            public void run() {
                while (true) {
                    while (Controller.isChanged()) {
                        ;
                    }
                    soundBar_H.setValue(Controller.getPosition());
                    show_time_label.setText(Controller.getPosition() + "%");
                }
            }
        };
        updateSlider.start();
    }

    public void updateInfo() {
        updateMedia = new Thread() {

            public void run() {
                while (true) {
                    if (music_name.getText() == null) {
                        music_name.setText(Controller.getCurrentMusic().getTitle());
                        music_artist.setText(Controller.getCurrentMusic().getSinger());
                        music_album.setText(Controller.getCurrentMusic().getAlbumName());
                        music_time.setText((Controller.getCurrentMusic().getDurationInSecond() / 60) + ":" + (Controller.getCurrentMusic().getDurationInSecond() % 60));
                    } else if (!music_name.getText().equals(Controller.getCurrentMusic().getTitle())) {
                        music_name.setText(Controller.getCurrentMusic().getTitle());
                        music_artist.setText(Controller.getCurrentMusic().getSinger());
                        music_album.setText(Controller.getCurrentMusic().getAlbumName());
                        music_time.setText((Controller.getCurrentMusic().getDurationInSecond() / 60) + ":" + (Controller.getCurrentMusic().getDurationInSecond() % 60));
                    }
                }
            }
        };
        updateMedia.start();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton add_playList_button;
    private javax.swing.JLabel course;
    private javax.swing.JButton enter_button;
    private javax.swing.JPanel info_panal;
    private javax.swing.JTextField input_field;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelSound;
    private javax.swing.JPanel jPanelInfo;
    private javax.swing.JPanel jPanelLIBRAR;
    private javax.swing.JPanel jPanelList;
    private javax.swing.JPanel jPanelMusic;
    private javax.swing.JPanel jPanelNework;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JToolBar library_bar;
    private javax.swing.JScrollPane library_display_panel;
    private javax.swing.JScrollPane list_content;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JFrame main_frame;
    private javax.swing.JLabel music_album;
    private javax.swing.JSeparator music_album_Separator;
    private javax.swing.JLabel music_album_text;
    private javax.swing.JLabel music_artist;
    private javax.swing.JSeparator music_artist_Separator;
    private javax.swing.JLabel music_artist_text;
    private javax.swing.JLabel music_name;
    private javax.swing.JSeparator music_name_Separator;
    private javax.swing.JLabel music_name_text;
    private javax.swing.JLabel music_time;
    private javax.swing.JSeparator music_time_Separator;
    private javax.swing.JLabel music_time_text;
    private javax.swing.JCheckBox mute_box;
    private javax.swing.JButton next_button;
    private javax.swing.JComboBox open_play_list_cb;
    private javax.swing.JComboBox option;
    private javax.swing.JLabel phase1;
    private javax.swing.JTextField playList_TF;
    private javax.swing.JLabel playList_label;
    private javax.swing.JTable playList_table;
    private javax.swing.JButton play_button;
    private javax.swing.JLabel play_list;
    private javax.swing.JToolBar play_list_bar;
    private javax.swing.JButton random_button;
    private javax.swing.JLabel remove_label;
    private javax.swing.JComboBox remove_play_list_cb;
    private javax.swing.JButton repeat_button;
    private javax.swing.JButton save_button;
    private javax.swing.JLabel search_label;
    private javax.swing.JTable search_result_table;
    private javax.swing.JLabel show_time_label;
    private javax.swing.JTextField song_TF;
    private javax.swing.JLabel song_label;
    private javax.swing.JSlider soundBar_H;
    private javax.swing.JSlider soundBar_v;
    private javax.swing.JLabel space;
    private javax.swing.JLabel space1;
    private javax.swing.JLabel space2;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JButton stop_button;
    private javax.swing.JButton trash_button;
    private javax.swing.JLabel volume_label;
    private javax.swing.JLabel work_in_progress;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
}
