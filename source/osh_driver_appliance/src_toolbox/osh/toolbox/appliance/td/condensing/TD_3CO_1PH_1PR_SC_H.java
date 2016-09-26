package osh.toolbox.appliance.td.condensing;

import osh.toolbox.appliance.ToolApplianceConfiguration;
import osh.toolbox.appliance.ToolApplianceConfigurationExtra;
import osh.toolbox.appliance.ToolApplianceConfigurationProfile;
import osh.toolbox.appliance.ToolApplianceConfigurationProgram;

/**
 * Dryer Low Energy 3 Configurations 3 Phase Multi Commodity
 * 
 * @author Ingo Mauser
 *
 */
public class TD_3CO_1PH_1PR_SC_H {

	public static ToolApplianceConfiguration[] configurations = {
			new ToolApplianceConfiguration(
					0,
					new ToolApplianceConfigurationProgram(0, "Drying 0",
							"Drying 0", "wasssssccchn"),
					new ToolApplianceConfigurationExtra[] {},
					new ToolApplianceConfigurationProfile[] {
							new ToolApplianceConfigurationProfile(
									0,
									"hybrid uninterruptible",
									new String[] { "Drying 0" },
									new String[] {
											"data/profiles/renamed/td/TD_CO_0_MC.csv", },
									0, 1, 2, -1), }),
			new ToolApplianceConfiguration(
					1,
					new ToolApplianceConfigurationProgram(0, "Drying 1",
							"Drying 1", "wasssssccchn"),
					new ToolApplianceConfigurationExtra[] {},
					new ToolApplianceConfigurationProfile[] {
							new ToolApplianceConfigurationProfile(
									0,
									"hybrid uninterruptible",
									new String[] { "Drying 0", },
									new String[] {
											"data/profiles/renamed/td/TD_CO_1_MC.csv", },
									0, 1, 2, -1), }),
			new ToolApplianceConfiguration(
					2,
					new ToolApplianceConfigurationProgram(0, "Drying 2",
							"Drying 2", "wasssssccchn"),
					new ToolApplianceConfigurationExtra[] {},
					new ToolApplianceConfigurationProfile[] {
							new ToolApplianceConfigurationProfile(
									0,
									"hybrid uninterruptible",
									new String[] { "Drying 0"  },
									new String[] {
											"data/profiles/renamed/td/TD_CO_2_MC.csv", },
									0, 1, 2, -1), }) };

}
