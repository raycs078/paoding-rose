package net.paoding.rose.web.portal;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimplePipeRender implements PipeRender {

    public static SimplePipeRender getInstance() {
        return new SimplePipeRender();
    }

    @Override
    public void render(Window window, PrintWriter out) throws IOException {
        out.println("<div id=\"");
        out.println(window.getName());
        out.println("\">");
        out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS").format(new Date()));
        out.println("&nbsp;&nbsp;&nbsp;&nbsp;");
        out.println(window.getName());
        out.println("=");
        out.println(window.getContent());
        out.println("</div>");
    }

}
