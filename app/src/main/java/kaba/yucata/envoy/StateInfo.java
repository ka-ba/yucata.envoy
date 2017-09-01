package kaba.yucata.envoy;

/**
 * Created by kaba on 08/08/17.
 */

public class StateInfo {
    final private int gamesTotal;
    final private int gamesWaiting;
    final private int personalInvites;
    final private boolean error;
    final private String errorMessage;

    /** to be used in good time */
    public StateInfo(int total, int waiting, int invites) {
        gamesTotal=total;
        gamesWaiting=waiting;
        personalInvites=invites;
        error=false;
        errorMessage=null;
    }

    /** to be used in bad time */
    public StateInfo(String msg) {
        gamesTotal=-1;
        gamesWaiting=-1;
        personalInvites=-1;
        error=true;
        errorMessage=msg;
    }

    public int getGamesTotal() {
        return gamesTotal;
    }

    public int getGamesWaiting() {
        return gamesWaiting;
    }

    public int getPersonalInvites() {
        return personalInvites;
    }

    public boolean wasErronous() { return error; }

    public String getErrorMessage() {return errorMessage; }
}
