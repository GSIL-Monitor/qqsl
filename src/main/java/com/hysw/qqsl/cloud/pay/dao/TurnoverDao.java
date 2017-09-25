package com.hysw.qqsl.cloud.pay.dao;

import com.hysw.qqsl.cloud.core.dao.BaseDao;
import com.hysw.qqsl.cloud.pay.entity.data.Turnover;
import org.springframework.stereotype.Repository;

/**
 * 流水dao
 */
@Repository("turnoverDao")
public class TurnoverDao extends BaseDao<Turnover,Long> {
}
