---
title: "uPort Android SDK"
index: 1
category: "android-sdk"
type: "content"
---

# uPort Android SDK

Identity for your Android dApps.

[![](https://jitpack.io/v/uport-project/uport-android-sdk.svg)](https://jitpack.io/#uport-project/uport-android-sdk)
[![CircleCI](https://circleci.com/gh/uport-project/uport-android-sdk.svg?style=svg)](https://circleci.com/gh/uport-project/uport-android-sdk)
[![Twitter Follow](https://img.shields.io/twitter/follow/uport_me.svg?style=social&label=Follow)](https://twitter.com/uport_me)

**This is a preview version of the uPort android SDK.
Many intended features are still missing, and the ones already present are under heavy development.
Expect some breaking changes**

Development planning is done at https://www.pivotaltracker.com/n/projects/2198688

## Usage


### Configure uPort in your Application class

```kotlin

override fun onCreate() {
    
    val config = Uport.Configuration()
                    .setApplicationContext(this)
    
    Uport.initialize(config)
}

```

### defaultAccount

This preview version of the SDK has the concept of `defaultAccount` as a nullable field in the
`Uport` object.
If there is no default account when a new one is created, it becomes the default.

```kotlin

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

In case the app gets killed during the account creation process, the `createAccount` method will
try to resume the process where it left off. It can be instructed to start from scratch, but that
may cost additional fuel.

### Account management

`Account` objects have a `handle` field that can be used to refer to them in the future.
The handle right now is an ethereum address but it should be treated as an opaque string,
as it will change in a future release. You should not send funds to that address.

### Key management

The `signer` library is used to create and manage keys for uport accounts.
Read full details in [key management documentation](./docs/overview/key_management.md)

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
val data : ByteArray = `<ABI encoded contract method call>`

val txHash : String = Uport.defaultAccount?.send(activity, contractAddress, data)
val receipt = Networks.rinkeby.awaitConfirmation(txHash)

```

### off-chain interaction

Off-chain interaction is essentially signing and verifying JWTs using specific JWT algorithms.
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
val publicKey = Crypto.getEncryptionPublicKey(privateKeyBytes)

//encrypt a message with an intended recipient
val encryptedBundle = Crypto.encrypt("hello world", recipientPublicKeyBase64)
val serializedMessage = encryptedBundle.toJson()

//decrypt a message
val receivedBundle = EncryptedMessage.fromJson(serializedMessage)
val decryptedMessage = Crypto.decrypt(receivedBundle, recipientSecretKey)
```

## Installation

This SDK is available through [jitpack](https://jitpack.io/)

In your main `build.gradle` file, add:

```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
        //...
    }
}
```

In your application `build.gradle` file, add:

```groovy
def uport_sdk_version = "0.5.1"
dependencies {
    //...
    // core SDK
    implementation "com.github.uport-project.uport-android-sdk:sdk:$uport_sdk_version"
}
```

The `sdk` artifact transitively includes and exposes the other libraries produced by this repository.
These libraries can be used independently as well:

```groovy
dependencies {
    //...
    
    // definitions and implementations for various Account types
    implementation "com.github.uport-project.uport-android-sdk:identity:$uport_sdk_version"
    
    // creates and verifies Verifiable Credentials, Presentations as well as several uport-specific JWT types
    implementation "com.github.uport-project.uport-android-sdk:credentials:$uport_sdk_version"
    
    // message encryption and communication with the uPort app and browser dApps through deep-links
    implementation "com.github.uport-project.uport-android-sdk:transport:$uport_sdk_version"
}
```

## Dependencies

* These libraries use [KEthereum](https://github.com/komputing/KEthereum) for a lot of the ethereum related work.
* The smart-contract binding code is generated using [bivrost-kotlin](https://github.com/gnosis/bivrost-kotlin)
* The off-chain/JWT interactions rely on [kotlin-did-jwt](https://github.com/uport-project/kotlin-did-jwt)
* Protected Key management is done by [uport-android-signer](https://github.com/uport-project/uport-android-signer) 


Currently there is a transient dependency on [spongycastle](https://rtyley.github.io/spongycastle/)
but that may be removed when pure kotlin implementations of the required cryptographic
primitives become available. 


## Contributing
Want to contribute to uport-android-sdk? Cool, please read our
[contribution guidelines](./docs/guides/CONTRIBUTING.md) to get an understanding of the process
we use for making changes to this repo.


## Changelog

see [CHANGELOG.md](./CHANGELOG.md)
