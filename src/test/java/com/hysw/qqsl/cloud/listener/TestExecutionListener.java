package com.hysw.qqsl.cloud.listener;

import com.hysw.qqsl.cloud.core.entity.Setting;
import com.hysw.qqsl.cloud.util.SettingUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * 运行测试用例监听
 *
 * @since 2017年10月5日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 */
public class TestExecutionListener extends AbstractTestExecutionListener {

    /**
     * 运行测试用例前，修改运行状态
     * @param testContext
     */
    @Override
    public void beforeTestClass(TestContext testContext) {
        Setting setting = SettingUtils.getInstance().getSetting();
        setting.setStatus("test");
    }
}
