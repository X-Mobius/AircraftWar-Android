package edu.hitsz;

import android.content.Intent;
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

        // DAO 与适配器在 onCreate 初始化，列表数据在 onResume 刷新。
        scoreDao = new ScoreDaoImpl(this);
        ListView rankListView = findViewById(R.id.lv_rank);
        emptyView = findViewById(R.id.tv_rank_empty);

        adapter = new RankListAdapter(this, scoreDao, scoreDao.getAllRecords());
        rankListView.setAdapter(adapter);
        refreshEmptyView();
        findViewById(R.id.btn_rank_close).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次返回页面都刷新最新排行，保证跨页面操作后的数据一致。
        List<ScoreRecord> latestRecords = scoreDao.getAllRecords();
        adapter.replaceData(latestRecords);
        refreshEmptyView();
    }

    private void refreshEmptyView() {
        // 空列表时显示提示文本，行为与旧版排行榜一致。
        if (adapter.getCount() == 0) {
            emptyView.setText(R.string.rank_empty);
        } else {
            emptyView.setText("");
        }
    }
}
