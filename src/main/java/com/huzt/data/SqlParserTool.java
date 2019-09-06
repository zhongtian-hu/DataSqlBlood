package com.huzt.data;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;

import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;

import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * jsqlparser解析SQL工具类
 * PlainSelect类不支持union、union all等请使用SetOperationList接口
 *啊
 */

public class SqlParserTool {



    /**
     * 由于jsqlparser没有获取SQL类型的原始工具，并且在下面操作时需要知道SQL类型，所以编写此工具方法
     * @param sql sql语句
     * @return sql类型，
     * @throws JSQLParserException
     */
    public static SqlType getSqlType(String sql) throws JSQLParserException {
        Statement sqlStmt = CCJSqlParserUtil.parse(new StringReader(sql));
        if (sqlStmt instanceof Alter) {
            return SqlType.ALTER;
        } else if (sqlStmt instanceof CreateIndex) {
            return SqlType.CREATEINDEX;
        } else if (sqlStmt instanceof CreateTable) {
            return SqlType.CREATETABLE;
        } else if (sqlStmt instanceof CreateView) {
            return SqlType.CREATEVIEW;
        } else if (sqlStmt instanceof Delete) {
            return SqlType.DELETE;
        } else if (sqlStmt instanceof Drop) {
            return SqlType.DROP;
        } else if (sqlStmt instanceof Execute) {
            return SqlType.EXECUTE;
        } else if (sqlStmt instanceof Insert) {
            return SqlType.INSERT;
        } else if (sqlStmt instanceof Merge) {
            return SqlType.MERGE;
        } else if (sqlStmt instanceof Replace) {
            return SqlType.REPLACE;
        } else if (sqlStmt instanceof Select) {
            return SqlType.SELECT;
        } else if (sqlStmt instanceof Truncate) {
            return SqlType.TRUNCATE;
        } else if (sqlStmt instanceof Update) {
            return SqlType.UPDATE;
        } else if (sqlStmt instanceof Upsert) {
            return SqlType.UPSERT;
        } else {
            return SqlType.NONE;
        }
    }

    /**
     * 获取sql操作接口,与上面类型判断结合使用
     * example:
     * String sql = "create table a(a string)";
     * SqlType sqlType = SqlParserTool.getSqlType(sql);
     * if(sqlType.equals(SqlType.SELECT)){
     *     Select statement = (Select) SqlParserTool.getStatement(sql);
     *  }
     * @param sql
     * @return
     * @throws JSQLParserException
     */
    public static Statement getStatement(String sql) throws JSQLParserException {
        Statement sqlStmt = CCJSqlParserUtil.parse(new StringReader(sql));
        return sqlStmt;
    }

    /**
     * 获取tables的表名
     * @param
     * @return
     */
    public static Map<String,Object> getTables(SelectBody sel){
        Map<String, Object> map= new HashMap<String, Object>();
        PlainSelect plain = (PlainSelect) sel;
        Table table = (Table)plain.getFromItem();
        if(table.getAlias() != null){
            map.put(table.getAlias().getName(),table.getName());
        }

        for(Join join : plain.getJoins()){
            FromItem fromItem=join.getRightItem();
            if (fromItem instanceof SubSelect) {
                map.put(fromItem.getAlias().getName(), fromItem.toString());
                //     map.put(fromItem.getAlias().getName(), getTables(((SubSelect) fromItem).getSelectBody()));
            } else if(((Table)fromItem).getAlias()!=null){
                map.put(((Table)fromItem).getAlias().getName(),((Table)fromItem).getName());
            }
        }
        return map;
    }

    /**
     * 获取join层级
     * @param selectBody
     * @return
     */
    public static List<Join> getJoins(SelectBody selectBody){
        if(selectBody instanceof PlainSelect){
            List<Join> joins =((PlainSelect) selectBody).getJoins();
            return joins;
        }
        return new ArrayList<Join>();
    }

    /**
     *
     * @param selectBody
     * @return
     */
    public static List<Table> getIntoTables(SelectBody selectBody){
        if(selectBody instanceof PlainSelect){
            List<Table> tables = ((PlainSelect) selectBody).getIntoTables();
            return tables;
        }
        return new ArrayList<Table>();
    }

    /**
     *
     * @param selectBody
     * @return
     */
    public static void setIntoTables(SelectBody selectBody,List<Table> tables){
        if(selectBody instanceof PlainSelect){
            ((PlainSelect) selectBody).setIntoTables(tables);
        }
    }



    /**
     * 获取FromItem不支持子查询操作
     * @param selectBody
     * @return
     */
    public static FromItem getFromItem(SelectBody selectBody){
        if(selectBody instanceof PlainSelect){
            FromItem fromItem = ((PlainSelect) selectBody).getFromItem();
            return fromItem;
        }else if(selectBody instanceof WithItem){
            SqlParserTool.getFromItem(((WithItem) selectBody).getSelectBody());
        }
        return null;
    }

    /**
     * 获取子查询
     * @param selectBody
     * @return
     */
    public static SubSelect getSubSelect(SelectBody selectBody){
        if(selectBody instanceof PlainSelect){
            FromItem fromItem = ((PlainSelect) selectBody).getFromItem();
            if(fromItem instanceof SubSelect){
                return ((SubSelect) fromItem);
            }
        }else if(selectBody instanceof WithItem){
            SqlParserTool.getSubSelect(((WithItem) selectBody).getSelectBody());
        }
        return null;
    }

    /**
     * 判断是否为多级子查询
     * @param selectBody
     * @return
     */
    public static boolean isMultiSubSelect(SelectBody selectBody){
        if(selectBody instanceof PlainSelect){
            FromItem fromItem = ((PlainSelect) selectBody).getFromItem();
            if(fromItem instanceof SubSelect){
                SelectBody subBody = ((SubSelect) fromItem).getSelectBody();
                if(subBody instanceof PlainSelect){
                    FromItem subFromItem = ((PlainSelect) subBody).getFromItem();
                    if(subFromItem instanceof SubSelect){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 获取查询字段
     * @param selectBody
     * @return
     */
    public static void getSelectItems(SelectBody selectBody, List allColumnNames) {

        List<SelectItem> selectItemlist = ((PlainSelect)selectBody).getSelectItems();
        SelectItem selectItem = null;
        SelectExpressionItem selectExpressionItem = null;
        Expression expression = null;

        if (selectItemlist != null) {
            for (int i = 0; i < selectItemlist.size(); i++) {
                selectItem = selectItemlist.get(i);
                SelectColumn column= new SelectColumn();
                if (selectItem instanceof SelectExpressionItem)
                {
                    selectExpressionItem = (SelectExpressionItem) selectItemlist.get(i);
                    column.expression=selectExpressionItem.getExpression().toString();
                    expression=selectExpressionItem.getExpression();

                    if(expression instanceof CastExpression){
                        if (((CastExpression) expression).getLeftExpression() instanceof Column){
                            column.sourcolumn.add(((CastExpression) expression).getLeftExpression().toString());
                            if (selectExpressionItem.getAlias()!=null){
                                column.alias = selectExpressionItem.getAlias().toString().replace("AS ","").replace("as ","");
                            }else{
                                column.alias =((CastExpression) expression).getLeftExpression().toString();
                            }
                        }else{
                            getSrcColumn(((CastExpression) expression).getLeftExpression().toString(),column.sourcolumn);
                            column.alias = selectExpressionItem.getAlias().toString().replace("AS ","").replace("as ","");
                        }
                    }else{
                        if (expression instanceof Column){
                            column.sourcolumn.add(expression.toString());
                            if (selectExpressionItem.getAlias()!=null){
                                column.alias = selectExpressionItem.getAlias().toString().replace("AS ","").replace("as ","");
                            }else{
                                column.alias =((Column) expression).getColumnName();
                            }
                        }else{
                            getSrcColumn(expression.toString(),column.sourcolumn);
                            column.alias = selectExpressionItem.getAlias().toString().replace("AS ","").replace("as ","");
                        }
                    }
                    allColumnNames.add(column);
                }
            }
        }

    }

    public static void getColumnName(Expression expression, List allColumnNames) {

        String columnName = null;
        if(expression instanceof BinaryExpression){
            //获得左边表达式
            Expression leftExpression = ((BinaryExpression) expression).getLeftExpression();

            //如果左边表达式为Column对象，则直接获得列名
            if(leftExpression  instanceof Column){
                //获得列名
                columnName = ((Column) leftExpression).getFullyQualifiedName();
                allColumnNames.add(columnName);
            }
            //否则，进行迭代
            else if(leftExpression instanceof BinaryExpression){
                getColumnName((BinaryExpression)leftExpression,allColumnNames);
            }
            //获得右边表达式，并分解
            Expression rightExpression = ((BinaryExpression) expression).getRightExpression();
            // Expression leftExpression2 = ((BinaryExpression) rightExpression).getLeftExpression();
            if(rightExpression instanceof Column){
                //获得列名
                columnName = ((Column) rightExpression).getFullyQualifiedName();
                allColumnNames.add(columnName);
            }//否则，进行迭代
            else if(rightExpression instanceof BinaryExpression){
                getColumnName((BinaryExpression)rightExpression,allColumnNames);
            }
        }
    }

    public static void getSrcColumn(String source,List column)
    {
        Pattern pattern = Pattern.compile("([a-zA-Z]+[a-zA-Z0-9_.]+)");
        Matcher m = pattern.matcher(source);
        Set<String> set = new TreeSet<>();
        String str = "";
        while (m.find()) {
            str = m.group(1);
            if (!str.toUpperCase().equals("CASE") && !str.toUpperCase().equals("WHEN") && !str.toUpperCase().equals("THEN")  && !str.toUpperCase().equals("MIN")  && !str.toUpperCase().equals("MAX")  && !str.toUpperCase().equals("SUM")
                    && !str.toUpperCase().equals("OR") && !str.toUpperCase().equals("AND")  && !str.toUpperCase().equals("ELSE")  && !str.toUpperCase().equals("NULL") && !str.toUpperCase().equals("END")  && !str.toUpperCase().equals("AVG") ){
                set.add(str);
            }
        }
        for (String value : set) {
            column.add(value);
        }
    }

    public static void getSrcTable(PlainSelect select, List<TableInfo>  tables, String parsel)
    {
        Table table = (Table)select.getFromItem();
        if(table.getAlias() != null){
            TableInfo fromtable=new TableInfo();
            fromtable.alias=table.getAlias().getName();
            fromtable.tablename=table.getName();
            fromtable.schemaName=table.getSchemaName();
            fromtable.tabletype="table";
            fromtable.partable=parsel;
            tables.add(fromtable);
        }

        if(select.getJoins()!= null) {
            for(Join join : select.getJoins()){
                TableInfo fromtable=new TableInfo();
                FromItem fromItem=join.getRightItem();
                if (fromItem instanceof SubSelect) {
                    List <SelectColumn> columnlist=new ArrayList<SelectColumn>();
                    getSelectItems(((SubSelect) fromItem).getSelectBody(),columnlist);
                    for (int i=0;i<columnlist.size();i++){
                        fromtable.columnname.add(columnlist.get(i).alias);
                    }
                    fromtable.tablename=fromItem.getAlias().getName();
                    fromtable.tabletype="SubSelect";
                    fromtable.alias=fromItem.getAlias().getName();
                    fromtable.partable=parsel;
                    tables.add(fromtable);
                    getSrcTable((PlainSelect)((SubSelect) fromItem).getSelectBody(),tables,fromItem.getAlias().getName());
                } else {
                    if(((Table)fromItem).getAlias()!=null)
                    {
                        fromtable.alias=fromItem.getAlias().getName();
                    }else {
                        fromtable.alias = ((Table)fromItem).getSchemaName()+"."+((Table)fromItem).getName();
                    }
                    fromtable.schemaName=((Table)fromItem).getSchemaName();
                    fromtable.tablename=((Table)fromItem).getName();
                    fromtable.tabletype="table";
                    fromtable.partable=parsel;
                    tables.add(fromtable);

                }
            }
        }
    }
    public static void main(String[] args) throws JSQLParserException {
        Statement sqlStmt = CCJSqlParserUtil.parse(new StringReader("show databases"));

    }

}