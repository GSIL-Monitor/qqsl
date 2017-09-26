package com.hysw.qqsl.cloud.entity.project;

import com.hysw.qqsl.cloud.entity.data.Account;
import com.hysw.qqsl.cloud.entity.data.Project;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by flysic on 17-5-16.
 */
public class Cooperate {

    private Cooperate(){

    }
    public Cooperate(Project project) {
        this.project = project;
        this.invite = new Stage(Stage.Type.INVITE);
        this.preparation = new Stage(Stage.Type.PREPARATION);
        this.building =  new Stage(Stage.Type.BUILDING);
        this.maintenance = new Stage(Stage.Type.MAINTENANCE);
    }

    private Project project;
    /** 查看　*/
    private List<CooperateVisit> visits =  new ArrayList<>();
    /** 招投标阶段 */
    private Stage invite;
    /** 项目前期 */
    private Stage  preparation;
    /** 建设期 */
    private Stage building;
    /** 运营阶段*/
    private Stage maintenance;

    public final Project getProject() {
        return project;
    }

    public final Stage getInvite() {
        return invite;
    }

    public final Stage getPreparation() {
        return preparation;
    }

    public final Stage getBuilding() {
        return building;
    }

    public final Stage getMaintenance() {
        return maintenance;
    }


    public void setVisits(List<CooperateVisit> visits) {
        this.visits = visits;
    }

    /**
     * 查看注册
     * @param account
     * @return
     */
    public void register(Account account) {
        if(visits.size()>0){
            for(int i = 0 ;i<visits.size();i++){
                if(visits.get(i).getAccount().getId().equals(account.getId())){
                    return;
                }
            }
        }
        visits.add(new CooperateVisit(account));
    }

    /**
     * 查看注销
     * @param account
     * @return
     */
    public boolean unRegister(Account account) {
        if(visits.size()==0){
            return true;
        };
        for(int i =0 ;i<visits.size();i++){
            if(visits.get(i).getAccount().getId().equals(account.getId())){
                visits.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     *
     * 招投标
     * @param account
     * @return
     */
    public boolean registerInviteElement(Account account) {
        return invite.registerElement(account);
    }

    public void unRegisterInviteElement() {
        invite.unRegisterElement();
    }

    public boolean registerInviteFile(Account account){
        return invite.registerFile(account);
    }

    public void unRegisterInviteFile(){
        invite.unRegisterFile();
    }

    /**
     * 项目前期
     * @param account
     * @return
     */
    public boolean registerPreparationElement(Account account) {
        return preparation.registerElement(account);
    }

    public void unRegisterPreparationElement() {
        preparation.unRegisterElement();
    }

    public boolean registerPreparationFile(Account account){
        return preparation.registerFile(account);
    }

    public void unRegisterPreparationFile(){
        preparation.unRegisterFile();
    }

    /**
     * 建设期
     * @param account
     * @return
     */
    public boolean registerBuildingElement(Account account) {
        return building.registerElement(account);
    }

    public void unRegisterBuildingElement() {
        building.unRegisterElement();
    }

    public boolean registerBuildingFile(Account account){
        return building.registerFile(account);
    }

    public void unRegisterBuildingFile(){
        building.unRegisterFile();
    }

    /**
     * 运营维护期
     * @param account
     * @return
     */
    public boolean registerMaintenanceElement(Account account) {
        return maintenance.registerElement(account);
    }

    public void unRegisterMaintenanceElement() {
        maintenance.unRegisterElement();
    }

    public boolean registerMaintenanceFile(Account account){
        return maintenance.registerFile(account);
    }

    public void unRegisterMaintenanceFile(){
        maintenance.unRegisterFile();
    }

    public List<Account> getVisitAccounts(){
        List<Account> accounts = new ArrayList<>();
        Account account;
        for(int i=0;i<visits.size();i++){
            account = visits.get(i).getAccount();
            accounts.add(account);
        }
        return accounts;
    }
    public Account getInviteElementAccount() {
        return invite.getElementVisit().getAccount();
    }
    public Account getInviteFileAccount() {
        return invite.getFileVisit().getAccount();
    }
    public Account getPreparationElementAccount() {
        return preparation.getElementVisit().getAccount();
    }
    public Account getPreparationFileAccount() {
        return preparation.getFileVisit().getAccount();
    }
    public Account getBuildingElementAccount() {
        return building.getElementVisit().getAccount();
    }
    public Account getBuildingFileAccount() {
        return building.getFileVisit().getAccount();
    }
    public Account getMaintenanceElementAccount() {
        return maintenance.getElementVisit().getAccount();
    }
    public Account getMaintenanceFileAccount() {
        return maintenance.getFileVisit().getAccount();
    }

    /**
     * 获取查看 Json 数据
     * @return
     */
    public JSONArray toViewJson(){
        JSONArray viewJsons= new JSONArray();
        if(this.visits.size()==0){
            return viewJsons;
        }
        JSONObject viewJson;
        for(int i=0;i<this.visits.size();i++){
           viewJson = visits.get(i).toJson();
           viewJsons.add(viewJson);
        }
        return viewJsons;
    }

    /**
     * 获取编辑 Json 数据
     * @return
     */
    public JSONObject toCooperateJson(){
        JSONObject cooperateJson = new JSONObject();
        JSONObject coomJson;
        JSONObject elementJson;
        JSONObject fileJson;
     //   {"invite":{},"preparation":{"element":{"id":1,"name":"qqsl","phone":"18661925010","createTime":1495101861722}},"building":{},"maintenance":{}}
        if(this.getInviteElementAccount()!=null||this.getInviteFileAccount()!=null){
            coomJson = new JSONObject();
            elementJson = this.invite.getElementVisit().toJson();
            fileJson = this.invite.getFileVisit().toJson();
            coomJson.put("element",elementJson);
            coomJson.put("file",fileJson);
            if(!coomJson.isEmpty()){
                cooperateJson.put("invite",coomJson);
            }
        }
        if(this.getPreparationElementAccount()!=null||this.getPreparationFileAccount()!=null){
            coomJson = new JSONObject();
            elementJson = this.preparation.getElementVisit().toJson();
            fileJson = this.preparation.getFileVisit().toJson();
            coomJson.put("element",elementJson);
            coomJson.put("file",fileJson);
            if(!coomJson.isEmpty()){
                cooperateJson.put("preparation",coomJson);
            }
        }
        if(this.getBuildingElementAccount()!=null||this.getBuildingFileAccount()!=null){
            coomJson = new JSONObject();
            elementJson = this.building.getElementVisit().toJson();
            fileJson = this.building.getFileVisit().toJson();
            coomJson.put("element",elementJson);
            coomJson.put("file",fileJson);
            if(!coomJson.isEmpty()){
                cooperateJson.put("building",coomJson);
            }
        }
        if(this.getMaintenanceElementAccount()!=null||this.getMaintenanceFileAccount()!=null){
            coomJson = new JSONObject();
            elementJson = this.maintenance.getElementVisit().toJson();
            fileJson = this.maintenance.getFileVisit().toJson();
            coomJson.put("element",elementJson);
            coomJson.put("file",fileJson);
            if(!coomJson.isEmpty()){
                cooperateJson.put("maintenance",coomJson);
            }
        }
        return cooperateJson;
    }

}
