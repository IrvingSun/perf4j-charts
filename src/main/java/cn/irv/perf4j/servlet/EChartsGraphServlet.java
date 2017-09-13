package cn.irv.perf4j.servlet;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.perf4j.GroupedTimingStatistics;
import org.perf4j.TimingStatistics;
import org.perf4j.helpers.MiscUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * 该实现支持
 * http://echarts.baidu.com/demo.html#line-marker所示的图表形式
 * 多数据折线图（保留基本功能）
 * Created by SUNWEI on 2017/9/12.
 */
public class EChartsGraphServlet extends AbstractChartServlet{

    protected List<JSONObject> packageSeries(List<GroupedTimingStatistics> datas, String currentKpi){
        Map<String, List<Integer>> seriesMap = new HashMap<>();
        List<JSONObject> seriesList = new ArrayList<>();
        for (int i = 0; i < datas.size(); i++) {
            SortedMap<String, TimingStatistics> tagData = datas.get(i).getStatisticsByTag();
            for (String key : tagData.keySet()) {
                if (!seriesMap.containsKey(key)) {
                    seriesMap.put(key, new ArrayList<Integer>());
                }
                String itemData = null;
                if ("avg".equals(currentKpi)) {
                    seriesMap.get(key).add(Integer.parseInt(String.valueOf(tagData.get(key).getMean()).split("\\.")[0]));
                } else if ("max".equals(currentKpi)) {
                    seriesMap.get(key).add(Integer.parseInt(String.valueOf(tagData.get(key).getMax()).split("\\.")[0]));
                } else if ("min".equals(currentKpi)) {
                    seriesMap.get(key).add(Integer.parseInt(String.valueOf(tagData.get(key).getMin()).split("\\.")[0]));
                } else if ("count".equals(currentKpi)) {
                    seriesMap.get(key).add(tagData.get(key).getCount());
                }
            }
        }

        for (String key : seriesMap.keySet()) {
            JSONObject seri = new JSONObject();
            seri.put("name", key);
            seri.put("type", "line");
            seri.put("data", seriesMap.get(key));
            seriesList.add(seri);
        }
        return seriesList;
    }

    protected List<String> getRequestKpis(HttpServletRequest request) {
        List<String> kpis = new ArrayList<>();
        List<String> supportKpis = new ArrayList<>();
        supportKpis.add("avg");
        supportKpis.add("max");
        supportKpis.add("min");
        supportKpis.add("count");

        String graphNamesString = request.getParameter("kpis");
        if (graphNamesString != null) {
            List<String> temp = Arrays.asList(MiscUtils.splitAndTrim(graphNamesString, ","));
            for (String kpi : temp) {
                if (supportKpis.contains(kpi.toLowerCase())) {
                    kpis.add(kpi);
                }
            }
        }
        return CollectionUtils.isEmpty(kpis) ? supportKpis : kpis;
    }

    protected JSONObject packageXAxis(List<GroupedTimingStatistics> datas){
        JSONObject xAxis = new JSONObject();
        xAxis.put("type", "category");
        xAxis.put("boundaryGap", false);
        List<String> xAxisData = new ArrayList<>();
        //获取时间戳
        for (int i = 0; i < datas.size(); i++) {
            GroupedTimingStatistics data = datas.get(i);
            if (i == 0) {
                xAxisData.add(MiscUtils.formatDateIso8601(data.getStartTime()));
                xAxisData.add(MiscUtils.formatDateIso8601(data.getStopTime()));
            } else {
                xAxisData.add(MiscUtils.formatDateIso8601(data.getStopTime()));
            }
        }
        xAxis.put("data", xAxisData);
        return xAxis;
    }

    protected JSONObject packageYAxis(){
        JSONObject yAxis = new JSONObject();
        yAxis.put("type", "value");
        JSONObject axisLabel = new JSONObject();
        axisLabel.put("formatter", "{value}");
        yAxis.put("axisLabel", axisLabel);
        return yAxis;
    }


    protected JSONObject packageLegend(List<GroupedTimingStatistics> datas){
        JSONObject legend = new JSONObject();
        String[] legendData = null;

        //获取到所有日志记录的方法profile注解中的名称
        for (int i = 0; i < datas.size(); i++) {
            GroupedTimingStatistics data = datas.get(i);
            if (data != null && !CollectionUtils.isEmpty(data.getStatisticsByTag().keySet())) {
                //初始化legendData
                if (legendData == null) {
                    legendData = data.getStatisticsByTag().keySet().toArray(new String[data.getStatisticsByTag().keySet().size()]);
                    legend.put("data", legendData);
                }
            }
        }
        return legend;
    }

    protected JSONObject packageOption(List<GroupedTimingStatistics> datas,String kpi){
        JSONObject option = new JSONObject();
        JSONObject title = new JSONObject();
        title.put("text", kpi + "记录");
        JSONObject tooltip = new JSONObject();
        tooltip.put("trigger", "axis");

        JSONObject legend = packageLegend(datas);

        JSONObject xAxis = packageXAxis(datas);
        JSONObject yAxis = packageYAxis();
        List<JSONObject> seriesList = packageSeries(datas, kpi);

        option.put("title", title);
        option.put("tooltip", tooltip);
        option.put("legend", legend);
        option.put("xAxis", xAxis);
        option.put("yAxis", yAxis);
        option.put("series", seriesList);

        return option;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doGet(request, response);
    }
}
