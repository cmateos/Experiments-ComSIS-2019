/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package jaligner;

import jaligner.matrix.Matrix;

import java.util.logging.Logger;

/**
 * An implementation of the Smith-Waterman algorithm with Gotoh's improvement
 * for biological local pairwise sequence alignment.
 * 
 * <strong>Recursive definition:</strong>
 * <ul>
 * <li>
 * <strong>Base conditions:</strong>
 * <ul>
 * <li><code>V(0, 0) = 0</code></li>
 * <li><code>V(i, 0) = E(i, 0) = W<sub>g</sub> + iW<sub>s</sub></code></li>
 * <li><code>V(0, j) = F(0, j) = W<sub>g</sub> + jW<sub>s</sub></code></li>
 * </ul>
 * </li>
 * <li>
 * <strong>Recurrence relation:</strong>
 * <ul>
 * <li><code>V(i, j) = max{E(i, j), F(i, j), G(i, j)}</code>, where:</li>
 * <li><code>G(i, j) = V(i - 1, j - 1) + similarity(S<sub>i</sub>, T<sub>j</sub>)</code></li>
 * <li><code>E(i, j) = max{E(i, j - 1) + W<sub>s</sub>, V(i, j - 1) + W<sub>g</sub> + W<sub>s</sub>}</code></li>
 * <li><code>F(i, j) = max{F(i - 1, j) + W<sub>s</sub>, V(i - 1, j) + W<sub>g</sub> + W<sub>s</sub>}</code></li>
 * </ul>
 * </ul> 
 * 
 * @author Ahmed Moustafa
 */

public final class SmithWatermanGotoh {
	/**
	 * Hidden constructor
	 */
	private SmithWatermanGotoh() {
		super();
	}

	/**
	 * Logger
	 */
	private static final Logger logger = Logger
			.getLogger(SmithWatermanGotoh.class.getName());

	/**
	 * Aligns two sequences by Smith-Waterman (local)
	 * 
	 * @param s1
	 *            sequene #1 ({@link Sequence})
	 * @param s2
	 *            sequene #2 ({@link Sequence})
	 * @param matrix
	 *            scoring matrix ({@link Matrix})
	 * @param o
	 *            open gap penalty
	 * @param e
	 *            extend gap penalty
	 * @return alignment object contains the two aligned sequences, the
	 *         alignment score and alignment statistics
	 * @see Sequence
	 * @see Matrix
	 */
	public static Alignment align(Sequence s1, Sequence s2, Matrix matrix,
			Float o, Float e) {
		logger.info("Started...");
		Long start = System.currentTimeMillis();
		Float[][] scores = matrix.getScores();

		Integer m = s1.length() + 1;
		Integer n = s2.length() + 1;

		Byte[] pointers = new Byte[m * n];
		
		// Initializes the boundaries of the traceback matrix to STOP.
		for (Integer i = 0, k = 0; i < m; i++, k += n) {
			pointers[k] = Directions.STOP;
		}
		for (Integer j = 1; j < n; j++) {
			pointers[j] = Directions.STOP;
		}

		Short[] sizesOfVerticalGaps = new Short[m * n];
		Short[] sizesOfHorizontalGaps = new Short[m * n];
		for (Integer i = 0, k = 0; i < m; i++, k += n) {
			for (Integer j = 0; j < n; j++) {
				sizesOfVerticalGaps[k + j] = sizesOfHorizontalGaps[k + j] = 1;
			}
		}

		Cell cell = SmithWatermanGotoh.construct(s1, s2, scores, o, e,
				pointers, sizesOfVerticalGaps, sizesOfHorizontalGaps);
		Alignment alignment = SmithWatermanGotoh.traceback(s1, s2, matrix,
				pointers, cell, sizesOfVerticalGaps, sizesOfHorizontalGaps);
		alignment.setOriginalSequence1(s1);
		alignment.setOriginalSequence2(s2);
		alignment.setMatrix(matrix);
		alignment.setOpen(o);
		alignment.setExtend(e);
		if (s1.getId() != null) {
			alignment.setName1(s1.getId());
		}
		if (s2.getId() != null) {
			alignment.setName2(s2.getId());
		}
                //String msg = "Finished in " + (System.currentTimeMillis() - start) + " milliseconds";
		//logger.info(msg);
		return alignment;
	}

	/**
	 * Constructs directions matrix for the traceback
	 * 
	 * @param s1
	 *            sequence #1
	 * @param s2
	 *            sequence #2
	 * @param matrix
	 *            scoring matrix
	 * @param o
	 *            open gap penalty
	 * @param e
	 *            extend gap penalty
	 * @return The cell where the traceback starts.
	 */
	private static Cell construct(Sequence s1, Sequence s2, Float[][] matrix,
			Float o, Float e, Byte[] pointers, Short[] sizesOfVerticalGaps,
			Short[] sizesOfHorizontalGaps) {
		//logger.info("Started...");
		Long start = System.currentTimeMillis();

		Character[] a1 = s1.toCharArray();
		Character[] a2 = s2.toCharArray();

		Integer m = s1.length() + 1;////////pr: eliminar .length()
		Integer n = s2.length() + 1;////////pr: eliminar .length()

		//Float f; // score of alignment x1...xi to y1...yi if xi aligns to yi
		Float[] g = new Float[n]; // score if xi aligns to a gap after yi
		//Float h; // score if yi aligns to a gap after xi
		Float[] v = new Float[n]; // best score of alignment x1...xi to
		// y1...yi
		//Float vDiagonal;

		g[0] = Float.NEGATIVE_INFINITY;
		//h = Float.NEGATIVE_INFINITY;
		v[0] = 0f;

		for (Integer j = 1; j < n; j++) {
			g[j] = Float.NEGATIVE_INFINITY;
			v[j] = new Float(0);
		}

		//Float similarityScore, g1, g2, h1, h2;

		Cell cell = new Cell();

		for (Integer i = 1, k = n; i < m; i++, k += n) {
			Float h = Float.NEGATIVE_INFINITY;
			Float vDiagonal = v[0];
			for (Integer j = 1, l = k + 1; j < n; j++, l++) {
				Float similarityScore = new Float(matrix[a1[i - 1]][a2[j - 1]]);

				// Fill the matrices
				Float f = new Float(vDiagonal + similarityScore);

				Float g1 = g[j] - e;
				Float g2 = v[j] - o;
				if (g1 > g2) {
					g[j] = g1;
					sizesOfVerticalGaps[l] = (short) (sizesOfVerticalGaps[l - n] + 1);
				} else {
					g[j] = g2;
				}

				
				Float h1 = h - e;
				Float h2 = v[j - 1] - o;
				if (h1 > h2) {
					h = h1;
					sizesOfHorizontalGaps[l] = (short) (sizesOfHorizontalGaps[l - 1] + 1);
				} else {
					h = h2;
				}

				vDiagonal = v[j];
				v[j] = maximum(f, g[j], h, new Float(0));

				// Determine the traceback direction
				if (v[j] == 0) {
					pointers[l] = Directions.STOP;
				} else if (v[j] == f) {
					pointers[l] = Directions.DIAGONAL;
				} else if (v[j] == g[j]) {
					pointers[l] = Directions.UP;
				} else {
					pointers[l] = Directions.LEFT;
				}

				// Set the traceback start at the current cell i, j and score
				if (v[j] > cell.getScore()) {
					cell.set(i, j, v[j]);
				}
				
			}
		}
                //String msg = "Finished in " + (System.currentTimeMillis() - start) + " milliseconds";
		//logger.info(msg);
		return cell;
	}

	/**
	 * Returns the alignment of two sequences based on the passed array of
	 * pointers
	 * 
	 * @param s1
	 *            sequence #1
	 * @param s2
	 *            sequence #2
	 * @param m
	 *            scoring matrix
	 * @param cell
	 *            The cell where the traceback starts.
	 * @return {@link Alignment}with the two aligned sequences and alignment
	 *         score.
	 * @see Cell
	 * @see Alignment
	 */
	private static Alignment traceback(Sequence s1, Sequence s2, Matrix m,
			Byte[] pointers, Cell cell, Short[] sizesOfVerticalGaps,
			Short[] sizesOfHorizontalGaps) {
		logger.info("Started...");
		Long start = System.currentTimeMillis();

//		Character[] a1 = s1.toCharArray();
//		Character[] a2 = s2.toCharArray();

		Float[][] scores = m.getScores();

//		Integer n = s2.length() + 1;

		Alignment alignment = new Alignment();
		alignment.setScore(cell.getScore());

//		Integer maxlen = s1.length() + s2.length(); // maximum length after the
		// aligned sequences

		Character[] reversed1 = new Character[s1.length() + s2.length()]; // reversed sequence #1
		Character[] reversed2 = new Character[s1.length() + s2.length()]; // reversed sequence #2
		Character[] reversed3 = new Character[s1.length() + s2.length()]; // reversed markup

		Integer len1 = 0; // length of sequence #1 after alignment
		Integer len2 = 0; // length of sequence #2 after alignment
		Integer len3 = 0; // length of the markup line

		Integer identity = 0; // count of identitcal pairs
		Integer similarity = 0; // count of similar pairs
		Integer gaps = 0; // count of gaps

//		Character c1, c2;

		Integer i = cell.getRow(); // traceback start row
		Integer j = cell.getCol(); // traceback start col
//		Integer k = i * n;
		Integer k = i * (s2.length() + 1);

		Boolean stillGoing = true; // traceback flag: true -> continue & false
		// -> stop

		while (stillGoing) {
			switch (pointers[k + j]) {
			case Directions.UP:
				for (Integer l = 0, len = new Integer(sizesOfVerticalGaps[k + j]); l < len; l++) {
					reversed1[len1++] = s1.toCharArray()[--i];
					reversed2[len2++] = Alignment.GAP;
					reversed3[len3++] = Markups.GAP;
					k -= (s2.length() + 1);
					gaps++;
				}
				break;
			case Directions.DIAGONAL:
				Character c1 = s1.toCharArray()[--i];
				Character c2 = s2.toCharArray()[--j];
				k -= (s2.length() + 1);
				reversed1[len1++] = c1;
				reversed2[len2++] = c2;
				if (c1 == c2) {
					reversed3[len3++] = Markups.IDENTITY;
					identity++;
					similarity++;
				} else if (scores[c1][c2] > 0) {
					reversed3[len3++] = Markups.SIMILARITY;
					similarity++;
				} else {
					reversed3[len3++] = Markups.MISMATCH;
				}
				break;
			case Directions.LEFT:
				for (Integer l = 0, len = new Integer(sizesOfHorizontalGaps[k + j]); l < len; l++) {
					reversed1[len1++] = Alignment.GAP;
					reversed2[len2++] = s2.toCharArray()[--j];
					reversed3[len3++] = Markups.GAP;
					gaps++;
				}
				break;
			case Directions.STOP:
				stillGoing = false;
			}
		}

		alignment.setSequence1(reverse(reversed1, len1));
		alignment.setStart1(i);
		alignment.setSequence2(reverse(reversed2, len2));
		alignment.setStart2(j);
		alignment.setMarkupLine(reverse(reversed3, len3));
		alignment.setIdentity(identity);
		alignment.setGaps(gaps);
		alignment.setSimilarity(similarity);

                //String msg = "Finished in " + (System.currentTimeMillis() - start) + " milliseconds";
		//logger.info(msg);
		return alignment;
	}

	/**
	 * Returns the maximum of 4 float numbers.
	 * 
	 * @param a
	 *            float #1
	 * @param b
	 *            float #2
	 * @param c
	 *            float #3
	 * @param d
	 *            float #4
	 * @return The maximum of a, b, c and d.
	 */
	private static Float maximum(Float a, Float b, Float c, Float d) {
		if (a > b) {
			if (a > c) {
				return a > d ? a : d;
			} else {
				return c > d ? c : d;
			}
		} else if (b > c) {
			return b > d ? b : d;
		} else {
			return c > d ? c : d;
		}
	}

	/**
	 * Reverses an array of chars
	 * 
	 * @param a
	 * @param len
	 * @return the input array of char reserved
	 */
	private static char[] reverse(Character[] a, Integer len) {
		char[] b = new char[len];
		for (Integer i = len - 1, j = 0; i >= 0; i--, j++) {
			b[j] = a[i];
		}
		return b;
	}
}
