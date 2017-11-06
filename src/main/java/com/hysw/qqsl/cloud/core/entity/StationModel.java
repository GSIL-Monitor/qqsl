package com.hysw.qqsl.cloud.core.entity;

import com.hysw.qqsl.cloud.CommonEnum;

import java.io.Serializable;

public class StationModel implements Serializable {
    private CommonEnum.StationType type;
    private String name;
    private String description;
    private double price;


    public CommonEnum.StationType getType() {
        return type;
    }

    public void setType(CommonEnum.StationType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
