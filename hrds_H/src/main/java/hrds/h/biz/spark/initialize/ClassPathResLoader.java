package hrds.h.biz.spark.initialize;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;

public class ClassPathResLoader {

//	private static Method addURL = initAddMethod();

	private static Instrumentation inst = null;

	// The JRE will call method before launching your main()
	public static void agentmain(final String a, final Instrumentation inst) {
		ClassPathResLoader.inst = inst;
	}

	/**
	 * 初始化addUrl 方法.
	 *
	 * @return 可访问addUrl方法的Method对象
	 */
//	private static Method initAddMethod() {
//
//		try {
//			Method add = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
//			add.setAccessible(true);
//			return add;
//		}
//		catch(Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
	public static void loadClasspath(String filepath) {

		File file = new File(filepath);
		loopFiles(file);
	}

	public static void loadResourceDir(String filepath) {

		File file = new File(filepath);
		loopDirs(file);
	}

	/** */
	/**
	 * 循环遍历目录，找出所有的资源路径。
	 *
	 * @param file 当前遍历文件
	 */
	private static void loopDirs(File file) {

		// 资源文件只加载路径
		if (file.isDirectory()) {
			addURL(file);
			File[] tmps = file.listFiles();
			for (File tmp : tmps) {
				loopDirs(tmp);
			}
		}
	}

	/**
	 * 循环遍历目录，找出所有xml文件
	 *
	 * @param file 当前遍历文件
	 */
	private static void loopFiles(File file) {

		if (file.isDirectory()) {
			File[] tmps = file.listFiles();
			for (File tmp : tmps) {
				loopFiles(tmp);
			}
		} else {
			if (file.getAbsolutePath().endsWith(".xml")) {//找出所有xml文件
				addURL(file);
			}
		}
	}

	/**
	 * 通过filepath加载文件到classpath。
	 *
	 * @return URL
	 * @throws Exception 异常
	 */
	private static void addURL(File file) {

		ClassLoader classloader = ClassLoader.getSystemClassLoader();
		try {
			// If Java 9 or higher use Instrumentation
			if (!(classloader instanceof URLClassLoader)) {
				inst.appendToSystemClassLoaderSearch(new JarFile(file));
				return;
			}
			// If Java 8 or below fallback to old method
			Method m = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
			m.setAccessible(true);
			m.invoke(classloader, (Object) file.toURI().toURL());
			//addURL.invoke(classloader, new Object[] { file.toURI().toURL() });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
