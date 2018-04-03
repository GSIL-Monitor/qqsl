/**
 * 
 */
package com.hysw.qqsl.cloud.core.controller;

import com.hysw.qqsl.cloud.core.ueditor.ActionEnter;
import com.hysw.qqsl.cloud.krpano.service.KrpanoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;



/**
 * @author XP
 */
@Controller
public class UeditorController {

	@Autowired
	private KrpanoService krpanoService;
	@RequestMapping("/ueditor/config")
//	@RequiresPermissions(value={"admin:article:add","admin:article:query","admin:article:delete","admin:article:upload"},logical=Logical.OR)
	public void config(HttpServletRequest request, HttpServletResponse response, String action) {
		response.setContentType("application/json");
		String rootPath = request.getSession().getServletContext().getRealPath("/");
		try {
			String exec = new ActionEnter(request, rootPath).exec();
			PrintWriter writer = response.getWriter();
			writer.write(exec);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@RequestMapping("/krpano")
	public void download(){
		krpanoService.makeKrpano("krpano/webwxgetmsgimg.jpg");
	}

}