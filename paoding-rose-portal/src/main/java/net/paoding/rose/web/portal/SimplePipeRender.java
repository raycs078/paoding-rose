package net.paoding.rose.web.portal;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.paoding.rose.web.Invocation;

public class SimplePipeRender implements PipeRender {

    public static SimplePipeRender getInstance() {
        return new SimplePipeRender();
    }

    public static void main(String[] args) {
        Integer c = 1;
        Integer a = c;
        Integer b = 1;
        System.out.println(c.hashCode());
        c++;
        System.out.println(c);
        System.out.println(a == c);
        System.out.println(a == b);
    }

    @Override
    public void render(Window window, PrintWriter out) throws IOException {
        Invocation portalInv = window.getPortal().getInvocation();
        String key = SimplePipeRender.class.getName() + "#count";
        Integer c = (Integer) portalInv.getAttribute(key);
        if (c == null) {
            c = 1;
        } else {
            c++;
        }
        portalInv.setAttribute(key, c);
        out.println("<div id=\"");
        out.println(window.getName());
        out.println("\">");
        out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS").format(new Date()));
        out.println("&nbsp;&nbsp;&nbsp;&nbsp;");
        out.print(c);
        out.println("&nbsp;&nbsp;&nbsp;&nbsp;");
        out.println(window.getName());
        out.println("&nbsp;&nbsp;&nbsp;&nbsp;");
        out.println(window.getContent());
        out.println("</div>");
    }

}
