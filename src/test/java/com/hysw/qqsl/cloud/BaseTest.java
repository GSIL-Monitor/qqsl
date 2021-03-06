package com.hysw.qqsl.cloud;

import com.hysw.qqsl.cloud.core.service.UserService;
import com.hysw.qqsl.cloud.listener.TestExecutionListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.util.ThreadContext;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(value = {TestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@ContextConfiguration(locations = {"classpath*:/applicationContext-test.xml", "classpath*:/applicationContext-cache-test.xml","classpath*:/applicationContext-shiro-test.xml"})
@Transactional(transactionManager = "transactionManager")
@Rollback(value = false)
public class BaseTest {

	// log
	protected Log logger = LogFactory.getLog(getClass());
	@Autowired
	private UserService userService;



	@BeforeClass
	public static void testStart() {
	}


	@Resource
	org.apache.shiro.mgt.SecurityManager securityManager;

	@Before
	public void setUp() {
		System.out.println("start");
		ThreadContext.bind(securityManager);

	}
	@Test
	public void test() {
		Assert.assertTrue(true);
	}

	@After
	public void tearDown() throws Exception {
	}

	@AfterClass
	public static void testOver() {
	}
}
