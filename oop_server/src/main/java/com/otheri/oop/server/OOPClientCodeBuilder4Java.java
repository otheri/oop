package com.otheri.oop.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

public class OOPClientCodeBuilder4Java {

	private String filePath;

	public OOPClientCodeBuilder4Java(String filePath) {
		this.filePath = filePath;

	}

	public void build(String className, OOPClass clazz) {
		// 创建文件

		StringBuilder sb = new StringBuilder();

		Class<?> rc = clazz.realClassObject.getClass();

		// class head start
		sb.append(rc.getPackage()).append(".client;\n\n");
		sb.append("import com.alibaba.fastjson.JSON;\n");
		sb.append("import com.alibaba.fastjson.JSONObject;\n");
		sb.append("import com.otheri.commons.msg.Request;\n");
		sb.append("import com.otheri.commons.msg.Response;\n");
		sb.append("import com.otheri.oop.client.http.rpc.RPCListener;\n");
		sb.append("import com.otheri.oop.client.http.rpc.RPCTask;\n\n");
		// class head end

		sb.append("public class ").append(rc.getSimpleName()).append("{\n\n");

		sb.append("\tprivate String url;\n\n");

		// 构造函数start
		sb.append("\tpublic ").append(rc.getSimpleName())
				.append("(String url) {\n");
		sb.append("\t\tthis.url = url;\n");
		sb.append("\t}\n\n");
		// 构造函数end

		// 私有公用方法start
		sb.append("\tprivate Request newRequest(){\n");
		sb.append("\t\tRequest req = new Request();\n");
		sb.append("\t\treq.setDomain(\"").append(className).append("\");\n");
		sb.append("\t\treq.setTimestamp(System.currentTimeMillis());\n");
		sb.append("\t\treturn req;\n");
		sb.append("\t}\n\n");
		// 私有公用方法end

		// 函数方法start
		Iterator<Entry<String, OOPMethod>> methodsIterator = clazz.methods
				.entrySet().iterator();
		while (methodsIterator.hasNext()) {
			Entry<String, OOPMethod> entry = methodsIterator.next();
			String methodName = entry.getKey();
			OOPMethod method = entry.getValue();

			sb.append("\tpublic void ").append(method.methodName).append('(');
			for (int i = 0; i < method.paramTypes.size(); i++) {
				sb.append(method.paramTypes.get(i));
				sb.append(' ');
				sb.append(method.paramNames.get(i));
				sb.append(", ");
			}
			if (method.verify) {
				sb.append("String token, ");
			}
			sb.append("RPCListener listener) {\n");
			sb.append("\t\tRequest req = newRequest();\n");

			sb.append("\t\treq.setMethod(\"").append(methodName)
					.append("\");\n");
			
			sb.append('\n');

			// JSONContent start
			if (method.paramTypes.size() > 0) {
				sb.append("\t\tJSONObject content = new JSONObject();\n");
				for (int i = 0; i < method.paramTypes.size(); i++) {
					String pn = method.paramNames.get(i);
					sb.append("\t\tcontent.put(\"").append(pn).append("\", ")
							.append(pn).append(");\n");
				}
				sb.append("\t\treq.setContent(content);\n");
			}
			// JSONContent end
			
			if (method.verify) {
				sb.append("\t\treq.verify(token);\n");
			}

			sb.append("\t\tRPCTask task = new RPCTask(url, \"")
					.append(method.methodType).append("\" , req, listener);\n");
			sb.append("\t\ttask.execute();\n");

			sb.append("\t}\n\n");
		}

		sb.append("\n}");
		// 函数方法end

		writeFile(rc.getPackage().getName(), rc.getSimpleName(), sb.toString());

	}

	private void writeFile(String packageName, String className, String content) {
		String path = filePath + packageName.replace('.', '/') + "/client/";
		File folder = new File(path);
		folder.mkdirs();

		File file = new File(path + className + ".java");
		System.out.println("file : " + path + className + ".java");
		try {
			FileWriter fw = new FileWriter(file);
			fw.write(content);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
