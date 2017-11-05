package kaba.yucata.envoy;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import kaba.yucata.envoy.datalink.GamelistDbHelper;
import kaba.yucata.envoy.datalink.StateInfo;

public class GameListActivity extends AppCompatActivity {
    private static boolean DEBUG=BuildConfig.DEBUG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);

        final RecyclerView game_list = (RecyclerView) findViewById(R.id.rv_gamelist);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        game_list.setLayoutManager( layoutManager );
        final GameAdapter adapter = new GameAdapter(this);
        game_list.setAdapter( adapter );
    }

    class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {
        List<StateInfo.Game> games=null;
        private Throwable throwable=null;

        GameAdapter(Context context) {
            try {
                final GamelistDbHelper dbh = new GamelistDbHelper(context);
                games = StateInfo.Game.queryGames(dbh);
            } catch (Throwable t) {
                throwable=t;
                games=new ArrayList<>(0);
            }
        }

        @Override
        public GameViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            final View view = inflater.inflate(R.layout.game_list_item, parent, false);
            return new GameViewHolder( view );
        }

        @Override
        public void onBindViewHolder(GameViewHolder holder, int position) {
            if( throwable!=null )
                holder.bind( throwable );
            else
                holder.bind( games.get(position) );
        }

        @Override
        public int getItemCount() {
            if( throwable!=null )
                return 1;
            else
                return games.size();
        }

        class GameViewHolder extends RecyclerView.ViewHolder {
            TextView tvGameName=null;
            TextView tvSinceLM=null;
            TextView tvOpponents=null;
            View onMove=null;
            View nextOnMove=null;
            GameViewHolder(View item_view) {
                super(item_view);
                tvGameName = (TextView)item_view.findViewById(R.id.tv_game_name);
                tvSinceLM = (TextView)item_view.findViewById(R.id.tv_since_last_move);
                tvOpponents = (TextView)item_view.findViewById(R.id.tv_opponents);
                onMove = item_view.findViewById(R.id.on_move);
                nextOnMove = item_view.findViewById(R.id.next_on_move);
            }
            void bind(StateInfo.Game game) {
                tvGameName.setText(game.name);
                tvSinceLM.setText(sinceString(game.lastMoveOn.getTime()));
                tvOpponents.setText(StateInfo.Player.toString(game.players));
                if (game.isNextOnTurn) {
                    nextOnMove.setVisibility(View.VISIBLE);
                    onMove.setVisibility(View.INVISIBLE);
                } else {
                    nextOnMove.setVisibility(View.INVISIBLE);
                    if( game.isOnTurn )
                        onMove.setVisibility(View.VISIBLE);
                    else
                        onMove.setVisibility(View.INVISIBLE);
                }
            }
            void bind(Throwable t) {
                tvGameName.setText( t.getMessage() );
                tvOpponents.setText( t.toString() );
            }
            // todo: i18n
            private String sinceString(long then_millis) {
                final long diff_millis = System.currentTimeMillis() - then_millis;
                if(true&&DEBUG)
                    System.out.println(" now "+System.currentTimeMillis()+" - "+then_millis+" = "+diff_millis);
                if( diff_millis > 24*60*60*1000 )
                    return ""+(diff_millis/(24*60*60*1000))+"d";
                if( diff_millis > 60*60*1000 )
                    return ""+(diff_millis/(60*60*1000))+"h";
                if( diff_millis > 60*1000 )
                    return ""+(diff_millis/(60*1000))+"m";
                return "now";
            }
        }
    }
}
