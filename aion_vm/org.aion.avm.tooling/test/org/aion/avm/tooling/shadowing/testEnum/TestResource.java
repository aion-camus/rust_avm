package org.aion.avm.tooling.shadowing.testEnum;

import java.math.RoundingMode;
import org.aion.avm.tooling.abi.Callable;

public class TestResource {
    static TestEnum earth = TestEnum.EARTH;
    static TestEnum jupiter = TestEnum.JUPITER;
    static TestEnum mars = TestEnum.MARS;

    @Callable
    public static boolean testEnumAccess(){
        boolean ret = true;
        ret = ret && (earth == TestEnum.EARTH);
        ret = ret && (earth == TestEnum.valueOf("EARTH"));
        ret = ret && (TestEnum.EARTH.mass() == earth.mass());

        return ret;
    }

    @Callable
    public static boolean testEnumValues(){
        boolean ret = true;

        TestEnum[] es = TestEnum.values();
        ret = ret && (es[0] == TestEnum.MERCURY);
        ret = ret && (es[1] == TestEnum.VENUS);
        ret = ret && (es[2] == TestEnum.EARTH);
        ret = ret && (es[3] == TestEnum.MARS);
        ret = ret && (es[4] == TestEnum.JUPITER);
        ret = ret && (es[5] == TestEnum.SATURN);
        ret = ret && (es[6] == TestEnum.URANUS);
        ret = ret && (es[7] == TestEnum.NEPTUNE);

        return ret;
    }


    @Callable
    public static boolean testShadowJDKEnum(){
        boolean ret = true;
        ret = ret && (RoundingMode.HALF_UP == RoundingMode.valueOf("HALF_UP"));
        ret = ret && (RoundingMode.CEILING instanceof Object);

        RoundingMode[] es = (RoundingMode[]) RoundingMode.values();
        ret = ret && (es[0] == RoundingMode.UP);
        ret = ret && (es[1] == RoundingMode.DOWN);
        ret = ret && (es[2] == RoundingMode.CEILING);
        ret = ret && (es[3] == RoundingMode.FLOOR);
        ret = ret && (es[4] == RoundingMode.HALF_UP);
        ret = ret && (es[5] == RoundingMode.HALF_DOWN);
        ret = ret && (es[6] == RoundingMode.HALF_EVEN);
        ret = ret && (es[7] == RoundingMode.UNNECESSARY);

        return ret;
    }
}
