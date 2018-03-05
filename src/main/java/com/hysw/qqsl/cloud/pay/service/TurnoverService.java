package com.hysw.qqsl.cloud.pay.service;

import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.service.BaseService;
import com.hysw.qqsl.cloud.core.service.MessageService;
import com.hysw.qqsl.cloud.pay.dao.TurnoverDao;
import com.hysw.qqsl.cloud.pay.entity.data.Trade;
import com.hysw.qqsl.cloud.pay.entity.data.Turnover;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 流水service
 *
 * @author chenl
 * @create 2017-08-21 下午5:02
 */
@Service("turnoverService")
public class TurnoverService extends BaseService<Turnover, Long> {
    @Autowired
    private TurnoverDao turnoverDao;

    @Autowired
    public void setBaseDao(TurnoverDao turnoverDao) {
        super.setBaseDao(turnoverDao);
    }


    /**
     * 增加流水
     * @param trade
     */
    public void writeTurnover(Trade trade) {
        Turnover turnover1=findByLastItem();
        if (turnover1 == null) {
            turnover1 = new Turnover();
        }
        Turnover turnover = null;
        if (trade.getStatus() == Trade.Status.PAY) {
            turnover = new Turnover(String.valueOf(trade.getPayDate().getTime()), trade.getOutTradeNo(), Turnover.Type.PAY, trade.getPrice(),turnover1.getBalance());
        } else if (trade.getStatus() == Trade.Status.REFUND) {
            turnover = new Turnover(String.valueOf(trade.getPayDate().getTime()), trade.getOutTradeNo(), Turnover.Type.REFUND, trade.getPrice(),turnover1.getBalance());
        }
        if (turnover != null) {
            save(turnover);
        }
    }

    /**
     * 查询最后一条数据
     * @return
     */
    protected Turnover findByLastItem() {
        Filter filter = Filter.desc("createDate");
        List<Turnover> list = turnoverDao.findList(filter);
        if (null != list && list.size() != 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 获取两个时间段之间的数据
     * @param begin
     * @param end
     * @return
     */
    public Message getTurnoverListBetweenDate(Date begin, Date end) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.between("createDate", begin, end));
        List<Turnover> list = turnoverDao.findList(0, null, filters);
        return MessageService.message(Message.Type.OK, list);
    }
}
