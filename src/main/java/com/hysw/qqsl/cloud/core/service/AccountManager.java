package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @anthor Administrator
 * @since 17:29 2018/4/18
 */
@Service("accountManager")
public class AccountManager{
    @Autowired
    private AccountService accountService;
    @Autowired
    private UserMessageService userMessageService;
    @Autowired
    private UserService userService;
    private Map<String, List<Account>> map = new LinkedHashMap<>();

    /**
     * 服务器启动时添加所有未确认子账户
     */
    public void init(){
        List<Account> accounts=accountService.findAllAwaiting();
        for (Account account : accounts) {
            add(account);
        }
    }

    public void add(Account account) {
        List<Account> accounts = map.get(account.getPhone());
        if (accounts == null || accounts.size() == 0) {
            accounts = new ArrayList<>();
        }
        accounts.add(account);
        map.put(account.getPhone(), accounts);
    }

    /**
     * 检查超过24小时未确认的，并删除,同时通知用户
     */
    public void changeExpiredAndDelete(){
        if (map.size() == 0) {
            return;
        }
        for (Map.Entry<String, List<Account>> entry : map.entrySet()) {
            Iterator<Account> iterator = entry.getValue().iterator();
            while (iterator.hasNext()) {
                Account account = iterator.next();
                if (account.getCreateDate().getTime() + 24 * 60 * 60 * 1000l < System.currentTimeMillis()) {
                    userMessageService.accountMessageExpired(account);
                    User user = userService.find(account.getUser().getId());
                    List<Account> accounts = user.getAccounts();
                    for (Account account1 : accounts) {
                        if (account1.getId().equals(account.getId())) {
                            accounts.remove(account1);
                            break;
                        }
                    }
                    iterator.remove();
                    delete(account);
                    List<Account> accounts1 = new ArrayList<>();
                    accounts1.addAll(accounts);
                    user.setAccounts(accounts1);
                    accountService.remove(account);
                    userService.save(user);
                }
            }
        }
    }

    public void delete(Account account) {
        if (account.getStatus() != Account.Status.AWAITING) {
            return;
        }
        List<Account> accounts = map.get(account.getPhone());
        accounts.remove(account);
        map.put(account.getPhone(), accounts);
    }

}
