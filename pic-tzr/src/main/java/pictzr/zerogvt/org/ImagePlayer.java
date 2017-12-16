package pictzr.zerogvt.org;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;


public class ImagePlayer implements Listener {
	public enum FocusPolicy { SCALE, NO_SCALE, CRAZY_SCALE };
	private static Logger logger = Logger.getLogger(Logger.class.getName());
	int nextimg = 0;
	int times[];
	FocusPolicy policy[];
	String imgfile=null;
	static Object o = new Object();
	private State state = State.PLAY;;

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
		times = new int[numImgs];
		policy = new FocusPolicy[numImgs];
		createScenario(imagefiles);
		nextimg=0;
		shell.addListener(SWT.KeyDown, this);
		display.timerExec(100, new Runnable() {
			int rep=0;
			String nextimgpath;
			public void run() {
				int secstonext = 1000;
				ImagePlayer.FocusPolicy random_policy = ImagePlayer.FocusPolicy.SCALE;
				if (state == State.FFWD) {
					rep = 0;
					secstonext = 1000;
				} else {
					rep++;
					if (nextimg != 0)
						secstonext = Utils.getRandomMinMax(3000, 8000);
				}
				if (rep==0 || rep%3==0) {
					if (state == State.REPEAT) {
						
					}
					else if ( state == State.BACK)
						if (nextimg-1 < 0)
							nextimg = imagefiles.size()-1;
						else {
							nextimg--;
						}
					else if (state == State.FFWD || state == State.PLAY)
						if (nextimg + 1 >= imagefiles.size())
							nextimg = 0;
						else {
							nextimg++;
						}
					nextimgpath=imagefiles.get(nextimg);
				}
				logger.log(Level.INFO, Integer.toString(nextimg));
				if (rep%3==0) {
					random_policy = ImagePlayer.FocusPolicy.CRAZY_SCALE;
				}
				if (rep%3==1) {
					random_policy = ImagePlayer.FocusPolicy.NO_SCALE;
				}
				if (rep%3==2) {
					random_policy = ImagePlayer.FocusPolicy.SCALE;
				}
				while (state == State.PAUSE) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					display.readAndDispatch();
				}
				TzrImage.displayImage(nextimgpath, shell, display, canvas, screen_h, screen_w, random_policy);
				canvas.redraw();
				logger.log(Level.INFO,"next in " + secstonext );
				display.timerExec(secstonext, this);
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


	@Override
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
}

