package com.hysw.qqsl.cloud.core.entity.data;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


/**
 * 移动端实体类
 *
 * @since 2017年6月27日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
@Entity
@Table(name="mobileInfo")
@SequenceGenerator(name="sequenceGenerator", sequenceName="mobile_sequence")
@JsonIgnoreProperties(value={"hibernateLazyInitializer"})
public class MobileInfo extends BaseEntity {

    // 版本号
    private Long version;

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
