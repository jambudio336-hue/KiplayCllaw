package com.k1play.agentpro.tools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.edit
import org.json.JSONObject

/** Receives Termux's result callback and stores a short-lived result for TermuxExecTool. */
class TermuxCommandResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val id = intent.getStringExtra(EXTRA_EXECUTION_ID) ?: return
        val bundle = intent.getBundleExtra(EXTRA_RESULT_BUNDLE)
        val result = JSONObject().apply {
            if (bundle == null) {
                put("error", "Termux returned no result bundle. Check the Termux version and external-app settings.")
            } else {
                put("stdout", bundle.getString(EXTRA_STDOUT).orEmpty())
                put("stderr", bundle.getString(EXTRA_STDERR).orEmpty())
                put("exit_code", bundle.getInt(EXTRA_EXIT_CODE, -1))
                val errorCode = bundle.getInt(EXTRA_ERROR_CODE, -1)
                val errorMessage = bundle.getString(EXTRA_ERROR_MESSAGE).orEmpty()
                if (errorCode != -1 || errorMessage.isNotBlank()) {
                    put("error", errorMessage.ifBlank { "Termux internal error code: $errorCode" })
                }
            }
        }
        context.getSharedPreferences(TermuxExecTool.PREFS_NAME, Context.MODE_PRIVATE).edit {
            putString(TermuxExecTool.RESULT_PREFIX + id, result.toString())
        }
    }

    companion object {
        const val EXTRA_EXECUTION_ID = "execution_id"
        private const val EXTRA_RESULT_BUNDLE = "com.termux.service.EXTRA_PLUGIN_RESULT_BUNDLE"
        private const val EXTRA_STDOUT = "com.termux.service.EXTRA_PLUGIN_RESULT_BUNDLE_STDOUT"
        private const val EXTRA_STDERR = "com.termux.service.EXTRA_PLUGIN_RESULT_BUNDLE_STDERR"
        private const val EXTRA_EXIT_CODE = "com.termux.service.EXTRA_PLUGIN_RESULT_BUNDLE_EXIT_CODE"
        private const val EXTRA_ERROR_CODE = "com.termux.service.EXTRA_PLUGIN_RESULT_BUNDLE_ERR"
        private const val EXTRA_ERROR_MESSAGE = "com.termux.service.EXTRA_PLUGIN_RESULT_BUNDLE_ERRMSG"
    }
}
