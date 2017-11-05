package kaba.yucata.envoy.datalink;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
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
    //    private static SimpleDateFormat DTFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    /** may be null on older versions */
    private static SimpleDateFormat DTFORMAT = null;
    { // older Java versions don't know X
        try {
            DTFORMAT = new SimpleDateFormat("y-M-d'T'H:m:s.SSSSSSSX");
        } catch(Throwable t) {
            DTFORMAT=null;
            System.out.println(t.toString());
            t.printStackTrace();
        }
    }

    /** to be used in good time */
    public StateInfo() {
        error=false;
        throwable =null;
    }

    /** @deprecated use zero-param version */
    public StateInfo(int total, int waiting, int invites) {
        this();
        gamesTotal=total;
        gamesWaiting=waiting;
        personalInvites=invites;
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
        final int onTurnGameI = json_d.getInt("NextGameOnTurn");  // easy if zero
        games=new TreeSet<>();
        final JSONArray gamesJ = json_d.getJSONArray("Games");
        for( int i=0; i<gamesJ.length(); i++ ) {
            if( gamesJ.isNull(i))
                continue;
            final JSONObject gameJ = gamesJ.getJSONObject(i);
            games.add( new Game(gameJ,onTurnGameI) );
        }
        gamesTotal=games.size();
        if(true&&DEBUG)
           System.out.println("read "+gamesTotal+" total games from json");
        gamesWaiting=0;
        myPlayerId=-1;
        if(onTurnGameI!=0) {
            onTurnGame = ((SortedSet<Game>)games).tailSet(new Game(onTurnGameI)).first();
            if( onTurnGame.id==onTurnGameI ) {
                myPlayerId = onTurnGame.playerOnTurn;
                for (Game game : games) {
                    if( game.figureOnTurn(myPlayerId) )
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

    public void storeGameInfo(Context context, long now_millis) {
        GamelistDbHelper dbh = new GamelistDbHelper(context);
        dbh.beginTransaction();
        try {
            dbh.clearGames();
            for( Game game : games )
                game.insert(dbh, now_millis);
            dbh.declareCommit();
        } finally {
            dbh.endTransaction();
        }
    }

    public static class Game implements Comparable<Game>
    {
        public final String name;
        public final int type;
        public final int id;
        public boolean isOnTurn=false;
        public final boolean isNextOnTurn;
        public final boolean isRanking;
        public final Date lastMoveOn;
        public final int playerOnTurn;
        public final Collection<Player> players;
        Game(JSONObject json, int game_on_turn)
                throws JSONException {
            name = json.getString("GameName");
            type = json.getInt("GameType");
            // HasReturn
            id = json.getInt("ID");
            isNextOnTurn = (id==game_on_turn);
            isRanking = json.getBoolean("IsRanking");
            // IsReturn
            // IsTournament
            // in java 8 't would be this:
            // lastMoveOn = OffsetDateTime.parse( json.getString("LastMoveOn") );
            try {
                Date d=null;
                if( DTFORMAT != null )
                    d = DTFORMAT.parse( json.getString("LastMoveOn") );
                lastMoveOn = d;
                if(true&&DEBUG)
                    System.out.println(" got date "+(d==null?"null":d.toString())+" by "+(DTFORMAT==null?"null":DTFORMAT.toString()) );
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
                players.add( new Player(playerJ,playerOnTurn) );
            }
        }
        /** dummy for search purposes only. */
        Game(int id) {
            this.id=id;
            name=null;
            type=-1;
            isNextOnTurn=false;
            isRanking=false;
            lastMoveOn=null;
            playerOnTurn=-1;
            players=null;
        }

        public Game(int id, String name, int type, boolean isRanking, Date lastMoveOn, int playerOnTurn, boolean isOnTurn, boolean isNextOnTurn, Collection<Player> players) {
            this.id=id;
            this.name=name;
            this.type=type;
            this.isRanking=isRanking;
            this.lastMoveOn=lastMoveOn;
            this.playerOnTurn=playerOnTurn;
            this.isNextOnTurn=isNextOnTurn;
            this.isOnTurn=isOnTurn;
            this.players=players;
        }

        boolean figureOnTurn(int myPlayerId) {
            if( playerOnTurn == myPlayerId )
                isOnTurn=true;
            return isOnTurn;
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

        public void insert(GamelistDbHelper dbh, long now_millis) {
            long key = dbh.insertGame(id,name,type,isRanking,lastMoveOn,playerOnTurn,isOnTurn,isNextOnTurn,now_millis);
            for( Player player : players )
                player.insert(dbh, key);
        }

        public static List<Game> queryGames(GamelistDbHelper dbh ) {
            Cursor cursor = dbh.queryGames();
            List<Game> games = new ArrayList<>( cursor.getCount() );
            for( cursor.moveToFirst(); ! cursor.isAfterLast(); cursor.moveToNext() ) {
                games.add( dbh.createGame(cursor) );
            }
            return games;
        }
    }

    public static class Player implements Comparable<Player>
    {
        public final boolean isOnVacation;
        public final boolean isOnTurn;
        public final String login;
        public final int order;
        public final int id;
        public final String rank;
        Player(JSONObject json, int playerOnTurn)
                throws JSONException {
            isOnVacation = json.getBoolean("IsOnVacation");
            login = json.getString("Login");
            order = json.getInt("Order");
            id = json.getInt("PlayerID");
            rank = json.getString("Rank");
            isOnTurn = (id==playerOnTurn);
            // VacationEnd
            // VacationReason
            // VacationStart
        }
        Player(int id, String login,String rank,int order,boolean isOnTurn,boolean isOnVacation) {
            this.id=id;
            this.login=login;
            this.rank=rank;
            this.order=order;
            this.isOnTurn=isOnTurn;
            this.isOnVacation=isOnVacation;
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

        public void insert(GamelistDbHelper dbh, long game_key) {
            dbh.insertPlayer(game_key,isOnVacation,login,order,id,rank,isOnTurn);
        }

    public static List<Player> queryPlayers(GamelistDbHelper dbh, int gamekey ) {
        Cursor cursor = dbh.queryPlayers(gamekey);
        List<Player> players = new ArrayList<>( cursor.getCount() );
        for( cursor.moveToFirst(); ! cursor.isAfterLast(); cursor.moveToNext() ) {
            players.add( dbh.createPlayer(cursor) );
        }
        return players;
    }

        public static String toString(Collection<Player> players) {
            final StringBuffer buffer = new StringBuffer();
            boolean sep=false;
            for( Player p : players ) {
                if( sep )
                    buffer.append( ", " );
                buffer.append( p.login );
                sep=true;
            }
            return buffer.toString();
        }
    }
}
