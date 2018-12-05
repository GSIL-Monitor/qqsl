package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.entity.buildModel.AttributeGroup;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by flysic on 17-4-11.
 */
public class AttributeGroupServiceTest extends BaseTest {

    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private AttributeGroupService attributeGroupService;


}