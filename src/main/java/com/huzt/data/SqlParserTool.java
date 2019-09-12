package com.huzt.data;

import com.huzt.TableInfo;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.CreateView;

import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;

import java.io.*;
import java.util.*;

/**
 * jsqlparser解析SQL工具类
 * PlainSelect类不支持union、union all等请使用SetOperationList接口
 *
 */

public class SqlParserTool {



    /**
     * 根据SQL语句获取sql操作接口
     * @param sql
     * @return
     * @throws JSQLParserException
     */
    public static Statement getStmtbysql(String sql) throws JSQLParserException {

        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        Statement sqlStmt = parserManager.parse(new StringReader(sql));
        return sqlStmt;
    }
    /**
     * 根据SQL文件获取sql操作接口
     * @param filepath
     * @return
     * @throws JSQLParserException
     */
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
     */
    public static void getSelectItems(SelectBody selectBody, List allColumnNames) {
        if (selectBody instanceof PlainSelect) {

            List<SelectItem> selectItemlist = ((PlainSelect)selectBody).getSelectItems();
            SelectItem selectItem = null;
            Expression expression  =null;

            if (selectItemlist != null) {
                for (int i = 0; i < selectItemlist.size(); i++) {
                    selectItem = selectItemlist.get(i);
                    com.huzt.SelectColumn column= new com.huzt.SelectColumn();
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
                    List<com.huzt.SelectColumn> sourcolumn =  new ArrayList<>();
                    getSelectItems(subbody,sourcolumn);
                    for (int i=0;i<allColumnNames.size();i++){
                        for (String value : sourcolumn.get(i).fromName){
                            ((com.huzt.SelectColumn)(allColumnNames.get(i))).fromName.add(value);
                        }
                        ((com.huzt.SelectColumn)(allColumnNames.get(i))).expression.add(sourcolumn.get(i).expression.get(0));
                    }
                }
            }
        }

    }

    /**
     * 获取查询引用的原表字段
     * @param expression
     * @return allColumnNames
     */
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

    /**
     * 获取sql的查询结果接口
     * @param selectBody
     * @return SelectInfo
     * @throws JSQLParserException
     */
    public static com.huzt.SelectInfo getSelectInfo(SelectBody selectBody)
    {
        com.huzt.SelectInfo sel=new com.huzt.SelectInfo();
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

    /**
     * 获取sql的查询分析结果
     * @param sql
     * @return SelectInfo
     * @throws JSQLParserException
     */
    public static com.huzt.SelectInfo getSelectInfo(String sql) throws JSQLParserException
    {
        com.huzt.SelectInfo resul=null;
        Statement stmt = getStmtbysql(sql);
        if (stmt instanceof Select) {
            Select select = (Select) stmt;
            SelectBody selectBody = select.getSelectBody();
            resul=SqlParserTool.getSelectInfo(selectBody);
            resul.alias="selecttmp";
        }else if (stmt instanceof Insert) {
            SelectBody selectBody = ((Insert)stmt).getSelect().getSelectBody();
            resul=SqlParserTool.getSelectInfo(selectBody);
            resul.alias=((Insert)stmt).getTable().getFullyQualifiedName();
        }else if (stmt instanceof CreateView) {
            SelectBody selectBody = ((CreateView)stmt).getSelect().getSelectBody();
            resul=SqlParserTool.getSelectInfo(selectBody);
            resul.alias=((CreateView)stmt).getView().getFullyQualifiedName();
        }else if (stmt instanceof CreateTable) {
            SelectBody selectBody = ((CreateTable)stmt).getSelect().getSelectBody();
            resul=SqlParserTool.getSelectInfo(selectBody);
            resul.alias=((CreateTable)stmt).getTable().getFullyQualifiedName();
        }
        return resul;
    }
    /**
     * 获取sql的查询的表信息
     * @param table
     * @return col
     * @throws JSQLParserException
     */
    public static void gettableinfo(Table table,List  tables,String parsel,List col) {
        TableInfo fromtable=new TableInfo();
        if(table.getAlias() != null){
            fromtable.alias=table.getAlias().getName();
        }else {
            fromtable.alias=table.getSchemaName()==null?table.getName():table.getSchemaName()+"."+table.getName();
        }
        fromtable.tablename=table.getName();
        fromtable.schemaName=table.getSchemaName();
        String viewstr= MetadataDb.getcolumns(table.getSchemaName(),table.getName(),fromtable.columnname);
        // System.out.println(viewstr);
        if (viewstr==null){
        }else {
            try {
                Statement stmt =getStmtbysql(viewstr);
                getexp(((CreateView)stmt).getSelect().getSelectBody(),tables,fromtable.alias,col);
            } catch (JSQLParserException e) {
                e.printStackTrace();
            }
        }
        fromtable.partable=parsel;
        tables.add(fromtable);
    }
    /**
     * 获取sql的子查询信息
     * @param subbod
     * @return col,tables,parsel
     * @throws JSQLParserException
     */
    public static void getsubinfo(SubSelect subbod,List  tables,String parsel,List col) {
        com.huzt.SelectInfo sub=new com.huzt.SelectInfo();
        if (col.size()==0)
            getSelectItems(subbod.getSelectBody(),col);
        else
            getSelectItems(subbod.getSelectBody(),sub.columnlist);
        getexp(subbod.getSelectBody(),sub.tables,subbod.getAlias().toString(),col);
        sub.partable=parsel;
        sub.alias=subbod.getAlias().toString();
        tables.add(sub);
    }
    /**
     * 获取sql的JOIN查询信息
     * @param subjoin
     */
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
    /**
     * 获取sql的详细信息入口
     * @param select
     */
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