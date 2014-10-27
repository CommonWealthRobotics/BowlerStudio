
/* 
 * RedirectableStream.java
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009–2012 Steinbeis Forschungszentrum (STZ Ölbronn),
 * Copyright (c) 2006–2012 by Michael Hoffer
 * 
 * This file is part of Visual Reflection Library (VRL).
 *
 * VRL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3
 * as published by the Free Software Foundation.
 * 
 * see: http://opensource.org/licenses/LGPL-3.0
 *      file://path/to/VRL/src/eu/mihosoft/vrl/resources/license/lgplv3.txt
 *
 * VRL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * This version of VRL includes copyright notice and attribution requirements.
 * According to the LGPL this information must be displayed even if you modify
 * the source code of VRL. Neither the VRL Canvas attribution icon nor any
 * copyright statement/attribution may be removed.
 *
 * Attribution Requirements:
 *
 * If you create derived work you must do three things regarding copyright
 * notice and author attribution.
 *
 * First, the following text must be displayed on the Canvas:
 * "based on VRL source code". In this case the VRL canvas icon must be removed.
 * 
 * Second, the copyright notice must remain. It must be reproduced in any
 * program that uses VRL.
 *
 * Third, add an additional notice, stating that you modified VRL. In addition
 * you must cite the publications listed below. A suitable notice might read
 * "VRL source code modified by YourName 2012".
 * 
 * Note, that these requirements are in full accordance with the LGPL v3
 * (see 7. Additional Terms, b).
 *
 * Publications:
 *
 * M. Hoffer, C.Poliwoda, G.Wittum. Visual Reflection Library -
 * A Framework for Declarative GUI Programming on the Java Platform.
 * Computing and Visualization in Science, 2011, in press.
 */
package eu.mihosoft.vrl.fxscad;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

/**
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class RedirectableStream extends PrintStream {

    public static PrintStream ORIGINAL_SOUT = System.out;
    public static PrintStream ORIGINAL_SERR = System.err;

    private final List<TextArea> views = new ArrayList<TextArea>();
    private boolean redirectToUi;
    private boolean redirectToStdOut;
    private final List<OutputFilter> filters = new ArrayList<>();

    public RedirectableStream(OutputStream out, TextArea... views) {
        super(out);
        this.views.clear();

        for (TextArea textArea : views) {
            addView(textArea);
        }

        setRedirectToStdOut(true);
    }

    @Override
    public synchronized void write(byte[] buf, int off, int len) {
        if (isRedirectToUi()) {

            invokeAndWait(() -> {

                int i = 0;
                for (TextArea view : views) {

                    String s = new String(buf, off, len);

                    if (filters.get(i).onMatch(s)) {
                        try {
                            int startOffSet = view.getText().length();
                            view.insertText(startOffSet, s);
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        }
                    }
                    i++;
                }
            });

//                view.setCaretPosition(startOffSet + len);
//            } 
//            catch (BadLocationException e) {
            // e.printStackTrace();
//            Platform.invokeLater(new Runnable() {
//
//                @Override
//                public void run() {
//                    // automatically scroll down to the last line
////                    view.scrollRectToVisible(
////                            new Rectangle(0, view.getHeight() - 1, 1, 1));
//
//                }
//            });
        }

        if (isRedirectToStdOut()) {
            super.write(buf, off, len);
        }
    }

    public final void addView(TextArea view) {
        views.add(view);
        filters.add((OutputFilter) (String s) -> {
            return true;
        });
    }

    public void setFilter(TextArea view, OutputFilter filter) {
        int i = views.indexOf(view);
        filters.set(i, filter);
    }

    /**
     * @return the redirectToUi
     */
    public boolean isRedirectToUi() {
        return redirectToUi;
    }

    /**
     * @param redirectToUi the redirectToUi to set
     */
    public void setRedirectToUi(boolean redirectToUi) {
        this.redirectToUi = redirectToUi;
    }

    /**
     * @return the redirectToStdOut
     */
    public boolean isRedirectToStdOut() {
        return redirectToStdOut;
    }

    /**
     * @param redirectToStdOut the redirectToStdOut to set
     */
    public final void setRedirectToStdOut(boolean redirectToStdOut) {
        this.redirectToStdOut = redirectToStdOut;
    }

    private static void invokeAndWait(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            FutureTask<Boolean> task = new FutureTask<>(r, true);

            Platform.runLater(task);

            try {
                task.get(); // like join()
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(RedirectableStream.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
