package com.hysw.qqsl.cloud.core.controller;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.service.PanoramaConfigService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

/**
 * Create by leinuo on 18-4-8 下午3:08
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 */
@Controller
@RequestMapping("/panorama")
public class PanoramaController {
    @Autowired
    private PanoramaConfigService panoramaConfigService;


    @RequestMapping(value = "/tour.xml/{instanceId}", method = RequestMethod.GET,produces="application/xml; charset=UTF-8")
    public
    @ResponseBody
    void getTour(HttpServletResponse httpResponse,@PathVariable("instanceId") String instanceId) {
        String tourStr = panoramaConfigService.getTour(instanceId);
        writer(httpResponse,tourStr);
    }

    @RequestMapping(value = "/skin.xml", method = RequestMethod.GET,produces="application/xml; charset=UTF-8")
    public
    @ResponseBody
    void getskin(HttpServletResponse httpResponse) {
        String skinStr = panoramaConfigService.getSkin();
        writer(httpResponse,skinStr);
    }
    private void writer(HttpServletResponse httpResponse,String xmlStr){
        try {
            httpResponse.setContentType("text/xml;charset=" + CommonAttributes.CHARSET);
            httpResponse.getWriter().write(xmlStr);
            httpResponse.getWriter().flush();
            httpResponse.getWriter().close();
        }catch (Exception exceptioe){

        }
    }
    @RequestMapping(value = "/{instanceId}", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getPanoramaConfig(@PathVariable("instanceId") String instanceId) {
        JSONObject jsonObject = panoramaConfigService.get(instanceId);
        return new Message(Message.Type.OK,jsonObject);
    }

}
