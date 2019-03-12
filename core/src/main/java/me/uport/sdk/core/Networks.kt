@file:Suppress("unused")

package me.uport.sdk.core

import me.uport.mnid.MNID
import org.walleth.khex.clean0xPrefix
import org.walleth.khex.prepend0xPrefix

/**
 * Convenience singleton that holds URLs and addresses for different eth networks.
 *
 * use the `registerNetwork` method to override defaults
 */
object Networks {

    private const val defaultFaucetUrl = "https://sensui.uport.me/api/v1/fund/"
    private const val defaultTxRelayUrl = "https://sensui.uport.me/api/v2/relay/"
    private const val mainnetId = "0x1"
    private const val ropstenId = "0x3"
    private const val rinkebyId = "0x4"
    private const val kovanId = "0x2a"
    private val NETWORK_CONFIG = emptyMap<String, EthNetwork>().toMutableMap()

    init {
        registerNetwork(EthNetwork(
                name = "mainnet",
                networkId = mainnetId,
                uPortRegistry = MNID.encode(mainnetId, "0xab5c8051b9a1df1aab0149f8b0630848b7ecabf6"),
                rpcUrl = "https://mainnet.infura.io/uport",
                explorerUrl = "https://etherscan.io",
                faucetUrl = defaultFaucetUrl,
                relayUrl = defaultTxRelayUrl,
                txRelayAddress = "0xec2642cd5a47fd5cca2a8a280c3b5f88828aa578"))
        registerNetwork(EthNetwork(
                name = "rinkeby",
                networkId = rinkebyId,
                uPortRegistry = MNID.encode(rinkebyId, "0x2cc31912b2b0f3075a87b3640923d45a26cef3ee"),
                rpcUrl = "https://rinkeby.infura.io/uport",
                explorerUrl = "https://rinkeby.etherscan.io",
                faucetUrl = "https://api.uport.me/sensui/fund/",
                relayUrl = "https://api.uport.me/sensui/relay/",
                txRelayAddress = "0xda8c6dce9e9a85e6f9df7b09b2354da44cb48331"))
        registerNetwork(EthNetwork(
                name = "ropsten",
                networkId = ropstenId,
                uPortRegistry = MNID.encode(ropstenId, "0x41566e3a081f5032bdcad470adb797635ddfe1f0"),
                rpcUrl = "https://ropsten.infura.io/uport",
                explorerUrl = "https://ropsten.io",
                faucetUrl = defaultFaucetUrl,
                relayUrl = defaultTxRelayUrl,
                txRelayAddress = "0xa5e04cf2942868f5a66b9f7db790b8ab662039d5"))
        registerNetwork(EthNetwork(
                name = "kovan",
                networkId = kovanId,
                uPortRegistry = MNID.encode(kovanId, "0x5f8e9351dc2d238fb878b6ae43aa740d62fc9758"),
                rpcUrl = "https://kovan.infura.io/uport",
                explorerUrl = "https://kovan.etherscan.io",
                faucetUrl = defaultFaucetUrl,
                relayUrl = defaultTxRelayUrl,
                txRelayAddress = "0xa9235151d3afa7912e9091ab76a36cbabe219a0c"))
    }

    /**
     * shorthand for getting the configuration for ETH mainnet (equivalent with Networks.get("0x1"))
     */
    val mainnet
        get() = get(mainnetId)

    /**
     * shorthand for getting the configuration for rinkeby test network (equivalent with Networks.get("0x4"))
     */
    val rinkeby
        get() = get(rinkebyId)

    /**
     * shorthand for getting the configuration for the ropsten test network (equivalent with Networks.get("0x3"))
     */
    val ropsten
        get() = get(ropstenId)

    /**
     * shorthand for getting the configuration for the kovan test network (equivalent with Networks.get("0x2a"))
     */
    val kovan
        get() = get(kovanId)

    /**
     * Register an ETH network configuration.
     * This overrides any previously registered network with the same `networkId`
     */
    fun registerNetwork(network: EthNetwork) {
        val normalizedId = cleanId(network.networkId)

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
        /**
         * Name of the network
         */
        val name: String,           //  ex: "kovan"

        /**
         * network ID - hex encoded number or first 4 bytes of genesis block hash
         */
        val networkId: String,     //  ex: "0x2a"

        /**
         * Json RPC endpoint to be used with this network.
         * For public networks this defaults to the infura URLs
         */
        val rpcUrl: String,         //  ex: "https://kovan.infura.io/uport"

        /**
         * Block explorer URL.
         * For public networks, this defaults to etherscan
         */
        val explorerUrl: String = "",    //  ex: "https://kovan.etherscan.io"

        /**
         * MNID encoded contract address for the `did:uport:` registry.
         * This is used by the uPort DID resolver to fetch the location of the DID document
         * for a particular uPort DID.
         *
         * **NOTE** uPort DIDs are being deprecated in favor of ethr DID
         */
        val uPortRegistry: String,       //  ex: MNID.encode({address: '0x5f8e9351dc2d238fb878b6ae43aa740d62fc9758', network: '0x2a'})

        /**
         * metaTX faucet URL
         */
        @Deprecated("uPort proxy contracts and meta TX functionality is not supported")
        val faucetUrl: String = "",      //  ex: "https://sensui.uport.me/api/v1/fund/"

        /**
         * transaction relay URL
         */
        @Deprecated("uPort proxy contracts and meta TX functionality is not supported")
        val relayUrl: String = "",        //  ex: "https://sensui.uport.me/api/v2/relay/"

        /**
         * transaction relay contract address
         */
        @Deprecated("uPort proxy contracts and meta TX functionality is not supported")
        val txRelayAddress: String = ""
)



