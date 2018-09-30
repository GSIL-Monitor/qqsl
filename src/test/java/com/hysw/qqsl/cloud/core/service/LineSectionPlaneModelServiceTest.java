package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.entity.builds.Line;
import com.hysw.qqsl.cloud.core.entity.builds.LineSectionPlaneModel;
import org.dom4j.DocumentException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author Administrator
 * @since 2018/9/19
 */
public class LineSectionPlaneModelServiceTest extends BaseTest {
    @Autowired
    private LineSectionPlaneModelService lineSectionPlaneModelService;
    @Autowired
    private LineService lineService;

    @Test
    public void test0001() throws DocumentException {
        List<LineSectionPlaneModel> lineSectionPlaneModel = lineSectionPlaneModelService.getLineSectionPlaneModel();
        System.out.println();
    }

    @Test
    public void test0002(){
        List<Line> lines = lineService.getLines();
        System.out.println();
    }


}
