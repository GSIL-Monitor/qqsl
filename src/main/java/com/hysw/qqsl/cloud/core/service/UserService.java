package com.hysw.qqsl.cloud.core.service;

import java.util.*;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.data.*;
import com.hysw.qqsl.cloud.pay.entity.PackageItem;
import com.hysw.qqsl.cloud.pay.entity.PackageModel;
import com.hysw.qqsl.cloud.pay.entity.ServeItem;
import com.hysw.qqsl.cloud.pay.entity.data.Package;
import com.hysw.qqsl.cloud.pay.service.PackageService;
import com.hysw.qqsl.cloud.pay.service.TradeService;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hysw.qqsl.cloud.core.dao.UserDao;
import com.hysw.qqsl.cloud.core.entity.QQSLException;
import com.hysw.qqsl.cloud.util.SettingUtils;
import org.springframework.util.StringUtils;

@Service("userService")
public class UserService extends BaseService<User, Long> {
	Log logger = LogFactory.getLog(getClass());
	@Autowired
	private UserDao userDao;
	@Autowired
	private AccountService accountService;
	@Autowired
	private UserMessageService userMessageService;
	@Autowired
	private CooperateService cooperateService;
	@Autowired
	private PackageService packageService;
	@Autowired
	private TradeService tradeService;
	@Autowired
	private CacheManager cacheManager;
	@Autowired
	private CertifyService certifyService;
	@Autowired
	private UserService userService;
	@Autowired
	private PollingService pollingService;
	@Autowired
	private NoteCache noteCache;
    @Autowired
    private StationService stationService;
    @Autowired
    private PanoramaService panoramaService;
	@Autowired
	public void setBaseDao(UserDao userDao) {
		super.setBaseDao(userDao);
	}

	@Override
	public User find(Long id) {
		List<User> all = findAll();
		for (User user : all) {
			if (user.getId().equals(id)) {
				return (User) SettingUtils.objectCopy(user);
			}
		}
		return null;
	}

	public User findByDao(Long id){
		User user = userDao.find(id);
		user.getAccounts();
		return user;
	}

	/**
	 * 为前台构建授权信息（用户，管理员）
	 */
	public JSONObject getAuthenticate(User user) {
		JSONObject subjectJson = new JSONObject();
		JSONObject infoJson = new JSONObject();
		JSONObject authcJson = new JSONObject();
		JSONObject principalJson = new JSONObject();
		List<String> roles = new ArrayList<>();
			principalJson.put("name", user.getPhone());
			principalJson.put("login",user.getPhone());
			principalJson.put("email",StringUtils.hasText(user.getEmail()) ? user.getEmail():"test@qqsl.com");
			if (user.getRoles().indexOf(",") == -1) {
				roles.add(user.getRoles());
			} else {
				roles.addAll(Arrays.asList(user.getRoles().split(",")));
			}
		authcJson.put("principal", principalJson);
		authcJson.put("credentials", principalJson);
		JSONObject authzJson = new JSONObject();
		List<String> permissions = new ArrayList<>();
        /*if(user.getUserName().equals("qqsl")){
			permissions.add("address:view,create,edit,delete");
		}else{
			permissions.add("address:view,edit,delete");
		}*/
		authzJson.put("roles", roles);
		authzJson.put("permissions", permissions);
		infoJson.put("authc", authcJson);
		infoJson.put("authz", authzJson);
		subjectJson.put("info", infoJson);
		return subjectJson;
	}

	/**
	 * 根据用户名找用户
	 * 
	 * @param userName
	 * @return
	 */
	public User findByUserName(String userName) {
		List<User> users = findAll();
		for (User user : users) {
			if (user.getUserName().equals(userName)) {
                return (User) SettingUtils.objectCopy(user);
            }
		}
		return null;
	}

    /**
     * 在用户中构建
     * @param user
     */
    private void buildPackage(User user) {
        Package aPackage = packageService.findByUser(user);
        if (aPackage == null) {
            return;
        }
        List<Package> packages = new ArrayList<>();
        packages.add(aPackage);
        user.setPackages(packages);
    }

//	/**
//	 * 构建认证
//	 * @param user
//	 * @return
//	 */
//	private User buildCertify(User user){
//		Certify certify = certifyService.findByUser(user);
//		if (certify == null) {
//			return user;
//		}
//		user.setPersonalStatus(certify.getPersonalStatus());
//		if (certify.getPersonalStatus() == CommonEnum.CertifyStatus.PASS || certify.getPersonalStatus() == CommonEnum.CertifyStatus.EXPIRING) {
//			user.setName(certify.getName());
//		}
//		user.setCompanyStatus(certify.getCompanyStatus());
//		if (certify.getCompanyStatus() == CommonEnum.CertifyStatus.PASS || certify.getCompanyStatus() == CommonEnum.CertifyStatus.EXPIRING) {
//			user.setCompanyName(certify.getCompanyName());
//		}
//		return user;
//	}

	/**
	 * 用户注册业务封装
	 *
	 * @param user
	 * @param userName
	 * @param phone
	 * @param password
	 */
	public boolean registerService(User user, String userName, String phone, String password) {
		try {
			if (phone.length() != 11 || SettingUtils.phoneRegex(phone) == false) {
				throw new QQSLException(phone + ":电话号码异常！");
			}
			if (password.length() != 32) {
				throw new QQSLException(password + ":密码异常！");
			}
		} catch (QQSLException e) {
			logger.info(e.getMessage());
			return false;
		}
		user.setUserName(userName);
		user.setPhone(phone);
		user.setPassword(password);
//				user.setType(User.Type.USER);
		//默认新注册用户角色为web
		user.setRoles(CommonAttributes.ROLES[2]);
		save(user);
		pollingService.addUser(user);
//				激活试用版套餐
		packageService.activateTestPackage(user);
//		构建认证状态
		Certify certify = new Certify(user);
		certifyService.save(certify);
		return true;
	}


	/**
	 * 获取除了自己以外的用户
	 * @param user 
	 * 
	 * @return
	 */
	public List<User> findUsersNeOwn(User user) {
		List<User> users = (List<User>) SettingUtils.objectCopy(findAll());
		Iterator<User> it = users.iterator();
//		Certify certify;
		User user1;
		while (it.hasNext()) {
			user1 = it.next();
			if (user1.getId().equals(user.getId())) {
				it.remove();
				continue;
			}
//			certify = certifyService.findByUser(user1);
//			if (certify == null) {
//				it.remove();
//				continue;
//			}
//			if (!(certify.getPersonalStatus() == CommonEnum.CertifyStatus.PASS || certify.getPersonalStatus() == CommonEnum.CertifyStatus.EXPIRING||certify.getCompanyStatus() == CommonEnum.CertifyStatus.PASS || certify.getCompanyStatus() == CommonEnum.CertifyStatus.EXPIRING)) {
//				it.remove();
//				continue;
//			}
//			user1.setName(certify.getName());
//            user1.setCompanyName(certify.getCompanyName());
//            user1.setPersonalStatus(certify.getPersonalStatus());
//            user1.setCompanyStatus(certify.getCompanyStatus());
//		}
//		return users;
//	}
			if (!(user1.getPersonalStatus() == CommonEnum.CertifyStatus.PASS ||
					user1.getPersonalStatus() == CommonEnum.CertifyStatus.EXPIRING||
					user1.getCompanyStatus() == CommonEnum.CertifyStatus.PASS ||
					user1.getCompanyStatus() == CommonEnum.CertifyStatus.EXPIRING)) {
				it.remove();
			}
		}
		return users;
	}

	/**
	 * 获取用户
	 *
	 * @return
	 */
	public List<User> findUsers() {
		List<User> users = (List<User>) SettingUtils.objectCopy(findAll());
		Iterator<User> it = users.iterator();
		Certify certify;
		User user;
		while (it.hasNext()) {
			user = it.next();
			certify = certifyService.findByUser(user);
			user.setPersonalStatus(certify.getPersonalStatus());
			user.setCompanyStatus(certify.getCompanyStatus());
		}
		return users;
	}

	/**
	 * 查看该手机号是否被注册
	 * @param phone
	 * @return
	 */
	public User findByPhone(String phone) {
		List<User> users = (List<User>) SettingUtils.objectCopy(findAll());
		for (User user : users) {
			if (user.getPhone().equals(phone)) {
				buildPackage(user);
				return user;
//				return buildCertify(user);
			}
		}
		return null;
	}

	/**
	 * 修改用户密码
	 * @param newPassword
	 * @param id
	 * @return
	 */
	public JSONObject updatePassword(String newPassword, Long id) {
		User user = findByDao(id);
		if(newPassword.length()!=32){
			return null;
		}
		user.setPassword(newPassword);
		save(user);
		return makeUserJson(user);
	}

	/**
	 * 根据用户名或手机号码查找用户
	 * @param argument
	 * @return
	 */
	public User findByPhoneOrEmial(String argument){
		User user;
		if(SettingUtils.phoneRegex(argument)){
			user = findByPhone(argument);
		}else if(SettingUtils.emailRegex(argument)){
			user = findByEmail(argument);
		}else {
			user = null;
		}
		return user;
	}

	/**
	 * 构建用户列表的json
	 * @param users
	 * @return
	 */
	public List<JSONObject> makeUserJsons(List<User> users) {
		List<JSONObject> userJsons = new ArrayList<JSONObject>();
		JSONObject userJson;
		User user ;
		for(int i=0;i<users.size();i++){
			userJson = new JSONObject();
			user = users.get(i);
			userJson.put("id", user.getId());
			userJson.put("name", user.getName());
			userJson.put("userName", user.getUserName());
			userJson.put("email", user.getEmail());
			userJson.put("phone", user.getPhone());
			userJson.put("roles",user.getRoles());
			setNickName(user,userJson);
			if(user.getLocked()==null||user.getLocked()==false){
				userJson.put("isLocked", false);
			}else{
				userJson.put("isLocked", true);
			}
			userJson.put("lockedDate", user.getLockedDate());
			userJson.put("loginDate", user.getLoginDate());
			userJson.put("loginType",user.getLoginType());
			if (user.getPersonalStatus() != null) {
				userJson.put("personalStatus", user.getPersonalStatus());
			}
			if (user.getCompanyStatus() != null) {
				userJson.put("companyStatus", user.getCompanyStatus());
			}
			userJsons.add(userJson);
		}
		return userJsons;
	}

	/**
	 * 更新或保存项目前缀和序号字段
	 * @param
	 * @param user
     */
	public void setPrefixOrderJson(String prefix,String order,User user){
		JSONObject prefixOrderJson = new JSONObject();
		prefixOrderJson.put("prefix",prefix);
		prefixOrderJson.put("order",order);
		user.setPrefixOrderStr(prefixOrderJson.toString());
	}

	/**
	 * 构建简单user
	 * @param user
	 * @return
     */
	public User getSimpleUser(User user){
		User simpleUser = new User();
		simpleUser.setId(user.getId());
//		simpleUser.setType(user.getType());
		simpleUser.setName(user.getName());
		simpleUser.setUserName(user.getUserName());
		return simpleUser;
	}

	/**
	 * 获取部分uesr信息
	 * @param user
	 * @return
	 */
	public JSONObject makeSimpleUserJson(User user){
		JSONObject userJson = new JSONObject();
		userJson.put("id",user.getId());
		userJson.put("name",user.getName());
		userJson.put("userName",user.getUserName());
		userJson.put("phone",user.getPhone());
		userJson.put("companyName", user.getCompanyName());
		setNickName(user,userJson);
		return userJson;
	}

	/**
	 * 构建完整的userJson
	 * @param user
	 * @return
     */
	public JSONObject makeUserJson(User user){
		JSONObject userJson = new JSONObject();
		userJson.put("id",user.getId());
		userJson.put("email",user.getEmail());
		setNickName(user,userJson);
		if (user.getName() == null) {
			userJson.put("name",user.getUserName());
		}else{
			userJson.put("name",user.getName());
		}
		userJson.put("phone",user.getPhone());
		userJson.put("userName",user.getUserName());
		userJson.put("prefixOrderStr",user.getPrefixOrderStr());
		userJson.put("personalStatus", user.getPersonalStatus());
		userJson.put("companyStatus", user.getCompanyStatus());
		if (user.getCompanyName() != null) {
			userJson.put("companyName", user.getCompanyName());
		}
		List<UserMessage> userMessages = userMessageService.findByUserAndType(user);
		List<JSONObject> jsonObjects = new ArrayList<>();
		JSONObject userMessageJson;
		for(int i = 0;i<userMessages.size();i++){
			userMessageJson = getUserMessageJson(userMessages.get(i));
			jsonObjects.add(userMessageJson);
		}
		userJson.put("userMessages",jsonObjects);
		List<Account> accounts = getAccountsByUserId(user.getId());
		jsonObjects = new ArrayList<>();
		JSONObject accountJson;
		for(int j = 0;j<accounts.size();j++){
			accountJson = accountService.makeSimpleAccountJson(accounts.get(j));
			jsonObjects.add(accountJson);
		}
		userJson.put("accounts",jsonObjects);
        Package aPackage = user.getPackages().get(0);
        JSONObject aPackageJson = new JSONObject();
        aPackageJson.put("expire", aPackage.getExpireDate().getTime());
        aPackageJson.put("type", aPackage.getType());
        userJson.put("package", aPackageJson);
        return userJson;
	}

	/**
	 * 设置nickName，用于保存分享时的名称，如果是企业认证的是企业名，如果是实名认证的，是真实姓名，否则是昵称
	 * @param user
	 * @param userJson
	 */
	private void setNickName(User user,JSONObject userJson){
		if(user.getPersonalStatus().equals(CommonEnum.CertifyStatus.PASS)){
			if(user.getCompanyStatus().equals(CommonEnum.CertifyStatus.PASS)){
				userJson.put("nickName", user.getCompanyName());
				return;
			}
			userJson.put("nickName", user.getName());
		}else {
			userJson.put("nickName", user.getUserName());
		}
	}

	public String nickName(Long userId) {
		User user = userService.find(userId);
		if (user.getPersonalStatus().equals(CommonEnum.CertifyStatus.PASS)) {
			if (user.getCompanyStatus().equals(CommonEnum.CertifyStatus.PASS)) {
				return user.getCompanyName();
			}
			return user.getName();
		} else {
			return user.getUserName();
		}
	}

	/**
	 * 通过用户id获取所属子账号
	 * @param id
	 * @return
	 */
	public List<Account> getAccountsByUserId(Long id) {
		User user = userDao.find(id);
		List<Account> accounts = user.getAccounts();
		return  accounts;
	}

	/**
	 * 判断项目及子账号归属
	 * @param own
	 * @param account
	 * @return
	 */
	public boolean isOwn(User own, Account account) {
		List<Account> accounts = userService.getAccountsByUserId(own.getId());
		for(int j=0;j<accounts.size();j++){
			if(account.getId().equals(accounts.get(j).getId())){
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取用户通知信息
	 * @param userMessage
	 * @return
	 */
	private JSONObject getUserMessageJson(UserMessage userMessage){
		JSONObject userMessageJson = new JSONObject();
		userMessageJson.put("id",userMessage.getId());
		userMessageJson.put("content",userMessage.getContent());
		userMessageJson.put("status",userMessage.getStatus());
		//userMessageJson.put("visitType",userMessage.getVisitType());
		userMessageJson.put("createDate",userMessage.getCreateDate().getTime());
		userMessageJson.put("modifyDate",userMessage.getModifyDate().getTime());
		userMessageJson.put("type", userMessage.getType());
		return userMessageJson;
	}

	/**
	 * 企业解绑子账号,并收回该子账号所有分配的项目的协同权限
	 * @param account
	 * @return
	 */
	public boolean deleteAccount(Account account,User user) {
		//收回权限
		cooperateService.cooperateRevoke(user,account);
		//收回全景权限
        panoramaService.revoke(user,account);
        //收回测站权限
        stationService.unCooperate(user, account);
        user = userService.findByDao(user.getId());
		List<Account> accounts = user.getAccounts();
		for (Account account1 : accounts) {
			if (account.getId().equals(account1.getId())) {
				accounts.remove(account1);
				if (account.getStatus() == Account.Status.CONFIRMED) {
					String msg = "[" + userService.nickName(account.getUser().getId()) + "]企业已解除与您的子账号关系，相关权限已被收回，您的子账户已被移除。";
					Note note = new Note(account.getPhone(), msg);
					noteCache.add(account.getPhone(),note);
				}
				accountService.remove(account1);
				break;
			}
		}
		user.setAccounts(accounts);
		save(user);
		return true;
	}

	/**
	 * 添加user组缓存
	 */
	public void userCache() {
		List<User> users = userDao.findList(0, null, null);
        Certify certify;
        for (User user : users) {
			user.getAccounts().size();
            certify = certifyService.findByUser(user);
			if (SettingUtils.getInstance().getSetting().getStatus().equals("test")) {
				continue;
			}
            user.setPersonalStatus(certify.getPersonalStatus());
            user.setCompanyStatus(certify.getCompanyStatus());
            if (certify.getPersonalStatus() == CommonEnum.CertifyStatus.PASS || certify.getPersonalStatus() == CommonEnum.CertifyStatus.EXPIRING) {
                user.setName(certify.getName());
            }
            if (certify.getCompanyStatus() == CommonEnum.CertifyStatus.PASS || certify.getCompanyStatus() == CommonEnum.CertifyStatus.EXPIRING) {
                user.setCompanyName(certify.getCompanyName());
            }
        }
		Cache cache = cacheManager.getCache("userAllCache");
		net.sf.ehcache.Element element = new net.sf.ehcache.Element("user", users);
		cache.put(element);
	}

	@Override
	public List<User> findAll() {
		Cache cache = cacheManager.getCache("userAllCache");
		net.sf.ehcache.Element element = cache.get("user");
		List<User> users=(List<User>) element.getValue();
		return users;
	}

	public User findByEmail(String email) {
		List<User> list = findAll();
		for (User user : list) {
			if (user.getEmail() != null && user.getEmail().equals(email)) {
				buildPackage(user);
				return user;
//				return buildCertify(user);
			}
		}
		return null;
	}

	/**
	 * 是否允许创建子账号
	 * @param user1
	 * @return  false  允许创建 true  不允许创建
	 */
	public boolean isAllowCreateAccount(User user1) {
		User user = find(user1.getId());
		Package aPackage = packageService.findByUser(user);
		if (aPackage == null) {
			return true;
		}
		PackageModel packageModel = tradeService.getPackageModel(aPackage.getType().toString());
		for (PackageItem packageItem : packageModel.getPackageItems()) {
			if (packageItem.getServeItem().getType() == ServeItem.Type.ACCOUNT && user.getAccounts().size() < packageItem.getLimitNum()) {
				return false;
			}
		}
		return true;
	}
}
