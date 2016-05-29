package util;

/*
 * 定义 HttpCallbackListener 接口
 * HttpUtil类中 使用该接口来回调服务返回的结果
 */
public interface HttpCallbackListener {
	
	void onFinish(String response);
	void onError(Exception e);
}
