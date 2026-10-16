<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Đăng nhập - PlantGrow</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
    </head>
    <body>
        <div class="login-container" style="width: 350px; margin: 50px auto; padding: 20px; border: 1px solid #ccc; border-radius: 5px;">
            <h2>Đăng nhập hệ thống</h2>
            <p class="sub">Hoc ky FALL2026</p>

            <%
                String error = (String) request.getAttribute("error");
                if (error != null) {
            %>
            <div class="error" style="color: red; margin-bottom: 10px;"><%= util.Web.esc(error)%></div>
            <%
                }
            %>

            <form action="login" method="POST">
                <div style="margin-bottom: 10px;">
                    <label style="display: block;">Username</label>
                    <input type="text" name="username" required style="width: 100%; padding: 5px;" />
                </div>

                <div style="margin-bottom: 15px;">
                    <label style="display: block;">Password</label>
                    <input type="password" name="password" required style="width: 100%; padding: 5px;" />
                </div>

                <button type="submit" style="width: 100%; padding: 8px;">Đăng nhập</button>
            </form>
        </div>
    </body>
</html>