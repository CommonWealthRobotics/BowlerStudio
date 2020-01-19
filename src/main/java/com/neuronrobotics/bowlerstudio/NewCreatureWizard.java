package com.neuronrobotics.bowlerstudio;

public class NewCreatureWizard {

  public static void run() {
    new Thread(
            () -> {
              Thread.setDefaultUncaughtExceptionHandler(new IssueReportingExceptionHandler());
            })
        .start();
  }
}
