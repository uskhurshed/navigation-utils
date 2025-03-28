package com.easyapps.navigation

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

object NavigationUtils {

    private const val TAG_LOG = "NavigationUtils"
    private var fragmentContainer: Int? = null


    fun AppCompatActivity.setDefaultFragment(@IdRes id: Int,fragment: Fragment) {
        fragmentContainer = id
        val tag = fragment::class.java.simpleName
        Log.e(TAG_LOG, "🔹 setDefaultFragment: $tag")
        supportFragmentManager.beginTransaction()
            .replace(fragmentContainer!!, fragment)
            .commit()
    }

    fun Fragment.navigateTo(fragment: Fragment, addToBackStack: Boolean = true, bundle: Bundle? = null) {
        val tag = fragment::class.java.simpleName
        val view = requireActivity().findViewById<View>(android.R.id.content)
        if (view.tag == true) return

        fragment.arguments = bundle

        Log.e(TAG_LOG, "➡️ navigateTo: $tag | addToBackStack=$addToBackStack")

        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .hide(this)
            .add(fragmentContainer!!, fragment, tag)
            .addToBackStack(if (addToBackStack) tag else null)
            .commit()

        view.tag = true
        view.postDelayed({ view.tag = false }, 400)
    }

    fun Fragment.navigateUp() {
        Log.e(TAG_LOG, "⬅️ navigateUp: backStackEntryCount = ${parentFragmentManager.backStackEntryCount}")
        parentFragmentManager.popBackStack()
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
        val view = requireActivity().findViewById<View>(android.R.id.content)
        if (view.tag == true) return

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
                .hide(this)
                .add(fragmentContainer!!, fragment, tag)
                .apply {
                    if (addToBackStack) addToBackStack(tag)
                }
                .commit()
        }

        view.tag = true
        view.postDelayed({ view.tag = false }, 400)
    }

    fun Fragment.removeFragmentOrUp(fragment: Fragment) {
        val tag = fragment::class.java.simpleName
        Log.e(TAG_LOG, "🗑 removeFragmentOrUp: $tag")
        parentFragmentManager.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }
}
