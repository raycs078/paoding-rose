package net.paoding.rose.mock.controllers;

import net.paoding.rose.web.annotation.DefaultController;
import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.ReqMapping;

@DefaultController
public class DefController {

	public String index() {
		return "index";
	}

	public String method() {
		return "method";
	}

	@ReqMapping(path = "param_{id}")
	public String param(@Param("id") String id) {
		return "param_" + id;
	}
}