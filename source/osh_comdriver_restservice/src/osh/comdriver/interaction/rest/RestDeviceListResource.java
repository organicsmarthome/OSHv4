package osh.comdriver.interaction.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import osh.comdriver.HttpRestInteractionProviderBusDriver;
import osh.comdriver.interaction.datatypes.RestDevice;
import osh.comdriver.interaction.datatypes.RestDeviceList;
import osh.comdriver.interaction.datatypes.RestDeviceMetaDetails;
import osh.comdriver.interaction.datatypes.RestSwitchCommand;
import osh.mgmt.commanager.HttpRestInteractionBusManager;

/**
 * 
 * @author Kaibin Bao, Ingo Mauser
 * 
 */
@Path("/")
public class RestDeviceListResource {

	@SuppressWarnings("unused")
	private HttpRestInteractionBusManager interactionComManager;
	private HttpRestInteractionProviderBusDriver interactionDriver;

	/**
	 * CONSTRUCTOR
	 * 
	 * @param comMgr
	 * @param driver
	 */
	public RestDeviceListResource(HttpRestInteractionBusManager comMgr,
			HttpRestInteractionProviderBusDriver driver) {
		super();
		
		this.interactionComManager = comMgr;
		this.interactionDriver = driver;
	}

	private RestDeviceList getDeviceStates(
			List<String> types,
			List<UUID> uuids, 
			List<String> locations, 
			List<String> deviceClassifications, 
			List<String> deviceTypes) {
		RestDeviceList list = new RestDeviceList();
		List<String> typeNames = null;

		if (!types.isEmpty()) {
			typeNames = new ArrayList<>();

			for (String type : types) {
				typeNames.add(type);
			}
		} /* else: typesLC = null */

		Map<UUID, RestDevice> stateMap = interactionDriver.getRestStateDetails();

		for (Map.Entry<UUID, RestDevice> ent : stateMap.entrySet()) {
			RestDevice dev = ent.getValue();

			// only states for a list of uuids
			if (uuids != null && !uuids.isEmpty()) { 
				if (!uuids.contains(ent.getKey())) {
					continue;
				}
			}

			if (dev.hasDeviceMetaDetails()) {
				RestDeviceMetaDetails rdmd = dev.getDeviceMetaDetails();
				if (!locations.isEmpty()
						&& !locations.contains(rdmd.getLocation()))
					continue;
				if (!deviceClassifications.isEmpty()
						&& !deviceClassifications.contains(rdmd.getDeviceClassification().name()))
					continue;
				if (!deviceTypes.isEmpty()
						&& !deviceTypes.contains(rdmd.getDeviceType().name()))
					continue;
			} else {
				if (!locations.isEmpty() || !deviceTypes.isEmpty())
					continue;
			}

			if (typeNames != null) {
				RestDevice clone = dev.cloneOnly(typeNames);
				if (clone != null)
					list.add(clone);
			} else
				list.add(dev);
		} // for every device

		return list;
	}

	@GET
	@Produces({ "application/xml", "application/json" })
	public RestDeviceList getAssignedDevices(
			@QueryParam("type") List<String> types,
			@QueryParam("uuid") List<UUID> uuids,
			@QueryParam("location") List<String> locations,
			@QueryParam("deviceclass") List<String> deviceClassifications,
			@QueryParam("devicetype") List<String> deviceTypes) {
		return getDeviceStates(types, uuids, locations, deviceClassifications, deviceTypes);
	}

	@POST
	@Path("/{uuid}/")
	@Consumes({ "application/xml", "application/json"  })
	public Response switchCommand(
			@PathParam("uuid") String sUUID,
			RestSwitchCommand command) {
		
		UUID uuid;
		try {
			uuid = UUID.fromString(sUUID);
		} 
		catch (IllegalArgumentException e) {
			throw new NotFoundException();
		}

		interactionDriver.sendSwitchRequest(uuid, command.isTurnOn());

		return Response.ok().build();
	}

	@GET
	@Path("/{uuid}/do")
	@Consumes({ "application/xml", "application/json"  })
	public Response doAction(
			@PathParam("uuid") String sUUID,
			@QueryParam("action") String action) {
		
		UUID uuid;
		try {
			uuid = UUID.fromString(sUUID);
		} 
		catch (IllegalArgumentException e) {
			throw new NotFoundException();
		}

		if ("start".equals(action)) {
			interactionDriver.sendStartRequest(uuid);
		}
		if ("stop".equals(action)) {
			interactionDriver.sendStopRequest(uuid);
		}
		if ("switchOn".equals(action)) {
			interactionDriver.sendSwitchRequest(uuid, true);
		}
		if ("switchOff".equals(action)) {
			interactionDriver.sendSwitchRequest(uuid, false);
		}

		return Response.ok().build();
	}

}
