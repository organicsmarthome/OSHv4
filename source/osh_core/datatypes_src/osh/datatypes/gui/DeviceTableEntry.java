package osh.datatypes.gui;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * @author ???
 *
 */
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class DeviceTableEntry implements Comparable<DeviceTableEntry> {
	
	private int entry;
	private UUID id;
	private String name;
	private int bits;
	private String representation;
	private String reschedule;
	
	
	@Deprecated
	public DeviceTableEntry() {
		this(0, null, "", 0, "", "");
	}
	
	public DeviceTableEntry(int entry, UUID id, String name, int bits, String reschedule, String representation) {
		super();
		
		this.entry = entry;
		this.id = id;
		this.name = name;
		this.bits = bits;
		this.reschedule = reschedule;
		this.representation = representation;
	}

	
	public int getEntry() {
		return entry;
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public int getBits() {
		return bits;
	}

	public String getRepresentation() {
		return representation;
	}
	
	public String getReschedule() {
		return reschedule;
	}
	
	@Override
	public int compareTo(DeviceTableEntry o) {
		return entry - o.entry;
	}
	
	@Override
	public DeviceTableEntry clone() {
		return new DeviceTableEntry(
				this.entry, this.id, this.name, this.bits, this.reschedule, this.representation);
	}

	
}