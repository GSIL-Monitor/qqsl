package com.hysw.qqsl.cloud.service;

import com.hysw.qqsl.cloud.entity.data.Build;
import com.hysw.qqsl.cloud.entity.data.Coordinate;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 建筑物归属线面
 * Created by chenl on 17-5-2.
 */
@Service("buildBelongService")
public class BuildBelongService implements Runnable{
    @Autowired
    private BuildService buildService;
    @Autowired
    private CoordinateService coordinateService;
    @Autowired
    private FieldService fieldService;
    private List<Long> ids = new ArrayList<>();

    Log logger = LogFactory.getLog(getClass());

    /**
     * 将线面的id写入build表中的coordinateId中，进行绑定
     */
    void buildBelongToCoordinate(){
        while (ids.size() > 0) {
            Build build = buildService.find(ids.get(0));
            if (build == null) {
                ids.remove(0);
            }
            List<Build> builds = new ArrayList<>();
            builds.add(build);
            boolean flag;
            List<Coordinate> coordinates = coordinateService.findByProject(build.getProject());
            for (Coordinate coordinate : coordinates) {
                flag = false;
                if (coordinate.getSource() == Build.Source.FIELD) {
                    JSONArray jsonArray = JSONArray.fromObject(coordinate.getCoordinateStr());
                    for (Object o : jsonArray) {
                        if (flag) {
                            break;
                        }
                        flag=isInLine(o, coordinate, builds, build, flag);
                    }
                }else{
                    //判断build是否包含在线内，是就在build表中填写相应的线id
                    flag=isInLine(coordinate.getCoordinateStr(),coordinate,builds,build,flag);
                }
                if (flag) {
                    break;
                }
            }
            ids.remove(0);
        }
    }

    private boolean isInLine(Object str, Coordinate coordinate, List<Build> builds, Build build, boolean flag) {
        JSONObject jsonObject = JSONObject.fromObject(str);
        Object o = jsonObject.get("coordinate");
        JSONArray jsonArray = JSONArray.fromObject(o);
        for (Object o1 : jsonArray) {
            JSONObject jsonObject1 = JSONObject.fromObject(o1);
            String longitude = jsonObject1.get("longitude").toString();
            String latitude = jsonObject1.get("latitude").toString();
            String elevation = jsonObject1.get("elevation").toString();
            Build build1 = fieldService.allEqual(builds, longitude, latitude, elevation);
            if (build1 != null) {
                build.setCoordinateId(coordinate.getId().toString());
                buildService.save(build);
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
     * 从数据库中查询build表中coordinateId为null的数据
     */
    void findAllCoordinateIdIsNULL(){
        List<Build> list = buildService.findByCoordinateIdNULL();
        for (Build build : list) {
            ids.add(build.getId());
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (ids.size() == 0) {
                    Thread.sleep(10000);
                    findAllCoordinateIdIsNULL();
                }
                buildBelongToCoordinate();
            } catch (Exception e) {
                ids.remove(0);
            }

        }
    }

}
