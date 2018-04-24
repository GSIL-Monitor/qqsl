package com.hysw.qqsl.cloud.core.dao;

import com.hysw.qqsl.cloud.core.entity.data.Note;
import org.springframework.stereotype.Repository;

/**
 * @anthor Administrator
 * @since 16:29 2018/4/18
 */
@Repository("noteDao")
public class NoteDao extends BaseDao<Note, Long> {
}
