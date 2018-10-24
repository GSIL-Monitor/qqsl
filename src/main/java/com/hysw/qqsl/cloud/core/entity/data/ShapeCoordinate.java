package com.hysw.qqsl.cloud.core.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hysw.qqsl.cloud.core.entity.buildModel.Elevation;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Administrator
 * @since 2018/9/20
 */
@Entity
@Table(name="shapeCoordinate")
@SequenceGenerator(name="sequenceGenerator", sequenceName="shapeCoordinate_sequence")
@JsonIgnoreProperties(value={"hibernateLazyInitializer"})
public class ShapeCoordinate extends BaseEntity {
    private String lon;
    private String lat;
    private String elevations;
    private List<Elevation> elevationList = new LinkedList<>();
    private Shape shape;
    private int cellNum;
    private boolean errorMsg;
    private Build build;

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getElevations() {
        return elevations;
    }

    public void setElevations(String elevations) {
        this.elevations = elevations;
    }

    public void setElevation(Elevation elevation) {
        JSONArray jsonArray;
        if (this.elevations == null) {
            jsonArray = new JSONArray();
        } else {
            jsonArray = JSONArray.fromObject(this.elevations);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("alias", elevation.getAlias());
        jsonObject.put("ele", elevation.getEle());
        jsonArray.add(jsonObject);
        this.elevations = jsonArray.toString();
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }

    @Transient
    public int getCellNum() {
        return cellNum;
    }

    public void setCellNum(int cellNum) {
        this.cellNum = cellNum;
    }

    @Transient
    public List<Elevation> getElevationList() {
        return elevationList;
    }

    public void setElevationList(Elevation elevation) {
        this.elevationList.add(elevation);
    }

    @Transient
    public boolean isErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(boolean errorMsg) {
        this.errorMsg = errorMsg;
    }

    @OneToOne(mappedBy = "shapeCoordinate")
    public Build getBuild() {
        return build;
    }

    public void setBuild(Build build) {
        this.build = build;
    }
}
