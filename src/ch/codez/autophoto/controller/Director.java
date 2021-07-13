/*
 * Created on 30.11.2007
 *
 */
package ch.codez.autophoto.controller;

import ch.codez.autophoto.AppOptions;
import ch.codez.autophoto.camera.CameraAdapter;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.Timer;

public class Director {
    
    private static Logger log = Logger.getLogger(Director.class);
    
    private static AppOptions SETTINGS = AppOptions.getInstance();
    
    
    List<Timer> timers = new ArrayList<Timer>();
    
    RandomCountdownTimerTask randomCountdown = null;
    
    Set<DirectorListener> listeners = new HashSet<DirectorListener>();

    
    public void andAction() {
        // stop worker & perform cleanup
        this.cancel();
        PhotoWorker.getInstance().stop();

        CountdownTimerTask counter = new CountdownTimerTask();
        if ( CameraAdapter.getInstance().isRandom() ) {
            this.randomCountdown = new RandomCountdownTimerTask();
            counter = this.randomCountdown;
        }

        int seq = SETTINGS.getNextSequence();
        this.timers.add(new SnapshotTimerTask(this, seq).schedule());

        Timer countdown = new Timer(true);
        countdown.scheduleAtFixedRate(counter, 0, 1000);
        this.timers.add(countdown);
    }
    
    public void snapshotReady(String filename) {
        if (this.randomCountdown != null) {
            this.randomCountdown.stop();

        }
        this.cancel();
        for (DirectorListener l : this.listeners) {
            l.ready(filename);
        }
    }
    
    public void cancel() {
        for (Timer timer : timers) {
            timer.cancel();
        }
        timers.clear();
        this.randomCountdown = null;
        log.debug("Director cancelled");
    }
    
    public void addListener(DirectorListener l) {
        this.listeners.add(l);
    }
    
    public void removeListener(DirectorListener l) {
        this.listeners.remove(l);
    }

    public class CountdownTimerTask extends TimerTask {
        protected int i = Director.SETTINGS.getDirectorCountdown();
        public void run() {
            for (DirectorListener l : Director.this.listeners) {
                l.countDownAt(i);
            }
            i--;
            if (i < 0) {
                log.debug("Countdown terminated");
                for (DirectorListener l : Director.this.listeners) {
                    l.processing();
                }
                this.cancel();
            }
        }
    }
    
    public class RandomCountdownTimerTask extends CountdownTimerTask {

        
        public void run() {
            super.run();
            this.i = (int)(Math.random() * 59 + 1);
        }
        
        public void stop() {
            this.i = 0;
        }
    }
}
