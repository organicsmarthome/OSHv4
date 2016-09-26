package osh.toolbox.appliance.wm;

import osh.toolbox.appliance.ToolApplianceConfiguration;
import osh.toolbox.appliance.ToolApplianceConfigurationExtra;
import osh.toolbox.appliance.ToolApplianceConfigurationProfile;
import osh.toolbox.appliance.ToolApplianceConfigurationProgram;

/**
 * Washer Low Energy 4 Configurations 3 Phase Multi Commodity
 * 
 * @author Ingo Mauser
 *
 */
public class WM_3CO_5PH_1PR_SC_E {

	public static ToolApplianceConfiguration[] configurations = {
			new ToolApplianceConfiguration(
					0,
					new ToolApplianceConfigurationProgram(0, "Washing 0",
							"Washing 0", "wasssssccchn"),
					new ToolApplianceConfigurationExtra[] {},
					new ToolApplianceConfigurationProfile[] { new ToolApplianceConfigurationProfile(
							0, "pure electric uninterruptible", new String[] {
									"Washing 0", "Washing 1", "Washing 2", "Washing 3", "Washing 4" },
							new String[] {
									"data/profiles/renamed/wm/WM_pause.csv",
									"data/profiles/renamed/wm/WM_0_A_0.csv",
									"data/profiles/renamed/wm/WM_pause.csv", 
									"data/profiles/renamed/wm/WM_0_A_1.csv",
									"data/profiles/renamed/wm/WM_pause.csv", 
									},
							0, 1, -1, -1), }),
			new ToolApplianceConfiguration(
					1,
					new ToolApplianceConfigurationProgram(0, "Washing 1",
							"Washing 1", "wasssssccchn"),
					new ToolApplianceConfigurationExtra[] {},
					new ToolApplianceConfigurationProfile[] { new ToolApplianceConfigurationProfile(
							0, "pure electric uninterruptible", new String[] {
									"Washing 0", "Washing 1", "Washing 2", "Washing 3", "Washing 4" },
							new String[] {
									"data/profiles/renamed/wm/WM_pause.csv",
									"data/profiles/renamed/wm/WM_1_A_0.csv",
									"data/profiles/renamed/wm/WM_pause.csv", 
									"data/profiles/renamed/wm/WM_1_A_1.csv",
									"data/profiles/renamed/wm/WM_pause.csv", 
									},
							0, 1, -1, -1), }),
			new ToolApplianceConfiguration(
					2,
					new ToolApplianceConfigurationProgram(0, "Washing 2",
							"Washing 2", "wasssssccchn"),
					new ToolApplianceConfigurationExtra[] {},
					new ToolApplianceConfigurationProfile[] { new ToolApplianceConfigurationProfile(
							0, "pure electric uninterruptible", new String[] {
									"Washing 0", "Washing 1", "Washing 2", "Washing 3", "Washing 4" },
							new String[] {
									"data/profiles/renamed/wm/WM_pause.csv",
									"data/profiles/renamed/wm/WM_2_A_0.csv",
									"data/profiles/renamed/wm/WM_pause.csv",
									"data/profiles/renamed/wm/WM_2_A_1.csv",
									"data/profiles/renamed/wm/WM_pause.csv",
									},
							0, 1, -1, -1), }), };

}
