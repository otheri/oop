package com.otheri.oop.client.http.rpc;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.otheri.commons.Consts;
import com.otheri.commons.http.HttpGetTask;
import com.otheri.commons.http.HttpListener;
import com.otheri.commons.http.HttpPostTask;
import com.otheri.commons.http.HttpTask;
import com.otheri.commons.io.Input;
import com.otheri.commons.msg.Message;

public class RPCTask implements HttpListener {

	private String url;

	private Message req;

	private RPCListener listener;

	private String method;

	public RPCTask(String url, String method, Message req, RPCListener listener) {
		this.url = url;
		this.method = method;
		this.req = req;
		this.listener = listener;
	}

	// public Message syncExecute() {
	//
	// if (method.equals(Consts.METHOD_GET)) {
	// try {
	// String MessageUrl = getMessageUrl(url, req);
	// HttpGetTask task = new HttpGetTask(MessageUrl, this);
	// HttpResult result = task.syncExecute();
	// if (result.isResult()) {
	// Object r = onConnect(task, result.getIn());
	// return (Message) r;
	// } else {
	// Message resp = new Message();
	// resp.setResult(false);
	//
	// JSONObject jo = new JSONObject();
	// jo.put("error", result.getError());
	// resp.setContent(jo.toJSONString());
	// return resp;
	// }
	//
	// } catch (Exception e) {
	// Message resp = new Message();
	// resp.setResult(false);
	// JSONObject jo = new JSONObject();
	// jo.put("error", e.toString());
	// resp.setContent(jo.toJSONString());
	// return resp;
	// }
	// } else {
	// OutputStream out = null;
	// try {
	// HttpPostTask task = new HttpPostTask(url, this);
	// out = task.getOutput();
	// byte[] data = JSON.toJSONBytes(req);
	//
	// out.write(data);
	// out.flush();
	//
	// HttpResult result = task.syncExecute();
	// if (result.isResult()) {
	// Object r = onConnect(task, result.getIn());
	// return (Message) r;
	// } else {
	// Message resp = new Message();
	// resp.setResult(false);
	// JSONObject jo = new JSONObject();
	// jo.put("error", result.getError());
	// resp.setContent(jo.toJSONString());
	// return resp;
	// }
	//
	// } catch (Exception e) {
	// Message resp = new Message();
	// resp.setResult(false);
	// JSONObject jo = new JSONObject();
	// jo.put("error", e.toString());
	// resp.setContent(jo.toJSONString());
	// return resp;
	// } finally {
	// try {
	// out.close();
	// } catch (Exception e) {
	// }
	// }
	// }
	// }

	public void execute() {
		if (method.equals(Consts.METHOD_GET)) {
			try {
				String MessageUrl = getMessageUrl(url, req);
				HttpGetTask task = new HttpGetTask(MessageUrl, this);
				task.execute();
			} catch (Exception e) {
				listener.onFailure(this, e.toString());
			}
		} else {
			OutputStream out = null;
			try {
				HttpPostTask task = new HttpPostTask(url, this);
				out = task.getOutput();
				byte[] data = JSON.toJSONBytes(req);

				out.write(data);
				out.flush();

				task.execute();
			} catch (IOException e) {
				listener.onFailure(this, e.toString());
			} finally {
				try {
					out.close();
				} catch (Exception e) {
				}
			}
		}
	}

	private String getMessageUrl(String url, Message req) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(url);
		if (!url.endsWith("/")) {
			sb.append('/');
		}
		sb.append(req.getDomain());
		sb.append('/').append(req.getMethod());

		JSONObject content = JSON.parseObject(req.getContent());
		if (null != content) {
			if (content.size() > 0) {
				sb.append('?');

				Iterator<Entry<String, Object>> itreator = content.entrySet()
						.iterator();
				while (itreator.hasNext()) {
					Entry<String, Object> entry = itreator.next();
					sb.append(entry.getKey());
					sb.append('=');
					sb.append(URLEncoder.encode(entry.getValue().toString(),
							Consts.ENCODING));
					sb.append('&');
				}

				sb.deleteCharAt(sb.length() - 1);
			}

		}

		return sb.toString();
	}

	private static CharsetDecoder charsetDecoder = Charset.forName(
			Consts.ENCODING).newDecoder();;

	@Override
	public Object onConnect(HttpTask httpTask, Input in) throws Exception {
		byte[] data = in.readAll();
		Message resp = JSON.parseObject(data, 0, data.length, charsetDecoder,
				Message.class);
		in.close();
		if (null == resp) {
			throw new Exception("no Message");
		} else {
			return resp;
		}
	}

	@Override
	public void onCancel(HttpTask httpTask) {
		listener.onCancel(this);
	}

	@Override
	public void onSuccess(HttpTask httpTask, Object obj) {
		listener.onSuccess(this, (Message) obj);
	}

	@Override
	public void onFailure(HttpTask httpTask, String error) {
		listener.onFailure(this, error);
	}
}
