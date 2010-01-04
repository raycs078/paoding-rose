package net.paoding.rose.mock.controllers.resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.paoding.rose.web.annotation.Param;

public class CollectionController {

    @SuppressWarnings("unchecked")
    public Class[] list(@Param(value = "int") List<Integer> ints, //
            @Param(value = "integer") ArrayList<Integer> integers, //
            @Param("name") Collection<String> names, //
            @Param(value = "bool") LinkedList<Boolean> bools) {
        ints.size();
        integers.size();
        names.size();
        bools.size();
        return new Class[] { ints.get(0).getClass(), integers.get(0).getClass(),
                names.iterator().next().getClass(), bools.get(0).getClass() };
    }

    @SuppressWarnings("unchecked")
    public Class[] set(@Param(value = "int") Set<Integer> ints, //
            @Param(value = "integer") HashSet<Integer> integers, //
            @Param("name") Set<?> names, //
            @Param(value = "bool") TreeSet<Boolean> bools) {
        return new Class[] { ints.iterator().next().getClass(),
                integers.iterator().next().getClass(), names.iterator().next().getClass(),
                bools.iterator().next().getClass() };
    }

    @SuppressWarnings("unchecked")
    public Class[] map(@Param("ss") Map<String, String> string2string,
            @Param(value = "is") Map<Integer, String> int2string,
            @Param(value = "sf") Map<String, Float> string2float,
            @Param(value = "ib") Map<Integer, Boolean> int2bool) {
        return new Class[] {
                string2string.keySet().iterator().next().getClass(),
                string2string.values().iterator().next().getClass(),
                //
                int2string.keySet().iterator().next().getClass(),
                int2string.values().iterator().next().getClass(),
                //
                string2float.keySet().iterator().next().getClass(),
                string2float.values().iterator().next().getClass(),
                //
                int2bool.keySet().iterator().next().getClass(),
                int2bool.values().iterator().next().getClass(), };
    }
}
