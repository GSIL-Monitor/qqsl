package com.hysw.qqsl.cloud.core.entity.builds;

/**
 * @author Administrator
 * @since 2018/8/16
 */
public class TaggedObject {
    private String value;
    private Boolean errorMsg = false;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(Boolean errorMsg) {
        this.errorMsg = errorMsg;
    }
}
