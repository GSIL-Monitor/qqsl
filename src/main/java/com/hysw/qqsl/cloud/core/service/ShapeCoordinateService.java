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
            if (shapeCoordinate.getBuild() != null) {
                jsonObject.put("buildId", shapeCoordinate.getBuild().getId());
            }
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    /**
     * 根据shapeCoordinateId删除对应数据
     * @param shapeCoordinate
     */
    public void deleteShapeCoordinateById(ShapeCoordinate shapeCoordinate) {
        if (shapeCoordinate.getParent()==null) {
            ShapeCoordinate next = shapeCoordinate.getNext();
            next.setParent(null);
            save(next);
        } else {
            ShapeCoordinate parent = shapeCoordinate.getParent();
            ShapeCoordinate next = shapeCoordinate.getNext();
            if (parent != null) {
                parent.setNext(next);
                save(parent);
            }
            if (next != null) {
                next.setParent(parent);
                save(next);
            }
        }
        remove(shapeCoordinate);
    }

    public JSONObject getCoordinateDetails(ShapeCoordinate shapeCoordinate) {
        JSONObject jsonObject = new JSONObject(), jsonObject1;
        jsonObject.put("id", shapeCoordinate.getId());
        jsonObject.put("lon", shapeCoordinate.getLon());
        jsonObject.put("lat", shapeCoordinate.getLat());
        jsonObject.put("elevations", JSONArray.fromObject(shapeCoordinate.getElevations()));
        if (shapeCoordinate.getBuild() != null) {
            jsonObject1 = new JSONObject();
            jsonObject1.put("id", shapeCoordinate.getBuild().getId());
            jsonObject1.put("name", shapeCoordinate.getBuild().getType().getTypeC());
            jsonObject1.put("childType", shapeCoordinate.getBuild().getChildType() == null ? null : shapeCoordinate.getBuild().getChildType());
            jsonObject1.put("type", shapeCoordinate.getBuild().getType());
            jsonObject.put("build", jsonObject1);
        }
        return jsonObject;
    }
}
