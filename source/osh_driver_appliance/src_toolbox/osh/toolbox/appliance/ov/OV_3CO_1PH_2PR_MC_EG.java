package osh.toolbox.appliance.ov;

import osh.toolbox.appliance.ToolApplianceConfiguration;
import osh.toolbox.appliance.ToolApplianceConfigurationExtra;
import osh.toolbox.appliance.ToolApplianceConfigurationProfile;
import osh.toolbox.appliance.ToolApplianceConfigurationProgram;

/**
 * Oven Low Energy 4 Configurations 1 Phase Single Commodity
 * 
 * @author Ingo Mauser
 *
 */
public class OV_3CO_1PH_2PR_MC_EG {

	public static ToolApplianceConfiguration[] configurations = {
			new ToolApplianceConfiguration(
					0,
					new ToolApplianceConfigurationProgram(0, "Baking 0",
							"Baking 0", "backaeeeen"),
					new ToolApplianceConfigurationExtra[] {},
					new ToolApplianceConfigurationProfile[] {
							new ToolApplianceConfigurationProfile(
									0,
									"pure electric uninterruptible",
									new String[] { "Baking Phase 0", },
									new String[] { "data/profiles/renamed/ov/OV_0.csv", },
									0, 1, -1, -1),
							new ToolApplianceConfigurationProfile(
									1,
									"pure electric uninterruptible",
									new String[] { "Baking Phase 0", },
									new String[] { "data/profiles/renamed/ov/OV_0_MC.csv", },
									0, 1, -1, 2) }),
			new ToolApplianceConfiguration(
					1,
					new ToolApplianceConfigurationProgram(0, "Baking 1",
							"Baking 1", "backaeeeen"),
					new ToolApplianceConfigurationExtra[] {},
					new ToolApplianceConfigurationProfile[] {
							new ToolApplianceConfigurationProfile(
									0,
									"pure electric uninterruptible",
									new String[] { "Baking Phase 0", },
									new String[] { "data/profiles/renamed/ov/OV_1.csv", },
									0, 1, -1, -1),
							new ToolApplianceConfigurationProfile(
									1,
									"pure electric uninterruptible",
									new String[] { "Baking Phase 0", },
									new String[] { "data/profiles/renamed/ov/OV_1_MC.csv", },
									0, 1, -1, 2) }),
			new ToolApplianceConfiguration(
					2,
					new ToolApplianceConfigurationProgram(0, "Baking 2",
							"Baking 2", "backaeeeen"),
					new ToolApplianceConfigurationExtra[] {},
					new ToolApplianceConfigurationProfile[] {
							new ToolApplianceConfigurationProfile(
									0,
									"pure electric uninterruptible",
									new String[] { "Baking Phase 0", },
									new String[] { "data/profiles/renamed/ov/OV_2.csv", },
									0, 1, -1, -1),
							new ToolApplianceConfigurationProfile(
									1,
									"pure electric uninterruptible",
									new String[] { "Baking Phase 0", },
									new String[] { "data/profiles/renamed/ov/OV_2_MC.csv", },
									0, 1, -1, 2) }), };

}
