package com.chetuan.askapp.client.netty;

import com.chetuan.askapp.model.ModelPrt.Request;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem;

public class RequestAndCallback {

	private boolean isForPush = false;
	
	private Request request; 
	
	private Callback callback;
	
	public RequestAndCallback(Request request,
			Callback callback) {
		this.isForPush = false;
		this.request = request;
		this.callback = callback;
	}
	
	public RequestAndCallback(boolean isForPush, Request request,
			Callback callback) {
		this.isForPush = isForPush;
		this.request = request;
		this.callback = callback;
	}
	
	public boolean isForPush() {
		return isForPush;
	}

	public String getRequestId() {
		return request.getId();
	}

	public Request getRequest() {
		return request;
	}
	
	public boolean shouldCallback()
	{
		return callback != null;
	}
	
	public void callback(ResponceItem responceItem)
	{
		callback.onResponce(responceItem);
	}
	
	public static abstract class Callback {
		protected abstract void onResponce(ResponceItem responceItem);
	}
	
}
