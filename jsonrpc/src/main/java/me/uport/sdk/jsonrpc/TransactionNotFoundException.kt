package me.uport.sdk.jsonrpc

class TransactionNotFoundException(txHash: String) : RuntimeException("The transaction with hash=$txHash has not been mined yet")