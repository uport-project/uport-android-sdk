@file:Suppress("DEPRECATION")

package com.uport.sdk.signer.encryption

import android.animation.Animator
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import com.uport.sdk.signer.R

@TargetApi(Build.VERSION_CODES.M)
class FingerprintDialog : DialogFragment() {

    private lateinit var fingerprintManager: FingerprintManager
    private lateinit var cancellationSignal: CancellationSignal
    private lateinit var cryptoObject: FingerprintManager.CryptoObject
    private lateinit var callbacks: FingerprintDialogCallbacks
    private lateinit var purpose: String

    private lateinit var purposeTextView: TextView
    private lateinit var imageViewStatus: ImageView
    private lateinit var textViewStatus: TextView
    private lateinit var cancelButton: View

    private var successColor: Int = 0
    private var failureColor: Int = 0

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retainInstance = true

        purpose = savedInstanceState?.getString(KEY_DIALOG_PURPOSE) ?: arguments?.getString(KEY_DIALOG_PURPOSE) ?: ""

        fingerprintManager = context?.getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager
        successColor = context?.getColor(R.color.uport_fingerprint_green) ?: Color.GREEN
        failureColor = context?.getColor(R.color.uport_fingerprint_red) ?: Color.RED
    }

    override
    fun onStart() {
        super.onStart()
        // Reset the cancellation signal which is used to cancel the
        // fingerprint authentication process.
        cancellationSignal = CancellationSignal()

        // Check if a valid CryptoObject has been provided
        try {
            // Start listening for fingerprint events
            fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, AuthCallbacks(), null)
        } catch (e: IllegalArgumentException) {
            // Should never be thrown since we have declared the USE_FINGERPRINT permission
            // in the manifest
        } catch (e: IllegalStateException) {
            //nop
        } catch (e: SecurityException) {
            //nop
        }
    }

    override
    fun onStop() {
        super.onStop()

        // If the fingerprint authentication process is running, cancel it.
        cancellationSignal.cancel()
    }

    override
    fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val content = inflater.inflate(R.layout.fragment_fingerprint, container)

        purposeTextView = content.findViewById(R.id.purpose)
        purposeTextView.text = purpose
        imageViewStatus = content.findViewById(R.id.imageViewFingerprintStatus)
        textViewStatus = content.findViewById(R.id.textViewFingerprintStatus)
        cancelButton = content.findViewById(R.id.buttonFingerprintCancel)

        cancelButton.setOnClickListener { onCancelPressed() }

        return content
    }

    override
    fun onDestroyView() {
        val dialog = dialog
        // handles https://code.google.com/p/android/issues/detail?id=17423
        if (dialog != null && retainInstance) {
            dialog.setDismissMessage(null)
        }
        super.onDestroyView()
    }

    private fun onCancelPressed() {
        callbacks.onFingerprintCancel()
        dismiss()
    }

    /**
     * Should be called before the dialog is shown in order to provide a valid CryptoObject.
     *
     * @param cryptoObject The [FingerprintManager.CryptoObject] we want to authenticate for
     * @param callbacks an implementation of the [FingerprintDialogCallbacks] interface
     */
    fun init(cryptoObject: FingerprintManager.CryptoObject, callbacks: FingerprintDialogCallbacks) {
        this.cryptoObject = cryptoObject
        this.callbacks = callbacks
    }

    /**
     * Updates the status text in the dialog with the provided error message.
     *
     * @param text represents the error message which will be shown
     */
    private fun showErrorText(text: CharSequence) {
        imageViewStatus.setImageResource(R.drawable.uport_ic_fingerprint_error)
        textViewStatus.text = text
        textViewStatus.setTextColor(failureColor)

        imageViewStatus.animate()
                .rotationBy(90f)
                .setInterpolator(OvershootInterpolator(1.4f)).duration = ANIMATION_DURATION.toLong()
    }

    /**
     * Updates the status text in the dialog with a success text.
     */
    private fun showSuccessText() {
        imageViewStatus.setImageResource(R.drawable.uport_ic_fingerprint_done)
        textViewStatus.text = getString(R.string.uport_fingerprint_success)
        textViewStatus.setTextColor(successColor)

        imageViewStatus.rotation = 60f
        imageViewStatus.animate()
                .rotation(0f)
                .setInterpolator(DecelerateInterpolator(1.4f))
                .setDuration(ANIMATION_DURATION.toLong())
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                        // Empty
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        // Wait for the animation to finish, then dismiss the dialog and
                        // invoke the callback method
                        callbacks.onFingerprintSuccess(cryptoObject)
                        dismiss()
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        // Empty
                    }

                    override fun onAnimationRepeat(animation: Animator) {
                        // Empty
                    }
                })
    }

    /**
     * The interface which the caller must implement
     */
    interface FingerprintDialogCallbacks {

        fun onFingerprintSuccess(cryptoObject: FingerprintManager.CryptoObject)

        fun onFingerprintCancel()
    }

    /**
     * This class represents the callbacks invoked by the FingerprintManager class.
     */
    inner class AuthCallbacks : FingerprintManager.AuthenticationCallback() {

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            showErrorText("Authentication failed")
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            //TODO: if "too many attempts error occurs, ask for device unlock"
            showErrorText(errString)
        }

        override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence) {
            super.onAuthenticationHelp(helpCode, helpString)
            showErrorText(helpString)
        }

        override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            showSuccessText()
        }
    }

    companion object {

        const val ANIMATION_DURATION = 500
        const val KEY_DIALOG_PURPOSE = "key_purpose"
        const val TAG_FINGERPRINT_DIALOG: String = "fingerprint_dialog"

        fun create(purpose: String): FingerprintDialog {
            val dialog = FingerprintDialog()
            val bundle = Bundle()
            bundle.putString(KEY_DIALOG_PURPOSE, purpose)
            dialog.arguments = bundle
            return dialog
        }

    }
}