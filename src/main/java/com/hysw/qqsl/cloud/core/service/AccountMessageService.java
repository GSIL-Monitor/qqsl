package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.dao.AccountMessageDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.data.*;
import com.hysw.qqsl.cloud.core.entity.project.CooperateVisit;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Create by leinuo on 17-5-16 下午3:43
 *
 * qq:1321404703 https://github.com/leinuo2016
 */
@Service("accountMessageService")
public class AccountMessageService extends BaseService<AccountMessage,Long> {

    @Autowired
    private AccountMessageDao accountMessageDao;
    @Autowired
    private UserService userService;
    @Autowired
    private PollingService pollingService;
    @Autowired
    private AccountService accountService;
    @Autowired
    public void setBaseDao(AccountMessageDao accountMessageDao){
        super.setBaseDao(accountMessageDao);
    }

    /**
     * 子账号登录查询账号消息
     * @param account
     * @return
     */
    public List<AccountMessage> getMessage(Account account) {
        Date newDate=new Date();
        Calendar calendar = Calendar.getInstance();  //得到日历
        calendar.setTime(newDate);//把当前时间赋给日历
        calendar.add(Calendar.DAY_OF_MONTH, -30);  //设置为前一天
        Date dBefore = calendar.getTime();   //得到前一天的时间
        List<Filter> filters1 = new ArrayList<Filter>();
        List<Filter> filters2 = new ArrayList<Filter>();
        filters1.add(Filter.eq("account", account.getId()));
        filters2.add(Filter.eq("account", account.getId()));
        filters1.add(Filter.in("status", CommonEnum.MessageStatus.UNREAD));
        filters2.add(Filter.between("createDate", dBefore, newDate));
        List<AccountMessage> accountMessages= accountMessageDao.findList(0, null, filters1,filters2);
        pollingService.changeMessageStatus(account,false);
        return accountMessages;
    }


    /**
     * 记录查看权限的分享消息
     * @param project
     * @param account
     */
    public void viewMessage(Project project, Account account,boolean isView) {
        AccountMessage accountMessage = new AccountMessage();
        String cooperateCode = isView?"0":"1";
        JSONObject contentJson = new JSONObject();
        contentJson.put("cooperateCode",cooperateCode);
        contentJson.put("nickName",userService.nickName(project.getUser().getId()));
        contentJson.put("projectName",project.getName());
        contentJson.put("projectId",project.getId());
        accountMessage.setAccount(account);
        accountMessage.setStatus(CommonEnum.MessageStatus.UNREAD);
        accountMessage.setContent(contentJson.toString());
        accountMessage.setAccount(account);
        accountMessage.setType(AccountMessage.Type.COOPERATE_PROJECT);
        accountMessageDao.save(accountMessage);
//        pollingService.changeMessageStatus(account,true);
        pollingService.changeShareStatus(account,true);
    }

    /**
     * 记录子账号编辑权限状态的消息
     * @param type
     * @param project
     * @param account
     */
    public void cooperate(CooperateVisit.Type type, Project project, Account account,boolean isCooperate) {
        AccountMessage accountMessage = new AccountMessage();
        String cooperateCode = isCooperate?"2":"3";
        JSONObject contentJson = new JSONObject();
        contentJson.put("cooperateCode",cooperateCode);
        contentJson.put("nickName",userService.nickName(project.getUser().getId()));
        contentJson.put("projectName",project.getName());
        contentJson.put("projectId",project.getId());
        contentJson.put("cooperateInfo",covert(type));
        accountMessage.setAccount(account);
        accountMessage.setStatus(CommonEnum.MessageStatus.UNREAD);
        accountMessage.setContent(contentJson.toString());
        accountMessage.setType(AccountMessage.Type.COOPERATE_PROJECT);
        accountMessageDao.save(accountMessage);
//        pollingService.changeMessageStatus(account,true);
        pollingService.changeCooperateStatus(account,true);
    }

    /**
     * type转字符串
     * @param type
     * @return
     */
    private String covert(CooperateVisit.Type type) {
        for (CooperateVisit.Type type1 : CooperateVisit.Type.values()) {
            if(type1.name().equals(type.toString())){
                return type1.getTypeC();
            }
        }
        return null;
    }

    public List<AccountMessage> findByAccount(Account account) {
        List<Filter> filters = new ArrayList<Filter>();
        filters.add(Filter.eq("account", account.getId()));
        List<AccountMessage> accountMessages = accountMessageDao.findList(0, null, filters);
        return accountMessages;
    }

    public void feedbackMessage(Feedback feedback) {
        AccountMessage accountMessage = new AccountMessage();
        Account account = accountService.find(feedback.getAccountId());
        if (account == null) {
            return;
        }
        accountMessage.setAccount(account);
        accountMessage.setContent("尊敬的水利云用户您好，关于您反馈的消息，管理员已经对您提出的问题进行了回复。");
        accountMessage.setStatus(CommonEnum.MessageStatus.UNREAD);
        accountMessage.setType(AccountMessage.Type.FEEDBACK);
        accountMessageDao.save(accountMessage);
        pollingService.changeMessageStatus(account,true);
    }
}
