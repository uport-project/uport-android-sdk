package me.uport.sdk.core

import me.uport.mnid.MNID
import org.walleth.khex.clean0xPrefix
import org.walleth.khex.prepend0xPrefix

private val defaultFaucetUrl = "https://sensui.uport.me/api/v1/fund/"
private val defaultTxRelayUrl = "https://sensui.uport.me/api/v2/relay/"
private val mainnetId = "0x1"
private val ropstenId = "0x3"
private val rinkebyId = "0x4"
private val kovanId = "0x2a"

/**
 * Holds urls and addresses for different eth networks.
 * use the `registerNetwork` method to override defaults
 */
object Networks {

    val mainnet = EthNetwork(
            "mainnet",
            mainnetId,
            MNID.encode(mainnetId, "0xab5c8051b9a1df1aab0149f8b0630848b7ecabf6"),
            "https://mainnet.infura.io/uport",
            "https://etherscan.io",
            defaultFaucetUrl,
            defaultTxRelayUrl,
            "0xec2642cd5a47fd5cca2a8a280c3b5f88828aa578")
    val ropsten = EthNetwork(
            "ropsten",
            ropstenId,
            MNID.encode(ropstenId, "0x41566e3a081f5032bdcad470adb797635ddfe1f0"),
            "https://ropsten.infura.io/uport",
            "https://ropsten.io",
            defaultFaucetUrl,
            defaultTxRelayUrl,
            "0xa5e04cf2942868f5a66b9f7db790b8ab662039d5")
    val kovan = EthNetwork(
            "kovan",
            kovanId,
            MNID.encode(kovanId, "0x5f8e9351dc2d238fb878b6ae43aa740d62fc9758"),
            "https://kovan.infura.io/uport",
            "https://kovan.etherscan.io",
            defaultFaucetUrl,
            defaultTxRelayUrl,
            "0xa9235151d3afa7912e9091ab76a36cbabe219a0c")
    val rinkeby = EthNetwork(
            "rinkeby",
            rinkebyId,
            MNID.encode(rinkebyId, "0x2cc31912b2b0f3075a87b3640923d45a26cef3ee"),
            "https://rinkeby.infura.io/uport",
            "https://rinkeby.etherscan.io",
            "https://api.uport.me/sensui/fund/",
            "https://api.uport.me/sensui/relay/",
            "0xda8c6dce9e9a85e6f9df7b09b2354da44cb48331")

    /**
     * a mapping between the ethereum network identifier and the related endpoints and metadata
     */
    private val NETWORK_CONFIG = mapOf(
            mainnetId to mainnet,
            ropstenId to ropsten,
            kovanId to kovan,
            rinkebyId to rinkeby
    ).toMutableMap()

    fun registerNetwork(networkId: String, network: EthNetwork) {
        val normalizedId = cleanId(networkId)

        //TODO: check if [network] has necessary fields
        NETWORK_CONFIG[normalizedId] = network
    }

    /**
     * Gets an [EthNetwork] based on a [networkId]
     */
    fun get(networkId: String): EthNetwork {
        val cleanNetId = cleanId(networkId)
        return NETWORK_CONFIG[cleanNetId]
                ?: NETWORK_CONFIG[networkId]
                ?: throw IllegalStateException("network [$networkId] not configured")
    }

    private fun cleanId(id: String) = id.clean0xPrefix().trimStart('0').prepend0xPrefix()

}

/**
 * A class that encapsulates the endpoints and metadata related to a particular ETH network
 */
data class EthNetwork(
        val name: String,           //  ex: "kovan"
        val networkId: String,     //  ex: "0x2a"
        val registry: String,       //  ex: MNID.encode({address: '0x5f8e9351dc2d238fb878b6ae43aa740d62fc9758', network: '0x2a'})
        val rpcUrl: String,         //  ex: "https://kovan.infura.io/uport"
        val explorerUrl: String,    //  ex: "https://kovan.etherscan.io"
        val faucetUrl: String,      //  ex: "https://sensui.uport.me/api/v1/fund/"
        val relayUrl: String,        //  ex: "https://sensui.uport.me/api/v2/relay/"
        val txRelayAddress: String
)



