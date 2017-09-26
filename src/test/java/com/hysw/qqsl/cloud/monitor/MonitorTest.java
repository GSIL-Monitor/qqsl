package com.hysw.qqsl.cloud.monitor;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.entity.Setting;
import com.hysw.qqsl.cloud.entity.monitor.Water;
import com.hysw.qqsl.cloud.entity.monitor.WaterMonitor;
import com.hysw.qqsl.cloud.util.SettingUtils;
import org.junit.Before;
import org.junit.Test;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Created by Administrator on 2016/9/1.
 */
public class MonitorTest extends BaseTest{
    private Connection conn;
    WaterMonitor waterMonitor;
    List<Water> curWaters;
    List<Water> hisWaters;
    SimpleDateFormat sim=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Before
    public void conntentJDBC() {
        waterMonitor = new WaterMonitor("");
        curWaters = waterMonitor.getCurWaters();
//        hisWaters = waterMonitor.getHisWaters();
        // 驱动程序名
        String driver = "com.mysql.jdbc.Driver";
        // URL指向要访问的数据库名scutcs
        Setting setting = SettingUtils.getInstance().getSetting();
        String url = "jdbc:mysql://"+setting.getWaterIP()+"/irtu?useUnicode=true&characterEncoding=UTF-8";
        // MySQL配置时的用户名
        String user = "root";
        // MySQL配置时的密码
        String password = "sa";
        try {
            // 加载驱动程序
            Class.forName(driver);
            // 连续数据库
            conn = DriverManager.getConnection(url, user, password);
            if (!conn.isClosed())
                System.out.println("Succeeded connecting to the Database!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            long l = System.currentTimeMillis();
            long l1=l-15000000;
            Date d = new Date(l);
            Date d1 = new Date(l1);
            String format = sim.format(d);
            String format1 = sim.format(d1);
            String sql = "select * from biz_water_history where DATA_TIME between \""+ format1 +"\" and \""+format+"\"";
            ResultSet rs = stmt.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testGiveTABLE(){
        List<String> list=new ArrayList<String>();
        try{
            DatabaseMetaData dmd= conn.getMetaData();
            ResultSet rs=dmd.getTables(null, null, "%", null);
            while(rs.next()){
                list.add(rs.getString("TABLE_NAME"));
            }
        }catch(SQLException e)
        {
            e.printStackTrace();
        }
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }
        System.out.println(Float.MAX_VALUE);
    }

//    @Test
//    public void testCheckTable() throws ParseException {
//        try {
//            Statement stmt = conn.createStatement();
//            String sql = "select * from biz_water_current";    //要执行的SQL
//            ResultSet rs = stmt.executeQuery(sql);//创建数据对象
//            while (rs.next()){
//                System.out.println(rs.getString(1));
//                Water water = new Water();
//                water.setTermSN(rs.getString(1));
//
//                String str = rs.getString(2);
//                SimpleDateFormat sim=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                java.util.Date d=sim.parse(str);
//                water.setDate(d);
//                System.out.println(rs.getString(3));
//                System.out.println(rs.getString(4));
//                System.out.println(rs.getString(5));
//                System.out.println(rs.getString(6));
//                System.out.println(rs.getString(7));
//                water.setMaxValue(rs.getFloat(7));
//                water.setMinValue(rs.getFloat(7));
//                System.out.println(rs.getString(8));
//                System.out.println(rs.getString(9));
//                System.out.println(rs.getString(10));
//                water.setVoltage(rs.getFloat(10));
//                System.out.println(rs.getString(11));
//                curWaters.add(water);
//            }
//            rs.close();
//            stmt.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

}
