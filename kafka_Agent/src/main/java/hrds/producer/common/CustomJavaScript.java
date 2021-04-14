package hrds.producer.common;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

public class CustomJavaScript {

	private static final transient Logger logger = LogManager.getLogger();

	public Invocable getInvocable(String js) {
		Invocable invocable = null;
		try {
			ScriptEngineManager sm = new ScriptEngineManager();
			NashornScriptEngineFactory factory = null;
			for (ScriptEngineFactory f : sm.getEngineFactories()) {
				if (f.getEngineName().equalsIgnoreCase("Oracle Nashorn")) {
					factory = (NashornScriptEngineFactory) f;
					break;
				}
			}
			String[] stringArray = new String[]{"-doe", "--global-per-engine"};
			ScriptEngine engine = factory.getScriptEngine(stringArray);
			engine.eval(js);
			invocable = (Invocable) engine;
		} catch (Exception e) {
			logger.error(e);
			System.exit(-1);
		}
		return invocable;

	}

}
