package com.uport.sdk.signer

import assertk.assert
import assertk.assertions.isEqualTo
import kotlinx.coroutines.runBlocking
import me.uport.sdk.core.decodeBase64
import org.junit.Test
import org.kethereum.extensions.hexToBigInteger
import org.kethereum.model.SignatureData

class SignatureTests {

    @Test
    fun `signature format matches`() = runBlocking {

        val refJwtMessage = "ZXlKMGVYQWlPaUpLVjFRaUxDSmhiR2NpT2lKRlV6STFOa3NpZlEuZXlKcGMzTWlPaUl6TkhkcWMzaDNkbVIxWVc1dk4wNUdRemgxYWs1S2JrWnFZbUZqWjFsbFYwRTRiU0lzSW1saGRDSTZNVFE0TlRNeU1URXpNeXdpWTJ4aGFXMXpJanA3SW01aGJXVWlPaUpDYjJJaWZTd2laWGh3SWpveE5EZzFOREEzTlRNemZR"
        val refTxData = "9oCFC6Q7dACDL+/YlJ4gaMziLeTh6A8Vy3HvQ1ogo7N8iA3gtrOnZAAAiQq83vASNFZ4kByAgA=="

        testData.forEach { ref ->

            val signer = KPSigner(ref.privateKey)
            assert(signer.getAddress()).isEqualTo(ref.address)

            val sig = signer.signJWT(refJwtMessage.decodeBase64())
            assert(sig.getJoseEncoded(true)).isEqualTo(ref.jwtSig)

            val txSig = signer.signETH(refTxData.decodeBase64())
            assert(txSig).isEqualTo(ref.txSig)
        }

    }

    data class Combo(val privateKey: String, val address: String, val jwtSig: String, val txSig: SignatureData)

    private val testData = arrayOf(

            Combo(
                    privateKey = "4e1559a4ec4dff8e2369635fc936fce9281d3044f49b8acaacd935041b6ae785",
                    address = "0xf34e6ddffedec32f759641552634f8b0df9c66a8",
                    jwtSig = "3Br7BEdZwSaX8wNBxBnJvDWDwlfjPhtP3LSR95J7p4QpZ_bhqXIIJIkZdU29a7F-A9CQRkTPVUGCWvGHJ2VUDAE",
                    txSig = SignatureData(v = 27, r = "0x4e2c81b13fabada80ab446f27aee63d2cdd405dd4c8353e16ae59ccf5f05a91e".hexToBigInteger(), s = "0x1fadfafcddfe2466c5f1597acff0a36ac0391f58251160fc2580fe50e4fcce7d".hexToBigInteger())
            ),
            Combo(
                    privateKey = "25692381beee3b4e85f2acf66ada03b10672db2113f91f9df94e90cce5d5a1ad",
                    address = "0xa998c6074b5cc04fcd775303e0f9a61ca535f35e",
                    jwtSig = "2AqNpkt89PyW0mfg80sJZ5EpVVrVS88iAX-VvbWvJIhWXD3o45-9cDl_PxDJaRGYUMQXpNrji4dPLO2rQO8MJQA",
                    txSig = SignatureData(v = 27, r = "0x3d3b145bf1bcfded000705c855903f5c73382430d07cb482b2debe318e3d2236".hexToBigInteger(), s = "0x215c48b00b745e7d81a1cd23e95f2ea1db3ec19b47ecb97005bb6929091585f3".hexToBigInteger())
            ),
            Combo(
                    privateKey = "95b44703405c3a9a2dc24502d22681437390b6b5366548003a4cce8c0f0fe146",
                    address = "0x7824fa28d444c250c0902fefd6c832fa88972db2",
                    jwtSig = "-wYyGYTgY_gSZGwwHg2pdu18qiqMtFh9x4nFPfCdaGhTb_GniZbo6GUb0xs1YvO0pO-fFuq4r5YUYcjbrAmbTAA",
                    txSig = SignatureData(v = 27, r = "0x63ad50820d43370263edd71db5dbe77e99a2cfed94465068f94201c4957956bc".hexToBigInteger(), s = "0x3ece112fad5fca89ba6a45578bae496987909c04cc70a82b8e64b95bc20a5774".hexToBigInteger())
            ),
            Combo(
                    privateKey = "efdd115c0ca8f5501ec03a2c4210343294956c898cd1fa4bae80d3ecc52b23db",
                    address = "0x8ac0e83a1b492bbdfd0da7a1048351a5b066249b",
                    jwtSig = "fcjKhN9m2HagKPb8fh5sMsldOt-GXt4GlfRiIDnhFIvOuWQVhVJyBbrkRbN8SmoeQ2mNkBhHm1PhT3DHWzEorAA",
                    txSig = SignatureData(v = 28, r = "0xcbfbbda3fa85865168b63ad281afba32b4e78a5c7eda4286915e9fe521403d45".hexToBigInteger(), s = "0x1d3c5759c77e46004bcc13d3aae956f456cf15d6546ab4522f2b33c8f4554ea3".hexToBigInteger())
            ),
            Combo(
                    privateKey = "6a5e632c21700b25a99b391ae4ef034e08a7a192914d38b4655dd9058fe498df",
                    address = "0xfb643e22564cb6cfdf7677a610348c172b83bc7b",
                    jwtSig = "CxVQYUF_9radbcDNFCbg3142xZ6YRnlc3c6bVUX3diyzVmQzXL_gLdw-AeryZF3AyOlkf17TOXYS3pIXoW48TAE",
                    txSig = SignatureData(v = 28, r = "0x3f35e463828a480f544eab85e52e96f85cc4f6329e206397756a1ac217c33076".hexToBigInteger(), s = "0x5eceee439b69d1d8e4a381dcabad526840d84bec36fc746e8ae04d5f0d327413".hexToBigInteger())
            ),
            Combo(
                    privateKey = "f93330497fe0d36e8e4fc39795ff8a82d5e4d388adb6c518c2582bfff2dae529",
                    address = "0x4ca3a9e0fd60bcfdd87681978bd6ab90e73b0832",
                    jwtSig = "-JaIQk8QIoUBSgLFpzPR0BLcN852zcB36zg4OKgSs_9lZXV-U5kK1GczXTihHhj8VoowdlSYj2HB58WKfD_uoAE",
                    txSig = SignatureData(v = 28, r = "0x7e6d110cd8254761472939eccbcbffe0cf3585beab38a358b792f218ce7db387".hexToBigInteger(), s = "0x68258a0829dafcf68bd275140a9793fb92e11987756c2cb266f8df556bb7b055".hexToBigInteger())
            ),
            Combo(
                    privateKey = "01060240a4a4f38707482d9a5faf7fef6f9d5a93704598ed43759a4515a7004a",
                    address = "0xf138e67af6783c1e29032dd095f9d836b05fe581",
                    jwtSig = "aLXtuFa1LsRV2JyPckvlEJRbPh2qTIlUVk2dRyIT2JnoV01tFO3MfaMn_uW8ahYW-9wiCemfA-4yJAUB6H93PwA",
                    txSig = SignatureData(v = 28, r = "0x3855e3c71feef2d4c0d70f868e6e293ec21cb53f641479eacdcac9906e8cb358".hexToBigInteger(), s = "0x60492218184b5c5519ad2050605efd12cd59d132258adab9b02a1b4ea73490ef".hexToBigInteger())
            ),
            Combo(
                    privateKey = "4ee10f8179f853314ad5814f9c8a12aa78b5173c93ca7cdd1731740403f413ca",
                    address = "0x69ff45046756ddb98d61f392056b3d04e32d8bd4",
                    jwtSig = "mZspCm8ml4c0wP28H-mpZY0RQby42ZteHsvQ3osXObjGV8XckL_FzFY6leSnpiDF_iCCp390NSyEe7sfGeN3_QA",
                    txSig = SignatureData(v = 27, r = "0xcf3eac72eafedeb56b2ee646c27551cbdd437a652f6b74e38b31baafd764bd67".hexToBigInteger(), s = "0x7f8f899ef5292306a0a39673faccae8267697cc86ce3f397f4c710636d50cdb7".hexToBigInteger())
            ),
            Combo(
                    privateKey = "884c225a67e48935779f390db094818b7c2c0319f55c76cf62eb5b7a4df03f69",
                    address = "0x674ba15dfd54b3e48f7134a246eb33a1202f31e3",
                    jwtSig = "BgFYvqekFZXmit58E6YyTOi7rkz9pXqY6B8Ly1lv-uA3eEAS-ajHbf5O3BHn8kKCTwRX7ObdZE_uACNn5k9-6wE",
                    txSig = SignatureData(v = 28, r = "0xb543c9d8b44868fe6f3cc267ccc6206fbccb72aef2eb03d3d30053ad3009d30d".hexToBigInteger(), s = "0x6724cc6234f2a2dafdde5dcb1a38dace720d623c27681f0a9cb920ee88e88309".hexToBigInteger())
            ),
            Combo(
                    privateKey = "019e0e384311b48e9ccc2750c0bd122e7e3b4db3d5c0a898b216068960f029fe",
                    address = "0xcccbc15856de1c89fc7dd2199e0fd307619224bd",
                    jwtSig = "2_ddtzVB6tRanvkqWWdet7mzr8pf1kOXbAGvYAwS1O-jsE20rM8ezMQAyFEifoB5cObeyZagiphadDyjvFEaEgE",
                    txSig = SignatureData(v = 28, r = "0xedaa5487d69116782df792834025bfc1e25537a218596515b081a6f93460a0bf".hexToBigInteger(), s = "0x7e8f7bc0d58b0dc8d6cfa51cf4bbe1fe587d92e1fab8f003184c3da37a322924".hexToBigInteger())
            ),
            Combo(
                    privateKey = "d003cd7071f4b3fe85256b4fa1046a1df20196b20b37869932dd28b9423e3f99",
                    address = "0x2860b31d01c27ee58b65c9cfa96a66428452209d",
                    jwtSig = "DDN1d4Lk0-Bq1JKs90ToO6f7gw9u9K6m_fUvPT5ENvIIDm_6KI_M0FFSf02YS7-HOl1mvlHwGAyR-ommaCNpFAA",
                    txSig = SignatureData(v = 28, r = "0x19d6f2aa28b5a3292953d476b1b80b7ddae701abaf42b6d50d0be49058638b25".hexToBigInteger(), s = "0x7b7e1daa4f2b2ca41844ac2e24a53398f6621bd212ee565bbe111cd640a26a0f".hexToBigInteger())
            ),
            Combo(
                    privateKey = "20fe2194231bbbd237c94f3ffc55f4a94f536aa191b5cf026eb0d9ff20704371",
                    address = "0x45e7dffad61bb58639de09d8e2f82af7a4b567ce",
                    jwtSig = "sqXseGUZDaoCtRC_IcO90b7zmeTMt9dvgNjx-vptsSg22b1EzS6N-DbaPEZk6z_oM2sEsWbnby-IX9JRI55zHwA",
                    txSig = SignatureData(v = 28, r = "0x30108b61995f50f56c5bb0ed7dae1467ace56a69176c5006249e77e918dc5ab9".hexToBigInteger(), s = "0x1aafefeffc49cc372ccf0ab711d46b85ef222699ef81198d5f3b8d147e9776c7".hexToBigInteger())
            ),
            Combo(
                    privateKey = "2fb2eb84a45c5a6b0f0a69c4868ce0d6deab7ae376cfc88f19b08f920746f69d",
                    address = "0x034d9566428abf22fa1e48a61d6f0f5688108b60",
                    jwtSig = "CFEcsb0s0t8HMkD4hTYoqRdtAcoIgQk0cTIyD__-FQeh6NZuIciyc49e4EcNWxtXkocPc8aZCOKA2JTnbwAZDQE",
                    txSig = SignatureData(v = 28, r = "0x7e732a82558d4eacddb07cb154cd80ab5eb69b63ecd3b2dffbe9ce99607cbad4".hexToBigInteger(), s = "0x5ff236a1e206ff4e1146ab752fa767b253a4b2741d05a7da748d9843a2755a19".hexToBigInteger())
            ),
            Combo(
                    privateKey = "fccc238b12e9c296c0f96d9e48a7dd3c0d4df195f53720d079728b8e1cc28b17",
                    address = "0x51396c5f444b6ef5fc81733a7526d281c083d201",
                    jwtSig = "Y-5l1cPkJElpCS8N5tBMnlSGO4rCKS4L4-Wo0ei9Wvz5_2fER_lMApxm87ROv_hsxWpEeA3lUlgoI61f6kj14gA",
                    txSig = SignatureData(v = 27, r = "0x75d98a10162bc8af835b5361050733dc71d2ea3d89718e74c761bc09e6aaf764".hexToBigInteger(), s = "0x4ece672fc2f5fb78a5828ed66ebb8cb03fb8d9e290e8dc5ce8a55e1dfa84574".hexToBigInteger())
            ),
            Combo(
                    privateKey = "a7fec323144afe1c1773b38df6ba30604d86a85788855c0579fda0b1ab82404e",
                    address = "0xdaa1c0545800dc929dea802d5dfda4023938671d",
                    jwtSig = "O3OPhOHNcHiqZ8lJQL9TsiO7zyOHU7Th3QZlnqtSAqFGglcDNGJJs0pTZBuybMt6tLohgGXCUiawcy68jvHOKAA",
                    txSig = SignatureData(v = 27, r = "0x8cfe724b4d0b389001cbb953300ede523214627cec21f1acac636c39731de29b".hexToBigInteger(), s = "0x7724a066d7aec63dde0fc5a8066d0e1007fa593b5df044c592ebefd37cec4227".hexToBigInteger())
            ),
            Combo(
                    privateKey = "19f99371c390cd8e70a0532c635e47e1355ceffda9c3bc8713d012ed03ec06e8",
                    address = "0x2192896ae7d442b6f5b9b4eb2d62c68b64be9b7c",
                    jwtSig = "lIzyO31ZgS41CTBseyzFA7MKEqi_FIP2MJPg8wyuaVoPYvNwmPCbqTftfyYwv3DjDQKovys2-Wa6KiPiXs1ScAE",
                    txSig = SignatureData(v = 27, r = "0x5b19a30bbd70d8d0732cb6776736a979859e1cd093eba63da0b21262617e6e47".hexToBigInteger(), s = "0x348bcbaa52eb0a9b5f9ae40db4ecbb93d65414fbdc1318b7ec97311688381775".hexToBigInteger())
            ),
            Combo(
                    privateKey = "62ce71798568e1f96495201a364e9670d399d97d8d5178398e3eb423dc0e7561",
                    address = "0x53dda7b3f76353a12c4330419edd002a40f6d264",
                    jwtSig = "SvfCtPcL_-i1760xXWP8AE8BEhEXvCTn6hbawf9kBIHIdiWiqz9uWoEobN5-3ma1Mjem7-zUgOvHV-qQ8ZbuVwE",
                    txSig = SignatureData(v = 27, r = "0xf88674f90b28d1958f92d463f69f1c5f0334bca7f2cb5f402a5eed91ac99d6ea".hexToBigInteger(), s = "0x3dcb3701775738c8761afdbaa846ea14a90a8e16bc1cf11b53fbe3253cefa2ba".hexToBigInteger())
            ),
            Combo(
                    privateKey = "e44c40c7728078ae6339101f81bf5302fc075011abd80ba09f32bd3ed2d06f56",
                    address = "0x33e76ac20f2c2968736a511ab951ca5f1d3c91dc",
                    jwtSig = "OAjf7G2JK2j-KRjL8axKOaQL4sb3z_N2LvsaX7wZewqrkUL9arKTv2is1FF_gsi5yJkHbVjJcT6VwW-NfWRpTwE",
                    txSig = SignatureData(v = 28, r = "0x75368b17f89da28918f8e9e8255d458fe6179d8e6cb1e03023ec791692befa43".hexToBigInteger(), s = "0x5a3fa448450572e5cca045868c5710cbf9abfb5477e17a33841ee899dba838cd".hexToBigInteger())
            ),
            Combo(
                    privateKey = "a8baaee5cc9de9c05e5eca19f24ffb2a02702b6f31cb456138ad641a324b15f1",
                    address = "0xf10c10a877bf48cd6e165e598420d184d545c710",
                    jwtSig = "mXooezXiSupDRnkg04HMu4rxljwa7jnhEhDQPOr3QKacmygDhq0YUQWwyKViyrbhsyYNLQmrOfHe8CaERUhexgA",
                    txSig = SignatureData(v = 27, r = "0x6f5131247445078a700ad20d4f709d0fa3c5d01926108ffa8a418e7201bd5d70".hexToBigInteger(), s = "0x1a9a191fead29cd1e8bd333c4ff419a19ba715f6b123dde04e610b784a707eef".hexToBigInteger())
            ),
            Combo(
                    privateKey = "7f505ebd23dadd2a01e1115be61f4ccb4e6f12f09eb7e9188ee21288f456e577",
                    address = "0xedd67644082494a750f27cd88bbf1a71cf924c97",
                    jwtSig = "kgjSiYtYE3YymfKMiZywD_c_nMRHhiyL-gTpoNXsaq2nNFuueIiWRwAYY1X2ZHl8mBMr__Ume_mQT-MAvaQSywA",
                    txSig = SignatureData(v = 27, r = "0x63a9677f3515ec3fafa712d6beb7ca394e9e17800b5f7b0ef97397b3af4583b2".hexToBigInteger(), s = "0x7dcac086cb224f70728e4ec45b45f395e03e521804d7806b1ea81e85063f3d68".hexToBigInteger())
            ),
            Combo(
                    privateKey = "ee97e7658fbfc07c5bfb57fc5a14f6f6c97d6ac8e3f341c999883175c20fcf05",
                    address = "0xacc258d24cf3bcce696970d82704b13651aa5110",
                    jwtSig = "gTskEAgYVe_pBEG2U9jA7U82gxIZLGxLV__m46KZO-fo-RIeEguk4kSeJft5MjuZXhyLkLxp3IDpihM8m99hwAE",
                    txSig = SignatureData(v = 27, r = "0x52211fd7673e9cec76ab02000bcbb9c27c34733c5c07fb0f3b6d7966dca94fea".hexToBigInteger(), s = "0x316310a4700aab7f43b1b12c0750066fe8439cdd6013c6dc0b67ce0e3688de4e".hexToBigInteger())
            ),
            Combo(
                    privateKey = "be9d4fb5c681f75c55243b81bdcbf30ecc2d7e1b8c7a34195fb9775ab76fc2ef",
                    address = "0x4c17ca8cc38ed5c6828085004d98dac7c1a88ca9",
                    jwtSig = "CcZq5_7Yv1UHJ_IkERzDL8hwDxZNd2hIAuQckvkuQWVFh9o8N4-aVxU5jLFGbMR4eNNhHwl4Q0PgeAads4sWOwE",
                    txSig = SignatureData(v = 28, r = "0x14dcf1690f676539e564e8d8d2aca652fbcb2a177c832ea526e8fb6f02240c52".hexToBigInteger(), s = "0x28cd38406e48d11cea6f997d7fbfac8facdb47f3ee8ae6ce06d4d6120da60fe0".hexToBigInteger())
            ),
            Combo(
                    privateKey = "3b0c49416d5ec9517e7870ab6566ac011da5ea5f011ddcff8e15d7cd0cd87328",
                    address = "0x5e120e24884dcd7f575a284ca3e9cbf9db70fd19",
                    jwtSig = "foeD4MInXaqrQFwFS8fu0e8Od-2wQ6YNRNAtHcmTg3YnOgXtwpZ9b50TskVdVh4B6ortcz2fenVHHET6Deo7-QE",
                    txSig = SignatureData(v = 27, r = "0x3b752a6d51a97b4e860eb04cd4a30ed40b1c8b686ad2e707379329cd91c3b9fd".hexToBigInteger(), s = "0x186752e5fbe99427164eecdf419e2ce2cc09bbd873b4f679270ce7f9ae54c3db".hexToBigInteger())
            ),
            Combo(
                    privateKey = "0565790c4d0915c167c089ddd63cdf3016f1651d97bd52541494cd7834e7a739",
                    address = "0xf087ac18893f337cc6c4f3392ac02cf36ed29b21",
                    jwtSig = "6vqOMUbEvEohSHucATLZOClqtR5XbfSVAvcO77ryoezGHKA51TtYAwFuP-CYjIBiCrN2BqiDCat9BEYpW-WoLgE",
                    txSig = SignatureData(v = 27, r = "0x178f3c0163c3e2cadf8a689a17aa1e7057ebc12017c1120c6c33410daa45ad58".hexToBigInteger(), s = "0x33f9ed16853fbe17cb3451ab654845c1df9627e010be5036ee6338b55a95758a".hexToBigInteger())
            ),
            Combo(
                    privateKey = "c8da839ad57a613d97c5e374d621f70b7b78902492da70f3ac271fe27d5652f2",
                    address = "0x7ba6f7ec98a081e00a4b4ea6d4608600a2140860",
                    jwtSig = "UVWK8Fj0_UCYOX5k054VDieDqjNyMEDqU9z7sR1pBlEkcnNHSPTy-_yERivs256JF6agLvUpkbEft8bM9SWRTAA",
                    txSig = SignatureData(v = 27, r = "0xdbe1785b2a44cbf40b6ad177b015b8ce655669d0778393f97f487951e18afc59".hexToBigInteger(), s = "0x1f523e375fd7dc1be2ac81c38526820dc830b2ef675fb623118db76e176b50e9".hexToBigInteger())
            ),
            Combo(
                    privateKey = "c995eab5513262c2822e15920b9064376d1b33f01b125e0d7093e7351e366d5e",
                    address = "0x5a9d02fccee8b2507820889c6a25907592b2352e",
                    jwtSig = "UMku-ulQQMyhJIsKfyTxIEe7_HV5k7IApDl8VtuPnnktbmoqFVdIbMNFarr15GYt-IoA125oQLP9Bo23OU_q6gA",
                    txSig = SignatureData(v = 27, r = "0x70a5309837a51d3729642736152249a302ab44362914db524aa2f6938e951f42".hexToBigInteger(), s = "0x3f713c016cc77147aef275e6065cda248a265adb8865cdce54cf6a9b15093921".hexToBigInteger())
            ),
            Combo(
                    privateKey = "f0176976a08750d2ad33eb46f339511b1f23990c2b5a8c59574fa75bb3bc9aad",
                    address = "0x94c43da2c7fb4986925ce2a3cc2adb4be42457de",
                    jwtSig = "CTlw7KDH6Pwt1mAeNbR1ePaQ06dv96NreO5Es7b4L8L4TP31vsupZWsiBSeOQ6m71TapkgyTOgpavNXF1wbYAAA",
                    txSig = SignatureData(v = 28, r = "0xd463b31c71e24192241c2a7ef84e550d0fa07e9c4716fe58b4891c9648541f87".hexToBigInteger(), s = "0x5ea77ea6d57dfd0a681eff19fe2af99950ac701616bc911efc4e004bd9d413f1".hexToBigInteger())
            ),
            Combo(
                    privateKey = "313bc6445765165aca25f412475f3321842c270d9400f585d59bb8ed23167c1e",
                    address = "0xd07eb4161cd20716e7756d37d6ac0d2e851ea3dd",
                    jwtSig = "vQTFP2H24XMthwRsd80Hxh_7GnaGeLAGWSUw0pEz3SfSaYbtdGUOefXEsdEBrvYXVSseQQr1VIGUc_hZ8j6rPAA",
                    txSig = SignatureData(v = 28, r = "0xc803f137c8fca3fa681d7fd34e02319bd1a059109d5fe0f9d3bc369c3586c9f4".hexToBigInteger(), s = "0x7c15dd8513ba5f6c1125522ff5c6f0945805936d9787657ece73828cc6fc9c22".hexToBigInteger())
            ),
            Combo(
                    privateKey = "035d88da05943288a90581ddca5a3dacb068beb60602efa26ab61f91b202b098",
                    address = "0x47221e33c828259edbebf4cde1e57bf09224fe7b",
                    jwtSig = "npBfIb-pGeG_JrEXjmDJrBWMa7SroSoPBqbAsuHC-yZjFFOBNuhoOc6nAJzRvtoGRH_oT7LXz_bqdoIVb2KglAA",
                    txSig = SignatureData(v = 28, r = "0xe003a9fa1fd6588614c8296cb74ca9c77ca8b314c552f850d750acf790289908".hexToBigInteger(), s = "0x3db1afbe3c70d47f06c2e9802978d772722ecfa537e6c7006b5586fdef4e4db2".hexToBigInteger())
            ),
            Combo(
                    privateKey = "c5f42cd60790e3d57f883a535f19902ee9515cf2f5e3031767499116e5ef0835",
                    address = "0xc7f2b6ce3c775d0e011c77a978ea1713c17e314e",
                    jwtSig = "1s6QCrFtWOghTRreZqimcJGqLob3dYq-fPOV7YBu4DmPiGdOZeF7aVOVh2mnPiKChdCWyws71t8Q4kcbn4KOvQA",
                    txSig = SignatureData(v = 28, r = "0xf13d2b58e85851d5c327ace636df9e66b70cfb4db7b6994913a312f8cd6fb52".hexToBigInteger(), s = "0x7607e1d1d86a3ccf90c9e4e0f13471fe8f7d60b405968a693fb20ab3a765e48a".hexToBigInteger())
            ),
            Combo(
                    privateKey = "18dc8bd93b827cdb2c03555ea1c25f641c14ca9e309aa4cc06c8c8a27682dbe1",
                    address = "0x8183a72d9163f19400bf4243de3d2307e42044d8",
                    jwtSig = "2S3X-IburN8P5uuaDuZkLs4KpMKkxhF3a20mtV1NJ0Q1liWt1h5x4zw9owN9clYpor5jBZgYAua9Gl7neaTkyAA",
                    txSig = SignatureData(v = 28, r = "0x2bdfd7aaaed275426cac0a3d84947b4fdbcdbf3b6420f4f11b755e34d9aab454".hexToBigInteger(), s = "0x6846282eaa5daa9769e7e45671d07893b9e4a50ae55c755f472747e0d7096d06".hexToBigInteger())
            ),
            Combo(
                    privateKey = "3dd60ce7a16cee8844304a3008e344a48802040f89aa37b5a8c644b3e03734a8",
                    address = "0x64ed433519e8c99c990104e5feecca360e88ad67",
                    jwtSig = "OQQuRn2m5NFVgm7OVL9amcr1rvfuOe6dv9ojul-PGOLWbqS5N_sFiiSbckrZXBElWcYe9NR6iULMEfFJ_aP0rQE",
                    txSig = SignatureData(v = 27, r = "0x27b9e2b405c16171464f2de3c03f2590cd66e4397d13463a7d498bbe716783f".hexToBigInteger(), s = "0x2c04841b252e9277b97dce5d2f5c3e097f5a8830b9614b7ede82ac839b460045".hexToBigInteger())
            ),
            Combo(
                    privateKey = "a0670660819203cc4cd606fb10e41964c88df593f10a7b3a6586a0c77cd7b788",
                    address = "0x8da5e4f050638e59e87bba9ecd2cb1e6daac777a",
                    jwtSig = "NfR8hmJDpwTjq0zUl4hql2t69AwiMXFSiA6jxDzoa4wLX-5o957Cryw0hUuX9qAqtw8AbMCvVJCiYe7LId3NqQA",
                    txSig = SignatureData(v = 27, r = "0xcf4974379001999c10c123211e1e07859a10175360babd9d0278211051d8278e".hexToBigInteger(), s = "0x157c9625b3b59cfdc281948b3d8c8b537d365b1c511d72a2ef4d15f9bcb4f4c".hexToBigInteger())
            ),
            Combo(
                    privateKey = "4058c4d945d175894a343593e20a7ea0e443bc7519f6e724cbfdff33caa1c417",
                    address = "0x0e899ed45a65b7df769409ad7b4022a88ba6e88d",
                    jwtSig = "i5H24fGipgB3uqKFybinTShghtVWXSDzLZgC7xv-OtDkkakkHXL0z-9zYv6CultHF4H7AokfR2jm-XEojVi9vQA",
                    txSig = SignatureData(v = 28, r = "0xe1fa66ba41b1600155d641f7ca87ef420aeac054941eb4d57a111030f0e4cc92".hexToBigInteger(), s = "0x63c692f926d917e7c1780d358c53d7e7c3ec7e829a090f1ccb872ca7417224bb".hexToBigInteger())
            ),
            Combo(
                    privateKey = "28f92893986f228df0701b671797de98692dbea624e9020ae90ca46324f1dbb8",
                    address = "0x2cfdba50cef4740afb6653637b9753e157013ab9",
                    jwtSig = "iOYwRCk49OhKhfpE9ToUdSnhnsT7kW9yJeQ6q-qN7kO8AkKh5_HvoJBnSem6Ttqls8kEjF-mcyTyBtvkTwJR6QE",
                    txSig = SignatureData(v = 28, r = "0x5b9fb7ad028a8d1192f6bfa9a282b97bb5142c333a255d77c8078ff5bbe3f3fc".hexToBigInteger(), s = "0x403f0b223d83cc6303297e4d60fbdde07e0f7881f1446af6267657f732ca44fe".hexToBigInteger())
            ),
            Combo(
                    privateKey = "538c6cf52b02f0197b3ab5ee0c4daf22d10d9e0481949bc9918216e89b17004b",
                    address = "0xc7567e2839190e618979a2e0ccd29a1b661b0901",
                    jwtSig = "-cyEBJEWjUYabyTJNi6rn7sDCIniKtlftxxoQmyuJEUE47o3nMAgfFALLoEH0NF1VuzzLyPoQmaRdrp5iZjrJgA",
                    txSig = SignatureData(v = 28, r = "0xb8fda60c66ea89af5d0285bc4c3c49017eb457a89f1d97b89cee9ff4cecb1d6".hexToBigInteger(), s = "0x551674a7f205692573a81c5996898bfa721bee4509136f247ce0ea5bc110604".hexToBigInteger())
            ),
            Combo(
                    privateKey = "5ec82d04429686c9808db35b85d3a84db754f58bc46e0499dafefac308bc0a69",
                    address = "0xf8484190e003797a21eb2b522b0e7f3545bc5c98",
                    jwtSig = "_-evI1296bUxUinbixLgfim-27QrMDo1HCOwmJd1cOH0Q2BL31F0mx--3rDvK9xbz3kIPNGGUBXhial_MnPxdwA",
                    txSig = SignatureData(v = 28, r = "0x933cb61ffd3efd43b2caea2d6b3d97b62e24c24a891dbbe96a0fb49e11980938".hexToBigInteger(), s = "0x98bc22f8f8db8f49b58fe45ba503a14cecc5ec75b20237e13c3d488d3cfba4c".hexToBigInteger())
            ),
            Combo(
                    privateKey = "25f904bda7cf7d8ae422a3dc0a8b8b838714064deac36d0481f8e999d3b06318",
                    address = "0xe694d6bfb0e0f60e047f37790f0bbf2e3a78352c",
                    jwtSig = "otDw4UATSRxEQl45M16cHez3AEIm4Vz_QZg8uy5zddqRO2kX49M8enr3yjsLVuRkE0cqH4W6nfu4tSRJb847kgE",
                    txSig = SignatureData(v = 27, r = "0x90aeaa277adfa752533819651261f38b23f23f368349cbbf52a6292db688c33b".hexToBigInteger(), s = "0x1ea00142834b6b9685eaca6998f5aea0a16b3d0b6d31a465b0ac798f9250da62".hexToBigInteger())
            ),
            Combo(
                    privateKey = "b3d9a71fcf70440e851e303e13851a89b35a5f37911d57ba0fb88627279f21a5",
                    address = "0x769952b356819d8e1ab63312d5ac3c2c14d1801e",
                    jwtSig = "xWUEZjxun0i2DpZ49uJKSlCAkZw_bkGRbU-PElF4r_0I0dP6DAJa_dd_c0VLmyZadeKNaaCssi4bJNnTY4vlDgA",
                    txSig = SignatureData(v = 28, r = "0x8beb76acec1919989aa287af3bd7ed228fa042a8d13ecc84e06eec735c798eb2".hexToBigInteger(), s = "0x7c8c5f26bd3eacdcc6ac938a0b35f9a3afe88636280ba0ea391a68a957c8f21f".hexToBigInteger())
            ),
            Combo(
                    privateKey = "c87645ab6c562e4eccb6637a70750bb79e751ea68910b1dcbc49d6ccab721e2b",
                    address = "0x4edd332bd5aabc51a7305f8c0b6220a3ce438c83",
                    jwtSig = "Rknuj10koIStG1Yj7ePzsaM3LIUlECK1O_oW3CsMbbHwfUK5LemfliZBGHAsyY-l_IeTRVucwlldc-W59Xui3wE",
                    txSig = SignatureData(v = 27, r = "0xcd0682b37003064536d421dad51d5af1ee79c08d84ea6e4a939ce631923d266d".hexToBigInteger(), s = "0x59a17d5675bb7473e5a6f5c2bacd3fab6c804a0fcac5737f4a0b2201074be914".hexToBigInteger())
            ),
            Combo(
                    privateKey = "c69522947b9a0e9ee3151d207ce4c8273f14e2862f8957d128f4a54e7248e31c",
                    address = "0x2a5c2ae9941f4cedb047289e2b9f3357b16baa0c",
                    jwtSig = "5JAjO5oKXaZUkQ8Ae6SmyVTP4lIBm4k2KNnzcwfbhylXzC6PbrX2aPm6YfaTU0ZyghMFLFOw6ElOMuOmMH3jswA",
                    txSig = SignatureData(v = 27, r = "0x7ca586033f4cbb3cf4a7fd96432c31db059a2ba9405c09d0aec7582ea55270d4".hexToBigInteger(), s = "0x4041e8c8f8146f5bbdbbda2a41c73d97978a06307a6071459ac5effd309eebcb".hexToBigInteger())
            ),
            Combo(
                    privateKey = "221c52e66ac846058181aea5e1d6379c4daa39887c744e03e0c1bf6b893f4375",
                    address = "0x44ab6c23c05f227447196356d7a8ba8f5f5ccfd0",
                    jwtSig = "q9X7DkW2nNxhBUDhb8h-wfQ8yDEu49WZjvQbeHCusPPSVGc6IeRiDIgPyAuqjQ7TheNVQwXfjDYZNn34MIZx-wA",
                    txSig = SignatureData(v = 27, r = "0xdc79780b9cfd68759eab3985a01e11a84b2569a4c0ccd7c56e3295a15d8f3aa0".hexToBigInteger(), s = "0x282acdb462bb5efd03b0d800b098eb23e195b0c1a56dfac0b9f08431bc7c6792".hexToBigInteger())
            ),
            Combo(
                    privateKey = "0b6bf54bec26d47b5313e725a3dd9fd39162d64b37d6fd773b0e789bd33b8b0d",
                    address = "0xfcaee7e3d0786365eb2917d3bb1fa6392444387a",
                    jwtSig = "w1V0LeVBDhTC5nOB65a8SJzM5_Z5_jJkJZZVE0G64RPLIE1SYCSeF2wFDBNmSHZBAX5MQPMyjV3POkNVLvfs5gE",
                    txSig = SignatureData(v = 27, r = "0x2515c84688364987ae74c22594c083b71676f244de34660f180b95b7d71cc5a3".hexToBigInteger(), s = "0x21c03e55e2f934ae81b88bdffe1c325c00d62b3cd37f489097f2281855b38e8d".hexToBigInteger())
            ),
            Combo(
                    privateKey = "17c7465cd05dba8fea7045cba4ec344f1bfc2b03cdc6927dfeced97126a84f82",
                    address = "0xde3700766d266524bfb1bc0d8d5e077fb6f65a42",
                    jwtSig = "lqSEFF-Mv3uQhB-Swx9yRgK1Cp5V77LLTKi9b9-opZpCOsapzjH8GzMgNnNUEJcO-EvF40_hlxzWykz3pm4qHQA",
                    txSig = SignatureData(v = 28, r = "0xdc38c256b289830b45c941d1ae89dd623936ddb0e3120a89011f811c80d8338f".hexToBigInteger(), s = "0x573433304a723b9c97830c05075a051b5bd314da7f38c753eba8ae43550d49c8".hexToBigInteger())
            ),
            Combo(
                    privateKey = "72255262378a076f766cd17ff015174df23f5c532a937c9bc03aa5b4e9afead8",
                    address = "0xc92a4bbb46f48fed7cd154a71afa755732e460eb",
                    jwtSig = "wzCVKbqYbNmeyFNN86TrlLESCh8q4pUZl7FqaW38DY50a3J_N7_1QVUrUTO6Nof-cpfl3gR3KqBz4ejTHWBCEgA",
                    txSig = SignatureData(v = 28, r = "0x6010e460863239ef329ea76ab26219505a4672d72bb9f31e35080b163bcb6721".hexToBigInteger(), s = "0x62cd62aa206eda51076c36e7f76c712df58f0eeed7c9286d532197aa0003e50c".hexToBigInteger())
            ),
            Combo(
                    privateKey = "92465e3f17be18e01509254a10dff6ddf18d8651e091f3a045332cf896dda240",
                    address = "0xb8489af2cb572304c607f14c991efa83517da888",
                    jwtSig = "Y03oDOsmltbseap8QuIUFM_lDzu2o7QolEXxwV5tlArlhxO9mAzcLSM7zJKYz0L74cPZoQiMiZaeKM1ohYgz0QE",
                    txSig = SignatureData(v = 27, r = "0x596e4048cbf970cad7a3de08da7a8e0070d3eb202b906e3e662f7f557efa2464".hexToBigInteger(), s = "0x396c2e34c43f9e49e67dc601c6c408a4b7cf757fd2c0efd18596641559512efb".hexToBigInteger())
            ),
            Combo(
                    privateKey = "73391c9fbb1b79f7a53614a2ac13058bf08e62d299fe0afb48337be4f98d33ab",
                    address = "0xe398a7b24b6931ff34b557ae14db3117dd51fbc7",
                    jwtSig = "2WH02NgmsZgPawIKXTLsz99JqTiyrQ2FXsNJBwyMNkQ3_XTK-tqc6BMU3XP2mg2CXs2RHEZSwn1GnJfP1VOomQE",
                    txSig = SignatureData(v = 27, r = "0x30370d3c46e8d94cd525e6a1d08567a7e084262ae4add7d0d9769dd826b7490c".hexToBigInteger(), s = "0x3762be0566440c2f15efac351e79cbe8614aa875b7bd1b547fec32add7ac0ec1".hexToBigInteger())
            ),
            Combo(
                    privateKey = "d602f2d7c4845a7b64fede9c79eb7367661e2299b877457cf957095c38ab1d9f",
                    address = "0xd972ed893a96444bd27ac6e21d8de4f12608e4d4",
                    jwtSig = "ZwnD-zdlXtmclgb4kxwrHjghiu_RiNAhvpcMtBg4dD2Tif60MCeg7Gf0LVbhnt-MWoHXDD0BKKqurawzAaTV0wA",
                    txSig = SignatureData(v = 28, r = "0xc7f59af7313580480705c7934e3a0d1e418c5e5fa3cdabe81d67ff4fd379b48c".hexToBigInteger(), s = "0x7eaf2fce318b95c3e36665f8eeea289ba8ea7ad857543ce03df13dfef2125c07".hexToBigInteger())
            ),
            Combo(
                    privateKey = "1e029460a97d52117f66b09e25d69eaf0a03cceefd160c089ef067b6708d014b",
                    address = "0x30fde40c45959274e8da4f16e641936c492fce2f",
                    jwtSig = "090Wgb_MnL1Ei5ZYwJBYTZtKx4KV2wN7Ar1r-jIl2gMWpiRgC3TztCQbxlrukAmvq9XyEmbnn8NaVDpDDEkZSQE",
                    txSig = SignatureData(v = 27, r = "0x5878d68a45ca349e01a07699fcac7c690ed2c437864fa028ac61fa75685219c".hexToBigInteger(), s = "0x42026e4c724048c4b77914bff56877c2b759ad2c6bf9e67b652e26afcf08b765".hexToBigInteger())
            ),
            Combo(
                    privateKey = "5499ea955c098840775cd627ea4fc93d8f3de17f8721a93be3ebe3b7b70b4fcc",
                    address = "0xd4318fab9656eb478b63caf458262ca95e914e78",
                    jwtSig = "yj3ebIZQQpX_TmZeKUxSzAmUib9PAAmI8DdshnrVpLDAY3eYb6rFnDtUgMf7Unz0ALXUfqvc0JFNKNDJK_WwXwA",
                    txSig = SignatureData(v = 28, r = "0x37708864d23409b99adc7bf21703638fec1396ddc574c1ea1204e43c9c630be7".hexToBigInteger(), s = "0xa20e127564ebc955d32c5bdf19d3711c34596bfde79c38152c8267080b9c033".hexToBigInteger())
            ),
            Combo(
                    privateKey = "818b285cf19c5fbd3d5a02b307d05132ef204d60dae8eff59535d03cbb63c932",
                    address = "0xde7c3bffe1205adc8c2dd654ecf8dece33efdb54",
                    jwtSig = "-99We6jDdOZwfYj_1N1a5VCX-X1brrcKRKZbG5LQDhYTU_2ObTuBRBU5gk0zG7hb5yizZiYUR0CS1y3Zt0H-yQA",
                    txSig = SignatureData(v = 27, r = "0x416bf6c393266b9467726049b2dd54b01b40f08c16255e1423d57d02a11372a2".hexToBigInteger(), s = "0x3d9ad2f56843a1c841787e83aa50a09ea6f6b479888c86639bcf80bf42f1affd".hexToBigInteger())
            )
    )

}

