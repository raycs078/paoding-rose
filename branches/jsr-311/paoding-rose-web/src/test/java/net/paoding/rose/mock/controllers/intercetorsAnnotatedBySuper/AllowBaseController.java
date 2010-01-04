package net.paoding.rose.mock.controllers.intercetorsAnnotatedBySuper;

import net.paoding.rose.web.annotation.Intercepted;

@Intercepted(allow = "tail")
public abstract class AllowBaseController {

}
