package com.hysw.qqsl.cloud.core.service;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.entity.data.Note;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Create by leinuo on 18-4-25 上午10:05
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 */
public class NoteServiceTest extends BaseTest{

    @Autowired
    private NoteService noteService;



    @Test
    public void sendSmsTest() throws Exception{
        SendSmsResponse sendSmsResponse =    noteService.sendSms("18661925010","qqsl");
    }

    @Test
    public void testFindByPhone() {
        Note note = noteService.findByPhone("13007781310");
        System.out.println(note);
    }


}
