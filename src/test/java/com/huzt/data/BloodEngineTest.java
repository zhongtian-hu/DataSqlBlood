package com.huzt.data;

import com.alibaba.fastjson.JSONObject;
import com.huzt.SelectInfo;
import net.sf.jsqlparser.JSQLParserException;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class BloodEngineTest {

    @Test

    public void sqltest()
    {String filepath = ".\\sqlfile\\test.sql";//定义一个file对象，用来初始化FileReader
        File file = new File(filepath);//定义一个file对象，用来初始化FileReader
        StringBuilder sb = new StringBuilder();//定义一个字符串缓存，将字符串存放缓存中

        FileReader reader = null;//定义一个fileReader对象，用来初始化BufferedReader
        try {
            reader = new FileReader(file);
            BufferedReader bReader = new BufferedReader(reader);//new一个BufferedReader对象，将文件内容读取到缓存

            String s = "";
            while ((s =bReader.readLine()) != null) {//逐行读取文件内容，不读取换行符和末尾的空格
                sb.append(s + "\n");//将读取的字符串添加换行符后累加存放在缓存中
            }
            bReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String sql = sb.toString().replace("\"","");
        List<String> sqls=new ArrayList<>();
        sqls.add(sql);
  /*   //返回SQL分析结果
        try {
            SelectInfo resul=SqlParserTool.getSelectInfo(sql);
           System.out.println(JSONObject.toJSONString(resul));

        }catch (JSQLParserException e) {
            e.printStackTrace();
        }
     //返回表的血缘关系图
        try {
            BloodEngineImpl test=new BloodEngineImpl();
            List<SqlBlood> blood=test.getTableBlood(sqls);
            //System.out.println(blood.toString());
            System.out.println(JSONObject.toJSONString(blood));
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        //返回字段的血缘关系图
        try {
            BloodEngineImpl test=new BloodEngineImpl();
            List<SqlBlood> blood=test.getFieldBlood(sqls);
            //System.out.println(blood.toString());
            System.out.println(JSONObject.toJSONString(blood));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
