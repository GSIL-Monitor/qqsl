package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.dao.AccountDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.Note;
import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.AccountMessage;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Create by leinuo on 17-4-26 下午6:30
 *
 * qq:1321404703 https://github.com/leinuo2016
 */
@Service("accountService")
public class AccountService extends BaseService<Account,Long> {
    @Autowired
    private AccountDao accountDao;
    @Autowired
    private NoteCache noteCache;
    @Autowired
    private UserService userService;
    @Autowired
    private AccountMessageService accountMessageService;
    @Autowired
    private UserMessageService userMessageService;
    @Autowired
    private PollingService pollingService;

    @Autowired
    public void setBaseDao(AccountDao accountDao){
        super.setBaseDao(accountDao);
    }


    public Account getSimpleAccount(Account account){
       Account simpleAccount = new Account();
       simpleAccount.setPhone(account.getPhone());
       simpleAccount.setName(account.getName());
       simpleAccount.setId(account.getId());
       return simpleAccount;
    }
    /**
     * 邀请并注册子账号
     * @param phone
     * @param user
     */
    public JSONObject invite(String phone,User user) {
        Account account = findByPhone(phone);
        String inviteCode = SettingUtils.createRandomVcode();
        String userName = user.getUserName();
        if(user.getName()!=null){
            userName = user.getName();
        }
        if(user.getCompanyName()!=null){
            userName = user.getCompanyName();
        }
        String noteMessage = account==null? "恭喜你已经被邀请成为"+userName+"企业的子账号，初始密码为"+inviteCode+"。请打开www.qingqingshuili.com网页登录并完善个人信息。": "恭喜你已经被邀请成为"+userName+"企业的子账号。";
        Note note = new Note(phone,noteMessage);
        if (account == null) {
            account = new Account();
            account.setPhone(phone);
            account.setPassword(DigestUtils.md5Hex(inviteCode));
            account.setRoles(CommonAttributes.ROLES[4]);
        } else {
            return null;
        }
        //记录子账号邀请消息
        account.setUser(user);
        save(account);
        noteCache.add(phone,note);
        return makeSimpleAccountJson(account);
    }

    /**
     * 根据手机号查询子账号
     * @param phone
     * @return
     */
    public Account findByPhone(String phone) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("phone",phone));
        List<Account> accounts = accountDao.findList(0,null,filters);
        if(accounts.size()==1){
            accounts.get(0).getUser();
            return accounts.get(0);
        }else{
            return null;
        }
    }

    /**
     * 根据email查询子账号
     * @param email
     * @return
     */
    public Account findByEmail(String email) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("email",email));
        List<Account> accounts = accountDao.findList(0,null,filters);
        if(accounts.size()==1){
            accounts.get(0).getUser();
            return accounts.get(0);
        }else{
            return null;
        }
    }

    /**
     * 根据用户名或手机号码查找用户
     * @param argument
     * @return
     */
    public Account findByPhoneOrEmial(String argument){
        Account account = null;
        if(SettingUtils.phoneRegex(argument)){
            account = findByPhone(argument);
        }else if(SettingUtils.emailRegex(argument)){
            account = findByEmail(argument);
        }
        return account;
    }

    /**
     * 根据手机号查询子账号
     * @param userName
     * @return
     */
    private Account findByUserName(String userName){
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("userName",userName));
        List<Account> accounts = accountDao.findList(0,null,filters);
        if(accounts.size()==1){
            accounts.get(0).getUser();
            return accounts.get(0);
        }else{
            return null;
        }

    }
    /**
     * 获取子账号json数据
     * @param account
     * @return
     */
    public JSONObject makeAccountJson(Account account) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id",account.getId());
        jsonObject.put("department",account.getDepartment());
        jsonObject.put("email",account.getEmail());
        jsonObject.put("name",account.getName());
        jsonObject.put("phone",account.getPhone());
        jsonObject.put("userName",account.getUserName());
        jsonObject.put("createDate",account.getCreateDate());
        jsonObject.put("modifyDate",account.getModifyDate());
        User user = getUserByAccountId(account.getId());
        JSONObject userJson = makeUserJson(user);
        List<AccountMessage> accountMessages = accountMessageService.getMessage(account);
        JSONArray messageJsons = new JSONArray();
        JSONObject messageJson;
        for(int i=0;i<accountMessages.size();i++){
            messageJson = new JSONObject();
            messageJson.put("id",accountMessages.get(i).getId());
            messageJson.put("content",accountMessages.get(i).getContent());
            messageJson.put("status",accountMessages.get(i).getStatus());
            messageJson.put("createDate",accountMessages.get(i).getCreateDate().getTime());
            messageJson.put("modifyDate", accountMessages.get(i).getModifyDate().getTime());
            messageJson.put("type", accountMessages.get(i).getType());
            messageJsons.add(messageJson);
        }
        jsonObject.put("accountMessages",messageJsons);
        jsonObject.put("user",userJson);
        return jsonObject;
    }


    /**
     * 获取子账号json数据
     * @param account
     * @return
     */
    public JSONObject makeSimpleAccountJson(Account account) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id",account.getId());
        jsonObject.put("department",account.getDepartment());
        jsonObject.put("email",account.getEmail());
        jsonObject.put("name",account.getName());
        jsonObject.put("phone",account.getPhone());
        jsonObject.put("userName",account.getUserName());
        jsonObject.put("createDate",account.getCreateDate());
        jsonObject.put("modifyDate",account.getModifyDate());
        return jsonObject;
    }



    /**
     * 获取所属users的json数据
     * @param user
     * @return
     */
    public JSONObject makeUserJson(User user) {
        JSONObject userJson = new JSONObject();
        userJson.put("id", user.getId());
        userJson.put("name", user.getName());
        userJson.put("userName", user.getUserName());
        userJson.put("email", user.getEmail());
        userJson.put("phone", user.getPhone());
        return userJson;
    }

    /**
     * 通过用户id获取所属企业用户
     * @param id
     * @return
     */
    public User getUserByAccountId(Long id) {
        Account account = accountDao.find(id);
        User user = account.getUser();
        return  user;
    }


    /**
     * 为前台构建子账号授权信息
     */
    public JSONObject getAuthenticate(Account account) {
        JSONObject subjectJson = new JSONObject();
        JSONObject infoJson = new JSONObject();
        JSONObject principalJson = new JSONObject();
        List<String> roles = new ArrayList<>();
        principalJson.put("name", account.getPhone());
        principalJson.put("login",account.getPhone());
        principalJson.put("email",StringUtils.hasText(account.getEmail()) ? account.getEmail():"test@qqsl.com");
        if (account.getRoles().indexOf(",") == -1) {
            roles.add(account.getRoles());
        } else {
            roles.addAll(Arrays.asList(account.getRoles().split(",")));
        }
        JSONObject authcJson = new JSONObject();
        authcJson.put("principal", principalJson);
        authcJson.put("credentials", principalJson);
        JSONObject authzJson = new JSONObject();
        List<String> permissions = new ArrayList<>();
        authzJson.put("roles", roles);
        authzJson.put("permissions", permissions);
        infoJson.put("authc", authcJson);
        infoJson.put("authz", authzJson);
        subjectJson.put("info", infoJson);
        return subjectJson;
    }

    /**
     * 子账号解绑企业
     * @param user
     * @return
     */
    public boolean unbindUser(User user,Account account) {
        List<Account> accounts = user.getAccounts();
        for (Account account1 : accounts) {
            if (account1.getId().equals(account.getId())) {
                accounts.remove(account1);
            }
        }
        user.setAccounts(accounts);
        userService.save(user);
        //记录子账号解绑企业的消息
        userMessageService.unbindMessage(account,user);
        return true;
    }

//    public List<JSONObject> makeAccountJsons(List<Account> accounts) {
//        JSONArray jsonArray = new JSONArray();
//        JSONObject accountJson;
//        for(int i=0;i<accounts.size();i++){
//            accountJson = makeSimpleAccountJson(accounts.get(i));
//            jsonArray.add(accountJson);
//        }
//        return jsonArray;
//    }

}
