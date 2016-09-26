package osh.datatypes.registry.driver.details.metering.raw;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
@Table(name="log_raw_heater_bcontroldriver")
public class BcontrolHeaterDriverRawLogDetails extends StateExchange {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6106877694895839658L;

	@Column(name="c_mode")
	protected String mode;
	
	@Column(name="c_order")
	protected int order;
	
	@Column(name="c_meter_timestamp")
	protected String meter_timestamp;
	
	@Column(name="c_label")
	protected String label;
	
	@Column(name="c_serial")
	protected String serial;
	
	@Column(name="c_uuid")
	protected String uuid;
	
	@Column(name="c_channel")
	protected int channel;
	
	@Column(name="c_manufacturer")
	protected String manufacturer;
	
	@Column(name="c_product")
	protected String product;
	
	@Column(name="c_state")
	protected String state;

	@Column(name="c_sw_version")
	protected String sw_version;
	
	@Column(name="c_hw_version")
	protected String hw_version;
	
	@Column(name="c_production_date")
	protected String production_date;
	
	@Column(name="c_vendor")
	protected String vendor;
	
	@Column(name="c_model")
	protected String model;
	
	@Column(name="c_operating_hours")
	protected int operating_hours;
	
	@Column(name="c_operating_seconds")
	protected int operating_seconds;
	
	@Column(name="c_temperatur_boiler")
	protected int temperatur_boiler;
	
	@Column(name="c_user_temperatur_nominal")
	protected int user_temperatur_nominal;
	
	
	@XmlElement(name="c_switches_500_power")
	private int switches_500_power;
	
	@XmlElement(name="c_switches_500_operating_seconds")
	private int switches_500_operating_seconds;

	@XmlElement(name="c_switches_500_switching_cycles")
	private int switches_500_switching_cycles;
	
	@XmlElement(name="c_switches_500_min_on_time")
	private int switches_500_min_on_time;
	
	@XmlElement(name="c_switches_500_min_off_time")
	private int switches_500_min_off_time;
	
	@XmlElement(name="c_switches_500_state")
	private String switches_500_state;
	
	@XmlElement(name="c_switches_1000_power")
	private int switches_1000_power;
	
	@XmlElement(name="c_switches_1000_operating_seconds")
	private int switches_1000_operating_seconds;

	@XmlElement(name="c_switches_1000_switching_cycles")
	private int switches_1000_switching_cycles;
	
	@XmlElement(name="c_switches_1000_min_on_time")
	private int switches_1000_min_on_time;
	
	@XmlElement(name="c_switches_1000_min_off_time")
	private int switches_1000_min_off_time;
	
	@XmlElement(name="c_switches_1000_state")
	private String switches_1000_state;
	
	@XmlElement(name="c_switches_2000_power")
	private int switches_2000_power;
	
	@XmlElement(name="c_switches_2000_operating_seconds")
	private int switches_2000_operating_seconds;

	@XmlElement(name="c_switches_2000_switching_cycles")
	private int switches_2000_switching_cycles;
	
	@XmlElement(name="c_switches_2000_min_on_time")
	private int switches_2000_min_on_time;
	
	@XmlElement(name="c_switches_2000_min_off_time")
	private int switches_2000_min_off_time;
	
	@XmlElement(name="c_switches_2000_state")
	private String switches_2000_state;
	

	@XmlElement(name="c_errors_id_1")
	private int errors_id_1;
	
	@XmlElement(name="c_errors_timestamp_1")
	private double errors_timestamp_1;
	
	@XmlElement(name="code_1")
	private int errors_code_1;
	
	@XmlElement(name="c_errors_id_2")
	private int errors_id_2;
	
	@XmlElement(name="c_errors_timestamp_2")
	private double errors_timestamp_2;
	
	@XmlElement(name="code_2")
	private int errors_code_2;
	
	@XmlElement(name="c_errors_id_3")
	private int errors_id_3;
	
	@XmlElement(name="c_errors_timestamp_3")
	private double errors_timestamp_3;
	
	@XmlElement(name="code_3")
	private int errors_code_3;
	
	@XmlElement(name="c_errors_id_4")
	private int errors_id_4;
	
	@XmlElement(name="c_errors_timestamp_4")
	private double errors_timestamp_4;
	
	@XmlElement(name="code_4")
	private int errors_code_4;
	
	@XmlElement(name="c_errors_id_5")
	private int errors_id_5;
	
	@XmlElement(name="c_errors_timestamp_5")
	private double errors_timestamp_5;
	
	@XmlElement(name="code_5")
	private int errors_code_5;
	
	@XmlElement(name="c_errors_id_6")
	private int errors_id_6;
	
	@XmlElement(name="c_errors_timestamp_6")
	private double errors_timestamp_6;
	
	@XmlElement(name="code_6")
	private int errors_code_6;
	
	@XmlElement(name="c_errors_id_7")
	private int errors_id_7;
	
	@XmlElement(name="c_errors_timestamp_7")
	private double errors_timestamp_7;
	
	@XmlElement(name="code_7")
	private int errors_code_7;
	
	@XmlElement(name="c_errors_id_8")
	private int errors_id_8;
	
	@XmlElement(name="c_errors_timestamp_8")
	private double errors_timestamp_8;
	
	@XmlElement(name="code_8")
	private int errors_code_8;
	
	@XmlElement(name="c_errors_id_9")
	private int errors_id_9;
	
	@XmlElement(name="c_errors_timestamp_9")
	private double errors_timestamp_9;
	
	@XmlElement(name="code_9")
	private int errors_code_9;
	
	@XmlElement(name="c_errors_id_10")
	private int errors_id_10;
	
	@XmlElement(name="c_errors_timestamp_10")
	private double errors_timestamp_10;
	
	@XmlElement(name="code_10")
	private int errors_code_10;
	
	
	@XmlElement(name="c_registers_register_0")
	private String registers_register_0;
	
	@XmlElement(name="c_registers_value_0")
	private double registers_value_0;
	
	@XmlElement(name="c_registers_register_1")
	private String registers_register_1;
	
	@XmlElement(name="c_registers_value_1")
	private double registers_value_1;
	
	
	@XmlElement(name="c_update_status")
	private String update_status;
	
	@XmlElement(name="c_update_progress")
	private double update_progress;
	
	
	/** for JAXB */
	@Deprecated
	public BcontrolHeaterDriverRawLogDetails() {
		super(null, 0L);
	}
	
	/**
	 * CONSTRUCTOR
	 * @param sender
	 * @param timestamp
	 */
	public BcontrolHeaterDriverRawLogDetails(UUID sender, long timestamp) {
		super(sender, timestamp);
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getMeter_timestamp() {
		return meter_timestamp;
	}

	public void setMeter_timestamp(String meter_timestamp) {
		this.meter_timestamp = meter_timestamp;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getSw_version() {
		return sw_version;
	}

	public void setSw_version(String sw_version) {
		this.sw_version = sw_version;
	}

	public String getHw_version() {
		return hw_version;
	}

	public void setHw_version(String hw_version) {
		this.hw_version = hw_version;
	}

	public String getProduction_date() {
		return production_date;
	}

	public void setProduction_date(String production_date) {
		this.production_date = production_date;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public int getOperating_hours() {
		return operating_hours;
	}

	public void setOperating_hours(int operating_hours) {
		this.operating_hours = operating_hours;
	}

	public int getOperating_seconds() {
		return operating_seconds;
	}

	public void setOperating_seconds(int operating_seconds) {
		this.operating_seconds = operating_seconds;
	}

	public int getTemperatur_boiler() {
		return temperatur_boiler;
	}

	public void setTemperatur_boiler(int temperatur_boiler) {
		this.temperatur_boiler = temperatur_boiler;
	}

	public int getUser_temperatur_nominal() {
		return user_temperatur_nominal;
	}

	public void setUser_temperatur_nominal(int user_temperatur_nominal) {
		this.user_temperatur_nominal = user_temperatur_nominal;
	}

	public int getSwitches_500_power() {
		return switches_500_power;
	}

	public void setSwitches_500_power(int switches_500_power) {
		this.switches_500_power = switches_500_power;
	}

	public int getSwitches_500_operating_seconds() {
		return switches_500_operating_seconds;
	}

	public void setSwitches_500_operating_seconds(int switches_500_operating_seconds) {
		this.switches_500_operating_seconds = switches_500_operating_seconds;
	}

	public int getSwitches_500_switching_cycles() {
		return switches_500_switching_cycles;
	}

	public void setSwitches_500_switching_cycles(int switches_500_switching_cycles) {
		this.switches_500_switching_cycles = switches_500_switching_cycles;
	}

	public int getSwitches_500_min_on_time() {
		return switches_500_min_on_time;
	}

	public void setSwitches_500_min_on_time(int switches_500_min_on_time) {
		this.switches_500_min_on_time = switches_500_min_on_time;
	}

	public int getSwitches_500_min_off_time() {
		return switches_500_min_off_time;
	}

	public void setSwitches_500_min_off_time(int switches_500_min_off_time) {
		this.switches_500_min_off_time = switches_500_min_off_time;
	}

	public String getSwitches_500_state() {
		return switches_500_state;
	}

	public void setSwitches_500_state(String switches_500_state) {
		this.switches_500_state = switches_500_state;
	}

	public int getSwitches_1000_power() {
		return switches_1000_power;
	}

	public void setSwitches_1000_power(int switches_1000_power) {
		this.switches_1000_power = switches_1000_power;
	}

	public int getSwitches_1000_operating_seconds() {
		return switches_1000_operating_seconds;
	}

	public void setSwitches_1000_operating_seconds(
			int switches_1000_operating_seconds) {
		this.switches_1000_operating_seconds = switches_1000_operating_seconds;
	}

	public int getSwitches_1000_switching_cycles() {
		return switches_1000_switching_cycles;
	}

	public void setSwitches_1000_switching_cycles(int switches_1000_switching_cycles) {
		this.switches_1000_switching_cycles = switches_1000_switching_cycles;
	}

	public int getSwitches_1000_min_on_time() {
		return switches_1000_min_on_time;
	}

	public void setSwitches_1000_min_on_time(int switches_1000_min_on_time) {
		this.switches_1000_min_on_time = switches_1000_min_on_time;
	}

	public int getSwitches_1000_min_off_time() {
		return switches_1000_min_off_time;
	}

	public void setSwitches_1000_min_off_time(int switches_1000_min_off_time) {
		this.switches_1000_min_off_time = switches_1000_min_off_time;
	}

	public String getSwitches_1000_state() {
		return switches_1000_state;
	}

	public void setSwitches_1000_state(String switches_1000_state) {
		this.switches_1000_state = switches_1000_state;
	}

	public int getSwitches_2000_power() {
		return switches_2000_power;
	}

	public void setSwitches_2000_power(int switches_2000_power) {
		this.switches_2000_power = switches_2000_power;
	}

	public int getSwitches_2000_operating_seconds() {
		return switches_2000_operating_seconds;
	}

	public void setSwitches_2000_operating_seconds(
			int switches_2000_operating_seconds) {
		this.switches_2000_operating_seconds = switches_2000_operating_seconds;
	}

	public int getSwitches_2000_switching_cycles() {
		return switches_2000_switching_cycles;
	}

	public void setSwitches_2000_switching_cycles(int switches_2000_switching_cycles) {
		this.switches_2000_switching_cycles = switches_2000_switching_cycles;
	}

	public int getSwitches_2000_min_on_time() {
		return switches_2000_min_on_time;
	}

	public void setSwitches_2000_min_on_time(int switches_2000_min_on_time) {
		this.switches_2000_min_on_time = switches_2000_min_on_time;
	}

	public int getSwitches_2000_min_off_time() {
		return switches_2000_min_off_time;
	}

	public void setSwitches_2000_min_off_time(int switches_2000_min_off_time) {
		this.switches_2000_min_off_time = switches_2000_min_off_time;
	}

	public String getSwitches_2000_state() {
		return switches_2000_state;
	}

	public void setSwitches_2000_state(String switches_2000_state) {
		this.switches_2000_state = switches_2000_state;
	}

	public int getErrors_id_1() {
		return errors_id_1;
	}

	public void setErrors_id_1(int errors_id_1) {
		this.errors_id_1 = errors_id_1;
	}

	public double getErrors_timestamp_1() {
		return errors_timestamp_1;
	}

	public void setErrors_timestamp_1(double errors_timestamp_1) {
		this.errors_timestamp_1 = errors_timestamp_1;
	}

	public int getErrors_code_1() {
		return errors_code_1;
	}

	public void setErrors_code_1(int errors_code_1) {
		this.errors_code_1 = errors_code_1;
	}

	public int getErrors_id_2() {
		return errors_id_2;
	}

	public void setErrors_id_2(int errors_id_2) {
		this.errors_id_2 = errors_id_2;
	}

	public double getErrors_timestamp_2() {
		return errors_timestamp_2;
	}

	public void setErrors_timestamp_2(double errors_timestamp_2) {
		this.errors_timestamp_2 = errors_timestamp_2;
	}

	public int getErrors_code_2() {
		return errors_code_2;
	}

	public void setErrors_code_2(int errors_code_2) {
		this.errors_code_2 = errors_code_2;
	}

	public int getErrors_id_3() {
		return errors_id_3;
	}

	public void setErrors_id_3(int errors_id_3) {
		this.errors_id_3 = errors_id_3;
	}

	public double getErrors_timestamp_3() {
		return errors_timestamp_3;
	}

	public void setErrors_timestamp_3(double errors_timestamp_3) {
		this.errors_timestamp_3 = errors_timestamp_3;
	}

	public int getErrors_code_3() {
		return errors_code_3;
	}

	public void setErrors_code_3(int errors_code_3) {
		this.errors_code_3 = errors_code_3;
	}

	public int getErrors_id_4() {
		return errors_id_4;
	}

	public void setErrors_id_4(int errors_id_4) {
		this.errors_id_4 = errors_id_4;
	}

	public double getErrors_timestamp_4() {
		return errors_timestamp_4;
	}

	public void setErrors_timestamp_4(double errors_timestamp_4) {
		this.errors_timestamp_4 = errors_timestamp_4;
	}

	public int getErrors_code_4() {
		return errors_code_4;
	}

	public void setErrors_code_4(int errors_code_4) {
		this.errors_code_4 = errors_code_4;
	}

	public int getErrors_id_5() {
		return errors_id_5;
	}

	public void setErrors_id_5(int errors_id_5) {
		this.errors_id_5 = errors_id_5;
	}

	public double getErrors_timestamp_5() {
		return errors_timestamp_5;
	}

	public void setErrors_timestamp_5(double errors_timestamp_5) {
		this.errors_timestamp_5 = errors_timestamp_5;
	}

	public int getErrors_code_5() {
		return errors_code_5;
	}

	public void setErrors_code_5(int errors_code_5) {
		this.errors_code_5 = errors_code_5;
	}

	public int getErrors_id_6() {
		return errors_id_6;
	}

	public void setErrors_id_6(int errors_id_6) {
		this.errors_id_6 = errors_id_6;
	}

	public double getErrors_timestamp_6() {
		return errors_timestamp_6;
	}

	public void setErrors_timestamp_6(double errors_timestamp_6) {
		this.errors_timestamp_6 = errors_timestamp_6;
	}

	public int getErrors_code_6() {
		return errors_code_6;
	}

	public void setErrors_code_6(int errors_code_6) {
		this.errors_code_6 = errors_code_6;
	}

	public int getErrors_id_7() {
		return errors_id_7;
	}

	public void setErrors_id_7(int errors_id_7) {
		this.errors_id_7 = errors_id_7;
	}

	public double getErrors_timestamp_7() {
		return errors_timestamp_7;
	}

	public void setErrors_timestamp_7(double errors_timestamp_7) {
		this.errors_timestamp_7 = errors_timestamp_7;
	}

	public int getErrors_code_7() {
		return errors_code_7;
	}

	public void setErrors_code_7(int errors_code_7) {
		this.errors_code_7 = errors_code_7;
	}

	public int getErrors_id_8() {
		return errors_id_8;
	}

	public void setErrors_id_8(int errors_id_8) {
		this.errors_id_8 = errors_id_8;
	}

	public double getErrors_timestamp_8() {
		return errors_timestamp_8;
	}

	public void setErrors_timestamp_8(double errors_timestamp_8) {
		this.errors_timestamp_8 = errors_timestamp_8;
	}

	public int getErrors_code_8() {
		return errors_code_8;
	}

	public void setErrors_code_8(int errors_code_8) {
		this.errors_code_8 = errors_code_8;
	}

	public int getErrors_id_9() {
		return errors_id_9;
	}

	public void setErrors_id_9(int errors_id_9) {
		this.errors_id_9 = errors_id_9;
	}

	public double getErrors_timestamp_9() {
		return errors_timestamp_9;
	}

	public void setErrors_timestamp_9(double errors_timestamp_9) {
		this.errors_timestamp_9 = errors_timestamp_9;
	}

	public int getErrors_code_9() {
		return errors_code_9;
	}

	public void setErrors_code_9(int errors_code_9) {
		this.errors_code_9 = errors_code_9;
	}

	public int getErrors_id_10() {
		return errors_id_10;
	}

	public void setErrors_id_10(int errors_id_10) {
		this.errors_id_10 = errors_id_10;
	}

	public double getErrors_timestamp_10() {
		return errors_timestamp_10;
	}

	public void setErrors_timestamp_10(double errors_timestamp_10) {
		this.errors_timestamp_10 = errors_timestamp_10;
	}

	public int getErrors_code_10() {
		return errors_code_10;
	}

	public void setErrors_code_10(int errors_code_10) {
		this.errors_code_10 = errors_code_10;
	}

	public String getRegisters_register_0() {
		return registers_register_0;
	}

	public void setRegisters_register_0(String registers_register_0) {
		this.registers_register_0 = registers_register_0;
	}

	public double getRegisters_value_0() {
		return registers_value_0;
	}

	public void setRegisters_value_0(double registers_value_0) {
		this.registers_value_0 = registers_value_0;
	}

	public String getRegisters_register_1() {
		return registers_register_1;
	}

	public void setRegisters_register_1(String registers_register_1) {
		this.registers_register_1 = registers_register_1;
	}

	public double getRegisters_value_1() {
		return registers_value_1;
	}

	public void setRegisters_value_1(double registers_value_1) {
		this.registers_value_1 = registers_value_1;
	}

	public String getUpdate_status() {
		return update_status;
	}

	public void setUpdate_status(String update_status) {
		this.update_status = update_status;
	}

	public double getUpdate_progress() {
		return update_progress;
	}

	public void setUpdate_progress(double update_progress) {
		this.update_progress = update_progress;
	}

	
}