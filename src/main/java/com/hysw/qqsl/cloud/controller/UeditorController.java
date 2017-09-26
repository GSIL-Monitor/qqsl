/**
 * 
 */
package com.hysw.qqsl.cloud.controller;

import com.hysw.qqsl.cloud.ueditor.ActionEnter;
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

}