package pictzr.zerogvt.org;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.imgscalr.Scalr;

public class TzrImage {
	private static Logger logger = Logger.getLogger(Logger.class.getName());


	static void displayImage(String imgfile, Shell shell, Display display,
			Canvas canvas, int screenHeight, int screenWidth, 
			ImagePlayer.FocusPolicy focusPolicy ) {
		logger.log(Level.INFO, "Asked to display img on path: " + imgfile );
		Image dispImage = null;
		if (imgfile==null) return;
		File f = new File(imgfile);
		int nh=0,nw=0;
		Point origin=new Point(0,0);
		if ( f.exists() ) {
			logger.log(Level.INFO, "Display: " + imgfile );
			Image orig = new Image (display, imgfile);
			float scale = 0;
			nh = orig.getBounds().height;
			nw = orig.getBounds().width;
			int h = orig.getBounds().height;
			int w = orig.getBounds().width;
			if (focusPolicy == ImagePlayer.FocusPolicy.SCALE) {
				if ( h > screenHeight ) {
					scale = 100 * screenHeight / h ;   
				}
				if (w > screenWidth) {
					float scale2 = 100 * screenWidth / w;
					if (scale2 < scale) scale = scale2;
				}
				if ((int) scale != 0) {
					nh = (int)(((double)scale/100.0) * h);
					nw = (int)(((double)scale/100.0) * w);
				}
				origin.x = (int)(screenWidth-nw)/2;
				origin.y = (int)(screenHeight-nh)/2;
			}
			if (focusPolicy == ImagePlayer.FocusPolicy.NO_SCALE) {
				nh=h;
				nw=w;
				if ( nh > screenHeight ) {
					origin.y = -Utils.getRandomMinMax(0, nh-screenHeight);
				}else {
					origin.y = (int)(screenHeight-nh)/2;
				}
				if ( nw > screenWidth ) {
					origin.x = -Utils.getRandomMinMax(0, nw-screenWidth);
				}else {
					origin.x = (int)(screenWidth-nw)/2;
				}

			}
			if (focusPolicy == ImagePlayer.FocusPolicy.CRAZY_SCALE) {
				nh=h;
				nw=w;
				int cscale = Utils.getRandomMinMax(0,300); 
				nh=(int)(h + h*cscale/100);
				nw=(int)(w + w*cscale/100);
				if ( nh > screenHeight ) {
					origin.y = -Utils.getRandomMinMax(0, nh-screenHeight);
				}else {
					origin.y = (int)(screenHeight-nh)/2;
				}
				if ( nw > screenWidth ) {
					origin.x = -Utils.getRandomMinMax(0, nw-screenWidth);
				} else {
					origin.x = (int)(screenWidth-nw)/2;
				}
			}
			BufferedImage awtImg = Scalr.resize(convertToAWT(orig.getImageData()), Scalr.Method.QUALITY, nw, nh);
			dispImage = new Image(display, convertToSWT(awtImg));
			//dispImage = new Image(display, orig.getImageData().scaledTo(nw, nh));
			orig.dispose();

		}
		if (dispImage != null) {
			final Image image = dispImage;
			final Point vieworigin = origin;
			final Canvas canvasref = canvas;
			final Display dis = display;
			canvas.addPaintListener ( new PaintListener () {
				public void paintControl (PaintEvent e) {
					Rectangle client = canvasref.getClientArea ();
					e.gc.setBackground(dis.getSystemColor(SWT.COLOR_BLACK));
					e.gc.fillRectangle(0, 0, client.width, client.height );
					e.gc.drawImage (image, vieworigin.x, vieworigin.y);
					logger.log(Level.INFO, "Image drawn");
					canvasref.removePaintListener(this); //TODO - stupid way must find sth more elegant
				}
			});
		}
	}


	static ImageData convertToSWT(BufferedImage bufferedImage) {
		if (bufferedImage.getColorModel() instanceof DirectColorModel) {
			DirectColorModel colorModel = (DirectColorModel)bufferedImage.getColorModel();
			PaletteData palette = new PaletteData(colorModel.getRedMask(), colorModel.getGreenMask(), colorModel.getBlueMask());
			ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette);
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					int rgb = bufferedImage.getRGB(x, y);
					int pixel = palette.getPixel(new RGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF)); 
					data.setPixel(x, y, pixel);
					if (colorModel.hasAlpha()) {
						data.setAlpha(x, y, (rgb >> 24) & 0xFF);
					}
				}
			}
			return data;		
		} else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
			IndexColorModel colorModel = (IndexColorModel)bufferedImage.getColorModel();
			int size = colorModel.getMapSize();
			byte[] reds = new byte[size];
			byte[] greens = new byte[size];
			byte[] blues = new byte[size];
			colorModel.getReds(reds);
			colorModel.getGreens(greens);
			colorModel.getBlues(blues);
			RGB[] rgbs = new RGB[size];
			for (int i = 0; i < rgbs.length; i++) {
				rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF, blues[i] & 0xFF);
			}
			PaletteData palette = new PaletteData(rgbs);
			ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette);
			data.transparentPixel = colorModel.getTransparentPixel();
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[1];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					data.setPixel(x, y, pixelArray[0]);
				}
			}
			return data;
		}
		return null;
	}


	static BufferedImage convertToAWT(ImageData data) {
		ColorModel colorModel = null;
		PaletteData palette = data.palette;
		if (palette.isDirect) {
			colorModel = new DirectColorModel(data.depth, palette.redMask, palette.greenMask, palette.blueMask);
			BufferedImage bufferedImage = new BufferedImage(colorModel, colorModel.createCompatibleWritableRaster(data.width, data.height), false, null);
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					int pixel = data.getPixel(x, y);
					RGB rgb = palette.getRGB(pixel);
					bufferedImage.setRGB(x, y,  rgb.red << 16 | rgb.green << 8 | rgb.blue);
				}
			}
			return bufferedImage;
		} else {
			RGB[] rgbs = palette.getRGBs();
			byte[] red = new byte[rgbs.length];
			byte[] green = new byte[rgbs.length];
			byte[] blue = new byte[rgbs.length];
			for (int i = 0; i < rgbs.length; i++) {
				RGB rgb = rgbs[i];
				red[i] = (byte)rgb.red;
				green[i] = (byte)rgb.green;
				blue[i] = (byte)rgb.blue;
			}
			if (data.transparentPixel != -1) {
				colorModel = new IndexColorModel(data.depth, rgbs.length, red, green, blue, data.transparentPixel);
			} else {
				colorModel = new IndexColorModel(data.depth, rgbs.length, red, green, blue);
			}		
			BufferedImage bufferedImage = new BufferedImage(colorModel, colorModel.createCompatibleWritableRaster(data.width, data.height), false, null);
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[1];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					int pixel = data.getPixel(x, y);
					pixelArray[0] = pixel;
					raster.setPixel(x, y, pixelArray);
				}
			}
			return bufferedImage;
		}
	}
}
