package kaba.yucata.envoy;

/**
 * Created by kaba on 08/08/17.
 */

public class StateInfo {
    final private int gamesTotal;
    final private int gamesWaiting;
    final private boolean error;

    /** to be used in good time */
    public StateInfo(int total, int waiting) {
        gamesTotal=total;
        gamesWaiting=waiting;
        error=false;
    }

    /** to be used in bad time */
    public StateInfo() {
        gamesTotal=-1;
        gamesWaiting=-1;
        error=true;
    }

    public int getGamesTotal() {
        return gamesTotal;
    }

    public int getGamesWaiting() {
        return gamesWaiting;
    }

    public boolean wasErronous() { return error; }
}
