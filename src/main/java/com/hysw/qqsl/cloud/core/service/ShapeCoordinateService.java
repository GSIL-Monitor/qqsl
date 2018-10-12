package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.ShapeCoordinateDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
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
        for (ShapeCoordinate shapeCoordinate : shapeCoordinates) {
            jsonObject = new JSONObject();
            jsonObject.put("id", shapeCoordinate.getId());
            jsonObject.put("lon", shapeCoordinate.getLon());
            jsonObject.put("lat", shapeCoordinate.getLat());
            jsonObject.put("elevations", shapeCoordinate.getElevations());
            jsonObject.put("buildId", shapeCoordinate.getBuild().getId());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }
}
