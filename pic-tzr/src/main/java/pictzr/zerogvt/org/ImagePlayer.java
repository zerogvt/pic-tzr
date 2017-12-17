package pictzr.zerogvt.org;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;


public class ImagePlayer {
	public enum FocusPolicy { SCALE, NO_SCALE, CRAZY_SCALE };
	private static Logger logger = Logger.getLogger(Logger.class.getName());
	int nextimg = 0;
	int times[];
	FocusPolicy policy[];
	String imgfile=null;
	static Object o = new Object();

	String setText(){
		return Integer.toString(o.hashCode());
	}


	boolean isImage (String filepath) {
		// array of supported extensions (use a List if you prefer)
		final String[] EXTENSIONS = new String[]{
				"jpg","jpeg","gif", "png", "bmp" // and other formats you need
		};
		for (final String ext : EXTENSIONS) {
			if (filepath.endsWith("." + ext)) {
				return (true);
			}
		}
		return (false);
	}


	void createScenario(ArrayList<String> imgs) {
		//shuffle images
		for (int i=0; i<imgs.size(); i++) {
			int j = Utils.getRandomMinMax(i,imgs.size()-1);
			String tmp = imgs.get(j);
			imgs.set(j, imgs.get(i));
			imgs.set(i, tmp);
			times[i] = Utils.getRandomMinMax(5000, 20000);
			policy[i] = ImagePlayer.FocusPolicy.values()[new Random().nextInt(ImagePlayer.FocusPolicy.values().length)];
		}
	}


	public int getNextImg(int numFiles) {
		return Utils.getRandomMinMax(0, numFiles);
	}



	public void play () {
		final Display display = new Display ();
		Monitor mon = Display.getDefault().getMonitors()[0]; // returns an array of monitors attached to device and 0 fetches first one.
		Rectangle screenrec = mon.getBounds();
		final int screen_w = screenrec.width;
		final int screen_h = screenrec.height;
		final Shell shell = new Shell (display);
		shell.setLayout(new FillLayout());
		final Canvas canvas = new Canvas (shell, SWT.NO_BACKGROUND | SWT.NONE);

		DirectoryDialog dialog = new DirectoryDialog (shell, SWT.OPEN);
		dialog.setText ("Select a directory");
		String string = dialog.open ();
		//String string = "images";
		File seldir = new File(string);
		final ArrayList<String> imagefiles = new ArrayList<String>();
		for (String fname : seldir.list() ) {
			if (isImage(fname)) {
				imagefiles.add(string + File.separator + fname);
			}
		}
		final int numImgs = imagefiles.size();
		FSM fsm = new FSM(numImgs);
		times = new int[numImgs];
		policy = new FocusPolicy[numImgs];
		createScenario(imagefiles);
		shell.addListener(SWT.KeyDown, fsm);
		display.timerExec(1000, new Runnable() {
			public void run() {
				while (fsm.state == FSM.State.PAUSE) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					display.readAndDispatch();
				}
				fsm.calcNext();
				String nextimgpath = imagefiles.get(fsm.imgid);
				TzrImage.displayImage(nextimgpath, shell, display, canvas, screen_h, screen_w, fsm.focus);
				canvas.redraw();
				logger.log(Level.INFO,"Next img in " + fsm.ttl );
				display.timerExec(fsm.ttl, this);
			}
		});
		shell.setMaximized(true);
		shell.setFullScreen(true);
		shell.open ();
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) 
				display.sleep ();
		}
	}

}

