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
import kotlin.reflect.KMutableProperty0

object NavigationUtils {

    private const val TAG_LOG = "NavigationUtils"
    private var fragmentContainer: Int? = null
    private var blockActivity = false


    fun AppCompatActivity.setDefaultFragment(@IdRes id: Int,fragment: Fragment) {
        fragmentContainer = id
        val tag = fragment::class.java.simpleName
        Log.e(TAG_LOG, "🔹 setDefaultFragment: $tag")
        supportFragmentManager.setDefaultFragment(id,fragment)
    }

    fun FragmentManager.setDefaultFragment(@IdRes id: Int,fragment: Fragment) {
        fragmentContainer = id
        val tag = fragment::class.java.simpleName
        Log.e(TAG_LOG, "🔹 setDefaultFragment: $tag")
        beginTransaction()
            .replace(fragmentContainer!!, fragment)
            .commit()
    }

    fun Fragment.navigateWithClearStack(fragment: Fragment, bundle: Bundle? = null) {
        if (blockActivity) return

        val tag = fragment::class.java.simpleName
        fragment.arguments = bundle

        // Очистить стек
        parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        parentFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            .replace(fragmentContainer!!, fragment, tag)
            .commit()

        blockActivity = true
        Handler(Looper.getMainLooper()).postDelayed({ blockActivity = false }, 400)
    }


    fun Fragment.navigateAndRemoveCurrentFragment(fragment: Fragment, bundle: Bundle? = null) {
        if (blockActivity) return

        val tag = fragment::class.java.simpleName
        fragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
            .remove(this)
            .add(fragmentContainer!!, fragment, tag)
            .commit()

        blockActivity = true
        Handler(Looper.getMainLooper()).postDelayed({ blockActivity = false }, 400)
    }


    fun Fragment.navigateTo(fragment: Fragment, addToBackStack: Boolean = true, bundle: Bundle? = null) {
        val tag = fragment::class.java.simpleName
        val currentFragment = parentFragmentManager.fragments.lastOrNull()
        if (blockActivity || currentFragment != this) return

        fragment.arguments = bundle

        Log.e(TAG_LOG, "➡️ navigateTo: $tag | addToBackStack=$addToBackStack")

        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .setMaxLifecycle(this, Lifecycle.State.STARTED) // <-- отключает "resumed"
            .hide(this)
            .add(fragmentContainer!!, fragment, tag)
            .addToBackStack(if (addToBackStack) tag else null)
            .setMaxLifecycle(fragment, Lifecycle.State.RESUMED)
            .commit()

        blockActivity = true
        Handler(Looper.getMainLooper()).postDelayed({ blockActivity = false }, 400)
    }

    fun Fragment.navigateUp() {
        val count = parentFragmentManager.backStackEntryCount
        if (count > 0) parentFragmentManager.popBackStack()
        else requireActivity().finish()
    }

    fun Fragment.getBackStack(): Pair<Int, List<String>> {
        val count = parentFragmentManager.backStackEntryCount
        val fragmentNames = mutableListOf<String>()

        for (i in 0 until count) {
            val fragmentName = parentFragmentManager.getBackStackEntryAt(i).name ?: ""
            fragmentNames.add(fragmentName)
        }

        Log.e(TAG_LOG, "📦 BackStack Fragment ($count): $fragmentNames")
        return count to fragmentNames
    }


    fun Fragment.navigateToIfHaveInStack(fragment: Fragment, addToBackStack: Boolean = true, bundle: Bundle? = null) {
        val tag = fragment::class.java.simpleName
        if (blockActivity) return

        fragment.arguments = bundle

        val isInBackStack = parentFragmentManager.findFragmentByTag(tag) != null

        if (isInBackStack) {
            val removedFragments = mutableListOf<String>()
            val count = parentFragmentManager.backStackEntryCount
            for (i in count - 1 downTo 0) {
                val entry = parentFragmentManager.getBackStackEntryAt(i)
                if (entry.name == tag) break
                removedFragments.add(entry.name ?: "unnamed")
            }

            Log.e(TAG_LOG, "🔁 Fragment '$tag' найден в backStack. Удаляется: $removedFragments")
            parentFragmentManager.popBackStack(tag, 0)
        } else {
            Log.e(TAG_LOG, "➕ Fragment '$tag' НЕ найден в backStack. Добавляется.")
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .setMaxLifecycle(this, Lifecycle.State.STARTED) // <-- отключает "resumed"
                .hide(this)
                .add(fragmentContainer!!, fragment, tag)
                .setMaxLifecycle(fragment, Lifecycle.State.RESUMED)
                .apply {
                    if (addToBackStack) addToBackStack(tag)
                }
                .commit()
        }

        blockActivity = true
        Handler(Looper.getMainLooper()).postDelayed({ blockActivity = false }, 400)
    }

    fun Fragment.removeFragmentOrUp(fragment: Fragment) {
        val tag = fragment::class.java.simpleName
        Log.e(TAG_LOG, "🗑 removeFragmentOrUp: $tag")
        parentFragmentManager.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }



    inline fun <reified T : Fragment> Fragment.showFragment(fragmentRef: KMutableProperty0<T?>, activeRef: KMutableProperty0<Fragment?>, containerId: Int, factory: () -> T) :Boolean {
        val transaction = childFragmentManager.beginTransaction()

        // Скрыть текущий фрагмент
        activeRef.get()?.let { transaction.hide(it) }

        // Показать/добавить новый
        var target = fragmentRef.get()
        if (target == null) {
            target = factory()
            fragmentRef.set(target)
            transaction.add(containerId, target)
        } else {
            transaction.show(target)
        }

        activeRef.set(target)
        transaction.commit()
        return true
    }


    fun onBackPressed(fragment: Fragment,onPressed: (Boolean) -> Unit = {}) {
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
