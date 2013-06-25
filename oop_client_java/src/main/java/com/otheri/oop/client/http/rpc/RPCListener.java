package com.otheri.oop.client.http.rpc;

import com.otheri.commons.msg.Message;

public interface RPCListener {

	public void onCancel(RPCTask rpcTask);

	public void onSuccess(RPCTask rpcTask, Message resp);

	public void onFailure(RPCTask rpcTask, String error);
}
