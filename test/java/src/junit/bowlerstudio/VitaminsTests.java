/**
 * 
 */
package junit.bowlerstudio;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;

/**
 * @author hephaestus
 *
 */
public class VitaminsTests {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		try {
			ScriptingEngine.runLogin();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		if(!ScriptingEngine.isLoginSuccess())
			try{
				ScriptingEngine.setupAnyonmous();
				
			}catch (Exception ex){
				System.out.println("User not logged in, test can not run");
			}
		for(String vitaminsType: Vitamins.listVitaminTypes()){
			HashMap<String, Object> meta = Vitamins.getMeta(vitaminsType);
			System.out.println("Type = "+vitaminsType);
			for(String vitaminSize:Vitamins.listVitaminSizes(vitaminsType)){
				if(!meta.isEmpty()){
					System.out.println("Meta configurations"+meta);
					try {
						//System.out.println(Vitamins.get(vitaminsType,vitaminSize));
					} catch (Exception e) {
						e.printStackTrace();
						fail();
					}
				}
				System.out.println("\tConfig = "+vitaminSize);
				HashMap<String, Object> config = Vitamins.getConfiguration(vitaminsType, vitaminSize);
				for(String param: config.keySet()){
					System.out.println("\t\t"+param+" = "+config.get(param));
				}
				
			}
			System.out.println(Vitamins.makeJson(vitaminsType));
//			try {
//				Vitamins.saveDatabase(vitaminsType);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
	}

}
