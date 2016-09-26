package osh.datatypes.registry.driver.details.metering.raw;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
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
@XmlRootElement(name="BControlSmartMeterDriverRawDetails")
@XmlType
@Entity
@Table(name="log_raw_bcontroldriver")
public class BcontrolMeterDriverRawLogDetails extends StateExchange {
	
	/** SERIAL */
	private static final long serialVersionUID = -4425534285325432789L;

	@Column(name="c_phase")
	protected int phase;
	
	@Column(name="c_authentication")
	protected boolean authentication;
	
	@Column(name="c_meter_count")
	protected int meter_count;
	
	@Column(name="c_sum_tariff")
	protected double sum_tariff;
	
	@Column(name="c_sum_energy")
	protected double sum_energy;
	
	@Column(name="c_sum_power")
	protected double sum_power;
	
	@Column(name="c_01_active_tariff")
	protected double _01_active_tariff;
	
	@Column(name="c_01_tariff_id")
	protected double _01_tariff_id;
	
	@Column(name="c_01_meter_number")
	protected String _01_meter_number;
	
	@Column(name="c_01_energy")
	protected double _01_energy;

	@Column(name="c_01_energy_tariff_1")
	protected double _01_energy_tariff_1;
	
	@Column(name="c_01_energy_tariff_2")
	protected double _01_energy_tariff_2;
	
	@Column(name="c_01_power")
	protected double _01_power;
	
	@Column(name="c_01_export_energy")
	protected double _01_export_energy;

	@Column(name="c_01_export_power")
	protected double _01_export_power;
	
	@Column(name="c_01_lastresponse")
	protected long _01_lastresponse;
	
	@Column(name="c_01_status")
	protected int _01_status;
	
	@Column(name="c_01_active_energy_a14_verification")
	protected double _01_active_energy_a14_verification;
	
	@Column(name="c_sum_import_power")
	protected double sum_import_power;
	
	@Column(name="c_sum_export_power")
	protected double sum_export_power;
	
	@Column(name="c_init_state")
	protected int init_state;
	
	@Column(name="c_time_state")
	protected int time_state;
	
	@Column(name="c_dlmsd_state")
	protected int dlmsd_state;
	
	@Column(name="c_meter_id")
	protected int meter_id;
	
	@Column(name="c_mum_status")
	protected int mum_status;
	
	@Column(name="c_mum_progress")
	protected int mum_progress;
	
	@Column(name="c_sys_time")
	protected String sys_time;
	
	@Column(name="c_version")
	protected double version;
	
	@Column(name="c_is_smartheater")
	protected boolean is_smartheater;
	

	/** for JAXB */
	@Deprecated
	public BcontrolMeterDriverRawLogDetails() {
		super(null, 0L);
	}
	
	/**
	 * CONSTRUCTOR
	 * @param sender
	 * @param timestamp
	 */
	public BcontrolMeterDriverRawLogDetails(UUID sender, long timestamp) {
		super(sender, timestamp);
	}
	
	
	
	public int getPhase() {
		return phase;
	}

	public void setPhase(int phase) {
		this.phase = phase;
	}
	
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
