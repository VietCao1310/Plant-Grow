package util;

public class Web {

    /**
     * Hàm escape ký tự đặc biệt để tránh lỗi XSS khi hiển thị lên JSP.
     */
    public static String esc(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}
