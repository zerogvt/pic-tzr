package pictzr.zerogvt.org;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class Controller implements Listener {
	State state = State.PLAY;
	private static Logger logger = Logger.getLogger(Logger.class.getName());
	@Override
	public void handleEvent(Event event) {
		if (event.character == SWT.SPACE && state != State.PAUSE) {
			state = State.PAUSE;
		}
		else if (event.character == SWT.SPACE && state == State.PAUSE) {
			state = State.PLAY;
		}
		if (event.character == SWT.ESC) {
			state = State.EXIT;
			System.exit(0);
		}
		if (event.character == 'n' && state != State.FWD) {
			state = State.FWD;
		}
		else if (event.character == 'n' && state == State.FWD) {
			state = State.PLAY;
		}
		logger.log(Level.INFO, "State after event: " + state.toString());
	}  
}
