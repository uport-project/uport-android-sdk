package me.uport.sdk.demoapp


internal fun formatException(err: Exception?): String? {

    if (err != null) {
        return "Error : $err"
    }

    return err
}
