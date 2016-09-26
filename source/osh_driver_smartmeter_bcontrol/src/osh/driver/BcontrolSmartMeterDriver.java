package osh.driver;

import java.io.IOException;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.driver.details.metering.raw.BcontrolHeaterDriverRawLogDetails;
import osh.datatypes.registry.driver.details.metering.raw.BcontrolMeterDriverRawLogDetails;
import osh.driver.meter.BcontrolConnectorThread;
import osh.driver.meter.BcontrolHeaterData;
import osh.driver.meter.BcontrolMeterData;
import osh.eal.hal.HALDeviceDriver;
import osh.eal.hal.exceptions.HALException;
import osh.eal.hal.exchange.HALControllerExchange;
import osh.registry.interfaces.IHasState;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class BcontrolSmartMeterDriver extends HALDeviceDriver implements IHasState {
	
	private int phase = 1;
	private String meterURL = "http://DOMAIN.TLD";
	private int meterNumber = 14317111;
	private UUID heaterUUID = null;
	
	private BcontrolConnectorThread runnable;
	private Thread thread;
	
	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param deviceID
	 * @param driverConfig
	 * @throws OSHException 
	 * @throws HALException 
	 */
	public BcontrolSmartMeterDriver(IOSH controllerbox, UUID deviceID,
			OSHParameterCollection driverConfig) throws OSHException, HALException {
		super(controllerbox, deviceID, driverConfig);
		
		{
			String phase = driverConfig.getParameter("phase");
			if( phase == null ) {
				throw new OSHException("Need config parameter phase");
			}
			else {
				this.phase = Integer.valueOf(phase);
			}
		}
		{
			String meterURL = driverConfig.getParameter("meterurl");
			if( meterURL == null ) {
				throw new OSHException("Need config parameter meterurl");
			}
			else {
				this.meterURL = meterURL;
			}
		}
		{
			String meterNumber = driverConfig.getParameter("meternumber");
			if( meterNumber == null ) {
				throw new OSHException("Need config parameter meternumber");
			}
			else {
				this.meterNumber = Integer.valueOf(meterNumber);
			}
		}
		{
			String heateruuid = driverConfig.getParameter("heateruuid");
			if( heateruuid == null ) {
				// N/A
			}
			else {
				this.heaterUUID = UUID.fromString(heateruuid);
			}
		}
		
	}
	
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		getTimer().registerComponent(this, 1);
		
		this.runnable = new BcontrolConnectorThread(
				getGlobalLogger(), 
				getTimer(), 
				this, 
				meterURL, 
				meterNumber,
				heaterUUID);
		this.thread = new Thread(runnable);
		this.thread.start();
	}

	
	@Override
	public void onNextTimePeriod() throws OSHException {
		super.onNextTimePeriod();
		//NOTHING
	}
	
	@Override
	protected void onControllerRequest(HALControllerExchange controllerRequest)
			throws HALException {
		//NOTHING
	}
	
	// TODO change to new version of Jackson v2.x
	public void receiveMeterMessageFromMeter(String msg) {
		// parse to JSON and send to database
		
		// Setting up Jackson Tree Model with ObjectMapper
		ObjectMapper om = new ObjectMapper();
		AnnotationIntrospector introspectorJackson = new JacksonAnnotationIntrospector();

		om.setAnnotationIntrospector(introspectorJackson);
		
		// Retrieve JSON responses from controller and build Jackson Tree
		// Model with package de.fzi.iik.habiteq.jackson
		BcontrolMeterData bcmd = null;
		try {
			bcmd = om.readValue(msg, BcontrolMeterData.class);
		} 
		catch (JsonParseException e) {
			getGlobalLogger().logWarning(e.getStackTrace(), e);
		} 
		catch (JsonMappingException e) {
			getGlobalLogger().logWarning(e.getStackTrace(), e);
		} 
		catch (IOException e) {
			getGlobalLogger().logWarning(e.getStackTrace(), e);
		}
		
//		getGlobalLogger().logDebug(msg);
//		getGlobalLogger().logDebug(bcmd);
		
		// save to registry
		getDriverRegistry().setStateOfSender(
				BcontrolMeterDriverRawLogDetails.class, 
				convertJsonToRawDetails(bcmd));
	}
	
	
	private BcontrolMeterDriverRawLogDetails convertJsonToRawDetails(BcontrolMeterData bcmd) {
		// convert to raw details object for logging
		BcontrolMeterDriverRawLogDetails rawDetails = new BcontrolMeterDriverRawLogDetails(
				getDeviceID(), 
				getTimer().getUnixTime());
		
		rawDetails.setPhase(phase);
		
		rawDetails.setAuthentication(bcmd.isAuthentication());
		rawDetails.setMeter_count(bcmd.getMeter_count());
		rawDetails.setSum_tariff(bcmd.getSum_tariff());
		rawDetails.setSum_energy(bcmd.getSum_energy());
		rawDetails.setSum_power(bcmd.getSum_power());
		
		rawDetails.set_01_active_tariff(bcmd.get_01_active_tariff());
		rawDetails.set_01_tariff_id(bcmd.get_01_tariff_id());
		rawDetails.set_01_meter_number(bcmd.get_01_meter_number());
		
		rawDetails.set_01_energy(bcmd.get_01_energy());
		rawDetails.set_01_energy_tariff_1(bcmd.get_01_energy_tariff_1());
		rawDetails.set_01_energy_tariff_2(bcmd.get_01_energy_tariff_2());
		rawDetails.set_01_power(bcmd.get_01_power());
		
		rawDetails.set_01_export_energy(bcmd.get_01_export_energy());
		rawDetails.set_01_export_power(bcmd.get_01_export_power());
		rawDetails.set_01_lastresponse(bcmd.get_01_lastresponse());
		rawDetails.set_01_status(bcmd.get_01_status());
		rawDetails.set_01_active_energy_a14_verification(bcmd.get_01_active_energy_a14_verification());
		
		rawDetails.setSum_import_power(bcmd.getSum_import_power());
		rawDetails.setSum_export_power(bcmd.getSum_export_power());
		
		rawDetails.setInit_state(bcmd.getInit_state());
		rawDetails.setTime_state(bcmd.getTime_state());
		rawDetails.setDlmsd_state(bcmd.getDlmsd_state());
		rawDetails.setMeter_id(bcmd.getMeter_id());
		
		rawDetails.setMum_status(bcmd.getMum_status());
		rawDetails.setMum_progress(bcmd.getMum_progress());
		rawDetails.setSys_time(bcmd.getSys_time());
		rawDetails.setVersion(bcmd.getVersion());
		rawDetails.setIs_smartheater(bcmd.isIs_smartheater());
		
		return rawDetails;
	}
	
	
	public void receiveHeaterMessageFromMeter(String msg) throws Exception {
		// parse to JSON and send to database
		
		// Setting up Jackson Tree Model with ObjectMapper
		ObjectMapper om = new ObjectMapper();
		AnnotationIntrospector introspectorJackson = new JacksonAnnotationIntrospector();

		om.setAnnotationIntrospector(introspectorJackson);

		// Retrieve JSON responses from controller and build Jackson Tree
		// Model with package de.fzi.iik.habiteq.jackson
		BcontrolHeaterData bchd = null;
//		try {
			bchd = om.readValue(msg, BcontrolHeaterData.class);
//		} 
//		catch (JsonParseException e) {
//			getGlobalLogger().logWarning(e.getStackTrace(), e);
//		} 
//		catch (JsonMappingException e) {
//			getGlobalLogger().logWarning(e.getStackTrace(), e);
//		} 
//		catch (IOException e) {
//			getGlobalLogger().logWarning(e.getStackTrace(), e);
//		}
		
//		getGlobalLogger().logDebug(msg);
//		getGlobalLogger().logDebug(bcmd);
		
		// save to registry
		getDriverRegistry().setStateOfSender(
				BcontrolHeaterDriverRawLogDetails.class, 
				convertJsonToRawDetails(bchd));
	}
	
	private BcontrolHeaterDriverRawLogDetails convertJsonToRawDetails(BcontrolHeaterData bcmd) {
		BcontrolHeaterDriverRawLogDetails rawDetails = new BcontrolHeaterDriverRawLogDetails(
				getDeviceID(), 
				getTimer().getUnixTime());
		
		rawDetails.setMode(bcmd.getMode());
		rawDetails.setOrder(bcmd.getOrder());
		rawDetails.setMeter_timestamp(bcmd.getTimestamp());
		rawDetails.setLabel(bcmd.getLabel());
		rawDetails.setSerial(bcmd.getSerial());
		rawDetails.setUuid(bcmd.getUuid());
		rawDetails.setChannel(bcmd.getChannel());
		rawDetails.setManufacturer(bcmd.getManufacturer());
		rawDetails.setProduct(bcmd.getProduct());
		rawDetails.setState(bcmd.getState());
		rawDetails.setSw_version(bcmd.getSw_version());
		rawDetails.setHw_version(bcmd.getHw_version());
		rawDetails.setProduction_date(bcmd.getProduction_date());
		rawDetails.setVendor(bcmd.getVendor());
		rawDetails.setModel(bcmd.getModel());
		rawDetails.setOperating_hours(bcmd.getOperating_hours());
		rawDetails.setOperating_seconds(bcmd.getOperating_seconds());
		rawDetails.setTemperatur_boiler(bcmd.getTemperatur_boiler());
		rawDetails.setUser_temperatur_nominal(bcmd.getUser_temperatur_nominal());
		
		rawDetails.setSwitches_500_power(
				bcmd.getSwitches().get(0).getPower());
		rawDetails.setSwitches_500_operating_seconds(
				bcmd.getSwitches().get(0).getOperating_seconds());
		rawDetails.setSwitches_500_switching_cycles(
				bcmd.getSwitches().get(0).getSwitching_cycles());
		rawDetails.setSwitches_500_min_on_time(
				bcmd.getSwitches().get(0).getMin_on_time());
		rawDetails.setSwitches_500_min_off_time(
				bcmd.getSwitches().get(0).getMin_off_time());
		
		rawDetails.setSwitches_1000_power(
				bcmd.getSwitches().get(1).getPower());
		rawDetails.setSwitches_1000_operating_seconds(
				bcmd.getSwitches().get(1).getOperating_seconds());
		rawDetails.setSwitches_1000_switching_cycles(
				bcmd.getSwitches().get(1).getSwitching_cycles());
		rawDetails.setSwitches_1000_min_on_time(
				bcmd.getSwitches().get(1).getMin_on_time());
		rawDetails.setSwitches_1000_min_off_time(
				bcmd.getSwitches().get(1).getMin_off_time());
		
		rawDetails.setSwitches_2000_power(
				bcmd.getSwitches().get(2).getPower());
		rawDetails.setSwitches_2000_operating_seconds(
				bcmd.getSwitches().get(2).getOperating_seconds());
		rawDetails.setSwitches_2000_switching_cycles(
				bcmd.getSwitches().get(2).getSwitching_cycles());
		rawDetails.setSwitches_2000_min_on_time(
				bcmd.getSwitches().get(2).getMin_on_time());
		rawDetails.setSwitches_2000_min_off_time(
				bcmd.getSwitches().get(2).getMin_off_time());
		
		rawDetails.setErrors_id_1(
				bcmd.getErrors().get(0).getId());
		rawDetails.setErrors_timestamp_1(
				bcmd.getErrors().get(0).getTimestamp());
		rawDetails.setErrors_code_1(
				bcmd.getErrors().get(0).getCode());
		
		rawDetails.setErrors_id_2(
				bcmd.getErrors().get(1).getId());
		rawDetails.setErrors_timestamp_2(
				bcmd.getErrors().get(1).getTimestamp());
		rawDetails.setErrors_code_2(
				bcmd.getErrors().get(1).getCode());
		
		rawDetails.setErrors_id_3(
				bcmd.getErrors().get(2).getId());
		rawDetails.setErrors_timestamp_3(
				bcmd.getErrors().get(2).getTimestamp());
		rawDetails.setErrors_code_3(
				bcmd.getErrors().get(2).getCode());
		
		rawDetails.setErrors_id_4(
				bcmd.getErrors().get(3).getId());
		rawDetails.setErrors_timestamp_4(
				bcmd.getErrors().get(3).getTimestamp());
		rawDetails.setErrors_code_4(
				bcmd.getErrors().get(3).getCode());
		
		rawDetails.setErrors_id_5(
				bcmd.getErrors().get(4).getId());
		rawDetails.setErrors_timestamp_5(
				bcmd.getErrors().get(4).getTimestamp());
		rawDetails.setErrors_code_5(
				bcmd.getErrors().get(4).getCode());
		
		rawDetails.setErrors_id_6(
				bcmd.getErrors().get(5).getId());
		rawDetails.setErrors_timestamp_6(
				bcmd.getErrors().get(5).getTimestamp());
		rawDetails.setErrors_code_6(
				bcmd.getErrors().get(5).getCode());
		
		rawDetails.setErrors_id_7(
				bcmd.getErrors().get(6).getId());
		rawDetails.setErrors_timestamp_7(
				bcmd.getErrors().get(6).getTimestamp());
		rawDetails.setErrors_code_7(
				bcmd.getErrors().get(6).getCode());
		
		rawDetails.setErrors_id_8(
				bcmd.getErrors().get(7).getId());
		rawDetails.setErrors_timestamp_8(
				bcmd.getErrors().get(7).getTimestamp());
		rawDetails.setErrors_code_8(
				bcmd.getErrors().get(7).getCode());
		
		rawDetails.setErrors_id_9(
				bcmd.getErrors().get(8).getId());
		rawDetails.setErrors_timestamp_9(
				bcmd.getErrors().get(8).getTimestamp());
		rawDetails.setErrors_code_9(
				bcmd.getErrors().get(8).getCode());
		
		rawDetails.setErrors_id_10(
				bcmd.getErrors().get(9).getId());
		rawDetails.setErrors_timestamp_10(
				bcmd.getErrors().get(9).getTimestamp());
		rawDetails.setErrors_code_10(
				bcmd.getErrors().get(9).getCode());
		
		rawDetails.setRegisters_register_0(bcmd.getRegisters().get(0).getRegister());
		rawDetails.setRegisters_value_0(bcmd.getRegisters().get(0).getValue());
		rawDetails.setRegisters_register_1(bcmd.getRegisters().get(1).getRegister());
		rawDetails.setRegisters_value_1(bcmd.getRegisters().get(1).getValue());
		
		rawDetails.setUpdate_status(bcmd.getUpdate().getStatus());
		rawDetails.setUpdate_progress(bcmd.getUpdate().getProgress());
		
		return rawDetails;
	}
	
	
	@Override
	public void onSystemShutdown() throws OSHException {
		super.onSystemShutdown();
		this.runnable.shutdown();
	}
	
	@Override
	public UUID getUUID() {
		return getDeviceID();
	}

}
