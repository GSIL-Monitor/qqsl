package com.hysw.qqsl.cloud.core.entity.data;

import com.hysw.qqsl.cloud.CommonEnum;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.persistence.*;
import java.util.Date;

/**
 * 服务权限认证
 */
@Entity
@Table(name = "certify")
@SequenceGenerator(name = "sequenceGenerator", sequenceName = "certify_sequence")
@JsonIgnoreProperties(value={"hibernateLazyInitializer"})
public class Certify extends BaseEntity{
    /** 真实姓名 */
    private String name;
    /** 身份证号码 */
    private String identityId;
    /** 审核意见 */
    private String identityAdvice;
    /** 法人姓名 */
    private String legal;
    /** 企业名称 */
    private String companyName;
    /** 企业地址 */
    private String companyAddress;
    /** 企业电话 */
    private String companyPhone;
    /** 企业许可证编号 */
    private String companyLicence;
    /** 审核意见 */
    private String companyAdvice;
    /** 身份证有效期至 */
    private Date validTill;
    /** 营业执照有效期至 */
    private Date validPeriod;
    /** 银行 */
//    private String bankName;
    /** 银行账号 */
//    private String bankAccount;

    private User user;

    /** 认证状态 */
    private CommonEnum.CertifyStatus personalStatus;
    private CommonEnum.CertifyStatus companyStatus;

    public Certify(){

    }

    public Certify(User user) {
        this.user = user;
        this.personalStatus = CommonEnum.CertifyStatus.UNAUTHEN;
        this.companyStatus = CommonEnum.CertifyStatus.UNAUTHEN;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentityId() {
        return identityId;
    }

    public void setIdentityId(String identityId) {
        this.identityId = identityId;
    }

    public String getIdentityAdvice() {
        return identityAdvice;
    }

    public void setIdentityAdvice(String identityAdvice) {
        this.identityAdvice = identityAdvice;
    }

    public String getLegal() {
        return legal;
    }

    public void setLegal(String legal) {
        this.legal = legal;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }

    public String getCompanyPhone() {
        return companyPhone;
    }

    public void setCompanyPhone(String companyPhone) {
        this.companyPhone = companyPhone;
    }

    public String getCompanyLicence() {
        return companyLicence;
    }

    public void setCompanyLicence(String companyLicence) {
        this.companyLicence = companyLicence;
    }

    public String getCompanyAdvice() {
        return companyAdvice;
    }

    public void setCompanyAdvice(String companyAdvice) {
        this.companyAdvice = companyAdvice;
    }

//    public String getBankName() {
//        return bankName;
//    }

//    public void setBankName(String bankName) {
//        this.bankName = bankName;
//    }

//    public String getBankAccount() {
//        return bankAccount;
//    }

//    public void setBankAccount(String bankAccount) {
//        this.bankAccount = bankAccount;
//    }

    @ManyToOne(fetch=FetchType.EAGER)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public CommonEnum.CertifyStatus getPersonalStatus() {
        return personalStatus;
    }

    public void setPersonalStatus(CommonEnum.CertifyStatus personalStatus) {
        this.personalStatus = personalStatus;
    }

    public CommonEnum.CertifyStatus getCompanyStatus() {
        return companyStatus;
    }

    public void setCompanyStatus(CommonEnum.CertifyStatus companyStatus) {
        this.companyStatus = companyStatus;
    }

    public Date getValidTill() {
        return validTill;
    }

    public void setValidTill(Date validTill) {
        this.validTill = validTill;
    }

    public Date getValidPeriod() {
        return validPeriod;
    }

    public void setValidPeriod(Date validPeriod) {
        this.validPeriod = validPeriod;
    }
}
