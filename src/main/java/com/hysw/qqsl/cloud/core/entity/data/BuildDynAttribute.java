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

}
