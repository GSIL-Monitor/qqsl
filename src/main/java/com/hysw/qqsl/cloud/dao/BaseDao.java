package com.hysw.qqsl.cloud.dao;

import com.hysw.qqsl.cloud.entity.Filter;
import com.hysw.qqsl.cloud.entity.Filter.Operator;
import com.hysw.qqsl.cloud.entity.data.BaseEntity;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

/**
 * 
 * Dao基类
 * 
 * @since 2015年8月12日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
public abstract class BaseDao<T extends BaseEntity, Id extends Serializable> {

	/** 实体类类型 */
	public Class<T> entityClass;
	/** JPA操作类型，该对象由spring管理 */
	@PersistenceContext
	protected EntityManager entityManager;

	@SuppressWarnings("unchecked")
	public BaseDao() {
		Type type = getClass().getGenericSuperclass();
		Type[] parameterizedType = ((ParameterizedType) type)
				.getActualTypeArguments();
		entityClass = (Class<T>) parameterizedType[0];
	}

	/**
	 * 查找实体对象
	 * 
	 * @param id
	 *            Id
	 * @return 实体对象，若不存在返回null
	 */
	public T find(Id id) {
		Assert.notNull(id);
		return entityManager.find(entityClass, id);
	}

	/**
	 * 保存实体对象
	 * 
	 * @param entity
	 *            实体对象
	 */
	public void save(T entity) {
		Assert.notNull(entity);
		if (entity.getId() == null) {
			entityManager.persist(entity);
		} else {
			entityManager.merge(entity);
		}
	}

	/**
	 * 删除实体对象
	 * 
	 * @param entity
	 *            实体对象
	 */
	public void remove(T entity) {
		Assert.notNull(entity);
		entityManager.remove(entityManager.merge(entity));
	}

	/**
	 * 立刻更新数据库
	 */
	public void flush(){
		entityManager.flush();
	}
	
	/**
	 * 查找实体对象集合(仅且关系)
	 * 
	 * @param first
	 *            起始记录
	 * @param count
	 *            数量
	 * @param filters
	 *            筛选
	 * @return 实体对象集合
	 */
	public List<T> findList(Integer first, Integer count, List<Filter> filters) {
		// CriteriaBuilder是一个工厂对象,用于构建JPA安全查询
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = criteriaBuilder
				.createQuery(entityClass);
		criteriaQuery.select(criteriaQuery.from(entityClass));
		// 加入条件
		addRestrictions(criteriaQuery, filters,null);
		// TypedQuery执行查询与获取元模型实例
		TypedQuery<T> query = entityManager.createQuery(criteriaQuery)
				.setFlushMode(FlushModeType.COMMIT);
		if (first != null) {
			query.setFirstResult(first);
		}
		if (count != null) {
			query.setMaxResults(count);
		}
		return query.getResultList();
	}


	/**
	 * 加入条件
	 * @param criteriaQuery
	 * @param filters
	 */
	@SuppressWarnings("unused")
	private void addRestrictions(CriteriaQuery<T> criteriaQuery,
			List<Filter> filters) {
		if (criteriaQuery==null || filters==null || filters.isEmpty()) {
			return;
		}
		Root<T> root = getRoot(criteriaQuery);
		// 实体的查询根对象
		if (root==null) {
			return;
		}
		// CriteriaBuilder是一个工厂对象,用于构建JPA安全查询
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		// Predicate 过滤条件
		Predicate restrictions = criteriaQuery.getRestriction()!=null ? criteriaQuery.getRestriction() : criteriaBuilder.conjunction();
		restrictions = filterToRestriction(restrictions, filters, criteriaBuilder, root);
		criteriaQuery.where(restrictions);
	}

	/**
	 * 取得查询根对象
	 * @param criteriaQuery
	 * @return
	 */
	private Root<T> getRoot(CriteriaQuery<T> criteriaQuery) {
		if (criteriaQuery == null || criteriaQuery.getRoots() == null
				|| criteriaQuery.getResultType() == null) {
			return null;
		}
		Class<T> clazz = criteriaQuery.getResultType();
		for (Root<?> root : criteriaQuery.getRoots()) {
			if (clazz.equals(root.getJavaType())) {
				return (Root<T>) root.as(clazz);
			}
		}
		return null;
	}
	
	/**
	 * 加入条件
	 * @param criteriaQuery
	 * @param filters2
	 */
	private void addRestrictions(CriteriaQuery<T> criteriaQuery,
			List<Filter> filters1,List<Filter> filters2) {
		if (criteriaQuery==null || filters1==null || filters1.isEmpty()) {
			return;
		}
		Root<T> root = getRoot(criteriaQuery);
		// 实体的查询根对象
		if (root==null) {
			return;
		}
		// CriteriaBuilder是一个工厂对象,用于构建JPA安全查询
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		// Predicate 过滤条件
		Predicate restrictions = criteriaQuery.getRestriction()!=null ? criteriaQuery.getRestriction() : criteriaBuilder.conjunction();
		Predicate restrictions1 = criteriaQuery.getRestriction()!=null ? criteriaQuery.getRestriction() : criteriaBuilder.conjunction();
		Predicate restrictions2 = criteriaQuery.getRestriction()!=null ? criteriaQuery.getRestriction() : criteriaBuilder.conjunction();
		restrictions1 = filterToRestriction(restrictions1,filters1,criteriaBuilder,root);
		if(filters2!=null && !filters2.isEmpty()){
			restrictions2 = filterToRestriction(restrictions2,filters2,criteriaBuilder,root);
			restrictions = criteriaBuilder.or(restrictions1, restrictions2);		
		}else{
			restrictions =restrictions1;
		}
		criteriaQuery.where(restrictions);
	}
	
	/**
	 * 过滤方法
	 * @param restrictions
	 * 				过滤条件
	 * @param filters
	 * 				筛选
	 * @param criteriaBuilder
	 * 				工厂对象
	 * @param root
	 * 				根对象
	 * @return
	 * 				过滤条件
	 */
	private Predicate filterToRestriction(Predicate restrictions, List<Filter> filters, CriteriaBuilder criteriaBuilder, Root<T> root) {
		for (Filter filter : filters) {
			if (filter==null || StringUtils.isEmpty(filter.getProperty())) {
				continue;
			}
			if (filter.getOperator()==Operator.eq) {
				restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get(filter.getProperty()), filter.getValue()));
			}
			if (filter.getOperator()==Operator.ne) {
				restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.notEqual(root.get(filter.getProperty()), filter.getValue()));
			}
			if (filter.getOperator()==Operator.gt) {
				restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.gt(root.<Number>get(filter.getProperty()), (Number)filter.getValue()));
			}
			if (filter.getOperator()==Operator.lt) {
				restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.lt(root.<Number>get(filter.getProperty()), (Number)filter.getValue()));
			}
			if (filter.getOperator()==Operator.ge) {
				restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.ge(root.<Number>get(filter.getProperty()), (Number)filter.getValue()));
			}
			if (filter.getOperator()==Operator.le) {
				restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.le(root.<Number>get(filter.getProperty()), (Number)filter.getValue()));
			}
			if (filter.getOperator()==Operator.like) {
				restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.like(root.<String>get(filter.getProperty()), (String)filter.getValue()));
			}
			if (filter.getOperator()==Operator.in) {
				restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.in(root.get(filter.getProperty())).value(filter.getValue()));
			}
			if (filter.getOperator()==Operator.isNull) {
				restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.isNull(root.get(filter.getProperty())));
			}
			if (filter.getOperator()==Operator.isNotNull) {
				restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.isNotNull(root.get(filter.getProperty())));
			}
			if (filter.getOperator()==Operator.between) {
				restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.between(root.<Date>get(filter.getProperty()),(Date)filter.getValue(),(Date)filter.getValue1()));
			}
		}
		return restrictions;
	}

	/**
	 * 查找实体对象集合(包含或关系)
	 * 
	 * @param first
	 *            起始记录
	 * @param count
	 *            数量
	 * @param filters1
	 *            筛选
	 * @param filters1
	 *            排序
	 * @return 实体对象集合
	 */
	public List<T> findList(Integer first, Integer count, List<Filter> filters1,List<Filter> filters2) {
		// CriteriaBuilder是一个工厂对象,用于构建JPA安全查询
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = criteriaBuilder
				.createQuery(entityClass);
		criteriaQuery.select(criteriaQuery.from(entityClass));
		// 加入条件
		addRestrictions(criteriaQuery, filters1,filters2);
		// TypedQuery执行查询与获取元模型实例
		TypedQuery<T> query = entityManager.createQuery(criteriaQuery)
				.setFlushMode(FlushModeType.COMMIT);
		if (first != null) {
			query.setFirstResult(first);
		}
		if (count != null) {
			query.setMaxResults(count);
		}
		return query.getResultList();
	}
	
	
	
}
