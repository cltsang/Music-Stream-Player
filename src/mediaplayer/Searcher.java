package mediaplayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.*;

public final class Searcher {
	private final String queryString;
	private final ArrayList<Music> musics;
	private ArrayList<Music> exact;
	private ArrayList<Music> exactIgnoreCase;
	private ArrayList<Music> concentrated;
	private ArrayList<Music> concentratedIgnoreCase;
	private ArrayList<Music> scattered;
	private ArrayList<Music> scatteredIgnoreCase;
	private ArrayList<Music> unordered;
	private ArrayList<Music> unorderedIgnoreCase;
	
	public Searcher(String queryString, ArrayList<Music> musics) {
		this.queryString = queryString;
		this.musics = musics;
		
		exact = new ArrayList<Music>();
		exactIgnoreCase = new ArrayList<Music>();
		concentrated = new ArrayList<Music>();
		concentratedIgnoreCase = new ArrayList<Music>();
		scattered = new ArrayList<Music>();
		scatteredIgnoreCase = new ArrayList<Music>();
		unordered = new ArrayList<Music>();
		unorderedIgnoreCase = new ArrayList<Music>();
	}
	
	public ArrayList<Music> searchTitle(){
		search(1);
		return returnRelvance();
	}
	
	public ArrayList<Music> searchSinger(){
		search(2);
		return returnRelvance();
	}
	
	public ArrayList<Music> searchAlbumName(){
		search(3);
		return returnRelvance();
	}
	
	public ArrayList<Music> searchFilePath(){
		search(4);
		return returnRelvance();
	}
	
	private void search(int i){
		Iterator<Music> itr = musics.iterator();
		while(itr.hasNext()){
			Music next = itr.next();
			
			String searchString = null;
			if(i == 1)
				searchString = next.getTitle();
			if(i == 2)
				searchString = next.getSinger();
			if(i == 3)
				searchString = next.getAlbumName();
			if(i == 4)
				searchString = next.getFilePath();
						
			switch(matchLevel(queryString, searchString)){
			case 1:
				exact.add(next);
				break;
			case 2:
				exactIgnoreCase.add(next);
				break;
			case 3:
				concentrated.add(next);
				break;
			case 4:
				concentratedIgnoreCase.add(next);
				break;
			case 5:
				scattered.add(next);
				break;
			case 6:
				scatteredIgnoreCase.add(next);
				break;
			}
		}
	}
	
	public static short matchLevel(String queryString, String musicInfoString){
		if(queryString.equals(musicInfoString))
			return 1;
		else if(queryString.equalsIgnoreCase(musicInfoString))
			return 2;
		else if(queryString.contains(musicInfoString))
			return 3;
		// contains ignore case
		else if(queryString.toLowerCase().contains(musicInfoString.toLowerCase()))
			return 4;
		// the string is scattered 
		else if(queryString.matches(".*" + musicInfoString.replaceAll(" ", ".*") + ".*"))
			return 5;
		else if(queryString.matches("(?i).*" + musicInfoString.replaceAll(" ", ".*") + ".*"))
			return 6;
		else {
			Pattern pattern = Pattern.compile(".*" + musicInfoString.replaceAll(" ", ".*") + ".*", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(queryString);
			if(matcher.find())
				return 7;
		}
		// no relations
		return 0;
	}

	public ArrayList<Music> returnRelvance(){
		ArrayList<Music> all = new ArrayList<Music>();
		all.addAll(exact);
		all.addAll(exactIgnoreCase);
		all.addAll(concentrated);
		all.addAll(concentratedIgnoreCase);
		all.addAll(scattered);
		all.addAll(scatteredIgnoreCase);
		all.addAll(unordered);
		all.addAll(unorderedIgnoreCase);
		return all;
	}
	
	public final String getString() {
		return queryString;
	}

	public final ArrayList<Music> getMusics() {
		return musics;
	}

	public final ArrayList<Music> getExact() {
		return exact;
	}

	public final ArrayList<Music> getExactIgnoreCase() {
		return exactIgnoreCase;
	}

	public final ArrayList<Music> getConcentrated() {
		return concentrated;
	}

	public final ArrayList<Music> getConcentratedIgnoreCase() {
		return concentratedIgnoreCase;
	}

	public final ArrayList<Music> getScattered() {
		return scattered;
	}

	public final ArrayList<Music> getScatteredIgnoreCase() {
		return scatteredIgnoreCase;
	}
	
}
