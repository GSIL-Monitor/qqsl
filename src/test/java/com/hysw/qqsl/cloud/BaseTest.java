package com.hysw.qqsl.cloud;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.UserService;
import com.hysw.qqsl.cloud.listener.MyCustomTestExecutionListener;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(value = {MyCustomTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@ContextConfiguration(locations = {"classpath*:/applicationContext-test.xml", "classpath*:/applicationContext-cache-test.xml"})
@Transactional(transactionManager = "transactionManager")
@Rollback(value = true)
public class BaseTest {
	protected Log logger = LogFactory.getLog(getClass());
	@Autowired
	private UserService userService;

	@BeforeClass
	public static void testStart() {
		System.out.println("unitTest start!");
	}

	@Before
	public void setUp() {
		System.out.println("start");
		User user = userService.findByUserName(com.hysw.qqsl.cloud.CommonTest.USER_NAME);
		if (user.getId() == null) {
			user.setName(CommonTest.NAME);
			user.setUserName(CommonTest.USER_NAME);
			user.setEmail(CommonTest.EMAIL);
			user.setPassword(DigestUtils.md5Hex(CommonTest.PASSWORD));
//			user.setType(User.Type.USER);
			userService.save(user);
		}
//		User admin = userService.findByUserName(CommonTest.ADMIN_NAME);
//		if (admin.getId() ==null) {
//			admin.setName(CommonTest.NAME);
//			admin.setUserName(CommonTest.ADMIN_NAME);
//			admin.setEmail(CommonTest.EMAIL);
//			admin.setPassword(DigestUtils.md5Hex(CommonTest.PASSWORD));
//			admin.setType(User.Type.ADMIN);
//			userService.save(admin);
//		}
	}

	@Test
	public void test() {
		Assert.assertTrue(true);
	}

	@After
	public void tearDown() throws Exception {
		System.out.println("over");
	}

	@AfterClass
	public static void testOver() {
		System.out.println("unitTest over!");
	}
}
