package osh.simulation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class OSHSimulationResults extends SimulationResults {

	private static final double kWHConversionFactor = 3600000.0;
	
	protected double activePowerConsumption;
	
	protected double activePowerPV;
	protected double activePowerPVAutoConsumption;
	protected double activePowerPVFeedIn;
	
	protected double activePowerCHP;
	protected double activePowerCHPAutoConsumption;
	protected double activePowerCHPFeedIn;
	
	protected double activePowerBatteryCharging;
	protected double activePowerBatteryDischarging;
	protected double activePowerBatteryAutoConsumption;	
	protected double activePowerBatteryFeedIn;
	
	protected double activePowerExternal;
	
	protected double reactivePowerExternal;
	
	protected double gasPowerExternal;
	
	protected double epsCosts;
	protected double plsCosts;
	protected double gasCosts;
	protected double feedInCostsPV;
	protected double feedInCostsCHP;
	protected double totalCosts;
	protected double autoConsumptionCosts;
	
	public double getTotalCosts() {
		return totalCosts;
	}
	
	public double getEpsCosts() {
		return epsCosts;
	}
	
	public double getPlsCosts() {
		return plsCosts;
	}
	
	public double getGasCosts() {
		return gasCosts;
	}
	
	public double getFeedInCostsPV() {
		return feedInCostsPV;
	}
	
	public double getFeedInCostsCHP() {
		return feedInCostsCHP;
	}
	
	public double getAutoConsumptionCosts() {
		return autoConsumptionCosts;
	}
	
	//only converting from Ws to kWh when getting values, will minimise errors due to fp-arithemtic
	public double getActivePowerConsumption() {
		return activePowerConsumption / kWHConversionFactor;
	}

	public double getActivePowerPV() {
		return activePowerPV / kWHConversionFactor;
	}

	public double getActivePowerCHP() {
		return activePowerCHP / kWHConversionFactor;
	}
	
	public double getActivePowerPVAutoConsumption() {
		return activePowerPVAutoConsumption / kWHConversionFactor;
	}

	public double getActivePowerPVFeedIn() {
		return activePowerPVFeedIn / kWHConversionFactor;
	}
	
	public double getActivePowerCHPAutoConsumption() {
		return activePowerCHPAutoConsumption / kWHConversionFactor;
	}

	public double getActivePowerCHPFeedIn() {
		return activePowerCHPFeedIn / kWHConversionFactor;
	}
	
	public double getActivePowerExternal() {
		return activePowerExternal / kWHConversionFactor;
	}
	
	public double getReactivePowerExternal() {
		return reactivePowerExternal / kWHConversionFactor;
	}
	
	public double getGasPowerExternal() {
		return gasPowerExternal / kWHConversionFactor;
	}
	
	public double getActivePowerBatteryCharging() {
		return activePowerBatteryCharging / kWHConversionFactor;
	}
	
	public double getActivePowerBatteryDischarging() {
		return activePowerBatteryDischarging / kWHConversionFactor;
	}

	public double getActivePowerBatteryAutoConsumption() {
		return activePowerBatteryAutoConsumption / kWHConversionFactor;
	}

	public double getActivePowerBatteryFeedIn() {
		return activePowerBatteryFeedIn / kWHConversionFactor;
	}

	public void addActivePowerConsumption(double additional) {
		activePowerConsumption = activePowerConsumption + additional;
	}
	
	public void addActivePowerPV(double additional) {
		activePowerPV = activePowerPV + additional;
	}
	
	public void addActivePowerPVAutoConsumption(double additional) {
		activePowerPVAutoConsumption = activePowerPVAutoConsumption + additional;
	}
	
	public void addActivePowerPVFeedIn(double additional) {
		activePowerPVFeedIn = activePowerPVFeedIn + additional;
	}
	
	public void addActivePowerCHP(double additional) {
		activePowerCHP = activePowerCHP + additional;
	}
	
	public void addActivePowerCHPAutoConsumption(double additional) {
		activePowerCHPAutoConsumption = activePowerCHPAutoConsumption + additional;
	}
	
	public void addActivePowerCHPFeedIn(double additional) {
		activePowerCHPFeedIn = activePowerCHPFeedIn + additional;
	}
	
	public void addActivePowerBatteryCharging(double additional) {
		activePowerBatteryCharging = activePowerBatteryCharging + additional;
	}
	
	public void addActivePowerBatteryDischarging(double additional) {
		activePowerBatteryDischarging = activePowerBatteryDischarging + additional;
	}

	public void addActivePowerBatteryAutoConsumption(double additional) {
		activePowerBatteryAutoConsumption = activePowerBatteryAutoConsumption + additional;
	}

	public void addActivePowerBatteryFeedIn(double additional) {
		activePowerBatteryFeedIn = activePowerBatteryFeedIn + additional;
	}
	
	public void addActivePowerExternal(double additional) {
		activePowerExternal = activePowerExternal + additional;
	}
	
	public void addCostsToTotalCosts(double additional) {
		totalCosts = totalCosts + additional;
	}
	
	public void addEpsCostsToEpsCosts(double epsCosts) {
		this.epsCosts = this.epsCosts + epsCosts;
	}
	
	public void addPlsCostsToPlsCosts(double plsCosts) {
		this.plsCosts = this.plsCosts + plsCosts;
	}
	
	public void addFeedInCostsToFeedInCostsPV(double feedInCostsPV) {
		this.feedInCostsPV = this.feedInCostsPV + feedInCostsPV;
	}
	
	public void addFeedInCostsToFeedInCostsCHP(double feedInCostsCHP) {
		this.feedInCostsCHP = this.feedInCostsCHP + feedInCostsCHP;
	}
	
	public void addReactivePowerExternal(double additional) {
		reactivePowerExternal = reactivePowerExternal + additional;
	}
	
	public void addGasPowerExternal(double additional) {
		gasPowerExternal = gasPowerExternal + additional;
	}

	public void addGasCostsToGasCosts(double additional) {
		this.gasCosts = this.gasCosts + additional;
	}
	
	public void addAutoConsumptionCostsToAutoConsumptionCosts(double additional) {
		this.autoConsumptionCosts = this.autoConsumptionCosts + additional;
	}
	
	public OSHSimulationResults clone() {
		OSHSimulationResults clone = new OSHSimulationResults();
		clone.activePowerCHP = this.activePowerCHP;
		clone.activePowerCHPAutoConsumption = this.activePowerCHPAutoConsumption;
		clone.activePowerCHPFeedIn = this.activePowerCHPFeedIn;
		clone.activePowerConsumption = this.activePowerConsumption;
		clone.activePowerExternal = this.activePowerExternal;
		clone.activePowerPV = this.activePowerPV;
		clone.activePowerPVAutoConsumption = this.activePowerPVAutoConsumption;
		clone.activePowerPVFeedIn = this.activePowerPVFeedIn;
		clone.activePowerBatteryCharging = this.activePowerBatteryCharging;
		clone.activePowerBatteryDischarging = this.activePowerBatteryDischarging;
		clone.activePowerBatteryAutoConsumption = this.activePowerBatteryAutoConsumption;
		clone.activePowerBatteryFeedIn = this.activePowerBatteryFeedIn;
		clone.epsCosts = this.epsCosts;
		clone.gasCosts = this.gasCosts;
		clone.gasPowerExternal = this.gasPowerExternal;
		clone.plsCosts = this.plsCosts;
		clone.reactivePowerExternal = this.reactivePowerExternal;
		clone.totalCosts = this.totalCosts;
		clone.feedInCostsPV = this.feedInCostsPV;
		clone.feedInCostsCHP = this.feedInCostsCHP;
		clone.autoConsumptionCosts = this.autoConsumptionCosts;
		
		return clone;
	}
	
	public void generateDiffToOtherResult(OSHSimulationResults result) {
		
		this.activePowerCHP = result.activePowerCHP - this.activePowerCHP;
		this.activePowerCHPAutoConsumption = result.activePowerCHPAutoConsumption - this.activePowerCHPAutoConsumption;
		this.activePowerCHPFeedIn = result.activePowerCHPFeedIn - this.activePowerCHPFeedIn;
		this.activePowerConsumption = result.activePowerConsumption - this.activePowerConsumption;
		this.activePowerExternal = result.activePowerExternal - this.activePowerExternal;
		this.activePowerPV = result.activePowerPV - this.activePowerPV;
		this.activePowerPVAutoConsumption = result.activePowerPVAutoConsumption - this.activePowerPVAutoConsumption;
		this.activePowerPVFeedIn = result.activePowerPVFeedIn - this.activePowerPVFeedIn;
		this.activePowerBatteryCharging = result.activePowerBatteryCharging - this.activePowerBatteryCharging;
		this.activePowerBatteryDischarging = result.activePowerBatteryDischarging - this.activePowerBatteryDischarging;
		this.activePowerBatteryAutoConsumption = result.activePowerBatteryAutoConsumption - this.activePowerBatteryAutoConsumption;		
		this.activePowerBatteryFeedIn = result.activePowerBatteryFeedIn - this.activePowerBatteryFeedIn;
		this.epsCosts = result.epsCosts - this.epsCosts;
		this.gasCosts = result.gasCosts - this.gasCosts;
		this.gasPowerExternal = result.gasPowerExternal - this.gasPowerExternal;
		this.plsCosts = result.plsCosts - this.plsCosts;
		this.reactivePowerExternal = result.reactivePowerExternal - this.reactivePowerExternal;
		this.totalCosts = result.totalCosts - this.totalCosts;
		this.feedInCostsPV = result.feedInCostsPV - this.feedInCostsPV;
		this.feedInCostsCHP = result.feedInCostsCHP - this.feedInCostsCHP;
		this.autoConsumptionCosts = result.autoConsumptionCosts - this.autoConsumptionCosts;
	}
	
	public void logCurrentStateToFile(File file, long runTime) throws FileNotFoundException {
		PrintWriter pwrFull = new PrintWriter(file);
			pwrFull.println("Runtime;" + runTime);
			pwrFull.println("ActivePowerConsumption;"
					+ this.getActivePowerConsumption());
			pwrFull.println("ActivePowerPV;"
					+ this.getActivePowerPV());
			pwrFull.println("ActivePowerPVAutoConsumption;"
					+ this.getActivePowerPVAutoConsumption());
			pwrFull.println("ActivePowerPVFeedIn;"
					+ this.getActivePowerPVFeedIn());
			pwrFull.println("ActivePowerCHP;"
					+ this.getActivePowerCHP());
			pwrFull.println("ActivePowerCHPAutoConsumption;"
					+ this.getActivePowerCHPAutoConsumption());
			pwrFull.println("ActivePowerCHPFeedIn;"
					+ this.getActivePowerCHPFeedIn());
			pwrFull.println("ActivePowerBatteryCharging;"
					+ this.getActivePowerBatteryCharging());
			pwrFull.println("ActivePowerBatteryDischarging;"
					+ this.getActivePowerBatteryDischarging());
			pwrFull.println("ActivePowerBatteryAutoConsumption;"
					+ this.getActivePowerBatteryAutoConsumption());
			pwrFull.println("ActivePowerBatteryFeedIn;"
					+ this.getActivePowerBatteryFeedIn());
			pwrFull.println("ActivePowerExternal;"
					+ this.getActivePowerExternal());
			pwrFull.println("ReactivePowerExternal;"
					+ this.getReactivePowerExternal());
			pwrFull.println("GasPowerExternal;"
					+ this.getGasPowerExternal());
			pwrFull.println("EpsCosts;" + this.getEpsCosts());
			pwrFull.println("PlsCosts;" + this.getPlsCosts());
			pwrFull.println("GasCosts;" + this.getGasCosts());
			pwrFull.println("FeedInCostsPV;" + this.getFeedInCostsPV());
			pwrFull.println("FeedInCostsCHP;" + this.getFeedInCostsCHP());
			pwrFull.println("AutoConsumptionCosts;" + this.getAutoConsumptionCosts());
			pwrFull.println("TotalCosts;" + this.getTotalCosts());			
		
		pwrFull.close();
	}
}
