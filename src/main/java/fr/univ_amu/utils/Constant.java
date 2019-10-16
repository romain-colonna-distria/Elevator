package fr.univ_amu.utils;

public class Constant {
    public static final short FLOOR_MAX = 4;
    public static final short FLOOR_MIN = 0;
    public static final short NB_FLOORS = (short) (Math.abs(FLOOR_MIN) + Math.abs(FLOOR_MAX) + 1); //marche pas si floor min et max negative
    public static final int FLOOR_SIZE = 129;

}
