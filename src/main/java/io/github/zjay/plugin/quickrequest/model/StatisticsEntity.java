package io.github.zjay.plugin.quickrequest.model;

public class StatisticsEntity {

    private Long time;

    private Boolean isError;

    private String errorMsg;

    public StatisticsEntity(){
        this(0L, false, "");
    }

    public StatisticsEntity(Long time, boolean isError, String errorMsg){
        this.time = time;
        this.isError = isError;
        this.errorMsg = errorMsg;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public boolean isError() {
        return isError;
    }

    public void setError(Boolean error) {
        isError = error;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
