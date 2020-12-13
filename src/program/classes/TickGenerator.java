package program.classes;

import program.Configuration;
import program.Main;
import program.util.ITickNotifier;

import java.util.ArrayList;

public class TickGenerator extends Thread {
    ArrayList<ITickNotifier> attachedComponents;

    private int currentTick = 0;

    private boolean running = false;

    public TickGenerator(ITickNotifier... attachedComponents) {
        this.attachedComponents = new ArrayList<>();

        for (ITickNotifier item : attachedComponents) {
            this.attachedComponents.add(item);
        }
    }

    public void attachSystemComponent(ITickNotifier component) {
        attachedComponents.add(component);
    }

    /**
     * генерируем тики с равными промежутками времени
     */
    @Override
    public void run() {
        running = true;

        System.out.println("System clock is running.");

        while (running) {
            if (!Main.pauseActive()) {
                try {
                    Thread.sleep(Math.floorDiv(1000, Configuration.getClockTps()));
                    for (ITickNotifier item : attachedComponents) {
                        item.tick(currentTick);
                    }
                    currentTick++;
                    Main.guiController.updateTicks();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("System clock is stopped.");
    }

    public int getTime() {
        return currentTick;
    }

    public String getInfo() {
        return "";
    }

    /*--external control--*/

    public void nextTick() {
        for (ITickNotifier item : attachedComponents) {
            item.tick(currentTick);
        }
        currentTick++;
        Main.guiController.updateTicks();
    }

    public void finishWork() {
        running = false;
    }
}
