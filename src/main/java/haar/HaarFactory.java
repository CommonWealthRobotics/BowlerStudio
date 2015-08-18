package haar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.neuronrobotics.imageprovider.NativeResource;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class HaarFactory {
	private static boolean haarsLoaded = false;
	private static List<String> availibleHaar=null;
	static{
		new Thread(){
			public void run(){
				
				try {
					availibleHaar = new ArrayList<String>();
					
					CodeSource src = HaarFactory.class.getProtectionDomain()
							.getCodeSource();
					if (src != null) {
						URL jar = src.getLocation();
						Log.debug("Loading from: " + jar);
						ZipInputStream zip = new ZipInputStream(jar.openStream());
						ZipEntry e = zip.getNextEntry();
						if (e == null) {
							// this is a run from the filesystem, not a jar
							File folder = new File(jar.getPath()+"/haar/");
							if(folder.isDirectory()){
								File[] listOfFiles = folder.listFiles();
								for (int i = 0; i < listOfFiles.length; i++) {
									if (listOfFiles[i].isFile()) {
										availibleHaar.add(listOfFiles[i].getName());
									}
								}
							}
						} else {
							while (true) {

								if (e == null) {
									Log.debug("end of jar " + jar);
									break;
								}
								String name = e.getName();
								if (name.endsWith(".xml")&& name.startsWith("haar/") ) {
									availibleHaar.add(name.split("/")[1]);
								} else {
									//Log.debug("Rejecting Haar " + name);
								}
								e = zip.getNextEntry();
							}
						}
					} else {
						/* Fail... */
						Log.debug("No code source found for haar search");
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//System.out.println("HAAR loaded");
				haarsLoaded=true;
			}
		}.start();
	}
	
	public static File jarResourceToFile(String resource) {

		File resourceLocation = NativeResource
				.prepResourceLocation("BowlerCache_" + resource);

		try {
			NativeResource.copyResource(getStream(resource), resourceLocation);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		return resourceLocation;
	}

	public static List<String> getAvailibHaar() throws IOException {
		
		if(availibleHaar!=null)
			return availibleHaar;
		while(!haarsLoaded){
			ThreadUtil.wait(10);
		}
		return availibleHaar;
	}

	public static InputStream getStream(String file) {
		return HaarFactory.class.getResourceAsStream(file);
	}
}