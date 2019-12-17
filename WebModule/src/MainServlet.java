import javafx.util.Pair;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;

/*
Допилить:
    - unit-тесты для всех ф-й которые что-то возвращают
    - maven
    - git
    - база данных для таблиц в текстовом файле +
 */

@WebServlet ({"/table", "/addNewTable", "/add", "/showTable", "/changeTable",
        "/deleteTable", "/tableAddString", "/tableAddColumn"})
public class MainServlet extends HttpServlet {

    private class TableBase implements Serializable {
        class MyTable implements Serializable{
            int realX;
            int realY;
            int xSize;
            int ySize;
            String name;
            String owner;
            String creatingDate;
            String changingDate;
            ArrayList <String> columsName;
            ArrayList <Pair<String,ArrayList<String>>> table;

            public MyTable(String name, int xSize, int ySize, String owner) {
                this.name = name;
                this.xSize = xSize;
                this.ySize = ySize;
                this.realX = xSize + 1;
                this.realY = ySize + 1;
                this.owner = owner;
                changingDate = new Date().toString();
                creatingDate = changingDate;

                columsName = new ArrayList<>();
                columsName.add("Student");
                for (int i = 1; i < realX; i++) {
                    columsName.add("Column Name");
                }

                table= new ArrayList<>();
                for (int i = 0; i < ySize; i++) {
                    table.add(new Pair <>("name", new ArrayList<>()));
                    for (int j = 0; j < xSize; j++) {
                        table.get(i).getValue().add("0");
                    }
                }
            }

            public void changeValue (String tableType, String Value, int x, int y){
                if (tableType.equals("studName")){
                    table.set(y, new Pair<>(Value, table.get(y).getValue()));
                }
                if (tableType.equals("columsName")){
                    columsName.set(x,Value);
                }
                if (tableType.equals("valuesTable")){
                    table.get(y).getValue().set(x,Value);
                }
                changingDate = new Date().toString();
                backUp();
            }

            public void addColumn(){
                realX++;
                xSize++;
                columsName.add("Column Name");
                for (int i = 0; i < table.size(); i++) {
                    table.get(i).getValue().add("0");
                    backUp();
                }
                changingDate = new Date().toString();
                backUp();
            }

            public void addString(){
                ySize++;
                realY++;
                ArrayList<String> tmp = new ArrayList<>();
                for (int j = 0; j < xSize; j++) {
                    tmp.add("0");
                }
                table.add(new Pair<>("name", tmp));
                changingDate = new Date().toString();
                backUp();
            }
        }

        ArrayList<MyTable> tables;

        public TableBase() {
            tables = new ArrayList<>();
        }

        public void createNewTable (String name, int xSize, int ySize, String owner){
            tables.add(new MyTable(name, xSize, ySize, owner));
            backUp();
        }

        public int getSize (){
            return tables.size();
        }

        public String getTableName (int index){
            return tables.get(index).name;
        }

        public int returnIndex (String name){
            for (int i = 0; i < tables.size(); i++){
                if (tables.get(i).name.equals(name))
                    return i;
            }
            throw new RuntimeException("WrongName!");
        }

        public boolean containsTable (String name){
            for (int i = 0; i < tables.size(); i++){
                if (tables.get(i).name.equals(name))
                    return true;
            }
            return false;
        }

        public void deleteTable (String name){
            if (containsTable(name))
                tables.remove(returnIndex(name));
            backUp();
        }

    }

    private TableBase tableBase;

    protected synchronized void backUp(){
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("backUp.txt"));
            out.writeObject(tableBase.tables);
            System.out.println("Сделал бекАП");
        }
        catch (IOException e){
            System.out.println("БекАп:" + e.getMessage());
        }
    }


    @Override
    public void init() throws ServletException {
        tableBase = new TableBase();
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("backUp.txt"));
            ArrayList<TableBase.MyTable> tmp = (ArrayList<TableBase.MyTable>) in.readObject();
            if (!tmp.isEmpty())
                tableBase.tables = tmp;
            System.out.println("ЗАГРУЗИЛОСЬ!");
        }
        catch (IOException | ClassNotFoundException e){
            System.out.println("Не смог загрузить:" + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        HttpSession httpSession = req.getSession();
        PrintWriter out = resp.getWriter();

        if (httpSession.getAttribute("userName") == null){
            resp.sendRedirect("/login");
            return;
        }

        req.getRequestDispatcher("header.html").include(req,resp);
        out.println("Welcome, <i>" + httpSession.getAttribute("userName") + "</i>");
        if (httpSession.getAttribute("userType").equals("t")){
            out.println("| <a href=\"addNewTable\">AddTable</a> | ");
        }
        req.getRequestDispatcher("links.html").include(req,resp);

        if (req.getRequestURI().contains("addNewTable") && req.getSession().getAttribute("userType").equals("t")){
            req.getRequestDispatcher("addTableForm.html").include(req,resp);
        }

        if (req.getRequestURI().contains("showTable")){

            int index = tableBase.returnIndex(req.getParameter("id"));
            out.println("<p>Table name: <b>" + tableBase.tables.get(index).name + "</b></p>");
            out.println("<p>Teacher: <i>" + tableBase.tables.get(index).owner + "</i></p>");
            out.println("<p>Creating date: " + tableBase.tables.get(index).creatingDate + "</p>");
            out.println("<p>Last change: " + tableBase.tables.get(index).changingDate + "</p>");
            out.println(getTableView(index,req));
            out.println("</body></html>");
            return;

        }

        if (req.getRequestURI().contains("changeTable") && req.getSession().getAttribute("userType").equals("t")){
            req.getRequestDispatcher("changeValue.html").include(req,resp);
            out.println("<input type=\"hidden\" name=\"tableName\" value=\""+ req.getParameter("tableName") +"\">");
            out.println("<input type=\"hidden\" name=\"tableType\" value=\""+ req.getParameter("tableType") +"\">");
            out.println("<input type=\"hidden\" name=\"x\" value=\""+ req.getParameter("x") +"\">");
            out.println("<input type=\"hidden\" name=\"y\" value=\""+ req.getParameter("y") +"\">");
            out.println("</form></body></html>");
            return;
        }

        if (req.getRequestURI().contains("tableAddString")){
            int index = tableBase.returnIndex(req.getParameter("tableName"));
            if (httpSession.getAttribute("userName").equals(tableBase.tables.get(index).owner))
                tableBase.tables.get(index).addString();
            resp.sendRedirect("showTable?id=" + req.getParameter("tableName"));
        }

        if (req.getRequestURI().contains("tableAddColumn")){
            int index = tableBase.returnIndex(req.getParameter("tableName"));
            if (httpSession.getAttribute("userName").equals(tableBase.tables.get(index).owner))
                tableBase.tables.get(index).addColumn();
            resp.sendRedirect("showTable?id=" + req.getParameter("tableName"));
        }

        if (req.getRequestURI().contains("deleteTable")){
            int index = tableBase.returnIndex(req.getParameter("tableName"));
            if (httpSession.getAttribute("userName").equals(tableBase.tables.get(index).owner)){
                tableBase.deleteTable(req.getParameter("tableName"));
            }
        }

        for (int i = 0; i < tableBase.getSize(); i++){
            out.println("<a href=\"showTable?id=" + tableBase.getTableName(i) + "\">" + tableBase.getTableName(i)
                    + "</a> ");
            if (httpSession.getAttribute("userName").equals(tableBase.tables.get(i).owner))
                out.println("[<a href=\"deleteTable?tableName=" + tableBase.getTableName(i) +"\">Delete</a>]");
            out.println("</br>");
        }

        out.println("</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if((req.getSession().getAttribute("userName") != null) &&
                (req.getSession().getAttribute("userType").equals("t"))) {
            if (req.getRequestURI().contains("changeTable")) {
                if (tableBase.containsTable(req.getParameter("tableName"))) {
                    int index = tableBase.returnIndex(req.getParameter("tableName"));
                    if (req.getSession().getAttribute("userName").equals(tableBase.tables.get(index).owner)) {
                        tableBase.tables.get(tableBase.returnIndex(req.getParameter("tableName"))).changeValue(
                                req.getParameter("tableType"),
                                req.getParameter("value"),
                                Integer.parseInt(req.getParameter("x")),
                                Integer.parseInt(req.getParameter("y")));
                    }
                }
                resp.sendRedirect("showTable?id=" + req.getParameter("tableName"));
                return;
            }
        }

        if((req.getSession().getAttribute("userName") != null) &&
                req.getSession().getAttribute("userType").equals("t")) {
            if (req.getRequestURI().contains("add")) {
                if (!tableBase.containsTable(req.getParameter("tableName"))) {
                    tableBase.createNewTable(req.getParameter("tableName"),
                            Integer.parseInt(req.getParameter("xSize")),
                            Integer.parseInt(req.getParameter("ySize")),
                            req.getSession().getAttribute("userName").toString());
                }
                resp.sendRedirect("/table");
                return;
            }
        }
    }

    private String getTableView (int index, HttpServletRequest req){

        StringBuilder stringBuilder = new StringBuilder();
        String userType = req.getSession().getAttribute("userType").toString();
        boolean tableOwner = userType.equals("t") && req.getSession().getAttribute("userName").
                equals(tableBase.tables.get(index).owner);
        stringBuilder.append("<table border=\"1\" cellpadding=\"7\" width=\"100%\"><tr>");

        for (int i = 0; i < tableBase.tables.get(index).columsName.size(); i++) {
            stringBuilder.append("<td align=\"center\">");
            if (tableOwner) {
                stringBuilder.append("<a href =\"changeTable?tableType=columsName&y=0&x=").append(i).
                        append("&tableName=").append(tableBase.tables.get(index).name).append("\">");
            }
            stringBuilder.append(tableBase.tables.get(index).columsName.get(i)).append("</a></td>");
        }

        stringBuilder.append("</tr>");

        for (int i = 0; i < tableBase.tables.get(index).table.size(); i++){

            stringBuilder.append("<td align=\"center\">");
            if (tableOwner){
                stringBuilder.append("<a href =\"changeTable?tableType=studName&y=").append(i).
                        append("&x=0&tableName=").append(tableBase.tables.get(index).name).append("\">");
            }
            stringBuilder.append(tableBase.tables.get(index).table.get(i).getKey()).append("</a></td>");

            for (int j = 0; j < tableBase.tables.get(index).table.get(i).getValue().size(); j++) {

                stringBuilder.append("<td  align=\"center\">");
                if (tableOwner) {
                    stringBuilder.append("<a href =\"changeTable?tableType=valuesTable&y=").append(i).append("&x=").
                            append(j).append("&tableName=").append(tableBase.tables.get(index).name).append("\">");
                }
                 stringBuilder.append(tableBase.tables.get(index).table.get(i).getValue().get(j)).append("</a></td>");

            }
            stringBuilder.append("</tr>");
        }
        stringBuilder.append("</table>");

        if (tableOwner){
            stringBuilder.append("[<a href=\"tableAddString?tableName=" + tableBase.tables.get(index).name
                    + "\">Add new string</a>]");
            stringBuilder.append("[<a href=\"tableAddColumn?tableName=" + tableBase.tables.get(index).name
                    + "\">Add new column</a>]");

        }
        return stringBuilder.toString();

    }
}
