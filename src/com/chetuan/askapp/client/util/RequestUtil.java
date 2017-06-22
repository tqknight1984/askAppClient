package com.chetuan.askapp.client.util;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chetuan.askapp.model.ModelPrt.Request;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;

public class RequestUtil {

	private static String userId;
	
	private static String deviceId;
	
	private static String tempToken;
	
	private static int clientType;
	
	public static void initParas(String userId, String deviceId, String tempToken)
	{
		RequestUtil.userId = userId;
		RequestUtil.deviceId = deviceId;
		RequestUtil.tempToken = tempToken;
//		RequestUtil.clientType = clientType;
	}
	
	public static String getUserId() {
		return userId;
	}

	public static void setUserId(String userId) {
		RequestUtil.userId = userId;
	}

	public static String getDeviceId() {
		return deviceId;
	}

	public static void setDeviceId(String deviceId) {
		RequestUtil.deviceId = deviceId;
	}

	public static String getTempToken() {
		return tempToken;
	}

	public static void setTempToken(String tempToken) {
		RequestUtil.tempToken = tempToken;
	}

	private static Map<String, FieldDescriptor> descriptors = new HashMap<String, FieldDescriptor>();
	static {
		Descriptor desc = Request.getDescriptor();
		List<FieldDescriptor> fileDescriptors = desc.getFields();
		for(FieldDescriptor descriptor: fileDescriptors)
		{
			if(descriptor.getType() == Type.MESSAGE)
			{
				String type = descriptor.getMessageType().getName();
				descriptors.put(type, descriptor);
			}
		}
	}
	
	public static Request createRequest(String method, Object... datas)
	{
		try {
			Request.Builder builder = Request.newBuilder();
			builder.setId(new Date().getTime() + "_" + (int)(Math.random() * 1000));
			builder.setUserId(userId);
			builder.setDeviceId(deviceId);
			builder.setTempToken(tempToken);
			builder.setClientType(clientType);
			builder.setMethod(method);
			
			if(datas != null)
			{
				int size = datas.length;
				for(int i = 0; i < size; i ++)
				{
					Object data = datas[i];
					if(data instanceof List)
					{
						List dataList = (List) data;
						int s = dataList.size();
						if(s > 0)
						{
							FieldDescriptor descriptor = descriptors.get(dataList.get(0).getClass().getSimpleName());
							if(descriptor != null)
							{
								for(int j = 0; j < s; j ++)
								{
									builder.addRepeatedField(descriptor, dataList.get(i));
								}
							}
						}
					}
					else {
						FieldDescriptor descriptor = descriptors.get(data.getClass().getSimpleName());
						if(descriptor != null)
						{
							
							builder.setField(descriptor, data);
						}
					}
				}
			}
			
			return builder.build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
