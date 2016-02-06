package com.neuronrobotics.bowlerstudio;

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.sdk.util.ThreadUtil;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jfree.util.Log;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHGistBuilder;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.net.URL;

/**
 * Created by Ryan Benasutti on 2/5/2016.
 */

public class GistHelper
{
    public static void createNewGist(String filename, String description, boolean isPublic)
    {
        //TODO: Perhaps this method should throw GitAPIException and IOException
        GitHub gitHub = ScriptingEngine.getGithub();
        GHGistBuilder builder = gitHub.createGist();
        builder.file(filename, "//Your code here");
        builder.description(description);
        builder.public_(isPublic);

        GHGist gist;
        try
        {
            gist = builder.create();
            String gistID = ScriptingEngine.urlToGist(gist.getHtmlUrl());
            BowlerStudio.openUrlInNewTab(new URL(gist.getHtmlUrl()));
            System.out.println("Creating repo");
            while (true)
            {
                try
                {
                    ScriptingEngine.fileFromGistID(gistID, filename);
                    break;
                }
                catch (GitAPIException e)
                {
                    e.printStackTrace();
                }

                ThreadUtil.wait(500);
                Log.warn(filename + " not built yet");
            }

            System.out.println("Creating gist at " + filename);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        System.out.println("Creating gist at " + filename);
    }
}
