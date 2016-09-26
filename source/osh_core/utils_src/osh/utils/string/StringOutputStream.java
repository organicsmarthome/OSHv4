package osh.utils.string;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class StringOutputStream extends OutputStream {
	
	StringBuilder mBuf = new StringBuilder();
	
	public void write(int bytes) throws IOException {
		mBuf.append((char) bytes);
	}
	
	public String getString() {
		return mBuf.toString();
	}
}
