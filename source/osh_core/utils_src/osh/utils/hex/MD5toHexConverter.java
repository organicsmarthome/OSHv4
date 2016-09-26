package osh.utils.hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Found in the Web...
 * @author Ingo Mauser
 *
 */
public class MD5toHexConverter {

	public static String md5Hex(String s) {
        String result = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(s.getBytes());
            result = toHex(digest);
        }
        catch (NoSuchAlgorithmException e) {
            // WILL NEVER HAPPEN
        }
        return result;
    }
	
	public static String toHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (int i = 0; i < a.length; i++) {
            sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
            sb.append(Character.forDigit(a[i] & 0x0f, 16));
        }
        return sb.toString();
    }
	
}
