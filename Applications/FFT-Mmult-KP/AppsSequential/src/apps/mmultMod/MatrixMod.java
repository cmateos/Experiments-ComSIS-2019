package apps.mmultMod;

/* $Id$ */

//
// Class MatrixMod
//
// recursive part of quad tree
//
final class MatrixMod{
//    MatrixMod _00, _01, _10, _11;

	private float[][] value;

    int size;
	
    public MatrixMod(int task, int rec, int loop, float dbl) {
        //super(task, rec, loop, dbl); // construct LeafMod
        this.size = loop*4*(task+rec);
        value = new float[size][size];
        for (int i = 0; i < loop; i++) {
            for (int j = 0; j < loop; j++) {
                value[i][j] = dbl;
            }
        }
        if (task + rec <= 0)
        {
            return;
        }

//        if (task > 0) {
//            task--;
//        } 
//        else {
//            rec--;
//        }

//        _00 = new MatrixMod(task, rec, loop, dbl);
//        _01 = new MatrixMod(task, rec, loop, dbl);
//        _10 = new MatrixMod(task, rec, loop, dbl);
//        _11 = new MatrixMod(task, rec, loop, dbl);
    }


    void multiplyStride2(MatrixMod a, MatrixMod b) {
        for (int j = 0; j < size; j += 2) {
            for (int i = 0; i < size; i += 2) {

                float[] a0 = a.value[i];
                float[] a1 = a.value[i + 1];

                float s00 = 0;
                float s01 = 0;
                float s10 = 0;
                float s11 = 0;

                for (int k = 0; k < size; k += 2) {

                    float[] b0 = b.value[k];

                    s00 += a0[k] * b0[j];
                    s10 += a1[k] * b0[j];
                    s01 += a0[k] * b0[j + 1];
                    s11 += a1[k] * b0[j + 1];

                    float[] b1 = b.value[k + 1];

                    s00 += a0[k + 1] * b1[j];
                    s10 += a1[k + 1] * b1[j];
                    s01 += a0[k + 1] * b1[j + 1];
                    s11 += a1[k + 1] * b1[j + 1];
                }

                value[i][j] = s00;
                value[i][j + 1] = s01;
                value[i + 1][j] = s10;
                value[i + 1][j + 1] = s11;
            }
        }
    }
}
