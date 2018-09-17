package com.aminography.primeadapter.sample.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.aminography.primeadapter.PrimeAdapter;
import com.aminography.primeadapter.PrimeDataHolder;
import com.aminography.primeadapter.callback.OnRecyclerViewItemClickListener;
import com.aminography.primeadapter.draghelper.OnRecyclerViewItemDragListener;
import com.aminography.primeadapter.sample.R;
import com.aminography.primeadapter.sample.logic.InstalledAppData;
import com.aminography.primeadapter.sample.logic.InstalledAppsViewModel;
import com.aminography.primeadapter.sample.tools.PackageUtils;
import com.aminography.primeadapter.sample.tools.UIUtils;
import com.aminography.primeadapter.sample.view.adapter.InstalledAppsListAdapter;
import com.aminography.primeadapter.sample.view.dataholder.InstalledAppListDataHolder;
import com.aminography.primeadapter.sample.view.dataholder.TitleInstalledAppListDataHolder;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aminography on 7/10/2018.
 */
public class InstalledAppsFragment extends Fragment implements OnRecyclerViewItemClickListener, OnRecyclerViewItemDragListener {

    private InstalledAppsListAdapter adapter = null;
    private List<InstalledAppData> dataList = null;
    private ProgressBar progressBar = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_installed_apps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onInitViews(view);
    }

    private void onInitViews(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);

        adapter = PrimeAdapter.Companion.with(recyclerView)
                .setItemClickListener(this)
                .setItemDragListener(this)
                .setLayoutManager(new LinearLayoutManager(getActivity()))
                .setDividerDrawable(R.drawable.divier, (int) (UIUtils.dp2px(getActivity(), 1F) * 72), 0, 0, 0)
                .setHasFixedSize(true)
                .setDraggable(false)
                .setExpandable(false)
                .set()
                .build(InstalledAppsListAdapter.class);

        InstalledAppsViewModel viewModel = ViewModelProviders.of(this).get(InstalledAppsViewModel.class);
        viewModel.getDataList(getActivity()).observe(this, new Observer<List<InstalledAppData>>() {
            @Override
            public void onChanged(@Nullable List<InstalledAppData> installedAppData) {
                dataList = installedAppData;
                fillList(installedAppData);
            }
        });
    }

    private void fillList(List<InstalledAppData> dataList) {
        if (dataList == null) {
            return;
        }
        List<PrimeDataHolder> list = new ArrayList<>();
        list.add(new TitleInstalledAppListDataHolder(dataList.size()));
        list.get(0).setHasDivider(false);

        for (InstalledAppData appData : dataList) {
            list.add(new InstalledAppListDataHolder(appData));
        }

        adapter.replaceDataList(list);
        progressBar.animate().setDuration(500).alpha(list.isEmpty() ? 1F : 0F).start();
    }

    public void setExpandable(boolean expandable) {
        adapter.setExpandable(expandable);
    }

    public void setDraggable(boolean draggable) {
        adapter.setDraggable(draggable);
    }

    public void setDivider(boolean divider) {
        if (divider) {
            adapter.setDividerDrawable(R.drawable.divier, (int) (UIUtils.dp2px(getActivity(), 1F) * 72), 0, 0, 0);
        } else {
            adapter.setDividerDrawable(null, 0, 0, 0, 0);
        }
    }

    @Override
    public void onItemClick(@NotNull PrimeDataHolder primeDataHolder) {
        if (primeDataHolder instanceof InstalledAppListDataHolder) {
            PackageUtils.openApplication(getActivity(), ((InstalledAppListDataHolder) primeDataHolder).getInstalledAppData().getPackageIdentifier());
        }
    }

    @Override
    public void onItemLongClick(@NotNull PrimeDataHolder primeDataHolder) {

    }

    @Override
    public void onItemMoved(int fromPosition, int toPosition) {
        // -1 because title row does not exist in dataList
        dataList.add(toPosition - 1, dataList.remove(fromPosition - 1));
    }

}
