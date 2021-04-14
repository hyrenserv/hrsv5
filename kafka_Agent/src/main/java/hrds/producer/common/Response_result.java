package hrds.producer.common;

public class Response_result {
	private String status;
	private Object data;
	private Object message;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Object getMessage() {
		return message;
	}

	public void setMessage(Object message) {
		this.message = message;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "{\"status\":\"" + status + "\""
				+ ",\"msg\":\"" + message + "\",\"data\":[" + data + "]}";
	}
	public String toJsonString() {
		return "{\"status\":\"" + status 
				+"\",\"msg\":\"" + message + "\",\"data\":" + data
				+ "}";
	}
}
