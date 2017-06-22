package com.chetuan.askapp.client.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.chetuan.askapp.client.test.ApiTest;
import com.chetuan.askapp.model.ModelPrt;
import com.chetuan.askapp.model.ModelPrt.Request;
import com.chetuan.askapp.model.ModelPrt.Responce;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem.RootUrlRST;

public class NettyClient {

    private EventLoopGroup group;

    private Bootstrap bootstrap;

    private String ip;

    private int port;

    private boolean isClientClose = false;

    private ChannelFuture curChannelFuture;

    private List<Request> requests = Collections.synchronizedList(new ArrayList<Request>());

    private Map<String, RequestAndCallback> callbacks = new HashMap<String, RequestAndCallback>();

    public static RootUrlRST rootUrlRST;

    public NettyClient(String ip, int port) {
        this.ip = ip;
        this.port = port;

        try {
            group = new NioEventLoopGroup();
            bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new MyChannelHandler());

            group.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    if (isClientClose) {
                        close();
                    } else if (curChannelFuture == null || !curChannelFuture.channel().isActive()) {
                        connect();
                    }
                }
            }, 0, 10, TimeUnit.SECONDS).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    private void test() {
        new Thread() {
            @Override
            public void run() {
                ApiTest test = new ApiTest(NettyClient.this);
                test.addCallbackForPush();
//				test.sendCode("18501735853");
//				test.registe("18918273895", "1234", "852002");
                test.login("18501735853", "1234", "");
//				test.logout("15", "939c4da7-978b-4b18-821b-e2fdcb629993");
//				test.locationUpdate(120.0d, 120.01d);
//				test.aroundDriver(Math.random() + 120, Math.random() + 120);//此接口仅普通用户端可以调用成功，司机端调用不会返回结果

//				try {
//					sleep(2000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				test.orderCreate(120.0d, 120.0d, 120.1d, 120.1d);
            }
        }.start();
    }

    public void addCallbackForPush(String id, RequestAndCallback requestAndCallback) {
        callbacks.put(id, requestAndCallback);
    }

    public void addCallback(RequestAndCallback requestAndCallback) {
        if (requestAndCallback != null && requestAndCallback.shouldCallback()) {
            callbacks.put(requestAndCallback.getRequestId(), requestAndCallback);
        }
    }

    public void sendRequest(RequestAndCallback requestAndCallback) {
        if (requestAndCallback == null || requestAndCallback.isForPush() || isClientClose) {
            return;
        } else {
            addCallback(requestAndCallback);
            if (curChannelFuture == null || !curChannelFuture.channel().isActive()) {
                requests.add(requestAndCallback.getRequest());
                connect();
            } else {
                curChannelFuture.channel().writeAndFlush(requestAndCallback.getRequest());
            }
        }
    }

    public void onRootUrlLoaded() {
        test();
    }

    public void onResponce(Responce responce) {
        int size = responce.getResponcesCount();
        for (int i = 0; i < size; i++) {
            ResponceItem item = responce.getResponces(i);
            RequestAndCallback callback = callbacks.get(item.getId());
            if (callback != null) {
                callback.callback(item);
                if (!callback.isForPush()) {
                    callbacks.remove(item.getId());
                }
            }
        }
    }

    private void connect() {
        try {
            if (curChannelFuture != null && curChannelFuture.channel().isActive()) {
                return;
            }
            curChannelFuture = bootstrap.connect(NettyClient.this.ip, NettyClient.this.port).sync();
            for (int i = 0; i < requests.size(); i = 0) {
                curChannelFuture.channel().writeAndFlush(requests.get(0));
                requests.remove(0);
            }
            curChannelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            close();
        }
    }

    public boolean isClosed() {
        return isClientClose;
    }

    public void close() {
        isClientClose = true;
        group.shutdownGracefully();
    }

    class MyChannelHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel channel) throws Exception {
            channel.pipeline().addLast(new ProtobufVarint32FrameDecoder());
            channel.pipeline().addLast(new ProtobufDecoder(ModelPrt.Responce.getDefaultInstance()));
            channel.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
            channel.pipeline().addLast(new ProtobufEncoder());
            channel.pipeline().addLast(new NettyClientHandler(NettyClient.this));
        }
    }

}
