package cn.irv.perf4j.servlet;

import cn.irv.perf4j.appender.EChartsGraphingStatisticsAppender;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.perf4j.GroupedTimingStatistics;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * ECharts 格式图表抽象实现
 * Created by SUNWEI on 2017/9/11.
 */
public abstract class AbstractChartServlet extends HttpServlet {
    private Logger logger = Logger.getLogger(AbstractChartServlet.class);
    public void init() throws ServletException {

    }

    public void destroy() {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<String> kpis = getRequestKpis(request);
        String callback = request.getParameter("callback");
        if (StringUtils.isEmpty(callback)) {
            logger.error("不支持非JSONP形式的调用");
            return;
        }
        // 每个kip对应一张图
        List<JSONObject> graphes = new ArrayList<JSONObject>();

        //获取到任意一个采样
        List<String> graphNames = getAllKnownGraphNames();
        if (CollectionUtils.isEmpty(graphNames)) {
            response.getWriter().write("-1");
            logger.warn("[AbstractChartServlet] 没有获取到采样信息");
            return;
        }

        List<GroupedTimingStatistics> datas = EChartsGraphingStatisticsAppender.getAppenderByName(graphNames.get(0)).getChartGenerator().getData();

        //拼装OPTION
        for (String kpi : kpis) {
            JSONObject option = packageOption(datas,kpi);
            graphes.add(option);
        }

        response.setContentType("text/html; charset=utf-8");
        PrintWriter out = response.getWriter();
        out.write(callback + "(" + JSONObject.toJSONString(graphes) + ")");
        out.flush();
        out.close();
        return;
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    protected List<String> getAllKnownGraphNames() {
        List<String> retVal = new ArrayList<String>();
        for (EChartsGraphingStatisticsAppender appender : EChartsGraphingStatisticsAppender.getAllChartGraphingStatiscsAppenders()) {
            retVal.add(appender.getName());
        }
        return retVal;
    }
    /**
     * 拼装显示数据
     * @param datas
     * @param currentKpi
     * @return
     */
    protected abstract List<JSONObject> packageSeries(List<GroupedTimingStatistics> datas, String currentKpi);

    /**
     * 拼装纵轴数据
     * @return
     */
    protected abstract JSONObject packageYAxis();

    /**
     * 拼装 横轴 数据
     * @param datas
     * @return
     */
    protected abstract JSONObject packageXAxis(List<GroupedTimingStatistics> datas);

    /**
     * 拼装legend显示数据
     * @param datas
     * @return
     */
    protected abstract JSONObject packageLegend(List<GroupedTimingStatistics> datas);

    /**
     * 获取请求的显示的指标
     * 如果没有指定，则获取4个指标
     * @param request
     * @return
     */
    protected abstract List<String> getRequestKpis(HttpServletRequest request);

    /**
     * 拼装Option
     * @param datas
     * @param kpi
     * @return
     */
    protected abstract JSONObject packageOption(List<GroupedTimingStatistics> datas, String kpi);
}

