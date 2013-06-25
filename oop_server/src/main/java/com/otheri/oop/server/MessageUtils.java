package com.otheri.oop.server;

import java.util.HashMap;

import com.alibaba.fastjson.JSON;
import com.otheri.commons.msg.Request;
import com.otheri.commons.msg.Response;
import com.otheri.commons.msg.ResultCode;

public class MessageUtils {

	public static Response success(Request req, String content) {
		Response resp = new Response();
		resp.setTimestamp(System.currentTimeMillis());
		resp.setDomain(req.getDomain());
		resp.setMethod(req.getMethod());
		resp.setResult(ResultCode.SUCCESS);
		if (content == null) {
			resp.setContent("");
		} else {
			resp.setContent(content);
		}
		return resp;
	}

	public static Response failure(Request req, String error) {
		Response resp = new Response();
		resp.setTimestamp(System.currentTimeMillis());
		resp.setResult(ResultCode.FAILURE);
		if (null == req) {
			resp.setDomain("");
			resp.setMethod("");

			StringBuffer sb = new StringBuffer("{\"error\":\"");
			sb.append(error);
			sb.append("\",\"request\":\"bad request.\"}");
			resp.setContent(sb.toString());
			return resp;
		} else {
			resp.setDomain(req.getDomain());
			resp.setMethod(req.getMethod());

			HashMap<String, Object> content = new HashMap<String, Object>();
			content.put("error", error);
			content.put("request", req);
			resp.setContent(JSON.toJSONString(content));
			return resp;
		}

	}

}
