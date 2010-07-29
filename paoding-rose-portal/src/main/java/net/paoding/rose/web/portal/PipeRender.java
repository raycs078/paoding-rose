package net.paoding.rose.web.portal;

import java.io.IOException;
import java.io.PrintWriter;

public interface PipeRender {

    public void render(Window window, PrintWriter out) throws IOException;
}
