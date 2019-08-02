package me.uport.sdk.identity

/**
 * This encapsulates types of [Account]s with regard to the algorithms used to sign transactions.
 *
 */
enum class AccountType {

    /**
     * This [Account] is backed by a key pair that resides in memory.
     * Transactions are signed directly by this key pair.
     */
    KeyPair,

    /**
     * This [Account] is backed by a key pair derived from a seed.
     * Transactions are signed directly by this derived key pair.
     */
    HDKeyPair,

    /**
     * This [Account] is backed by a proxy contract that is controlled
     * through a MetaIdentityManager, supporting meta-transactions.
     * The signer used by this Account needs to wrap a key pair signer.
     * Transactions are signed by a key that is authorized to control the proxy and
     * the check is made by the meta-identity-manager contract.
     */
    MetaIdentityManager,

    /**
     * This Account is backed by a Proxy contract.
     * This is not directly used.
     * Please use [MetaIdentityManager] instead.
     */
    Proxy,

    /**
     * This [Account] is backed by a key pair that is only present on this device.
     * This is deprecated, please use a [KeyPair]
     */
    @Deprecated("please use KeyPair instead", ReplaceWith("KeyPair"))
    Device,

    /**
     * This [Account] is backed by a proxy contract that is controlled
     * through an IdentityManager contract.
     * The signer used by this Account needs to wrap a key pair signer.
     * Transactions are signed by a key that is authorized to control the proxy and
     * the check is made by the meta-identity-manager contract.
     *
     * This is deprecated, please use a [MetaIdentityManager]
     */
    @Deprecated("please use MetaIdentityManager instead", ReplaceWith("MetaIdentityManager"))
    IdentityManager,
}
