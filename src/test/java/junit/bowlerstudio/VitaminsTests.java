/**
 * 
 */
package junit.bowlerstudio;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.HashMap;

import com.neuronrobotics.bowlerstudio.NewVitaminWizardController;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;
import com.neuronrobotics.sdk.addons.kinematics.JavaFXInitializer;
import org.junit.jupiter.api.Test;

/**
 * @author hephaestus
 *
 */
public class VitaminsTests {

	@Test
	public void testCreateVitamin() throws Exception {
//		try {
//			ScriptingEngine.login();
//		} catch (IOException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		}
//		if (!ScriptingEngine.isLoginSuccess()) {
//			try {
//				ScriptingEngine.setupAnyonmous();
//
//			} catch (Exception ex) {
//				System.out.println("User not logged in, test can not run");
//			}
//			return;
//		}
//		JavaFXInitializer.go();
//		NewVitaminWizardController.launchWizard();
//		Thread.sleep(10000);
	}

	@Test
	public void test() throws Exception {
		try {
			ScriptingEngine.login();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		if (!ScriptingEngine.isLoginSuccess())
			try {
				ScriptingEngine.setupAnyonmous();

			} catch (Exception ex) {
				System.out.println("User not logged in, test can not run");
			}
		for (String vitaminsType : Vitamins.listVitaminTypes()) {
			HashMap<String, Object> meta = Vitamins.getMeta(vitaminsType);
			System.out.println("Type = " + vitaminsType);
			for (String vitaminSize : Vitamins.listVitaminSizes(vitaminsType)) {
				if (!meta.isEmpty()) {
					System.out.println("Meta configurations" + meta);
					try {
						// System.out.println(Vitamins.get(vitaminsType,vitaminSize));
					} catch (Exception e) {
						e.printStackTrace();
						fail();
					}
				}
				System.out.println("\tConfig = " + vitaminSize);
				HashMap<String, Object> config = Vitamins.getConfiguration(vitaminsType, vitaminSize);
				for (String param : config.keySet()) {
					System.out.println("\t\t" + param + " = " + config.get(param));
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
		Vitamins.get("capScrew", "M3");

	}

}
