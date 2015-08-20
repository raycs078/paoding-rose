错误处理器代码参考




# 最简单的 #

**记录错误日志** <br>
<b>使用专门的错误页面提示用户</b>

<pre><code>// 直接放在controllers或其子package下(也可以放到其他package下，但需要applicationContext配置)<br>
package com.xiaonei.rose.usage.controllers;<br>
import net.paoding.rose.web.ControllerErrorHandler;<br>
<br>
public class ErrorHandler implements ControllerErrorHandler {<br>
<br>
    public Object onError(Invocation inv, Throwable ex) {<br>
        Log logger = LogFactory.getLog(inv.getControllerClass());<br>
        logger.error("", ex);<br>
        // forward to webapp/views/500.jsp<br>
        return "/views/500.jsp";<br>
    }<br>
}<br>
</code></pre>

<h1>有点区分的</h1>

<b>通过code提示不同的错误信息</b>

<pre><code>import net.paoding.rose.web.ControllerErrorHandler;<br>
<br>
public class ErrorHandler implements ControllerErrorHandler {<br>
<br>
    @Override<br>
    public Object onError(Invocation inv, Throwable ex) {<br>
        if (ex instanceof BizException) {<br>
            BizException bizEx = (BizException) ex;<br>
            String code = bizEx.getCode();<br>
            // 在控制器所在的package中或WEB-INF目录下配置messages.xml，<br>
            // 可配置多个，优先找控制器自己package的，然后是父package的，最后是WEB-INF的<br>
            // messages.xml格式参考在下面<br>
            MessageSource msgSource = inv.getApplicationContext();<br>
            String msg = msgSource.getMessage(code, bizEx.getArgs(), inv.getRequest().getLocale());<br>
            // 在jsp中使用${errorMsg}输出该错误<br>
            inv.addModel("errorMsg", msg); <br>
            return "/views/biz-500.jsp";<br>
        }<br>
        Log logger = LogFactory.getLog(inv.getControllerClass());<br>
        logger.error("", ex);<br>
        // forward to webapp/views/500.jsp<br>
        return "/views/500.jsp";<br>
    }<br>
}<br>
</code></pre>

<b>/views/biz-500.jsp</b>
<pre><code>${errorMsg}<br>
</code></pre>
<b>messages.xml格式参考</b>
<pre><code>&lt;?xml version="1.0" encoding="UTF-8"?&gt;<br>
&lt;!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd"&gt;<br>
&lt;properties&gt;<br>
&lt;comment&gt;Rhyme&lt;/comment&gt;<br>
&lt;entry key="seven-eight"&gt;lay them straight&lt;/entry&gt;<br>
&lt;entry key="five-six"&gt;pick up sticks&lt;/entry&gt;<br>
&lt;entry key="nine-ten"&gt;a big, fat hen&lt;/entry&gt;<br>
&lt;entry key="three-four"&gt;shut the door&lt;/entry&gt;<br>
&lt;entry key="one-two"&gt;buckle my shoe&lt;/entry&gt;<br>
&lt;/properties&gt;<br>
</code></pre>

<h1>更好的区分</h1>
<pre><code>import net.paoding.rose.web.ControllerErrorHandler;<br>
<br>
public class ErrorHandler implements ControllerErrorHandler {<br>
<br>
    // @since 0.9.4 <br>
    // 把方法第2个参数换上具体的异常类...这个onError就只处理所声明的这类异常<br>
    public Object onError(Invocation inv, BizException bizEx) {<br>
        // 略去具体的处理代码......<br>
	return "/views/biz-500.jsp";<br>
    }<br>
<br>
    // 通用onError方法，处理其他onError无法处理的异常<br>
    @Override<br>
    public Object onError(Invocation inv, Throwable ex) {<br>
        // 略去具体的处理代码......<br>
        return "/views/500.jsp";<br>
    }<br>
}<br>
</code></pre>

<h1>将异常让渡给上级模块的错误处理器处理</h1>

<b>1</b> <br>
controllers自己或其子package下都可以拥有独立的ControllerErrorHandler。<br>
如果在web调用过程中，控制器、拦截器等发生异常时，如果给定的module含有自己ControllerErrorHanlder时，则由他处理；如果自己没有则调用上级的ControllerErrorHandler处理。<p>


<b>2</b> <br>
但是如果所在的module有ControllerErrorHandler，如何在有必要的时候将异常抛给上级的ControllerErrorHandler呢？<p>
<pre><code><br>
package com.xiaonei.rose.usage.controllers.error;<br>
<br>
import net.paoding.rose.web.ControllerErrorHandler;<br>
import net.paoding.rose.web.Invocation;<br>
import net.paoding.rose.web.ParentErrorHandler;<br>
<br>
import org.springframework.beans.factory.annotation.Autowired;<br>
<br>
public class ErrorHandler implements ControllerErrorHandler {<br>
<br>
    // 声明ParentErrorHandler,注意，这里不是ControllerErrorHandler<br>
    // 万一上级没有ControllerErrorHandler, 这个字段也不会为空<br>
    @Autowired<br>
    ParentErrorHandler parent;<br>
<br>
    // 处理这个处理器只想处理的<br>
    public Object onError(Invocation inv, RuntimeException ex) throws Throwable {<br>
        System.out.println("---------RuntimeException----------");<br>
        inv.getResponse().getWriter().write("&lt;pre&gt;RuntimeException&lt;br&gt;");<br>
        ex.printStackTrace(inv.getResponse().getWriter());<br>
        inv.getResponse().getWriter().write("&lt;/pre&gt;");<br>
        return "";<br>
    }<br>
<br>
    // 通用的异常抛给上级ControllerErrorHanlder或上级的上级去处理<br>
    @Override<br>
    public Object onError(Invocation inv, Throwable ex) throws Throwable {<br>
        return parent.onError(inv, ex);<br>
    }<br>
<br>
}<br>
</code></pre>

以上parentErrorHanlder的逻辑，Rose提供的ErrorHandlerAdpater类已经封装了<br>
<b>建议您通过extends ErrorHandlerAdapter</b> 实现错误处理器，而非直接实现ControllerErrorHandler<br>
<ul><li>处理这个处理器只想处理的<br>
</li><li>通用的默认就会抛给上级模块的错误处理器处理<br>
<pre><code>public class ErrorHandler extends ErrorHandlerAdapter {<br>
<br>
    public Object onError(Invocation inv, RuntimeException ex) throws Throwable {<br>
        System.out.println("---------RuntimeException----------");<br>
        inv.getResponse().getWriter().write("&lt;pre&gt;RuntimeException&lt;br&gt;");<br>
        ex.printStackTrace(inv.getResponse().getWriter());<br>
        inv.getResponse().getWriter().write("&lt;/pre&gt;");<br>
        return "";<br>
    }<br>
<br>
<br>
}<br>
</code></pre>