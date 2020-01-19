package com.neuronrobotics.bowlerstudio;

import javafx.event.Event;
import javafx.event.EventHandler;

public abstract class MenuResettingEventHandler implements EventHandler<Event> {
  private Runnable menuReset = null;

  public Runnable getMenuReset() {
    if (menuReset == null)
      menuReset =
          new Runnable() {

            @Override
            public void run() {
              // TODO Auto-generated method stub

            }
          };
    return menuReset;
  }

  public void setMenuReset(Runnable menuReset) {
    this.menuReset = menuReset;
  }
}
