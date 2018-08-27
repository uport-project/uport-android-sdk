package com.uport.sdk.signer.encryption

import android.annotation.SuppressLint
import android.app.Activity
import android.app.KeyguardManager
import android.content.Context.KEYGUARD_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import com.uport.sdk.signer.hasMarshmallow

class KeyguardLaunchFragment : Fragment() {

    private lateinit var keyguardManager: KeyguardManager
    private lateinit var callback: KeyguardCallback
    private lateinit var purpose: String

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        keyguardManager = context?.getSystemService(KEYGUARD_SERVICE) as KeyguardManager

        val keyguardIntent = createConfirmDeviceCredentialIntent("uPort", purpose)
        startActivityForResult(keyguardIntent, REQUEST_CODE_KEYGUARD)
    }

    /**
     * Pops up the keyguard with the corresponding text.
     * On API 23+, this is needed to unlock keys that have been created with [setRequiresAuthentication(true)]
     */
    @SuppressLint("NewApi")
    private fun createConfirmDeviceCredentialIntent(title: String, description: String): Intent {
        return if (hasMarshmallow()) {
            keyguardManager.createConfirmDeviceCredentialIntent(title, description)
        } else {
            val action = "android.app.action.CONFIRM_DEVICE_CREDENTIAL"
            val keyguardIntent = Intent(action)
            keyguardIntent.putExtra("android.app.extra.DESCRIPTION", description)

            keyguardIntent.putExtra("android.app.extra.TITLE", "$title\n\n$description")
            keyguardIntent.setPackage(getSettingsPackageForIntent(keyguardIntent))

            keyguardIntent
        }
    }

    /**
     * based on AOSP KeyguardManager.java for API 23+
     */
    @SuppressLint("InlinedApi")
    private fun getSettingsPackageForIntent(intent: Intent): String {
        val ctx = context
        if (ctx != null) {
            val resolveInfos = ctx.packageManager
                    .queryIntentActivities(intent, PackageManager.MATCH_SYSTEM_ONLY)
            for (i in resolveInfos.indices) {
                return resolveInfos[i].activityInfo.packageName
            }
        }
        return "com.android.settings"
    }

    private fun init(purpose: String, callback: KeyguardCallback) {
        this.callback = callback
        this.purpose = purpose
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_KEYGUARD) {
            val result = resultCode == Activity.RESULT_OK
            callback.onKeyguardResult(result)
            dismiss()
        }
    }

    private fun dismiss() {
        fragmentManager?.beginTransaction()?.remove(this)?.commit()
    }

    interface KeyguardCallback {
        fun onKeyguardResult(unlocked: Boolean)
    }

    companion object {

        private const val TAG_KEYGUARD_FRAGMENT: String = "keyguard_fragment"
        private const val REQUEST_CODE_KEYGUARD: Int = 19867

        fun show(fragManager: FragmentManager, purpose: String, callback: KeyguardCallback) {

            //cleanup..
            val headlessFragment = fragManager.findFragmentByTag(TAG_KEYGUARD_FRAGMENT) as KeyguardLaunchFragment?
            if (headlessFragment != null) {
                fragManager.beginTransaction().remove(headlessFragment).commitAllowingStateLoss()
            }

            val fragment = KeyguardLaunchFragment()
            fragment.init(purpose, callback)
            fragManager.beginTransaction().add(fragment, TAG_KEYGUARD_FRAGMENT).commit()
        }


    }
}