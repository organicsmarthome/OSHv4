package osh.mgmt.commanager;

import java.util.UUID;

import osh.core.interfaces.IOSHOC;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class KITHttpRestInteractionComManager extends HttpRestInteractionBusManager {

	public KITHttpRestInteractionComManager(IOSHOC controllerbox, UUID uuid) {
		super(controllerbox, uuid);
	}

}
