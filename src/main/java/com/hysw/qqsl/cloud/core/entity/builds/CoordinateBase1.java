package com.hysw.qqsl.cloud.core.entity.builds;

/**
 * @author Administrator
 * @since 2018/8/16
 */
public class CoordinateBase1 {
    private TaggedObject lon;
    private TaggedObject lat;
    private TaggedObject ele;
    private TaggedObject type;
    private TaggedObject description;
    private Integer num;

    public TaggedObject getLon() {
        return lon;
    }

    public void setLon(String lon) {
        TaggedObject taggedObject = new TaggedObject();
        taggedObject.setValue(lon);
        this.lon = taggedObject;
    }

    public void setLonErrorMsgTrue(){
        this.lon.setErrorMsg(true);
    }

    public TaggedObject getLat() {
        return lat;
    }

    public void setLat(String lat) {
        TaggedObject taggedObject = new TaggedObject();
        taggedObject.setValue(lat);
        this.lat = taggedObject;
    }

    public void setLatErrorMsgTrue(){
        this.lat.setErrorMsg(true);
    }

    public TaggedObject getEle() {
        return ele;
    }

    public void setEle(String ele) {
        TaggedObject taggedObject = new TaggedObject();
        taggedObject.setValue(ele);
        this.ele = taggedObject;
    }

    public void setEleErrorMsgTrue(){
        this.ele.setErrorMsg(true);
    }

    public TaggedObject getType() {
        return type;
    }

    public void setType(String type) {
        TaggedObject taggedObject = new TaggedObject();
        taggedObject.setValue(type);
        this.type = taggedObject;
    }

    public void setTypeErrorMsgTrue(){
        this.type.setErrorMsg(true);
    }

    public TaggedObject getDescription() {
        return description;
    }

    public void setDescription(String description) {
        TaggedObject taggedObject = new TaggedObject();
        taggedObject.setValue(description);
        this.description = taggedObject;
    }

    public void setDescriptionErrorMsgTrue(){
        this.description.setErrorMsg(true);
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }
}
