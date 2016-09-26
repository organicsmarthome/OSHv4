package osh.utils.hex;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class HexUtilities {


	public static boolean checkTextIfHex(String text) {
				
		if (text.matches("[0-9A-Fa-f]+")) {
			return true;
		}
		else {
			return false;
		}
	}


}
