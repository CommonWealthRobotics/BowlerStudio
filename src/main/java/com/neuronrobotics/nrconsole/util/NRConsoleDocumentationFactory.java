package com.neuronrobotics.nrconsole.util;

import java.net.URI;
import java.net.URISyntaxException;
//
//import com.neuronrobotics.nrconsole.MenuBar;
//import com.neuronrobotics.nrconsole.plugin.DyIO.DyIOPanel;
//import com.neuronrobotics.nrconsole.plugin.DyIO.GoogleChat.GoogleChatLogin;
import com.neuronrobotics.sdk.common.BowlerDocumentationFactory;
import com.neuronrobotics.sdk.common.Log;

/**
 * Factory used to centralize references to web pages (specifically
 * documentation). Any documentation for an object type defined in NRConsole
 * will be found here. (Hint: if the URI refers to an object type defined in
 * NRConsole, it goes here).
 * 
 * Calls BowlerDocumentationFactory.getDocumentationURL(input) first, if type
 * not found it continues here.
 * 
 * See also: BowlerDocumentationFactory.java
 * 
 */
public class NRConsoleDocumentationFactory {

	public static URI getDocumentationURL(Object input) {

		try {
			return BowlerDocumentationFactory.getDocumentationURL(input);
		} catch (RuntimeException e) {
//			if (input instanceof MenuBar) {
//
//				try {
//					return new URI(
//							"http://neuronrobotics.github.io/DyIO-First-Steps/Testing-With-NR-Console");
//				} catch (URISyntaxException uriE) {
//					Log.error(e.getMessage());
//				}
//
//			} else if (input instanceof DyIOPanel) {
//
//				try {
//					return new URI(
//							"http://neuronrobotics.github.io/Getting-to-Know-the-DyIO/Anatomy-of-the-DyIO/");
//				} catch (URISyntaxException uriE) {
//					Log.error(e.getMessage());
//				}
//
//			} else if (input instanceof GoogleChatLogin) {
//				try {
//					return new URI(
//							"http://wiki.neuronrobotics.com/DyIO_Cloud_Connect");
//				} catch (URISyntaxException uriE) {
//					Log.error(e.getMessage());
//				}
//			}

			throw new RuntimeException("No documentation for object of type "
					+ input.getClass());
		}

	}

}
