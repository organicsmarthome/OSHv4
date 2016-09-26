package osh.toolbox.appliance.td.heatpump;

import osh.toolbox.appliance.ToolApplianceConfiguration;
import osh.toolbox.appliance.ToolApplianceConfigurationExtra;
import osh.toolbox.appliance.ToolApplianceConfigurationProfile;
import osh.toolbox.appliance.ToolApplianceConfigurationProgram;

/**
 * 
 * @author Matthias Maerz
 *
 */
public class TD_HP_3CO_3PH_1PR_SC_E {

	public static ToolApplianceConfiguration[] configurations = {
			new ToolApplianceConfiguration(
					0,
					new ToolApplianceConfigurationProgram(
							0, 
							"Koch/Bunt schranktrocken", 
							"Koch/Bunt schranktrocken ohne Optionen",
							"Koch/Bunt schranktrocken ohne Optionen"),
					new ToolApplianceConfigurationExtra[] {},
					new ToolApplianceConfigurationProfile[] { new ToolApplianceConfigurationProfile(
							0,
							"Koch/Bunt schranktrocken",
							new String[] { 
									"Drying 0",
									"Drying 1",
									"Drying 2", },
							new String[] { 
									"data/td_bosch/renamed/TD_HP_pause_0.csv",
									"data/td_bosch/renamed/TD_HP_0.csv",
									"data/td_bosch/renamed/TD_HP_pause_1.csv",
									},
							0, -1, -1, -1)}
			),
			new ToolApplianceConfiguration(
					1,
					new ToolApplianceConfigurationProgram(
							0, 
							"Koch/Bunt schranktrocken extra", 
							"Koch/Bunt schranktrocken extra ohne Optionen",
							"Koch/Bunt schranktrocken extra ohne Optionen"),
					new ToolApplianceConfigurationExtra[] {},
					new ToolApplianceConfigurationProfile[] {
					new ToolApplianceConfigurationProfile(
							0,
							"Koch/Bunt schranktrocken extra",
							new String[] { 
									"Drying 0",
									"Drying 1",
									"Drying 2", },
							new String[] { 
									"data/td_bosch/renamed/TD_HP_pause_0.csv",
									"data/td_bosch/renamed/TD_HP_1.csv",
									"data/td_bosch/renamed/TD_HP_pause_1.csv", 
									},
							0, -1, -1, -1)}
			),
			new ToolApplianceConfiguration(
					2,
					new ToolApplianceConfigurationProgram(
							0, 
							"Koch/Bunt trocken extra", 
							"Koch/Bunt trocken extra ohne Optionen",
							"Koch/Bunt trocken extra ohne Optionen"),
					new ToolApplianceConfigurationExtra[] {},
					new ToolApplianceConfigurationProfile[] { 
					new ToolApplianceConfigurationProfile(
							0,
							"Koch/Bunt trocken extra",
							new String[] { 
									"Drying 0",
									"Drying 1",
									"Drying 2", },
							new String[] { 
									"data/td_bosch/renamed/TD_HP_pause_0.csv",
									"data/td_bosch/renamed/TD_HP_2.csv",
									"data/td_bosch/renamed/TD_HP_pause_1.csv", 
									},
							0, -1, -1, -1)}
			)
	};

}
