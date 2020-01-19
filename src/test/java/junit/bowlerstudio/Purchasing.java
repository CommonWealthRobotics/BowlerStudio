package junit.bowlerstudio;


import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Purchasing {

	ArrayList<String> emptyList() {
		return new ArrayList<String>();
	}

	HashMap<String, String> emptyMap() {
		return new HashMap<String, String>();
	}
	
	List<Object> asList(Object ...objects){
		return  Arrays.asList(objects);
	}

	@Test
	public void test() throws XmlRpcException, MalformedURLException {

		XmlRpcClient client = new XmlRpcClient();

		XmlRpcClientConfigImpl start_config = new XmlRpcClientConfigImpl();
		start_config.setServerURL(new URL("https://demo.odoo.com/start"));
		Map<String, String> info = (Map<String, String>) client.execute(start_config, "start", emptyList());

		String url = info.get("host"), db = info.get("database"), username = info.get("user"),
				password = info.get("password");
		XmlRpcClientConfigImpl common_config = new XmlRpcClientConfigImpl();
		common_config.setServerURL(new URL(String.format("%s/xmlrpc/2/common", url)));
		Map<String, String> serverInfo = (Map<String, String>) client.execute(common_config, "version", emptyList());

		int uid = (int) client.execute(common_config, "authenticate",
				new Object[] { db, username, password, emptyMap() });

		XmlRpcClient models = new XmlRpcClient() {
			{
				setConfig(new XmlRpcClientConfigImpl() {
					{
						setServerURL(new URL(String.format("%s/xmlrpc/2/object", url)));
					}
				});
			}
		};
		models.execute("execute_kw", new Object[] { db, uid, password, "res.partner", "check_access_rights",
				new Object[] { "read" }, new HashMap() {
					{
						put("raise_exception", false);
					}
				} });
		// https://sourceforge.net/p/openerpjavaapi/wiki/Home/
		

	}

}
