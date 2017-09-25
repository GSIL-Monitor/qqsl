package com.hysw.qqsl.cloud.listener;

import com.hysw.qqsl.cloud.core.entity.Setting;
import com.hysw.qqsl.cloud.util.SettingUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * Created by flysic on 2017/8/31.
 */
public class MyCustomTestExecutionListener extends AbstractTestExecutionListener {

    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        Setting setting = SettingUtils.getInstance().getSetting();
        setting.setStatus("test");
    }
}
