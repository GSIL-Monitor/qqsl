package com.hysw.qqsl.cloud.service;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.dao.AccountMessageDao;
import com.hysw.qqsl.cloud.entity.Filter;
import com.hysw.qqsl.cloud.entity.data.Account;
import com.hysw.qqsl.cloud.entity.data.AccountMessage;
import com.hysw.qqsl.cloud.entity.data.Project;
import com.hysw.qqsl.cloud.entity.data.User;
import com.hysw.qqsl.cloud.entity.project.CooperateVisit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    private CooperateService cooperateService;
    @Autowired
    public void setBaseDao(AccountMessageDao accountMessageDao){
        super.setBaseDao(accountMessageDao);
    }

    /**
     * 记录企业解绑子账号的消息
     * @param user
     */
    public void bindMsessage(User user, Account account,boolean isBind) {
        AccountMessage accountMessage = new AccountMessage();
        accountMessage.setAccount(account);
        String content;
        if(isBind){
            content = "尊敬的用户，您好！您已经被邀请成为"+user.getName()+"企业的子账号。";
        }else{
            content = "尊敬的用户，您好！手机号为: "+account.getPhone()+" 的子账号已与企业解绑。";
        }
        accountMessage.setContent(content);
        accountMessage.setStatus(AccountMessage.Status.UNREAD);
        accountMessage.setUserId(user.getId());
        accountMessageDao.save(accountMessage);
    }

    /**
     * 子账号登录查询账号消息
     * @param account
     * @return
     */
    public List<AccountMessage> getMessage(Account account) {
        List<Filter> filters = new ArrayList<Filter>();
        filters.add(Filter.eq("account", account.getId()));
        List<AccountMessage> accountMessages = accountMessageDao.findList(0, null, filters);
        return accountMessages;
    }


    /**
     * 记录查看权限的分享消息
     * @param project
     * @param account
     */
    public void viewMessage(Project project, Account account,boolean isView) {
        AccountMessage accountMessage = new AccountMessage();
        String content;
        if(isView) {
            content = "尊敬的用户，您好！"+project.getUser().getName()+"企业已将《"+project.getName()+"》项目的查看权限分配给您，您已获得查看权限。";
        }else{
            content = "尊敬的用户，您好！"+project.getUser().getName()+"企业已将《"+project.getName()+"》项目的查看权限收回。";
        }

        accountMessage.setAccount(account);
        accountMessage.setUserId(project.getUser().getId());
        accountMessage.setStatus(AccountMessage.Status.UNREAD);
        accountMessage.setContent(content);
        accountMessage.setAccount(account);
        accountMessage.setProjectId(project.getId());
        accountMessageDao.save(accountMessage);
    }

    /**
     * 记录子账号编辑权限状态的消息
     * @param type
     * @param project
     * @param account
     */
    public void cooperate(CooperateVisit.Type type, Project project, Account account,boolean isCooperate) {
        AccountMessage accountMessage = new AccountMessage();
        String content;
        if(isCooperate){
            content="尊敬的用户，您好！"+project.getUser().getUserName()+"企业已将《"+project.getName()+"》项目的"+covert(type)+"权限分配给您，您已获得编辑权限，请注意添加内容。";
        }else{
            content="尊敬的用户，您好！"+project.getUser().getUserName()+"企业已将《"+project.getName()+"》项目的"+covert(type)+"权限收回，已不能编辑该项目。";
        }
        accountMessage.setAccount(account);
        accountMessage.setUserId(project.getUser().getId());
        accountMessage.setStatus(AccountMessage.Status.UNREAD);
        accountMessage.setContent(content);
        accountMessage.setAccount(account);
        accountMessage.setProjectId(project.getId());
        accountMessageDao.save(accountMessage);
    }

    /**
     * type转字符串
     * @param type
     * @return
     */
    private String covert(CooperateVisit.Type type) {
        for (int i = 0; i < CommonAttributes.STAGEE.length; i++) {
            if(CommonAttributes.STAGEE[i].equals(type.toString())){
                return CommonAttributes.STAGEC[i];
            }
        }
        return null;
    }
}
