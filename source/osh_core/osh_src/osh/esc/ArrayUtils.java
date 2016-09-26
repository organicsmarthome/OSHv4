package osh.esc;

public class ArrayUtils {
	
	public static void fillArrayBoolean(boolean[] array, boolean value) {
//		int len = array.length;
//
//		if (len > 0)
//		{
//			array[0] = value;
//		}
//
//		for (int i = 1; i < len; i += i) 
//		{
//			System.arraycopy(array, 0, array, i, ((len - i) < i) ? (len - i) : i);
//		}

		for (int i = 0; i < array.length; i++) {
			array[i] = value;
		}
	}
	
	public static void fillArrayDouble(double[] array, double value) {
//		int len = array.length;
//
//		if (len > 0)
//		{
//			array[0] = value;
//		}
//
//		for (int i = 1; i < len; i += i) 
//		{
//			System.arraycopy(array, 0, array, i, ((len - i) < i) ? (len - i) : i);
//		}
		for (int i = 0; i < array.length; i++) {
			array[i] = value;
		}
	}

}
