package constructsimulation.generation.device.old.waterheating;

import java.util.UUID;

import constructsimulation.generation.device.CreateDevice;
import constructsimulation.generation.parameter.CreateConfigurationParameter;
import osh.configuration.eal.AssignedDevice;
import osh.configuration.system.ConfigurationParameter;
import osh.configuration.system.DeviceClassification;
import osh.configuration.system.DeviceTypes;
import osh.simulation.screenplay.ScreenplayType;

/**
 * 
 * @author Ingo Mauser
 *
 */
@Deprecated
public class CreateWaterHeatingDevice {
	
//	private static String drawofftypesfile = 			"configfiles/watertank/own/drawofftypesfile.csv";
	private static String drawoffscheduleweekdayfile = 	"configfiles/watertank/own/1p_wd_schedule.csv";
	private static String drawoffscheduleweekendfile = 	"configfiles/watertank/own/1p_we_schedule.csv";
//	private static String drawoffscheduleweekdayfile = 	"configfiles/watertank/own/3p_wd_schedule.csv";
//	private static String drawoffscheduleweekendfile = 	"configfiles/watertank/own/3p_we_schedule.csv";
//	private static String drawoffscheduleweekdayfile = 	"configfiles/watertank/own/5p_wd_schedule.csv";
//	private static String drawoffscheduleweekendfile = 	"configfiles/watertank/own/5p_we_schedule.csv";
	
	private static String drawofftypesfile = 			"configfiles/watertank/eu/eu_drawofftypesfile.csv";
//	private static String drawoffscheduleweekdayfile =	"configfiles/watertank/eu/eu_s_schedule.csv";
//	private static String drawoffscheduleweekendfile =	"configfiles/watertank/eu/eu_s_schedule.csv";
//	private static String drawoffscheduleweekdayfile =	"configfiles/watertank/eu/eu_m_schedule.csv";
//	private static String drawoffscheduleweekendfile =	"configfiles/watertank/eu/eu_m_schedule.csv";
//	private static String drawoffscheduleweekdayfile =	"configfiles/watertank/eu/eu_l_schedule.csv";
//	private static String drawoffscheduleweekendfile =	"configfiles/watertank/eu/eu_l_schedule.csv";
	
//	private static String tankcapacity = "25"; // [liter]
//	private static String tankcapacity = "50"; // [liter]
//	private static String tankcapacity = "75"; // [liter]
	private static String tankcapacity = "100"; // [liter]
//	private static String tankcapacity = "125"; // [liter]
//	private static String tankcapacity = "150"; // [liter]
//	private static String tankcapacity = "250"; // [liter]
//	private static String tankcapacity = "300"; // [liter]
//	private static String tankcapacity = "500"; // [liter]

	public static AssignedDevice createWaterHeatingDevice(
			UUID uuid,
			String driverClassName,
			String localObserverClass,
			String localControllerClass,
			ScreenplayType screenplayType,
			String[] pvNominalPower
			) {
		
		AssignedDevice _assdev = CreateDevice.createDevice(
				DeviceTypes.WATERHEATERSTORAGE, 
				DeviceClassification.HVAC, 
				uuid, 
				driverClassName, 
				localObserverClass, 
				true, 
				localControllerClass);
		
		// ### PARAMETERS ###
		{
			ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
					"screenplaytype", 
					"String", 
					"" + screenplayType);
			_assdev.getDriverParameters().add(cp);
		}
		{
			int pvNominalPowerInt = 0;
			for (String nP : pvNominalPower) {
				pvNominalPowerInt = pvNominalPowerInt + Integer.valueOf(nP);
			}
			
			ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
					"pvnominalpower", 
					"String", 
					"" + pvNominalPowerInt);
			_assdev.getDriverParameters().add(cp);
		}
		{
			ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
					"drawofftypesfile", 
					"String", 
					"" + drawofftypesfile);
			_assdev.getDriverParameters().add(cp);
		}
		{
			ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
					"drawoffscheduleweekdayfile", 
					"String", 
					"" + drawoffscheduleweekdayfile);
			_assdev.getDriverParameters().add(cp);
		}
		{
			ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
					"drawoffscheduleweekendfile", 
					"String", 
					"" + drawoffscheduleweekendfile);
			_assdev.getDriverParameters().add(cp);
		}
		{
			ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
					"tankcapacity", 
					"String", 
					"" + tankcapacity);
			_assdev.getDriverParameters().add(cp);
		}
				
		return _assdev;
	}
}
