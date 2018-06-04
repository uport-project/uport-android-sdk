# uPort Android SDK

Identity for your Android dApps.

This is a preview version of the uPort android SDK.
Many intended features are still missing, stay tuned.

### Installation

This SDK is currently being distributed using [jitpack](https://jitpack.io/)

In your main `build.gradle` file, add:

```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
        ...
    }
}
```

In your application `build.gradle`:
```groovy
def uport_sdk_version = "v0.0.1"
dependencies {
    ...
    // core SDK
    implementation "com.github.uport-project.uport-android-sdk:sdk:$uport_sdk_version"
    // required for meta transaction fueling
    implementation "com.github.uport-project.uport-android-sdk:fuelingservice:$uport_sdk_version"
}
```

### Usage

This preview version requires that the SDK be configured with a functional `IFuelTokenProvider`
There is a `FuelTokenProvider` implementation provided in the `fuelingservice` library

##### Configure uPort in your Application class

```kotlin

override fun onCreate() {
    
    val config = Uport.Configuration()
                    .setApplicationContext(this)
                    .setFuelTokenProvider( FuelTokenProvider(this, "<your dApp MNID>"))
    
    Uport.initialize(config)
}

```

##### defaultAccount

This preview version of the SDK allows creation of a single account
that can be accessed by the nullable `defaultAccount` field in the `Uport` object.

```kotlin

Uport.defaultAccount?.address // Returns the mnid address of the default account
Uport.defaultAccount?.proxyAddress // Returns the hex address of the default account
Uport.defaultAccount?.network // Returns the network id of the default account

//returns the ETH balance of the deviceAddress (measured in wei)
Uport.defaultAccount?.getBalance() { err, balance ->
    // do something with balance or respond to err
}

//or as a coroutine:
val balanceInWei = Uport.defaultAccount?.getBalance()

```

##### accountCreation

```kotlin

if (Uport.defaultAccount == null) {
    
    Uport.createAccount(network = Networks.rinkeby) { err, account ->
            // update UI to reflect the existence of a defaultAccount
    }
    
}
```

In case the app gets killed during the account creation process, the `createAccount` method will try to resume the process where it left off.
It can be instructed to start from scratch, but that may cost additional fuel.

##### ethereum interaction

uPort SDK lets you create, sign, and submit Ethereum transactions on behalf of your users.

This preview uses [metaTransactions](https://medium.com/uport/making-uport-smart-contracts-smarter-part-3-fixing-user-experience-with-meta-transactions-105209ed43e0) for `defaultAccount`


```kotlin
//send value
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
val data = <ABI encoded method call>

val txHash = Uport.defaultAccount?.send(activity, contractAddress, data)
val receipt = Networks.rinkeby.awaitConfirmation(txHash)

```


### Dependencies

This library uses [kethereum](https://github.com/walleth/kethereum) for a lot of ethereum related work.

The smart-contract encoding is generated using [bivrost-kotlin](https://github.com/gnosis/bivrost-kotlin)

Private key management is done using [uport-android-signer](https://github.com/uport-project/uport-android-signer)

Currently there is a transient dependency on [spongycastle](https://rtyley.github.io/spongycastle/)
but that may be removed when pure kotlin implementations of the required cryptographic primitives become available. 
