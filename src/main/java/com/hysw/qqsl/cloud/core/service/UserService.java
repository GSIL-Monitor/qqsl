package com.hysw.qqsl.cloud.core.service;

import java.util.*;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.Verification;
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

import com.hysw.qqsl.cloud.core.controller.Message;
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
	private NoteService noteService;
	@Autowired
	private AccountService accountService;
	@Autowired
	private AuthentService authentService;
	@Autowired
	private AccountMessageService accountMessageService;
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
	public void setBaseDao(UserDao userDao) {
		super.setBaseDao(userDao);
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
		List<User> users = (List<User>) SettingUtils.objectCopy(findAll());
		for (User user : users) {
			if (user.getUserName().equals(userName)) {
                return user;
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

	/**
	 * 构建认证
	 * @param user
	 * @return
	 */
	private User buildCertify(User user){
		Certify certify = certifyService.findByUser(user);
		if (certify == null) {
			return user;
		}
		user.setPersonalStatus(certify.getPersonalStatus());
		if (certify.getPersonalStatus() == CommonEnum.CertifyStatus.PASS || certify.getPersonalStatus() == CommonEnum.CertifyStatus.EXPIRING) {
			user.setName(certify.getName());
		}
		user.setCompanyStatus(certify.getCompanyStatus());
		if (certify.getCompanyStatus() == CommonEnum.CertifyStatus.PASS || certify.getCompanyStatus() == CommonEnum.CertifyStatus.EXPIRING) {
			user.setCompanyName(certify.getCompanyName());
		}
		return user;
	}

    /**
	 * 用户注册业务封装
	 * @param map
	 * @param verification
	 * @return
     */
	public Message registerService(Map<String,Object> map,Verification verification){
     	Message	message = checkCode(map.get("verification").toString(), verification);
		if (message.getType()!=Message.Type.OK) {
			return message;
		}
		try {
			message = register(map.get("userName").toString(), verification.getPhone(),
					map.get("password").toString());
		} catch (QQSLException e) {
			logger.info(e.getMessage());
			return new Message(Message.Type.FAIL);
		}
		return message;
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
		Certify certify;
		User user1;
		while (it.hasNext()) {
			user1 = it.next();
			if (user1.getId().equals(user.getId())) {
				it.remove();
				continue;
			}
			certify = certifyService.findByUser(user1);
			if (certify == null) {
				it.remove();
				continue;
			}
			if (!(certify.getPersonalStatus() == CommonEnum.CertifyStatus.PASS || certify.getPersonalStatus() == CommonEnum.CertifyStatus.EXPIRING)) {
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
				return buildCertify(user);
			}
		}
		return null;
	}


	/**
	 * 企业用户注册
	 * @param phone
	 * @param password
	 * @return
	 * @throws QQSLException 
	 */
	public Message register(String userName,String phone, String password) throws QQSLException {
        if(phone.length()!=11||SettingUtils.phoneRegex(phone)==false){
        	throw new QQSLException(phone+":电话号码异常！");
		}
        if(password.length()!=32){
        	throw new QQSLException(password+":密码异常！");
        }
		User user = findByPhone(phone);
		// 用户已存在
		if (user!= null) {
			return new Message(Message.Type.EXIST);
		}else{
			user = new User();
		}
		user.setUserName(userName);
		user.setPhone(phone);
		user.setPassword(password);
//				user.setType(User.Type.USER);
		//默认新注册用户角色为web
		user.setRoles(CommonAttributes.ROLES[2]);
		save(user);
//				激活试用版套餐
		packageService.activateTestPackage(user);
//		构建认证状态
		Certify certify = new Certify(user);
		certifyService.save(certify);
		return new Message(Message.Type.OK);
	}

	/**
	 * 验证验证码
	 *
	 * @param code
	 * @param verification
	 * @return
	 */
	public Message checkCode(String code, Verification verification) {
		if (verification == null) {
			return new Message(Message.Type.INVALID);
		}
		if (verification.isInvalied()) {
			// 验证码过期
			return new Message(Message.Type.INVALID);
		}
		boolean result = noteService.checkCode(code, verification);
		if (result) {
			return new Message(Message.Type.INVALID);
		}
		return new Message(Message.Type.OK);
	}

	/**
	 * 用户信息修改
	 * @param userName
	 * @param name
	 * @param email
	 * @param password
	 * @return
     * @throws QQSLException
     */
	public Message update(String userName, String name,
			String email, String password) throws QQSLException {
		if(SettingUtils.parameterRegex(userName)==false){
			throw new QQSLException(userName+" 用户名格式异常！");
		}
		User user = findByUserName(userName);
		if (user == null) {
			//用户不存在;
			return new Message(Message.Type.FAIL);
		}
		if(password.length()!=32){
			throw new QQSLException(password+" 密码格式异常！");
		}
		user.setEmail(email);
		user.setName(name);
		user.setPassword(password);
		userDao.save(user);
		authentService.updateSession(user);
		return new Message(Message.Type.OK,makeUserJson(user));
	}

	/**
	 * 修改用户密码
	 * @param newPassword
	 * @param id
	 * @return
	 */
	public Message updatePassword(String newPassword, Long id) {
		User user = userDao.find(id);
		if(newPassword.length()!=32){
			return new Message(Message.Type.OTHER);
		}
		user.setPassword(newPassword);
		userDao.save(user);
		authentService.updateSession(user);
		return new Message(Message.Type.OK,makeUserJson(user));
	}

	/**
	 * 修改用户名和邮箱
	 * @param name
	 * @param email
	 * @param id
	 * @return
	 */
	public Message updateInfo(String name, String email, Long id) {
		User user = userDao.find(id);
		if(!SettingUtils.emailRegex(email)){
			return new Message(Message.Type.OTHER);
		}
		user.setName(name);
		user.setEmail(email);
		userDao.save(user);
		authentService.updateSession(user);
		return new Message(Message.Type.OK,makeUserJson(user));
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
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("id",user.getId());
		jsonObject.put("name",user.getName());
		jsonObject.put("userName",user.getUserName());
		jsonObject.put("phone",user.getPhone());
		return jsonObject;
	}

	/**
	 * 构建完整的userJson
	 * @param user
	 * @return
     */
	public JSONObject makeUserJson(User user){
		JSONObject userJson = new JSONObject();
		userJson.put("id",user.getId());
		userJson.put("avatar",user.getAvatar());
		userJson.put("email",user.getEmail());
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
//		userJson.put("type",user.getType());
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
	 * 获取用户通知信息
	 * @param userMessage
	 * @return
	 */
	private JSONObject getUserMessageJson(UserMessage userMessage){
		JSONObject userMessageJson = new JSONObject();
		userMessageJson.put("id",userMessage.getId());
		userMessageJson.put("content",userMessage.getContent());
		userMessageJson.put("projectId",userMessage.getProjectId());
		//userMessageJson.put("sensorId",userMessage.getSensorId());
		userMessageJson.put("sign",userMessage.getSign());
		userMessageJson.put("status",userMessage.getStatus());
		//userMessageJson.put("visitType",userMessage.getVisitType());
		userMessageJson.put("createDate",userMessage.getCreateDate().getTime());
		userMessageJson.put("modifyDate",userMessage.getModifyDate().getTime());
		return userMessageJson;
	}

	/**
	 * 企业解绑子账号,并收回该子账号所有分配的项目的协同权限
	 * @param account
	 * @return
	 */
	public Message unbindAccount(Account account) {
		User user = authentService.getUserFromSubject();
		//收回权限
		cooperateService.cooperateRevoke(user,account);
		List<Account> accounts = getAccountsByUserId(user.getId());
		List<User> users;
		boolean flag = false;
		for(int i=0;i<accounts.size();i++){
			if(accounts.get(i).getId().equals(account.getId())){
				users = accounts.get(i).getUsers();
				for(int k=0;k<users.size();k++){
					if(users.get(k).getId().equals(user.getId())){
						users.remove(k);
						flag = true;
						break;
					}
				}
				accounts.get(i).setUsers(users);
				accountService.save(account);
				accounts.remove(i);
				break;
			}
		}
		if(!flag){
			return new Message(Message.Type.UNKNOWN);
		}
		user.setAccounts(accounts);
		userDao.save(user);
		//记录企业解绑子账号的消息
		accountMessageService.bindMsessage(user,account,false);
		return new Message(Message.Type.OK);
	}

	/**
	 * 判断项目及子账号归属
	 * @param own
	 * @param project
	 * @param account
	 * @return
	 */
    public Message isOwn(User own, Project project, Account account) {
		if(account==null){
			return new Message(Message.Type.FAIL);
		}
		if(account.getName()==null){
			return new Message(Message.Type.FAIL);
			//return new Message(Message.Type.OTHER);
		}
		if (project==null||!project.getUser().getId().equals(own.getId())) {
			return new Message(Message.Type.FAIL);
		}
		List<Account> accounts = getAccountsByUserId(own.getId());
		for(int i=0;i<accounts.size();i++){
			if(account.getId().equals(accounts.get(i).getId())){
				return new Message(Message.Type.OK);
			}
		}
		return new Message(Message.Type.FAIL);
    }

	/**
	 * 添加user组缓存
	 */
	public void userCache() {
		List<User> users = userDao.findList(0, null, null);
		for (User user : users) {
			user.getAccounts().size();
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
				return buildCertify(user);
			}
		}
		return null;
	}
}
