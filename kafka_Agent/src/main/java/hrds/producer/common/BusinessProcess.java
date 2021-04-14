package hrds.producer.common;

import com.alibaba.fastjson.JSONObject;
import org.apache.avro.generic.GenericRecord;

import java.util.List;

public interface BusinessProcess {
	//avro
	GenericRecord process(List<String> listColumn, GenericRecord genericRecord);
	//string
	JSONObject process(List<String> listColumn, JSONObject json);

}
