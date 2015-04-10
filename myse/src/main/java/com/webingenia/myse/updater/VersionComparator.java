package com.webingenia.myse.updater;

import java.util.Comparator;

public class VersionComparator implements Comparator<String> {

	private static final String SIMULATED_DEV_VERSION = "1.1";

	private int[] stringToInts(String str) {
		str = str.split("-")[0];
		String[] spl = str.split("\\.");
		int ints[] = new int[spl.length];
		for (int i = 0; i < spl.length; i++) {
			ints[i] = Integer.parseInt(spl[i]);
		}
		return ints;
	}

	@Override
	public int compare(String o1, String o2) {

		{ // For dev environment only
			if (o2.startsWith("${")) {
				o2 = SIMULATED_DEV_VERSION;
			}
		}

		{ // Standard version comparison
			int[] array1 = stringToInts(o1);
			int[] array2 = stringToInts(o2);
			int max = Math.max(array1.length, array2.length);
			for (int i = 0; i < max; i++) {
				int v1 = i < array1.length ? array1[i] : 0;
				int v2 = i < array2.length ? array2[i] : 0;
				if (v1 != v2) {
					return v1 - v2;
				}
			}
		}

		{ // Remaining part of version comparison
			String array1[] = o1.split("-");
			String array2[] = o2.split("-");
			int max = Math.max(array2.length, array2.length);
			for (int i = 0; i < max; i++) {
				String v1 = i < array1.length ? array1[i] : "";
				String v2 = i < array2.length ? array2[i] : "";
				int cmp = v1.compareTo(v2);
				if (cmp != 0) {
					return cmp;
				}
			}
		}

		return 0;
	}

}
