package io.github.zjay.plugin.fastrequest.model;

public class JmhResultEntity {

    private double[][] data;

    private double avg;

    public double[][] getData() {
        return data;
    }

    public void setData(double[][] data) {
        this.data = data;
    }

    public double getAvg() {
        return avg;
    }

    public void setAvg(double avg) {
        this.avg = avg;
    }
}
