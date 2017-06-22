package com.chetuan.askapp.client.netty;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.logging.Logger;

import com.chetuan.askapp.client.constant.Global;
import com.chetuan.askapp.client.netty.RequestAndCallback.Callback;
import com.chetuan.askapp.client.util.RequestUtil;
import com.chetuan.askapp.model.ModelPrt.Responce;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem;

public class NettyClientHandler extends ChannelHandlerAdapter {

	private static Logger logger = Logger.getLogger(NettyClientHandler.class.getName());

	private NettyClient client;
	
	public NettyClientHandler(NettyClient client) {
		super();
		this.client = client;
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		super.handlerAdded(ctx);
		System.out.println("准备连接服务器");
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		super.handlerRemoved(ctx);
		System.out.println("与服务器连接断开");
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		if(NettyClient.rootUrlRST == null)
		{
			client.sendRequest(new RequestAndCallback(RequestUtil.createRequest(Global.ROOT_URL), new Callback() {
				@Override
				protected void onResponce(ResponceItem responceItem) {
					if(responceItem.hasRootUrlRST())
					{
						NettyClient.rootUrlRST = responceItem.getRootUrlRST();
						client.onRootUrlLoaded();
						System.err.println(responceItem);
					}
				}
			}));
		}
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		client.onResponce((Responce) msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		logger.warning("Unexpected exception from downstream : " + cause.getMessage());
		ctx.close();
	}
	
}
