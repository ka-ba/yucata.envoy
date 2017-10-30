package kaba.yucata.envoy.datalink;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import kaba.yucata.envoy.BuildConfig;

/**
 * Created by kaba on 08/08/17.
 */

public class StateInfo {
    private static boolean DEBUG=BuildConfig.DEBUG;
    private Collection<Game> games=null;
    private Game onTurnGame=null;
    private int gamesTotal=-1;
    private int gamesWaiting=-1;
    private int personalInvites=-1;
    private int myPlayerId=-1;
    final private boolean error;
    final private Throwable throwable;
    private ServerAbstraction.SessionAbstraction session=null;

    /** to be used in good time */
    public StateInfo() {
        error=false;
        throwable =null;
    }

    /** @deprecated use zero-param version */
    public StateInfo(int total, int waiting, int invites) {
        gamesTotal=total;
        gamesWaiting=waiting;
        personalInvites=invites;
        error=false;
        throwable =null;
    }

    /** to be used in bad time */
    public StateInfo(Throwable t) {
        gamesTotal=-1;
        gamesWaiting=-1;
        personalInvites=-1;
        error=true;
        throwable =t;
    }

    public int addGamesInfo(/*YucataSession y_session,*/ String json_str) throws JSONException {
        final JSONObject json = new JSONObject(json_str);
        final JSONObject json_d = json.getJSONObject("d");
        final int totalGames = json_d.getInt("TotalGames");
        if(true&&DEBUG)
            System.out.println("read "+totalGames+" total games indication from json");
        games=new TreeSet<>();
        final JSONArray gamesJ = json_d.getJSONArray("Games");
        for( int i=0; i<gamesJ.length(); i++ ) {
            if( gamesJ.isNull(i))
                continue;
            final JSONObject gameJ = gamesJ.getJSONObject(i);
            games.add( new Game(gameJ) );
        }
        gamesTotal=games.size();
        if(true&&DEBUG)
           System.out.println("read "+gamesTotal+" total games from json");
        final int onTurnGameI = json_d.getInt("NextGameOnTurn");  // easy if zero
        gamesWaiting=0;
        myPlayerId=-1;
        if(onTurnGameI!=0) {
            onTurnGame = ((SortedSet<Game>)games).tailSet(new Game(onTurnGameI)).first();
            if( onTurnGame.id==onTurnGameI ) {
                myPlayerId = onTurnGame.playerOnTurn;
                for (Game game : games) {
                    if( game.playerOnTurn == myPlayerId )
                        gamesWaiting++;
                }
            } else {  // onTurnGame not (really) found
                onTurnGame = null;
                if(true&&DEBUG&&(onTurnGameI>-1))
                    System.out.println("WARNING: cannot find onTurnGame "+onTurnGameI);
            }
        }
        return myPlayerId;

        // FIXME: invitation count ?
//        if(true&&DEBUG)
//            System.out.println("received games info: "+totalGames+" total, "+onTurnCount+" on turn");
//        return new StateInfo(totalGames,onTurnCount,-1);
    }

    public boolean hasGamesInfo() { return (gamesTotal != -1); }

    public int getGamesTotal() {
        return gamesTotal;
    }

    public int getGamesWaiting() {
        return gamesWaiting;
    }

    public int getPersonalInvites() {
        return personalInvites;
    }

    public boolean deductedPlayerId() { return (myPlayerId != -1) ; }

    public int getMyPlayerId() { return myPlayerId; }

    public boolean wasErronous() { return error; }

    public Throwable getThrowable() {return throwable; }

    /** @deprecated session still needed in StateInfo? */
    public void setSession(ServerAbstraction.SessionAbstraction session) {
        this.session = session;
    }

    /** @deprecated session still needed in StateInfo? */
    public ServerAbstraction.SessionAbstraction getSession() {
        return session;
    }

    /** @deprecated session still needed in StateInfo? */
    public boolean hasSession() {
        return session!=null;
    }

    private static SimpleDateFormat DTFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    public class Game implements Comparable<Game>
    {
        public final String name;
        public final int type;
        public final int id;
        public final boolean isRanking;
        public final Date lastMoveOn;
        public final int playerOnTurn;
        public final Collection<Player> players;
        Game(JSONObject json )
                throws JSONException {
            name = json.getString("GameName");
            type = json.getInt("GameType");
            // HasReturn
            id = json.getInt("ID");
            isRanking = json.getBoolean("IsRanking");
            // IsReturn
            // IsTournament
            // in java 8 't would be this:
            // lastMoveOn = OffsetDateTime.parse( json.getString("LastMoveOn") );
            try {
                lastMoveOn = DTFORMAT.parse( json.getString("LastMoveOn") );
            } catch (ParseException e) {
                throw new JSONException("cannot parse date in LastMoveOn: "+e.getMessage());
            }
            // MayDelete
            // NumPlayers
            playerOnTurn = json.getInt("PlayerOnTurn");
            players = new TreeSet<>();
            final JSONArray playersJ = json.getJSONArray("Players");
            for( int i=0; i<playersJ.length(); i++ ) {
                if( playersJ.isNull(i))
                    continue;
                final JSONObject playerJ = playersJ.getJSONObject(i);
                players.add( new Player(playerJ) );
            }
        }
        /** dummy for search purposes only. */
        Game(int id) {
            this.id=id;
            name=null;
            type=-1;
            isRanking=false;
            lastMoveOn=null;
            playerOnTurn=-1;
            players=null;
        }
        @Override
        public int compareTo(@NonNull Game other) {
            if( this.id < other.id )
                return -1;
            if( this.id > other.id )
                return 1;
            return 0;
        }
        @Override
        public boolean equals(Object obj) {
            if( ! (obj instanceof  Game) )
                return false;
            final Game other = (Game)obj;
            return ( this.id==other.id );
        }
    }

    public class Player implements Comparable<Player>
    {
        public final boolean isOnVacation;
        public final String login;
        public final int order;
        public final int id;
        public final String rank;
        Player(JSONObject json)
                throws JSONException {
            isOnVacation = json.getBoolean("IsOnVacation");
            login = json.getString("Login");
            order = json.getInt("Order");
            id = json.getInt("PlayerID");
            rank = json.getString("Rank");
            // VacationEnd
            // VacationReason
            // VacationStart
        }
        @Override
        public int compareTo(@NonNull Player other) {
            if( this.order < other.order )
                return -1;
            if( this.order > other.order )
                return 1;
            if( this.id < other.id )
                return -1;
            if( this.id > other.id )
                return 1;
            return 0;
        }
        @Override
        public boolean equals(Object obj) {
            if( ! (obj instanceof  Player) )
                return false;
            final Player other = (Player)obj;
            return ( (this.order==other.order) && (this.id==other.id) );
        }
    }
}
