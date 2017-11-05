package kaba.yucata.envoy.datalink;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.provider.BaseColumns;

import java.util.Date;

/**
 * Created by kaba on 25/10/17.
 */

public class GamelistDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "gamelist.db";
    private static final int DB_VERSION = 5;
    /** for use in transactions */
    private SQLiteDatabase writableDatabase=null, readableDatabase=null;
    public GamelistDbHelper(Context context) {
        super(context,DB_NAME,null,DB_VERSION);
        readableDatabase = getReadableDatabase();
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            db.setForeignKeyConstraintsEnabled(true);  // API16
        }
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                "CREATE TABLE "+ GamelistEntry.TABLE_NAME+" ("
                + GamelistEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"
                + GamelistEntry.COLUMN_GAME_NAME+" TEXT NOT NULL,"
                + GamelistEntry.COLUMN_GAME_ID+" INTEGER NOT NULL,"
                + GamelistEntry.COLUMN_GAME_TYPE+" INTEGER NOT NULL,"
                + GamelistEntry.COLUMN_GAME_RANKING+" INTEGER NOT NULL,"  // means boolean
                + GamelistEntry.COLUMN_LAST_MOVE+" INTEGER,"
                + GamelistEntry.COLUMN_PLAYER_ON_TURN+" INTEGER NOT NULL,"  // FIXME: already marked in other table
                + GamelistEntry.COLUMN_IS_ON_TURN+" INTEGER NOT NULL,"  // means boolean
                + GamelistEntry.COLUMN_IS_NEXT_ON_TURN+" INTEGER NOT NULL,"  // means boolean
                + GamelistEntry.COLUMN_STAMP+" TIMESTAMP NOT NULL);"
        );
        sqLiteDatabase.execSQL(
                "CREATE TABLE "+ GameplayerEntry.TABLE_NAME+" ("
                        + GameplayerEntry.COLUMN_GAME_ID+" INTEGER NOT NULL,"
                        + GameplayerEntry.COLUMN_PLAYER_LOGIN+" TEXT NOT NULL,"
                        + GameplayerEntry.COLUMN_PLAYER_ID+" INTEGER NOT NULL,"
                        + GameplayerEntry.COLUMN_PLAYER_RANK+" TEXT NOT NULL,"
                        + GameplayerEntry.COLUMN_PLAYER_ORDER+" INTEGER NOT NULL,"
                        + GameplayerEntry.COLUMN_IS_ON_TURN +" INTEGER NOT NULL,"
                        + GameplayerEntry.COLUMN_IS_ON_VACATION+" INTEGER NOT NULL,"
                        +"FOREIGN KEY("+ GameplayerEntry.COLUMN_GAME_ID+") REFERENCES "+ GamelistEntry.TABLE_NAME+"("+ GamelistEntry._ID+"));"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(
                "DROP TABLE IF EXISTS "+ GameplayerEntry.TABLE_NAME );
        sqLiteDatabase.execSQL(
                "DROP TABLE IF EXISTS "+ GamelistEntry.TABLE_NAME );
        onCreate(sqLiteDatabase);
    }

    public void beginTransaction() {
        writableDatabase = getWritableDatabase();
        writableDatabase.beginTransaction();
    }

    public void endTransaction() {
        if( ! writableDatabase.inTransaction() )
            throw new IllegalStateException("not in transaction");
        writableDatabase.endTransaction();
        writableDatabase = null;
    }

    public void declareCommit() {
        if( ! writableDatabase.inTransaction() )
            throw new IllegalStateException("not in transaction");
        writableDatabase.setTransactionSuccessful();
    }

    public void clearGames() {
        if( ! writableDatabase.inTransaction() )
            throw new IllegalStateException("not in transaction");
//        writableDatabase.execSQL("DELETE FROM "+GamelistContract.GameplayerEntry.TABLE_NAME);
//        writableDatabase.execSQL("DELETE FROM "+GamelistContract.GamelistEntry.TABLE_NAME);
        writableDatabase.delete(GameplayerEntry.TABLE_NAME,null,null);
        writableDatabase.delete(GamelistEntry.TABLE_NAME,null,null);
    }

    public long insertGame(int id, String name, int type, boolean isRanking, Date lastMoveOn,
                           int playerOnTurn, boolean isOnTurn, boolean isNextOnTurn, long now_millis) {
        if( ! writableDatabase.inTransaction() )
            throw new IllegalStateException("not in transaction");
        final ContentValues values = new ContentValues(7);
        values.put( GamelistEntry.COLUMN_GAME_NAME, name );
        values.put( GamelistEntry.COLUMN_GAME_ID, id );
        values.put( GamelistEntry.COLUMN_GAME_TYPE, type );
        values.put( GamelistEntry.COLUMN_GAME_RANKING, isRanking );
        values.put( GamelistEntry.COLUMN_LAST_MOVE,
                (lastMoveOn==null?null:lastMoveOn.getTime()) );
        values.put( GamelistEntry.COLUMN_PLAYER_ON_TURN, playerOnTurn );
        values.put( GamelistEntry.COLUMN_IS_ON_TURN, isOnTurn );
        values.put( GamelistEntry.COLUMN_IS_NEXT_ON_TURN, isNextOnTurn );
        values.put( GamelistEntry.COLUMN_STAMP, now_millis );
        // assume that rowid can be taken for primary key (see SQLite docs):
        return writableDatabase.insert(GamelistEntry.TABLE_NAME, null, values);
    }

    private boolean getCursorBool(Cursor cursor,int i) {
        return ((cursor.getInt(i))>0);
    }

    public StateInfo.Game createGame(Cursor cursor) {
        final int key = cursor.getInt(cursor.getColumnIndex(GamelistEntry._ID));
        return new StateInfo.Game(
                cursor.getInt(cursor.getColumnIndex(GamelistEntry.COLUMN_GAME_ID)),
                cursor.getString(cursor.getColumnIndex(GamelistEntry.COLUMN_GAME_NAME)),
                cursor.getInt(cursor.getColumnIndex(GamelistEntry.COLUMN_GAME_TYPE)),
                getCursorBool(cursor, cursor.getColumnIndex(GamelistEntry.COLUMN_GAME_RANKING)),
                new Date( cursor.getLong(cursor.getColumnIndex(GamelistEntry.COLUMN_LAST_MOVE)) ),
                cursor.getInt(cursor.getColumnIndex(GamelistEntry.COLUMN_PLAYER_ON_TURN)),
                getCursorBool(cursor, cursor.getColumnIndex(GamelistEntry.COLUMN_IS_ON_TURN)),
                getCursorBool(cursor, cursor.getColumnIndex(GamelistEntry.COLUMN_IS_NEXT_ON_TURN)),
                StateInfo.Player.queryPlayers(this,key));
    }

    public void insertPlayer(long game_key, boolean isOnVacation, String login, int order, int id,
                             String rank, boolean isOnTurn) {
        if( ! writableDatabase.inTransaction() )
            throw new IllegalStateException("not in transaction");
        final ContentValues values = new ContentValues(7);
        values.put( GameplayerEntry.COLUMN_GAME_ID, game_key );
        values.put( GameplayerEntry.COLUMN_PLAYER_LOGIN, login );
        values.put( GameplayerEntry.COLUMN_PLAYER_ID, id );
        values.put( GameplayerEntry.COLUMN_PLAYER_RANK, rank );
        values.put( GameplayerEntry.COLUMN_PLAYER_ORDER, order );
        values.put( GameplayerEntry.COLUMN_IS_ON_TURN, isOnTurn );
        values.put( GameplayerEntry.COLUMN_IS_ON_VACATION, isOnVacation );
        writableDatabase.insert(GameplayerEntry.TABLE_NAME, null, values);
    }

    public StateInfo.Player createPlayer(Cursor cursor) {
        return new StateInfo.Player(
                cursor.getInt(cursor.getColumnIndex(GameplayerEntry.COLUMN_PLAYER_ID)),
                cursor.getString(cursor.getColumnIndex(GameplayerEntry.COLUMN_PLAYER_LOGIN)),
                cursor.getString(cursor.getColumnIndex(GameplayerEntry.COLUMN_PLAYER_RANK)),
                cursor.getInt(cursor.getColumnIndex(GameplayerEntry.COLUMN_PLAYER_ORDER)),
                getCursorBool(cursor, cursor.getColumnIndex(GameplayerEntry.COLUMN_IS_ON_TURN)),
                getCursorBool(cursor, cursor.getColumnIndex(GameplayerEntry.COLUMN_IS_ON_VACATION)) );
    }

    // todo: use precompiled sql select
    public Cursor queryGames() {
        return readableDatabase.query(
                GamelistEntry.TABLE_NAME,
                null, null, null, null, null,
                GamelistEntry.COLUMN_IS_NEXT_ON_TURN+" DESC,"+GamelistEntry.COLUMN_IS_ON_TURN+" DESC,"+GamelistEntry.COLUMN_GAME_ID
        );
    }

    // todo: use precompiled sql select
    public Cursor queryPlayers(int gamekey) {
        return readableDatabase.query(
                true,
                GameplayerEntry.TABLE_NAME,null,
                GameplayerEntry.COLUMN_GAME_ID+"="+gamekey,null, null, null,
                GameplayerEntry.COLUMN_PLAYER_ORDER,null
        );
    }

    public static final class GamelistEntry implements BaseColumns {
        public static final String TABLE_NAME = "gamelist";
        public static final String COLUMN_GAME_NAME = "gamename";
        public static final String COLUMN_GAME_ID = "gameid";
        public static final String COLUMN_GAME_TYPE = "gametype";
        public static final String COLUMN_GAME_RANKING = "isranking";
        public static final String COLUMN_LAST_MOVE = "lastmove";
        public static final String COLUMN_PLAYER_ON_TURN = "playeronturn";
        public static final String COLUMN_IS_ON_TURN = "isonturn";
        public static final String COLUMN_IS_NEXT_ON_TURN = "isnextonturn";
        public static final String COLUMN_STAMP = "timestamp";
    }

    public static final class GameplayerEntry implements BaseColumns {
        public static final String TABLE_NAME = "gameplayers";
        public static final String COLUMN_GAME_ID = "game_id";
        public static final String COLUMN_PLAYER_LOGIN = "playerlogin";
        public static final String COLUMN_PLAYER_ID = "playerid";
        public static final String COLUMN_PLAYER_RANK = "playerrank";
        public static final String COLUMN_PLAYER_ORDER = "playerorder";
        public static final String COLUMN_IS_ON_TURN = "isonturn";
        public static final String COLUMN_IS_ON_VACATION = "isonvacation";
    }
}
