# perf4j-charts
## 实现功能点：
- 自定义能返回Echarts折线图数据的Servlet
- 以JSONP的方式返回JSON数据，可以在本地建立任意的页面调用

## 效果图
![这里写图片描述](http://img.blog.csdn.net/20170913234305389?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvc3Vud2VpX3B5dw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

## 使用方式
- build 并引入

- 在log4j配置文件中，添加图表appender
``` xml
    
    <appender name="CoalescingStatistics"
              class="org.perf4j.log4j.AsyncCoalescingStatisticsAppender">
        <!-- TimeSlice配置多少时间间隔去做一次汇总写入文件中 默认值是 30000 ms ,设置为2分钟-->
        <param name="TimeSlice" value="20000"/>
        <appender-ref ref="performanceAppenderLOG"/>
        <appender-ref ref="record"/>
    </appender>
    <!-- 汇总的perf4j的日志信息-->
    <appender name="performanceAppenderLOG" class="org.apache.log4j.FileAppender">
        <param name="File" value="${catalina.home}/logs/perf4j/perfStats.log"/>
        <param name="Append" value="true"/>
        <layout class="org.apache.log4j.PatternLayout">
            <param name="ConversionPattern" value="%m%n"/>
        </layout>
    </appender>
    <appender name="record" class="org.perf4j.log4j.GraphingStatisticsAppender">
            <param name="GraphType" value="Max"/>
            <appender-ref ref="graphsFileAppender"/>
    </appender>
    
```
- 添加servlet
``` xml
  <servlet>
    <servlet-name>perf4j</servlet-name>
    <servlet-class>cn.irv.perf4j.servlet.EChartsGraphServlet</servlet-class>
  </servlet>
  <servlet-mapping>
    <servlet-name>perf4j</servlet-name>
    <url-pattern>/admin</url-pattern>
  </servlet-mapping>
```
- 页面参考：
``` html
<html>
<head>
<title>统计图表</title>
<script src="echarts.min.js"></script>
<script src="jquery-3.2.1.min.js"></script>
</head>
<body>
<h1>统计图表</h1>
<script type="text/javascript">
/**
    url可以以GET方式添加参数：
    kpis=avg,count  表示只显示avg和count的图表，支持的kpi为：avg、max、min、count
**/
var url = "http://localhost:8080/admin";
var divContent = '<div id="contentId" style="width:650px; height: 400px; float:left"></div>';
var charts = [];
    $.ajax({
        url: url,
        type: 'GET',
        dataType: 'JSONP',
        async: false,
        jsonp: 'callback',
        success: function (data) {
            data.forEach( function(element, index) {
                $(document.body).append(divContent.replace("contentId","contentId"+index));
                var myChart = echarts.init($("#contentId"+index)[0]);
                charts.push(myChart);
                myChart.setOption(element);
            });
        }
    });

  function loop(){
    $.ajax({
        url: url,
        type: 'GET',
        dataType: 'JSONP',
        jsonp: 'callback',
        success: function (data) {
            data.forEach( function(element, index) {
                charts[index].setOption(element);
            });
        }
    });
  }
  setInterval("loop()", 5000);
</script>
</body>
</html>
```
