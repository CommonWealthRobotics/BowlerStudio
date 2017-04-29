package com.neuronrobotics.bowlerstudio;

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.sdk.util.ThreadUtil;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;

public class Tutorial {
    private static int WEBSERVER_PORT = 8065;
    private static String HOME_URL = "http://CommonWealthRobotics.com/BowlerStudio/Welcome-To-BowlerStudio/";
    private static String HOME_Local_URL = "http://localhost:" + WEBSERVER_PORT + "/BowlerStudio/Welcome-To-BowlerStudio/";
    private static boolean doneLoadingTutorials;
    private static Boolean startedLoadingTutorials = false;

    public static String getHomeUrl() {

        if (!doneLoadingTutorials) {
            if (!startedLoadingTutorials) {
                synchronized (startedLoadingTutorials) {
                    startedLoadingTutorials = true;
                }

                new Thread(() -> {
                    try {

                        File indexOfTutorial = ScriptingEngine.fileFromGit(
                                "https://github.com/CommonWealthRobotics/CommonWealthRobotics.github.io.git",
                                "master",// the default branch is source, so this needs to be specified
                                "index.html");

                        //HOME_Local_URL = indexOfTutorial.toURI().toString().replace("file:/", "file:///");
                        Server server = new Server();
                        ServerConnector connector = new ServerConnector(server);
                        server.setConnectors(new Connector[]{connector});
                        ResourceHandler resource_handler = new ResourceHandler();
                        resource_handler.setDirectoriesListed(true);
                        resource_handler.setWelcomeFiles(new String[]{"index.html"});
                        System.out.println("Serving " + indexOfTutorial.getParent());
                        resource_handler.setResourceBase(indexOfTutorial.getParent());

                        HandlerList handlers = new HandlerList();
                        handlers.setHandlers(new Handler[]{resource_handler, new DefaultHandler()});
                        server.setHandler(handlers);

                        try {
                            server.start();
                            WEBSERVER_PORT = connector.getLocalPort();
                            HOME_Local_URL = "http://localhost:" + WEBSERVER_PORT + "/BowlerStudio/Welcome-To-BowlerStudio/";
                            doneLoadingTutorials = true;
                            server.join();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } catch (GitAPIException | IOException e2) {
                        e2.printStackTrace();
                    }
                }).start();
            }
            long start = System.currentTimeMillis();
            // wait up to 30 seconds for menue to load, then fail over to the web version
            while (!doneLoadingTutorials && (System.currentTimeMillis() - start < 3000))
                ThreadUtil.wait(100);
            if (doneLoadingTutorials)
                HOME_URL = HOME_Local_URL;
        }
        return HOME_URL;
    }
}
