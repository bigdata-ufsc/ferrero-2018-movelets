package br.ufsc.trajectoryclassification.utils;

import java.util.Comparator;

public class ArrayIndexComparator implements Comparator<Integer> {
	private final double[] array;

	public ArrayIndexComparator(double[] array) {
		this.array = array;
	}

	public Integer[] createIndexArray() {
		Integer[] indexes = new Integer[array.length];
		for (int i = 0; i < array.length; i++) {
			indexes[i] = i; // Autoboxing
		}
		return indexes;
	}

	@Override
	public int compare(Integer index1, Integer index2) {
		return Double.compare(array[index1], array[index2]);
	}
}