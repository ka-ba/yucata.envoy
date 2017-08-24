package kaba.yucata.envoy;

/**
 * Created by kaba on 08/08/17.
 */

public class StateInfo {
    final private int gamesTotal;
    final private int gamesWaiting;

    public StateInfo(int total, int waiting) {
        gamesTotal=total;
        gamesWaiting=waiting;
    }

    public int getGamesTotal() {
        return gamesTotal;
    }

    public int getGamesWaiting() {
        return gamesWaiting;
    }
}
