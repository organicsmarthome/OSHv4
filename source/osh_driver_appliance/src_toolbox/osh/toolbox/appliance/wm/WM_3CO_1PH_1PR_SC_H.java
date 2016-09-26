package osh.toolbox.appliance.wm;

import osh.toolbox.appliance.ToolApplianceConfiguration;
import osh.toolbox.appliance.ToolApplianceConfigurationExtra;
import osh.toolbox.appliance.ToolApplianceConfigurationProfile;
import osh.toolbox.appliance.ToolApplianceConfigurationProgram;

/**
 * Washer Low Energy 4 Configurations 1 Phase Multi Commodity
 * 
 * @author Ingo Mauser
 *
 */
public class WM_3CO_1PH_1PR_SC_H {

	public static ToolApplianceConfiguration[] configurations = {
			new ToolApplianceConfiguration(
					0,
					new ToolApplianceConfigurationProgram(0, "Washing 0",
							"Washing 0", "wasssssccchn"),
					new ToolApplianceConfigurationExtra[] {},
					new ToolApplianceConfigurationProfile[] {
							new ToolApplianceConfigurationProfile(
									0,
									"hybrid uninterruptible",
									new String[] { "Washing 0" },
									new String[] {
											"data/profiles/renamed/wm/WM_0_MC.csv", },
									0, 1, 2, -1),
							}),
			new ToolApplianceConfiguration(
					1,
					new ToolApplianceConfigurationProgram(0, "Washing 1",
							"Washing 1", "wasssssccchn"),
					new ToolApplianceConfigurationExtra[] {},
					new ToolApplianceConfigurationProfile[] {
							new ToolApplianceConfigurationProfile(
									0,
									"hybrid uninterruptible",
									new String[] { "Washing 0" },
									new String[] {
											"data/profiles/renamed/wm/WM_1_MC.csv", },
									0, 1, 2, -1),
							}),
			new ToolApplianceConfiguration(
					2,
					new ToolApplianceConfigurationProgram(0, "Washing 2",
							"Washing 2", "wasssssccchn"),
					new ToolApplianceConfigurationExtra[] {},
					new ToolApplianceConfigurationProfile[] {
							new ToolApplianceConfigurationProfile(
									0,
									"hybrid uninterruptible",
									new String[] { "Washing 0" },
									new String[] {
											"data/profiles/renamed/wm/WM_2_MC.csv", },
									0, 1, 2, -1), 
							}) };

}
