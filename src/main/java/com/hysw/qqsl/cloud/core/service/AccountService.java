package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.dao.AccountDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.AccountMessage;
import com.hysw.qqsl.cloud.core.entity.data.Note;
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
    private AccountManager accountManager;

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
    public JSONObject create(String phone,User user,Object name,Object department,Object remark) {
        List<Account> accounts = findByPhone(phone);
        for (Account account : accounts) {
            if (account.getStatus() == Account.Status.CONFIRMED) {
                return null;
            }
            if (account.getPhone().equals(phone) && account.getUser().getId().equals(user.getId())) {
                return null;
            }
        }
        String userName = user.getUserName();
        if(user.getName()!=null){
            userName = user.getName();
        }
        if(user.getCompanyName()!=null){
            userName = user.getCompanyName();
        }
        String noteMessage = userName + "企业邀请您成为其企业子账号，同意回复Y，不同意回复N。（24小时有效期）";
        //新方式发送短信
//        Note note = new Note(phone,noteMessage);
        Account account = new Account();
        account.setPhone(phone);
        account.setName(name.toString());
        if (department != null) {
            account.setDepartment(department.toString());
        }
        if (remark != null) {
            account.setRemark(remark.toString());
        }
        account.setRoles(CommonAttributes.ROLES[4]);
        account.setStatus(Account.Status.AWAITING);

        //记录子账号邀请消息
        account.setUser(user);
        save(account);
        accountManager.add(account);
//        pollingService.addAccount(account);
//        noteCache.add(phone,note);
        return makeSimpleAccountJson(account);
    }

    /**
     * 根据手机号查询子账号
     * @param phone
     * @return
     */
    public Account findByPhoneConfirmed(String phone) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("phone",phone));
        filters.add(Filter.eq("status", Account.Status.CONFIRMED));
        List<Account> accounts = accountDao.findList(0,null,filters);
        if(accounts.size()==1){
            accounts.get(0).getUser();
            return accounts.get(0);
        }else{
            return null;
        }
    }

    public List<Account> findByPhone(String phone) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("phone",phone));
        return accountDao.findList(0,null,filters);
    }

    public List<Account> findByEmail(String email) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("email",email));
        return accountDao.findList(0,null,filters);
    }

    /**
     * 根据email查询子账号
     * @param email
     * @return
     */
    public Account findByEmailConfirmed(String email) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("email",email));
        filters.add(Filter.eq("status", Account.Status.CONFIRMED));
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
    public List<Account> findByPhoneOrEmial(String argument){
        List<Account> accounts = null;
        if(SettingUtils.phoneRegex(argument)){
            accounts = findByPhone(argument);
        }else if(SettingUtils.emailRegex(argument)){
            accounts = findByEmail(argument);
        }
        return accounts;
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
        jsonObject.put("remark",account.getRemark());
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
        jsonObject.put("remark",account.getRemark());
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

//    /**
//     * 子账号解绑企业
//     * @param user
//     * @return
//     */
//    public boolean unbindUser(User user,Account account) {
//        List<Account> accounts = user.getAccounts();
//        for (Account account1 : accounts) {
//            if (account1.getId().equals(account.getId())) {
//                accounts.remove(account1);
//            }
//        }
//        user.setAccounts(accounts);
//        userService.save(user);
//        //记录子账号解绑企业的消息
//        userMessageService.unbindMessage(account,user);
//        return true;
//    }

    public List<Account> findAllAwaiting() {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("status", Account.Status.AWAITING));
        return accountDao.findList(0,null,filters);
    }

    public boolean accountUpdate(Object id, Object name, Object department, Object remark) {
        Account account = find(Long.valueOf(id.toString()));
        if (account == null) {
            return false;
        }
        account.setName(name.toString());
        if (department != null) {
            account.setDepartment(department.toString());
        }
        if (remark != null) {
            account.setRemark(remark.toString());
        }
        save(account);
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

    /**
     * 激活子账号
     */
    public void activateAccount(List<Account> accounts,String accountId) {
        String msg = "";
        boolean flag = false;
        Account account = null;
        for (Account account1 : accounts) {
            if (account1.getStatus() == Account.Status.CONFIRMED) {
                flag = true;
                msg = "您已经成为==>" + userService.nickName(account1.getUser().getId()) + "<==企业的子账户，该项不能重复操作，如需绑定到另一企业，请先解绑。";
                break;
            }
            if (account1.getId().toString().equals(accountId)) {
                account = account1;
            }
        }
        if (null != account && !flag) {
            account.setStatus(Account.Status.CONFIRMED);
            String inviteCode = SettingUtils.createRandomVcode();
            account.setPassword(DigestUtils.md5Hex(inviteCode));
            save(account);
            msg = "尊敬的用户您好，恭喜您已成为==>" + userService.nickName(account.getUser().getId()) + "<==企业的子账户,密码为："+inviteCode+"。";
            userMessageService.accountMessageAgree(account);
        }
        Note note = new Note(account.getPhone(), msg);
        noteCache.add(account.getPhone(),note);
    }

    /**
     * 拒绝成为该企业子账号
     */
    public void refusedAccount(List<Account> accounts,String accountId) {
        boolean flag = false;
        Account account = null;
        for (Account account1 : accounts) {
            if (account1.getStatus() == Account.Status.CONFIRMED) {
                flag = true;
                break;
            }
            if (account1.getId().toString().equals(accountId)) {
                account = account1;
            }
        }
        if (null != account && !flag) {
            remove(account);
            userMessageService.accountMessageRefused(account);
        }
    }

}
