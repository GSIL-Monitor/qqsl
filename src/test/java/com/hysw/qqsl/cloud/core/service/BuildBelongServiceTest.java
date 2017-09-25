package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.service.BuildBelongService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class BuildBelongServiceTest extends BaseTest {
    @Autowired
    private BuildBelongService buildBelongService;

    @Test
    public void testBuildBelongToCoordinate(){
        buildBelongService.buildBelongToCoordinate();
        buildBelongService.findAllCoordinateIdIsNULL();
    }
}
