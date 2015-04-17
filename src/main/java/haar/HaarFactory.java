package haar;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.neuronrobotics.jniloader.NativeResource;

public class HaarFactory {
	public static File jarResourceToFile(String resource){
		
		File resourceLocation = NativeResource.prepResourceLocation("BowlerCache_"+resource);
		
		try {
			NativeResource.copyResource(getStream(resource), resourceLocation);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		return resourceLocation;
	}
	public static InputStream getStream(String file) {
		return HaarFactory.class.getResourceAsStream(file);
	}
}

