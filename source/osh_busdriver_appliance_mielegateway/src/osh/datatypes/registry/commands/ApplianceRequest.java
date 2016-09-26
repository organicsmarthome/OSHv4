package osh.datatypes.registry.commands;

import java.util.UUID;

import osh.datatypes.registry.CommandExchange;
import osh.en50523.EN50523OIDExecutionOfACommandCommands;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class ApplianceRequest extends CommandExchange {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -67829648900818704L;
	private EN50523OIDExecutionOfACommandCommands command;

	public ApplianceRequest(UUID sender, UUID receiver, long timestamp, EN50523OIDExecutionOfACommandCommands command) {
		super(sender, receiver, timestamp);
		
		this.command = command;
	}

	public EN50523OIDExecutionOfACommandCommands getCommand() {
		return command;
	}

	public void setCommand(EN50523OIDExecutionOfACommandCommands command) {
		this.command = command;
	}

}
