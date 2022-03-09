package de.yard.threed.services.util;

import java.util.ArrayList;
import java.util.List;

public class Util {

    public static <E> List<E> buildList(Iterable<E> iter) {
        List<E> list = new ArrayList<>();
        iter.forEach(list::add);
        return list;
    }
}
