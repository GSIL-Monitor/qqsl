package com.hysw.qqsl.cloud.core.service;

import com.aliyuncs.dysmsapi.model.v20170525.QuerySendDetailsResponse;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.google.gson.JsonObject;
import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.dao.AccountDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.data.*;
import com.hysw.qqsl.cloud.util.HttpRequestUtil;
import com.hysw.qqsl.cloud.util.SettingUtils;
import com.hysw.qqsl.cloud.wechat.util.WeChatHttpRequest;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
    private NoteService noteService;
    @Autowired
    private PollingService pollingService;
    @Autowired
    private HttpRequestUtil httpRequestUtil;
    @Autowired
    private ApplicationTokenService applicationTokenService;

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
        Account account = findByPhone(phone);
//        Note note;

        if (null == account) {
            try {
                SendSmsResponse sendSmsResponse = noteService.sendSms(phone, userService.nickName(user.getId()));
//                QuerySendDetailsResponse querySendDetailsResponse = noteService.querySendDetails(sendSmsResponse.getBizId(), phone);
//                note = new Note();
//                note.setPhone(phone);
//                note.setSendMsg(querySendDetailsResponse.getSmsSendDetailDTOs().get(0).getContent());
            } catch (ClientException e) {
                e.printStackTrace();
                return null;
            }
            account = new Account();
            account.setPhone(phone);
            account.setName(name.toString());
            account.setDepartment(department != null?department.toString():null);
            account.setRemark(remark != null ? remark.toString() : null);
            account.setRoles(CommonAttributes.ROLES[4]);
            account.setStatus(Account.Status.AWAITING);

            //记录子账号邀请消息
            account.setUser(user);
            save(account);
//            note.setAccountId(account.getId());
//            noteService.save(note);
            accountManager.add(account);
            pollingService.addAccount(account);
//        noteCache.add(phone,note);
            return makeSimpleAccountJson(account);
        }else{
            if (account.getStatus() == Account.Status.CONFIRMED) {
                return new JSONObject();
            } else if (account.getStatus() == Account.Status.AWAITING) {
                return null;
            }
        }
        return null;
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

    public Account findByPhone(String phone) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("phone",phone));
        List<Account> accounts = accountDao.findList(0, null, filters);
        if(accounts.size()==1){
            accounts.get(0).getUser();
            return accounts.get(0);
        }else{
            return null;
        }
    }

    public Account findByEmail(String email) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("email",email));
        List<Account> accounts = accountDao.findList(0, null, filters);
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
        jsonObject.put("status",account.getStatus().toString());
        User user = getUserByAccountId(account.getId());
        JSONObject userJson = new JSONObject();
        if(user!=null){
            userJson = makeUserJson(user);
        }
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
        if (account == null) {
            jsonObject.put("name","子账号已删除");
            return jsonObject;
        }
        jsonObject.put("id",account.getId());
        jsonObject.put("department",account.getDepartment());
        jsonObject.put("email",account.getEmail());
        jsonObject.put("name",account.getName());
        jsonObject.put("phone",account.getPhone());
        jsonObject.put("remark",account.getRemark());
        jsonObject.put("createDate",account.getCreateDate());
        jsonObject.put("modifyDate",account.getModifyDate());
        jsonObject.put("status",account.getStatus().toString());
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
        account.setDepartment(department != null?department.toString():null);
        account.setRemark(remark!=null?remark.toString():null);
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
    public void activateAccount(Account account) {
        String msg = "";
        if (account == null) {
            return;
        }
        if (account.getStatus() == Account.Status.CONFIRMED) {
            msg = "您已经成为[" + userService.nickName(account.getUser().getId()) + "]企业的子账户，该项不能重复操作，如需绑定到另一企业，请先解绑。";
        }
        if (account.getStatus()==Account.Status.AWAITING) {
            account.setStatus(Account.Status.CONFIRMED);
            String inviteCode = SettingUtils.createRandomVcode();
            account.setPassword(DigestUtils.md5Hex(inviteCode));
            save(account);
            msg = "尊敬的用户您好，恭喜您已成为[" + userService.nickName(account.getUser().getId()) + "]企业的子账户,密码为："+inviteCode+"。";
            userMessageService.accountMessageAgree(account);
        }
        Note note = new Note(account.getPhone(), msg);
        noteCache.add(account.getPhone(),note);
        flush();
    }

    /**
     * 拒绝成为该企业子账号
     */
    public void refusedAccount(Account account) {
        if (account == null) {
            return;
        }
        if (account.getStatus() == Account.Status.CONFIRMED) {
            return;
        }
        if (account.getStatus() == Account.Status.AWAITING) {
            userMessageService.accountMessageRefused(account);
            remove(account);
        }
        flush();
    }

    /**
     * 激活子账号
     */
    public void activeAccount() {
        JSONObject jsonObject1 = WeChatHttpRequest.jsonObjectHttpRequest("http://"+SettingUtils.getInstance().getSetting().getAliPayIP() + ":8080/qqsl/user/getSmsUpList?token=" + applicationTokenService.getToken(), "GET", null);
        if (jsonObject1.get("data") == null||JSONArray.fromObject(jsonObject1.get("data")).size()==0) {
            return;
        }
        for (Object o : JSONArray.fromObject(jsonObject1.get("data"))) {
            JSONObject jsonObject = JSONObject.fromObject(o);
            if (jsonObject.get("reply").toString().equalsIgnoreCase("y")) {
                Account account = findByPhone(String.valueOf(jsonObject.get("phone")));
                if (account != null && account.getStatus() == Account.Status.AWAITING) {
                    activateAccount(account);
                }
            }
            if (jsonObject.get("reply").toString().equalsIgnoreCase("n")) {
                Account account = findByPhone(String.valueOf(jsonObject.get("phone")));
                if (account != null && account.getStatus() == Account.Status.AWAITING) {
                    refusedAccount(account);
                }
            }
        }
    }

    /**
     * 根据ids查询account列表
     * @param accountIds
     * @return
     */
    public List<Account> findByIdList(Object accountIds) {
        String[] split = accountIds.toString().split(",");
        Account account;
        List<Account> accounts = new ArrayList<>();
        for (String s : split) {
            account = find(Long.valueOf(s));
            accounts.add(account);
        }
        return accounts;
    }
}
