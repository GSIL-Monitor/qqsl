package com.hysw.qqsl.cloud.pay.entity;

import com.hysw.qqsl.cloud.CommonEnum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 套餐模板
 */
public class PackageModel implements Serializable{
    /** 套餐名 */
    private  String name;
    /** 套餐类型 */
    private CommonEnum.PackageType type;
    /** 描述 */
    private String description;
    /** 套餐价格 */
    private long price;
    /** 套餐等级 */
    private int level;
    /** 套餐项目 */
    private List<PackageItem> packageItems=new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CommonEnum.PackageType getType() {
        return type;
    }

    public void setType(CommonEnum.PackageType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<PackageItem> getPackageItems() {
        return packageItems;
    }

    public void setPackageItems(List<PackageItem> packageItems) {
        this.packageItems = packageItems;
    }
}
