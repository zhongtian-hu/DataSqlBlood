package com.huzt.data;

import com.facebook.presto.jdbc.PrestoConnection;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.facebook.presto.jdbc.PrestoStatement;

import java.sql.*;
import java.util.List;

public class MetadataDb {

    static int lport = 3306;//本地端口
    static String rhost = "18.209.16.20";//远程MySQL服务器
    static int rport = 3306;//远程MySQL服务端口
    private boolean ssh = false;//ssh服务启动标志

    public static String getcolumns(String schemaName,String tablename,List column) {
        String user = "root";//SSH连接用户名
        String password = "8888888";//SSH连接密码
        String host = "t.88888.com";//SSH服务器
        String msg = null;
        int port = 9002;//SSH访问端口
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            session.setPortForwardingL(lport, rhost, rport);
            //System.out.println(host+":" + port + " -> " + rhost + ":" + rport);
            msg=hive(schemaName,tablename.replace("\"",""),column);

            session.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msg;
    }
    public static String hive(String schemaName,String tablename,List column) {
        Connection conn = null;
        ResultSet rs = null;
        Statement st = null;
        String msg = null;
        if (schemaName==null)
            return null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "hive", "hive123");
            st = conn.createStatement();
            String sql = "select col_name,tbl_comment from hive.vw_tab_cols where lower(db_name)='"+schemaName.toLowerCase()+"' and lower(tbl_name)='"+tablename.toLowerCase()+"' and col_name is not null order by col_idx;";
            //System.out.println(sql);
            rs = st.executeQuery(sql);
            while (rs.next()){
                if (rs.getString(2)!=null &&rs.getString(2).equals("Presto View")){
                    msg=presto(schemaName,tablename,column);
                }else {
                    column.add(rs.getString(1));
                }
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            try{
                if(st!=null) st.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        return msg;
    }

    public static String presto(String schemaName,String tablename,List column) {
        PrestoConnection connection = null;
        PrestoStatement stmt = null;
        String msg = null;
        try {
            Class.forName("com.facebook.presto.jdbc.PrestoDriver");
            connection = (PrestoConnection)DriverManager.getConnection("jdbc:presto://presto.fzzq.com:9001/hive", "hive", null);
            stmt = (PrestoStatement)connection.createStatement();
            //System.out.println("show create view "+schemaName+"."+tablename);
 /*           ResultSet rs = stmt.executeQuery("show create view "+schemaName+"."+tablename);
            //System.out.println(rs.first());
            while (rs.next()) {
                msg=rs.getString(1);
            }
            rs.close();*/
            ResultSet rs = stmt.executeQuery("select * from "+schemaName+"."+tablename+" limit 1");
            try {
                for(int i=1;i<=rs.getMetaData().getColumnCount();i++)
                {
                    column.add(rs.getMetaData().getColumnName(i));
                }
            }catch (SQLException e) {
                e.printStackTrace();
            }
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(connection!=null) connection.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        return msg;
    }
}
