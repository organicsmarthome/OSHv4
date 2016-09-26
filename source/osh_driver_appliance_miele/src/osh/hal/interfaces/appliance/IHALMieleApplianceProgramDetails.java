package osh.hal.interfaces.appliance;

import java.util.ArrayList;
import java.util.EnumMap;

import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.PowerProfileTick;

/**
 * 
 * @author Ingo Mauser
 *
 */
public interface IHALMieleApplianceProgramDetails {
	public String getProgramName();
	public String getPhaseName();
	public EnumMap<Commodity, ArrayList<PowerProfileTick>> getExpectedLoadProfiles();
}
