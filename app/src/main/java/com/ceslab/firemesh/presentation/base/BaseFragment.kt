package com.ceslab.firemesh.presentation.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.ceslab.firemesh.R
import com.ceslab.firemesh.factory.ViewModelFactory
import com.ceslab.firemesh.presentation.main.activity.MainActivity
import com.ceslab.firemesh.util.AndroidDialogUtil
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Viet Hua on 01/30/2021.
 */

abstract class BaseFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    protected abstract fun getResLayoutId(): Int
    protected abstract fun onMyViewCreated(view: View)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("onCreateView")
        val view = inflater.inflate(getResLayoutId(), container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onMyViewCreated(view)
    }

    fun setupToolbarTitle(title:String){
        val mainActivity = activity as MainActivity
        mainActivity.setToolbarTitle(title)
    }

    fun replaceFragment(fragment: Fragment, tag: String, containerId: Int) {
        Timber.d("replaceFragment: name=${fragment.javaClass.name}")
        val fragmentManager = childFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left,android.R.anim.slide_out_right)
        fragmentTransaction.replace(containerId, fragment, tag)
            .addToBackStack(tag)
            .commit()
    }


    fun showProgressDialog(message: String) {
        AndroidDialogUtil.getInstance().showLoadingDialog(activity, message)
    }

    fun showWarningDialog(message: String) {
        AndroidDialogUtil.getInstance().showWarningDialog(activity, message)
    }

    fun showSuccessDialog(message: String) {
        AndroidDialogUtil.getInstance().showSuccessDialog(activity, message)
    }


    fun showFailedDialog(message: String) {
        AndroidDialogUtil.getInstance().showFailureDialog(activity, message)
    }

    fun hideDialog() {
        Timber.d("hideDialog")
        AndroidDialogUtil.getInstance().hideDialog()
    }

    fun showToastMessage(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

}