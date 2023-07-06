package io.github.zjay.plugin.fastrequest.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import io.github.zjay.plugin.fastrequest.config.PressureFunction;
import io.github.zjay.plugin.fastrequest.model.PressureEntity;
import io.github.zjay.plugin.fastrequest.model.StatisticsEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

//import io.gatling.javaapi.core.*;
//import io.gatling.javaapi.http.*;
//import scala.collection.immutable.Seq;
//
//import static io.gatling.javaapi.core.CoreDsl.*;
//import static io.gatling.javaapi.http.HttpDsl.*;


public class PressureUtils {

    private static ThreadPoolExecutor createThreadPool(int count) {
        return new ThreadPoolExecutor(count, count, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1));
    }

    public static PressureEntity beginPressure(int count, PressureFunction pressureFunction) {

        //初始线程池
        ThreadPoolExecutor threadPool = createThreadPool(count);
        try {
            return doPressure(threadPool, count, pressureFunction);
        }catch (Exception e){

        }finally {
            threadPool.shutdown();
        }
        return null;
    }

//    private static class BasicSimulationJava extends Simulation { // 3
//
//        HttpProtocolBuilder httpProtocol = http // 4
//                .baseUrl("https://www.baidu.com") // 5
//                .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // 6
//                .doNotTrackHeader("1")
////                .acceptLanguageHeader("en-US,en;q=0.5")
//                .acceptEncodingHeader("gzip, deflate")
//                .userAgentHeader("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0");
//
//        ScenarioBuilder scn = scenario("BasicSimulation") // 7
//                .exec(http("request_1") // 8
//                        .get("/")) // 9
//                .pause(5); // 10
//
//        public BasicSimulationJava(){
//            scn.injectOpen(atOnceUsers(1)).protocols(httpProtocol);
//        }
//
//    }

    private static PressureEntity doPressure(ThreadPoolExecutor threadPool, int count, PressureFunction pressureFunction) throws Exception {
        //定义同步器
        CountDownLatch countDownLatch = new CountDownLatch(count);
        //存储Future用于获取返回
        List<Future> futureList = new LinkedList<>();
        //定义耗时Map
        List<StatisticsEntity> statisticsEntities = Collections.synchronizedList(new LinkedList<StatisticsEntity>());
        //提交count个任务，并等待最终一起执行，实现并发效果
        pressure(countDownLatch, count, statisticsEntities, futureList, threadPool, pressureFunction);
        //wait
        Long requestTime = waitForGet(countDownLatch, futureList);
        //统计
        return statistics(statisticsEntities, requestTime);
    }

    private static Long waitForGet(CountDownLatch countDownLatch, List<Future> futureList) throws Exception {
        //主线程也等待
        countDownLatch.await();
        long begin = System.currentTimeMillis();
        //同步获取结果
        for (Future future : futureList) {
            future.get();
        }
        return System.currentTimeMillis() - begin;
    }

    private static void pressure(CountDownLatch countDownLatch, int count, List<StatisticsEntity> statisticsEntities, List<Future> futureList, ThreadPoolExecutor threadPool, PressureFunction pressureFunction) {
        for (int i = 0; i < count; i++) {
            futureList.add(threadPool.submit(() -> {
                StatisticsEntity statistics;
                try {
                    //build request
                    HttpRequest httpRequest = pressureFunction.buildRequest();
                    //-1并挺住等待执行
                    countDownLatch.countDown();
                    countDownLatch.await();
                    long begin = System.currentTimeMillis();
                    HttpResponse response = httpRequest.execute();
                    if(response.getStatus() != 200){
                        throw new RuntimeException(response.body());
                    }
                    statistics = new StatisticsEntity(System.currentTimeMillis() - begin, false, "");
                } catch (Exception e) {
                    statistics = new StatisticsEntity(0L, true, e.getMessage());
                }
                statisticsEntities.add(statistics);
            }));
        }
    }

    private static PressureEntity statistics(List<StatisticsEntity> statisticsEntities, Long requestTime) {
        long maxTime = 0;
        long minTime = 0;
        long allTime = 0;
        long errors = 0;
        long corrects = 0;
        List<String> errorMsg = new LinkedList<>();
        for (StatisticsEntity statisticsEntity : statisticsEntities) {
            if(!statisticsEntity.isError()){
                Long time = statisticsEntity.getTime();
                if(time > maxTime){
                    maxTime = time;
                }
                if(minTime == 0){
                    minTime = time;
                }
                if(time < minTime){
                    minTime = time;
                }
                allTime += time;
                corrects++;
            }else {
                errors ++;
                errorMsg.add(statisticsEntity.getErrorMsg());
            }
        }
        PressureEntity pressureEntity = new PressureEntity();
        pressureEntity.setMaxTime(maxTime);
        pressureEntity.setMinTime(minTime);
        pressureEntity.setAllTime(allTime);
        pressureEntity.setErrors(errors);
        pressureEntity.setCorrects(corrects);
        if(statisticsEntities.size() > 0){
            BigDecimal size = new BigDecimal(statisticsEntities.size());
            if(corrects > 0){
                pressureEntity.setAvgTime(allTime/corrects);
                pressureEntity.setQps(new BigDecimal(1000).multiply(new BigDecimal(corrects)).divide(new BigDecimal(requestTime), 2, RoundingMode.HALF_UP));
                pressureEntity.setCorrectRate(new BigDecimal(corrects).divide(size, 3, RoundingMode.HALF_UP));
            }
            if(errors > 0){
                if(pressureEntity.getCorrectRate() != null && pressureEntity.getCorrectRate().compareTo(BigDecimal.ZERO) > 0){
                    pressureEntity.setErrorRate(BigDecimal.ONE.subtract(pressureEntity.getCorrectRate()));
                }else {
                    pressureEntity.setErrorRate(BigDecimal.ONE);
                }
                Map<String, Long> errorDetailMap = errorMsg.stream().collect(Collectors.groupingBy(x->x, Collectors.counting()));
                pressureEntity.setErrorDetailMap(errorDetailMap);
            }
        }
        return pressureEntity;
    }
}
