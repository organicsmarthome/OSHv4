package osh.utils.uuid;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.UUID;

/**
 * 
 * @author Kaibin Bao, Ingo Mauser
 *
 */
public class UUIDLists {
	
	public static ArrayList<UUID> parseUUIDArray( String str ) throws IllegalArgumentException {
		while ( str.startsWith("[") )
			str = str.substring(1);
		
		while ( str.endsWith("]") )
			str = str.substring(0, str.length()-1);
		
		StringTokenizer strtok = new StringTokenizer(str, ",");
		ArrayList<UUID> uuidList = new ArrayList<UUID>();
		
		while ( strtok.hasMoreElements() ) {
			UUID uuid = UUID.fromString( strtok.nextToken() );
			uuidList.add(uuid);
		}
		
		return uuidList;
	}
}
