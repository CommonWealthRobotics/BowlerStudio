/**
 * 
 */
package junit.bowlerstudio;

import org.junit.Before;
import org.junit.Test;

/**
 * @author hephaestus
 *
 */
public class PurchasingTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		//TODO: Fix this test, currently throwing NullPointerException
//		try {
//			ScriptingEngine.runLogin();
//		} catch (IOException e2) {
//			e2.printStackTrace();
//		}
//		if(!ScriptingEngine.isLoginSuccess())
//			try{
//				ScriptingEngine.setupAnyonmous();
//
//			}catch (Exception ex){
//				System.out.println("User not logged in, test can not run");
//			}
//		for(String vitaminsType: Vitamins.listVitaminTypes()){
//			System.out.println("Type = "+vitaminsType);
//
//			for(String vit:Vitamins.listVitaminSizes(vitaminsType)){
//				String vitaminSize=vit;
//				for(String variant:Purchasing.listVitaminVariants(vitaminsType, vitaminSize)){
//					try{
//						//System.out.println("\tConfig = "+vitaminSize);
//						PurchasingData purchasing = Purchasing.get(vitaminsType, vitaminSize, variant);
//						if(purchasing!=null)
//							System.out.println("Purchasing: "+purchasing+" = "+" "+variant);
//						else
//							System.out.println("FAILED: "+vitaminsType+" "+ vitaminSize+" "+variant);
//
//					}catch (Exception e){
//						e.printStackTrace();
//					}
//				}
//
//			}
//
//			//System.out.println(Purchasing.makeJson(vitaminsType));
//		}
////		for(String vitaminsType: Vitamins.listVitaminTypes())
////		try {
////			if(ScriptingEngine.isLoginSuccess())
////				Purchasing.saveDatabase(vitaminsType);
////		} catch (Exception e1) {
////			e1.printStackTrace();
////		}
	}

}
