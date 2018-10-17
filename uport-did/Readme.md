DID resolver - rough alpha
==========================

This library provides a way to lookup a DID document for a given MNID

The basic steps to resolve a DID are:
* decodes the MNID to get the `network` and `address`
* obtains the (hardcoded) registry contract_address by `network`
* `eth_call`s the registry contract with the `address` to get the IPFS hash
* gets the Document from IPFS and tries to decode it into a `DDO` object

The document it obtains is described [here](https://github.com/uport-project/specs/blob/develop/pki/identitydocument.md)

TL;DR it looks like this:
```json
{
  "@context":"http://schema.org",
  "@type":"Person",
  "publicKey":"0x04613bb3a4874d2703...",
  "publicEncKey":"QCFPBLm..."
}
```

### Usage

```kotlin
val mnid = "2ozs2ntCXceKkAQKX4c9xp2zPS8pvkJhVqC"
[...]
//in a background thread:
document = DIDResolver().getProfileDocumentSync(mnid)

// on main thread:
DIDResolver().getProfileDocument(mnid, { err, document ->
//do something with document
})
```

### NOTE

This work is very early and about to be changed a lot.
Expect breaking changes between releases.