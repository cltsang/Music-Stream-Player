package mediaplayer;

import java.io.Serializable;
import java.util.Date;

import javax.swing.ImageIcon;

public class Music implements Serializable {
	private String filePath;
	private ImageIcon imageIcon;
	private Date timeImported;
	
	private String title;
	private String singer;
	private String albumName;
	
	private float sampleRate;
	private long bitRate;
	private long durationInSecond;
	private boolean isStero;
	private long fileSize;
	
	private long subChunk2Size;     // byte
    private int numChannels;        // 1 or 2
    private int bitsPerSample;      // bit

    public void setSubChunk2Size(long subChunk2Size){
        this.subChunk2Size = subChunk2Size;
    }
    public long getSubChunk2Size(){
        return subChunk2Size;
    }
    public void setNumChannels(int numChannels){
        this.numChannels = numChannels;
    }
    public int getNumChannels(){
        return numChannels;
    }
    public int getBitsPerSample(){
        return bitsPerSample;
    }
    public void setBitsPerSample(int bitsPerSample){
        this.bitsPerSample = bitsPerSample;
    }
	
	public Music(){
		this.timeImported = new Date();
	}

	public Music(String title, String singer, String albumName){
		this.title = title;
		this.singer = singer;
		this.albumName = albumName;
	}

	public final String getFilePath() {
		return filePath;
	}

	public final void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	public final ImageIcon getImageIcon() {
		return imageIcon;
	}

	public final void setImageIcon(ImageIcon imageIcon) {
		this.imageIcon = imageIcon;
	}
	
	public float getSampleRate() {
		return sampleRate;
	}
	public void setSampleRate(float sampleRate) {
		this.sampleRate = sampleRate;
	}

	public long getDurationInSecond() {
		return durationInSecond;
	}
	public void setDurationInSecond(long durationInSecond) {
		this.durationInSecond = durationInSecond;
	}

	public long getBitRate() {
		return bitRate;
	}
	public void setBitRate(long l) {
		this.bitRate = l;
	}

	public boolean isStero() {
		return isStero;
	}
	public void setStero(boolean isStero) {
		this.isStero = isStero;
	}

	public long getFileSize() {
		return fileSize;
	}
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getSinger() {
		return singer;
	}
	public void setSinger(String singer) {
		this.singer = singer;
	}
	
	public String getAlbumName() {
		return albumName;
	}
	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}

	/**
	 * @return the timeImported
	 * Use DateFormat class to format the Date object and display the date and time of import
	 */
	public Date getTimeImported() {
		return timeImported;
	}
}
