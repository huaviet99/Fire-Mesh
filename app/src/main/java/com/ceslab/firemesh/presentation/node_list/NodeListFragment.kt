package com.ceslab.firemesh.presentation.node_list

import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ceslab.firemesh.R
import com.ceslab.firemesh.meshmodule.model.MeshNode
import com.ceslab.firemesh.presentation.base.BaseFragment
import com.ceslab.firemesh.presentation.base.BaseRecyclerViewAdapter
import com.ceslab.firemesh.presentation.main.activity.MainActivity
import com.ceslab.firemesh.presentation.node.NodeFragment
import com.ceslab.firemesh.presentation.node.NodeViewModel
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_node_list.*
import timber.log.Timber

class NodeListFragment : BaseFragment(){
    companion object {
        const val TAG = "NodeListFragment"
    }

    private lateinit var nodeListRecyclerViewAdapter: NodeListRecyclerViewAdapter
    private lateinit var nodeListViewModel: NodeListViewModel


    override fun getResLayoutId(): Int {
       return R.layout.fragment_node_list
    }

    override fun onMyViewCreated(view: View) {
       Timber.d("onMyViewCreated")
        setupViewModel()
        setupRecyclerView()
    }

    private fun setupViewModel() {
        Timber.d("setupViewModel")
        AndroidSupportInjection.inject(this)
        nodeListViewModel = ViewModelProvider(this, viewModelFactory).get(NodeListViewModel::class.java)
        nodeListViewModel.getMeshNodeList().observe(this,meshNodeListObserver)
    }

    private fun setupRecyclerView(){
        Timber.d("setupRecyclerView")
        val linearLayoutManager = LinearLayoutManager(view!!.context)
        nodeListRecyclerViewAdapter = NodeListRecyclerViewAdapter(view!!.context)
        nodeListRecyclerViewAdapter.itemClickListener = onNodeItemClickedListener
        rv_node_list.apply {
            layoutManager = linearLayoutManager
            setHasFixedSize(true)
            adapter = nodeListRecyclerViewAdapter
        }
    }

    private val onNodeItemClickedListener = object : BaseRecyclerViewAdapter.ItemClickListener<MeshNode> {
        override fun onClick(position: Int, item: MeshNode) {
            Timber.d("onNodeItemClickedListener: clicked")
            nodeListViewModel.setDeviceToConfigure(item)
            val mainActivity = activity as MainActivity
            mainActivity.replaceFragment(NodeFragment(), NodeFragment.TAG,R.id.container_main)
        }
    }

    private val meshNodeListObserver = Observer<Set<MeshNode>> {
       nodeListRecyclerViewAdapter.setDataList(it.toMutableList())
    }
}