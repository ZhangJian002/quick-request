package io.github.zjay.plugin.quickrequest.jmh;

import com.intellij.ui.JBColor;
import io.github.zjay.plugin.quickrequest.config.FastRequestComponent;
import io.github.zjay.plugin.quickrequest.jfree.MyStandardChartTheme;
import io.github.zjay.plugin.quickrequest.model.FastRequestConfiguration;
import io.github.zjay.plugin.quickrequest.model.JmhResultEntity;
import io.github.zjay.plugin.quickrequest.util.OkHttp3Util;
import io.github.zjay.plugin.quickrequest.util.ToolWindowUtil;
import io.github.zjay.plugin.quickrequest.view.FastRequestToolWindow;
import io.github.zjay.plugin.quickrequest.view.inner.JmhTestErrorView;
import okhttp3.Request;
import okhttp3.Response;
import org.jfree.chart.*;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.*;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.labels.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@Fork(0)
public class JMHTest {

    public final static List<String> exceptions = Collections.synchronizedList(new ArrayList<>());


    private Request httpRequest;

    @Setup(Level.Iteration)
    public void setUp() {
        FastRequestToolWindow fastRequestToolWindow = ToolWindowUtil.getFastRequestToolWindow(FastRequestToolWindow.project);
        this.httpRequest = fastRequestToolWindow.buildOkHttpRequest();
    }

    @Benchmark
    public int sendRequest() {

        try {
            Response response = OkHttp3Util.getClientInstance().newCall(httpRequest).execute();
            response.close();
            if (response.code() != 200) {
                throw new RuntimeException("请求失败：" + response.body().string());
            }
            return response.code();
        } catch (Exception e) {
            exceptions.add(e.getMessage());
        }
        return -1;
    }

    public static Collection<RunResult> jmhTest() {
        exceptions.clear();
        //初始话客户端
        OkHttp3Util.getClientInstance();
        FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
        assert config != null;
        final Options opts = new
                OptionsBuilder().include(JMHTest.class.getSimpleName())
                //统计结果时间单位
                .timeUnit(TimeUnit.SECONDS)
                //按吞吐量、平均时间等等模型统计
                .mode(Mode.Throughput)
                //开启线程数
                .threads(config.getThreads() == null ? 50 : config.getThreads())
                //压多少次 根据次数平均值取最终结果
                .measurementIterations(config.getTestCount() == null ? 5 : config.getTestCount())
                .measurementTime(TimeValue.seconds(20))
                //热身几次
                .warmupIterations(0)
                .build();
        try {
            Collection<RunResult> result = new Runner(opts).run();
            //释放
            if(OkHttp3Util.clientInstance != null){
                OkHttp3Util.clientInstance.connectionPool().evictAll();
                OkHttp3Util.clientInstance = null;
            }
            return result;
        } catch (RunnerException e) {


        }
        return null;
    }


    public static ChartPanel pain(JmhResultEntity jmhResultEntity) {
        // 创建数据集
        DefaultXYDataset dataset = new DefaultXYDataset();
        dataset.addSeries("Throughput", jmhResultEntity.getData());
        // 创建图表
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Throughput",  // 图表标题
                "",           // x 轴标签
                "opts/s",           // y 轴标签
                dataset,       // 数据集
                PlotOrientation.VERTICAL,  // 图表方向
                true,          // 是否显示图例
                true,          // 是否生成工具提示
                true          // 是否生成 URL 链接
        );
        MyStandardChartTheme darknessTheme = (MyStandardChartTheme) MyStandardChartTheme.createDarknessTheme();
        darknessTheme.apply(chart);
        ChartPanel chartPanel = new ChartPanel(chart);
        XYPlot xyPlot = chart.getXYPlot();
        xyPlot.getRenderer().setSeriesPaint(0, JBColor.GREEN);
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) xyPlot.getRenderer();
        renderer.setDefaultShapesVisible(true);
        renderer.setDefaultShapesFilled(true);
        renderer.setDrawOutlines(true);
        renderer.setUseFillPaint(true);
        renderer.setDefaultItemLabelGenerator(new CustomLabelGenerator());
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelPaint(JBColor.BLACK);
        renderer.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.BOTTOM_CENTER));

        // 添加平均值线
        XYLineAnnotation averageLine = new XYLineAnnotation(
                xyPlot.getDomainAxis().getLowerBound(), jmhResultEntity.getAvg(),
                xyPlot.getDomainAxis().getUpperBound(), jmhResultEntity.getAvg(),
                new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                        10.0f, new float[]{5.0f}, 0.0f),
                JBColor.YELLOW
        );
        xyPlot.addAnnotation(averageLine);
        // 设置绘图区域的边界空白

        // 添加平均值文本注释
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        XYTextAnnotation averageText = new XYTextAnnotation(
                "Avg: " + decimalFormat.format(jmhResultEntity.getAvg()),
                xyPlot.getDomainAxis().getLowerBound(), jmhResultEntity.getAvg()
        );
        averageText.setPaint(JBColor.BLACK);
        averageText.setFont(new Font("SansSerif", Font.PLAIN, 13));
        averageText.setTextAnchor(TextAnchor.BOTTOM_LEFT);
        xyPlot.addAnnotation(averageText);

        NumberAxis numberAxis = (NumberAxis) xyPlot.getDomainAxis();
        numberAxis.setTickUnit(new NumberTickUnit(1)); // 设置刻度单位

        LegendItemCollection legendItems = new LegendItemCollection();

        legendItems.add(new LegendItem(exceptions.size() + " error(s)",JBColor.RED));
        xyPlot.setFixedLegendItems(legendItems);
        chartPanel.addChartMouseListener(new ChartMouseListener() {
            @Override
            public void chartMouseClicked(ChartMouseEvent event) {
                if (event.getEntity() instanceof LegendItemEntity) {
                    if(exceptions.size() > 0){
                        JmhTestErrorView dialog = new JmhTestErrorView(exceptions);
                        dialog.show();
                    }
                }
            }

            @Override
            public void chartMouseMoved(ChartMouseEvent event) {
                // 处理鼠标移动事件
            }
        });

        return chartPanel;
    }


    static class CustomLabelGenerator implements XYItemLabelGenerator {


        @Override
        public String generateLabel(XYDataset dataset, int series, int item) {
            return dataset.getYValue(series, item) + "";
        }
    }


//    public static void main(String[] args) {
//        double[] xData = {1, 2, 3, 4, 5};
//        double[] yData = {10, 20, 15, 25, 18};
//        double[][] data = {xData, yData};
//        pain(data);
//
//    }

}
