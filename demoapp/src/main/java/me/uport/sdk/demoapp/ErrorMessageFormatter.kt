package me.uport.sdk.demoapp


internal fun formatExecption(err: Exception): String {

    if (err != null) {
        return "Error : $err"
    }

    return err
}
