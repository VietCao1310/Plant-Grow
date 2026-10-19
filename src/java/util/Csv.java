package util;

// Small helpers to write one CSV cell (RFC 4180 quoting)
public class Csv {

    // For numbers, dates and codes: only quotes the cell when it has a comma, a quote or a line break
    public static String cell(String s) {
        if (s == null) return "";
        boolean quote = s.indexOf(',') >= 0 || s.indexOf('"') >= 0
                     || s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0;
        if (quote) return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }

    /*
     For text a person typed (a reason, a name). A cell that starts with = + - @ would be run as a
     formula by Excel, so it gets a leading apostrophe first.
     */
    public static String text(String s) {
        if (s == null) return "";
        if (!s.isEmpty() && "=+-@\t\r".indexOf(s.charAt(0)) >= 0) s = "'" + s;
        return cell(s);
    }
}