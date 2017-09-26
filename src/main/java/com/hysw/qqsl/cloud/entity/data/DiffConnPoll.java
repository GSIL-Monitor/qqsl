package com.hysw.qqsl.cloud.entity.data;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Date;

/**
 * 移动端差分连接池实体类
 *
 * @since 2017年04月07日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
@Entity
@Table(name = "diff_conn_poll")
@SequenceGenerator(name = "sequenceGenerator",sequenceName = "diff_conn_poll_sequence")
@JsonIgnoreProperties(value={"hibernateLazyInitializer"})
public class DiffConnPoll extends BaseEntity {

    /** 差分账号 */
    private String userName;
    /** 密码 */
    private String password;
    /** 过期时间 */
    private long timeout;

    public DiffConnPoll(String userName, String password, long timeout) {
        this.userName = userName;
        this.password = password;
        this.timeout = timeout;
    }

    public DiffConnPoll() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
