/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mediaplayer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author MingOr
 */
public class Controller {

    /*
     * States of the media player
     */
    // for selection of next song
    // 0 for next song in the list
    // 1 for repeating the same song
    // 2 for random song
    // 3 for stop after the current song
    private static int mode = 0;
    // for pause / resume 
    // true if it is playing
    // false if it is paused
    private static boolean playing = true;
    // null if no song is being played
    // not null if a song is being played (no matter paused or not)
    private static PlayMedia playingMedia = null;
    // null if not playing in a play list
    // not null if playing in a play list
    private static Playlist currentPlayList = null;
    // null if not playing in a song
    // not null if playing in a song
    private static Music currentMusic = null;
    // 0 - 100
    private static int volume = 50;
    private static boolean mute = false;
    // 0 - 100
    private static int position = 0;
    private static boolean changed = false;
    private static List<Resource> resource = new ArrayList<Resource>();
    private static Playlist masterList = null;
    private static Music onlineMedia = null;
    
    /*
     * Available buttons / controllers of the media player
     */
    public static void play(Music music) throws InterruptedException {

        // kill the existing thread
        if (playingMedia != null) {            
            playingMedia.stop();
            playingMedia.getDecoder().getBufferMedia().stop();
            position = 0;
        }

        // start a new thread
        position = 0;
        currentMusic = music;
        playingMedia = new PlayMedia(music.getTitle());
        playingMedia.start();
        playing = true;
    }

    public static String nextSong() throws InterruptedException {
        switch (mode) {
            // next song in the play list
            case 0:
                if (currentPlayList != null) {
                    Music nextMusic = currentPlayList.next(currentMusic);
                    currentMusic = nextMusic;
                    if (currentMusic == null) {
                        return null;
                    } else {
                        return currentMusic.getTitle();
                    }
                } else {
                    currentMusic = null;
                    return null;
                }
            // same song
            case 1:
                return playingMedia.getName();
            // random song in the play list
            case 2:
                if (currentPlayList != null) {
                    currentMusic = currentPlayList.nextSuffle();
                    return currentMusic.getTitle();
                } else {
                    currentMusic = null;
                    return null;
                }
            // last song
            case 3:
                currentMusic = null;
                return null;
            default:
                currentMusic = null;
                return playingMedia.getName();
        }
    }

    public static void stop() {
        if (playingMedia != null) {            
            playingMedia.stop();
            playingMedia.getDecoder().getBufferMedia().stop();
            playingMedia = null;
            position = 0;
            currentMusic = null;
            currentPlayList = null;
            playing = false;
        }
    }

    public static void pauseResume() {
        if (playing) {
            playing = false;
            playingMedia.suspend();
        } else {
            playing = true;
            playingMedia.resume();
        }
    }

    public static void mute() {
        if (mute) {
            mute = false;
        } else {
            mute = true;
        }
    }

    public static boolean isPlaying() {
        return playing;
    }

    public static int getMode() {
        return mode;
    }

    public static PlayMedia getPlayingMedia() {
        return playingMedia;
    }

    public static boolean isMute() {
        return mute;
    }

    public static int getVolume() {
        return volume;
    }

    public static int getPosition() {
        return position;
    }

    public static Playlist getCurrentPlayList() {
        return currentPlayList;
    }

    public static Music getCurrentMusic() {
        return currentMusic;
    }

    public static void setCurrentPlayList(Playlist currentPlayList) {
        Controller.currentPlayList = currentPlayList;
    }

    public static void setPosition(int position) {
        Controller.position = position;
    }

    public static void setMode(int mode) {
        Controller.mode = mode;
    }

    public static boolean isChanged() {
        return changed;
    }

    public static void setChanged(boolean changed) {
        Controller.changed = changed;
    }

    public static List<Resource> getResource() {
        return resource;
    }

    public static Music getOnlineMedia() {
        return onlineMedia;
    }

    public static void setOnlineMedia(Music onlineMedia) {
        Controller.onlineMedia = onlineMedia;
    }

    public static Playlist getMasterList() {
        return masterList;
    }

    public static void setMasterList(Playlist masterList) {
        Controller.masterList = masterList;
    }

    public static void setVolume(int volume) {
        Controller.volume = volume;
    }
}
