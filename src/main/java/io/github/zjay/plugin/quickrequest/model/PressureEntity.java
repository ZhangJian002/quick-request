package io.github.zjay.plugin.quickrequest.model;

import java.math.BigDecimal;
import java.util.Map;

public class PressureEntity {

    private Long maxTime;

    private Long minTime;

    private Long avgTime;

    private Long allTime;

    private BigDecimal qps;

    private Long errors;

    private Long corrects;

    private BigDecimal errorRate = BigDecimal.ZERO;

    private BigDecimal correctRate = BigDecimal.ZERO;

    private Map<String, Long> errorDetailMap;

    public Long getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(Long maxTime) {
        this.maxTime = maxTime;
    }

    public Long getMinTime() {
        return minTime;
    }

    public void setMinTime(Long minTime) {
        this.minTime = minTime;
    }

    public Long getAvgTime() {
        return avgTime;
    }

    public void setAvgTime(Long avgTime) {
        this.avgTime = avgTime;
    }

    public Long getAllTime() {
        return allTime;
    }

    public void setAllTime(Long allTime) {
        this.allTime = allTime;
    }

    public BigDecimal getQps() {
        return qps;
    }

    public void setQps(BigDecimal qps) {
        this.qps = qps;
    }

    public Long getErrors() {
        return errors;
    }

    public void setErrors(Long errors) {
        this.errors = errors;
    }

    public Long getCorrects() {
        return corrects;
    }

    public void setCorrects(Long corrects) {
        this.corrects = corrects;
    }

    public BigDecimal getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(BigDecimal errorRate) {
        this.errorRate = errorRate;
    }

    public BigDecimal getCorrectRate() {
        return correctRate;
    }

    public void setCorrectRate(BigDecimal correctRate) {
        this.correctRate = correctRate;
    }

    public Map<String, Long> getErrorDetailMap() {
        return errorDetailMap;
    }

    public void setErrorDetailMap(Map<String, Long> errorDetailMap) {
        this.errorDetailMap = errorDetailMap;
    }

}
