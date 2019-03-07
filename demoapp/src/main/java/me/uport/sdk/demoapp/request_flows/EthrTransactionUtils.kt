package me.uport.sdk.demoapp.request_flows

import me.uport.mnid.MNID
import me.uport.sdk.core.Networks
import me.uport.sdk.ethrdid.EthrDIDResolver
import me.uport.sdk.uportdid.UportDIDResolver
import java.lang.IllegalArgumentException


/**
 * This utility method helps generate the [address] and [network]
 * from a given [uPortDID] or [EthrDID]
 * I returns a Network and Address [Pair]
 * and throws [IllegalArgumentException]
 */
fun getNetworkAndAddressFromDID(did: String): Pair<String, String> {

    // converts possible ethr DIDs to a Pair of Network and Address
    val ethrMatchResult = EthrDIDResolver.identityExtractPattern.find(did)
    if (ethrMatchResult != null) {
        val (_, _, _, _, _, address) = ethrMatchResult.destructured
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