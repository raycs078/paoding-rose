package net.paoding.rose.mock.controllers;

import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.ReqMapping;

@ReqMapping(path = "{user.id}/order")
public class OrderController {

	public String list(@Param("user.id") Long userId) {
		return "list/" + userId;
	}

	public String show(@Param("user.id") Long userId, @Param("id") String id) {
		return "show/" + userId + "/" + id;
	}

	@ReqMapping(path = "")
	public String def() {
		return "def";
	}
}
