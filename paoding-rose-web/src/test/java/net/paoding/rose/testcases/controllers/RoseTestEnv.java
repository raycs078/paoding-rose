package net.paoding.rose.testcases.controllers;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import net.paoding.rose.RoseFilter;
import net.paoding.rose.mock.web.instruction.MockInstructionExecutor;

import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockServletContext;

public class RoseTestEnv {

    private static RoseTestEnv env;

    private RoseFilter roseFilter;

    private MockInstructionExecutor instructionExecutor;

    public synchronized static RoseTestEnv instance() throws ServletException {
        if (env == null) {
            env = new RoseTestEnv();
        }
        return env;
    }

    private RoseTestEnv() throws ServletException {
        File file = new File("target/test");
        ServletContext servletContext = new MockServletContext(file.getPath(),
                new FileSystemResourceLoader());
        instructionExecutor = new MockInstructionExecutor();
        instructionExecutor.setStoresInstructionInRequest(true);
        roseFilter = new RoseFilter();
        roseFilter.setInstructionExecutor(instructionExecutor);
        roseFilter.init(new MockFilterConfig(servletContext, "roseFilter"));
    }

    public RoseFilter getRoseFilter() {
        return roseFilter;
    }

    public MockInstructionExecutor getInstructionExecutor() {
        return instructionExecutor;
    }

}
