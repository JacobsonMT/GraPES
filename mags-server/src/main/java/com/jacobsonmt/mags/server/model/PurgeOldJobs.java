package com.jacobsonmt.mags.server.model;

import lombok.extern.log4j.Log4j2;

import java.util.Iterator;
import java.util.Map;

@Log4j2
public class PurgeOldJobs implements Runnable {

    private Map<String, JobDO> savedJobs;

    public PurgeOldJobs( Map<String, JobDO> savedJobs ) {
        this.savedJobs = savedJobs;
    }

    @Override
    public void run() {
        int jobsPurged = 0;
        synchronized ( savedJobs ) {
            for ( Iterator<Map.Entry<String, JobDO>> it = savedJobs.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, JobDO> entry = it.next();
                JobDO job = entry.getValue();
                if ( job.isComplete() && System.currentTimeMillis() > job.getSaveExpiredDate() ) {
                    job.setSaveExpiredDate( null );
                    it.remove();
                    log.debug( "Purged " + job.getJobId() );
                    jobsPurged++;
                }
            }
        }
        log.info( "Purged " + jobsPurged + " old jobs." );
    }

}