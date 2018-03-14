package com.hysw.qqsl.cloud.core.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.hysw.qqsl.cloud.core.controller.CommonController;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.QQSLException;
import com.hysw.qqsl.cloud.core.entity.Verification;
import com.hysw.qqsl.cloud.core.entity.data.Contact;
import com.hysw.qqsl.cloud.core.entity.data.User;

import static org.junit.Assert.*;


/**
 *  1.注册业务测试
 *  2.验证码业务测试
 *  3.登录业务测试
 *  4.手机密码重置业务测试
 *     管理员密码重置业务测试
 *  5.用户信息修改业务测试
 *
 * @author Administrator
 *
 */
//@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = false)
public class UserServiceTest extends BaseTest{
	@Autowired
	private UserService userService;
    @Autowired
    private ContactService contactService;
	@Autowired
	private NoteService noteService;
	@Autowired
	private AccountService accountService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private CommonController commonController;

    private static final String code = "123456";
	private static final String errorCode = "123451";
    private static final String userName = "qqsl";
    private static final String userName2 = "青清水利";
    private static final String name = "liujianbin";
    private static final String phone = "18661925010";
    private static final String errorPhone = "186619250100";
    private static final String password = "abc";
    private static final String email = "123465@qq.com";


    /**
     * 注册业务测试
     */
	@Test
    public void testRegisterRight(){
		//检查用户名是否能已注册
		Long times = System.currentTimeMillis();
		String userName = "hysw"+times;
		User user = userService.findByUserName(userName);
		assertTrue(user==null);
		//获取手机验证码
		String phone = "18661925012";
		//查看手机号码是否注册
        user = userService.findByPhone(phone);
		assertTrue(user==null);
    	Verification verification = new Verification();
    	verification.setCode(code);
    	verification.setPhone(phone);
		//发送验证码
		MockHttpSession session = new MockHttpSession();
		boolean flag = noteService.isSend(phone, session);
		assertTrue(flag);
		//手机验证码验证
		Verification verification1 = (Verification) session.getAttribute("verification");
		 Message message  = commonController.checkCode(code,verification1);
		assertTrue(message.getType()==Message.Type.OK);
		//用户注册
		boolean b = userService.registerService(new User(),userName, phone, DigestUtils.md5Hex(password));
		assertTrue(b);
		//删除测试数据
		User user2 = userService.findByUserName(userName);
		assertTrue(user2.getId()!=null);
		userService.remove(user2);
    }

	/**
	 * 登陆业务测试
	 */
	@Test
	public void testLogin(){
		//判断用户是否存在
        List<User> users = userService.findAll();
		assertTrue(users.size()>0);
        User user = users.get(0);
		String userName = user.getUserName();
		String password = user.getPassword();
		//用户名密码是否正确
		user = userService.findByPhoneOrEmial("18661925010");
		assertEquals(userName,user.getUserName());
		assertEquals(password,user.getPassword());
		//是否第一次登陆
//		assertNotNull(user.getLoginIp());
//		MockHttpServletRequest request = new MockHttpServletRequest();
//		String ip = "117.22.173.240";
//		//是否替换或更新登陆次数ip
	}

	/**
	 * 忘记密码业务测试
	 */
	@Test
	public void testChangePassword(){
		String userName = "hysw1471433440812";
		String phone = "18661925010";
		String newPassword = DigestUtils.md5Hex("123456");
		User user = new User();
		user.setName("青海鸿源");
		user.setUserName(userName);
		user.setEmail("119238122@qq.com");
		user.setPassword(DigestUtils.md5Hex("abc"));
		user.setPhone(phone);
//		user.setType(User.Type.USER);
		userService.save(user);
		//判断用户是否存在
		user = userService.findByUserName(userName);
		assertTrue(user.getId()!=null);
		//判断用户注册手机
		assertEquals(phone,user.getPhone());
		Verification verification = new Verification();
		verification.setCode(code);
		verification.setPhone(phone);
		//获取验证码
		MockHttpSession session = new MockHttpSession();
		boolean b = noteService.isSend(user.getPhone(), session);
		assertTrue(b);
		//手机验证码验证
		Verification verification1 = (Verification) session.getAttribute("verification");
		Message message  = commonController.checkCode(code,verification1);
		assertTrue(message.getType()==Message.Type.OK);
		//更新用户
		user.setPassword(newPassword);
		userService.save(user);
		assertEquals(user.getPassword(),newPassword);
		userService.remove(user);
	}

	/**
	 * 修改手机号业务测试
	 */
	@Test
	public void testChangePhone(){
		String newPhone = "18661925010";
		//检查该手机号是否注册
		User user = userService.findByPhone(newPhone);
		assertTrue(user.getId()!=null);
		//获取验证码
		Verification verification = new Verification();
		verification.setCode(code);
		verification.setPhone(newPhone);
		MockHttpSession session = new MockHttpSession();
		boolean b = noteService.isSend(user.getPhone(),session);
		assertTrue(b);
		//手机验证码验证
		Verification verification1 = (Verification) session.getAttribute("verification");
		Message message = commonController.checkCode(code, verification1);
		assertTrue(message.getType()==Message.Type.OK);
		//更新用户
	}

//	@Test
//	public void testChangePhone1(){
//		MockHttpSession session = new MockHttpSession();
//		Message message = noteService.isSend("18661925010",session);
//	}


	public void testRegisterErrorPassword(){
    	//手机验证码验证
    	Verification verification = new Verification();
    	verification.setCode(code);
    	verification.setPhone(phone);
		User user = userService.findByPhone(phone);
		Message message  = commonController.checkCode(errorCode,verification);
		assertTrue(message.getType()!=Message.Type.OK);
		boolean b=userService.registerService(user,userName, phone,password);
		if (b) {
			fail("应该出错却没有错！");
		}
    }
 
    /**
     * 通讯录保存业务测试
     */
	@Test
	public void testSaveContacts(){
		User user = userService.findByUserName("qqsl");
		Contact contact = new Contact();
		contact.setUser(user);
		contact.setCompany("hongyuan");
		contact.setDepart("xinxi");
		contact.setMaster("hh");
		contact.setName("hg");
		contact.setEmail("123@qq.com");
		contact.setMasterEmail("321@qq.com");
		contact.setMasterPhone("18661925010");
		contact.setPhone("18661925010");
		contact.setQualify("甲级");
		contact.setType(Contact.Type.DESIGN);
		contactService.doSaveContact(contact);
		List<Contact> contacts = contactService.findByUser(user, Contact.Type.DESIGN);
		assertTrue(contacts.size()>0);
	}

	/**
	 * 通过手机号码找到用户
	 */
	@Test
	public void testFindByPhone(){
		User user  = userService.findByPhone(phone);
		assertTrue(user.getId()!=null);
		User user1  = userService.findByPhone("18661925011");
		assertTrue(user1!=null);
	}

   /**
    * 根据用户名或手机号查找用户
    * 手机号码查找需要排除管理员
    */
   @Test
   public void testFindByPhoneOrUserName(){
	   User user1 = userService.findByPhoneOrEmial("18661925010");
	   assertTrue(user1.getId()!=null);
	   User user2 = userService.findByPhoneOrEmial("18661925555");
	   assertTrue(user2==null);
   }


  @Test
  public void testMakeUserJsons(){
		List<User> users =  userService.findAll();
		List<JSONObject> userJsons = userService.makeUserJsons(users);
		assertTrue(users.size()==userJsons.size());
		assertTrue(users.get(0).getId()==Long.valueOf(userJsons.get(0).get("id").toString()));
	}
  @Test
  public void testMakeUserJson(){
	  User user = userService.findByDao(1l);
	  JSONObject userJson = userService.makeUserJson(user);
	  assertTrue(Long.valueOf(userJson.get("id").toString()).equals(user.getId()));
  }


	@Test
	public void testUpdateLoginIp(){
		List<User> users = userService.findAll();
		for(int i=0;i<users.size();i++){
			users.get(i).setLoginIp(null);
			userService.save(users.get(i));
		}
	}

	@Test
	public void testGetAuthenticate(){
		JSONObject jsonObject = userService.getAuthenticate(userService.find(1l));
		Assert.assertNotNull(jsonObject);
	}

	@Test
	public void testRegisterService(){
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("verification", 123456);
		map.put("userName", "aaa");
		map.put("password", DigestUtils.md5Hex("123456"));
		Verification verification=new Verification();
		verification.setCode("123456");
		verification.setPhone("13519779005");
		boolean b = userService.registerService(new User(),"aaa", "13519779005",DigestUtils.md5Hex("123456"));
		Assert.assertTrue(b);
	}

	@Test
	public void testUpdatePassword(){
		JSONObject jsonObject= userService.updatePassword(DigestUtils.md5Hex("123456"), 1l);
		Assert.assertTrue(!jsonObject.isEmpty());
	}

	@Test
	public void testIsAllowCreateAccount(){
		boolean b = userService.isAllowCreateAccount(userService.find(1l));
		Assert.assertTrue(!b);
	}

	@Test
	public void testUnbindAccount(){
		boolean b = userService.unbindAccount(accountService.find(1l), userService.find(1l));
		Assert.assertTrue(b);
	}

//	@Test
//	public void testIsOwn(){
//		Message message = userService.isOwn(userService.find(1l), projectService.find(848l), accountService.find(1l));
//		Assert.assertTrue(message.getType()== Message.Type.OK);
//	}

}
