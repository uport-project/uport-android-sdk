## Changelog

* 0.5.1
    * [support] change version naming, remove `v` prefix

* v0.5.0
    * [breaking][bugfix] align JWT signature to spec (#93)
    * [breaking][support] externalized did-jwt and signer modules (#97)
    * [breaking][feature]`Account` is now an interface and the default implementation used is `HDAccount` (#89)
    * [feature] easier configuration of JsonRPC endpoints (#91)
    * [feature] add `verifyDisclosure()` method (#96)
    * [feature] add `authenticateDisclosureResponse()` (#98)
    * [feature] add W3C methods to create Verifiable Credential and Presentation (#100)
    * [support] reduce UI test flakyness (#92)

* v0.4.2
    * updated infura JsonRPC endpoint URLs.
    To avoid sudden disconnect you are strongly encouraged to use your own project on
    infura or to use a dedicated JsonRPC endpoint for your use case.
    
* v0.4.1
    * maintenance release

* v0.4.0
    * `core` module is a simple java library (and can be used as a dependency in JVM tests)
    * [breaking] `AccountCreator` interface methods are now using suspend instead of callbacks.
    * [breaking] The callback variant of `createAccount` from the Uport object has been deprecated.
    * [breaking] all available `JsonRPC` methods are suspend methods and are full members of the class
    (and can now be easily mocked). The callback variants no longer exist.
    * `network_id` field in `EthNetwork` was renamed to `networkId`
    * add `Transports.sendExpectingResult()` functionality
    * add `createPersonalSignRequest`, `createVerificationSignatureRequest`, `createEthereumTransactionRequest`
 convenience methods to `Credentials` to interact with the uPort app
    * extensive refactoring of tests with better mocks and ability to run offline
    * also publishing `sources.jar` for each module

* v0.3.2
    * JWT timestamps are checked with allowance for clock drift
    * JWT verification is more explicit about failures
    * added method to extract `shareResp` JWTs from callback URIs
    * expose `getDID()` method on `Account` objects
    
* v0.3.1
    * add https DID resolver
    * use UniversalDID for JWT verification
    * add encryption/decryption functionality
    
* v0.3.0
    * add universal DID resolver
    * add cleaner way of creating JWTs with abstracted signer
    * updated to kethereum 0.63 which has a different key derivation and mnemonic API.
        If you're using an older version in parallel, you need to update as well. 

* v0.2.2
    * update of dependencies for coroutines and build tools

* v0.2.1
    * bugfix: crash when decrypting fingerprint protected seed

* v0.2.0
    * add `:ethr-did` module with support for [resolving `ethr-did`s](https://github.com/uport-project/ethr-did-resolver) 
    * move [uport-android-signer](https://github.com/uport-project/uport-android-signer) into this SDK as `:signer` module
    * allow multiple root accounts
    * add option to delete an account

* v0.1.1
    * add option to import seeds phrases as account
    * bugfix: default account is updated on first creation 

* v0.1.0
    * default account type is `KeyPair`
    * updated kethereum to 0.53 , some APIs have changed to extension functions
    * updated uport-android-signer - allows minSDK to be 21
    * renamed `Uport.defaultAccount?.proxyAddress` to `publicAddress`
    
* v0.0.2
    * add coroutine support for account creation
    * add getAddress to Account objects
    
* v0.0.1
    * initial release