package me.uport.sdk.demoapp.request_flows

import me.uport.mnid.MNID
import me.uport.sdk.core.Networks
import me.uport.sdk.ethrdid.EthrDIDResolver
import me.uport.sdk.uportdid.UportDIDResolver
import java.lang.IllegalArgumentException


/**
 * This utility method helps generate the `address` and `network`
 * from a given `uPortDID` or `EthrDID`
 * I returns a Network and Address `Pair`
 * and throws [IllegalArgumentException]
 *
 * TODO: Move this functionality to the SDK and create an API so apps can easily fetch an address from a DID
 */

fun getNetworkAndAddressFromDID(did: String?): Pair<String, String> {

    if (did.isNullOrBlank()) { return ("" to "") }

    // converts possible ethr DIDs to a Pair of Network and Address
    val ethrMatchResult =  ethrDIDAddress().find(did)
    if (ethrMatchResult != null) {
        val (address,_) = ethrMatchResult.destructured
        // This demo app only uses the rinkeby network. It is not safe to assume that all `ethrDID`s are used on rinkeby
        return (Networks.rinkeby.networkId to address)
    }

    // converts possible uport DIDs to a Pair of Network and Address
    val uportMatchResult = UportDIDResolver.uportDIDPattern.find(did)
    if (uportMatchResult != null) {
        val (_, _, mnid) = uportMatchResult.destructured
        val account = MNID.decode(mnid)
        return (account.network to account.address)
    }

    throw IllegalArgumentException("The provided did ($did) is not valid for this operation")
}

private fun ethrDIDAddress() = "^did:ethr:(0x[0-9a-fA-F]{40})".toRegex()