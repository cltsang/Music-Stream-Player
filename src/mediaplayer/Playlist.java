package mediaplayer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Random;

import javax.swing.ImageIcon;

public final class Playlist implements Serializable {
	private ArrayList<Music> musics;
	private String listName;
	private ImageIcon listIcon;

	public Playlist(String listName) {
		this.listName = listName;
		musics = new ArrayList<Music>();
	}

	public Playlist(String listName, ArrayList<Music> musicList) {
		this.listName = listName;
		musics = musicList;
	}

	public void debug() {
		Iterator<Music> itr = musics.iterator();
		System.out.println(musics.size());
		while (itr.hasNext()) {
			Music music = itr.next();
			System.out.println(music.getTitle() + " ; " + music.getFilePath());
		}
	}

	/**
	 * Shuffle the list
	 */
	public void shuffle() {
		Collections.shuffle(musics);
	}

	public void sortTitle() {
		for (int i = 0; i < musics.size(); i++)
			for (int j = 1; j < (musics.size() - i); j++)
				if (musics.get(j-1).getTitle().compareTo(musics.get(j).getTitle()) > 0)
					Collections.swap(musics, j, j-1);
	}

	public void sortTitleReverse() {
		for (int i = 0; i < musics.size(); i++)
			for (int j = 1; j < (musics.size() - i); j++)
				if (musics.get(j-1).getTitle().compareTo(musics.get(j).getTitle()) < 0)
					Collections.swap(musics, j, j-1);
	}

	public void sortTimeImported() {
		for (int i = 0; i < musics.size(); i++)
			for (int j = 1; j < (musics.size() - i); j++)
				if (musics.get(j-1).getTimeImported().compareTo(musics.get(j).getTimeImported()) > 0)
					Collections.swap(musics, j, j-1);
	}

	public void sortTimeImportedReverse() {
		for (int i = 0; i < musics.size(); i++)
			for (int j = 1; j < (musics.size() - i); j++)
				if (musics.get(j-1).getTimeImported().compareTo(musics.get(j).getTimeImported()) < 0)
					Collections.swap(musics, j, j-1);
	}

	public ArrayList<Music> searchTitle(String title) { // throws ConcurrentModificationException{
		Searcher searcher = new Searcher(title, musics);
		return searcher.searchTitle();
	}

        public boolean isMusicExist(String title) {
            System.out.println("[Playlist]:" + title);
            int i = 0;
            for (i = 0; i < musics.size(); i++) {
                if (musics.get(i).getTitle().equals(title)) {
                    return true;
                }
            }
            return false;
        }
        
        public int getIndex(String title) {
            int i = 0;
            for (i = 0; i < musics.size(); i++) {
                if (musics.get(i).getTitle().equals(title)) {
                    return i;
                }
            }
            return -1;
        }
        
	public ArrayList<Music> searchSinger(String singerName) { // throws ConcurrentModificationException{
		Searcher searcher = new Searcher(singerName, musics);
		return searcher.searchSinger();
	}

	public ArrayList<Music> searchAlbumName(String albumName) { // throws ConcurrentModificationException{
		Searcher searcher = new Searcher(albumName, musics);
		return searcher.searchAlbumName();
	}

	public ArrayList<Music> searchFilePath(String filePath) { // throws ConcurrentModificationException{
		Searcher searcher = new Searcher(filePath, musics);
		return searcher.searchFilePath();
	}

	// return true and add the object to the list if it does not exist before
	public boolean addMusic(Music music) {
		if (musics.contains(music))
			return false;
		else {
			return musics.add(music);
		}
	}

	// return true if the removal is successful, false if the list does not
	// contain the given object
	public boolean removeMusic(Music music) {
		return musics.remove(music);
	}

	public void append(ArrayList<Music> newList) {
		for (Music itr : newList)
			addMusic(itr);
	}

	public void append(Playlist newList) {
		for (Music itr : newList.getMusics())
			addMusic(itr);
	}

	public Music previous(Music music) {
		for (int i = 0; i < musics.size(); i++) {
			if (musics.equals(music))
				// the music is in the front of the playlist
				if (i == 0)
					return music;
				else
					return musics.get(i - 1);
		}
		// the supplied Music object is not in this playlist
		return null;
	}

	public Music next(Music music) {
		for (int i = 0; i < musics.size(); i++) {
			if (musics.get(i).equals(music))
				// the music is in the end of the playlist
				if (i == musics.size() - 1)
					return null;
				else
					return musics.get(i + 1);
		}
		// the supplied Music object is not in this playlist
		return null;
	}

	public Music nextSuffle() {
		Random rand = new Random();
		int randomNum = rand.nextInt(musics.size() - 1) + 1;

		return musics.get(randomNum);
	}
	
	public final ArrayList<Music> getMusics(){
		return musics;
	}

	public final String getListName() {
		return listName;
	}

	public final void setListName(String listName) {
		this.listName = listName;
	}

	public final ImageIcon getListIcon() {
		return listIcon;
	}

	public final void setListIcon(ImageIcon listIcon) {
		this.listIcon = listIcon;
	}
	
	public final int size(){
		return musics.size();
	}
        
        @Override
        public String toString(){
            return listName;
        }
}
