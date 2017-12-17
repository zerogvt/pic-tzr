package pictzr.zerogvt.org;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import pictzr.zerogvt.org.ImagePlayer.FocusPolicy;

public class FSM implements Listener{
	public static final int DEFAULT_TTL = 1000;
	private static Logger logger = Logger.getLogger(Logger.class.getName());
	public enum State {
		PAUSE, PLAY, FFWD, BACK, REPEAT, EXIT
	}
	//time to live
	int ttl;
	//our current state
	State state;
	//current focus policy
	FocusPolicy focus;
	//how many times we've seen curr img
	int rep;
	//img id to show
	int imgid;
	//num of available images
	final int numImages; 

	//init FSM
	public FSM(int numImages) {
		state = State.PLAY;
		ttl = 1000;
		focus = FocusPolicy.SCALE;
		rep = 0;
		this.numImages = numImages;
	}


	public void handleEvent(Event event) {
		if (event.character == SWT.ESC) {
			state = State.EXIT;
			System.exit(0);
		}
		//pause
		if (event.character == SWT.SPACE && state != State.PAUSE) {
			state = State.PAUSE;
		}
		//unpause
		else if (event.character == SWT.SPACE && state == State.PAUSE) {
			state = State.PLAY;
		}
		else if (event.character == 'b') {
			state = State.BACK;
		}
		else if (event.character == 'n') {
			state = State.PLAY;
		}
		else if (event.character == 'f') {
			state = State.FFWD;
		}
		else if (event.character == 'r') {
			state = State.REPEAT;
		}
		logger.log(Level.INFO, "State after event: " + state.toString());
	}  
	
	public void calcNext() {
		ttl = Utils.getRandomMinMax(3000, 8000);
		if (state == State.FFWD) {
			rep = 0;
			ttl = 1000;
		} else {
			rep++;
				
		}
		if (rep==0 || rep%3==0) {
			if (state == State.REPEAT) {
			}
			else if ( state == State.BACK) {
				if (imgid-1 < 0)
					imgid = numImages-1;
				else {
					imgid-=1;
				}
			}
			else if (state == State.FFWD || state == State.PLAY) {
				if (imgid + 1 >= numImages)
					imgid = 0;
				else {
					imgid+=1;
				}
			}
		}
		if (rep%3==0) {
			focus = ImagePlayer.FocusPolicy.CRAZY_SCALE;
		}
		if (rep%3==1) {
			focus = ImagePlayer.FocusPolicy.NO_SCALE;
		}
		if (rep%3==2) {
			focus = ImagePlayer.FocusPolicy.SCALE;
		}
	}


}
