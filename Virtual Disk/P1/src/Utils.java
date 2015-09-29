

/**
 * @author Kevin Santiago
 *
 */
public class Utils {

	/**
	 * @param number
	 * 				is an integer number
	 * @return true if the integer if a power of 2 and false if not
	 */
	public static boolean powerOf2(int number) {
		while ((number / 2.0) % 1 == 0)
			number /= 2.0;
		return number == 1;
	}
	
}
