package com.neuronrobotics.bowlerstudio;

import com.neuronrobotics.bowlerstudio.scripting.PasswordManager;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.util.ThreadUtil;
import org.eclipse.jgit.api.errors.GitAPIException;
//import org.jfree.util.Log;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHGistBuilder;
import org.kohsuke.github.GHGistFile;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.net.URL;

/**
 * Created by Ryan Benasutti on 2/5/2016.
 */

public class GistHelper
{
    private GistHelper() {}

    public static String createNewGist(String filename, String description, boolean isPublic)
    {
        //TODO: Perhaps this method should throw GitAPIException and IOException
        //Setup gist
    	String defaultContents = "";//;
        GitHub gitHub = PasswordManager.getGithub();
        GHGistBuilder builder = gitHub.createGist();
        builder.file(filename, defaultContents);
        builder.description(description);
        builder.public_(isPublic);

        //Make gist
        return createGistFromBuilder(builder, filename);
    }

    public static String addFileToGist(String filename, String content, GHGist gistID)
    {
        GitHub gitHub = PasswordManager.getGithub();
        try
        {
            //Copy from old gist
            GHGist oldGist = gistID;
            GHGistBuilder builder = gitHub.createGist();

            builder.description(oldGist.getDescription());
            builder.public_(oldGist.isPublic());

            for (String key : oldGist.getFiles().keySet())
                builder.file(key, oldGist.getFiles().get(key).getContent());

            //Add new file
            builder.file(filename, content);

            //Make new gist with old filename
            return createGistFromBuilder(builder, oldGist.getFiles().values().iterator().next().getFileName());

            
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private static String createGistFromBuilder(GHGistBuilder builder, String filename)
    {
        GHGist gist;
        try
        {
            gist = builder.create();
            //String gistID = ScriptingEngine.urlToGist(gist.getHtmlUrl());
            
            //BowlerStudio.openUrlInNewTab(new URL(gist.getHtmlUrl()));
            System.out.println("Creating repo");
            while (true)
            {
                try
                {
                    ScriptingEngine.fileFromGit(gist.getGitPullUrl(), filename);
                    break;
                }
                catch (GitAPIException e)
                {
                    e.printStackTrace();
                }

                ThreadUtil.wait(500);
                //Log.warn(filename + " not built yet");
            }

            System.out.println("Creating gist at " + filename);
            ScriptingEngine.getLangaugeByExtention(filename).getDefaultContents(gist.getGitPullUrl(), filename);
            return gist.getGitPullUrl();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
