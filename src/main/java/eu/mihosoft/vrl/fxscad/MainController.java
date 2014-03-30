/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.fxscad;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.MeshContainer;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.StyleSpansBuilder;
import org.reactfx.Change;
import org.reactfx.EventStream;
import org.reactfx.EventStreams;

/**
 * FXML Controller class
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class MainController implements Initializable {

    private static final String[] KEYWORDS = new String[]{
        "abstract", "assert", "boolean", "break", "byte",
        "case", "catch", "char", "class", "const",
        "continue", "default", "do", "double", "else",
        "enum", "extends", "final", "finally", "float",
        "for", "goto", "if", "implements", "import",
        "instanceof", "int", "interface", "long", "native",
        "new", "package", "private", "protected", "public",
        "return", "short", "static", "strictfp", "super",
        "switch", "synchronized", "this", "throw", "throws",
        "transient", "try", "void", "volatile", "while"
    };

    private static final Pattern KEYWORD_PATTERN
            = Pattern.compile("\\b(" + String.join("|", KEYWORDS) + ")\\b");

    private final Group viewGroup = new Group();

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //

        final CodeArea codeArea = new CodeArea();
        codeArea.textProperty().addListener(
                (ObservableValue<? extends String> observable,
                        String oldText, String newText) -> {
                    Matcher matcher = KEYWORD_PATTERN.matcher(newText);
                    int lastKwEnd = 0;
                    StyleSpansBuilder<Collection<String>> spansBuilder
                    = new StyleSpansBuilder<>();
                    while (matcher.find()) {
                        spansBuilder.add(Collections.emptyList(),
                                matcher.start() - lastKwEnd);
                        spansBuilder.add(Collections.singleton("keyword"),
                                matcher.end() - matcher.start());
                        lastKwEnd = matcher.end();
                    }
                    spansBuilder.add(Collections.emptyList(),
                            newText.length() - lastKwEnd);
                    codeArea.setStyleSpans(0, spansBuilder.create());
                });

        EventStream<Change<String>> textEvents
                = EventStreams.changesOf(codeArea.textProperty());

        textEvents.reduceCloseSuccessions((a, b) -> b, Duration.ofMillis(500)).
                subscribe(code -> compile(code.getNewValue()));
        
        codeArea.replaceText(0, 0, "\n"
                + "CSG cube = new Cube(2).toCSG()\n"
                + "CSG sphere = new Sphere(1.25).toCSG()\n"
                + "\n"
                + "cube.difference(sphere)");

        editorContainer.setContent(codeArea);

        TextArea logView = new TextArea();

        logContainer.setContent(logView);

        // redirect sout
        RedirectableStream sout = new RedirectableStream(RedirectableStream.ORIGINAL_SOUT, logView);
        sout.setRedirectToUi(true);
        System.setOut(sout);

        // redirect sout
        RedirectableStream serr = new RedirectableStream(RedirectableStream.ORIGINAL_SERR, logView);
        serr.setRedirectToUi(true);
        System.setErr(serr);

        SubScene subScene = new SubScene(viewGroup, 100, 100, true, SceneAntialiasing.BALANCED);
//        subScene.setFill(Color.BLACK);
        
        subScene.widthProperty().bind(viewContainer.widthProperty());
        subScene.heightProperty().bind(viewContainer.heightProperty());

        PerspectiveCamera subSceneCamera = new PerspectiveCamera(false);
        subScene.setCamera(subSceneCamera);

        viewContainer.getChildren().add(subScene);
    }

    private void compile(String code) {
        try {

            CompilerConfiguration cc = new CompilerConfiguration();

            cc.addCompilationCustomizers(new ImportCustomizer().addStarImports("eu.mihosoft.vrl.v3d"));

            GroovyShell shell = new GroovyShell(getClass().getClassLoader(), new Binding(), cc);

//            shell.getContext().setVariable("cube", csg);
            Script script = shell.parse(code);

            Object obj = script.run();
//
//            System.out.println("obj: " + obj);

            if (obj instanceof CSG) {

                System.out.println("setting mesh");

                CSG csg = (CSG) obj;
                viewGroup.getChildren().clear();
                
                MeshContainer meshContainer = csg.toJavaFXMesh();

                final MeshView meshView = new MeshView(meshContainer.getMesh());
                
                setMeshScale(meshContainer, viewContainer.getBoundsInLocal(), meshView);

                PhongMaterial m = new PhongMaterial(Color.RED);

                meshView.setCullFace(CullFace.NONE);

                meshView.setMaterial(m);
                
                viewGroup.layoutXProperty().bind(viewContainer.widthProperty().divide(2));
                viewGroup.layoutYProperty().bind(viewContainer.heightProperty().divide(2));
                
                viewContainer.boundsInLocalProperty().addListener(
                        (ObservableValue<? extends Bounds> ov, Bounds t, Bounds t1) -> {
                    setMeshScale(meshContainer, t1, meshView);
                });

                VFX3DUtil.addMouseBehavior(meshView, viewContainer, MouseButton.PRIMARY);

                viewGroup.getChildren().add(meshView);

            }

        } catch (Throwable ex) {
            ex.printStackTrace(System.err);
        }
    }

    private void setMeshScale(MeshContainer meshContainer, Bounds t1, final MeshView meshView) {
        double maxDim =
                Math.max(meshContainer.getWidth(),
                        Math.max(meshContainer.getHeight(), meshContainer.getDepth()));
        
        double minContDim = Math.min(t1.getWidth(), t1.getHeight());
        
        double scale = minContDim/(maxDim*2);
        
        //System.out.println("scale: " + scale + ", maxDim: " + maxDim + ", " + meshContainer);
        
        meshView.setScaleX(scale);
        meshView.setScaleY(scale);
        meshView.setScaleZ(scale);
    }

    @FXML
    private ScrollPane editorContainer;

    @FXML
    private Pane viewContainer;

    @FXML
    private ScrollPane logContainer;

    /**
     * Returns the location of the Jar archive or .class file the specified
     * class has been loaded from. <b>Note:</b> this only works if the class is
     * loaded from a jar archive or a .class file on the locale file system.
     *
     * @param cls class to locate
     * @return the location of the Jar archive the specified class comes from
     */
    public static File getClassLocation(Class<?> cls) {

//        VParamUtil.throwIfNull(cls);
        String className = cls.getName();
        ClassLoader cl = cls.getClassLoader();
        URL url = cl.getResource(className.replace(".", "/") + ".class");

        String urlString = url.toString().replace("jar:", "");

        if (!urlString.startsWith("file:")) {
            throw new IllegalArgumentException("The specified class\""
                    + cls.getName() + "\" has not been loaded from a location"
                    + "on the local filesystem.");
        }

        urlString = urlString.replace("file:", "");
        urlString = urlString.replace("%20", " ");

        int location = urlString.indexOf(".jar!");

        if (location > 0) {
            urlString = urlString.substring(0, location) + ".jar";
        } else {
            //System.err.println("No Jar File found: " + cls.getName());
        }

        return new File(urlString);
    }

    public static synchronized void loadLibrary(java.io.File jar) {
        try {
            /*We are using reflection here to circumvent encapsulation; addURL is not public*/
            java.net.URLClassLoader loader = (java.net.URLClassLoader) ClassLoader.getSystemClassLoader();
            java.net.URL url = jar.toURI().toURL();
            /*Disallow if already loaded*/
            for (java.net.URL it : java.util.Arrays.asList(loader.getURLs())) {
                if (it.equals(url)) {
                    //throw new myException("library " + jar.toString() + " is already loaded");
                }
            }
            java.lang.reflect.Method method = java.net.URLClassLoader.class.getDeclaredMethod(
                    "addURL",
                    new Class[]{java.net.URL.class}
            );
            method.setAccessible(true); /*promote the method to public access*/

            method.invoke(loader, new Object[]{url});

        } catch (final NoSuchMethodException |
                java.lang.IllegalAccessException |
                java.net.MalformedURLException |
                java.lang.reflect.InvocationTargetException e) {
            //throw new myException(e.getMessage());

            e.printStackTrace(System.err);
        }
    }

}
