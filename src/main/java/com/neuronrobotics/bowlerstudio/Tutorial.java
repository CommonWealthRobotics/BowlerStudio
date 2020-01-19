package com.neuronrobotics.bowlerstudio;

import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.sdk.util.ThreadUtil;
import java.io.File;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;

public class Tutorial {
  private static int WEBSERVER_PORT = 37037;
  private static String HOME_Local_URL_ROOT = null;
  private static String HOME_URL = null;
  private static String HOME_Local_URL = null;
  private static boolean doneLoadingTutorials;
  private static Boolean startedLoadingTutorials = false;

  public static String getHomeUrl() throws Exception {
    File i = null;
    do {
      i =
          ScriptingEngine.fileFromGit(
              (String)
                  ConfigurationDatabase.getObject(
                      "BowlerStudioConfigs",
                      "tutorialSource",
                      "https://github.com/CommonWealthRobotics/CommonWealthRobotics.github.io.git"),
              (String)
                  ConfigurationDatabase.getObject(
                      "BowlerStudioConfigs",
                      "tutorialBranch",
                      "master"), // the default branch is source, so this needs to
              // be specified
              "index.html");
    } while (!i.exists());
    HOME_Local_URL_ROOT =
        (String)
            ConfigurationDatabase.getObject(
                "BowlerStudioConfigs", "tutorialBaseUrl", "/BowlerStudio/Welcome-To-BowlerStudio/");
    File indexOfTutorial = i;
    if (!doneLoadingTutorials) {
      if (!startedLoadingTutorials) {
        // synchronized(startedLoadingTutorials){
        startedLoadingTutorials = true;
        // }
        new Thread() {
          public void run() {
            Thread.currentThread()
                .setUncaughtExceptionHandler(new IssueReportingExceptionHandler());

            // HOME_Local_URL = indexOfTutorial.toURI().toString().replace("file:/", "file:///");
            Server server = new Server(WEBSERVER_PORT);
            ServerConnector connector = new ServerConnector(server);
            server.setConnectors(new Connector[] {connector});
            ResourceHandler resource_handler = new ResourceHandler();
            resource_handler.setDirectoriesListed(true);
            resource_handler.setWelcomeFiles(new String[] {"index.html"});
            System.out.println("Serving " + indexOfTutorial.getParent());
            resource_handler.setResourceBase(indexOfTutorial.getParent());

            HandlerList handlers = new HandlerList();
            handlers.setHandlers(new Handler[] {resource_handler, new DefaultHandler()});
            server.setHandler(handlers);

            try {
              server.start();
              WEBSERVER_PORT = connector.getLocalPort();
              HOME_Local_URL = "http://localhost:" + WEBSERVER_PORT + HOME_Local_URL_ROOT;
              doneLoadingTutorials = true;
              server.join();
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          }
        }.start();
      }
      long start = System.currentTimeMillis();
      // wait up to 30 seconds for menue to load, then fail over to the web version
      while (!doneLoadingTutorials && (System.currentTimeMillis() - start < 3000)) {
        ThreadUtil.wait(100);
      }

      if (doneLoadingTutorials) HOME_URL = HOME_Local_URL;
      else HOME_URL = "http://CommonWealthRobotics.com" + HOME_Local_URL_ROOT;
    }
    return HOME_URL;
  }
}
