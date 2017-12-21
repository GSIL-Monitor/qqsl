package com.hysw.qqsl.cloud.pay.dao;

import com.hysw.qqsl.cloud.core.dao.BaseDao;
import com.hysw.qqsl.cloud.pay.entity.data.Trade;
import org.springframework.stereotype.Repository;

/**
 * 订单dao
 */
@Repository("tradeDao")
public class TradeDao extends BaseDao<Trade,Long>{
}
