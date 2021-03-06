package com.abien.xray.business.statistics.boundary;

import com.abien.xray.business.HitsPer;
import static com.abien.xray.business.HitsPer.Frequency.MINUTE;
import com.abien.xray.business.hits.control.HitsManagement;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * @author Adam Bien, blog.adam-bien.com
 */
@Path("hitsperminute")
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@Produces({"text/plain"})
public class MinutelyStatisticsCalculator {

    @Inject
    HitsManagement hits;

    private long currentRate = 0;
    private long lastMeasurement = 0;
    private long maxHitsPerHour = 0;

    @Inject
    @HitsPer(MINUTE)
    Event<Long> minutelyEvent;

    @Schedule(minute = "*/1", hour = "*", persistent = false)
    public void computeStatistics() {
        long totalHits = hits.totalHits();
        currentRate = totalHits - lastMeasurement;
        lastMeasurement = totalHits;
        this.maxHitsPerHour = Math.max(currentRate, this.maxHitsPerHour);
        minutelyEvent.fire(currentRate);
    }

    @GET
    public String getHitsPerMinute() {
        return String.valueOf(currentRate);
    }

    @GET
    @Path("max")
    public String getMaxHitsPerMinute() {
        return String.valueOf(maxHitsPerHour);
    }

}
