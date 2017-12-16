package pictzr.zerogvt.org;

public class Utils {
	public static int getRandomMinMax(int min, int max) {
		return (int) (min + (Math.random() * ((max-min) + 1)));
	}
}
