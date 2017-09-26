package com.hysw.qqsl.cloud.dao;

import com.hysw.qqsl.cloud.entity.data.Contact;
import org.springframework.stereotype.Repository;



@Repository("contactDao")
public class ContactDao extends BaseDao<Contact, Long> {

}
