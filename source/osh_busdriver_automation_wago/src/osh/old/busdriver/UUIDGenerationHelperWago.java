package osh.old.busdriver;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.UUID;

import osh.old.busdriver.wago.Wago750860ModuleType;

/**
 * 
 * @author Kaibin Bao, Ingo Mauser
 *
 */
public class UUIDGenerationHelperWago {
	
	/* WAGO 750-860 STUFF */
	
	public static final long WAGO_750_860_UUID_PREFIX = 0x75086000; // 750 860 00
	public static final int WAGO_750_860_DEFAULT_PORT = 9155;
	public static final short WAGO_750_860_GROUP_ID = (short) 0xFFFF;
	
	/**
	 * Generates the higher part of Wago-UUIDs
	 * @param moduleType
	 * @param id
	 * @param wagoPort
	 * @return
	 */
	public static long getWago750860UUIDHigherPart( Wago750860ModuleType moduleType, short id, short wagoPort ) {
		return ( ( (((WAGO_750_860_UUID_PREFIX + moduleType.value())) & 0xFFFFFFFFL) << 32 ) |
			     ( (((long)id) & 0xFFFFL)   << 16 ) |
			     ( (((long)wagoPort) & 0xFFFFL) <<  0 ));
	}
	
	public static UUID changeWago750860UUIDPort( UUID uuid, short port) {
		return new UUID((uuid.getMostSignificantBits() & ~(0xFFFFL)) | port, uuid.getLeastSignificantBits());
	}

	

	/* GENERIC STUFF */

	/**
	 * Generates the lower part of our UUIDs which contains port and ip of 
	 * e.g. the wago controller or the miele gateway
	 * 
	 * @param address
	 * @param port
	 * @return
	 * @throws Exception 
	 */
	public static long getUUIDLowerPart( InetAddress address, int port ) throws Exception {
		byte[] b_addr = address.getAddress();

		if( address instanceof Inet4Address || address instanceof Inet6Address )
			return getUUIDLowerPart(b_addr, port&0xffff);
		else
			throw new Exception("Unknown IP version");
	}
	
	private static long getUUIDLowerPart( byte[] low_array, int high_part ) {
		// keep brackets and 0xff because of negative numbers
		if( low_array.length >= 8 ) {
			return	(((long)high_part&0xffffffff) << 32) ^ // just don't ask why...
					((((long)low_array[0]&0xff) << 48) |
					 (((long)low_array[1]&0xff) << 32) |
					 (((long)low_array[2]&0xff) << 24) | // EUI-64 ... byte 4..5 are skipped
					 (((long)low_array[5]&0xff) << 16) |
					 (((long)low_array[6]&0xff) <<  8) |
					 (((long)low_array[7]&0xff) <<  0));
		} else
		if( low_array.length >= 4 ) {
			return	(((long)high_part&0xffffffff) << 32) |
					(((long)low_array[0]&0xff) << 24) |
					(((long)low_array[1]&0xff) << 16) |
					(((long)low_array[2]&0xff) <<  8) |
					(((long)low_array[3]&0xff) <<  0);
		} else {
			return	(((long)high_part&0xffffffff) << 32);
		}
	}
	
	
//	public static long get
}
