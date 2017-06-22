package com.chetuan.askapp.client.test;

import com.chetuan.askapp.client.constant.Global;
import com.chetuan.askapp.client.netty.NettyClient;
import com.chetuan.askapp.client.netty.RequestAndCallback;
import com.chetuan.askapp.client.netty.RequestAndCallback.Callback;
import com.chetuan.askapp.client.util.RequestUtil;
import com.chetuan.askapp.model.ModelPrt.Request.DestinationLoc;
import com.chetuan.askapp.model.ModelPrt.Request.Location;
import com.chetuan.askapp.model.ModelPrt.Request.Login;
import com.chetuan.askapp.model.ModelPrt.Request.Logout;
import com.chetuan.askapp.model.ModelPrt.Request.OrderAccept;
import com.chetuan.askapp.model.ModelPrt.Request.OrderInfo;
import com.chetuan.askapp.model.ModelPrt.Request.Registe;
import com.chetuan.askapp.model.ModelPrt.Request.SendCode;
import com.chetuan.askapp.model.ModelPrt.Request.StartLoc;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem;

public class ApiTest {

	private NettyClient client;
	
	public ApiTest(NettyClient client) {
		this.client = client;
	}
	
	public void addCallbackForPush()
	{
		client.addCallbackForPush(Global.PUSH_FORCE_LOGOUT, new RequestAndCallback(true, null, new Callback() {
			@Override
			protected void onResponce(ResponceItem responceItem) {
				//用户在心底客户端登录，强制当前客户端下线
				System.out.println("强制用户下线");
				RequestUtil.setUserId("");
				RequestUtil.setTempToken("");
//				TODO 清除本地的用户数据，并提示用户被强制下线
				
			}
		}));
		
		client.addCallbackForPush(Global.PUSH_ORDER, new RequestAndCallback(true, null, new Callback() {
			@Override
			protected void onResponce(ResponceItem responceItem) {
				System.out.println("接收到订单推送");
				printResponce(responceItem);
			}
		}));
		
		client.addCallbackForPush(Global.PUSH_ORDER_UPDATE, new RequestAndCallback(true, null, new Callback() {
			@Override
			protected void onResponce(ResponceItem responceItem) {
				System.out.println("订单状态改变");
				printResponce(responceItem);
			}
		}));
	}
	
	public void closeChannel() {
		client.sendRequest(new RequestAndCallback(RequestUtil.createRequest("test/closeChannel"), null));
	}

	public void sendCode(String phone) {
		SendCode sendCode = SendCode.newBuilder()
				.setPhone(phone)
				.build();
		client.sendRequest(new RequestAndCallback(RequestUtil.createRequest(NettyClient.rootUrlRST.getSendCode(), sendCode), new Callback() {
			@Override
			protected void onResponce(ResponceItem responceItem) {
				printResponce(responceItem);
				//test
				ApiTest.this.registe("18501735853", "1234", "666666");
			}
		}));
	}
	
	public void registe( String phone, String password, String code) {
		Registe registe = Registe.newBuilder()
				.setPhone(phone)
				.setCode(code)
				.setPassword(password)
				.build();
		client.sendRequest(new RequestAndCallback(RequestUtil.createRequest(NettyClient.rootUrlRST.getRegiste(), registe), new Callback() {
			@Override
			protected void onResponce(ResponceItem responceItem) {
				printResponce(responceItem);
			}
		}));
	}
	
	public void login(String phone, String password, String token) {
		Login login = Login.newBuilder()
				.setPhone(phone)
				.setPassword(password)
				.setToken(token)
				.build();
		client.sendRequest(new RequestAndCallback(RequestUtil.createRequest(NettyClient.rootUrlRST.getLogin(), login), new Callback() {
			@Override
			protected void onResponce(ResponceItem responceItem) {
				printResponce(responceItem);
				
				if(responceItem.getSuccess())
				{
					RequestUtil.setUserId("" + responceItem.getLoginRST().getId());
					RequestUtil.setTempToken(responceItem.getLoginRST().getTempToken());
					//TODO 用户登陆成功，在本地保存用户信息，token可用于下一次自动登录
					
				}
			}
		}));
	}
	
	public void logout(String userid, String token) {
		Logout logout = Logout.newBuilder()
				.setUserid(userid)
				.setToken(token)
				.build();
		client.sendRequest(new RequestAndCallback(RequestUtil.createRequest(NettyClient.rootUrlRST.getLogout(), logout), new Callback() {
			@Override
			protected void onResponce(ResponceItem responceItem) {
				printResponce(responceItem);
				if(responceItem.getSuccess())
				{
					RequestUtil.setUserId("");
					RequestUtil.setTempToken("");
					//TODO 用户退出登陆成功，清空本地信息
					
				}
			}
		}));
	}
	
	public void locationUpdate(double longitude, double latitude) {
		Location location = Location.newBuilder()
				.setLongitude(longitude)
				.setLatitude(latitude)
				.build();
		client.sendRequest(new RequestAndCallback(RequestUtil.createRequest(NettyClient.rootUrlRST.getUpdateAndGetLocation(), location), null));
	}
	
	public void locationUpdateAndGet(double longitude, double latitude, String orderId, String userId, String driverId) {
		Location location = Location.newBuilder()
				.setLongitude(longitude)
				.setLatitude(latitude)
				.build();
		OrderInfo orderInfo = OrderInfo.newBuilder()
				.setOrderId(orderId)
				.setUserId(userId)
				.setDriverId(driverId)
				.build();
		client.sendRequest(new RequestAndCallback(RequestUtil.createRequest(NettyClient.rootUrlRST.getUpdateAndGetLocation(), location, orderInfo), null));
	}
	
	public void aroundDriver(double longitude, double latitude) {
		Location location = Location.newBuilder()
				.setLongitude(longitude)
				.setLatitude(latitude)
				.build();
		client.sendRequest(new RequestAndCallback(RequestUtil.createRequest(NettyClient.rootUrlRST.getAroundDriver(), location), new Callback() {
			@Override
			protected void onResponce(ResponceItem responceItem) {
				printResponce(responceItem);
			}
		}));
	}
	
	public void orderCreate(double startLon, double startLat, double destinationLon, double destinationLat) {
		StartLoc startLoc = StartLoc.newBuilder()
				.setLongitude(startLon)
				.setLatitude(startLat)
				.build();
		DestinationLoc destinationLoc = DestinationLoc.newBuilder()
				.setLongitude(destinationLon)
				.setLatitude(destinationLat)
				.build();
		client.sendRequest(new RequestAndCallback(RequestUtil.createRequest(NettyClient.rootUrlRST.getOrderCreate(), startLoc, destinationLoc), new Callback() {
			@Override
			protected void onResponce(ResponceItem responceItem) {
				printResponce(responceItem);
			}
		}));
	}
	
	public void orderAccept(String orderId, String userId) {
		OrderAccept orderAccept = OrderAccept.newBuilder()
				.setOrderId(orderId)
				.setUserId(userId)
				.build();
		client.sendRequest(new RequestAndCallback(RequestUtil.createRequest(NettyClient.rootUrlRST.getOrderAccept(), orderAccept), new Callback() {
			@Override
			protected void onResponce(ResponceItem responceItem) {
				printResponce(responceItem);
			}
		}));
	}
	
	public void orderUpdate(String orderId, String userId, int status) {
		OrderInfo orderInfo = OrderInfo.newBuilder()
				.setOrderId(orderId)
				.setUserId(userId)
				.setStatus(status)
				.build();
		client.sendRequest(new RequestAndCallback(RequestUtil.createRequest(NettyClient.rootUrlRST.getOrderUpdate(), orderInfo), new Callback() {
			@Override
			protected void onResponce(ResponceItem responceItem) {
				printResponce(responceItem);
			}
		}));
	}
	
	private void printResponce(ResponceItem item)
	{
		System.out.println("this is client >>> "+item);
		System.out.println("this is client >>> "+item.getMsg());
	}
}
