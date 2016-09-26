package osh.esc;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import osh.configuration.system.GridConfig;
import osh.eal.hal.exceptions.HALManagerException;
import osh.esc.grid.ElectricalEnergyGrid;
import osh.esc.grid.EnergyGrid;
import osh.esc.grid.EnergySimulationTypes;
import osh.esc.grid.ThermalEnergyGrid;

/**
 * 
 * @author Ingo Mauser, Sebastian Kramer
 *
 */
public abstract class EnergySimulationCore implements Serializable {

	/** Serial ID */
	private static final long serialVersionUID = 2812564530571697308L;

	protected Map<EnergySimulationTypes,EnergyGrid> grids = new Object2ObjectOpenHashMap<EnergySimulationTypes,EnergyGrid>();
	protected UUID meterUUID;
	
	
	/**
	 * CONSTRUCTOR with GridConfigs and String-UUID
	 */
	public EnergySimulationCore(
			List<GridConfig> grids, 
			String meterUUID) throws HALManagerException {
		try {
			for (GridConfig singleGrid : grids) {			
				if (singleGrid.getGridType().equals("thermal")) 
					this.grids.put(EnergySimulationTypes.THERMAL, new ThermalEnergyGrid(singleGrid.getGridLayoutSource()));
				else 
					this.grids.put(EnergySimulationTypes.ELECTRICAL, new ElectricalEnergyGrid(singleGrid.getGridLayoutSource()));
			}
		} catch (FileNotFoundException | JAXBException e) {
			throw new HALManagerException(e);
		}
		this.meterUUID = UUID.fromString(meterUUID);
	}
	
	/**
	 * CONSTRUCTOR with grids and UUID
	 */
	public EnergySimulationCore(Map<EnergySimulationTypes, EnergyGrid> grids, UUID meterUUID) {
		super();
		this.grids = grids;
		this.meterUUID = meterUUID;
	}

	/**
	 * CONSTRUCTOR for serialization, do NOT use!
	 */
	@Deprecated
	protected EnergySimulationCore() {
		
	}

	
	public Map<EnergySimulationTypes, EnergyGrid> getGrids() {
		return grids;
	}

	public UUID getMeterUUID() {
		return meterUUID;
	}
}
