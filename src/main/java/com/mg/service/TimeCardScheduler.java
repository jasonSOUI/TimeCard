package com.mg.service;

import com.mg.enums.TimeCardStatus;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TimeCardScheduler {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final Map<TimeCardStatus, ScheduledFuture<?>> scheduledTasks = new HashMap<>();

    public void schedule(TimeCardStatus status, int hour, int minute) {
        // Cancel any existing task for this status
        cancel(status);

        Runnable task = () -> {
            System.out.println("Executing scheduled task: " + status.name());
            try {
                new AutoTimeCardService().checkin(status, false);
            } catch (Exception e) {
                System.out.println("Scheduled " + status.name() + " task failed: " + e.getMessage());
                e.printStackTrace();
            }
        };

        long initialDelay = calculateInitialDelay(hour, minute);
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(task, initialDelay, TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS);
        scheduledTasks.put(status, future);

        System.out.println("Scheduled " + status.name() + " at " + String.format("%02d:%02d", hour, minute) + " daily.");
        System.out.println("Task will run in " + TimeUnit.MILLISECONDS.toHours(initialDelay) + " hours.");
    }

    public void cancel(TimeCardStatus status) {
        ScheduledFuture<?> future = scheduledTasks.get(status);
        if (future != null) {
            future.cancel(false);
            scheduledTasks.remove(status);
            System.out.println("Cancelled scheduled " + status.name());
        }
    }

    private long calculateInitialDelay(int hour, int minute) {
        Calendar now = Calendar.getInstance();
        Calendar nextRun = Calendar.getInstance();
        nextRun.set(Calendar.HOUR_OF_DAY, hour);
        nextRun.set(Calendar.MINUTE, minute);
        nextRun.set(Calendar.SECOND, 0);
        nextRun.set(Calendar.MILLISECOND, 0);

        if (now.after(nextRun)) {
            nextRun.add(Calendar.DAY_OF_MONTH, 1);
        }

        return nextRun.getTimeInMillis() - now.getTimeInMillis();
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}
