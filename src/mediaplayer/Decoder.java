/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mediaplayer;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.*;

/**
 *
 * @author MingOr
 */
public class Decoder {

    private static final int BLOCK_SIZE = 4096;
    private int blockNum;
    private String inputFile;       // full input file name
    private DataInputStream inputStream;
    private long chunkSize;         // byte
    private long subChunk1Size;     // byte
    private float sampleRate;        // Hz
    private long byteRate;          // byte/s
    private long subChunk2Size;     // byte
    private int audioFormat;        // 1 for PCM
    private int numChannels;        // 1 or 2
    private int blockAlign;
    private int bitsPerSample;      // bit
    private SourceDataLine line;
    private double position;        // 0.0 - 100.0 for storing the current status
    private boolean local;
    private byte[] fileBuf;
    private boolean[] bitMap;
    private BufferMedia bufferMedia;

    Decoder(String filename) {
        this.inputFile = filename;
        if (Controller.getMasterList() != null) {
            int index = Controller.getMasterList().getIndex(inputFile);
            if (index == -1) {
                this.sampleRate = Controller.getOnlineMedia().getSampleRate();
                this.bitsPerSample = Controller.getOnlineMedia().getBitsPerSample();
                this.numChannels = Controller.getOnlineMedia().getNumChannels();
                this.subChunk2Size = Controller.getOnlineMedia().getSubChunk2Size();
                this.local = false;
            } else {
                try {
                    this.inputStream = new DataInputStream(new FileInputStream(Controller.getMasterList().getMusics().get(index).getFilePath()));
                } catch (Exception e) {
                    System.out.println("[Decoder]: Cannot open file");
                }
                this.local = true;
            }
        } else {
            try {
                this.inputStream = new DataInputStream(new FileInputStream(inputFile));
            } catch (Exception e) {
                System.out.println("[Decoder]: Cannot open file");
            }
            this.local = true;
        }
    }

    public void play() {
        if (!local) {
            try {
                this.streamingWAV();
            } catch (Exception e) {
                System.out.println("[Decoder]: online media streaming exception");
            }
            return;
        } else {
            try {
                if (!this.checkInputFormat()) {
                    System.out.println("[Decoder]: Unsupported Format");
                    return;
                }
                this.streamingWAV();
            } catch (LineUnavailableException e) {
                System.out.println("[Decoder]: Line unavailable");
            } catch (IOException e) {
                System.out.println("[Decoder]: IO Exception");
            }
        }
    }

    public boolean checkInputFormat() {

        byte[] tempLong = new byte[4];
        byte[] tempInt = new byte[2];
        try {

            String chunkID = "" + (char) inputStream.readByte() + (char) inputStream.readByte() + (char) inputStream.readByte() + (char) inputStream.readByte();
            if (!chunkID.equals("RIFF")) {
                System.out.println("[Decoder]: The file is not in RIFF");
                return false;
            }

            inputStream.read(tempLong);
            this.chunkSize = TypeConverter.byteArrayToLong(tempLong);
            System.out.println("ChunkSize = " + this.chunkSize);

            String format = "" + (char) inputStream.readByte() + (char) inputStream.readByte() + (char) inputStream.readByte() + (char) inputStream.readByte();
            if (!format.equals("WAVE")) {
                System.out.println("[Decoder]: It is not a WAVE file");
                return false;
            }

            String subChunkID = "" + (char) inputStream.readByte() + (char) inputStream.readByte() + (char) inputStream.readByte() + (char) inputStream.readByte();
            if (!subChunkID.equals("fmt ")) {
                System.out.println("[Decoder]: Could not locate 'fmt' chunk, problem in file format?");
                return false;
            }

            inputStream.read(tempLong);
            this.subChunk1Size = TypeConverter.byteArrayToLong(tempLong);
            System.out.println("Subchunk1Size = " + this.subChunk1Size);

            inputStream.read(tempInt);
            this.audioFormat = TypeConverter.byteArrayToShort(tempInt);
            System.out.println("AudioFormat = " + this.audioFormat);

            inputStream.read(tempInt);
            this.numChannels = TypeConverter.byteArrayToShort(tempInt);
            System.out.println("NumChannels = " + this.numChannels);

            inputStream.read(tempLong);
            this.sampleRate = (float) TypeConverter.byteArrayToLong(tempLong);
            System.out.println("SampleRate = " + this.sampleRate);

            inputStream.read(tempLong);
            this.byteRate = TypeConverter.byteArrayToLong(tempLong);
            System.out.println("ByteRate = " + this.byteRate);

            inputStream.read(tempInt);
            this.blockAlign = TypeConverter.byteArrayToShort(tempInt);
            System.out.println("BlockAlign = " + this.blockAlign);

            inputStream.read(tempInt);
            this.bitsPerSample = TypeConverter.byteArrayToShort(tempInt);
            System.out.println("BitsPerSample = " + this.bitsPerSample);

            String dataChunkID = "" + (char) inputStream.readByte() + (char) inputStream.readByte() + (char) inputStream.readByte() + (char) inputStream.readByte();
            if (!dataChunkID.equals("data")) {
                System.out.println("[Decoder]: Could not locate 'data' chunk, problem in file format?");
                return false;
            }

            inputStream.read(tempLong);
            this.subChunk2Size = TypeConverter.byteArrayToLong(tempLong);
            System.out.println("Subchunk2Size = " + this.subChunk2Size);

            System.out.printf("Duration = %02d:%02d\n", this.getDuration() / 60, this.getDuration() % 60);

            return true;
        } catch (Exception e) {
            System.out.println("[Decoder]: Cannot read file");
            return false;
        }

    }

    private void streaming() {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File(this.inputFile));
        } catch (UnsupportedAudioFileException e) {
            System.out.println("Unsupported format");
        } catch (IOException e) {
            System.out.println("Cannot open input file");
        }
    }

    private void streamMIDI() {
        try {
            Sequencer sequencer = MidiSystem.getSequencer();
            if (sequencer == null) {
                throw new MidiUnavailableException();
            }
            sequencer.open();
            Sequence mySeq = MidiSystem.getSequence(inputStream);
            sequencer.setSequence(mySeq);
            sequencer.start();
        } catch (Exception e) {
            System.out.println("[Decoder]: Midi Unavailable");
        }

    }

    private void streamingWAV() throws IOException, LineUnavailableException {
        AudioFormat format = new AudioFormat((float) this.sampleRate, this.bitsPerSample, this.numChannels, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        blockNum = (int) Math.ceil(this.subChunk2Size / BLOCK_SIZE);
        fileBuf = new byte[(int) this.subChunk2Size];
        bitMap = new boolean[blockNum];

        /*isLocal = true;
        if (this.isLocal) {
        inputStream.read(fileBuf, 0, fileBuf.length);
        }*/



        try {
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            FloatControl volume = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
            BooleanControl mute = (BooleanControl) line.getControl(BooleanControl.Type.MUTE);

            // start buffer the media data once line is got
            bufferMedia = new BufferMedia(this);
            bufferMedia.start();

            // Allocate a buffer for reading from the input stream and writing to the line.  
            // Make it large enough to hold 4k audio frames.
            // Note that the SourceDataLine also has its own internal buffer.
            int frameSize = format.getFrameSize();
            byte[] buffer = new byte[4 * 1024 * frameSize];
            int filledBuffer = 0;
            int readBytes = 0;
            int originalPos = 0;
            int i = 0;
            long filePos = 0;

            while (true) {

                /**
                 *  controls here
                 */
                mute.setValue(Controller.isMute());
                if (!Controller.isMute()) {
                    float volGain = volume.getMinimum() + (float) Controller.getVolume() / 100f * (volume.getMaximum() - volume.getMinimum());
                    volume.setValue(volGain);
                }

                if (i > blockNum) {
                    bufferMedia.stop();
                    break;
                }
                
                //System.out.println("[Decoder]: Block " + i);

                while (!bitMap[i]) {
                    
                }
                /**
                 * add control here for p2p
                 */
                //System.out.println("[Decoder]: Available " + i + " = " + bitMap[i]);
                bufferMedia.suspend();
                System.arraycopy(fileBuf, i * BLOCK_SIZE, buffer, filledBuffer, buffer.length - filledBuffer);
                bufferMedia.resume();
                // count num of bytes filled in the buffer
                filledBuffer += BLOCK_SIZE;

                // start the line
                line.start();

                // write bytes to the line in an integer multiple of the frameSize  
                // compute how many bytes to write
                int bytesToWrite = (filledBuffer / frameSize) * frameSize;

                // write the bytes. The line will buffer them and play
                // this call will block until all bytes are written
                line.write(buffer, 0, bytesToWrite);

                // if we didn't have an integer multiple of the frame size, 
                // then copy the remaining bytes to the start of the buffer.
                int remaining = filledBuffer - bytesToWrite;

                if (remaining == 0) {
                    System.arraycopy(buffer, bytesToWrite, buffer, 0, remaining);
                }
                filledBuffer = remaining;

                /*
                 * update time slider
                 */

                if (!Controller.isChanged()) {
                    int newPos = (int) ((i + 1) * BLOCK_SIZE / (double) this.subChunk2Size * 100);
                    Controller.setPosition(newPos);
                    originalPos = newPos;
                    System.out.println("[Decoder] Position = " + Controller.getPosition());
                    i++;
                } else {
                    int skipPos = ((int) ((double) Controller.getPosition() / 100.0 * this.subChunk2Size));
                    // update block position
                    i = (int) (skipPos / BLOCK_SIZE);
                    filledBuffer = 0;
                    Controller.setChanged(false);
                }

            }

            line.drain();
            line.close();

        } catch (Exception e) {
            System.out.println("[Decoder]: Line unavailable");
            return;
        }
    }

    public int getAudioFormat() {
        return audioFormat;
    }

    public int getBitsPerSample() {
        return bitsPerSample;
    }

    public int getBlockAlign() {
        return blockAlign;
    }

    public long getBitRate() {
        return 8 * byteRate;
    }

    public String getInputFile() {
        return inputFile;
    }

    public int getNumChannels() {
        return numChannels;
    }

    public float getSampleRate() {
        return sampleRate;
    }

    public SourceDataLine getLine() {
        return line;
    }

    public long getDuration() {
        return subChunk2Size / byteRate;
    }

    public boolean isStero() {
        if (numChannels == 1) {
            return false;
        } else {
            return true;
        }
    }

    public long getFileSize() {
        return chunkSize + 8;
    }

    public double getPosition() {
        return position;
    }

    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
    }

    public int getBlockNum() {
        return blockNum;
    }

    public BufferMedia getBufferMedia() {
        return bufferMedia;
    }

    public long getSubChunk2Size() {
        return subChunk2Size;
    }

    public DataInputStream getInputStream() {
        return inputStream;
    }

    public boolean[] getBitMap() {
        return bitMap;
    }

    public byte[] getFileBuf() {
        return fileBuf;
    }

    public boolean isLocal() {
        return local;
    }
}
