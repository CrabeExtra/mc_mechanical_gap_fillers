package mods.mechanicalgapfillers.utility.energy;

public class Joules {

    public static double feToJ(int fe) {
        return fe * 2.5;
    }

    public static double JToFe(int j) {
        return j / 2.5;
    }
}
