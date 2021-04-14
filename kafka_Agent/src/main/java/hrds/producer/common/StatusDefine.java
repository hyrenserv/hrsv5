package hrds.producer.common;


public class StatusDefine {

	public static void success(Response_result rr, Object data, Object message) {
		rr.setData(data);
		rr.setMessage(message);
		rr.setStatus("1");
	}
	public static void fail(Response_result rr, Object data, Object message) {
		rr.setData(data);
		rr.setMessage(message);
		rr.setStatus("0");
	}
	public static void error(Response_result rr, Object data, Object message) {
		rr.setData(data);
		rr.setMessage(message);
		rr.setStatus("0");
	}
}
