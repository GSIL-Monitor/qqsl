package com.hysw.qqsl.cloud.service;

import com.hysw.qqsl.cloud.BaseTest;
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
