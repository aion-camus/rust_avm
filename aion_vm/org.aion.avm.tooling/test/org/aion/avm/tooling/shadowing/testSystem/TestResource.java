package org.aion.avm.tooling.shadowing.testSystem;

import org.aion.avm.tooling.abi.Callable;

public class TestResource {

    @Callable
    public static boolean testArrayCopy(){
        boolean ret = true;

        boolean[]   a = {true, false, true, false, true, false};
        byte[]      b = {1,2,3,4,5,6};
        char[]      c = {'a', 'b', 'c', 'd', 'e', 'f'};
        double[]    d = {1,2,3,4,5,6};
        float[]     e = {1,2,3,4,5,6};
        int[]       f = {1,2,3,4,5,6};
        long[]      g = {1,2,3,4,5,6};
        short[]     h = {1,2,3,4,5,6};
        String[]    i = {"a", "b", "c", "d", "e", "f"};
        String[][]  j = {{"a", "b", "c", "d", "e", "f"}, {"a", "b", "c", "d", "e", "f"},
                         {"a", "b", "c", "d", "e", "f"}, {"a", "b", "c", "d", "e", "f"},
                         {"a", "b", "c", "d", "e", "f"}, {"a", "b", "c", "d", "e", "f"}};

        boolean[]   aa = new boolean[6];
        byte[]      bb = new byte[6];
        char[]      cc = new char[6];
        double[]    dd = new double[6];
        float[]     ee = new float[6];
        int[]       ff = new int[6];
        long[]      gg = new long[6];
        short[]     hh = new short[6];
        String[]    ii = new String[6];
        String[][]  jj = new String[6][6];

        System.arraycopy(a, 0, aa, 0, 6);
        System.arraycopy(b, 0, bb, 0, 6);
        System.arraycopy(c, 0, cc, 0, 6);
        System.arraycopy(d, 0, dd, 0, 6);
        System.arraycopy(e, 0, ee, 0, 6);
        System.arraycopy(f, 0, ff, 0, 6);
        System.arraycopy(g, 0, gg, 0, 6);
        System.arraycopy(h, 0, hh, 0, 6);
        System.arraycopy(i, 0, ii, 0, 6);
        System.arraycopy(j, 0, jj, 0, 6);

        ret = ret && (a[2] == aa[2]);
        ret = ret && (b[3] == bb[3]);
        ret = ret && (c[3] == cc[3]);
        ret = ret && (d[3] == dd[3]);
        ret = ret && (e[3] == ee[3]);
        ret = ret && (f[3] == ff[3]);
        ret = ret && (g[3] == gg[3]);
        ret = ret && (h[3] == hh[3]);
        ret = ret && (i[3].equals(ii[3]));
        ret = ret && (j[3][3].equals(jj[3][3]));

        return ret;
    }

    @Callable
    public static boolean testArrayCopyNullPointerException(){
        int[] src = { 0, 1, 2, 3, 4, 5 };
        int[] dest = null;
        boolean didMatch = false;
        try {
            System.arraycopy(src, 1, dest, 0, 1);
        } catch (NullPointerException e){
            didMatch = true;
        }
        return didMatch;
    }

    @Callable
    public static boolean testArrayCopyIndexOutOfBoundsException(){
        int[] src = {0, 1, 2, 3, 4, 5};
        int[] dest = {10, 20, 30};

        boolean didMatch = false;
        try {
            System.arraycopy(src, 1, dest, 0, 5);
        } catch (IndexOutOfBoundsException e){
            didMatch = true;
        }
        return didMatch;
    }

    @Callable
    public static boolean testArrayCopyArrayStoreException(){
        int[] src = {0, 1, 2, 3, 4, 5};
        String[] dest = {"str1", "str2"};

        boolean didMatch = false;
        try {
            System.arraycopy(src, 1, dest, 0, 5);
        } catch (ArrayStoreException e){
            didMatch = true;
        }
        return didMatch;
    }
}
