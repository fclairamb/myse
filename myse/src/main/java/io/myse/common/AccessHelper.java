package io.myse.common;

import io.myse.access.File;
import java.util.ArrayList;
import java.util.List;

public class AccessHelper {

	public static List<File> listAll(File src) throws Exception {
		List<File> list = new ArrayList<>();
		for (File file : src.listFiles()) {
			list.add(file);
			if (file.isDirectory()) {
				list.addAll(listAll(file));
			}
		}
		return list;
	}
}
