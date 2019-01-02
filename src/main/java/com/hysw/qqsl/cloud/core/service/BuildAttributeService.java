package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.BuildAttributeDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.data.Build;
import com.hysw.qqsl.cloud.core.entity.data.BuildAttribute;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenl on 17-4-10.
 */
@Service("buildAttributeService")
public class BuildAttributeService extends BaseService<BuildAttribute,Long> {
    @Autowired
    private BuildAttributeDao buildAttributeDao;
    @Autowired
    public void setBaseDao(BuildAttributeDao buildAttributeDao) {
        super.setBaseDao(buildAttributeDao);
    }

    public List<BuildAttribute> findByNewBuild(Build build1) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("build", build1));
        return buildAttributeDao.findList(0,null,filters);
    }

    public List<BuildAttribute> findByBuild(Build build) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("build", build));
        return buildAttributeDao.findList(0, null, filters);
    }

    public String editBuildAttribute(Build build, Object buildAttributes) {
        if (buildAttributes != null && !buildAttributes.toString().equals("")) {
            BuildAttribute buildAttribute1 = null;
            for (Object buildAttribute : JSONArray.fromObject(buildAttributes)) {
                JSONObject jsonObject1 = JSONObject.fromObject(buildAttribute);
                if (jsonObject1.get("id") != null) {
                    buildAttribute1 = find(Long.valueOf(jsonObject1.get("id").toString()));
                    if (buildAttribute1 == null) {
                        return "2";
                    }
                    if (!buildAttribute1.getBuild().getId().equals(build.getId())) {
                        return null;
                    }
                    buildAttribute1.setValue(jsonObject1.get("value").toString());
                } else if (jsonObject1.get("alias").equals("position") || jsonObject1.get("alias").equals("designElevation") || jsonObject1.get("alias").equals("remark") || jsonObject1.get("alias").equals("center")) {
                    JSONObject jsonObject;
                    if (jsonObject1.get("alias").equals("center")) {
                        continue;
                    }
                    if (jsonObject1.get("alias").equals("position") && jsonObject1.get("value") != null) {
                        String[] centers = jsonObject1.get("value").toString().split(",");
                        if (centers.length != 2) {
                            return "";
                        }
                        jsonObject = new JSONObject();
                        jsonObject.put("lon", centers[0]);
                        jsonObject.put("lat", centers[1]);
                        build.setPositionCoor(jsonObject.toString());
                    }
                    if (jsonObject1.get("alias").equals("designElevation") && jsonObject1.get("value") != null) {
                        build.setDesignElevation(jsonObject1.get("value").toString());
                    }
                    if (jsonObject1.get("alias").equals("remark") && jsonObject1.get("value") != null) {
                        build.setRemark(jsonObject1.get("value").toString());
                    }
                } else {
                    buildAttribute1 = new BuildAttribute();
                    buildAttribute1.setValue(jsonObject1.get("value").toString());
                    buildAttribute1.setAlias(jsonObject1.get("alias").toString());
                    buildAttribute1.setBuild(build);
                }
                if (buildAttribute1 != null) {
                    save(buildAttribute1);
                }
            }
        }
        return "1";
    }
}
