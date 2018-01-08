package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.BuildDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.build.AttribeGroup;
import com.hysw.qqsl.cloud.core.entity.data.Attribe;
import com.hysw.qqsl.cloud.core.entity.data.Build;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leinuo on 17-4-13.
 */
@Service("buildService")
public class BuildService extends BaseService<Build,Long> {

    @Autowired
    private BuildDao buildDao;
    @Autowired
    private BuildGroupService buildGroupService;
    @Autowired
    private FieldService fieldService;
    @Autowired
    public void setBaseDao(BuildDao buildDao) {
        super.setBaseDao( buildDao);
    }

    public List<Build> findByProject(Project project) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("project", project));
        filters.add(Filter.eq("cut", false));
        List<Build> list = buildDao.findList(0, null, filters);
        for (Build build : list) {
            build.getId();
            if (build.getAttribeList() == null) {
                continue;
            }
            for (Attribe attribe : build.getAttribeList()) {
                attribe.getId();
            }
        }
        return list;
    }

    @Override
    public Build find(Long id){
        Build build = super.find(id);
        if (build == null) {
            return null;
        }
        build.getId();
        for (Attribe attribe : build.getAttribeList()) {
            attribe.getId();
        }
        return build;
    }
    //      public void findByProject() {
//        List<Filter> filters = new ArrayList<>();
//        filters.add(Filter.eq(""))
//    }

    public List<Build> findByProjectAndAlias(Project project) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("project", project));
//        filters.add(Filter.eq("alias", alias));
        filters.add(Filter.eq("cut", false));
        List<Build> list = buildDao.findList(0, null, filters);
        return list;
    }


    public List<Build> findByProjectAndSource(Project project, Build.Source source) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("project", project));
        filters.add(Filter.eq("source", source));
        filters.add(Filter.eq("cut", false));
        List<Build> list = buildDao.findList(0, null, filters);
        for (Build build : list) {
            build.getId();
            if (build.getAttribeList() == null) {
                continue;
            }
            for (Attribe attribe : build.getAttribeList()) {
                attribe.getId();
            }
        }
        return list;
    }

    public List<Build> findByProjectAndSourceCoordinateId(Project project, Build.Source source,Long coordinateId) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("project", project));
        filters.add(Filter.eq("source", source));
        filters.add(Filter.eq("coordinateId", coordinateId));
        filters.add(Filter.eq("cut", false));
        List<Filter> filters1 = new ArrayList<>();
        filters1.add(Filter.eq("project", project));
        filters1.add(Filter.eq("source", source));
        filters1.add(Filter.isNull("coordinateId"));
        filters1.add(Filter.eq("cut", false));
        List<Build> list = buildDao.findList(0, null, filters,filters1);
        for (Build build : list) {
            build.getId();
            if (build.getAttribeList() == null) {
                continue;
            }
            for (Attribe attribe : build.getAttribeList()) {
                attribe.getId();
            }
        }
        return list;
    }

    public JSONObject buildJson(Build build2) {
        Build build = null;
        List<Build> builds1 = buildGroupService.getBuildsDynamic();
        for (Build build1 : builds1) {
            if (build2.getType().equals(build1.getType())) {
                build = (Build) SettingUtils.objectCopy(build1);
                fieldService.setProperty(build,build2,true);
                break;
            }
        }
        JSONObject jsonObject;
        JSONObject jsonObject1;
        jsonObject = new JSONObject();
        jsonObject.put("id", build.getId());
        jsonObject.put("name", build.getName());
        jsonObject.put("alias", build.getAlias());
        jsonObject.put("type", build.getType());
        jsonObject.put("centerCoor", build.getCenterCoor());
        jsonObject.put("positionCoor", build.getPositionCoor());
        jsonObject.put("remark", build.getRemark());
        jsonObject1 = new JSONObject();
        writeAttribeGroup(build.getMaterAttribeGroup(),jsonObject1);
        if (!jsonObject1.isEmpty()) {
            jsonObject.put("materAttribeGroup", jsonObject1);
        }
        jsonObject1 = new JSONObject();
        writeAttribeGroup(build.getDimensionsAttribeGroup(),jsonObject1);
        if (!jsonObject1.isEmpty()) {
            jsonObject.put("dimensionsAttribeGroup", jsonObject1);
        }
        jsonObject1 = new JSONObject();
        writeAttribeGroup(build.getHydraulicsAttribeGroup(),jsonObject1);
        if (!jsonObject1.isEmpty()) {
            jsonObject.put("hydraulicsAttribeGroup", jsonObject1);
        }
        jsonObject1 = new JSONObject();
        writeAttribeGroup(build.getGeologyAttribeGroup(),jsonObject1);
        if (!jsonObject1.isEmpty()) {
            jsonObject.put("geologyAttribeGroup", jsonObject1);
        }
        jsonObject1 = new JSONObject();
        writeAttribeGroup(build.getStructureAttribeGroup(),jsonObject1);
        if (!jsonObject1.isEmpty()) {
            jsonObject.put("structureAttribeGroup", jsonObject1);
        }
        return jsonObject;
    }

    private void writeAttribeGroup(AttribeGroup attribeGroup, JSONObject jsonObject) {
        if (attribeGroup == null) {
            return;
        }
        JSONArray jsonArray;
        jsonArray = new JSONArray();
        writeAttribe(attribeGroup.getAttribes(),jsonArray);
        if (!jsonArray.isEmpty()) {
            jsonObject.put("name", attribeGroup.getName());
            jsonObject.put("alias", attribeGroup.getAlias());
            jsonObject.put("status", attribeGroup.getStatus());
            jsonObject.put("attribes", jsonArray);
        }
        jsonArray = new JSONArray();
        writeChild(attribeGroup.getChilds(), jsonArray);
        if (!jsonArray.isEmpty()) {
            jsonObject.put("name", attribeGroup.getName());
            jsonObject.put("alias", attribeGroup.getAlias());
            jsonObject.put("status", attribeGroup.getStatus());
            jsonObject.put("child", jsonArray);
        }
    }

    private void writeChild(List<AttribeGroup> attribeGroups, JSONArray jsonArray) {
        JSONObject jsonObject;
        if (attribeGroups == null) {
            return;
        }
        for (AttribeGroup attribeGroup : attribeGroups) {
            jsonObject = new JSONObject();
            writeAttribeGroup(attribeGroup, jsonObject);
            if (!jsonObject.isEmpty()) {
                jsonArray.add(jsonObject);
            }
        }
    }

    private void writeAttribe(List<Attribe> attribes, JSONArray jsonArray) {
        if (attribes == null) {
            return;
        }
        JSONObject jsonObject;
        for (Attribe attribe : attribes) {
            if (attribe.getValue() == null) {
                continue;
            }
            jsonObject = new JSONObject();
            jsonObject.put("id", attribe.getId());
            jsonObject.put("name", attribe.getName());
            jsonObject.put("alias", attribe.getAlias());
            jsonObject.put("type", attribe.getType());
            jsonObject.put("value", attribe.getValue());
            jsonObject.put("code", attribe.getCode());
            if (attribe.getSelects() != null && attribe.getSelects().size() != 0) {
                jsonObject.put("selects", attribe.getSelects());
            }
            if (attribe.getUnit() != null && !attribe.getUnit().equals("")) {
                jsonObject.put("unit", attribe.getUnit());
            }
            jsonArray.add(jsonObject);
        }
    }

    public List<Build> findByCoordinateId(long id) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("coordinateId", id));
        filters.add(Filter.eq("cut", false));
        return buildDao.findList(0, null, filters);
     }

    public void removes(List<Build> builds) {
        for (Build build : builds) {
            remove(build);
        }
    }

    public List<Build> findByCoordinateIdNULL() {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.isNull("coordinateId"));
        filters.add(Filter.eq("cut", false));
        return buildDao.findList(0, null, filters);
    }

    public List<Build> findByCutIsTrue() {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("cut", true));
        return buildDao.findList(0, null, filters);
    }

    public void deleteSimpleBuild(){
        List<Build> builds = findByCutIsTrue();
        for (Build build : builds) {
            try {
                remove(build);
            }catch (Exception e){
                continue;
            }
        }
    }
}
