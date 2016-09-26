package osh.comdriver.interaction.rest;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * 
 * @author Kaibin Bao
 *
 */
@Path("/")
public class RestPutBLEResource {
	
	private String lastMessage = null;
	
	/**
	 * CONSTRUCTOR
	 * @param comMgr
	 * @param driver
	 */
	public RestPutBLEResource() {
		super();
	}
	
	@POST
	@Path("/put/")
//	@Produces(MediaType.TEXT_PLAIN)
    public String putBle(final String message) {
		String returnValue = "Hi REST!".toLowerCase() + ": " + message + " lastMessage was: " + lastMessage;
//		synchronized (lastMessage) {
			lastMessage = message;
//		}
		
        return returnValue;
    }

	@GET
	@Path("/get/")
//	@Produces(MediaType.TEXT_PLAIN)
    public String getBle(final String message) {
        return "" + lastMessage;
    }
	
}
