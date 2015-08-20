本页面主要展示一些常用的控制器类代码片段，以期能够通过这些案例了解控制器的编写要领



# 1、返回json #
http://127.0.0.1/user/json?id=1<br>
<a href='http://127.0.0.1/user/json?id=2'>http://127.0.0.1/user/json?id=2</a>

<pre><code>public class UserController {<br>
<br>
    public Object json(@Param("id") String id) {<br>
        JSONObject json = new JSONObject();<br>
        json.put("id", id);<br>
        json.put("name", "rose");<br>
        json.put("text", "可以有中文");<br>
        // rose将调用json.toString()渲染<br>
        return json;<br>
    }<br>
<br>
    // 把JSONObject放到方法中，Rose将帮忙创建实例<br>
    public Object json2(JSONObject json, @Param("id") String id) {<br>
        json.put("id", id);<br>
        json.put("name", "rose");<br>
        json.put("text", "可以有中文");<br>
        // rose将调用json.toString()渲染<br>
        return json;<br>
    }<br>
}<br>
</code></pre>

<h1>2、返回xml</h1>
<a href='http://127.0.0.1/user/xml'>http://127.0.0.1/user/xml</a><br>


<pre><code>public class UserController {<br>
<br>
    public Object xml(Invocation inv) {<br>
        User user = new User();<br>
        user.setId(1);<br>
        user.setName("rose");<br>
        inv.addModel("user", user);<br>
        // rose将调用user-xml.xml或.vm或.jsp渲染页面(按字母升序顺序优先: jsp, vm, xml)<br>
        // 使用user-xml.xml的，默认contentType是text/xml;charset=UTF-8，语法同velocity<br>
        // 使用user-xml.jsp或user-xml.vm的可在本方法中标注@HttpFeatures(contentType="xxx")改变<br>
        // jsp的也可通过&lt;%@ page contentType="text/html;charset=UTF-8" %&gt;改变<br>
        return "user-xml";<br>
    }<br>
<br>
<br>
}<br>
</code></pre>
<b>views/user-xml.xml</b>
<pre><code>&lt;?xml version="1.0" encoding="UTF-8"?&gt;<br>
&lt;user&gt;<br>
    &lt;id&gt;$user.id&lt;/id&gt;<br>
    &lt;name&gt;$user.name&lt;/name&gt;<br>
&lt;/user&gt;<br>
</code></pre>
如果是使用DOM构建xml的，则建议在所在的controllers下创建DocumentInterceptor拦截器，<br>
控制器直接返回Document对象，<br>
<br>
<a href='http://127.0.0.1/user/xml2'>http://127.0.0.1/user/xml2</a><br>
<pre><code>package com.xiaonei.rose.usage.controllers;<br>
<br>
public class DocumentInterceptor extends ControllerInterceptorAdapter {<br>
    @Override<br>
    public Object after(Invocation inv, Object instruction) throws Exception {<br>
        if (instruction instanceof Documenet) {<br>
            Docuement doc = (Docuement) instruction;<br>
            HttpServletResponse response = inv.getResponse();<br>
            if (response.getContentType() == null) {<br>
                response.setContentType("text/xml;charset=UTF-8");<br>
            }<br>
            document.write(response.getWriter());<br>
            return ""; // 返回空串给Rose，让其不用再管render的事情了<br>
        }<br>
        return instruction; <br>
    }<br>
}<br>
<br>
</code></pre>
<pre><code>import org.dom4j.Document;<br>
import org.dom4j.DocumentHelper;<br>
import org.dom4j.DomDocument;<br>
import org.dom4j.Element;<br>
<br>
public class UserController {<br>
<br>
    public Object xml2(Invocation inv) {<br>
        Document doc = new DomDocument();<br>
        Element listElement = doc.addElement("user-list");<br>
  <br>
        Element user1Element = listElement.addElement("user");<br>
        user1Element.addAttribute("id", "1");<br>
        user1Element.addAttribute("name", "paoding");<br>
<br>
        Element user2Element = listElement.addElement("user");<br>
        user1Element.addAttribute("id", "2");<br>
        user1Element.addAttribute("name", "rose");<br>
<br>
        return doc;<br>
    }<br>
<br>
<br>
}<br>
</code></pre>