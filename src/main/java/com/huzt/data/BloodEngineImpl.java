package com.huzt.data;
import com.huzt.SelectColumn;
import com.huzt.SelectInfo;
import com.huzt.TableInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class BloodEngineImpl implements BloodEngine{

    public    void getSelectTBlood(SelectInfo select, BloodNode parnode, List<BloodNode> nodes, Set<BloodEdge> edges, int level)  {
        level=level+1;
        for (Object tabinfo : select.tables){
            if (tabinfo instanceof TableInfo){
                BloodNode node =new BloodNode(((TableInfo) tabinfo).schemaName,((TableInfo) tabinfo).tablename,level);
                BloodEdge edge=new BloodEdge(node.getId(),parnode.getId());
                nodes.add(node);
                edges.add(edge);
            }else if  (tabinfo instanceof SelectInfo){
                BloodNode join =((SelectInfo)tabinfo).alias.contains(".")?new BloodNode(((SelectInfo)tabinfo).alias.split("\\.")[0],((SelectInfo)tabinfo).alias.split("\\.")[1],level):new BloodNode(((SelectInfo)tabinfo).alias,level);
                nodes.add(join);
                BloodEdge edge=new BloodEdge(join.getId(),parnode.getId());
                edges.add(edge);
                getSelectTBlood((SelectInfo)tabinfo,join,nodes,edges,level);
            }
        }

    }
    public static   void getSelectFBlood(SelectInfo select,BloodNode parnode,List<BloodNode> nodes,Set<BloodEdge> edges,int level)  {
        level=level+1;
        for (SelectColumn colinfo : select.columnlist){
            BloodNode top =select.alias.contains(".")?new BloodNode(select.alias.split("\\.")[0],select.alias.split("\\.")[1],colinfo.NameParse.contains(".")?colinfo.NameParse.split("\\.")[1]:colinfo.NameParse,level):new BloodNode("temp",select.alias,colinfo.NameParse.contains(".")?colinfo.NameParse.split("\\.")[1]:colinfo.NameParse,level);
            nodes.add(top);
            if (parnode !=null){
                BloodEdge edge=new BloodEdge(top.getId(),parnode.getId());
                edges.add(edge);

            }
            Iterator<String> it = colinfo.fromName.iterator();
            while(it.hasNext()){
                String fromcol=it.next();
                for (Object tabinfo : select.tables)
                {
                    if (fromcol.contains(".")){
                        if ((tabinfo  instanceof TableInfo) &&  (((TableInfo) tabinfo).alias.trim().equals(fromcol.split("\\.")[0]))){
                            BloodNode node =new BloodNode(((TableInfo) tabinfo).schemaName,((TableInfo) tabinfo).tablename,fromcol.split("\\.")[1],level);
                            BloodEdge edge=new BloodEdge(node.getId(),top.getId());
                            nodes.add(node);
                            edges.add(edge);
                        }else if ((tabinfo instanceof SelectInfo) &&  (((SelectInfo) tabinfo).alias.trim().equals(fromcol.split("\\.")[0]))){
                            getSelectFBlood((SelectInfo)tabinfo,top,nodes, edges, level+1);
                        }
                    }else{
                        if (tabinfo  instanceof TableInfo){
                            for (String column:((TableInfo) tabinfo).columnname){
                                if (column.trim().equals(fromcol.trim())){
                                    BloodNode node =new BloodNode(((TableInfo) tabinfo).schemaName,((TableInfo) tabinfo).tablename,fromcol,level);
                                    BloodEdge edge=new BloodEdge(node.getId(),top.getId());
                                    nodes.add(node);
                                    edges.add(edge);
                                }
                            }
                        }else if (tabinfo instanceof SelectInfo){
                            for (SelectColumn column:((SelectInfo) tabinfo).columnlist){
                                if ((column.NameParse.contains(".")?column.NameParse.split("\\.")[1]:column.NameParse).trim().equals(fromcol.trim())){
                                    getSelectFBlood((SelectInfo)tabinfo,top,nodes, edges, level+1);
                                }
                            }
                        }
                    }

                }

            }
        }

    }
    @Override
    public List<SelectInfo> parser(List<String> sqls) throws Exception {
        List<SelectInfo> results=new ArrayList<>();
        for (String sql :sqls){
            SelectInfo resul=SqlParserTool.getSelectInfo(sql);
            results.add(resul);
        }
        return results;
    }

    @Override
    public  List<SqlBlood> getTableBlood(List<String> sqls) throws Exception {
        //解析sql
        List<SelectInfo> results = this.parser(sqls);
        //构建图
        List<SqlBlood> Tblood = new ArrayList<>();
        for(SelectInfo result : results) {
            SqlBlood sqlblood = new SqlBlood();
            BloodNode top = result.alias.contains(".") ? new BloodNode(result.alias.split("\\.")[0], result.alias.split("\\.")[1], 0) : new BloodNode(result.alias, 0);
            sqlblood.nodes.add(top);
            getSelectTBlood(result, top, sqlblood.nodes, sqlblood.edges, 0);
            Tblood.add(sqlblood);
        }
        //返回结果
        return Tblood;
    }

    @Override
    public  List<SqlBlood> getFieldBlood(List<String> sqls) throws Exception {
        //解析sql
        List<SelectInfo> results = this.parser(sqls);
        //获取字段血缘图
        List<SqlBlood> Fblood = new ArrayList<>();
        for(SelectInfo result : results) {
            SqlBlood sqlblood = new SqlBlood();
            getSelectFBlood(result, null, sqlblood.nodes, sqlblood.edges, 0);
            Fblood.add(sqlblood);
        }

        //返回字段血缘
        return Fblood;
    }

}
