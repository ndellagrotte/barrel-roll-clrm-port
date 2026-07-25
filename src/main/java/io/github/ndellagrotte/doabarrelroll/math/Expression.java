package io.github.ndellagrotte.doabarrelroll.math;

import java.util.Map;

public interface Expression {
    double eval(Map<String, Double> vars);
}
