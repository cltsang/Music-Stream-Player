package mediaplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.*;

public final class Importer implements Callable<Music>{
	private String filePath;
	public static Playlist MasterList;
	
	public Importer(String filePath){
		this.filePath = filePath;
	}

	@Override
	public Music call() {
		File file = new File(filePath);
		Music music = null;

		if(file.exists() && file.isFile()){
			Decoder decoder = new Decoder(filePath);
			if (decoder.checkInputFormat()) {
				music = new Music();
				music.setStero(decoder.isStero());
				music.setBitRate(decoder.getBitRate());
				music.setSampleRate(decoder.getSampleRate());
				music.setTitle(file.getName());
				music.setDurationInSecond(decoder.getDuration());
				music.setFileSize(file.length());
				music.setFilePath(filePath);
                                
                                music.setBitsPerSample(decoder.getBitsPerSample());
                                music.setNumChannels(decoder.getNumChannels());
                                music.setSubChunk2Size(decoder.getSubChunk2Size());
			}
			else
				System.out.println("Unsupported format");
		}
		else
			System.out.println("file " + filePath + " does not exist or is a directory.");
		
		return music;
	}
	
	public static Music singleImport(String path) {
		FutureTask<Music> task = new FutureTask<Music>(new Importer(path));
		Thread thread = new Thread(task);
		thread.start();
		Music returnMusicObj = null;
		try {
			returnMusicObj = task.get();
		} catch (Exception e) {
			System.out.println("Error singleImport(): music object not returned");
			e.printStackTrace();
		}
		if(returnMusicObj != null)
			if(MasterList != null)
				MasterList.addMusic(returnMusicObj);
		return returnMusicObj;
	}
	
	public static ArrayList<Music> bulkImport(ArrayList<String> paths) {
		ArrayList<Music> musicList = new ArrayList<Music>();

        ExecutorService service = Executors.newFixedThreadPool(paths.size());
        ArrayList<Future<Music>>  taskList = new ArrayList<Future<Music>>();

        for(int i=0; i<paths.size(); i++)
        	taskList.add(service.submit(new Importer(paths.get(i))));
        
        for (Future<Music> future : taskList)
			try {
				Music music = future.get();
				if(music != null){
					if(MasterList != null)
						MasterList.addMusic(music);
					musicList.add(music);
				}
			} catch (Exception e) {
				System.out.println("Error bulkImport(): music object not returned");
				e.printStackTrace();
			}
        
		return musicList;
	}
}
