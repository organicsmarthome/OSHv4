package osh.driver.dachs;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class _testGLTDachsPowerRequest {
	public static void main(String[] args) {
		
		String dachsHost = "192.168.1.111";
		String dachsPort = "8080";
		
		String dachsURL = "http://" + dachsHost + ":" + dachsPort + "/";
		
		String loginName = "glt";
		String loginPwd = "";
		
		GLTDachsPowerRequestThread powerRequestThread = new GLTDachsPowerRequestThread(
				null,
				true,
				dachsURL,
				loginName,
				loginPwd);
		new Thread(powerRequestThread, "DachsPowerRequestThread").start();
	}
	
}
