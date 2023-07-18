package io.github.zjay.plugin.fastrequest.util;

import io.github.zjay.plugin.fastrequest.jmh.JMHTest;
import io.github.zjay.plugin.fastrequest.model.JmhResultEntity;
import org.apache.commons.collections.CollectionUtils;
import org.openjdk.jmh.results.IterationResult;
import org.openjdk.jmh.results.RunResult;

import java.util.*;

public class PressureUtils {

    public static JmhResultEntity jmhTest(){
        JmhResultEntity jmhResultEntity = new JmhResultEntity();
        Collection<RunResult> runResults = JMHTest.jmhTest();
        if(CollectionUtils.isNotEmpty(runResults)){
            for (RunResult runResult : runResults) {
                jmhResultEntity.setAvg((long)runResult.getPrimaryResult().getScore());
                int size = runResult.getAggregatedResult().getIterationResults().size();
                double[] x = new double[size];
                double[] y = new double[size];
                int i = 0;
                for (IterationResult iterationResult : runResult.getAggregatedResult().getIterationResults()) {
                    x[i] = i + 1;
                    y[i++] = (long)iterationResult.getPrimaryResult().getScore();
                }
                jmhResultEntity.setData(new double[][]{x, y});
            }
        }
        return jmhResultEntity;
    }


}
