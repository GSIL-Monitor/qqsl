package com.hysw.qqsl.cloud.core.controller;

import org.junit.Ignore;
import org.junit.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by Administrator on 2016/10/20.
 */
@Ignore
public class ElementGroupControllerTest extends BaseControllerTest {

    public ElementGroupControllerTest() {

    }

    @Test
    public void testView() throws Exception {
        super.mockMvc.perform(get("/project/infos")).
                andExpect(status().isOk()).andReturn();

    }

    @Test
    public void testView1() throws Exception {
        super.mockMvc.perform(get("/project/infos")).
                andExpect(status().isOk()).andReturn();
    }

    @Test
    public void testView2() throws Exception {
        super.mockMvc.perform(get("/project/infos")).
                andExpect(status().isOk()).andReturn();
    }
}
