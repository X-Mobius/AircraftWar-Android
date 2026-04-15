package edu.hitsz.rank;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import edu.hitsz.R;

public class RankListAdapter extends BaseAdapter {

    private final LayoutInflater inflater;
    private final ScoreDao scoreDao;
    private final List<ScoreRecord> records = new ArrayList<>();

    public RankListAdapter(Context context, ScoreDao scoreDao, List<ScoreRecord> initialRecords) {
        this.inflater = LayoutInflater.from(context);
        this.scoreDao = scoreDao;
        if (initialRecords != null) {
            records.addAll(initialRecords);
        }
    }

    public void replaceData(List<ScoreRecord> newRecords) {
        // 当前记录量下采用全量替换刷新，逻辑更直观。
        records.clear();
        if (newRecords != null) {
            records.addAll(newRecords);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return records.size();
    }

    @Override
    public Object getItem(int position) {
        return records.get(position);
    }

    @Override
    public long getItemId(int position) {
        return records.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        // 使用 ViewHolder 模式复用行视图，减少 findViewById 开销。
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_rank_row, parent, false);
            holder = new ViewHolder();
            holder.rankNo = convertView.findViewById(R.id.tv_rank_no);
            holder.playerName = convertView.findViewById(R.id.tv_rank_name);
            holder.score = convertView.findViewById(R.id.tv_rank_score);
            holder.time = convertView.findViewById(R.id.tv_rank_time);
            holder.deleteBtn = convertView.findViewById(R.id.btn_rank_delete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ScoreRecord record = records.get(position);
        holder.rankNo.setText(String.valueOf(position + 1));
        holder.playerName.setText(record.getPlayerName());
        holder.score.setText(inflater.getContext().getString(R.string.rank_score_prefix, record.getScore()));
        holder.time.setText(record.getTime());
        holder.deleteBtn.setOnClickListener(v -> {
            // 删除时同步数据库与内存列表，避免界面与数据源不一致。
            scoreDao.deleteRecordById(record.getId());
            records.remove(record);
            notifyDataSetChanged();
            Toast.makeText(inflater.getContext(), R.string.rank_deleted, Toast.LENGTH_SHORT).show();
        });

        return convertView;
    }

    private static class ViewHolder {
        TextView rankNo;
        TextView playerName;
        TextView score;
        TextView time;
        Button deleteBtn;
    }
}
