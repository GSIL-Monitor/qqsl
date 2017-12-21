package com.hysw.qqsl.cloud.pay.entity;

import com.hysw.qqsl.cloud.CommonEnum;

import java.io.Serializable;

public class GoodsModel implements Serializable {
    private static final long serialVersionUID = 4461067742243039479L;
    /** 数据服务类型 */
    private CommonEnum.GoodsType type;
    /** 服务名 */
    private String name;
    /** 服务描述 */
    private String description;
    /** 价格 */
    private double price;

    public CommonEnum.GoodsType getType() {
        return type;
    }

    public void setType(CommonEnum.GoodsType type) {
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
