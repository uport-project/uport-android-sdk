package me.uport.sdk.transport

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isZero
import org.junit.Test
import org.walleth.khex.hexToByteArray


class CryptoTest {

    @Test
    fun `can do correct zero padding on unicode`() {

        val messages = listOf(
                "hello",
                "小路の藪",
                "柑子、パイ",
                "ポパイポ パイ",
                "ポのシューリンガ",
                "ン。五劫の擦り切れ",
                "、食う寝る処に住む処",
                "。グーリンダイのポンポ",
                "コピーのポンポコナーの、",
                "長久命の長助、寿限無、寿限",
                "無、グーリンダイのポンポコピ",
                "ーのポンポコナーの。やぶら小路",
                "🇯🇵 🇰🇷 🇩🇪 🇨🇳 🇺🇸 🇫🇷 🇪🇸 🇮🇹 🇷🇺 🇬🇧",
                "🎄 🌟 ❄️ 🎁 🎅 🦌")

        messages.forEach { original ->
            val padded = original.padToBlock()
            assertThat(padded).isNotEqualTo(original.toByteArray())
            assertThat(padded.size % BLOCK_SIZE).isZero()

            val unpadded = padded.unpadFromBlock()
            assertThat(unpadded).isEqualTo(original)
        }

    }

    @Test
    fun `can encrypt and decrypt`() {
        val msg = "Hello EIP1098"
        val bobSecretKey = ByteArray(32) { it.toByte() }
        val bobPublicKeyBase64 = Crypto.getEncryptionPublicKey(bobSecretKey)
        val enc = Crypto.encryptMessage(msg, bobPublicKeyBase64)
        val recoveredMessage = Crypto.decryptMessage(enc, bobSecretKey)
        assertThat(recoveredMessage).isEqualTo(msg)
    }

    @Test
    fun `can decrypt EIP1024 message`() {
        val c = EncryptedMessage(
                nonce = "1dvWO7uOnBnO7iNDJ9kO9pTasLuKNlej",
                ephemPublicKey = "FBH1/pAEHOOW14Lu3FWkgV3qOEcuL78Zy+qW1RwzMXQ=",
                ciphertext = "f8kBcl/NCyf3sybfbwAKk/np2Bzt9lRVkZejr6uh5FgnNlH/ic62DZzy")
        val decryptedMessage = Crypto.decryptMessage(c, "7e5374ec2ef0d91761a6e72fdf8f6ac665519bfdf6da0a2329cf0d804514b816".hexToByteArray())
        assertThat(decryptedMessage).isEqualTo("My name is Satoshi Buterin")
    }

    @Test
    fun `can deserialize json string to EncryptedMessage`() {
        //language=JSON
        val json = """{"version":"x25519-xsalsa20-poly1305","nonce":"JAX+g+/e3RnnNXHRS4ct5Sb+XdgYoJeY","ephemPublicKey":"JLBIe7eSVyq6egVexeWrlKQyOukSo66G3N0PlimMUyI","ciphertext":"Yr2o6x831YWFZr6KESzSkBqpMv1wYkxPULbVSZi21J+2vywrVeZnDe/U2GW40wzUpLv4HhFgL1kvt+cORrapsqCfSy2L1ltMtkilX06rJ+Q"}"""
        val enc = EncryptedMessage.fromJson(json)
        assertThat(enc).isNotNull()
        enc!!
        assertThat(enc.version).isEqualTo("x25519-xsalsa20-poly1305")
        assertThat(enc.nonce).isEqualTo("JAX+g+/e3RnnNXHRS4ct5Sb+XdgYoJeY")
        assertThat(enc.ephemPublicKey).isEqualTo("JLBIe7eSVyq6egVexeWrlKQyOukSo66G3N0PlimMUyI")
        assertThat(enc.ciphertext).isEqualTo("Yr2o6x831YWFZr6KESzSkBqpMv1wYkxPULbVSZi21J+2vywrVeZnDe/U2GW40wzUpLv4HhFgL1kvt+cORrapsqCfSy2L1ltMtkilX06rJ+Q")
    }

    @Test
    fun `can serialize EncryptedMessage to json string`() {
        val input = EncryptedMessage(
                nonce = "1dvWO7uOnBnO7iNDJ9kO9pTasLuKNlej",
                ephemPublicKey = "FBH1/pAEHOOW14Lu3FWkgV3qOEcuL78Zy+qW1RwzMXQ=",
                ciphertext = "f8kBcl/NCyf3sybfbwAKk/np2Bzt9lRVkZejr6uh5FgnNlH/ic62DZzy")

        //language=JSON
        val expected = """{"version":"x25519-xsalsa20-poly1305","nonce":"1dvWO7uOnBnO7iNDJ9kO9pTasLuKNlej","ephemPublicKey":"FBH1/pAEHOOW14Lu3FWkgV3qOEcuL78Zy+qW1RwzMXQ=","ciphertext":"f8kBcl/NCyf3sybfbwAKk/np2Bzt9lRVkZejr6uh5FgnNlH/ic62DZzy"}"""

        val json = input.toJson()
        assertThat(json).isEqualTo(expected)
    }

    @Test
    fun `can encrypt and decrypt large message`() {
        val loremIpsum = """
            やぶら小路の藪柑子、パイポパイポ パイポのシューリンガン。五劫の擦り切れ、食う寝る処に住む処。グーリンダイのポンポコピーのポンポコナーの、長久命の長助、寿限無、寿限無、グーリンダイのポンポコピーのポンポコナーの。やぶら小路の藪柑子。
            パイポパイポ パイポのシューリンガン、水行末 雲来末 風来末。シューリンガンのグーリンダイ、グーリンダイのポンポコピーのポンポコナーの。やぶら小路の藪柑子、寿限無、寿限無。パイポパイポ パイポのシューリンガン。寿限無、寿限無、長久命の長助。シューリンガンのグーリンダイ。長久命の長助、水行末 雲来末 風来末。グーリンダイのポンポコピーのポンポコナーの。
            パイポパイポ パイポのシューリンガン。長久命の長助。やぶら小路の藪柑子、長久命の長助、パイポパイポ パイポのシューリンガン、グーリンダイのポンポコピーのポンポコナーの、海砂利水魚の、寿限無、寿限無。食う寝る処に住む処。水行末 雲来末 風来末、シューリンガンのグーリンダイ、五劫の擦り切れ。グーリンダイのポンポコピーのポンポコナーの。海砂利水魚の、食う寝る処に住む処、シューリンガンのグーリンダイ。五劫の擦り切れ。水行末 雲来末 風来末。寿限無、寿限無、やぶら小路の藪柑子。
            やぶら小路の藪柑子、寿限無、寿限無。長久命の長助。五劫の擦り切れ、グーリンダイのポンポコピーのポンポコナーの。パイポパイポ パイポのシューリンガン。シューリンガンのグーリンダイ、五劫の擦り切れ、食う寝る処に住む処。
            水行末 雲来末 風来末、水行末 雲来末 風来末、パイポパイポ パイポのシューリンガン。グーリンダイのポンポコピーのポンポコナーの、五劫の擦り切れ、寿限無、寿限無、シューリンガンのグーリンダイ。海砂利水魚の、パイポパイポ パイポのシューリンガン。五劫の擦り切れ、やぶら小路の藪柑子。海砂利水魚の、食う寝る処に住む処、食う寝る処に住む処、寿限無、寿限無。長久命の長助、やぶら小路の藪柑子。グーリンダイのポンポコピーのポンポコナーの。
            국회는 헌법개정안이 공고된 날로부터 60일 이내에 의결하여야 하며. 모든 국민은 법률이 정하는 바에 의하여 선거권을 가진다. 이 경우 공무원 자신의 책임은 면제되지 아니한다, 국가원로자문회의의 조직·직무범위 기타 필요한 사항은 법률로 정한다.
            समूह ढांचा शुरुआत मानसिक उसीएक् दोषसके संसाध मेमत सकते निर्देश विस्तरणक्षमता शीघ्र और्४५० प्राथमिक ध्वनि उनका एसलिये सम्पर्क प्राधिकरन यधपि हिंदी मुखय प्रमान आशाआपस प्रतिबध समजते हार्डवेर संस्क्रुति केवल संसाध नयेलिए जानकारी स्वतंत्र विवरन मानव स्थिति है।अभी निरपेक्ष सकते विशेष उपेक्ष निर्देश ध्वनि करती तकरीबन विनिमय सुस्पश्ट भारत करता। एसेएवं एकत्रित विवरन एछित मुख्य सभिसमज निरपेक्ष स्वतंत्रता २४भि
            ومن هو مدينة غينيا. لها مع الشطر العصبة المتساقطة،, جيما الذود و ولم, الآخر انذار بمباركة بـ حيث. جُل في بتطويق حاملات والكساد, أضف هناك الأولى ولاتّساع في. بحث أم وترك عسكرياً الجنرال, عل مايو المارق حين, بقعة شدّت المشترك تعد في.
            լոռեմ իպսում դոլոռ սիթ ամեթ, վենիամ դելենիթ նե սիթ, եոս վեռեառ ինթեգռե ծոռպոռա իդ, պոսթեա պռոդեսսեթ վիմ ան. նոսթռուդ վիվենդո նո պեռ. եի նոսթռո ֆասթիդիի ինծիդեռինթ եում, մոդո պռոբաթուս ռեծթեքուե նե դուո. եամ սինթ մունեռե.
            ლორემ იფსუმ დოლორ სით ამეთ, ეუმ რებუმ აფფელლანთურ ეა. ვის ეა სოლეთ რათიონიბუს, ეა ნეც ყუანდო ფართიენდო ირაცუნდია. ცუმ ად ფალლი ვოლუთფათ. ან აეთერნო თამყუამ ვის, ეა ილლუდ აეყუე ველ. ნე ყუო ალია.
            שער מוגש בשפה הקהילה אם. סרבול ביוני על לוח. את לחבר המלצת חבריכם אחר, מה שתי בשפה להפוך ניהול. המחשב משפטים ויקיפדיה אם בקר.
            En ruffen iw'rem grousse oft. Do alles d'Beem weisen ons, hun Räis kille Stret jo. Um kille frësch sin, d'Stroos däischter un zum. Ronn aremt Schuebersonndeg as ass, un sinn geplot wou. Un erem d'Liewen d'Vullen hun. Ké der fond Noper uechter, Feld éiweg gewëss en hir, en Ronn lait heemlech hir.
            डाले। गएआप वेबजाल रहारुप निरपेक्ष साधन औषधिक अत्यंत निर्माण विशेष संभव स्थापित पहोचाना संपुर्ण आजपर सारांश सभिसमज निर्देश विभाग सदस्य ब्रौशर केन्द्रित विचरविमर्श ७हल अत्यंत माहितीवानीज्य विवरन मुखय ध्वनि प्रौध्योगिकी विकेन्द्रियकरण सुनत कीसे हमारी परिवहन हार्डवेर सार्वजनिक होने प्रति जिम्मे बेंगलूर प्रतिबध्दता मानसिक और्४५० प्राधिकरन ध्येय ७हल पहेला पत्रिका विश्वास मुश्किले जैसी कार्य नयेलिए भोगोलिक सोफ़्टवेर कर्य
            🇯🇵 🇰🇷 🇩🇪 🇨🇳 🇺🇸 🇫🇷 🇪🇸 🇮🇹 🇷🇺 🇬🇧
            🎄 🌟 ❄️ 🎁 🎅 🦌
        """.trimIndent()

        val bobSecretKey = ByteArray(32) { it.toByte() }
        val bobPublicKeyBase64 = Crypto.getEncryptionPublicKey(bobSecretKey)
        val enc = Crypto.encryptMessage(loremIpsum, bobPublicKeyBase64)
        val recoveredMessage = Crypto.decryptMessage(enc, bobSecretKey)
        assertThat(recoveredMessage).isEqualTo(loremIpsum)
    }

}
