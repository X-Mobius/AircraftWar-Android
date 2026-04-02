package edu.hitsz;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import edu.hitsz.rank.RankListAdapter;
import edu.hitsz.rank.ScoreDao;
import edu.hitsz.rank.ScoreDaoImpl;
import edu.hitsz.rank.ScoreRecord;

public class RankActivity extends AppCompatActivity {

    private ScoreDao scoreDao;
    private RankListAdapter adapter;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank);

        scoreDao = new ScoreDaoImpl(this);
        ListView rankListView = findViewById(R.id.lv_rank);
        emptyView = findViewById(R.id.tv_rank_empty);

        adapter = new RankListAdapter(this, scoreDao, scoreDao.getAllRecords());
        rankListView.setAdapter(adapter);
        refreshEmptyView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<ScoreRecord> latestRecords = scoreDao.getAllRecords();
        adapter.replaceData(latestRecords);
        refreshEmptyView();
    }

    private void refreshEmptyView() {
        if (adapter.getCount() == 0) {
            emptyView.setText(R.string.rank_empty);
        } else {
            emptyView.setText("");
        }
    }
}
