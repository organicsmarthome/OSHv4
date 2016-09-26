package osh.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

/** Converting "arrays as string" back to primitive arrays
 * 
 * @author Sebastian Kramer
 *
 */
public class StringConversions {
	
	public static Long[] fromStringToLongArray(String s) {
		s = s.replaceAll("\\s", "").replaceAll("\\[|\\]", "");
		
		return Arrays.asList(s.split(",")).stream().map(inp -> Long.decode(inp)).toArray(Long[]::new);		
	}
	
	public static Long[][] fromStringTo2DimLongArray(String s) {
		s = s.replaceAll("\\s", "");
	
		StringTokenizer st = new StringTokenizer(s, "]");
		ArrayList<Long[]> list = new ArrayList<Long[]>();
		
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			token = token.replaceAll(",\\[", "");
			token = token.replaceAll(", \\[", "");
			token = token.replaceAll("\\[", "");
			token = token.replaceAll("\\s+", "");
			
			list.add(fromStringToLongArray(token));
		}
		
		return list.stream().toArray(Long[][]::new);	
	}
	
	public static Integer[] fromStringToIntegerArray(String s) {
		s = s.replaceAll("\\s", "").replaceAll("\\[|\\]", "");
		
		return Arrays.asList(s.split(",")).stream().map(inp -> Integer.decode(inp)).toArray(Integer[]::new);		
	}
	
	public static Integer[][] fromStringTo2DimIntegerArray(String s) {
		s = s.replaceAll("\\s", "");
	
		StringTokenizer st = new StringTokenizer(s, "]");
		ArrayList<Integer[]> list = new ArrayList<Integer[]>();
		
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			token = token.replaceAll(",\\[", "");
			token = token.replaceAll(", \\[", "");
			token = token.replaceAll("\\[", "");
			token = token.replaceAll("\\s+", "");
			list.add(fromStringToIntegerArray(token));
		}
		
		return list.stream().toArray(Integer[][]::new);	
	}
}
