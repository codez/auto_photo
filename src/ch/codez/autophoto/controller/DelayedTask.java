/*
 * Created on 02.12.2007
 *
 */
package ch.codez.autophoto.controller;

import java.util.Timer;
import java.util.TimerTask;

public abstract class DelayedTask extends TimerTask {

    private long delay;

    public DelayedTask(long delay) {
        this.delay = delay;
    }

    public long getDelay() {
        return this.delay;
    }

    public Timer schedule() {
        Timer timer = new Timer(true);
        timer.schedule(this, delay);
        return timer;
    }

}
