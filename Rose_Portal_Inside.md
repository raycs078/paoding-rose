Portal原理



# 内容 #
这个wiki主要介绍rose portal的实现技术,解析rose portal内部机制。

# 妄语 #

我认为roes portal技术，是一个亮点，在实现上是独创。

rose portal是一个服务端portal技术，而非客户端技术。所谓服务端portal技术，是指所有工作发生在服务器上，客户端在整个过程中没有任何作用，使用rose portal不需要任何js技术的支持。

使用rose portal，您可以将一个页面切成互不相关的多个子模块(我们称之为"window")，如果和配置组件结合，您可以根据根据不同用户的选择为之选择不同的模板以及窗口。


虽然rose portal不依赖前端技术，但你也可以通过和前端技术的结合实现"rose pipe"技术，所谓rose pipe借助了rose portal，不仅仅能够加快了服务器的执行速度，更是降低了页面的渲染和传输等待时间。

关于rose pipe技术及其实现，详见 [《RosePipe》](Rose_Portal_RosePipe.md)

# 开发概述 #

rose portal是rose扩展开的一种重要技术，从某种角度其光耀甚至超过作为它基础的rose。

我们尽量把复杂和创意封装在实现代码中，暴露出来的api是极其简约的。如同 [《第一支portal程序》](Roese_Portal_Demo.md) 所述的那样，开发一个portal程序，你只需要把Portal参数声明在方法上，并在方法中调用portal.addWindow就完成了整个代码结构。这就是和普通rose唯一不同的地方，剩下就都一样了。

# 技术决策 #

本节简单介绍rose portal的技术方向决策，规范、统领技术的实现。

## 转发 ##

servlet容器能够将一个请求从某一个地址转到另外的地址，由转发后的地址实际处理该请求。rose portal利用了这个特性。rose portal在处理window请求时，本质使用forward来实现。和普通的web请求不一样的地方是，所有window的请求都是由同一个起点地址（主控控制器）开始转发，而非由由一个它转发给一个window，然后在由该window转发给下一个window。

## 并发 ##

主控制器把请求转发给不同的window并不是串行的，即不是等某个window执行完毕后，再由主控制器转发给下一个window。rose portal在此作了特别处理，每当您调用portal.addWindow时，就意味着转发已经开始，但是这个转发是由另外的thread来执行的(称为异步)。portal.addWindow并不wait，而是立即返回，所以当您调用多次addWindow时，可能意味着由多少个thread在为你同时执行(如果线程池线程充足)。

## 线程池 ##

web容器本身提供了一个线程池，用于处理容器接收到的用户请求。rose portal框架下的主控是由这个线程池的线程执行的，但对于window控制器却非如此。rose portal另行依赖于独立容器线程池之外java.util.concurrent.ExecutorService服务，并把window的执行交由这个执行服务来执行。

在具体实现上，使用的是java.util.concurrent.ThreadPoolExecutor，这个执行服务提供了一个线程池的策略。


## 输出 ##

作为一个服务端portal框架，无论服务端如何并行、甚至是“乱序”的并行，但是给用户的html或者其它的输出必须是一定的，和并行的调度无关的！

rose portal要把每个window的输出独立出来，window执行服务可以和普通控制器那样合并数据到模板并调用out.println“输出”，但是不可以真的由window自己决定向客户端输出！真正的输出必须由主控线程来统一控制。

rose portal目前采用的是window自行缓冲页面内容，并由主线程来负责实际输出的形式。

# 技术实现关键组件 #

## 技术难点 ##
  * 如何使web容器在并发转发下不会出错？
  * 各个window如何进行独立输出？
  * 主线程和并发的window线程的交互以及结果合并。

## 关键代码 ##

  * [net.paoding.rose.web.portal.impl.PortalRequest.java](http://paoding-rose.googlecode.com/svn/trunk/paoding-rose-portal/src/main/java/net/paoding/rose/web/portal/impl/PortalRequest.java)
  * [net.paoding.rose.web.portal.impl.WindowImpl.java](http://paoding-rose.googlecode.com/svn/trunk/paoding-rose-portal/src/main/java/net/paoding/rose/web/portal/impl/WindowImpl.java)
  * [net.paoding.rose.web.portal.impl.PrivateRequestWrapper.java](http://paoding-rose.googlecode.com/svn/trunk/paoding-rose-portal/src/main/java/net/paoding/rose/web/portal/impl/PrivateRequestWrapper.java)
  * [net.paoding.rose.web.portal.impl.PortalWaitInterceptor.java](http://paoding-rose.googlecode.com/svn/trunk/paoding-rose-portal/src/main/java/net/paoding/rose/web/portal/impl/PortalWaitInterceptor.java)