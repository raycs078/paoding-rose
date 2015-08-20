#Invocation对象使用说明

## Invocation是 ##

通常意义上的“Invocation”是指一次调用，特别地是指对方法的一次调用，是对“一次调用”的抽象与封装，比如包含了调用的输入参数、被调用的对象与方法等等。

在Rose框架下，最终的处理者应该是某个控制器方法，与之相关的还有参数解析器、验证器以及很有知名度较高的拦截器。为了封装与一次请求处理相关的数据，我们设计了一个Invocation类来表示（习惯上用 inv 这 3 个字母称呼它即可)。

你可以从 inv 获取和本次调用有关的 HttpServletRequest、HttpServletResponse 对象、本次调用的最终控制器类名以及控制器对象、方法对象以及参数等。同时inv还提供了相关的方法，setAttribute和 getAttribute 用于存储只与本次调用有关的属性(如果出现forward，则forward后的这些 attribute 将不是直接可见的)、通过使用addModel(name, value)将数据传递给视图对象(比如jsp、velocity等)

如果一个请求从一个控制器方法中 forward 到另外一个控制器方法，或 include 了另外的控制器方法，前后的inv对象将是不同的对象。当然 inv 也提供了 getPreInvocation 是的能够知道当前当前请求是由哪个调用 forard 或 include 来的。特别的，倘诺一个请求 forward 或 include 了另外一个非 rose 能匹配的地址或页面的，这个页面有 forward 或 include 了一个 rose 匹配的控制器方法，getPreInvocation 也将返回非空，且指得就是最近的那个rose控制器调用，而非中间那个页面。这个功能通常是没有什么特别的价值的，但在 portal 开发中却可以大显身手。

## Invocation在哪里使用？ ##

只有当rose匹配成功并且认定有对应的控制器方法能够处理该请求后，inv才会被创建。一旦创建之后，就可以在参数解析、验证中、拦截器、错误处理器中使用。这些组件都有一些标准的接口，你需要实现相应的方法来实现您的需要，这些方法都提供的inv参数，您可以直接使用。

除了在上面所列的这些组件，还可以在“台风的中心”控制器方法上使用。您可以在控制器方法声明Invocation inv参数(按惯例应该是第一个参数)。这样，在控制器中你就可以通过inv.addModel(name, value)方法向视图组件传递要被渲染的业务数据。

## inv.addModel 与 request.setAttribute ##

在标准的servlet开发规范中，您会调用reqeust.setAttribute向jsp传递要被渲染的业务数据，这种做法没有任何问题。即使在rose框架中，如果您使用jsp作为渲染技术，你一样还可以这样使用。

但是讲究一点的话，我们建议您使用addModel来做这件事情，这样Model-View-Controller就完整了。同时，假如您要使用 velocity 的话，addModel就把数据自动送到vm页面，以后还有什么视图技术吗？如果有的话，通过 addModel 可以很好的实现。

如果不讲究的话，即使您通过request.setAttribute来设置属性，rose一样有办法做到能够把数据提交给 velocity 渲染。但目前还处于“有办法”而已，因为这个“办法”没有被采用。

总之，我建议使用概念更加清晰的addModel、而非request.setAttribute来履行MVC的功能。

另外，需要您了解的是，addModel的具体实现并不是通过调用request.setAttribute来实现的。addModel加入的数据，通过reqeust.getAttribute是得不到的，除在jsp页面中。