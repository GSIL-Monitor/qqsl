package com.hysw.qqsl.cloud.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.entity.build.AttribeGroup;
import com.hysw.qqsl.cloud.entity.build.BuildGroup;
import com.hysw.qqsl.cloud.entity.build.Config;
import com.hysw.qqsl.cloud.entity.data.Build;
import com.hysw.qqsl.cloud.entity.data.Attribe;
import com.hysw.qqsl.cloud.entity.data.Project;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by leinuo on 17-4-13.
 */
public class BuildServiceTest extends BaseTest {

    @Autowired
    private BuildService buildService;
    @Autowired
    private BuildGroupService buildGroupService;
    @Autowired
    private AttribeService attribeService;
    @Autowired
    private ProjectService projectService;
    @Test
    public void save(){
        Build build = new Build();
        build.setAlias("11");
        Project project = projectService.find(531l);
        build.setProject(project);
        build.setType(Config.CommonType.QS);
        build.setName("泉室");
        buildService.save(build);
    }

    @Test
    public void saveAttribe(){
        Build build1 = buildService.find(1l);
        Build build = getBuildModel(build1);
        List<Attribe> attribes = new ArrayList<>();
        assertNotNull(build);
        assertNotNull(build.getAlias());
        assertNotNull(build.getMaterAttribeGroup());
        List<Attribe> maters = build.getMaterAttribeGroup().getAttribes();
        add(attribes,maters);
        List<Attribe> dimensions = build.getDimensionsAttribeGroup().getAttribes();
        add(attribes,dimensions);
        List<Attribe> hydraulics = build.getHydraulicsAttribeGroup().getAttribes();
        add(attribes,hydraulics);
        List<Attribe> structures = build.getStructureAttribeGroup().getAttribes();
        add(attribes,structures);
        save(attribes,build1);
    }

    private Build getBuildModel(Build build1){
        Build build = null;
        List<BuildGroup> buildGroups = buildGroupService.getCompleteBuildGroups();
        for(int i=0;i<buildGroups.size();i++){
            List<Build> builds = buildGroups.get(i).getBuilds();
            for(int j=0;j<builds.size();j++){
                build = builds.get(j);
                if(build.getName().equals(build1.getName())){
                    build.setId(build1.getId());
                    break;
                }
            }
            if(build != null && build.getName().equals(build1.getName())) break;
        }
        return build;
    }

    private void add(List<Attribe> attribes,List<Attribe> attribeList){
        for(int i=0;i<attribeList.size();i++){
            attribes.add(attribeList.get(i));
        }
    }
  private void save(List<Attribe> attribes,Build build){
        for (int i=0;i<attribes.size();i++){
            attribes.get(i).setBuild(build);
            if(attribes.get(i).getType().equals(Attribe.Type.SELECT)){
                String value = attribes.get(i).getSelects().get(0);
                attribes.get(i).setValue(value);
            }else{
                attribes.get(i).setValue("test");
            }
            attribeService.save(attribes.get(i));
        }
    }

    @Test
    public void buildBuild(){
        Build build1 = buildService.find(1l);
        List<Attribe> attribes = build1.getAttribeList();
        Build build = getBuildModel(build1);
        String head;
        Attribe attribe;
        for (int i=0;i<attribes.size();i++){
            attribe =  attribes.get(i);
            head = attribe.getAlias().substring(0,1);
            switch (head){
                case "M":
                    getAttribeGroup(build.getMaterAttribeGroup().getAttribes(),attribe);
                    break;
                case "D":
                    getAttribeGroup(build.getDimensionsAttribeGroup().getAttribes(),attribe);
                    break;
                case "S":
                    getAttribeGroup(build.getStructureAttribeGroup().getAttribes(),attribe);
                    break;
                case "H":
                    getAttribeGroup(build.getHydraulicsAttribeGroup().getAttribes(),attribe);
                    break;
                default:
                    break;
            }
        }
        assertNotNull(attribes);
        assertNotNull(build.getMaterAttribeGroup().getAttribes().get(0).getValue());
    }

    private void getAttribeGroup(List<Attribe> attribes,Attribe attribe){
        for(int j=0;j<attribes.size();j++){
            if(attribes.get(j).getAlias().equals(attribe.getAlias())){
                attribes.get(j).setValue(attribe.getValue());
                break;
            }
        }
    }

    public void saveDucao(){
        Project project = projectService.find(531l);

        List<Build> builds = buildService.findByProjectAndAlias(project,"27");
        if(builds.size()>0){
            Build build1 = builds.get(0);
        }else{
            Build build = new Build();
            build.setAlias("27");

            build.setProject(project);
            build.setType(Config.CommonType.DC);
            build.setName("渡槽");
            buildService.save(build);
        }
    }

    @Test
    public void saveDucaoAttribe(){
        saveDucao();
        Project project = projectService.find(531l);
        List<Build> builds = buildService.findByProjectAndAlias(project,"27");
        Build build1 = builds.get(0);
        Build build = getBuildModel(build1);
        //List<AttribeGroup> attribeGroups = new ArrayList<>();
        saveGroup(build.getDimensionsAttribeGroup(),build1);
        saveGroup(build.getGeologyAttribeGroup(),build1);
        saveGroup(build.getHydraulicsAttribeGroup(),build1);
        saveGroup(build.getMaterAttribeGroup(),build1);
        saveGroup(build.getStructureAttribeGroup(),build1);
    }

    public void saveGroup(AttribeGroup attribeGroup,Build build){
           if(attribeGroup!=null){
              if(attribeGroup.getChilds()!=null&&attribeGroup.getChilds().size()>0){
                  if(attribeGroup.getStatus().equals(Config.Status.SELECT)){
                     String select = attribeGroup.getChilds().get(0).getName();
                     for(int k=0;k<attribeGroup.getChilds().size();k++){
                         if(select.equals(attribeGroup.getChilds().get(k).getName())){
                             saveGroup(attribeGroup.getChilds().get(k),build);
                         }
                     }
                  }else{
                      for(int i=0;i<attribeGroup.getChilds().size();i++){
                          saveGroup(attribeGroup.getChilds().get(i),build);
                      }
                  }

              }
              if(attribeGroup.getAttribes()!=null&&attribeGroup.getAttribes().size()>0){
                  save(attribeGroup.getAttribes(),build);
              }
    }
    }

}