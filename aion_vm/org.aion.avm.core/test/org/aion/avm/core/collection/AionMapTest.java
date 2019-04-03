package org.aion.avm.core.collection;

import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;
import org.junit.Assert;
import org.junit.Test;


public class AionMapTest {
    /**
     * Creates an empty map, checks its size, and verifies we can't read or remove from it.
     */
    @Test
    public void emptyMapTest() {
        AionMap<Integer, Void> map = new AionMap<>();
        Assert.assertEquals(0, map.size());
        Assert.assertEquals(null, map.get(Integer.valueOf(4)));
        Assert.assertEquals(null, map.remove(Integer.valueOf(5)));
    }

    /**
     * Adds 100 elements, forcing growth, and then tries interacting with the map.
     */
    @Test
    public void addManyElements() {
        AionMap<Integer, String> map = new AionMap<>();
        for (int i = 0; i < 100; ++i) {
            map.put(i, "int_ " + i);
        }
        Assert.assertEquals(100, map.size());
        for (int i = 0; i < 100; ++i) {
            Assert.assertEquals("int_ " + i, map.get(i));
        }
        Assert.assertEquals(100, map.size());
        for (int i = 0; i < 100; ++i) {
            Assert.assertEquals("int_ " + i, map.remove(i));
        }
        Assert.assertEquals(0, map.size());
    }

    /**
     * Adds the same 20 elements, over and over, verifying that we only get the last version.
     */
    @Test
    public void addDuplicates() {
        AionMap<Integer, String> map = new AionMap<>();
        for (int i = 0; i < 10; ++i) {
            for (int j = 0; j < 20; ++j) {
                map.put(j, "int_ " + i);
            }
        }
        Assert.assertEquals(20, map.size());
        for (int j = 0; j < 20; ++j) {
            Assert.assertEquals("int_ 9", map.remove(j));
        }
        Assert.assertEquals(0, map.size());
    }

    @Test
    public void stressOperation(){
        AionMap<Integer, Integer> m = new AionMap<>();
        Integer res;

        m.clear();

        for (int i = 0; i < 10000; i++){
            m.put(Integer.valueOf(i), Integer.valueOf(i));
        }

        for (int i = 0; i < 10000; i++){
            res = m.get(Integer.valueOf(i));
            Assert.assertTrue(res.equals(Integer.valueOf(i)));
        }

        for (int i = 0; i < 10000; i++){
            res = m.remove(Integer.valueOf(i));
            Assert.assertTrue(res.equals(Integer.valueOf(i)));
        }

        m.clear();

        for (int i = 10000; i > 0; i--){
            m.put(Integer.valueOf(i), Integer.valueOf(i));
        }

        for (int i = 10000; i > 0; i--){
            res = m.get(Integer.valueOf(i));
            Assert.assertTrue(res.equals(Integer.valueOf(i)));
        }

        for (int i = 10000; i > 0; i--){
            res = m.remove(Integer.valueOf(i));
            Assert.assertTrue(res.equals(Integer.valueOf(i)));
        }

        m.clear();

        for (int i = 10000; i > 0; i--){
            m.put(Integer.valueOf(i), Integer.valueOf(i));
        }

        for (int i = 10000; i > 0; i--){
            res = m.get(Integer.valueOf(i));
            Assert.assertTrue(res.equals(Integer.valueOf(i)));
        }

        for (int i = 1; i <= 10000; i++){
            res = m.remove(Integer.valueOf(i));
            Assert.assertTrue(res.equals(Integer.valueOf(i)));
        }
    }

    @Test
    public void testBSet(){
        AionSet<Integer> s = new AionSet<>();
        Integer res;

        for (int i = 0; i < 10000; i++){
            s.add(Integer.valueOf(i));
        }

        for (int i = 0; i < 10000; i++){
            boolean b = s.contains(Integer.valueOf(i));
            Assert.assertTrue(b);
        }

        for (int i = 0; i < 10000; i++){
            boolean b = s.remove(Integer.valueOf(i));
            Assert.assertTrue(b);
        }

    }

}
