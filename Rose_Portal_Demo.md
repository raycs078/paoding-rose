Portal示例



# 介绍 #

“portal”一词中文翻译为“门户”，所谓门户是指各种信息的集成。

rose portal基于rose框架，是rose的一个插件。这个技术不是基于JavaEE的portlet规范，您只需要使用标准的servlet容器即可实现，而且更简单。

本示例示范如何使用rose portal (基于servlet规范)，在一个门户可以显示两个独立的“信息”，效果如下：

![http://paoding-rose.googlecode.com/svn/other/imgs/demo/portal/portal-with-two-windows.jpg](http://paoding-rose.googlecode.com/svn/other/imgs/demo/portal/portal-with-two-windows.jpg)

# 创建控制器 #

1个是Portal主控，另外2个是窗口控制器
1) 在controllers或子目录下创建Portal控制器：PortalController，创建处理方法，并声明Portal参数

PortalController.java
```
    // 按照rose规范来编写控制器即可，控制器必须放到controllers或其子package
    package com.xiaonei.xxx.controllers;
 
    import net.paoding.rose.web.annotation.ReqMapping;
    import net.paoding.rose.web.annotation.rest.Get;
    import net.paoding.rose.web.portal.Portal;
 
    @Path("portal")
    public class PortalController {
 
        // 标注@Get,表示这个方法要处理的是对/portal的GET请求
        // 在主控控制方法上声明Portal参数即表示这个页面是portal，就这样!
        @Get
        public String home(Portal portal) throws Exception {
            // 使用addWindow向这个portal页面加入各种子信息(我们成为窗口)
            portal.addWindow("weather", "/windows/weather");

            // 第一个参数是用于标识该窗口，使得portal页面中可以引用到这个窗口的html
            // 第二个参数表示这个窗口的地址(可以包含参数)，这个地址等价于forward的地址(也就是这里只要能forward的地址都可以，无论是否是rose框架的，甚至可以是一个jsp页面)
            // 因此，地址没有说一定要以"/windows"开始
            portal.addWindow("todo", "/windows/todo?name=value");

            return "portal-home";
        }
    }
```

2) 在controllers.windows下创建可被/windows/weather，/windows/todo访问的控制器以及方法
WeatherController.java
```
    package com.xiaonei.xxx.controllers.windows;
 
    import net.paoding.rose.web.Invocation;
    import net.paoding.rose.web.annotation.rest.Get;
 
    @Path("weather")
    public class WeatherController {
 
        // 这个方法处理"/windows/weather"，它只是返回一串中文，而非jsp、vm等页面
        @Get
        public String xxx(Invocation inv) {
            return "@今天天气真好，花儿都开料!";
        }
    }
```

TodoController.java
```
    package com.xiaonei.xxx.controllers.windows;
 
    import net.paoding.rose.web.Invocation;
    import net.paoding.rose.web.annotation.rest.Get;
 
    @Path("todo")
    public class TodoController {

        // 在控制器使用全局变量并不好，在此仅是一个演示而已
        private int count;
 
        // 方法名是什么不要紧，关进是@Get代表了这个方法用于处理对/todo的GET请求
        @Get
        public String xxx(Invocation inv, Window window) {
            List<String> list = new ArrayList<String>();
            list.add("吃饭");
            list.add("睡觉");
            list.add(String.valueOf(count++));
            inv.addModel("todolist", list);
            // 返回页面，rose将从这个模块对应的/views/windows下找名字以todo开始的页面
            return "todo";
        }
    }
```

# portal页面 #

webapp/views下创建PortalController需要的portal-home.vm
（没限制只能是vm，亦可使用jsp）
```
<html>
	<head>
	<link href="/static/portal.css" rel="stylesheet" type="text/css" media="all" />
	<title>Portal</title>
	</head>
 
	<body>
	<h2>Portal</h2>
 
	<div class="window">
	<div class="title">天气</div>
        <!--这里使用$weather的"weather"即是第一个window的标识-->
	<div class="content">$weather</div>
	</div>
 
 
	<div class="window">
	<div class="title">待做</div>
        <!--$todo实际是一个Window对象，velocity会调用其toString()输出html的-->
	<div class="content">$todo</div>
	</div>
 
	</body>
</html>
```

# window页面 #

webapp/views/windows创建TodoController需要的页面
todo.vm
```
	<ul>
	#foreach($item in $todolist)
	<li>$item</li>
	#end
	</ul>
```

# css文件 #

在webapp/static下
portal.css
```
	.window {
	  width: 300px;
	  height: 200px;
	  border: solid black 1px;
	  margin-left: 15px;
	  float: left;
	  background-color: white;
	}
	.window .title {
	  background-color: black;
	  color:white;
	  margin: 2px;
	  padding-top: 2px;
	  padding-left: 2px;
	  height: 26px;
	}
	.window .content {
	  height: 100%
	  padding-top: 2px;
	  padding: 2px 2px 0px 2px;
	}
```

# 并发数配置 #

在web.xml的`<web-app>`下配置并发参数值：
```
	<context-param>
		<param-name>portalExecutorCorePoolSize</param-name>
		<param-value>200</param-value>
	</context-param>
```
如果没有配置以上参数，默认portalExecutorCorePoolSize取1，相当于除了http主线程外只有另外1个执行线程，
这对程序的正确性没有任何影响，只是并发能力下降了，整个portal的执行时间也会变长。

# 高级话题 #

1) 可以在PortalController.home方法上设置@PortalSetting(timeout = 100)表示最多等待各个窗口100ms(包括window的页面渲染耗费时间)

2) 可以通过引入xiaonei-commons-interceptors的@Throughput(maxConcurrent = 20)放置在window的xxx方法上，控制最多并发数

3) 可在web.xml配置全局参数设置poral执行器的线程池参数：portalExecutorCorePoolSize、portalExecutorMaxPoolSize、portalExecutorKeepAliveSeconds

参数意义分别参考java.util.concurrent.ThreadPoolExecutor的corePoolSize、maximumPoolSize、keepAliveTime说明

4) 在portal-home页面中的$weather实际是一个net.paoding.rose.web.portal.Window对象，因此可通过$weather.success 判断window的执行是否完成并且是200的，通过$weather.statusCode等了解具体的执行情况。详细请参考net.paoding.rose.web.portal.Window类属性列表。

5) 可在window的控制器TodoController.xxx方法中声明Window window对象，通过window.setTilte(title)或window.set(name, value)相关属性，并在portal-home.vm使用$todo.title
在todo.vm中，则除了使用todo的名字使用$todo.title，也可以通过$window.title来使用。每个$window在不同的窗口的页面代表自己的Window对象，不会“乱串”

# 启动服务器 #

运行该webapp，访问http://localhost/portal 显示如下

![http://paoding-rose.googlecode.com/svn/other/imgs/demo/portal/portal-with-two-windows.large.jpg](http://paoding-rose.googlecode.com/svn/other/imgs/demo/portal/portal-with-two-windows.large.jpg)

# 接下来 #

如果您对上面的演示已经掌握，想看看具体的实现，请看： [《rose portal原理](Rose_Portal_Inside.md)〉