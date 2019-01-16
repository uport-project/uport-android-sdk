---
title: "uPort Android SDK"
index: 1
category: "android-sdk"
type: "content"
---

# uPort Android SDK

Identity for your Android dApps.

**This is a preview version of the uPort android SDK.
Many intended features are still missing, and the ones already present are under heavy development.
Expect some breaking changes**

Development planning is done at https://www.pivotaltracker.com/n/projects/2198688

## Installation

This SDK is available through [jitpack](https://jitpack.io/)

[![](https://jitpack.io/v/uport-project/uport-android-sdk.svg)](https://jitpack.io/#uport-project/uport-android-sdk)

In your main `build.gradle` file, add:

```
groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
        //...
    }
}
```

In your application `build.gradle` file, add:

```
groovy
def uport_sdk_version = "v0.3.2"
dependencies {
    //...
    // core SDK
    implementation "com.github.uport-project.uport-android-sdk:sdk:$uport_sdk_version"
}
```

## Usage


### Configure uPort in your Application class

```
kotlin

override fun onCreate() {
    
    val config = Uport.Configuration()
                    .setApplicationContext(this)
    
    Uport.initialize(config)
}

```

### defaultAccount

This preview version of the SDK has the concept of `defaultAccount` as a nullable field in the `Uport` object.
If there is no default account when a new one is created, it becomes the default.

```
kotlin

Uport.defaultAccount?.address // Returns the mnid address of the default account
Uport.defaultAccount?.publicAddress // Returns the hex address of the default account
Uport.defaultAccount?.network // Returns the network id of the default account

//returns the ETH balance of the deviceAddress (measured in wei)
Uport.defaultAccount?.getBalance() { err, balance ->
    // do something with balance or respond to err
}

//or as a coroutine:
val balanceInWei = Uport.defaultAccount?.getBalance()

```

### Account Creation

```kotlin

if (Uport.defaultAccount == null) {
    
    Uport.createAccount(network = Networks.rinkeby) { err, account ->
            // update UI to reflect the existence of a defaultAccount
    }
    
}
```

In case the app gets killed during the account creation process, the `createAccount` method will try to resume the process where it left off.
It can be instructed to start from scratch, but that may cost additional fuel.

### Account management

`Account` objects have a `handle` field that can be used to refer to them in the future.
The handle right now is an ethereum address but it should be treated as an opaque string, as it will change in a future release.
You should not send funds to that address.

### Ethereum interaction

uPort SDK lets you create, sign, and submit Ethereum transactions.

This SDK preview version uses simple KeyPair accounts which require self-funding.
There is also support for [proxy-contract accounts](https://github.com/uport-project/uport-identity)
 and [metaTransactions](https://medium.com/uport/making-uport-smart-contracts-smarter-part-3-fixing-user-experience-with-meta-transactions-105209ed43e0)
 but it is not used by default any more.


```kotlin
//transfer value
val destination: String = "0x010101...."
val amountInWei = BigInteger.valueOf(1_000_000_000)

Uport.defaultAccount?.send(activity, destination, amountInWei) { err, txHash ->
  // Update UI to indicate that transaction has been sent and is confirming
  Networks.rinkeby.awaitConfirmation(txHash) { err, receipt ->
    // Complete operation in UX
  }
}


//`send` can also be used in coroutines

//call contract
val contractAddress = "0x010101..."
val data : ByteArray = <ABI encoded contract method call>

val txHash : String = Uport.defaultAccount?.send(activity, contractAddress, data)
val receipt = Networks.rinkeby.awaitConfirmation(txHash)

```

### off-chain interaction

Off-chain interaction is essentially signing and verifying JWTs using uport-specific JWT algorithms.
Verification of such tokens implies resolving a 
[Decentralized Identity (DID) document](https://github.com/uport-project/specs/blob/develop/pki/diddocument.md)
that will contain the keys or address that should match a JWT signature.
To obtain a `DIDDocument` one needs to use a `DIDResolver`.


The `UniversalDID` is a global registry of `DIDResolver`s for apps using the SDK.
During SDK initialization this registry gets populated with default resolvers for
[uport-did](https://github.com/uport-project/uport-did-resolver),
[ethr-did](https://github.com/uport-project/ethr-did-resolver)
and [https-did](https://github.com/uport-project/https-did-resolver)
You can register your own resolver(s) using `UniversalDID.registerResolver(resolver)`
Registering a new resolver that resolves the same DID method will override the previous one.

These `DIDDocument`s are used during verification of compatible JWT tokens.

#### verify a JWT token

```kotlin

//in a coroutine context:
val tokenPayload : JWTPayload? = JWTTools().verify(token)

if (tokenPayload != null) {
    //verified
} else {
    //token cannot be verified
}

```


#### create a JWT token

```kotlin

val payload = mapOf( "claims" to mapOf( "name" to "R Daneel Olivaw" ) )

val signer = KPSigner("0x1234")
val issuer = "did:ethr:0x${signer.getAddress()}"

//in a coroutine context
val jwt : String = JWTTools().create(payload, issuer, signer)

```

### Encrypted messaging

```kotlin
//compute an encryption publicKey starting from a private key (can be an ethereum private key) 
val publicKey = Crypto.getEncryptionPublicKey(privateKeyBytes).

//encrypt a message with an intended recipient
val encryptedBundle = Crypto.encrypt("hello world", recipientPublicKeyBase64)
val serializedMessage = encryptedBundle.toJson()

//decrypt a message
val receivedBundle = EncryptedMessage.fromJson(serializedMessage)
val decryptedMessage = Crypto.decrypt(receivedBundle, recipientSecretKey)
```



## Dependencies

This library uses [kethereum](https://github.com/walleth/kethereum) for a lot of ethereum related work.

The smart-contract binding code is generated using [bivrost-kotlin](https://github.com/gnosis/bivrost-kotlin)

Currently there is a transient dependency on [spongycastle](https://rtyley.github.io/spongycastle/)
but that may be removed when pure kotlin implementations of the required cryptographic primitives become available. 


## Changelog

* 0.3.2
    * JWT timestamps are checked with allowance for clock drift
    * JWT verification is more explicit about failures
    * added method to extract `shareResp` JWTs from callback URIs
    * expose `getDID()` method on `Account` objects
    
* 0.3.1
    * add https DID resolver
    * use UniversalDID for JWT verification
    * add encryption/decryption functionality
    
* 0.3.0
    * add universal DID resolver
    * add cleaner way of creating JWTs with abstracted signer
    * updated to kethereum 0.63 which has a different key derivation and mnemonic API.
        If you're using an older version in parallel, you need to update as well. 

* 0.2.2
    * update of dependencies for coroutines and build tools

* 0.2.1
    * bugfix: crash when decrypting fingerprint protected seed

* 0.2.0
    * add `:ethr-did` module with support for [resolving `ethr-did`s](https://github.com/uport-project/ethr-did-resolver) 
    * move [uport-android-signer](https://github.com/uport-project/uport-android-signer) into this SDK as `:signer` module
    * allow multiple root accounts
    * add option to delete an account

* 0.1.1
    * add option to import seeds phrases as account
    * bugfix: default account is updated on first creation 

* 0.1.0
    * default account type is `KeyPair`
    * updated kethereum to 0.53 , some APIs have changed to extension functions
    * updated uport-android-signer - allows minSDK to be 21
    * renamed `Uport.defaultAccount?.proxyAddress` to `publicAddress`
    
* 0.0.2
    * add coroutine support for account creation
    * add getAddress to Account objects
    
* 0.0.1
    * initial release
