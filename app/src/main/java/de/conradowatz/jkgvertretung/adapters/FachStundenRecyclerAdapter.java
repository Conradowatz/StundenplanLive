package de.conradowatz.jkgvertretung.adapters;

import android.os.AsyncTask;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import de.conradowatz.jkgvertretung.R;
import de.conradowatz.jkgvertretung.fragments.FachStundenFragment;
import de.conradowatz.jkgvertretung.variables.Fach;

public class FachStundenRecyclerAdapter extends RecyclerView.Adapter<FachStundenRecyclerAdapter.ViewHolder> {

    public static final int TYPE_LEFT = 1;
    private static final int TYPE_TOP = 0;
    private static final int TYPE_NORMAL = 2;

    private Fach fach;
    private int state;
    private boolean[][] stunden;
    private boolean[][] stundenB; //nur bei state==IMMER genutzt
    private boolean[][] belegt;
    private Callback callback;

    public FachStundenRecyclerAdapter(int state, Fach fach, Callback callback) {

        this.state = state;
        this.fach = fach;
        updateData(null);
        this.callback = callback;
    }

    public void updateData(final Integer pos) {

        new AsyncTask<Boolean, Integer, Boolean>() {

            boolean[][] tmpStunden;
            boolean[][] tmpStundenB; //nur bei state==IMMER genutzt
            boolean[][] tmpBelegt;

            @Override
            protected Boolean doInBackground(Boolean... params) {

                switch (state) {
                    case FachStundenFragment.STATE_AWOCHE:
                        tmpStunden = fach.getAStunden();
                        tmpBelegt = fach.getBelegteAStunden();
                        break;
                    case FachStundenFragment.STATE_BWOCHE:
                        tmpStunden = fach.getBStunden();
                        tmpBelegt = fach.getBelegteBStunden();
                        break;
                    case FachStundenFragment.STATE_IMMER:
                        tmpStunden = fach.getAStunden();
                        tmpStundenB = fach.getBStunden();
                        tmpBelegt = fach.getBelegteStunden();
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {

                stunden = tmpStunden;
                stundenB = tmpStundenB;
                belegt = tmpBelegt;
                if (pos==null) notifyDataSetChanged();
                else notifyItemChanged(pos);
            }
        }.execute();

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        switch (viewType) {
            case TYPE_TOP:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_einzelstunde_label_top, parent, false);
                break;
            case TYPE_LEFT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_einzelstunde_label_left, parent, false);
                break;
            default:
                TYPE_NORMAL:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_einzelstunde, parent, false);
        }
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        int viewType = getItemViewType(holder.getAdapterPosition());
        if (viewType == TYPE_NORMAL) {
            final int[] i = getItemPosition(holder.getAdapterPosition());

            holder.wocheText.setText("");
            if ((state != FachStundenFragment.STATE_IMMER && stunden[i[0]][i[1]]) || (state == FachStundenFragment.STATE_IMMER && stunden[i[0]][i[1]] && stundenB[i[0]][i[1]])) {
                holder.backgroundView.setBackgroundResource(R.drawable.round_shape_yellow);
                holder.imageView.setImageResource(R.drawable.ic_done);
                holder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        callback.onRemoveStunde(i[0]+1, i[1], holder.getAdapterPosition());
                    }
                });
            } else if (belegt[i[0]][i[1]]) {
                holder.backgroundView.setBackgroundResource(R.drawable.round_shape_blue);
                holder.imageView.setImageResource(R.drawable.ic_clear);
                holder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        callback.onReplaceStunde(i[0]+1, i[1], holder.getAdapterPosition());
                    }
                });
            } else {
                holder.backgroundView.setBackgroundResource(R.drawable.round_shape_white);
                holder.imageView.setImageResource(0);
                holder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        callback.onNewStunde(i[0]+1, i[1], holder.getAdapterPosition());
                    }
                });
                if (state == FachStundenFragment.STATE_IMMER) {
                    if (stunden[i[0]][i[1]]) holder.wocheText.setText("A");
                    if (stundenB[i[0]][i[1]]) holder.wocheText.setText("B");
                }
            }

        } else if (viewType == TYPE_LEFT)

        {
            int stunde = (int) Math.floor(holder.getAdapterPosition() / 6)-1;
            if (stunde > -1) holder.textView.setText(String.valueOf(stunde));
            else holder.textView.setText("");
        } else if (viewType == TYPE_TOP)

        {
            int tag = holder.getAdapterPosition() % 6 - 1;
            String tagString = "";
            switch (tag) {
                case 0:
                    tagString = "Mo";
                    break;
                case 1:
                    tagString = "Di";
                    break;
                case 2:
                    tagString = "Mi";
                    break;
                case 3:
                    tagString = "Do";
                    break;
                case 4:
                    tagString = "Fr";
                    break;
            }
            holder.textView.setText(tagString);
        }

    }

    @Override
    public int getItemViewType(int position) {

        if (position % 6 == 0) return TYPE_LEFT;
        else if (position <= 5) return TYPE_TOP;
        return TYPE_NORMAL;
    }

    @Override
    public int getItemCount() {

        if (stunden==null) return 0;
        return 6 * 14;
    }

    private int[] getItemPosition(int position) {

        int[] i = new int[2];
        i[0] = (position % 6) - 1;
        i[1] = (int) Math.floor(position / 6) - 1;
        return i;

    }

    public interface Callback {

        void onNewStunde(int tag, int stunde, int pos);

        void onRemoveStunde(int tag, int stunde, int pos);

        void onReplaceStunde(int tag, int stunde, int pos);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        View backgroundView;
        TextView textView;
        TextView wocheText;

        public ViewHolder(View itemView, int viewType) {
            super(itemView);
            if (viewType != TYPE_NORMAL) textView = (TextView) itemView;
            else {
                imageView = (ImageView) itemView.findViewById(R.id.imageView);
                backgroundView = itemView.findViewById(R.id.backgroundView);
                wocheText = (TextView) itemView.findViewById(R.id.wocheText);
            }
        }
    }
}
