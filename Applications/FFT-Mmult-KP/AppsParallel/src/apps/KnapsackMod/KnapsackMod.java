package apps.KnapsackMod;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import apps.main.Properties;
import bateria.log.Logger;
import bateria.runner.Runner;

/*The goal is to find a set of items, each with a weight w and a value v such
that the total value is maximal, while not exceeding a fixed weight limit.
The problem for n items is recursively divided into two subproblems for
n-1 items, one with the missimg item placed into the knapsack, and one
without it.
The program takes one parameter: the number of items. The weights and
values are chosen randomly.*/

public final class KnapsackMod extends Runner {

	static final int THRESHOLD = 20;
	static final int MAXVAL = 2000;
	static final int MAXWEIGHT = 1000;

	static final int table_size = 97;
	static final int two_16 = 65536;
	static final int two_15 = two_16 / 2;
	static final double two_31 = two_16 * two_15;
	int[] term;
	int[] fac;
	int[] mod;
	double[] inv_mod;
	int[] current;
	double[] table;

	public void run() {
		term = new int[] { 54773, 28411, 51349 };
		fac = new int[] { 7141, 8121, 4561 };
		mod = new int[] { 259200, 134456, 243000 };
		inv_mod = new double[3];
		current = new int[3];
		table = new double[table_size];

		for (int i1 = 0; i1 < 3; i1++) {
			inv_mod[i1] = 1.0 / (double) mod[i1];
		}

		current[0] = (term[0] + 1) % mod[0];
		current[0] = (fac[0] * current[0] + term[0]) % mod[0];
		current[1] = current[0] % mod[1];
		current[1] = (fac[1] * current[1] + term[1]) % mod[1];

		current[0] = (fac[0] * current[0] + term[0]) % mod[0];
		current[2] = current[0] % mod[2];

		for (int i1 = 0; i1 < table_size; i1++) {
			table[i1] = (float) (((double) current[0] + (double) current[1] * inv_mod[1]) * inv_mod[0]);
		}

		int N, i, limw, totv = 0;
		byte[] s, opts;
		int[] values;
		int[] weights;
		KnapsackMod k = new KnapsackMod();
		ReturnMod ret;

		// no dejo el 18 q es el q esta por defecto en la app original porque
		// activa mucho el gc
		N = 11;
		int Nplus = N + 1;
		s = new byte[Nplus];
		opts = new byte[Nplus];
		values = new int[Nplus];
		weights = new int[Nplus];
		float[] prios = new float[Nplus];

		int ix;
		float r;

		for (i = 0; i <= N; i++) {
			// current[0] = (fac[0] * current[0] + term[0]) % mod[0];
			// current[1] = (fac[1] * current[1] + term[1]) % mod[1];
			// current[2] = (fac[2] * current[2] + term[2]) % mod[2];
			// ix = (table_size * current[2]) / mod[2];
			// r = (float) (table[ix]* two_31);
			// table[ix] = (current[0] + current[1] * inv_mod[1]) * inv_mod[0];

			weights[i] = (this.nextInt()) % MAXWEIGHT;

			// current[0] = (fac[0] * current[0] + term[0]) % mod[0];
			// current[1] = (fac[1] * current[1] + term[1]) % mod[1];
			// current[2] = (fac[2] * current[2] + term[2]) % mod[2];
			// ix = (table_size * current[2]) / mod[2];
			// r = (float) (table[ix]* two_31);
			// table[ix] = (current[0] + current[1] * inv_mod[1]) * inv_mod[0];

			values[i] = (this.nextInt()) % MAXVAL;

			prios[i] = (float) weights[i] / values[i];
			totv += values[i];
		}

		KnapsackMod.sort(prios, values, weights);

		limw = N * MAXWEIGHT / 2;

		if (s == null)
			Logger.getLogger().log("s is NULL");
		if (opts == null)
			Logger.getLogger().log("opts is NULL");

		ret = k.spawn_try_it(1, 0, totv, limw, 0, values, weights, s, opts);
		// Log.d("Kna",Long.toString(System.currentTimeMillis()-time));
	}

	@Override
	public void JGFrun(int size) {
		// TODO Auto-generated method stub

	}

	public ReturnMod spawn_try_it(int i, int tw, int av, int limw, int maxv, int[] values, int[] weights, byte[] s,
			byte[] opts) {
		// return try_it(i, tw, av, limw, maxv, values, weights, s, opts);
		int av1, maxv2;
		byte[] optdup = null;
		av1 = av - values[i];
		maxv2 = -1;
		ReturnMod ret1 = null, ret2 = null, result = null;
		int spawn1 = 0;
		int spawn2 = 0;
		int N = s.length - 1;

		if (tw + weights[i] <= limw) {
			s[i] = 1;
			if (i < N) {
				maxv2 = maxv;
				optdup = (byte[]) opts.clone();

				if (i < THRESHOLD) {
					byte[] sdup = (byte[]) s.clone(); // only for spawn...
					ret1 = spawn_try_it(i + 1, tw + weights[i], av, limw, maxv2, values, weights, sdup, optdup);
				} else {
					ret1 = new ReturnMod();
					ret1.maxv = seq_do_try(i + 1, N, tw + weights[i], av, limw, maxv2, values, weights, s, optdup);
					ret1.opts = optdup;
				}
				spawn1 = 1;
			} else if (av > maxv) {
				maxv = av;
				System.arraycopy(s, 0, opts, 0, N + 1);
			}
			s[i] = 0;
		}
		if (av1 > maxv) {
			if (i < N) {
				if (i < THRESHOLD) {
					byte[] sdup = (byte[]) s.clone(); // only for spawn...
					ret2 = spawn_try_it(i + 1, tw, av1, limw, maxv, values, weights, sdup, opts);
				} else {
					ret2 = new ReturnMod();
					ret2.maxv = seq_do_try(i + 1, N, tw, av1, limw, maxv, values, weights, s, opts);
					ret2.opts = opts;
				}
				spawn2 = 1;
			} else {
				maxv = av1;
				System.arraycopy(s, 0, opts, 0, N + 1);
			}
		}

		if (spawn1 == 1) {
			maxv2 = ret1.maxv;
			optdup = ret1.opts;
			result = ret1;
		}
		if (spawn2 == 1) {
			if (ret2 == null)
				System.err.println("ret2 is NULL");
			maxv = ret2.maxv;
			opts = ret2.opts;
			result = ret2;
		}

		if (result == null) {
			result = new ReturnMod();
		}

		if (maxv2 > maxv) {
			result.maxv = maxv2;
			result.opts = optdup;
		} else {
			result.maxv = maxv;
			if (spawn2 == 1) {
				result.opts = opts;
			} else {
				if (opts == null)
					System.err.println("opts is NULL");
				result.opts = (byte[]) opts.clone(); // make a copy of param!
			}
		}

		return result;
	}

	public int seq_do_try(int i, int N, int tw, int av, int limw, int maxv, int[] values, int[] weights, byte[] s,
			byte[] opts) {
		int av1, maxv2;
		byte[] optdup = null;
		av1 = av - values[i];
		maxv2 = -1;

		if (tw + weights[i] <= limw) {
			s[i] = 1;
			if (i < N) {
				maxv2 = maxv;
				optdup = (byte[]) opts.clone();
				maxv2 = seq_do_try(i + 1, N, tw + weights[i], av, limw, maxv2, values, weights, s, optdup);
			} else if (av > maxv) {
				maxv = av;
				System.arraycopy(s, 0, opts, 0, N + 1);
			}
			s[i] = 0;
		}

		if (av1 > maxv) {
			if (i < N)
				maxv = seq_do_try(i + 1, N, tw, av1, limw, maxv, values, weights, s, opts);
			else {
				maxv = av1;
				System.arraycopy(s, 0, opts, 0, N + 1);
			}
		}

		if (maxv2 > maxv) {
			maxv = maxv2;
			System.arraycopy(optdup, 0, opts, 0, N + 1);
		}

		return maxv;
	}

	private static void sort(float[] prio, int[] a, int[] b) {

		float min = Integer.MAX_VALUE;
		float d;
		int tmp;

		for (int i = 0; i < prio.length - 1; i++) {
			// putmin(prio, a, b, i); // zet min elt in a(vanaf i) op plaats i.

			int minpos = i;
			for (int j = i; j < prio.length; j++) {
				if (prio[j] == 0)
					prio[j] = Integer.MAX_VALUE;
				if (prio[j] < min) {
					minpos = j;
					min = prio[j];
					/* reverse elts in prio, a and b */
					d = prio[i];
					prio[i] = prio[minpos];
					prio[minpos] = d;

					tmp = a[i];
					a[i] = a[minpos];
					a[minpos] = tmp;

					tmp = b[i];
					b[i] = b[minpos];
					b[minpos] = tmp;
				}
			}

		}
	}

	public int nextInt() {
		int ix;
		double r;

		current[0] = (fac[0] * current[0] + term[0]) % mod[0];
		current[1] = (fac[1] * current[1] + term[1]) % mod[1];
		current[2] = (fac[2] * current[2] + term[2]) % mod[2];
		ix = (table_size * current[2]) / mod[2];
		r = table[ix];
		table[ix] = ((double) current[0] + (double) current[1] * inv_mod[1]) * inv_mod[0];
		return (int) (r * two_31);
	}

	public static void mergeSort(float[] prio, int[] a, int[] b) {
		if (prio.length > 1) {
			int elementsInA1 = prio.length / 2;
			int elementsInA2 = prio.length - elementsInA1;

			float arr1[] = new float[elementsInA1];
			float arr2[] = new float[elementsInA2];

			int arrA1[] = new int[elementsInA1];
			int arrA2[] = new int[elementsInA2];

			int arrB1[] = new int[elementsInA1];
			int arrB2[] = new int[elementsInA2];

			int i;
			for (i = 0; i < elementsInA1; i++) {
				arr1[i] = prio[i];
				arrA1[i] = a[i];
				arrB1[i] = b[i];
			}

			int j;
			for (i = elementsInA1; i < elementsInA1 + elementsInA2; i++) {
				j = i - elementsInA1;
				arr2[j] = prio[i];
				arrA2[j] = a[i];
				arrB2[j] = b[i];
			}

			mergeSort(arr1, arrA1, arrB1);
			mergeSort(arr2, arrA2, arrB2);

			i = 0;
			j = 0;
			int k = 0;

			while (arr1.length != j && arr2.length != k) {
				if (arr1[j] <= arr2[k]) {
					prio[i] = arr1[j];
					a[i] = arrA1[i];
					b[i] = arrB1[i];
					i++;
					j++;
				} else {
					prio[i] = arr2[k];
					a[i] = arrA2[k];
					b[i] = arrB2[k];
					i++;
					k++;
				}
			}

			System.arraycopy(arr1, 0, prio, 0, arr1.length);
			System.arraycopy(arr2, 0, prio, arr1.length, arr2.length);

			System.arraycopy(arrA1, 0, a, 0, arrA1.length);
			System.arraycopy(arrA2, 0, a, arrA1.length, arrA2.length);

			System.arraycopy(arrB1, 0, b, 0, arrB1.length);
			System.arraycopy(arrB2, 0, b, arrB1.length, arrB2.length);

			// while(arr1.length != j) {
			// prio[i] = arr1[j];
			// a[i] = arrA1[i];
			// b[i] = arrB1[i];
			// i++;
			// j++;
			// }
			// while(arr2.length != k) {
			// prio[i] = arr2[k];
			// a[i] = arrA2[i];
			// b[i] = arrB2[i];
			// i++;
			// k++;
			// }
		}
	}

}
