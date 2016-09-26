package osh.old.busdriver.wago.parser;

import osh.old.busdriver.wago.SmartPlugException;
import osh.old.busdriver.wago.TCPUDPConnectionHandler;
import osh.old.busdriver.wago.TCPUDPConnectionHandler.CommandGenerator;

public class SwitchCommand extends CommandGenerator {

	private String device;
	private int id, function;
	private Command cmd;
	private TCPUDPConnectionHandler handler;
	
	public static enum Command {
		CMD_ON("on"),
		CMD_OFF("off"),
		CMD_TOGGLE("toggle");
		
		private String command;
		
		private Command(String cmd) {
			this.command = cmd;
		}
		
		public String getCommand() {
			return command;
		}
	}

	public SwitchCommand(String device, int id, Command cmd, TCPUDPConnectionHandler handler) {
		this(device, id, 0, cmd, handler);
	}

	public SwitchCommand(String device, int id, int function, Command cmd, TCPUDPConnectionHandler handler) {
		this.device = device;
		this.id = id;
		this.function = function;
		this.cmd = cmd;
		this.handler = handler;
	}
	
	@Override
	public void sendCommand() throws SmartPlugException {
		super.sendCommand(device, Integer.toString(id), Integer.toString(function), cmd.getCommand(), handler);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof SwitchCommand)) return false;
		SwitchCommand obj = (SwitchCommand) o;
		
		if (obj.cmd == cmd &&
			equalsTarget(obj)) return true; else return false;
	}

	@Override
	public boolean equalsTarget(Object o) {
		if (!(o instanceof SwitchCommand)) return false;
		SwitchCommand obj = (SwitchCommand) o;
		
		if (obj.device.equals(device) &&
			obj.function == function &&
			obj.id == id &&
			obj.handler == handler) return true; else return false;
	}
	
	
}
