package me.uport.sdk.demoapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.did_resolver.*
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import me.uport.sdk.core.UI
import me.uport.sdk.universaldid.DIDDocument
import me.uport.sdk.universaldid.UniversalDID

class DIDResolverActivity : AppCompatActivity() {

    val ethr_did_text = "did:ethr:0xf3beac30c498d9e26865f34fcaa57dbb935b0d74"
    val uport_did_text = "did:uport:2ozs2ntCXceKkAQKX4c9xp2zPS8pvkJhVqC"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.did_resolver)

        ethr_did.text = "Ethr DID\n\n$ethr_did_text"
        uport_did.text = "Uport DID\n\n$uport_did_text"

        resolve_btn.setOnClickListener {

            progress.visibility = View.VISIBLE

            GlobalScope.launch {

                // validate the did before resolving
                val ethrDIDResultText = if (UniversalDID.canResolve(ethr_did_text)) {
                    val ddo: DIDDocument = UniversalDID.resolve(ethr_did_text)

                    "Ethr DID Document\n\n$ddo"
                } else {
                    "Invalid Ethr DID"
                }

                withContext(UI) { ethr_did_doc.text = ethrDIDResultText }

                val uportDIDResultText = if (UniversalDID.canResolve(uport_did_text)) {
                    val ddo: DIDDocument = UniversalDID.resolve(uport_did_text)

                    "Uport DID Document\n\n$ddo"
                } else {
                    "Invalid Uport DID"
                }

                withContext(UI) {
                    uport_did_doc.text = uportDIDResultText
                    progress.visibility = View.GONE
                }
            }
        }
    }
}