package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.entity.Setting;
import com.hysw.qqsl.cloud.core.entity.XMLFileException;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hysw.qqsl.cloud.core.entity.build.AttribeGroup;
import com.hysw.qqsl.cloud.core.entity.data.Attribe;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by leinuo on 17-3-28.
 * 读取建筑物属性文件
 */
@Service("attribeGroupService")
public class AttribeGroupService {

    private static final String DYNAMIC_ALIAS= "163,181,191,1101";
    @Autowired
    private CacheManager cacheManager;
    private Setting setting = SettingUtils.getInstance().getSetting();
    Log logger = LogFactory.getLog(getClass());

    public AttribeGroup getMaterGroup(String alias){
        Cache cache = cacheManager.getCache("attribeGroupsCache");
        net.sf.ehcache.Element element = cache.get("mater");
        if(element==null){
            element = new net.sf.ehcache.Element("mater", getAttribeGroups(setting.getBuildsMater()));
            if(((List<AttribeGroup>) element.getValue()).size()!=9){
                System.err.println("材质属性组size错误！");
            }
            cache.put(element);
        }
        List<AttribeGroup> attribeGroups = (List<AttribeGroup>) element.getValue();
        return  getAttribeGroup(alias,attribeGroups);
    }


    public AttribeGroup getDimensionsGroup(String alias){
        Cache cache = cacheManager.getCache("attribeGroupsCache");
        net.sf.ehcache.Element element = cache.get("dimension");
        if(element==null){
            element = new net.sf.ehcache.Element("dimension", getAttribeGroups(setting.getBuildsDimension()));
            if(((List<AttribeGroup>) element.getValue()).size()!=24){
                System.err.println("控制尺寸属性组size错误！");
            }
            cache.put(element);
        }
        List<AttribeGroup> attribeGroups = (List<AttribeGroup>) element.getValue();
        return  getAttribeGroup(alias,attribeGroups);
    }


    public AttribeGroup getHydraulicsGroup(String alias){
        Cache cache = cacheManager.getCache("attribeGroupsCache");
        net.sf.ehcache.Element element = cache.get("hydraulics");
        if(element==null){
            element = new net.sf.ehcache.Element("hydraulics", getAttribeGroups(setting.getBuildsHydraulics()));
            if(((List<AttribeGroup>) element.getValue()).size()!=10){
                System.err.println("水利属性组size错误！");
            }
            cache.put(element);
        }
        List<AttribeGroup> attribeGroups = (List<AttribeGroup>) element.getValue();
        return  getAttribeGroup(alias,attribeGroups);
    }



    public AttribeGroup getGeologyGroup(String alias){
        Cache cache = cacheManager.getCache("attribeGroupsCache");
        net.sf.ehcache.Element element = cache.get("geology");
        if(element==null){
            element = new net.sf.ehcache.Element("geology", getAttribeGroups(setting.getBuildsGeology()));
            if(((List<AttribeGroup>) element.getValue()).size()!=1){
                System.err.println("地质属性组size错误！");
            }
            cache.put(element);
        }
        List<AttribeGroup> attribeGroups = (List<AttribeGroup>) element.getValue();
        return  getAttribeGroup(alias,attribeGroups);
    }

    /**
     * 获取含有code的地质属性组
     */
    public AttribeGroup getGeologyDynamicGroups(String alias){
        Cache cache = cacheManager.getCache("attribeGroupsCache");
        net.sf.ehcache.Element element = cache.get("geologyDynamic");
        if(element==null){
            element = new net.sf.ehcache.Element("geologyDynamic", getGeologyDynamicAttribeGroups());
            if(getGeologyDynamicAttribeGroups().size()!=1){
                System.err.println("动态地质属性组size错误！");
            }
            cache.put(element);
        }
        List<AttribeGroup> attribeGroups = (List<AttribeGroup>) element.getValue();
        return  getAttribeGroup(alias,attribeGroups);
    }


    public AttribeGroup getStructureGroup(String alias){
        Cache cache = cacheManager.getCache("attribeGroupsCache");
        net.sf.ehcache.Element element = cache.get("structure");
        if(element==null){
            element = new net.sf.ehcache.Element("structure", getAttribeGroups(setting.getBuildsStructure()));
            if(((List<AttribeGroup>) element.getValue()).size()!=4){
                System.err.println("建筑结构属性组size错误！");
            }
            cache.put(element);
        }
        List<AttribeGroup> attribeGroups = (List<AttribeGroup>) element.getValue();
        return  getAttribeGroup(alias,attribeGroups);
    }

    private  AttribeGroup getAttribeGroup(String alias,List<AttribeGroup> attribeGroups){
        for(int i=0;i<attribeGroups.size();i++){
            if(attribeGroups.get(i).getAlias().equals(alias)){
                return attribeGroups.get(i);
            }
        }
        return null;
    }
    /**
     *
     * 读取xml创建各个elementGroup对象的方法
     *
     * @paramXMLName
     * @return
     * @throws
     */
    private List<AttribeGroup> getAttribeGroups(String XMLName){
        Element root,element;
        List<AttribeGroup> attribeGroups = new ArrayList<>();
        AttribeGroup attribeGroup;
        List<Element> elements;
        //检测别名是否重复
        List<String> groupAliass = new ArrayList<>();
        List<String> attribeAliass = new ArrayList<>();
        try {
            // 获取根节点
          root = SettingUtils.getInstance().getRootElement(
                    XMLName);
            // 获取子节点ElementGroup的List
          elements = SettingUtils.getInstance()
                    .getElementGroupList(root);
            // 遍历elements的子节点
            for(int i = 0;i<elements.size();i++){
                element = elements.get(i);
                attribeGroup = new AttribeGroup();
                attribeGroup.setAlias(element.attributeValue("alias"));
                attribeGroup.setName(element.attributeValue("name"));
                attribeGroup.setGenre(element.attributeValue("status"));
                checkGroupAlias(attribeGroup.getAlias(),groupAliass);
                readChilds(elements.get(i),attribeGroup,groupAliass,attribeAliass);
                attribeGroups.add(attribeGroup);
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }catch (Exception e){
            logger.error(XMLName+","+e.getMessage());
            e.printStackTrace();
        }
        return attribeGroups;
    }
    /**
     * 递归读取子节点
     * @param element
     * @param attribeGroup
     */
    private void readChilds(Element element,AttribeGroup attribeGroup,List<String> groupAliass, List<String> attribeAliass) throws XMLFileException{
        List<Element> elements;
        Element elementChild;
        List<AttribeGroup> attribeGroupChilds = new ArrayList<>();
        Attribe attribe;
        AttribeGroup attribeGroupChild;
        elements = element.elements();
        List<Attribe> attribes = new ArrayList<>();
         for(int i=0;i<elements.size();i++){
                elementChild = elements.get(i);
                if(elementChild.elements().size()>0){
                    attribeGroupChild = new AttribeGroup();
                    attribeGroupChild.setAlias(elementChild.attributeValue("alias"));
                    attribeGroupChild.setName(elementChild.attributeValue("name"));
                    attribeGroupChild.setGenre(elementChild.attributeValue("status"));
                    //检测属性组别名是否重复
                    checkGroupAlias(attribeGroupChild.getAlias(),groupAliass);
                    readChilds(elementChild,attribeGroupChild,groupAliass,attribeAliass);
                    attribeGroupChilds.add(attribeGroupChild);
                }else if(elementChild.getName().equals("attribeGroup")&&elementChild.elements().size()==0){
                    attribeGroupChild = new AttribeGroup();
                    attribeGroupChild.setAlias(elementChild.attributeValue("alias"));
                    attribeGroupChild.setName(elementChild.attributeValue("name"));
                    attribeGroupChild.setGenre(elementChild.attributeValue("status"));
                    //检测属性组别名是否重复
                    checkGroupAlias(attribeGroupChild.getAlias(),groupAliass);
                    attribeGroupChilds.add(attribeGroupChild);
                }else{
                    attribe = new Attribe();
                    attribe.setAlias(elementChild.attributeValue("alias"));
                    attribe.setName(elementChild.attributeValue("name"));
                    attribe.setUnit(elementChild.attributeValue("unit"));
                    attribe.setGenre(elementChild.attributeValue("status"));
                    checkAttribeAlias(attribe.getAlias(),attribeAliass);
                    if(StringUtils.hasText(elementChild.attributeValue("type"))){
                        attribe.setType(Attribe.Type.valueOf(elementChild.attributeValue("type").toUpperCase()));
                        if(elementChild.attributeValue("type").equals("select")){
                            attribe.setSelect(elementChild.attributeValue("select"));
                        }
                    }
                    attribes.add(attribe);
                }
            }
            if(attribes.size()>0){
                attribeGroup.setAttribes(attribes);
            }
        attribeGroup.setChilds(attribeGroupChilds);
    }

    /**
     * 检测属性组别名是否重复
     * @param groupAlias
     * @param groupAliass
     * @throws XMLFileException
     */
    private void checkGroupAlias(String groupAlias, List<String> groupAliass) throws XMLFileException{
    if(!groupAliass.contains(groupAlias)){
        groupAliass.add(groupAlias);
      }else throw new XMLFileException("属性组别名重复:"+groupAlias);
    }

    private void checkAttribeAlias(String attribeAlias, List<String> attribeAliass) throws XMLFileException{
    if(!attribeAliass.contains(attribeAlias)){
        attribeAliass.add(attribeAlias);
      }else throw new XMLFileException("属性别名重复:"+attribeAlias);
    }



    private List<AttribeGroup>  getGeologyDynamicAttribeGroups(){
        Map<String,List<AttribeGroup>> map = assignGeologyGroupCode();
        List<AttribeGroup> attribeGroups = getGeologyDynamicGroup(map);
        return attribeGroups;
    }

    /**
     * 为所有动态地质属性组的code赋值
     * @return
     */
    private  Map<String,List<AttribeGroup>> assignGeologyGroupCode(){
        List<AttribeGroup> attribeGroups = getGeologyDynamicGroup(null);
        Map<String,List<AttribeGroup>> map = new HashedMap();
        List<AttribeGroup> geologyGroupCodes;
        AttribeGroup attribeGroup;
        for(int j=0;j<attribeGroups.size();j++){
            geologyGroupCodes = new ArrayList<>();
        for(int i=0;i<10;i++){
               attribeGroup = (AttribeGroup) SettingUtils.objectCopy(attribeGroups.get(j));
               attribeGroup.setCode(i);
                if(attribeGroup.getChilds()!=null&&attribeGroup.getChilds().size()>0){
                    assignGeologyChildGroupCode(attribeGroup.getChilds(),i);
                }
                if(attribeGroup.getAttribes()!=null&&attribeGroup.getAttribes().size()>0){
                    for(int k=0;k<attribeGroup.getAttribes().size();k++){
                        attribeGroup.getAttribes().get(k).setCode(i);
                    }
                }
                geologyGroupCodes.add(attribeGroup);
            }
            map.put(attribeGroups.get(j).getAlias(),geologyGroupCodes);
        }
        return map;
    }

    /**
     * 递归赋值code
     * @param childs
     * @param i
     */
    private void assignGeologyChildGroupCode( List<AttribeGroup> childs,int i) {
        AttribeGroup attribeGroup;
        for(int j=0;j<childs.size();j++){
            attribeGroup = childs.get(j);
               attribeGroup.setCode(i);
                if(attribeGroup.getChilds()!=null&&attribeGroup.getChilds().size()>0){
                    assignGeologyChildGroupCode(attribeGroup.getChilds(),i);
                }
                if(attribeGroup.getAttribes()!=null&&attribeGroup.getAttribes().size()>0){
                    for(int k=0;k<attribeGroup.getAttribes().size();k++){
                        attribeGroup.getAttribes().get(k).setCode(i);
                    }
                }
        }
    }

    /**
     * 获取地质动态属性组
     * alias 163,181,191,1101
     * @return
     */
    private List<AttribeGroup> getGeologyDynamicGroup(Map<String,List<AttribeGroup>> map){
        List<String> dynamicAliass = Arrays.asList(DYNAMIC_ALIAS.split(","));
        AttribeGroup geology = (AttribeGroup)SettingUtils.objectCopy(getGeologyGroup("1"));
        List<AttribeGroup> dynamicGroups = new ArrayList<>();
        if(null!=map){
            getDynamicGroup(geology,dynamicGroups,dynamicAliass,map);
            dynamicGroups.add(geology);
        }else{
            getDynamicGroup(geology,dynamicGroups,dynamicAliass,null);
        }
        return dynamicGroups;
    }

    /**
     *
     dynamicGroups.add(attribeGroups.get(i));
     * 递归获取地质属性动态组
     * @param geology
     * @param dynamicGroups
     * @param dynamicAliass
     * @param map　为空表示挑取动态属性组，不为空非表示用带有编号的动态组替换原有动态组
     */
    private void getDynamicGroup(AttribeGroup geology, List<AttribeGroup> dynamicGroups, List<String> dynamicAliass,Map<String,List<AttribeGroup>> map) {
        List<AttribeGroup> attribeGroups = geology.getChilds();
        for(int i = 0;i<attribeGroups.size();i++){
            if(null==map){
                if(dynamicAliass.contains(attribeGroups.get(i).getAlias())){
                    dynamicGroups.add(attribeGroups.get(i));
                }
                if(attribeGroups.get(i).getChilds()!=null&&attribeGroups.get(i).getChilds().size()>0){
                    getDynamicGroup(attribeGroups.get(i),dynamicGroups,dynamicAliass,map);
                }
            }else{
                if(dynamicAliass.contains(attribeGroups.get(i).getAlias())){
                    geology.setChilds(map.get(attribeGroups.get(i).getAlias()));
                }
                if(attribeGroups.get(i).getChilds()!=null&&attribeGroups.get(i).getChilds().size()>0){
                    getDynamicGroup(attribeGroups.get(i),dynamicGroups,dynamicAliass,map);
                }
            }
        }
    }
}
