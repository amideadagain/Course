package program;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import program.classes.CPU;
import program.classes.Resource;
import program.classes.TaskScheduler;
import program.classes.TickGenerator;

import java.util.ArrayList;

public class Main extends Application {
    public static Controller guiController;
    private static TickGenerator m_systemClock;
    private static CPU m_cpu;
    private static TaskScheduler m_taskScheduler;
    private static ArrayList<Resource> m_resources;
    private static boolean m_running = false;
    private static boolean m_firstRun = true;
    private static boolean m_pause = false;

    public static void main(String[] args) {
        launch(args);
    }

    public static void setupSystem() {
        System.out.println("Starting system.");

        m_cpu = new CPU(4);
        m_taskScheduler = new TaskScheduler(m_cpu, Configuration.getMemoryVolume());
        m_systemClock = new TickGenerator(m_cpu, m_taskScheduler);
        m_resources = new ArrayList<>();
        for (int i = 0; i < Configuration.getResourcesCount(); i++) {
            Resource r = new Resource("R" + (i + 1));
            m_resources.add(r);
            m_systemClock.attachSystemComponent(r);
        }

        guiController.initResourcesBar(m_resources);

        m_systemClock.start();
        m_running = true;

        System.out.println("System setup completed!");

        m_firstRun = false;
    }

    public static void finishWork() {
        if (m_taskScheduler != null) m_taskScheduler.finishWork();
        if (m_systemClock != null) m_systemClock.finishWork();
        m_running = false;
        System.out.println("System shutdown.");
    }

    public static int getSystemTime() {
        return m_systemClock.getTime();
    }

    public static TickGenerator getSystemClock() {
        return m_systemClock;
    }

    public static TaskScheduler getTaskScheduler() {
        return m_taskScheduler;
    }

    public static ArrayList<Resource> getSystemResources() {
        return m_resources;
    }

    public static boolean isRunning() {
        return m_running;
    }

    public static boolean isFirstRun() {
        return m_firstRun;
    }

    public static synchronized boolean pauseActive() {
        return m_pause;
    }

    public static synchronized void setPause(boolean value) {
        m_pause = value;
    }

    /*--Cross-Thread methods--*/

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("proga.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Course Work");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setResizable(false);

        guiController = loader.getController();
        guiController.initBaseTabs();
        guiController.initControlButtons();
        guiController.initTextFields();
        guiController.initSliders();
        guiController.initCheckBoxes();

        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        finishWork();
    }
}
