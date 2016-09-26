package osh.utils.physics;

/**
* complexPower S in [VA]: S^2 = P^2 + Q^2<br>
* cosPhi = P / S (Active Factor, DE: Wirkfaktor)
* 
* @author Ingo Mauser
*
*/
public class ComplexPowerUtil {
	/** 
	 * EN: Active Power ^= Real Power P in [W]<br>
	 * Value > 0 : energy consumption<br>
	 * Value < 0 : energy generation<br>
	 * DE: Wirkleistung
	 */
	private double activePower;
	
	/**
	 * EN: Reactive Power Q in [VAR]<br>
	 * Value > 0 : inductive<br>
	 * Value < 0 : capacitive<br>
	 * DE: Blindleistung
	 */
	private double reactivePower;
	
	/**
	 * EN: Complex Power S in [VA]: S^2 = P^2 + Q^2<br>
	 * DE: Scheinleistung
	 */
	private double complexPower;
	
	/** 
	 * EN: Active Factor cosPhi = P / S<br>
	 * DE: Wirkfaktor<br>
	 * ^= Power Factor (DE: Leistungsfaktor) in case of no harmonics (DE: Oberschwingungen)<br>
	 * (Note: Displacement Factor, DE: Verschiebungsfaktor, |P| / S = |cos(Phi)|)
	 */
	private double cosPhi;
	
	/**
	 * EN: Phase difference angle phi in [rad]<br>
	 * DE: Phasenverschiebungswinkel
	 */
	private double phiInRadian;
		
	
	/**
	 * EN: inductive true/false positive/negative Reactive Power (false ^= capacitive)
	 * DE: induktiv true/false positive/negative Blindleistung (false ^= kapazitiv)
	 */
	private boolean inductive;
	
	/**
	 * You need sometimes more info than 2 values!<br>
	 * a) activePower and cosPhi is NOT sufficient -> needs inductive/capacitive<br>
	 * b) activePower and inductive/capacitive is NOT sufficient -> needs cosPhi<br>
	 * <b>c) reactivePower and cosPhi IS sufficient (IF reactivePower & cosPhi != 0)</b><br>
	 * d) reactivePower and inductive/capacitive is NOT sufficient -> needs cosPhi<br>
	 * e) complexPower and cosPhi is NOT sufficient -> needs inductive/capacitive<br>
	 * f) complexPower and inductive/capacitive is NOT sufficient -> needs cosPhi<br>
	 * @param power (0) activePower or (1) reactivePower or (2) complexPower
	 * @param cosPhi Active Factor cosPhi (DE: Wirkfaktor)
	 * @param powerType 0: activePower 1: reactivePower 2: complexPower else: activePower
	 * @param inductive true: inductive, false: capacitive
	 * @throws Exception 
	 */
	public ComplexPowerUtil(double power, int powerType, double cosPhi, boolean inductive) throws Exception {
		cosPhi = checkCosPhi(cosPhi);
		
		this.cosPhi = cosPhi;
		this.inductive = inductive;
		
		if (inductive == true) {
			this.phiInRadian = Math.acos(cosPhi);
		}
		else {
			this.phiInRadian = 2 * Math.PI - Math.acos(cosPhi);
		}
		
		// 0: activePower
		if (powerType == 0) {
			if (cosPhi == 0 || power == 0) {
				throw new Exception("ERROR: Impossible to compute reactivePower and complexPower with activePower = 0 or cosPhi = 0! Setting activePower, reactivePower, complexPower and cosPhi to 0.");
//				this.activePower = 0;
//				this.reactivePower = 0;
//				this.complexPower = 0;
//				this.cosPhi = 0;
			}
			else {
				this.activePower = power;
				this.reactivePower = convertActiveToReactivePower(power, cosPhi, inductive); //???
				this.complexPower = convertActiveToComplexPower(power, cosPhi); //???
			}
		}
		// 1: reactivePower
		else if (powerType == 1) {
			this.activePower = convertReactiveToActivePower(power, cosPhi);
			if (!(activePower == activePower)) { //IMPORTANT: NaN!!!
				throw new Exception("ERROR: Impossible to compute activePower and complexPower with given input values! Setting activePower, reactivePower, complexPower and cosPhi to 0.");
//				this.activePower = 0;
//				this.reactivePower = 0;
//				this.complexPower = 0;
//				this.cosPhi = 0;
			}
			else {
				this.reactivePower = power; //???
				this.complexPower = convertReactiveToComplexPower(power, cosPhi); //???
			}
		}
		// 2: complexPower
		else if (powerType == 2) {
			power = checkComplexPower(power);
			
			this.activePower = convertComplexToActivePower(power, cosPhi);
			this.reactivePower = convertComplexToReactivePower(power, cosPhi, inductive); //???
			this.complexPower = power; //???
		}
		// else: activePower
		else {
			if (cosPhi == 0 || power == 0) {
				throw new Exception("ERROR: Impossible to compute reactivePower and complexPower with activePower = 0 or cosPhi = 0! Setting activePower, reactivePower, complexPower and cosPhi to 0.");
//				this.activePower = 0;
//				this.reactivePower = 0;
//				this.complexPower = 0;
//				this.cosPhi = 0;
			}
			else {
				this.activePower = power;
				this.reactivePower = convertActiveToReactivePower(power, cosPhi, inductive); //???
				this.complexPower = convertActiveToComplexPower(power, cosPhi); //???
			}
		}
	}
	
	/**
	 * Default: Power=activePower (needs always 3 parameters for full information)
	 * @param activePower ^= Real Power P in [W] (DE: Wirkleistung)
	 * @param cosPhi Active Factor cosPhi (DE: Wirkfaktor)
	 * @param inductive true: inductive, false: capacitive
	 * @throws Exception 
	 */
	public ComplexPowerUtil(double activePower, double cosPhi, boolean inductive) throws Exception {
		this(activePower, 0, cosPhi, inductive);
	}
	
	/**
	 * <b>Reactive Power only!</b><br>
	 * Power = reactivePower Q, cosPhi && Q != 0 (otherwise not computable)<br>
	 * activePower = 0 if cosPhi = 0<br>
	 * activePower = Double.POSITIVE_INFINITY if cosPhi = 1 & reactivePower != 0<br>
	 * activePower = Double.NEGATIVE_INFINITY if cosPhi = -1 & reactivePower != 0<br>
	 * activePower = Double.NaN if cosPhi = 1 & reactivePower == 0<br>
	 * activePower = Double.NaN if cosPhi = -1 & reactivePower == 0
	 * @param reactivePower Q in [VAR] (DE: Blindleistung)
	 * @param cosPhi Active Factor cosPhi (DE: Wirkfaktor)
	 * @throws Exception 
	 */
	public ComplexPowerUtil(double reactivePower, double cosPhi) throws Exception {
		cosPhi = checkCosPhi(cosPhi);
		this.cosPhi = cosPhi;
		
		if (reactivePower >= 0) {
			this.inductive = true;
			this.phiInRadian = Math.acos(cosPhi);
		}
		else {
			this.inductive = false;
			this.phiInRadian = 2 * Math.PI - Math.acos(cosPhi);
		}
		
		this.activePower = convertReactiveToActivePower(reactivePower, cosPhi);
		this.reactivePower = reactivePower; //???
		this.complexPower = convertReactiveToComplexPower(reactivePower, cosPhi); //???
	}
	
	/**
	 * Get the Active Power (DE: Wirkleistung)
	 * @return Active Power ^= Real Power P in [W] (DE: Wirkleistung)
	 */
	public double getActivePower() {
		return activePower;
	}
	
	/**
	 * Adjust activePower P, KEEP cosPhi, set whether the state (inductive/capacitive) should remain or change
	 * @param activePower ^= Real Power P in [W] (DE: Wirkleistung)
	 * @param keepInductiveState true: inductive, false: !inductive
	 * @throws Exception 
	 */
	public void adjustActivePower(double activePower, boolean keepInductiveState) throws Exception {
		this.activePower = activePower;
		if (keepInductiveState) {
			this.reactivePower = convertActiveToReactivePower(activePower, this.cosPhi, this.inductive); //???
		}
		else {
			this.reactivePower = convertActiveToReactivePower(activePower, this.cosPhi, !this.inductive); //???
		}
		this.complexPower = convertActiveToComplexPower(activePower, this.cosPhi); //???
	}
	
	/**
	 * Get the<br>
	 * EN: Active Factor cosPhi = P / S<br>
	 * DE: Wirkfaktor<br>
	 * ^= Power Factor (DE: Leistungsfaktor) in case of no harmonics (DE: Oberschwingungen)<br>
	 * (Note: Displacement Factor, DE: Verschiebungsfaktor, |P| / S = |cos(Phi)|)
	 * @return Active Factor cosPhi (DE: Wirkfaktor)
	 */
	public double getCosPhi() {
		return cosPhi;
	}
	
	/**
	 * Adjust cosPhi, KEEP the powerType set in powerTypeToKeep, set whether the state (inductive/capacitive) should remain or change
	 * @param cosPhi New Active Factor cosPhi (DE: Wirkfaktor)
	 * @param powerTypeToKeep 0: activePower 1: reactivePower 2: complexPower else: activePower
	 * @param keepInductiveState true: inductive, false: !inductive
	 * @throws Exception 
	 */
	public void adjustCosPhi(double cosPhi, int powerTypeToKeep, boolean keepInductiveState) throws Exception {
		cosPhi = checkCosPhi(cosPhi);
		this.cosPhi = cosPhi;
		
		if (!keepInductiveState) {
			this.inductive = !this.inductive;
		}
		
		if (powerTypeToKeep == 0) {
			if (cosPhi == 0) {
				if (this.activePower != 0) {
					throw new Exception("ERROR: Impossible to keep activePower with cosPhi = 0! Setting activePower to 0. Keeping complexPower. Adjusting reactivePower to complexPower.");
//					this.activePower = 0;
//					this.reactivePower = this.complexPower;
				}
				else {
					throw new Exception("ERROR: Impossible to keep activePower with cosPhi = 0! Keeping complexPower. Adjusting reactivePower to complexPower.");
//					this.reactivePower = this.complexPower;
				}
			}
			else {
				this.reactivePower = convertActiveToReactivePower(this.activePower, this.cosPhi, this.inductive);
				this.complexPower = convertActiveToComplexPower(this.activePower, this.cosPhi);
			}
		}
		else if (powerTypeToKeep == 1) {
			this.activePower = convertReactiveToActivePower(this.reactivePower, this.cosPhi);
			this.complexPower = convertReactiveToComplexPower(this.reactivePower, this.cosPhi);
		}
		else if (powerTypeToKeep == 2) {
			this.activePower = convertComplexToActivePower(this.complexPower, this.cosPhi);
			this.reactivePower = convertComplexToReactivePower(this.complexPower, this.cosPhi, this.inductive);
		}
		else {
			this.reactivePower = convertActiveToReactivePower(this.activePower, this.cosPhi, this.inductive);
			this.complexPower = convertActiveToComplexPower(this.activePower, this.cosPhi);
		}
	}
	
	/**
	 * Get the<br>
	 * EN: Phase difference angle phi in [rad]<br>
	 * DE: Phasenverschiebungswinkel
	 * @return Phase difference angle phi in [rad] (DE: Phasenverschiebungswinkel)
	 */
	public double getPhiRadian() {
		return phiInRadian;
	}
	
	/**
	 * @param phi
	 * @param powerTypeToKeep
	 * @throws Exception 
	 */
	public void adjustPhi(double phi, int powerTypeToKeep) throws Exception {
		phi = checkPhiRadian(phi);
		
		if (phi <= Math.PI) {
			if (this.inductive == true) {
				adjustCosPhi(Math.cos(phi), powerTypeToKeep, true);
			}
			else {
				adjustCosPhi(Math.cos(phi), powerTypeToKeep, false);
			}
		}
		else {
			if (this.inductive == false) {
				adjustCosPhi(Math.cos(phi), powerTypeToKeep, true);
			}
			else {
				adjustCosPhi(Math.cos(phi), powerTypeToKeep, false);
			}
		}
	}
	
	/**
	 * 
	 * @return Reactive power Q in [VAR] (DE: Blindleistung)
	 */
	public double getReactivePower() {
		return reactivePower;
	}
	
	/**
	 * 
	 * @return Complex power S in [VA] (DE: Scheinleistung)
	 */
	public double getComplexPower() {
		return complexPower;
	}
	
	/**
	 * Get the<br>
	 * EN: Phase difference angle phi in [degree]<br>
	 * DE: Phasenverschiebungswinkel
	 * @return Phase difference angle phi in [degree] (DE: Phasenverschiebungswinkel)
	 */
	public double getPhiDegrees() {
		return Math.toDegrees(getPhiRadian());
	}
	
	/**
	 * Get the Quadrant in Coordinate System
	 * @return returns: 1, 2, 3, 4, 12, 23, 34, 41 or 0
	 */
	public int getQuadrant() {
		if (activePower > 0 && reactivePower > 0) {
			return 1;
		}
		else if (activePower < 0 && reactivePower > 0) {
			return 2;
		}
		else if (activePower < 0 && reactivePower < 0) {
			return 3;
		}
		else if (activePower < 0 && reactivePower < 0) {
			return 4;
		}
		else if (activePower == 0 && reactivePower > 0) {
			return 12;
		}
		else if (activePower < 0 && reactivePower == 0) {
			return 23;
		}
		else if (activePower == 0 && reactivePower < 0) {
			return 34;
		}
		else if (activePower > 0 && reactivePower == 0) {
			return 41;
		}
		else {
			return 0;
		}
	}
	
	/**
	 * 
	 * @return true if Reactive Power > 0
	 */
	public boolean isInductive() {
		return (reactivePower > 0);
	}
	
	/**
	 * 
	 * @return true if Reactive Power < 0
	 */
	public boolean isCapacitive() {
		return (reactivePower < 0);
	}
	
	
	
	/**
	 * 
	 * @param activePower P (^= Real Power) in [W] (DE: Wirkleistung)
	 * @param cosPhi Active Factor: cosPhi = P / S (DE: Wirkfaktor)
	 * @return reactivePower Q in [VAR] (DE: Blindleistung)<br>
	 * @throws Exception 
	 */
	public static double convertActiveToReactivePower(double activePower, double cosPhi, boolean inductive) throws Exception {
		cosPhi = checkCosPhi(cosPhi);
		
		double reactivePower;
		
		if (cosPhi == 0) {
			throw new Exception("cosPhi=0 is not possible!");
		}
		else if (cosPhi == 1 || cosPhi == -1) {
			reactivePower = 0;
		}
		else {
			double complexPower = convertActiveToComplexPower(activePower, cosPhi);
			reactivePower = Math.sqrt((complexPower * complexPower) - (activePower * activePower));
			
			if (!inductive) {
				reactivePower = (-1) * reactivePower; // negative reactivePower
			}
		}
		
		return reactivePower;
	}
	
	/**
	 * 
	 * @param reactivePower Q in [VAR] (DE: Blindleistung)
	 * @param cosPhi Active Factor: cosPhi = P / S (DE: Wirkfaktor)
	 * @return activePower P (^= Real Power) in [W] (DE: Wirkleistung)<br>
	 * @throws Exception 
	 */
	public static double convertReactiveToActivePower (double reactivePower, double cosPhi) throws Exception {
		cosPhi = checkCosPhi(cosPhi);
		
		double activePower;
		
		if (cosPhi == 0) {
			activePower = 0;
		}
		else if (cosPhi == 1 && reactivePower != 0) {
			throw new Exception("ERROR: CosPhi = 1 and reactivePower != 0 is impossible!");
		}
		else if (cosPhi == -1 && reactivePower != 0) {
			throw new Exception("ERROR: CosPhi = -1 and reactivePower != 0 is impossible!");
		}
		else if (cosPhi == 1 && reactivePower == 0) {
			throw new Exception("ERROR: Impossible to calculate activePower from cosPhi = 1 and reactivePower == 0!");
		}
		else if (cosPhi == -1 && reactivePower == 0) {
			throw new Exception("ERROR: Impossible to calculate activePower from cosPhi = -1 and reactivePower == 0!");
		}
		else {
			double activePower2 = (reactivePower * reactivePower) / ((1 / (cosPhi * cosPhi)) - 1);
			activePower = Math.sqrt(activePower2);
			
			if (cosPhi < 0) {
				// Depends on when power is negative
				activePower = (-1) * activePower;
			}
		}
		
		return activePower;
	}
	
	/**
	 * 
	 * @param activePower P (^= Real Power) in [W] (DE: Wirkleistung)
	 * @param cosPhi Active Factor: cosPhi = P / S (DE: Wirkfaktor)
	 * @return complexPower S in [VA]: S^2 = P^2 + Q^2
	 * @throws Exception 
	 */
	public static double convertActiveToComplexPower(double activePower, double cosPhi) throws Exception {
		cosPhi = checkCosPhi(cosPhi);
		
		double complexPower;
		
		if (cosPhi != 0) {
			complexPower = activePower / cosPhi;
		}
		else {
			throw new Exception("ERROR: ComplexPower not computeable from activePower for cosPhi = 0! Returning Double.NaN.");
		}
		return complexPower;
	}
	
	/**
	 * 
	 * @param complexPower S in [VA]: S^2 = P^2 + Q^2
	 * @param cosPhi Active Factor: cosPhi = P / S (DE: Wirkfaktor)
	 * @return activePower P (^= Real Power) in [W] (DE: Wirkleistung)
	 * @throws Exception 
	 */
	public static double convertComplexToActivePower(double complexPower, double cosPhi) throws Exception {
		cosPhi = checkCosPhi(cosPhi);
		complexPower = checkComplexPower(complexPower);
		
		double activePower;
		activePower = complexPower * cosPhi;
		return activePower;
	}
	
	/**
	 * 
	 * @param reactivePower Q in [VAR] (DE: Blindleistung)
	 * @param cosPhi Active Factor: cosPhi = P / S (DE: Wirkfaktor)
	 * @return complexPower S in [VA]: S^2 = P^2 + Q^2<br>
	 * returns Double.NaN if activePower = NaN || POSITIVE_INFINITY || NEGATIVE_INFINITY
	 * @throws Exception 
	 */
	public static double convertReactiveToComplexPower(double reactivePower, double cosPhi) throws Exception {
		cosPhi = checkCosPhi(cosPhi);
		
		double activePower = convertReactiveToActivePower(reactivePower, cosPhi);
		double complexPower;
		
		if (!(activePower == activePower)) { //ACHTUNG: NaN!!!!
			complexPower = Double.NaN;
		}
		else {
			complexPower = convertActiveToComplexPower(activePower, cosPhi);
		}
		
		return complexPower;
	}
	
	/**
	 * 
	 * @param complexPower S in [VA]: S^2 = P^2 + Q^2
	 * @param cosPhi Active Factor: cosPhi = P / S (DE: Wirkfaktor)
	 * @return  reactivePower Q in [VAR] (DE: Blindleistung)
	 * @throws Exception 
	 */
	public static double convertComplexToReactivePower(double complexPower, double cosPhi, boolean inductive) throws Exception {
		complexPower = checkComplexPower(complexPower);
		cosPhi = checkCosPhi(cosPhi);
		
		double reactivePower;
		
		if (cosPhi == 0) {
			reactivePower = complexPower;
		}
		else {
			double activePower = convertComplexToActivePower(complexPower, cosPhi);
			reactivePower = convertActiveToReactivePower(activePower, cosPhi, inductive);
		}
		
		return reactivePower;
	}
	
	/**
	 * 
	 * @param activePower
	 * @param reactivePower
	 * @return
	 */
	public static double convertActiveAndReactivePowerToComplexPower(double activePower, double reactivePower) {
		return Math.sqrt(activePower * activePower + reactivePower * reactivePower);
	}
	
	/**
	 * 
	 * @param activePower
	 * @param reactivePower
	 * @return
	 * @throws Exception 
	 */
	public static double convertActiveAndReactivePowerToCosPhi(double activePower, double reactivePower) throws Exception {
		double complexPower = convertActiveAndReactivePowerToComplexPower(activePower, reactivePower);
		double cosPhi = activePower / complexPower;
		cosPhi = checkCosPhi(cosPhi);
		return cosPhi;
	}

	
	/**
	 * Check whether cosPhi is in [-1,1], otherwise set to border value
	 * @param cosPhi Active Factor: cosPhi = P / S (DE: Wirkfaktor)
	 * @return Corrected cosPhi
	 * @throws Exception 
	 */
	public static double checkCosPhi(double cosPhi) throws Exception {
		if (cosPhi > 1) {
			cosPhi = 1;
			throw new Exception("ERROR: CosPhi is NEVER > 1! Adjusted to 1.");
		}
		else if (cosPhi < -1) {
			cosPhi = -1;
			throw new Exception("ERROR: CosPhi is NEVER <-1! Adjusted to 1.");
		}
		return cosPhi;
	}
	
	/**
	 * Check whether phi could reduced
	 * @param phi
	 * @return
	 */
	public static double checkPhiRadian(double phiInRadian) {
		while (phiInRadian >= 2 * Math.PI) {
			phiInRadian = phiInRadian - 2 * Math.PI;
		}
		while (phiInRadian < 0) {
			phiInRadian = phiInRadian + 2 * Math.PI;
		}
		return phiInRadian;
	}

	
	/**
	 * Check whether complexPower is >= 0, otherwise multiply with (-1)
	 * @param complexPower S in [VA]: S^2 = P^2 + Q^2
	 * @return Corrected complexPower
	 * @throws Exception 
	 */
	public static double checkComplexPower(double complexPower) throws Exception {
		if (complexPower < 0) {
			complexPower = (-1) * complexPower;
			throw new Exception("ERROR: ComplexPower is NEVER <0! Multiplied with (-1).");
		}
		return complexPower;
	}
}
