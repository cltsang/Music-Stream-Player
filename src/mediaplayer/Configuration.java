package mediaplayer;

import java.io.*;
//import java.nio.file.Path;
//import java.nio.file.Paths;
import java.util.*;

public final class Configuration implements Serializable {
	private int volume;
	private String mediaFolderPath;
	private boolean autoImport;
	private ArrayList<Playlist> playlists;
	private Playlist masterList;

	private transient Thread watchThread;
	
	private Configuration(){
		this.volume = 0;
		setMediaFolderPath("media");
		autoImport = false;
		playlists = new ArrayList<Playlist>();
		initializeMasterList();
		Importer.MasterList = masterList;
		playlists.add(masterList);
		this.watchThread = null;
	}
	
	/*
	 *  copy constructor
	 */
	public Configuration(Configuration config){
		this.volume = config.volume;
		this.mediaFolderPath = config.mediaFolderPath;
		this.autoImport = config.autoImport;
		this.masterList = config.masterList;
		this.playlists = config.playlists;
		this.watchThread = null;
		Importer.MasterList = masterList;
	}
	
	/*
	 *  add all songs in the media folder to the masterList
	 */
	private void initializeMasterList(){
		ArrayList<String> paths = FileManager.listDirectory(mediaFolderPath);
		//for(String path : paths)
			//System.out.println("!!! --- " + path);
		masterList = new Playlist("All Song", Importer.bulkImport(paths));
	}

	/*
	 *  return true and add the object to the list if it does not exist before
	 */
	public boolean addPlayList(Playlist playlist){
		if(playlists.contains(playlist))
			return false;
		else
			return playlists.add(playlist);
	}

	/*
	 *  return true if the removal is successful, false if the list does not contain the given object 
	 */
	public boolean removePlaylist(Playlist playlist){
		if(playlist.equals(masterList)){
			System.out.println("\"All Song\" list cannot be removed");
			return false;
		}
		else
			return playlists.remove(playlist);
	}
	
	private void resetImport(){
		setAutoImport(true);
	}

	public void setAutoImport(boolean autoImport) {
		this.autoImport = autoImport;
		/*if(autoImport){
	        // register directory and process its events
	        Path dir = Paths.get(mediaFolderPath);
	        DirectoryMonitor directoryMonitor = null;
			try {
				directoryMonitor = new DirectoryMonitor(dir, true);
			} catch (IOException e) {
				System.out.println("Error setAutoImport(): cannot instantiate DirectoryMonitor object");
				e.printStackTrace();
			}
			if(watchThread != null && watchThread.isAlive())
				watchThread.stop();
			watchThread = new Thread(directoryMonitor);
			watchThread.start();
		}
		else{
			watchThread.stop();
			watchThread = null;
		}*/
	}

	public boolean isAutoImport() {
		return autoImport;
	}
	
	public static Configuration loadConfiguration() {
		// by default the configuration file is saved as named "config" the working directory
		return loadConfiguration("config");
	}

	public static Configuration loadConfiguration(String filePath){
		ObjectInputStream in = null;
		Configuration config = null;
		if(new File(filePath).exists()){
			try {
				in = new ObjectInputStream(new FileInputStream(filePath));
			} catch (IOException e) {
				System.out.println("Error loadConfiguration(): cannot open the file for reading");
				e.printStackTrace();
				config = new Configuration();
			}
			try {
				config = (Configuration) in.readObject();
			} catch (IOException e) {
				System.out.println("Error loadConfiguration(): cannot cast to Configuration object");
				e.printStackTrace();
				config = new Configuration();
			} catch (ClassNotFoundException e) {
				System.out.println("Error loadConfiguration(): cannot cast to Configuration object");
				e.printStackTrace();
				config = new Configuration();
			}
		}
		else
			config = new Configuration();
		return config;
	}
	
	public static void saveConfiguration(Configuration config) {
		saveConfiguration(config, "config");
	}
	
	public static void saveConfiguration(Configuration config, String filePath) {
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(filePath, false));
		} catch (IOException e) {
			System.out.println("Error saveConfiguration(): cannot open the file for writing");
			e.printStackTrace();
		}
		try {
			out.writeObject(config);
		} catch (IOException e) {
			System.out.println("Error saveConfiguration(): cannot save to the file");
			e.printStackTrace();
		}
	}

	public final String getMediaFolderPath() {
		return mediaFolderPath;
	}
	
	public boolean setMediaFolderPath(String path) {
		File file = new File(path);
		if(file.exists() && file.isFile())
			return false;
		else{
			this.mediaFolderPath = path;

			if(autoImport)
				resetImport();
		}
		return true;
	}
	
	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}

	public ArrayList<Playlist> getPlaylists() {
		return playlists;
	}
	
	public final Playlist getMasterList() {
		return masterList;
	}

	public Thread getWatchThread() {
		return watchThread;
	}
}
