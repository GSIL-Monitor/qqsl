package com.hysw.qqsl.cloud.core.entity.project;

import com.hysw.qqsl.cloud.core.entity.data.Account;

import java.io.Serializable;

/**
 * 阶段
 * 相对于当前用户对当前项目的权限
 * @author Administrator
 *
 */
public class Stage implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum Type {
		INVITE,
		PREPARATION,
		BUILDING,
		MAINTENANCE
	}

	private Stage() {}
	
	public Stage(Type type) {
		if (type==Type.INVITE) {
			this.elementVisit = new CooperateVisit(CooperateVisit.Type.VISIT_INVITE_ELEMENT);
			this.fileVisit = new CooperateVisit(CooperateVisit.Type.VISIT_INVITE_FILE);
		} else if (type==Type.PREPARATION) {
			this.elementVisit = new CooperateVisit(CooperateVisit.Type.VISIT_PREPARATION_ELEMENT);
			this.fileVisit = new CooperateVisit(CooperateVisit.Type.VISIT_PREPARATION_FILE);
		} else if (type==Type.BUILDING) {
			this.elementVisit = new CooperateVisit(CooperateVisit.Type.VISIT_BUILDING_ELEMENT);
			this.fileVisit = new CooperateVisit(CooperateVisit.Type.VISIT_BUILDING_FILE);
		} else if (type==Type.MAINTENANCE) {
			this.elementVisit = new CooperateVisit(CooperateVisit.Type.VISIT_MAINTENANCE_ELEMENT);
			this.fileVisit = new CooperateVisit(CooperateVisit.Type.VISIT_MAINTENANCE_FILE);
		}
	}

	/** 权限属性 */
	private CooperateVisit elementVisit;
	private CooperateVisit fileVisit;


	public final CooperateVisit getElementVisit() {
		return elementVisit;
	}

	public final CooperateVisit getFileVisit() {
		return fileVisit;
	}

	public boolean registerElement(Account account) {
		return elementVisit.register(account);
	}
	
	public void unRegisterElement() {
		elementVisit.unRegister();
	}
	
	public boolean registerFile(Account account){
		return fileVisit.register(account);
	}
	
	public void unRegisterFile(){
		fileVisit.unRegister();
	}
	
}
