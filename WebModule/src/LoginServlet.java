import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;

@WebServlet ("/login")
public class LoginServlet extends HttpServlet {

    private class UserInfo {

        String userPassword;
        String userType;

        public UserInfo(String userPassword, String userType) {
            this.userPassword = userPassword;
            this.userType = userType;
        }

    }

    private HashMap<String,UserInfo> userBase;

    @Override
    public void init() throws ServletException {
        userBase = new HashMap<>();

        File file = new File ("userBase.txt");

        try {
            file.createNewFile();
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()){
                String [] tmp  = scanner.nextLine().split(":");
                if (tmp.length == 3)
                    if (tmp[0].length() != 0 && tmp[1].length() != 0)
                        if (tmp[2].equals("t")||tmp[2].equals("s"))
                            userBase.put(tmp[0], new UserInfo(tmp[1],tmp[2]));
            }

            System.out.println("Users loaded: " + userBase.size());
            for (HashMap.Entry<String,UserInfo> entry: userBase.entrySet()){
                System.out.println(entry.getKey() + " User type: " + entry.getValue().userType);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (req.getSession().getAttribute("userName") == null) {
            PrintWriter out = resp.getWriter();
            req.getRequestDispatcher("header.html").include(req, resp);
            out.println("<h1>Login</h1>");
            req.getRequestDispatcher("login.html").include(req, resp);
            out.println("</html></body>");
        }
        else {
            resp.sendRedirect("/table");
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String userName = req.getParameter("userName");
        String password = req.getParameter("password");

        PrintWriter out = resp.getWriter();

        if( userBase.containsKey(userName) ){
            if (userBase.get(userName).userPassword.equals(password)) {
                HttpSession session = req.getSession();
                session.setAttribute("userName", userName);
                session.setAttribute("userType", userBase.get(userName).userType);
                resp.sendRedirect("/table");
            }
        }
        else{
            req.getRequestDispatcher("header.html").include(req, resp);
            out.print("<i>Sorry, username or password error!</i>");
            req.getRequestDispatcher("login.html").include(req, resp);
            out.println("</html></body>");
        }

        out.close();

    }
}
