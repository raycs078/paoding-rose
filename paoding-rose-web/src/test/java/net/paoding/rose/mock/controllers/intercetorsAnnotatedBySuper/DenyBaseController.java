package net.paoding.rose.mock.controllers.intercetorsAnnotatedBySuper;

import net.paoding.rose.web.annotation.Intercepted;

@Intercepted(deny = "tail")
public abstract class DenyBaseController {

}
