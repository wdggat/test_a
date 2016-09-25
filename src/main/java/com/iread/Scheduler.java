package com.iread;

import java.util.concurrent.*;

import com.iread.bean.Species;
import com.iread.conf.ConfMan;
import com.iread.spider.AmazonSpider;
import com.iread.spider.Spider;
import org.apache.log4j.Logger;

public class Scheduler {
    private static Logger logger = Logger.getLogger(Scheduler.class);
    
    private final ScheduledExecutorService scheduler;
    private final int initialDelay;
    private static ConfMan conf;
    private Species species;
    private static final int NUM_THREADS = 1;

    Scheduler(ConfMan conf, Species species) {
        this.initialDelay = conf.getInitDelay();
        this.conf = conf;
        this.species = species;
        scheduler = Executors.newScheduledThreadPool(NUM_THREADS);
    }
    
    public void startToWork() {
        Spider spider = null;
        if (species.equals(Species.AMAZON)) {
            spider = new AmazonSpider(conf);
        }
        startToWork(new ScrawlerTask(conf, spider));
    }

    public void startToWork(Runnable task) {
        logger.info("Scheduler is started");
        final ScheduledFuture<?> scrawlerFuture = scheduler
                .scheduleAtFixedRate(task, initialDelay, 0,
                        TimeUnit.SECONDS);

        scheduler.schedule(new Runnable() {
            public void run() {
                scrawlerFuture.cancel(false);
                logger.info("Scheduler is terminated.");                
            }
        }, 0, TimeUnit.SECONDS);
    }
}
