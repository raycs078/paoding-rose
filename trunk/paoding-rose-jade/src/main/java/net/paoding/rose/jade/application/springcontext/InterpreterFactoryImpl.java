package net.paoding.rose.jade.application.springcontext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import net.paoding.rose.jade.statement.Interpreter;
import net.paoding.rose.jade.statement.InterpreterComparator;
import net.paoding.rose.jade.statement.InterpreterFactory;
import net.paoding.rose.jade.statement.StatementMetaData;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class InterpreterFactoryImpl implements InterpreterFactory, ApplicationContextAware {

    Interpreter[] interpreters;

    ApplicationContext applicationContext;

    public InterpreterFactoryImpl() {
    }

    public InterpreterFactoryImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Interpreter[] getInterpreters(StatementMetaData metaData) {
        if (interpreters == null) {
            synchronized (this) {
                if (interpreters == null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Interpreter> map = applicationContext
                            .getBeansOfType(Interpreter.class);
                    ArrayList<Interpreter> interpreters = new ArrayList<Interpreter>(map.values());
                    Collections.sort(interpreters, new InterpreterComparator());
                    this.interpreters = interpreters.toArray(new Interpreter[0]);
                }
            }
        }
        return interpreters;
    }

}
