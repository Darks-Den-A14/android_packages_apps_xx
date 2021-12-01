/*
 * Copyright (C) 2021 Chaldeaprjkt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.chaldeaprjkt.gamespace.preferences

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import androidx.activity.result.ActivityResult
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import io.chaldeaprjkt.gamespace.R
import io.chaldeaprjkt.gamespace.data.SystemSettings
import io.chaldeaprjkt.gamespace.data.UserGame
import io.chaldeaprjkt.gamespace.preferences.appselector.AppSelectorActivity
import io.chaldeaprjkt.gamespace.utils.GameModeUtils


class AppListPreferences(context: Context?, attrs: AttributeSet?) :
    PreferenceCategory(context, attrs), Preference.OnPreferenceClickListener {

    private val apps = mutableListOf<UserGame>()
    private var settings: SystemSettings? = null

    init {
        isOrderingAsAdded = false
        settings = context?.let { SystemSettings(it) }
    }

    private val makeAddPref by lazy {
        Preference(context).apply {
            title = "Add"
            key = KEY_ADD_GAME
            setIcon(R.drawable.ic_add)
            isPersistent = false
            onPreferenceClickListener = this@AppListPreferences
        }
    }

    private fun getAppInfo(packageName: String): ApplicationInfo? {
        return try {
            context.packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    private fun updateAppList() {
        apps.clear()
        if (!settings?.userGames.isNullOrEmpty()) {
            settings?.let { apps.addAll(it.userGames) }
        }
        removeAll()
        addPreference(makeAddPref)
        apps.map {
            val appInfo = getAppInfo(it.packageName) ?: return
            Preference(context).apply {
                key = it.packageName
                title = appInfo.loadLabel(context.packageManager)
                summary = appInfo.packageName
                icon = appInfo.loadIcon(context.packageManager)
                isPersistent = false
                onPreferenceClickListener = this@AppListPreferences
            }
        }.sortedBy { it.title.toString().lowercase() }
            .forEach(::addPreference)
    }

    private fun registerApp(packageName: String) {
        if (!apps.any { it.packageName == packageName }) {
            apps.add(UserGame(packageName))
        }
        settings?.let { it.userGames = apps }
        GameModeUtils.setupIntervention(packageName)
        updateAppList()
    }

    private fun unregisterApp(preference: Preference) {
        apps.removeIf { it.packageName == preference.key }
        settings?.let { it.userGames = apps }
        GameModeUtils.clearIntervention(preference.key)
        updateAppList()
    }

    override fun onAttached() {
        super.onAttached()
        updateAppList()
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        if (preference != makeAddPref) {
            val message = context.getString(R.string.game_remove_message, preference.title)
            AlertDialog.Builder(context).setTitle(R.string.game_list_title)
                .setMessage(message)
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    unregisterApp(preference)
                }
                .show()
        } else {
            parentActivity?.startActivity(Intent(context, AppSelectorActivity::class.java))
        }
        return true
    }

    fun useSelectorResult(result: ActivityResult?) {
        result?.takeIf { it.resultCode == Activity.RESULT_OK }
            ?.data?.getStringExtra(EXTRA_APP)
            ?.let { registerApp(it) }
    }

    private val parentActivity: Activity?
        get() {
            if (context is Activity)
                return context as Activity

            if (context is ContextThemeWrapper && (context as ContextThemeWrapper).baseContext is Activity)
                return (context as ContextThemeWrapper).baseContext as Activity
            return null
        }


    companion object {
        const val KEY_ADD_GAME = "add_game"
        const val EXTRA_APP = "selected_app"
    }
}