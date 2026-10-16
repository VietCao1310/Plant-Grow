package controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import dao.UserDAO;
import model.AppUser;
import util.PasswordUtil;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private final UserDAO users = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        AppUser u = (username == null) ? null : users.findByUsername(username.trim());

        // One generic message, so the page does not reveal which part was wrong.
        if (u == null || password == null || !PasswordUtil.verify(password, u.getPassHash())) {
            fail(request, response, "Sai ten dang nhap hoac mat khau");
            return;
        }
        if (u.isLocked()) {
            fail(request, response, "Tai khoan da bi khoa");
            return;
        }

        // Drop any old session first, so a pre-login session id cannot be reused.
        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        u.setPassHash(null);   // never keep the hash in the session
        request.getSession(true).setAttribute("user", u);

        response.sendRedirect(request.getContextPath() + "/dashboard");
    }

    private void fail(HttpServletRequest request, HttpServletResponse response, String msg)
            throws ServletException, IOException {
        request.setAttribute("error", msg);
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }
}