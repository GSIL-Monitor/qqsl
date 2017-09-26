package com.hysw.qqsl.cloud.controller;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by Administrator on 2016/10/20.
 */
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
