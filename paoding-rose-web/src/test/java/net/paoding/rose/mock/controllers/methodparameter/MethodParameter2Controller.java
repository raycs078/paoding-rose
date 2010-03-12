package net.paoding.rose.mock.controllers.methodparameter;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.ParamConf;
import net.paoding.rose.web.var.Flash;
import net.paoding.rose.web.var.Model;

import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

public class MethodParameter2Controller {

    public void inv(Invocation inv) {
    }

    public void request(ServletRequest request) {
    }

    public void request2(HttpServletRequest request) {
    }

    public void response(ServletResponse response) {
    }

    public void response2(HttpServletResponse response) {
    }

    public void session(HttpSession session) {
    }

    public void session2(@Param(value = "", required = false) HttpSession session) {
    }

    public void model(Model request) {
    }

    public void flash(Flash request) {
    }

    public void hello(@Param("name") String name, @Param("int") int i, @Param("bool") boolean bool) {
    }

    public void multipart0(@Param("file1") MultipartFile file1,
            @Param("file2") MultipartFile file2, MultipartRequest request) {
    }

    public void multipart(@Param("file1") MultipartFile file1, @Param("file2") MultipartFile file2,
            MultipartRequest request) {
    }

    public void multipart2(MultipartRequest request, @Param("file2") MultipartFile file1,
            @Param("file1") MultipartFile file2) {
    }

    public void array(@Param("int") int[] ints, @Param("integer") Integer[] integers,
            @Param("name") String[] names, @Param("bool") boolean[] bools) {
    }

    public void list(@Param(value = "int") List<Integer> ints, //
            @Param(value = "integer") ArrayList<Integer> integers, //
            @Param("name") Collection<String> names, //
            @Param(value = "bool") LinkedList<Boolean> bools) {
    }

    public void set(@Param(value = "int") Set<Integer> ints, //
            @Param(value = "integer") HashSet<Integer> integers, //
            @Param("name") Set<String> names, //
            @Param(value = "bool") TreeSet<Boolean> bools) {
    }

    public void map(@Param("ss") Map<String, String> string2string,
            @Param(value = "is") Map<Integer, String> int2string,
            @Param(value = "null") Map<String, Float> adf,
            @Param(value = "sf") Map<String, Float> string2float,
            @Param(value = "ib") Map<Integer, Boolean> int2bool) {
    }

    public void date(@Param("d") Date d, @Param("sd") java.sql.Date sd, @Param("t") Time t,
            @Param("ts") Timestamp ts) {
    }

    public void datedef(@Param(value = "d", def = "") Date d,
            @Param(value = "sd", def = "123456") java.sql.Date sd, @Param(value = "t") Time t,
            @Param("ts") Timestamp ts) {
    }

    public void datePattern1(@Param(value = "d", conf = {
            @ParamConf(name = "pattern", value = "yyyy.MMddHHmmss"),
            @ParamConf(name = "pattern", value = "yyMMddHHmmss") }) Date d,
            @Param(value = "t", conf = { @ParamConf(name = "pattern", value = "HHmmss"),
                    @ParamConf(name = "pattern", value = "ss.HHmm") }) Time t) {
    }

    public void datePattern2(
            @Param(value = "d", conf = { @ParamConf(name = "pattern", value = "long") }) Date d,
            @Param(value = "t", conf = { @ParamConf(name = "pattern", value = "long") }) Time t) {
    }

    public void userBean(User user) {
    }

    public void userBean2(@Param("ua") User a, @Param("ub") User b) {
    }

    public void bindingResult(User user, BindingResult userBr) {
    }

    //

    public void innt(@Param("controller.id") int cid) {
    }

    public void integer(@Param("controller.id") Integer cid) {
    }

    public void bool(@Param("controller.bool") boolean cid) {
    }

    public void boool(@Param("controller.bool") Boolean cid) {
    }

    public void loong(@Param("controller.id") long cid) {
    }

    public void looong(@Param("controller.id") Long cid) {
    }

    public void string(@Param("controller.id") String cid) {
    }

    public void nullPrimitiveInt(@Param("controller.afadfa2dfdafd") int cid) {
    }

    public void nullPrimitiveBool(@Param("controller.abcda4dfadfed") boolean cid) {
    }

    public void nullPrimitiveBoolWrapper(@Param("controller.abcdeadf2d") Boolean cid) {
    }

    public void inf(Interface in, @Param("a") int a, @Param("controller.bool") boolean b) {

    }

    public interface Interface {

    }

    public static class User {

        private String name;

        private Long id;

        private int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

    }

}

