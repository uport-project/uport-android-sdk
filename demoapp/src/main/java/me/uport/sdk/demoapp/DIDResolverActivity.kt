package me.uport.sdk.demoapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.did_resolver.*
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import kotlinx.serialization.json.JSON
import me.uport.sdk.core.UI
import me.uport.sdk.universaldid.DIDDocument
import me.uport.sdk.universaldid.UniversalDID

class DIDResolverActivity : AppCompatActivity() {

    val ethr_did_text = "did:ethr:0xf3beac30c498d9e26865f34fcaa57dbb935b0d74"
    val uport_did_text = "did:uport:2nQtiQG6Cgm1GYTBaaKAgr76uY7iSexUkqX"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.did_resolver)

        ethr_did.text = "Ethr DID\n$ethr_did_text"
        uport_did.text = "Ethr DID\n$uport_did_text"

        resolve_btn.setOnClickListener {
            GlobalScope.launch(UI) {

                // validate the did before resolving
                var status = UniversalDID.canResolve(ethr_did_text)
                if (status) {
                    val ddo: DIDDocument = UniversalDID.resolve(ethr_did_text)

                    ethr_did_doc.text = "Ethr DID Document\n${ddo}"
                } else ethr_did_doc.text = "Invalid Ethr DID"

                status = UniversalDID.canResolve(uport_did_text)
                if (status) {
                    val ddo: DIDDocument = UniversalDID.resolve(uport_did_text)

                    uport_did_doc.text = "Uport DID Document\n${ddo}"
                } else uport_did_doc.text = "Invalid Uport DID"
            }
        }
    }
}