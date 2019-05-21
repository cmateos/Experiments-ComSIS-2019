package com.isistan.gradientdescent;

public class Properties {

	// Application's specific parameters
	public static double[][] X_MATRIX = {{1,2}, {1,4}, {1,6}, {1,8}};
	public static double[][] Y_MATRIX = {{2}, {5}, {5}, {8}};
	public static double[][] THETA_MATRIX = new double[2][1];
	public static double ALPHA = 0.01;
	public static int ALGORITHM_ITERATIONS= 9000;
	
	// Microbench's parameters
	public static int ITERATIONS = 10;
	public static int RUNS = 1000;
	public static int WAIT = 0;
	public static int NUM_THREADS = 4;
}
