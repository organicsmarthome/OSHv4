package constructsimulation.datatypes;

import osh.configuration.cal.CALConfiguration;
import osh.configuration.eal.EALConfiguration;
import osh.configuration.oc.OCConfiguration;
import osh.configuration.system.OSHConfiguration;
import osh.simulation.screenplay.Screenplay;

public class SimulationConfigurationWrapper {
	
	public SimulationConfigurationWrapper(
			OCConfiguration occonfig,
			EALConfiguration ealconfig,
			CALConfiguration calConfig,
			OSHConfiguration oshConfig,
			Screenplay myScreenplay) {
		this.occonfig = occonfig;
		this.ealconfig = ealconfig;
		this.calConfig = calConfig;
		this.oshConfig = oshConfig;
		this.myScreenplay = myScreenplay;
	}
	
	private OCConfiguration occonfig;
	private EALConfiguration ealconfig;
	private CALConfiguration calConfig;
	private OSHConfiguration oshConfig;
	private Screenplay myScreenplay;
	
	public OCConfiguration getOcconfig() {
		return occonfig;
	}
	public EALConfiguration getEalconfig() {
		return ealconfig;
	}
	public CALConfiguration getCalConfig() {
		return calConfig;
	}
	public OSHConfiguration getOshConfig() {
		return oshConfig;
	}
	public Screenplay getMyScreenplay() {
		return myScreenplay;
	}
	


}
