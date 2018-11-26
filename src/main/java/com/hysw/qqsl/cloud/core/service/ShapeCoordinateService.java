package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.ShapeCoordinateDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.data.Build;
import com.hysw.qqsl.cloud.core.entity.data.Shape;
import com.hysw.qqsl.cloud.core.entity.data.ShapeCoordinate;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @since 2018/9/20
 */
@Service("shapeCoordinateService")
public class ShapeCoordinateService extends BaseService<ShapeCoordinate, Long> {
    @Autowired
    private ShapeCoordinateDao shapeCoordinateDao;
    @Autowired
    private BuildService buildService;

    @Autowired
    public void setBaseDao(ShapeCoordinateDao shapeCoordinateDao) {
        super.setBaseDao(shapeCoordinateDao);
    }

    public List<ShapeCoordinate> findByShape(Shape shape) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("shape", shape));
        return shapeCoordinateDao.findList(0, null, filters);
    }

    public JSONArray toJSON(List<ShapeCoordinate> shapeCoordinates) {
        JSONObject jsonObject;
        JSONArray jsonArray = new JSONArray();
        Build build;
        for (ShapeCoordinate shapeCoordinate : shapeCoordinates) {
            jsonObject = new JSONObject();
            jsonObject.put("id", shapeCoordinate.getId());
            jsonObject.put("lon", shapeCoordinate.getLon());
            jsonObject.put("lat", shapeCoordinate.getLat());
            jsonObject.put("elevations", shapeCoordinate.getElevations());
            build = buildService.findByShapeCoordinate(shapeCoordinate);
            if (build != null) {
                jsonObject.put("buildId", build.getId());
            }
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    public ShapeCoordinate findByBuild(Build build) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("build", build));
        List<ShapeCoordinate> list = shapeCoordinateDao.findList(0, null, filters);
        if (list.size() == 1) {
            return list.get(0);
        }
        return null;
    }
}
