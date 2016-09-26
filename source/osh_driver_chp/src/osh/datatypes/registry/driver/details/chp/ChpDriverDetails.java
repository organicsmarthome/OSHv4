package osh.datatypes.registry.driver.details.chp;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import osh.datatypes.registry.StateExchange;


/**
 * 
 * @author Ingo Mauser
 *
 */
@XmlAccessorType( XmlAccessType.FIELD )
@XmlRootElement(name="ChpDriverDetails")
@XmlType

public class ChpDriverDetails extends StateExchange {
	
	/** Serial */
	private static final long serialVersionUID = 4269794621296448446L;
	
	// Heating request or power request? Or both?
	protected boolean powerGenerationRequest;
	protected boolean heatingRequest;

	// current power
	protected double currentElectricalPower;
	protected double currentThermalPower;
	
	// total energy
	protected double generatedElectricalWork;
	protected double generatedThermalWork;
	
	// priorities
	protected boolean electicalPowerPriorizedControl;
	protected boolean thermalPowerPriorizedControl;
	
	//
	protected int temperatureIn;
	protected int temperatureOut;
	
	
	/** for JAXB */
	@SuppressWarnings("unused")
	@Deprecated
	private ChpDriverDetails() {
		super(null, 0);
	}
	
	/**
	 * CONSTRUCTOR
	 */
	public ChpDriverDetails(UUID sender, long timestamp) {
		super(sender, timestamp);
	}

	
	public double getCurrentElectricalPower() {
		return currentElectricalPower;
	}

	public void setCurrentElectricalPower(double currentElectricalPower) {
		this.currentElectricalPower = currentElectricalPower;
	}

	public double getCurrentThermalPower() {
		return currentThermalPower;
	}

	public void setCurrentThermalPower(double currentThermalPower) {
		this.currentThermalPower = currentThermalPower;
	}
	
	public double getGeneratedElectricalWork() {
		return generatedElectricalWork;
	}
	public void setGeneratedElectricalWork(double generatedElectricalWork) {
		this.generatedElectricalWork = generatedElectricalWork;
	}
	
	public double getGeneratedThermalWork() {
		return generatedThermalWork;
	}
	public void setGeneratedThermalWork(double generatedThermalWork) {
		this.generatedThermalWork = generatedThermalWork;
	}
	
	public boolean getElecticalPowerPriorizedControl() {
		return electicalPowerPriorizedControl;
	}
	public void setElecticalPowerPriorizedControl(boolean electicalPowerPriorizedControl) {
		this.electicalPowerPriorizedControl = electicalPowerPriorizedControl;
	}
	
	public boolean getThermalPowerPriorizedControl() {
		return thermalPowerPriorizedControl;
	}
	public void setThermalPowerPriorizedControl(boolean thermalPowerPriorizedControl) {
		this.thermalPowerPriorizedControl = thermalPowerPriorizedControl;
	}
	
	public boolean isHeatingRequest() {
		return heatingRequest;
	}

	public void setHeatingRequest(boolean heatingRequest) {
		this.heatingRequest = heatingRequest;
	}

	public boolean isPowerGenerationRequest() {
		return powerGenerationRequest;
	}

	public void setPowerGenerationRequest(boolean powerGenerationRequest) {
		this.powerGenerationRequest = powerGenerationRequest;
	}

	public int getTemperatureIn() {
		return temperatureIn;
	}

	public void setTemperatureIn(int temperatureIn) {
		this.temperatureIn = temperatureIn;
	}

	public int getTemperatureOut() {
		return temperatureOut;
	}

	public void setTemperatureOut(int temperatureOut) {
		this.temperatureOut = temperatureOut;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[currentElectricalPower=").append(currentElectricalPower).append(",");
		builder.append("currentThermalPower=").append(currentThermalPower).append(",");
		builder.append("generatedElectricalWork=").append(generatedElectricalWork).append(",");
		builder.append("generatedThermalWork=").append(generatedThermalWork).append(",");
		builder.append("electicalPowerPriorizedControl=").append(electicalPowerPriorizedControl).append(",");
		builder.append("thermalPowerPriorizedControl=").append(thermalPowerPriorizedControl).append("]");
		return builder.toString();
	}

}
