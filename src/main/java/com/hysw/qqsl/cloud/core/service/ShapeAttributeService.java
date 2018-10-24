package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.ShapeAttributeDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.buildModel.LineAttributeGroup;
import com.hysw.qqsl.cloud.core.entity.buildModel.LineSectionPlaneModel;
import com.hysw.qqsl.cloud.core.entity.data.Shape;
import com.hysw.qqsl.cloud.core.entity.data.ShapeAttribute;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenl on 17-4-10.
 */
@Service("shapeAttributeService")
public class ShapeAttributeService extends BaseService<ShapeAttribute,Long> {
    @Autowired
    private ShapeAttributeDao shapeAttributeDao;
    @Autowired
    private LineSectionPlaneModelService lineSectionPlaneModelService;
    @Autowired
    private ShapeService shapeService;
    @Autowired
    public void setBaseDao(ShapeAttributeDao shapeAttributeDao) {
        super.setBaseDao(shapeAttributeDao);
    }

    public List<ShapeAttribute> findByShape(Shape shape) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("shape", shape));
        return shapeAttributeDao.findList(0, null, filters);
    }

    public JSONArray buildJson(Shape shape) {
        JSONArray jsonArray = new JSONArray();
        LineSectionPlaneModel lineSectionPlaneModel = shapeService.pickedShapeAndSetProperty(shape);
        if (lineSectionPlaneModel.getShapeAttribute() == null || lineSectionPlaneModel.getShapeAttribute().size() == 0) {
            return new JSONArray();
        }
        jsonArray.add(makeJson(lineSectionPlaneModel.getRemark()));
        jsonArray.add(makeJson(lineSectionPlaneModel.getLineWaterResources()));
        jsonArray.add(makeJson(lineSectionPlaneModel.getLineControlSize()));
        jsonArray.add(makeJson(lineSectionPlaneModel.getLineGroundStress()));
        jsonArray.add(makeJson(lineSectionPlaneModel.getLineComponent()));
        return jsonArray;
    }

    private JSONObject makeJson(LineAttributeGroup lineAttributeGroup) {
        JSONObject jsonObject = new JSONObject(), jsonObject1;
        if (lineAttributeGroup == null) {
            return null;
        }
        jsonObject.put("name", lineAttributeGroup.getName());
        jsonObject.put("alias", lineAttributeGroup.getAlias());
        JSONArray jsonArray = new JSONArray();
        if (lineAttributeGroup.getShapeAttributes() != null) {
            for (ShapeAttribute shapeAttribute : lineAttributeGroup.getShapeAttributes()) {
                jsonObject1 = new JSONObject();
                jsonObject1.put("name", shapeAttribute.getName());
                jsonObject1.put("value", shapeAttribute.getValue());
                jsonObject1.put("alias", shapeAttribute.getAlias());
                jsonObject1.put("selects", shapeAttribute.getSelects());
                jsonObject1.put("type", shapeAttribute.getType());
                jsonObject1.put("unit", shapeAttribute.getUnit());
                jsonArray.add(jsonObject1);
            }
            jsonObject.put("shapeAttribute", jsonArray);
        }
        if (lineAttributeGroup.getChilds() != null) {
            jsonArray = new JSONArray();
            for (LineAttributeGroup child : lineAttributeGroup.getChilds()) {
                jsonArray.add(makeJson(child));
            }
            jsonObject.put("child", jsonArray);
        }
        return jsonObject;
    }

    public JSONArray getModelType() {
        JSONObject jsonObject;
        JSONArray jsonArray = new JSONArray();
        for (LineSectionPlaneModel.Type value : LineSectionPlaneModel.Type.values()) {
            jsonObject = new JSONObject();
            jsonObject.put("typeC", value.getTypeC());
            jsonObject.put("lineSectionPlaneModelType", value.name());
            jsonObject.put("abbreviate", value.getAbbreviate());
            jsonObject.put("type", value.getType());
            jsonObject.put("commonType", value.getCommonType());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    public JSONArray toJSON(List<ShapeAttribute> shapeAttributes) {
        JSONObject jsonObject;
        JSONArray jsonArray = new JSONArray();
        for (ShapeAttribute shapeAttribute : shapeAttributes) {
            jsonObject = new JSONObject();
            jsonObject.put("alias", shapeAttribute.getAlias());
            jsonObject.put("value", shapeAttribute.getValue());
            jsonObject.put("id", shapeAttribute.getId());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }
}
