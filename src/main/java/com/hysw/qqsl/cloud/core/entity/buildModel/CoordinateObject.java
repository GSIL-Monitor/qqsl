package com.hysw.qqsl.cloud.core.entity.buildModel;

import com.hysw.qqsl.cloud.core.entity.data.Project;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @since 2018/8/16
 */
public class CoordinateObject {
    private List<CoordinateBase1> coordinateBase1s;
    private String remark;
    private String name;
    private Boolean errorMsg = false;
    private Project project;
    private Map<Integer, String> errorMsgInfo = new LinkedHashMap<>();
    /** 随机字符串 */
    private String noticeStr;

    public List<CoordinateBase1> getCoordinateBase1s() {
        return coordinateBase1s;
    }

    public void setCoordinateBase1s(List<CoordinateBase1> coordinateBase1s) {
        this.coordinateBase1s = coordinateBase1s;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsgTrue() {
        this.errorMsg = true;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getNoticeStr() {
        return noticeStr;
    }

    public void setNoticeStr(String noticeStr) {
        this.noticeStr = noticeStr;
    }

    public Map<Integer, String> getErrorMsgInfo() {
        return errorMsgInfo;
    }

    public void setErrorMsgInfo(Integer key,String value) {
        this.errorMsgInfo.put(key, value);
    }
}
