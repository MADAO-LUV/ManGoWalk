package com.example.mangowalking;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.WalkPath;
import com.example.mangowalking.adapter.BusSegmentListAdapter;
import com.example.mangowalking.adapter.WalkSegmentListAdapter;
import com.example.mangowalking.databinding.ActivityRouteDetailBinding;
import com.example.mangowalking.utils.MapUtil;
import com.example.mangowalking.utils.SchemeBusStep;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

public class RouteDetailActivity extends AppCompatActivity {

    private ActivityRouteDetailBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityRouteDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initView();
    }


    /**
     * 初始化视图
     */
    private void initView() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        switch (intent.getIntExtra("type", 0)) {
            case 0://步行
                walkDetail(intent);
                break;
            case 1://
                busDetail(intent);
                break;
            default:
                break;
        }
    }

    /**
     * 步行详情
     * @param intent
     */
    private void walkDetail(Intent intent) {
        binding.toolbar.setTitle("步行路线规划");
        WalkPath walkPath = intent.getParcelableExtra("path");
        String dur = MapUtil.getFriendlyTime((int) walkPath.getDuration());
        String dis = MapUtil.getFriendlyLength((int) walkPath.getDistance());
        binding.tvTime.setText(dur + "(" + dis + ")");
        binding.rvRouteDetail.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRouteDetail.setAdapter(new WalkSegmentListAdapter(walkPath.getSteps()));
    }

    private void busDetail(Intent intent) {
        binding.toolbar.setTitle("公交路线规划");

        BusPath busPath = intent.getParcelableExtra("path");
        if (busPath == null) {
            Log.e("RouteDetail", "busPath is null!");
            return;
        }

        String dur = MapUtil.getFriendlyTime((int) busPath.getDuration());
        String dis = MapUtil.getFriendlyLength((int) busPath.getDistance());
        binding.tvTime.setText(dur + "(" + dis + ")");

        List<SchemeBusStep> stepList = getBusSteps(busPath.getSteps());
        Log.d("RouteDetail", "Steps count: " + stepList.size());

        binding.rvRouteDetail.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRouteDetail.setAdapter(new BusSegmentListAdapter(R.layout.item_segment, stepList));
    }





    /**
     * 公交方案数据组装
     * @param list
     * @return
     */
    private List<SchemeBusStep> getBusSteps(List<BusStep> list) {
        List<SchemeBusStep> busStepList = new ArrayList<>();
        SchemeBusStep start = new SchemeBusStep(null);
        start.setStart(true);
        busStepList.add(start);
        for (BusStep busStep : list) {
            if (busStep.getWalk() != null && busStep.getWalk().getDistance() > 0) {
                SchemeBusStep walk = new SchemeBusStep(busStep);
                walk.setWalk(true);
                busStepList.add(walk);
            }
            if (busStep.getBusLine() != null) {
                SchemeBusStep bus = new SchemeBusStep(busStep);
                bus.setBus(true);
                busStepList.add(bus);
            }
            if (busStep.getRailway() != null) {
                SchemeBusStep railway = new SchemeBusStep(busStep);
                railway.setRailway(true);
                busStepList.add(railway);
            }

            if (busStep.getTaxi() != null) {
                SchemeBusStep taxi = new SchemeBusStep(busStep);
                taxi.setTaxi(true);
                busStepList.add(taxi);
            }
        }
        SchemeBusStep end = new SchemeBusStep(null);
        end.setEnd(true);
        busStepList.add(end);
        return busStepList;
    }


}
