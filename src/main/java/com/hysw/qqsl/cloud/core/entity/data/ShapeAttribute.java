package com.hysw.qqsl.cloud.core.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * @author Administrator
 * @since 2018/9/13
 */
@Entity
@Table(name="shapeAttribute")
@SequenceGenerator(name="sequenceGenerator", sequenceName="shapeAttribute_sequence")
@JsonIgnoreProperties(value={"hibernateLazyInitializer"})
public class ShapeAttribute extends BaseEntity {

}
