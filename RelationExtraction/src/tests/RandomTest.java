package tests;

import java.util.ArrayList;

public class RandomTest {
	public static void main(String[] args) {
		ArrayList<Integer> ints = new ArrayList<Integer>();
		ints.add(1);
		ints.add(2);
		System.out.println(ints.contains(1));
		System.out.println(ints.contains(0));
	}
}
