package com.chetuan.askapp.client.netty;

import com.chetuan.askapp.client.util.RequestUtil;

public class ClientLuncher {

	public static void main(String[] args) {
		RequestUtil.initParas("", "NX403A3_" + (int) (Math.random() * 10000), "");
//		RequestUtil.initParas("", "NX403A_49", "", 1);
		new NettyClient("127.0.0.1", 9100);
	}
	
}