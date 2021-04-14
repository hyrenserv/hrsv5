package hrds.producer.common;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class CusClassLoader {

	public URLClassLoader getURLClassLoader() throws MalformedURLException {

		File fileS = new File(new File(CusClassLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent() + File.separator + "Cus_lib");
		File[] listFiles = fileS.listFiles();
		List<String> filesURL = new ArrayList<>();
		if (listFiles != null) {
			for (File file : listFiles) {
				if (file.getName().endsWith("jar")) {
					filesURL.add(file.toURI().toString());
				}
			}
		}
		Object[] array = filesURL.toArray();
		URL[] urls = new URL[array.length];
		for (int i = 0; i < array.length; i++) {
			urls[i] = new URL(array[i].toString());
		}
		return new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
	}

}
