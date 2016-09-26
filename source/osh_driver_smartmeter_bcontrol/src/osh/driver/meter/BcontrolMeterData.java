package osh.driver.meter;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class BcontrolMeterData {
	
	@XmlElement(name="authentication")
	private boolean authentication;
	
	@XmlElement(name="meter_count")
	private int meter_count;
	
	@XmlElement(name="sum_tariff")
	private double sum_tariff;
	
	@XmlElement(name="sum_energy")
	private double sum_energy;
	
	@XmlElement(name="sum_power")
	private double sum_power;
	
	@XmlElement(name="01_active_tariff")
	private double _01_active_tariff;
	
	@XmlElement(name="01_tariff_id")
	private double _01_tariff_id;
	
	@XmlElement(name="01_meter_number")
	private String _01_meter_number;
	
	@XmlElement(name="01_energy")
	private double _01_energy;

	@XmlElement(name="01_energy_tariff_1")
	private double _01_energy_tariff_1;
	
	@XmlElement(name="01_energy_tariff_2")
	private double _01_energy_tariff_2;
	
	@XmlElement(name="01_power")
	private double _01_power;
	
	@XmlElement(name="01_export_energy")
	private double _01_export_energy;

	@XmlElement(name="01_export_power")
	private double _01_export_power;
	
	@XmlElement(name="01_lastresponse")
	private long _01_lastresponse;
	
	@XmlElement(name="01_status")
	private int _01_status;
	
	@XmlElement(name="01_active_energy_a14_verification")
	private double _01_active_energy_a14_verification;
	
	@XmlElement(name="sum_import_power")
	private double sum_import_power;
	
	@XmlElement(name="sum_export_power")
	private double sum_export_power;
	
	@XmlElement(name="registers")
	private List<BcontrolMeterDataRegister> registers;
	
	@XmlElement(name="init_state")
	private int init_state;
	
	@XmlElement(name="time_state")
	private int time_state;
	
	@XmlElement(name="dlmsd_state")
	private int dlmsd_state;
	
	@XmlElement(name="meter_id")
	private int meter_id;
	
	
	@XmlElement(name="meter_types")
	private List<Integer> meter_types;
	
	@XmlElement(name="mum_status")
	private int mum_status;
	
	@XmlElement(name="mum_progress")
	private int mum_progress;
	
	@XmlElement(name="sys_time")
	private String sys_time;
	
	@XmlElement(name="version")
	private double version;
	
	@XmlElement(name="is_smartheater")
	private boolean is_smartheater;

	
	// GETTERS / SETTERS
	
	public boolean isAuthentication() {
		return authentication;
	}

	public void setAuthentication(boolean authentication) {
		this.authentication = authentication;
	}

	public int getMeter_count() {
		return meter_count;
	}

	public void setMeter_count(int meter_count) {
		this.meter_count = meter_count;
	}

	public double getSum_tariff() {
		return sum_tariff;
	}

	public void setSum_tariff(double sum_tariff) {
		this.sum_tariff = sum_tariff;
	}

	public double getSum_energy() {
		return sum_energy;
	}

	public void setSum_energy(double sum_energy) {
		this.sum_energy = sum_energy;
	}

	public double getSum_power() {
		return sum_power;
	}

	public void setSum_power(double sum_power) {
		this.sum_power = sum_power;
	}

	public double get_01_active_tariff() {
		return _01_active_tariff;
	}

	public void set_01_active_tariff(double _01_active_tariff) {
		this._01_active_tariff = _01_active_tariff;
	}

	public double get_01_tariff_id() {
		return _01_tariff_id;
	}

	public void set_01_tariff_id(double _01_tariff_id) {
		this._01_tariff_id = _01_tariff_id;
	}

	public String get_01_meter_number() {
		return _01_meter_number;
	}

	public void set_01_meter_number(String _01_meter_number) {
		this._01_meter_number = _01_meter_number;
	}

	public double get_01_energy() {
		return _01_energy;
	}

	public void set_01_energy(double _01_energy) {
		this._01_energy = _01_energy;
	}

	public double get_01_energy_tariff_1() {
		return _01_energy_tariff_1;
	}

	public void set_01_energy_tariff_1(double _01_energy_tariff_1) {
		this._01_energy_tariff_1 = _01_energy_tariff_1;
	}

	public double get_01_energy_tariff_2() {
		return _01_energy_tariff_2;
	}

	public void set_01_energy_tariff_2(double _01_energy_tariff_2) {
		this._01_energy_tariff_2 = _01_energy_tariff_2;
	}

	public double get_01_power() {
		return _01_power;
	}

	public void set_01_power(double _01_power) {
		this._01_power = _01_power;
	}

	public double get_01_export_energy() {
		return _01_export_energy;
	}

	public void set_01_export_energy(double _01_export_energy) {
		this._01_export_energy = _01_export_energy;
	}

	public double get_01_export_power() {
		return _01_export_power;
	}

	public void set_01_export_power(double _01_export_power) {
		this._01_export_power = _01_export_power;
	}

	public long get_01_lastresponse() {
		return _01_lastresponse;
	}

	public void set_01_lastresponse(long _01_lastresponse) {
		this._01_lastresponse = _01_lastresponse;
	}

	public int get_01_status() {
		return _01_status;
	}

	public void set_01_status(int _01_status) {
		this._01_status = _01_status;
	}

	public double get_01_active_energy_a14_verification() {
		return _01_active_energy_a14_verification;
	}

	public void set_01_active_energy_a14_verification(
			double _01_active_energy_a14_verification) {
		this._01_active_energy_a14_verification = _01_active_energy_a14_verification;
	}

	public double getSum_import_power() {
		return sum_import_power;
	}

	public void setSum_import_power(double sum_import_power) {
		this.sum_import_power = sum_import_power;
	}

	public double getSum_export_power() {
		return sum_export_power;
	}

	public void setSum_export_power(double sum_export_power) {
		this.sum_export_power = sum_export_power;
	}
	

	
	public List<BcontrolMeterDataRegister> getRegisters() {
		return registers;
	}

	public void setRegisters(List<BcontrolMeterDataRegister> registers) {
		this.registers = registers;
	}
	
	

	public int getInit_state() {
		return init_state;
	}

	public void setInit_state(int init_state) {
		this.init_state = init_state;
	}
	

	public int getTime_state() {
		return time_state;
	}

	public void setTime_state(int time_state) {
		this.time_state = time_state;
	}

	public int getDlmsd_state() {
		return dlmsd_state;
	}

	public void setDlmsd_state(int dlmsd_state) {
		this.dlmsd_state = dlmsd_state;
	}

	public int getMeter_id() {
		return meter_id;
	}

	public void setMeter_id(int meter_id) {
		this.meter_id = meter_id;
	}
	

	public List<Integer> getMeter_types() {
		return meter_types;
	}

	public void setMeter_types(List<Integer> meter_types) {
		this.meter_types = meter_types;
	}
	
	

	public int getMum_status() {
		return mum_status;
	}

	public void setMum_status(int mum_status) {
		this.mum_status = mum_status;
	}
	
	public int getMum_progress() {
		return mum_progress;
	}
	
	public void setMum_progress(int mum_progress) {
		this.mum_progress = mum_progress;
	}
	
	public String getSys_time() {
		return sys_time;
	}
	
	public void setSys_time(String sys_time) {
		this.sys_time = sys_time;
	}

	public double getVersion() {
		return version;
	}

	public void setVersion(double version) {
		this.version = version;
	}

	public boolean isIs_smartheater() {
		return is_smartheater;
	}

	public void setIs_smartheater(boolean is_smartheater) {
		this.is_smartheater = is_smartheater;
	}
	
	
}
