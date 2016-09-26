package osh.esc.grid.instances;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;

import osh.configuration.grid.DevicePerMeter;
import osh.configuration.grid.GridLayout;
import osh.configuration.grid.LayoutConnection;
import osh.esc.grid.EnergyRelation;
import osh.esc.grid.EnergySourceSink;
import osh.esc.grid.carrier.RealConnectionType;
import osh.utils.xml.XMLSerialization;

/**
 * 
 * @author Sebastian Kramer
 *
 */
public class GridInstance {
	
	protected final List<EnergySourceSink> sourceSinkList = new ArrayList<EnergySourceSink>();
	
	protected final List<EnergyRelation<? extends RealConnectionType>> relationList = new ArrayList<>();
	
	protected final List<EnergySourceSink> meters = new ArrayList<>();
	
	protected final Map<EnergySourceSink, Map<String, List<EnergySourceSink>>> specialSnowflakes = new HashMap<>();
	
	
	private static void marshal(GridInstance grid, String fileName) throws FileNotFoundException, JAXBException {
		
		GridLayout produce = new GridLayout();
		
		for (EnergySourceSink meter : grid.meters) {
			produce.getMeterUUIDs().add(meter.getDeviceUuid().toString());
		}		
		
		for (EnergyRelation<? extends RealConnectionType> er : grid.relationList) {
			
			LayoutConnection conn = new LayoutConnection();
			conn.setActiveEntityUUID(er.getActiveEntity().getDeviceUuid().toString());
			conn.setPassiveEntityUUID(er.getPassiveEntity().getDeviceUuid().toString());
			conn.setActiveToPassiveCommodity(er.getActiveToPassive().getCommodity().toString());
			conn.setPassiveToActiveCommodity(er.getPassiveToActive().getCommodity().toString());
			
			produce.getConnections().add(conn);			
		}
		
		for (Entry<EnergySourceSink, Map<String, List<EnergySourceSink>>> en : grid.specialSnowflakes.entrySet()) {
			
			EnergySourceSink meter = en.getKey();
			
			for (Entry<String, List<EnergySourceSink>> en2 : en.getValue().entrySet()) {
				
				for (EnergySourceSink sink : en2.getValue()) {
					DevicePerMeter dev = new DevicePerMeter();
					dev.setDeviceType(en2.getKey());
					dev.setDeviceUUID(sink.getDeviceUuid().toString());
					dev.setMeterUUID(meter.getDeviceUuid().toString());
					
					produce.getDeviceMeterMap().add(dev);
				}
			}
			
			
		}
		
		XMLSerialization.marshal2File(fileName, produce);
	}
	
	public static void main(String[] args) throws FileNotFoundException, JAXBException {
		
		GridInstance[] gridsToWrite = {
//				new SimulationElectricalEnergyGrid(),
//				new SimulationThermalEnergyGrid(),
				new ESHLElectricalEnergyGrid(),
				new ESHLThermalEnergyGrid(),
		};
		
		String[] fileNames = {
//				"data/SimulationElectricalGrid.xml",
//				"data/SimulationThermalGrid.xml",
				"data/ESHLElectricalGrid.xml",
				"data/ESHLThermalGrid.xml",
		};
		
		for (int i = 0; i < gridsToWrite.length; i++) {
			marshal(gridsToWrite[i], fileNames[i]);
		}
		
	}

}
