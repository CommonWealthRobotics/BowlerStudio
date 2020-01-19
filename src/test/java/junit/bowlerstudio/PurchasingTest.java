package junit.bowlerstudio;

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.vitamins.Purchasing;
import com.neuronrobotics.bowlerstudio.vitamins.PurchasingData;
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;
import java.io.IOException;
import org.junit.jupiter.api.Test;

/** @author hephaestus */
public class PurchasingTest {

  @Test
  public void test() {
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
      System.out.println("Type = " + vitaminsType);

      for (String vit : Vitamins.listVitaminSizes(vitaminsType)) {
        String vitaminSize = vit;
        for (String variant : Purchasing.listVitaminVariants(vitaminsType, vitaminSize)) {
          try {
            // System.out.println("\tConfig = "+vitaminSize);
            PurchasingData purchasing = Purchasing.get(vitaminsType, vitaminSize, variant);
            if (purchasing != null)
              System.out.println("Purchasing: " + purchasing + " = " + " " + variant);
            else System.out.println("FAILED: " + vitaminsType + " " + vitaminSize + " " + variant);

          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }

      // System.out.println(Purchasing.makeJson(vitaminsType));
    }
    //		for(String vitaminsType: Vitamins.listVitaminTypes())
    //		try {
    //			if(ScriptingEngine.isLoginSuccess())
    //				Purchasing.saveDatabase(vitaminsType);
    //		} catch (Exception e1) {
    //			// TODO Auto-generated catch block
    //			e1.printStackTrace();
    //		}
  }
}
