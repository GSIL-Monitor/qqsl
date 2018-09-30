package com.hysw.qqsl.cloud.core.entity.buildModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @since 2018/8/20
 */
public class Msg {
    private String fileName;
    private String sheetName;
    private String noticeStr;
    private List<ErrorMsgAndRow> errorMsgAndRows = new ArrayList<>();

    public Msg(String fileName, String sheetName, String noticeStr) {
        this.fileName = fileName;
        this.sheetName = sheetName;
        this.noticeStr = noticeStr;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public String getNoticeStr() {
        return noticeStr;
    }

    public void setNoticeStr(String noticeStr) {
        this.noticeStr = noticeStr;
    }

    public List<ErrorMsgAndRow> getErrorMsgAndRows() {
        return errorMsgAndRows;
    }

    public void setErrorMsgAndRows(String key, Integer value) {
        ErrorMsgAndRow errorMsgAndRow = new ErrorMsgAndRow(key, value);
        this.errorMsgAndRows.add(errorMsgAndRow);
    }

    public class ErrorMsgAndRow {
        private String errorMsg;
        private Integer row;

        public ErrorMsgAndRow(String errorMsg, Integer row) {
            this.errorMsg = errorMsg;
            this.row = row;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        public void setErrorMsg(String errorMsg) {
            this.errorMsg = errorMsg;
        }

        public Integer getRow() {
            return row;
        }

        public void setRow(Integer row) {
            this.row = row;
        }
    }
}

