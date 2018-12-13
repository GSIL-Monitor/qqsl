package com.hysw.qqsl.cloud.core.service;

import com.google.gson.JsonObject;
import com.hysw.qqsl.cloud.CommonEnum;
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

    public JSONObject buildJson(Shape shape) {
        JSONObject jsonObject = new JSONObject();
        LineSectionPlaneModel lineSectionPlaneModel = shapeService.pickedShapeAndSetProperty(shape);
        jsonObject.put("remark",makeJson(lineSectionPlaneModel.getRemark()));
        jsonObject.put("waterResource",makeJson(lineSectionPlaneModel.getLineWaterResource()));
        jsonObject.put("controlSize",makeJson(lineSectionPlaneModel.getLineControlSize()));
        jsonObject.put("groundStress",makeJson(lineSectionPlaneModel.getLineGroundStress()));
        jsonObject.put("component",makeJson(lineSectionPlaneModel.getLineComponent()));
        return jsonObject;
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
                jsonObject1.put("id", shapeAttribute.getId());
                jsonObject1.put("name", shapeAttribute.getName());
                jsonObject1.put("value", shapeAttribute.getValue());
                jsonObject1.put("alias", shapeAttribute.getAlias());
                jsonObject1.put("selects", shapeAttribute.getSelects());
                jsonObject1.put("type", shapeAttribute.getType());
                jsonObject1.put("unit", shapeAttribute.getUnit());
                jsonArray.add(jsonObject1);
            }
            jsonObject.put("attribute", jsonArray);
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

    public JSONArray getModelType(CommonEnum.CommonType type) {
        JSONObject jsonObject;
        JSONArray jsonArray = new JSONArray();
        for (LineSectionPlaneModel.Type value : LineSectionPlaneModel.Type.values()) {
            if (value.getCommonType() != type) {
                continue;
            }
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
            if (shapeAttribute == null) {
                continue;
            }
            jsonObject = new JSONObject();
            jsonObject.put("alias", shapeAttribute.getAlias());
            jsonObject.put("value", shapeAttribute.getValue());
            jsonObject.put("id", shapeAttribute.getId());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    public void editShapeAttribute(Shape shape, Object attributes) {
        JSONObject jsonObject;
        ShapeAttribute shapeAttribute;
        for (Object attribute : JSONArray.fromObject(attributes)) {
            jsonObject = JSONObject.fromObject(attribute);
            if (jsonObject.get("id") == null) {
                shapeAttribute = new ShapeAttribute();
                shapeAttribute.setAlias(jsonObject.get("alias") == null ? null : jsonObject.get("alias").toString());
                shapeAttribute.setValue(jsonObject.get("value") == null ? null : jsonObject.get("value").toString());
                shapeAttribute.setShape(shape);
                save(shapeAttribute);
            } else {
                shapeAttribute = find(Long.valueOf(jsonObject.get("id").toString()));
                shapeAttribute.setValue(jsonObject.get("value") == null ? null : jsonObject.get("value").toString());
                save(shapeAttribute);
            }
        }
    }
}
