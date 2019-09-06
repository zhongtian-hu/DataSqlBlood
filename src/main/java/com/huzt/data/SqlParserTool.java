package com.huzt.data;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
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

import java.io.*;
import java.util.*;

/**
 * jsqlparser解析SQL工具类
 * PlainSelect类不支持union、union all等请使用SetOperationList接口
 *
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
    public static Statement getStmtbysql(String sql) throws JSQLParserException {

        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        Statement sqlStmt = parserManager.parse(new StringReader(sql));
        return sqlStmt;
    }
    public static Statement getStmtbyfile(String filepath) throws JSQLParserException {
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
        String sql = sb.toString();
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        Statement sqlStmt = parserManager.parse(new StringReader(sql.replace("\"","")));
        return sqlStmt;
    }



    /**
     * 获取查询字段
     * @param selectBody
     * @return
     */
    public static void getSelectItems(SelectBody selectBody, List allColumnNames) {
        if (selectBody instanceof PlainSelect) {

            List<SelectItem> selectItemlist = ((PlainSelect)selectBody).getSelectItems();
            SelectItem selectItem = null;
            Expression expression  =null;

            if (selectItemlist != null) {
                for (int i = 0; i < selectItemlist.size(); i++) {
                    selectItem = selectItemlist.get(i);
                    SelectColumn column= new SelectColumn();
                    expression=((SelectExpressionItem) selectItem).getExpression();
                    getfromcolum(expression,column.fromName);
                    column.expression.add(expression.toString());
                    if (((SelectExpressionItem) selectItem).getAlias()!=null){
                        column.NameParse = ((SelectExpressionItem) selectItem).getAlias().toString().replace("AS ","").replace("as ","");
                    }else if (column.fromName.size()==1){
                        Iterator<String > it = column.fromName.iterator();
                        column.NameParse =it.next();
                    }
                    allColumnNames.add(column);
                }
            }
        }else if(selectBody instanceof SetOperationList) {
            SetOperationList setOperationList = (SetOperationList) selectBody;
            List<SelectBody> selects = setOperationList.getSelects();
            for (SelectBody subbody : selects) {
                if (allColumnNames.size()==0){
                    getSelectItems(subbody,allColumnNames);
                }else {
                    List<SelectColumn> sourcolumn =  new ArrayList<>();
                    getSelectItems(subbody,sourcolumn);
                    for (int i=0;i<allColumnNames.size();i++){
                        for (String value : sourcolumn.get(i).fromName){
                            ((SelectColumn)(allColumnNames.get(i))).fromName.add(value);
                        }
                        ((SelectColumn)(allColumnNames.get(i))).expression.add(sourcolumn.get(i).expression.get(0));
                    }
                }
            }
        }

    }
    public static void getfromcolum(Expression expression, Set<String> allColumnNames) {
        if (expression instanceof Column){
            allColumnNames.add(expression.toString());
        }else if (expression instanceof CastExpression){
            getfromcolum(((CastExpression)expression).getLeftExpression(),allColumnNames);
        }else if (expression instanceof Function){
            for (Expression subexp : ((Function)expression).getParameters().getExpressions()){
                getfromcolum(subexp,allColumnNames);
            }
        }else if (expression instanceof Parenthesis){
            getfromcolum(((Parenthesis)expression).getExpression(),allColumnNames);
        }else if (expression instanceof CaseExpression){
            getfromcolum(((CaseExpression)expression).getElseExpression(),allColumnNames);
            getfromcolum(((CaseExpression)expression).getSwitchExpression(),allColumnNames);
            for (WhenClause caseexp : ((CaseExpression)expression).getWhenClauses()){
                getfromcolum(caseexp.getThenExpression(),allColumnNames);
                getfromcolum(caseexp.getWhenExpression(),allColumnNames);
            }

        }else if (expression instanceof BinaryExpression){
            getfromcolum(((BinaryExpression)expression).getLeftExpression(),allColumnNames);
            getfromcolum(((BinaryExpression)expression).getRightExpression(),allColumnNames);

        }else if (expression instanceof InExpression){
            getfromcolum(((InExpression)expression).getLeftExpression(),allColumnNames);

        }else if (expression instanceof NotExpression){
            getfromcolum(((NotExpression)expression).getExpression(),allColumnNames);

        }else if (expression instanceof IsNullExpression){
            getfromcolum(((IsNullExpression)expression).getLeftExpression(),allColumnNames);

        }else if (expression instanceof ExistsExpression){
            getfromcolum(((ExistsExpression)expression).getRightExpression(),allColumnNames);

        }else{
            // System.out.println(((CaseExpression)expression).getElseExpression());
            //         System.out.println(expression);
            //        System.out.println(expression instanceof BinaryExpression);
            //System.out.println(JSONObject.toJSONString(expression));
            //         if (expression!=null &&expression.toString().contains(" IS ")){

            //             System.out.println(((BinaryExpression)expression).getLeftExpression());
            //          }

        }
    }

    public static SelectInfo getSelectInfo(SelectBody selectBody)
    {
        SelectInfo sel=new SelectInfo();
        if (selectBody instanceof PlainSelect) {
            getSelectItems(selectBody,sel.columnlist);
            getexp(selectBody,sel.tables,"",sel.columnlist);
        }else if(selectBody instanceof SetOperationList) {
            SetOperationList setOperationList = (SetOperationList) selectBody;
            List<SelectBody> selects = setOperationList.getSelects();
            for (SelectBody subbody : selects) {
                getSelectItems(selectBody,sel.columnlist);
                getexp(subbody,sel.tables,"",sel.columnlist);
            }
        }
        return sel;
    }
    public static void gettableinfo(Table table,List  tables,String parsel,List col) {
        TableInfo fromtable=new TableInfo();
        if(table.getAlias() != null){
            fromtable.alias=table.getAlias().getName();
        }else {
            fromtable.alias=table.getSchemaName()==null?table.getName():table.getSchemaName()+"."+table.getName();
        }
        fromtable.tablename=table.getName();
        fromtable.schemaName=table.getSchemaName();
        String viewstr="";//= MetadataDb.getcolumns(table.getSchemaName(),table.getName(),fromtable.columnname);
        // System.out.println(viewstr);
        if (viewstr==null){
        }else {
            //         try {
            //              Statement stmt =getStmtbysql(viewstr);
            //               getexp(((CreateView)stmt).getSelect().getSelectBody(),tables,fromtable.alias);
            //         } catch (JSQLParserException e) {
//                e.printStackTrace();
            //          }
        }
        fromtable.partable=parsel;
        tables.add(fromtable);
    }
    public static void getsubinfo(SubSelect subbod,List  tables,String parsel,List col) {
        SelectInfo sub=new SelectInfo();
        if (col.size()==0)
            getSelectItems(subbod.getSelectBody(),col);
        else
            getSelectItems(subbod.getSelectBody(),sub.columnlist);
        getexp(subbod.getSelectBody(),sub.tables,subbod.getAlias().toString(),col);
        sub.partable=parsel;
        sub.alias=subbod.getAlias().toString();
        tables.add(sub);
    }
    public static void getjoininfo(SubJoin subjoin,List  tables,String parsel,List col) {
        FromItem joinleft = subjoin.getLeft();
        if (joinleft instanceof SubSelect) {
            getsubinfo((SubSelect)joinleft,tables,parsel,col);
        }
        if (joinleft instanceof Table) {
            gettableinfo((Table) joinleft,tables,parsel,col);
        }
        if (joinleft instanceof SubJoin) {
            getjoininfo((SubJoin) joinleft,tables,parsel,col);
        }
        for (Join join :  subjoin.getJoinList()) {
            FromItem fromItem = join.getRightItem();
            if (fromItem instanceof SubSelect) {
                getsubinfo((SubSelect)fromItem,tables,parsel,col);
            }
            if (fromItem instanceof Table) {
                gettableinfo((Table) fromItem,tables,parsel,col);
            }
            if (fromItem instanceof SubJoin) {
                getjoininfo((SubJoin) fromItem,tables,parsel,col);
            }
        }
    }
    public static void getexp(SelectBody select,List  tables,String parsel,List col) {
        if (select instanceof PlainSelect) {
            FromItem fromleft = ((PlainSelect) select).getFromItem();
            if (fromleft instanceof SubSelect) {
                getsubinfo((SubSelect)fromleft,tables,parsel,col);
            }
            if (fromleft instanceof SubJoin) {
                getjoininfo((SubJoin) fromleft,tables,parsel,col);
            }
            if (fromleft instanceof Table) {
                gettableinfo((Table) fromleft,tables,parsel,col);
            }
            if (((PlainSelect) select).getJoins() != null) {
                for (Join join : ((PlainSelect) select).getJoins()) {
                    FromItem fromItem = join.getRightItem();
                    if (fromItem instanceof SubSelect) {
                        getsubinfo((SubSelect)fromItem,tables,parsel,col);
                    }
                    if (fromItem instanceof Table) {
                        gettableinfo((Table) fromItem,tables,parsel,col);
                    }
                    if (fromItem instanceof SubJoin) {
                        getjoininfo((SubJoin) fromleft,tables,parsel,col);
                    }
                }
            }
        }
        if (select instanceof SetOperationList) {
            SetOperationList setOperationList = (SetOperationList) select;
            List<SelectBody> selects = setOperationList.getSelects();
            for (SelectBody subbody : selects) {
                getexp(subbody, tables,parsel,col);
            }

        }
    }
}