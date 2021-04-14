package hrds.producer.string.rest;

import org.apache.kafka.clients.producer.KafkaProducer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SingletonProducer {
	private static Map<String, KafkaProducer<String, String>> map = new ConcurrentHashMap<String, KafkaProducer<String, String>>();
	private static Map<String, Object> list = new ConcurrentHashMap<String, Object>();
	
	public Map<String, Object> getList() {
	
		return list;
	}

	public void setList(Map<String, Object> list) {
	
		SingletonProducer.list = list;
	}

	public  Map<String, KafkaProducer<String, String>> getMap() {
	
		return map;
	}
	
	public void setMap(Map<String, KafkaProducer<String, String>> map) {
	
		SingletonProducer.map = map;
	}

	// 定义一个私有构造方法
	private SingletonProducer() {

	}

	//定义一个静态私有变量(不初始化，不使用final关键字，使用volatile保证了多线程访问时instance变量的可见性，避免了instance初始化时其他变量属性还没赋值完时，被另外线程调用)
	private static volatile SingletonProducer instance;

	//定义一个共有的静态方法，返回该类型实例
	public static SingletonProducer getInstance() {

		// 对象实例化时与否判断（不使用同步代码块，instance不等于null时，直接返回对象，提高运行效率）
		if( instance == null ) {
			//同步代码块（对象未初始化时，使用同步代码块，保证多线程访问时对象在第一次创建后，不再重复被创建）
			synchronized( SingletonProducer.class ) {
				//未初始化，则初始instance变量
				if( instance == null ) {
					instance = new SingletonProducer();
				}
			}
		}
		return instance;

	}
}