package net.atlaspvp.raidoutpost;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class GlobalTimer extends BukkitRunnable {

    private final RaidOutpost raidOutpost;
    private final List<RealTimeRunnable> runnables = new ArrayList<>();
    private long compareTime = System.currentTimeMillis();

    public GlobalTimer(RaidOutpost raidOutpost) {
        this.raidOutpost = raidOutpost;
        this.runTaskTimerAsynchronously(raidOutpost, 0, 1);
    }

    @Override
    public void run() {
        long delta = System.currentTimeMillis() - compareTime;
        if (raidOutpost.getCurrentRoFaction() != null && raidOutpost.getCurrentRoFaction().getTime() > 0) {
            long time = raidOutpost.getCurrentRoFaction().getTime() - delta;
            if (time < 0) raidOutpost.getCurrentRoFaction().setTime(0);
            else raidOutpost.getCurrentRoFaction().setTime(raidOutpost.getCurrentRoFaction().getTime() - delta);
        }
        compareTime = System.currentTimeMillis();
        if (runnables.isEmpty()) return;

        ListIterator<RealTimeRunnable> iterator = runnables.listIterator();
        while (iterator.hasNext()) {
            RealTimeRunnable runnable = iterator.next();
            if (runnable.canRun()) {
                runnable.runTask();
                iterator.remove();
            }
        }
    }

    public List<RealTimeRunnable> getRunnables() {
        return runnables;
    }
}

abstract class RealTimeRunnable extends BukkitRunnable {

    private final RaidOutpost raidOutpost;
    private final long compareTime = System.currentTimeMillis();
    private final long delay;
    private final int taskType;
    public static final int SYNC = 0;
    public static final int ASYNC = 1;

    public RealTimeRunnable(RaidOutpost raidOutpost, long delay, int taskType) {
        this.raidOutpost = raidOutpost;
        this.delay = delay;
        this.taskType = taskType;
        raidOutpost.getGlobalTimer().getRunnables().add(this);
    }

    public void runTask() {
        switch (taskType) {
            case 0 -> runTask(raidOutpost);
            case 1 -> runTaskAsynchronously(raidOutpost);
        }
    }

    public boolean canRun() {
        return System.currentTimeMillis() >= compareTime + delay;
    }

    public void stop() {
        raidOutpost.getGlobalTimer().getRunnables().remove(this);
    }
}
