package com.hysw.qqsl.cloud.wechat.util;


import org.apache.commons.lang.ClassUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.dao.DataAccessException;

import java.io.*;
import java.util.Date;

/**
 * Create by leinuo on 17-7-18 下午5:57
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 *
 *
 */
public class MysqlUtil {
    private static String USERNAME;//数据库用户名
    private static String PASSWORD;//数据库用户密码
    private static String PORT;//数据库开放端口
    private static String IP;//数据库ip地址
    private static String DATABASENAME;//数据库名称
    private static  Log LOG = LogFactory.getLog(MysqlUtil.class);

    /**
     * 配置数据库连接信息
     * @param username 数据库用户名
     * @param password 数据库用户密码
     * @param databaseName 数据库名称
     * @param ip 数据库ip地址
     * @param port 数据库开放端口
     */
    public static void config(String username,String password,String databaseName,String ip,String port){
        MysqlUtil.USERNAME = username;
        MysqlUtil.PASSWORD = password;
        MysqlUtil.DATABASENAME = databaseName;
        MysqlUtil.PORT = port;
        MysqlUtil.IP = ip;
        LOG.info("MysqlUtil完成初始化!");
    }

    /**
     * 根据数据库表名查找对应创建该表的sql语句脚本
     * @param tableName 数据库名
     * @return             创建该表的sql语句脚本
     * @throws IOException              文件读写错误
     */
    public static String getTableSql(String tableName) throws IOException{
        File f =  getMysqlDump();
        if(f == null){
            throw new FileNotFoundException("在lib路径下没有找到名为'mysqldump'的mysql工具,MysqlDumpUtil类的功能依赖此文件!");
        }

        //构建远程数据库命令字符串
        StringBuilder cmdStr = new StringBuilder();
        cmdStr.append(" -d --compact -u")
                .append(USERNAME)
                .append(" -p")
                .append(PASSWORD)
                .append(" -h")
                .append(IP)
                .append(" -P")
                .append(PORT)
                .append(" ").append(DATABASENAME)
                .append(" ").append(tableName);

        Runtime rt = Runtime.getRuntime();
        //数据库构建表的sql脚本
        StringBuilder tableSql = new StringBuilder();
        // 调用mysqldump的cmd命令
        Process p = null;
        if(isWindows()){
            p = rt.exec(new String[]{"cmd","/c",f.getPath() + cmdStr.toString()});
        } else {
            p = rt.exec(new String[]{"sh","-c","cd "+ f.getParent() + " | mysqldump " + cmdStr.toString()});
        }
        // 把进程执行中的控制台输出信息写入.sql文件
        // 注:如果不对控制台信息进行读出，则会导致进程堵塞无法运行
        InputStream in = p.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "utf8"));

        for (String inStr = null; (inStr = br.readLine()) != null;) {
            tableSql.append(inStr).append("<br>");
        }

        in.close();
        br.close();

        //处理错误的信息
        String errMsg = getErrMsg(p);
        if (errMsg != null) {
            throw new IOException(errMsg);
        }

        return tableSql.toString();
    }


    /**
     * 备份数据库操作
     * @return BackupInfo备份状态对象
     * @throws IOException 文件读写错误
     */
    public static BackupInfo backup() throws IOException{
        File f =  getMysqlDump();
        if(f == null){
            throw new FileNotFoundException("在lib路径下没有找到名为'mysqldump'的mysql工具,MysqlDumpUtil类的功能依赖此文件!");
        }

        //构建远程数据库命令字符串
        StringBuilder cmdStr = new StringBuilder();
        cmdStr.append(" -u")
                .append(USERNAME)
                .append(" -p")
                .append(PASSWORD)
                .append(" -h")
                .append(IP)
                .append(" -P")
                .append(PORT)
                .append("  --ignore-table=")
                .append(DATABASENAME).append(".t_backup_database ")
                .append(" ").append(DATABASENAME);

        //创建备份信息对象
        final BackupInfo bki = new BackupInfo();
        Runtime rt = Runtime.getRuntime();
        // 调用mysqldump的cmd命令
        final Process p;
        if(isWindows()){
            p = rt.exec(new String[]{"cmd","/c",f.getPath() + cmdStr.toString()});
        } else {
            p = rt.exec(new String[]{"sh","-c","cd "+ f.getParent() + " | mysqldump "+cmdStr.toString()});
        }
        //备份文件地址
        final File path = new File(getBackupPath().getPath() + "/" + new Date().toString().replaceAll(" |:", "") + ".sql");
        LOG.info("备份文件地址 ：" + path);
        //创建线程异步执行备份操作
        new Thread(){
            /* (non-Javadoc)
             * @see java.lang.Thread#run()
             */
            @Override
            public void run(){
                //得到远程数据库备份的流文件
                InputStream in = p.getInputStream();
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(in, "utf8"));
                    OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path), "utf8");
                    //向指定地址写文件
                    for(String str = null;(str = br.readLine()) != null;){
                        writer.write(str+"/r/n");
                        writer.flush();
                        //记录当前写入字节数
                        bki.setSize(str.getBytes().length + bki.getSize());
                    }

                    bki.setPath(path.getPath());
                    bki.setComplete(true);
                    LOG.info("备份文件完成!"+bki.isComplete());

                    in.close();
                    br.close();
                    writer.close();

                    //处理错误的信息
                    String errMsg = getErrMsg(p);
                    if(errMsg != null){
                        bki.setError(true);
                        bki.setComplete(true);
                        bki.setErrMsg(errMsg);
                    }
                  //  LOG.info(ClassUtil.getMethodName(), "备份方法结束!");
                } catch (IOException e) {
                   // LOG.error(ClassUtil.getMethodName(),e.getMessage());
                    bki.setComplete(true);
                    bki.setError(true);
                    bki.setErrMsg(e.getMessage());
                }
            }
        }.start();

        return bki;
    }


    /**
     * 还原数据库
     * @param path 要还原的sql脚本文件路径
     * @throws IOException 文件读写出错
     */
    public static void loadData(String path) throws IOException{
        File f =  getMysql();
        if(f == null){
            throw new FileNotFoundException("在lib路径下没有找到名为'mysql'的mysql工具,MysqlDumpUtil类的功能依赖此文件!");
        }

        //构建远程数据库命令字符串
        StringBuilder cmdStr = new StringBuilder();
        cmdStr.append(" -u")
                .append(USERNAME)
                .append(" -p")
                .append(PASSWORD)
                .append(" -h")
                .append(IP)
                .append(" -P")
                .append(PORT)
                .append(" ").append(DATABASENAME).append("<").append(path);

        Runtime rt = Runtime.getRuntime();
        // 调用mysqldump的cmd命令
        Process p = null;
        if(isWindows()){
            p = rt.exec(new String[]{"cmd","/c",f.getPath() + cmdStr.toString()});
        } else {
            p = rt.exec(new String[]{"sh","-c","cd "+ f.getParent() + " | mysql "+cmdStr.toString()});
        }

        //处理错误的信息
        String errMsg = getErrMsg(p);
        if (errMsg != null) {
            throw new IOException(errMsg);
        }
    }


    /**
     * @author scottCgi
     * @since  2009-1-19
     * 封装了备份数据库时的状态信息
     */
    public static class BackupInfo{
        //备份是否完成
        private boolean isComplete;
        //备份文件大小
        private double size;
        //备份是否出错
        private boolean isError;
        //出错信息
        private String errMsg;
        //备份路径
        private String path;

        public boolean isComplete() {
            return isComplete;
        }
        public void setComplete(boolean isComplete) {
            this.isComplete = isComplete;
        }
        public boolean isError() {
            return isError;
        }
        public void setError(boolean isError) {
            this.isError = isError;
        }
        public String getErrMsg() {
            return errMsg;
        }
        public void setErrMsg(String errMsg) {
            this.errMsg = errMsg;
        }
        public void setSize(double size) {
            this.size = size;
        }
        public double getSize() {
            return size;
        }
        public String getPath() {
            return path;
        }
        public void setPath(String path) {
            this.path = path;
        }
    }

    /**
     * 获取mysqldump工具所的位置
     * @return 名为"mysqldump.exe"的文件路径
     */
    private static File getMysqlDump(){
        File f = null;
        try{
            if(isWindows()){
                f = new File(MysqlUtil.class.getResource("/../lib/mysqldump.exe").getPath());
            } else {
                f = new File(MysqlUtil.class.getResource("/../lib/mysqldump").getPath());
            }

        } catch(NullPointerException e){}

        return f;
    }

    /**
     * 获取mysql工具所在的位置
     * @return 名为"mysql.exe"的文件路径
     */
    private static File getMysql(){
        File f = null;
        try{
            if(isWindows()){
                f = new File(MysqlUtil.class.getResource("/../lib/mysql.exe").getPath());
            } else {
                f = new File(MysqlUtil.class.getResource("/../lib/mysql").getPath());
            }

        } catch (NullPointerException e){}
        return f;
    }

    /**
     * 获取备份文件夹,没有则创建一个
     * @return 返回备份文件夹
     */
    private static File getBackupPath(){
        File f = new File(MysqlUtil.class.getResource("/").getPath());
        //获得web-info目录下的backup文件夹
        f = new File(f.getParent() + "/backup");
      //  LOG.info(ClassUtil.getMethodName(), f.getPath());
        if(!f.isDirectory()){
            f.mkdir();
        }

        return f;
    }

    /**
     * 获取cmd命令执行的错误信息
     * @param p Process对象
     * @return  错误信息字符串
     * @throws IOException 文件读写错误
     */
    private static String getErrMsg(Process p) throws IOException{
        StringBuilder errMsg = new StringBuilder();
        InputStream in = p.getErrorStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(in,"utf8"));
        for(String inStr = null;(inStr = br.readLine()) != null;){
            errMsg.append(inStr).append("<br>");
        }
        in.close();
        br.close();
        return errMsg.length() == 0 ? null : errMsg.toString();
    }

    /**
     * 判断是不是windows平台
     * @return true是Windows平台,false非Windows平台
     */
    private static boolean isWindows() {
        return System.getProperty("os.name").indexOf("Windows") != -1;
    }
}
