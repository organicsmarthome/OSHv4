package osh.comdriver.interaction.datatypes;

import java.util.List;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

//import osh.comdriver.interaction.datatypes.fzi.RestHabiteqDetails;
//import osh.comdriver.interaction.datatypes.fzi.RestRemoteControlDetails;

/**
 * XML serializable
 * 
 * @author Kaibin Bao, Ingo Mauser
 */
@XmlRootElement(name="Device")
@XmlAccessorType(XmlAccessType.FIELD)
public class RestDevice {

	@XmlAttribute(name="uuid")
	public UUID uuid;
	
	public RestDeviceMetaDetails deviceMetaDetails;
	
	public RestGenericParametersDetails genericParametersDetails;
	
	public RestBusDeviceStatusDetails busDeviceStatusDetails;
	
	public RestPowerDetails powerDetails;
	public RestElectricityDetails electricityDetails;
	
	public RestSwitchDetails switchDetails;
	
	public RestApplianceDetails applianceDetails;
	
	public RestTemperatureDetails temperatureDetails;
	public RestConfigurationDetails configurationDetails;
	public RestScheduleDetails scheduleDetails;
	
	//FZI
//	public RestRemoteControlDetails remoteControlDetails;
	
	public RestTemperatureSensorDetails temperatureSensorDetails;
	public RestLightStateDetails lightStateDetails;
	public RestWindowSensorDetails windowSensorDetails;
	
//	public RestHabiteqDetails habiteqDetails;

	
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
	
		
	public RestDeviceMetaDetails getDeviceMetaDetails() {
		if( deviceMetaDetails == null )
			deviceMetaDetails = new RestDeviceMetaDetails(null, 0L);
		return deviceMetaDetails;
	}
	
	public boolean hasDeviceMetaDetails() {
		return (deviceMetaDetails != null);
	}
	
	public RestGenericParametersDetails getGenericParametersDetails() {
		if( genericParametersDetails == null )
			genericParametersDetails = new RestGenericParametersDetails(null, 0L);
		return genericParametersDetails;
	}
	
	public boolean hasGenericParametersDetails() {
		return (genericParametersDetails != null);
	}
	
	public RestBusDeviceStatusDetails getBusDeviceStatusDetails() {
		if( busDeviceStatusDetails == null )
			busDeviceStatusDetails = new RestBusDeviceStatusDetails(null, 0L);
		return busDeviceStatusDetails;
	}

	
	public RestPowerDetails getPowerDetails() {
		if( powerDetails == null )
			powerDetails = new RestPowerDetails(null, 0L);
		return powerDetails;
	}

	public RestSwitchDetails getSwitchDetails() {
		if( switchDetails == null )
			switchDetails = new RestSwitchDetails(null, 0L);
		return switchDetails;
	}

	public RestElectricityDetails getElectricityDetails() {
		if( electricityDetails == null )
			electricityDetails = new RestElectricityDetails(null, 0L);
		return electricityDetails;
	}
	
//	public void setElectricityDetails(RestElectricityDetails electricityDetails) {
//		this.electricityDetails = electricityDetails;
//	}
	
	public RestApplianceDetails getApplianceDetails() {
		if( applianceDetails == null )
			applianceDetails = new RestApplianceDetails(null, 0L);
		return applianceDetails;
	}
	
	
	public RestTemperatureDetails getTemperatureDetails() {
		if( temperatureDetails == null )
			temperatureDetails = new RestTemperatureDetails(null, 0L);
		return temperatureDetails;
	}
	
	public RestConfigurationDetails getConfigurationDetails() {
		if( configurationDetails == null )
			configurationDetails = new RestConfigurationDetails(null, 0L);
		return configurationDetails;
	}
	
	public RestScheduleDetails getScheduleDetails() {
		if( scheduleDetails == null )
			scheduleDetails = new RestScheduleDetails(null, 0L);
		return scheduleDetails;
	}
	
	public void setScheduleDetails(RestScheduleDetails scheduleDetails) {
		this.scheduleDetails = scheduleDetails;
	}
	

	
	
	public RestTemperatureSensorDetails getTemperatureSensorDetails() {
		if( temperatureSensorDetails == null )
			temperatureSensorDetails = new RestTemperatureSensorDetails(null, 0L);
		return temperatureSensorDetails;
	}

	
	
	public RestLightStateDetails getLightStatusDetails() {
		if( lightStateDetails == null )
			lightStateDetails = new RestLightStateDetails(null, 0L);
		return lightStateDetails;
	}

	
	public RestWindowSensorDetails getWindowSensorDetails() {
		if( windowSensorDetails == null )
			windowSensorDetails = new RestWindowSensorDetails(null, 0L);
		return windowSensorDetails;
	}

	
	public RestDevice cloneOnly(List<String> typeNames) {
		RestDevice clone = new RestDevice();
		clone.setUuid(uuid);
		boolean cloneHasContent = false;

		// RestDeviceMetaDetails deviceMetaDetails
		if (typeNames.contains(RestDeviceMetaDetails.class.getAnnotation(
				XmlType.class).name())) {
			clone.deviceMetaDetails = deviceMetaDetails;
			if (clone.deviceMetaDetails != null)
				cloneHasContent = true;
		}
		
		// RestGenericParametersDetails genericParametersDetails
		if (typeNames.contains(RestGenericParametersDetails.class.getAnnotation(
				XmlType.class).name())) {
			clone.genericParametersDetails = genericParametersDetails;
			if (clone.genericParametersDetails != null)
				cloneHasContent = true;
		}
		
		// RestBusDeviceStatusDetails busDeviceStatusDetails
		if (typeNames.contains(RestBusDeviceStatusDetails.class.getAnnotation(
				XmlType.class).name())) {
			clone.busDeviceStatusDetails = busDeviceStatusDetails;
			if (clone.busDeviceStatusDetails != null)
				cloneHasContent = true;
		}
		
		// RestPowerDetails powerDetails
		if (typeNames.contains(RestPowerDetails.class.getAnnotation(
				XmlType.class).name())) {
			clone.powerDetails = powerDetails;
			if (clone.powerDetails != null)
				cloneHasContent = true;
		}
		
		// RestElectricityDetails electricityDetails
		if (typeNames.contains(RestElectricityDetails.class.getAnnotation(
				XmlType.class).name())) {
			clone.electricityDetails = electricityDetails;
			if (clone.electricityDetails != null)
				cloneHasContent = true;
		}
		
		// RestSwitchDetails switchDetails
		if (typeNames.contains(RestSwitchDetails.class.getAnnotation(
				XmlType.class).name())) {
			clone.switchDetails = switchDetails;
			if (clone.switchDetails != null)
				cloneHasContent = true;
		}
		
		// RestApplianceDetails applianceDetails
		if (typeNames.contains(RestApplianceDetails.class.getAnnotation(
				XmlType.class).name())) {
			clone.applianceDetails = applianceDetails;
			if (clone.applianceDetails != null)
				cloneHasContent = true;
		}
		
		// RestTemperatureDetails temperatureDetails
		if (typeNames.contains(RestTemperatureDetails.class.getAnnotation(
				XmlType.class).name())) {
			clone.temperatureDetails = temperatureDetails;
			if (clone.temperatureDetails != null)
				cloneHasContent = true;
		}
		
		// RestConfigurationDetails configurationDetails
		if (typeNames.contains(RestConfigurationDetails.class.getAnnotation(
				XmlType.class).name())) {
			clone.configurationDetails = configurationDetails;
			if (clone.configurationDetails != null)
				cloneHasContent = true;
		}
		
		// RestScheduleDetails scheduleDetails
		if (typeNames.contains(RestScheduleDetails.class.getAnnotation(
				XmlType.class).name())) {
			clone.scheduleDetails = scheduleDetails;
			if (clone.scheduleDetails != null)
				cloneHasContent = true;
		}
		
		// ###########
		// ### FZI ###
		// ###########
		
		
		// RestTemperatureSensorDetails temperatureSensorDetails
		if (typeNames.contains(RestTemperatureSensorDetails.class.getAnnotation(
				XmlType.class).name())) {
			clone.temperatureSensorDetails = temperatureSensorDetails;
			if (clone.temperatureSensorDetails != null)
				cloneHasContent = true;
		}
		
		// RestLightStateDetails lightStateDetails
		if (typeNames.contains(RestLightStateDetails.class.getAnnotation(
				XmlType.class).name())) {
			clone.lightStateDetails = lightStateDetails;
			if (clone.lightStateDetails != null)
				cloneHasContent = true;
		}
		
		// RestWindowSensorDetails windowSensorDetails
		if (typeNames.contains(RestWindowSensorDetails.class.getAnnotation(
				XmlType.class).name())) {
			clone.windowSensorDetails = windowSensorDetails;
			if (clone.windowSensorDetails != null)
				cloneHasContent = true;
		}

		if( cloneHasContent )
			return clone;
		else
			return null;
	}
}
