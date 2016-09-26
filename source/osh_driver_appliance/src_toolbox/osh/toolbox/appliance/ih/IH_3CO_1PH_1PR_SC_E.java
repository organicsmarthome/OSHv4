package osh.toolbox.appliance.ih;

import osh.toolbox.appliance.ToolApplianceConfiguration;
import osh.toolbox.appliance.ToolApplianceConfigurationExtra;
import osh.toolbox.appliance.ToolApplianceConfigurationProfile;
import osh.toolbox.appliance.ToolApplianceConfigurationProgram;

/**
 * Induction Hob Low Energy 1 Phase Single Commodity
 * 
 * @author Ingo Mauser
 *
 */
public class IH_3CO_1PH_1PR_SC_E {

	public static ToolApplianceConfiguration[] configurations = {
			new ToolApplianceConfiguration(
					0,
					new ToolApplianceConfigurationProgram(0, "hot 1", "Hot 1",
							"wahhhhm"),
					new ToolApplianceConfigurationExtra[] {},
					new ToolApplianceConfigurationProfile[] { new ToolApplianceConfigurationProfile(
							0,
							"pure electric uninterruptible",
							new String[] { "Cooking 0", },
							new String[] { "data/profiles/renamed/ih/IH_0.csv", },
							0, 1, -1, -1) }),
			new ToolApplianceConfiguration(
					1,
					new ToolApplianceConfigurationProgram(0, "hot 1", "Hot 1",
							"wahhhhm"),
					new ToolApplianceConfigurationExtra[] {},
					new ToolApplianceConfigurationProfile[] { new ToolApplianceConfigurationProfile(
							0,
							"pure electric uninterruptible",
							new String[] { "Cooking 1", },
							new String[] { "data/profiles/renamed/ih/IH_1.csv", },
							0, 1, -1, -1) }),
			new ToolApplianceConfiguration(
					2,
					new ToolApplianceConfigurationProgram(0, "hot 1", "Hot 1",
							"wahhhhm"),
					new ToolApplianceConfigurationExtra[] {},
					new ToolApplianceConfigurationProfile[] { new ToolApplianceConfigurationProfile(
							0,
							"pure electric uninterruptible",
							new String[] { "Cooking 2", },
							new String[] { "data/profiles/renamed/ih/IH_2.csv", },
							0, 1, -1, -1) }) };

}
