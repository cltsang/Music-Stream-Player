package mediaplayer;

import java.util.EventObject;

public class FileChangedEvent extends EventObject {

	public FileChangedEvent(Object source ) {
		super(source);
		// TODO Auto-generated constructor stub
	}

	public static interface FileChangedEventListener {
	    public void fileChanged( FileChangedEvent event );
	}
}