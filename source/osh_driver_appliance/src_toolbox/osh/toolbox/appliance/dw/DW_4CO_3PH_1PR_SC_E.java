package osh.toolbox.appliance.dw;

import osh.toolbox.appliance.ToolApplianceConfiguration;
import osh.toolbox.appliance.ToolApplianceConfigurationExtra;
import osh.toolbox.appliance.ToolApplianceConfigurationProfile;
import osh.toolbox.appliance.ToolApplianceConfigurationProgram;

/**
 * Dishwasher Low Energy 4 Configurations 1 Phase Single Commodity
 * 
 * @author Ingo Mauser
 *
 */
public class DW_4CO_3PH_1PR_SC_E {

	public static ToolApplianceConfiguration[] configurations = {
			new ToolApplianceConfiguration(
					0,
					new ToolApplianceConfigurationProgram(0, "Washing 0",
							"Washing 0", "spueeehhhhln"),
					new ToolApplianceConfigurationExtra[] {},
					new ToolApplianceConfigurationProfile[] { new ToolApplianceConfigurationProfile(
							0, "pure electric uninterruptible", new String[] {
									"Washing 0", "Washing 1", "Washing 2", },
							new String[] {
									"data/profiles/renamed/dw/DW_pause.csv",
									"data/profiles/renamed/dw/DW_0.csv",
									"data/profiles/renamed/dw/DW_pause.csv", },
							0, 1, -1, -1) }),
			new ToolApplianceConfiguration(
					1,
					new ToolApplianceConfigurationProgram(0, "Washing 1",
							"Washing 1", "spueeehhhhln"),
					new ToolApplianceConfigurationExtra[] {},
					new ToolApplianceConfigurationProfile[] { new ToolApplianceConfigurationProfile(
							0, "pure electric uninterruptible", new String[] {
									"Washing 0", "Washing 1", "Washing 2", },
							new String[] {
									"data/profiles/renamed/dw/DW_pause.csv",
									"data/profiles/renamed/dw/DW_1.csv",
									"data/profiles/renamed/dw/DW_pause.csv", },
							0, 1, -1, -1) }),
			new ToolApplianceConfiguration(
					2,
					new ToolApplianceConfigurationProgram(0, "Washing 2",
							"Washing 2", "spueeehhhhln"),
					new ToolApplianceConfigurationExtra[] {},
					new ToolApplianceConfigurationProfile[] { new ToolApplianceConfigurationProfile(
							0, "pure electric uninterruptible", new String[] {
									"Washing 0", "Washing 1", "Washing 2", },
							new String[] {
									"data/profiles/renamed/dw/DW_pause.csv",
									"data/profiles/renamed/dw/DW_2.csv",
									"data/profiles/renamed/dw/DW_pause.csv", },
							0, 1, -1, -1) }),
			new ToolApplianceConfiguration(
					3,
					new ToolApplianceConfigurationProgram(0, "Washing 3",
							"Washing 3", "spueeehhhhln"),
					new ToolApplianceConfigurationExtra[] {},
					new ToolApplianceConfigurationProfile[] { new ToolApplianceConfigurationProfile(
							0, "pure electric uninterruptible", new String[] {
									"Washing 0", "Washing 1", "Washing 2", },
							new String[] {
									"data/profiles/renamed/dw/DW_pause.csv",
									"data/profiles/renamed/dw/DW_3.csv",
									"data/profiles/renamed/dw/DW_pause.csv", },
							0, 1, -1, -1) }) };

}
