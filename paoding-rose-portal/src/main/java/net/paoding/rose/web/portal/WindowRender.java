package net.paoding.rose.web.portal;

import java.io.IOException;
import java.io.Writer;

public interface WindowRender {

    public void render(Window window, Writer out) throws IOException;
}
