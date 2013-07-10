package mediaplayer;

import javax.media.*;
import javax.media.control.*;
import javax.media.protocol.*;
import javax.media.format.*;

import java.io.IOException;
import java.io.File;

/**
 * Creates a new media transmitter.  The media transmitter may be used to 
 * transmit a data source over a network.
 */
public class MediaTransmitter {

    /**
     * Output locator - this is the broadcast address for the media.
     */
    private MediaLocator mediaLocator = null;

    /**
     * The data sink object used to broadcast the results from the processor
     * to the network.
     */
    private DataSink dataSink = null;

    /**
     * The processor used to read the media from a local file, and produce an
     * output stream which will be handed to the data sink object for 
     * broadcast.
     */
    private Processor mediaProcessor = null;

    /**
     * The track formats used for all data sources in this transmitter.  It is 
     * assumed that this transmitter will always be associated with the same 
     * RTP stream format, so this is made static.
     */
    private static final Format[] FORMATS = new Format[] {
        new AudioFormat(AudioFormat.MPEG_RTP)};

    /**
     * The content descriptor for this transmitter.  It is assumed that this
     * transmitter always handles the same type of RTP content, so this is
     * made static.
     */
    private static final ContentDescriptor CONTENT_DESCRIPTOR = 
        new ContentDescriptor(ContentDescriptor.RAW_RTP);

    /**
     * Creates a new transmitter with the given outbound locator.
     */
    public MediaTransmitter(MediaLocator locator) {
        mediaLocator = locator;
    }

    /**
     * Starts transmitting the media.
     */
    public void startTransmitting() throws IOException {
        // start the processor
        mediaProcessor.start();

        // open and start the data sink
        dataSink.open();
        dataSink.start();
    }

    /**
     * Stops transmitting the media.
     */
    public void stopTransmitting() throws IOException {
        // stop and close the data sink
        dataSink.stop();
        dataSink.close();

        // stop and close the processor
        mediaProcessor.stop();
        mediaProcessor.close();
    }

    /**
     * Sets the data source.  This is where the transmitter will get the media
     * to transmit.
     */
    public void setDataSource(DataSource ds) throws IOException, 
        NoProcessorException, CannotRealizeException, NoDataSinkException {

        /* Create the realized processor.  By calling the 
           createRealizedProcessor() method on the manager, we are guaranteed 
           that the processor is both configured and realized already.  
           For this reason, this method will block until both of these 
           conditions are true.  In general, the processor is responsible 
           for reading the file from a file and converting it to
           an RTP stream.
        */
        mediaProcessor = Manager.createRealizedProcessor(new ProcessorModel(ds, FORMATS, CONTENT_DESCRIPTOR));

        /* Create the data sink.  The data sink is used to do the actual work 
           of broadcasting the RTP data over a network.
        */
        dataSink = Manager.createDataSink(mediaProcessor.getDataOutput(), 
                                          mediaLocator);
    }

    /**
     * Prints a usage message to System.out for how to use this class
     * through the command line.
     */
    public static void printUsage() {
        System.out.println("Usage: java MediaTransmitter mediaLocator " +
                           "mediaFile");
        System.out.println("  example: java MediaTransmitter " +
            "rtp://192.168.1.72:49150/audio mysong.mp3");
        System.out.println("  example: java MediaTransmitter " +
            "rtp://192.168.1.255:49150/audio mysong.mp3");
    }
}