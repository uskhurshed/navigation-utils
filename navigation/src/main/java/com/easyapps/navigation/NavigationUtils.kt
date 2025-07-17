package com.easyapps.navigation

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.easyapps.navigation.NavigationUtils.navigateTo
import kotlin.reflect.KMutableProperty0

object NavigationUtils {

    private const val TAG_LOG = "NavigationUtils"
    private var fragmentContainer: Int? = null
    private var blockActivity = false

    fun AppCompatActivity.setDefaultFragment(@IdRes id: Int,fragment: Fragment,stateSaved: Bundle? = null, bundle: Bundle? = null) {
        fragment::class.java.simpleName.showLog(  "🔹 setDefaultFragment:")
        fragmentContainer = id
        if (stateSaved == null) supportFragmentManager.setDefaultFragment(id,fragment,bundle)
    }

    fun FragmentManager.setDefaultFragment(@IdRes id: Int,fragment: Fragment, bundle: Bundle? = null) {
        fragment::class.java.simpleName.showLog(  "🔹 setDefaultFragment:")
        fragmentContainer = id
        fragment.arguments = bundle
        val beginTransaction = beginTransaction()
        beginTransaction.replace(fragmentContainer!!, fragment)
        beginTransaction.commitAllowingStateLoss()
    }

    private fun Any?.showLog(prefix:String ="") {
        if (BuildConfig.DEBUG) Log.e(TAG_LOG, "$prefix $this")
    }

    fun Fragment.navigateWithClearStack(fragment: Fragment, bundle: Bundle? = null) {
        val tag = fragment::class.java.simpleName
        val currentFragment = requireActivity().supportFragmentManager.fragments.lastOrNull()
        if (blockActivity || currentFragment == fragment) return
        fragment.arguments = bundle

        val supportFragmentManager = requireActivity().supportFragmentManager
        val beginTransaction = supportFragmentManager.beginTransaction()
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE) // - Очистить стек
        beginTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
        beginTransaction .replace(fragmentContainer!!, fragment, tag)
        beginTransaction  .commitAllowingStateLoss()
        blockActivity = true
        Handler(Looper.getMainLooper()).postDelayed({ blockActivity = false }, 400)
    }

    fun Fragment.navigateAndRemoveCurrentFragment(fragment: Fragment, addToBackStack: Boolean = true, fadeAnimation: Boolean = false, bundle: Bundle? = null) {
        if (blockActivity) return
        fragment.arguments = bundle
        val fm = requireActivity().supportFragmentManager
        fm.fragments.lastOrNull()?.tag?.let { prevTag ->
            fm.popBackStack(prevTag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        val beginTransaction = fm.beginTransaction()
        if (fadeAnimation) beginTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
         else beginTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
        beginTransaction.replace(fragmentContainer!!, fragment, fragment.javaClass.simpleName)
        if (addToBackStack) beginTransaction.addToBackStack(fragment.javaClass.simpleName)
        beginTransaction.commitAllowingStateLoss()

        blockActivity = true
        Handler(Looper.getMainLooper()).postDelayed({ blockActivity = false }, 400)
    }




    fun Fragment.navigateTo(fragment: Fragment, addToBackStack: Boolean = true,fadeAnimation:Boolean = false, bundle: Bundle? = null) {
        val tag = fragment::class.java.simpleName
        val currentFragment = requireActivity().supportFragmentManager.fragments.lastOrNull()
        if (blockActivity || currentFragment == fragment) return
        fragment.arguments = bundle

        val beginTransaction = requireActivity().supportFragmentManager.beginTransaction()
        if (fadeAnimation) beginTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
        else beginTransaction. setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
        beginTransaction.setMaxLifecycle(currentFragment  ?: this, Lifecycle.State.STARTED) // <-- отключает "resumed"
        beginTransaction.hide(currentFragment ?: this)
        beginTransaction.add(fragmentContainer!!, fragment, tag)
        beginTransaction.addToBackStack(if (addToBackStack) tag else null)
        beginTransaction.setMaxLifecycle(fragment, Lifecycle.State.RESUMED)
        beginTransaction.commitAllowingStateLoss()

        blockActivity = true
        Handler(Looper.getMainLooper()).postDelayed({ blockActivity = false }, 400)
    }

    fun Fragment.navigateUp() {
        val supportFragmentManager = requireActivity().supportFragmentManager
        val count = supportFragmentManager.backStackEntryCount
        if (count > 0) supportFragmentManager.popBackStack()
        else requireActivity().finish()
    }

    fun Fragment.getBackStack(): Pair<Int, List<String>> {
        val supportFragmentManager = requireActivity().supportFragmentManager
        val count = supportFragmentManager.backStackEntryCount
        val fragmentNames = mutableListOf<String>()

        for (i in 0 until count) {
            val fragmentName = requireActivity().supportFragmentManager.getBackStackEntryAt(i).name ?: ""
            fragmentNames.add(fragmentName)
        }
        fragmentNames.showLog("📦 BackStack Fragment ($count):")
        return count to fragmentNames
    }


    fun Fragment.navigateToIfHaveInStack(fragment: Fragment, addToBackStack: Boolean = true, bundle: Bundle? = null) {
        val tag = fragment::class.java.simpleName
        if (blockActivity) return

        fragment.arguments = bundle
        val supportFragmentManager = requireActivity().supportFragmentManager
        val isInBackStack = supportFragmentManager.findFragmentByTag(tag) != null

        if (isInBackStack) {
            val removedFragments = mutableListOf<String>()
            val count = supportFragmentManager.backStackEntryCount
            for (i in count - 1 downTo 0) {
                val entry = supportFragmentManager.getBackStackEntryAt(i)
                if (entry.name == tag) break
                removedFragments.add(entry.name ?: "unnamed")
            }
            removedFragments.showLog("🔁 Fragment '$tag' найден в backStack. Удаляется: ")
            supportFragmentManager.popBackStack(tag, 0)
        } else {
            showLog("➕ Fragment '$tag' НЕ найден в backStack. Добавляется.")
            val beginTransaction = requireActivity().supportFragmentManager.beginTransaction()
            beginTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
            beginTransaction.setMaxLifecycle(this, Lifecycle.State.STARTED) // <-- отключает "resumed"
            beginTransaction.hide(this)
            beginTransaction.add(fragmentContainer!!, fragment, tag)
            beginTransaction.setMaxLifecycle(fragment, Lifecycle.State.RESUMED)
            if (addToBackStack) beginTransaction.addToBackStack(tag)
            beginTransaction.commitAllowingStateLoss()
        }

        blockActivity = true
        Handler(Looper.getMainLooper()).postDelayed({ blockActivity = false }, 400)
    }

    fun Fragment.removeFragmentOrUp(fragment: Fragment) {
        val tag = fragment::class.java.simpleName
        showLog("🗑 removeFragmentOrUp: $tag")
        val supportFragmentManager = requireActivity().supportFragmentManager
        supportFragmentManager.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }


    inline fun <reified T : Fragment> Fragment.showFragment(fragmentRef: KMutableProperty0<T?>, activeRef: KMutableProperty0<Fragment?>, containerId: Int, factory: () -> T): Boolean {
        val tag = T::class.java.simpleName
        val fragmentManager = childFragmentManager
        val transaction = fragmentManager.beginTransaction()

        var target = fragmentManager.findFragmentByTag(tag) as? T
        if (target == null) target = fragmentRef.get()

        if (target == null) {
            target = factory()
            fragmentRef.set(target)
            transaction.add(containerId, target, tag)
        }

        fragmentManager.fragments.forEach {
            if (it != target && it.isAdded && !it.isHidden) {
                transaction.hide(it)
            }
        }
        transaction.show(target)
        activeRef.set(target)
        fragmentRef.set(target)
        transaction.commitAllowingStateLoss()
        return true
    }



    fun FragmentManager.addFragmentChangedListener(callback: (Fragment, Bundle?) -> Unit) {
        registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentResumed(fm: FragmentManager, fragment: Fragment) =
                callback(fragment, fragment.arguments)
        }, true)
    }



    fun setOnBackPressedFragment(fragment: Fragment,onPressed: (Boolean) -> Unit = {}) {
        var lastBackPressedTime = 0L

        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed(){
                val currentTime = System.currentTimeMillis()
                val isDoubleClick = currentTime - lastBackPressedTime < 1000
                lastBackPressedTime = currentTime
                onPressed(isDoubleClick)
            }
        }
        fragment.viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> fragment.requireActivity().onBackPressedDispatcher.addCallback(fragment.viewLifecycleOwner, backCallback)
                Lifecycle.Event.ON_PAUSE -> backCallback.remove()
                else -> {}
            }
        })
    }


}
