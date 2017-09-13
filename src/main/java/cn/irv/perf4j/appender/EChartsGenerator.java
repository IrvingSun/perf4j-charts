package cn.irv.perf4j.appender;

import org.perf4j.GroupedTimingStatistics;
import org.perf4j.chart.StatisticsChartGenerator;
import org.perf4j.helpers.StatsValueRetriever;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * generate the EChart json data for display
 * Created by SUNWEI on 2017/9/11.
 */
public class EChartsGenerator implements StatisticsChartGenerator {
    private StatsValueRetriever valueRetriever;
    private LinkedList<GroupedTimingStatistics> data = new LinkedList<GroupedTimingStatistics>();
    private int maxDataPoints = 100;
    private Set<String> enabledTags = null;

    public EChartsGenerator(StatsValueRetriever valueRetriever) {
        this.valueRetriever = valueRetriever;
    }

    @Override
    public String getChartUrl() {
        return null;
    }

    @Override
    public void appendData(GroupedTimingStatistics statistics) {
        if (this.data.size() >= this.maxDataPoints) {
            this.data.removeFirst();
        }
        this.data.add(statistics);
    }

    @Override
    public List<GroupedTimingStatistics> getData() {
        return Collections.unmodifiableList(this.data);
    }

    public Set<String> getEnabledTags() {
        return enabledTags;
    }

    public void setEnabledTags(Set<String> enabledTags) {
        this.enabledTags = enabledTags;
    }
}
