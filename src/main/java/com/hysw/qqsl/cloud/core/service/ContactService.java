package com.hysw.qqsl.cloud.core.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hysw.qqsl.cloud.core.dao.ContactDao;
import com.hysw.qqsl.cloud.core.entity.element.Element;
import com.hysw.qqsl.cloud.core.entity.element.ElementGroup;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.element.Unit;
import com.hysw.qqsl.cloud.core.entity.data.Contact;
import com.hysw.qqsl.cloud.core.entity.data.Contact.Type;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.util.SettingUtils;

@Service("contactService")
public class ContactService extends BaseService<Contact, Long> {
	@Autowired
	private ContactDao contactDao;

	@Autowired
	public void setBaseDao(ContactDao contactDao) {
		super.setBaseDao(contactDao);
	}

	/**
	 * 获取用户的某一类型通讯录列表
	 * @param user
	 * @param type
	 * @return
	 */
	public List<Contact> findByUser(User user, Type type) {
		List<Filter> filters = new ArrayList<Filter>();
		filters.add(Filter.eq("user", user.getId()));
		filters.add(Filter.eq("type", type.ordinal()));
		List<Contact> contacts = contactDao.findList(0, null, filters);
		return contacts;
	}

	/**
	 * 获取用户的所有通讯录列表
	 * @param user
	 * @return
	 */
	public List<Contact> findByUser(User user){
		List<Filter> filters = new ArrayList<Filter>();
		filters.add(Filter.eq("user", user.getId()));
		List<Contact> contacts = contactDao.findList(0, null, filters);
		return contacts;
	}
	/**
	 * 根据单元构建通讯录List
	 * 
	 * @param unit
	 * @param elementGroup
	 * @return
	 */
	public void findPhase(Unit unit, ElementGroup elementGroup) {
		List<Contact> contacts = new ArrayList<Contact>();
		if (elementGroup.getElements().get(0).getDescription() == null) {
			return;
		}
		if (SettingUtils.stringMatcher("CONTACTS_OWN_MASTER", elementGroup
				.getElements().get(0).getDescription())) {
			contacts = findByUser(unit.getProject().getUser(),
					Contact.Type.OWN_MASTER);
		}
		if (SettingUtils.stringMatcher("CONTACTS_OWN_NAME", elementGroup
				.getElements().get(0).getDescription())) {
			contacts = findByUser(unit.getProject().getUser(),
					Contact.Type.OWN_NAME);
		}
		if (SettingUtils.stringMatcher("CONTACTS_CON", elementGroup
				.getElements().get(0).getDescription())) {
			contacts = findByUser(unit.getProject().getUser(), Contact.Type.CON);
		}
		if (SettingUtils.stringMatcher("CONTACTS_DESIGN", elementGroup
				.getElements().get(0).getDescription())) {
			contacts = findByUser(unit.getProject().getUser(),
					Contact.Type.DESIGN);
		}
		if (SettingUtils.stringMatcher("CONTACTS_SUP", elementGroup
				.getElements().get(0).getDescription())) {
			contacts = findByUser(unit.getProject().getUser(), Contact.Type.SUP);
		}
		if (SettingUtils.stringMatcher("CONTACTS_QC", elementGroup
				.getElements().get(0).getDescription())) {
			contacts = findByUser(unit.getProject().getUser(), Contact.Type.QC);
		}
		if (SettingUtils.stringMatcher("CONTACTS_OTHER", elementGroup
				.getElements().get(0).getDescription())) {
			contacts = findByUser(unit.getProject().getUser(),
					Contact.Type.OTHER);
		}
		unit.setContacts(contacts);

	}

	/**
	 * 构建通讯录负责人选择值
	 * 
	 * @param user
	 * @param type
	 * @return
	 */
	public List<String> getContactsNameSelect(User user, Type type) {
		List<Contact> contacts;
		List<String> contactSelect = new ArrayList<String>();
		String name = "";
		contacts = findByUser(user, type);
		for (int i = 0; i < contacts.size(); i++) {
			name = contacts.get(i).getName();
			contactSelect.add(name);
		}
		return contactSelect;
	}

	/**
	 * 保存通讯录
	 * 
	 * @param contactMap
	 * @param user
	 */
	public void doSaveContact(Map<String, Object> contactMap, User user) {
		if (contactMap.get("type") == null) {
			return;
		}
		if (contactMap.get("name") == null
				|| contactMap.get("name").toString().trim() == ""
				|| contactMap.get("company") == null
				|| contactMap.get("company").toString().trim() == "") {
			return;
		}
		Contact contact;
		if (contactMap.get("id") == null) {
			contact = new Contact();
			contact.setType(Contact.Type.valueOf(contactMap.get("type")
					.toString()));
		} else {
			long id = Long.valueOf(contactMap.get("id").toString());
			contact = find(id);
		}
		if (contactMap.get("qualify") != null) {
			contact.setQualify(contactMap.get("qualify").toString());
		}
		if (contactMap.get("depart") != null) {
			contact.setDepart(contactMap.get("depart").toString());
		}
		if (contactMap.get("company") != null) {
			contact.setCompany(contactMap.get("company").toString());
		}
		if (contactMap.get("master") != null) {
			contact.setMaster(contactMap.get("master").toString());
		}
		if (contactMap.get("masterPhone") != null) {
			contact.setMasterPhone(contactMap.get("masterPhone").toString());
		}
		if (contactMap.get("masterEmail") != null) {
			contact.setMasterEmail(contactMap.get("masterEmail").toString());
		}
		if (contactMap.get("name") != null) {
			contact.setName(contactMap.get("name").toString());
		}
		if (contactMap.get("phone") != null) {
			contact.setPhone(contactMap.get("phone").toString());
		}
		if (contactMap.get("email") != null) {
			contact.setEmail(contactMap.get("email").toString());
		}
		if (contact != null) {
			contact.setUser(user);
			doSaveContact(contact);
		}
	}

	/**
	 * 保存通讯录
	 * 
	 * @param contact
	 */
	public void doSaveContact(Contact contact) {
		if (contact.getId() != null) {
			save(contact);
		} else {
			// 看看是否有重复的通讯录
			Contact contact1 = findByUserNameAndCompany(contact);
			if (contact1.getUser() != null&& contact1.getName() != null) {
				save(contact1);
			}
		}
	}

	/**
	 * 判断同一用户下是否有相同数据的通讯录
	 * 
	 * @param contact
	 * @return
	 */
	private Contact findByUserNameAndCompany(Contact contact) {
		List<Filter> filters = new ArrayList<Filter>();
		filters.add(Filter.eq("user", contact.getUser().getId()));
		filters.add(Filter.eq("type", contact.getType().ordinal()));
		filters.add(Filter.eq("company", contact.getCompany()));
		filters.add(Filter.eq("name", contact.getName()));
		List<Contact> contacts = contactDao.findList(0, null, filters);
		if (contacts.size() == 0) {
			return contact;
		}
		return new Contact();
	}

	/**
	 * 得到对应单元中中的contactNameSelect，并绑定到对应的要素选择值上
	 * 
	 * @param element
	 * @param user
	 */
	public void contactNameSelect(Element element, User user) {
		if (element.getDescription() != null
				&& SettingUtils.stringMatcher("CONTACTS_OWN_MASTER",
						element.getDescription())
				&& SettingUtils.stringMatcher("name", element.getDescription())) {
			element.setSelects(getContactsNameSelect(user,
					Contact.Type.OWN_MASTER));
		}
		if (element.getDescription() != null
				&& SettingUtils.stringMatcher("CONTACTS_OWN_NAME",
						element.getDescription())
				&& SettingUtils.stringMatcher("name", element.getDescription())) {
			element.setSelects(getContactsNameSelect(user,
					Contact.Type.OWN_NAME));
		}
		if (element.getDescription() != null
				&& SettingUtils.stringMatcher("CONTACTS_CON",
						element.getDescription())
				&& SettingUtils.stringMatcher("name", element.getDescription())) {
			element.setSelects(getContactsNameSelect(user, Contact.Type.CON));
		}
		if (element.getDescription() != null
				&& SettingUtils.stringMatcher("CONTACTS_DESIGN",
						element.getDescription())
				&& SettingUtils.stringMatcher("name", element.getDescription())) {
			element.setSelects(getContactsNameSelect(user, Contact.Type.DESIGN));
		}
		if (element.getDescription() != null
				&& SettingUtils.stringMatcher("CONTACTS_OTHER",
						element.getDescription())
				&& SettingUtils.stringMatcher("name", element.getDescription())) {
			element.setSelects(getContactsNameSelect(user, Contact.Type.OTHER));
		}
		if (element.getDescription() != null
				&& SettingUtils.stringMatcher("CONTACTS_QC",
						element.getDescription())
				&& SettingUtils.stringMatcher("name", element.getDescription())) {
			element.setSelects(getContactsNameSelect(user, Contact.Type.QC));
		}
		if (element.getDescription() != null
				&& SettingUtils.stringMatcher("CONTACTS_SUP",
						element.getDescription())
				&& SettingUtils.stringMatcher("name", element.getDescription())) {
			element.setSelects(getContactsNameSelect(user, Contact.Type.SUP));
		}
	}

	/**
	 * 得到对应单元中中的contactCompanySelect，并绑定到对应的要素选择值上
	 * 
	 * @param element
	 * @param user
	 */
	public void contactCompanySelect(Element element, User user) {
		if (element.getDescription() != null
				&& SettingUtils.stringMatcher("CONTACTS_OWN_MASTER",
						element.getDescription())
				&& SettingUtils.stringMatcher("company",
						element.getDescription())) {
			element.setSelects(getContactsCompanySelect(user,
					Contact.Type.OWN_MASTER));
		}
		if (element.getDescription() != null
				&& SettingUtils.stringMatcher("CONTACTS_OWN_NAME",
						element.getDescription())
				&& SettingUtils.stringMatcher("company",
						element.getDescription())) {
			element.setSelects(getContactsCompanySelect(user,
					Contact.Type.OWN_NAME));
		}
		if (element.getDescription() != null
				&& SettingUtils.stringMatcher("CONTACTS_CON",
						element.getDescription())
				&& SettingUtils.stringMatcher("company",
						element.getDescription())) {
			element.setSelects(getContactsCompanySelect(user, Contact.Type.CON));
		}
		if (element.getDescription() != null
				&& SettingUtils.stringMatcher("CONTACTS_DESIGN",
						element.getDescription())
				&& SettingUtils.stringMatcher("company",
						element.getDescription())) {
			element.setSelects(getContactsCompanySelect(user,
					Contact.Type.DESIGN));
		}
		if (element.getDescription() != null
				&& SettingUtils.stringMatcher("CONTACTS_OTHER",
						element.getDescription())
				&& SettingUtils.stringMatcher("company",
						element.getDescription())) {
			element.setSelects(getContactsCompanySelect(user,
					Contact.Type.OTHER));
		}
		if (element.getDescription() != null
				&& SettingUtils.stringMatcher("CONTACTS_QC",
						element.getDescription())
				&& SettingUtils.stringMatcher("company",
						element.getDescription())) {
			element.setSelects(getContactsCompanySelect(user, Contact.Type.QC));
		}
		if (element.getDescription() != null
				&& SettingUtils.stringMatcher("CONTACTS_SUP",
						element.getDescription())
				&& SettingUtils.stringMatcher("company",
						element.getDescription())) {
			element.setSelects(getContactsCompanySelect(user, Contact.Type.SUP));
		}

	}

	private List<String> getContactsCompanySelect(User user, Type type) {
		List<Contact> contacts;
		List<String> contactSelect = new ArrayList<String>();
		String company = "";
		contacts = findByUser(user, type);
		if (contacts.size() == 0) {
			return null;
		}
		for (int i = 0; i < contacts.size(); i++) {
			company = contacts.get(i).getCompany();
			if (contactSelect.contains(company) == false) {
				contactSelect.add(company);
			}
		}
		return contactSelect;
	}

}
