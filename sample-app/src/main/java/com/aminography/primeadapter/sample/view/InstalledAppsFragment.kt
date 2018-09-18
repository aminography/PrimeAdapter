package com.aminography.primeadapter.sample.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aminography.primeadapter.PrimeAdapter
import com.aminography.primeadapter.PrimeDataHolder
import com.aminography.primeadapter.callback.OnRecyclerViewItemClickListener
import com.aminography.primeadapter.draghelper.OnRecyclerViewItemDragListener
import com.aminography.primeadapter.sample.R
import com.aminography.primeadapter.sample.logic.InstalledAppData
import com.aminography.primeadapter.sample.logic.InstalledAppsViewModel
import com.aminography.primeadapter.sample.tools.PackageUtils
import com.aminography.primeadapter.sample.tools.UIUtils
import com.aminography.primeadapter.sample.view.adapter.InstalledAppsListAdapter
import com.aminography.primeadapter.sample.view.dataholder.InstalledAppListDataHolder
import com.aminography.primeadapter.sample.view.dataholder.TitleInstalledAppListDataHolder
import kotlinx.android.synthetic.main.fragment_installed_apps.*


/**
 * Created by aminography on 7/10/2018.
 */
class InstalledAppsFragment : Fragment(), OnRecyclerViewItemClickListener, OnRecyclerViewItemDragListener {

    private var adapter: InstalledAppsListAdapter? = null
    private var dataList: MutableList<InstalledAppData>? = null

    var expandable: Boolean = false
        set(value) {
            field = value
            adapter?.setExpandable(value)
        }

    var draggable: Boolean = false
        set(value) {
            field = value
            adapter?.setDraggable(value)
        }

    var hasDivider: Boolean = true
        set(value) {
            field = value
            if (value) {
                adapter?.setDivider(insetLeft = UIUtils.dp2px(activity!!, 72F).toInt())
            } else {
                adapter?.setDivider(null)
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_installed_apps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onInitViews()
    }

    private fun onInitViews() {
        adapter = PrimeAdapter.with(recyclerView)
                .setItemClickListener(this)
                .setItemDragListener(this)
                .setLayoutManager(LinearLayoutManager(activity))
                .setDivider(insetLeft = UIUtils.dp2px(activity!!, 72F).toInt())
                .setHasFixedSize(true)
                .setDraggable(false)
                .setExpandable(false)
                .set()
                .build(InstalledAppsListAdapter::class.java)

        val viewModel = ViewModelProviders.of(this).get(InstalledAppsViewModel::class.java)
        viewModel.getDataList(activity!!)?.observe(this, Observer<MutableList<InstalledAppData>?> {
            it?.apply {
                dataList = this
                fillList(this)
            }
        })
    }

    private fun fillList(dataList: MutableList<InstalledAppData>) {
        val list = ArrayList<PrimeDataHolder>()
        list.add(TitleInstalledAppListDataHolder(dataList.size))
        list[0].hasDivider = false

        for (appData in dataList) {
            list.add(InstalledAppListDataHolder(appData))
        }

        adapter?.replaceDataList(list)
        progressBar.animate().setDuration(500).alpha(if (list.isEmpty()) 1F else 0F).start()
    }

    override fun onItemMoved(fromPosition: Int, toPosition: Int) {
        // -1 because title row does not exist in dataList
        dataList?.add(toPosition - 1, dataList!!.removeAt(fromPosition - 1))
    }

    override fun onItemClick(primeDataHolder: PrimeDataHolder) {
        if (primeDataHolder is InstalledAppListDataHolder) {
            PackageUtils.openApplication(activity!!, primeDataHolder.installedAppData.packageIdentifier)
        }
    }

    override fun onItemLongClick(primeDataHolder: PrimeDataHolder) {
    }

}
