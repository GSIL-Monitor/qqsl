package com.hysw.qqsl.cloud.core.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * @author Administrator
 * @since 2018/12/25
 */
@Entity
@Table(name = "buildDynAttribute")
@SequenceGenerator(name = "sequenceGenerator", sequenceName = "buildDynAttribute_sequence")
@JsonIgnoreProperties(value = {"hibernateLazyInitializer"})
public class BuildDynAttribute extends BaseEntity {
    private String alias;
    private String groupAlias;
    private int code;
    private String value;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getGroupAlias() {
        return groupAlias;
    }

    public void setGroupAlias(String groupAlias) {
        this.groupAlias = groupAlias;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
