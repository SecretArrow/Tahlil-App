package my.tahlil.app.data

import kotlinx.serialization.Serializable

/** Type of a section — drives the colored badge and accent color. */
@Serializable
enum class SectionType(val key: String, val label: String) {
    Pembukaan("pembukaan", "Pembukaan"),
    Sholawat("sholawat", "Shalawat"),
    Surah("surah", "Surat"),
    Tahlil("tahlil", "Tahlil"),
    Ayat("ayat", "Ayat"),
    Doa("doa", "Doa"),
    Dzikir("dzikir", "Dzikir");

    companion object {
        fun fromKey(key: String): SectionType =
            values().firstOrNull { it.key == key } ?: Tahlil
    }
}

/** A single block within a section — Arabic verse + transliteration + translation. */
@Serializable
data class ContentBlock(
    val arabic: String? = null,
    val transliteration: String,
    val translation: String,
    val note: String? = null,
    val repeat: Int? = null,
)

/** A numbered section of the prayer book. */
@Serializable
data class Section(
    val id: String,
    val number: Int,
    val title: String,
    val subtitle: String? = null,
    val type: SectionType,
    val blocks: List<ContentBlock>,
)

/** Hero copy shown at the top of the home screen. */
object HeroInfo {
    const val title = "Tahlil Lengkap"
    const val subtitle = "Tahlil Malam Jumat & Doa Arwah"
    const val description =
        "Rangkaian lengkap tahlil, zikir, dan doa untuk mendoakan para arwah kaum muslimin. 32 bagian disusun dari Al-Qur'an, hadits, dan amaliah ulama — sesuai tatacara Tahlil Malam Jumat."
    const val arabicBismillah = "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ"
    const val source = "Kitab Tahlil Lengkap (Malam Jumat)"
}

/**
 * Singleton holding the full content of the prayer book — Tahlil Lengkap
 * (Malam Jumat) with 32 sections, sourced from Kitab Tahlil
 * Lengkap. Mirrors `src/data/tahlil-content.ts` of the web app.
 *
 * Loaded lazily on first access so the file doesn't pay an upfront cost on
 * app launch.
 */
object TahlilContent {

    val sections: List<Section> by lazy { buildSections() }

    /** Look up a section by its stable id. */
    fun byId(id: String): Section? = sections.firstOrNull { it.id == id }

    /** Index of a section in the [sections] list, or -1 if not found. */
    fun indexOf(id: String): Int = sections.indexOfFirst { it.id == id }

    private fun buildSections(): List<Section> = listOf(

// 1 ──────────────────────────────────────────────────────────────────
Section(
    id = "pembuka-basmalah-syahadat",
    number = 1,
    title = "Pembuka — Basmalah dan Syahadat",
    subtitle = "Membuka majelis tahlil dengan basmalah dan dua kalimat syahadat",
    type = SectionType.Pembukaan,
    blocks = listOf(
        ContentBlock(
                arabic = "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ",
                transliteration = "Bismillâhir-rahmânir-rahîm",
                translation = "Dengan menyebut nama Allah Yang Maha Pengasih lagi Maha Penyayang.",
        ),
        ContentBlock(
                arabic = "أَشْهَدُ أَنْ لَا إِلٰهَ إِلَّا اللّٰهُ وَأَشْهَدُ أَنَّ مُحَمَّدًا رَسُوْلُ اللّٰهِ",
                transliteration = "Asyhadu an lâ ilâha illallâh, wa asyhadu anna Muhammadar rasûlullâh",
                translation = "Saya bersaksi tidak ada Tuhan yang berhak disembah kecuali Allah. Dan saya bersaksi bahwa sesungguhnya Nabi Muhammad adalah utusan Allah.",
        )
    ),
),

// 2 ──────────────────────────────────────────────────────────────────
Section(
    id = "istighfar",
    number = 2,
    title = "Istighfar",
    subtitle = "Memohon ampun kepada Allah Yang Maha Agung",
    type = SectionType.Dzikir,
    blocks = listOf(
        ContentBlock(
                arabic = "أَسْتَغْفِرُ اللّٰهَ الْعَظِيْمَ",
                transliteration = "Astaghfirullâhal-'azhîm",
                translation = "Saya mohon ampun kepada Allah Yang Maha Agung.",
        ),
        ContentBlock(
                arabic = "أَسْتَغْفِرُ اللّٰهَ الْعَظِيْمَ الَّذِيْ لَا إِلٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّوْمُ وَأَتُوْبُ إِلَيْهِ",
                transliteration = "Astaghfirullâhal-'azhîmal-ladzî lâ ilâha illâ huwal-hayyul-qayyûm wa atûbu ilaih",
                translation = "Saya mohon ampun kepada Allah Yang Maha Agung, yang tiada Tuhan selain Dia, Yang Maha Hidup lagi terus-menerus mengurus makhluk-Nya, dan saya bertobat kepada-Nya.",
        )
    ),
),

// 3 ──────────────────────────────────────────────────────────────────
Section(
    id = "tawassul-nabi",
    number = 3,
    title = "Tawassul / Hadhoroh Pertama",
    subtitle = "Kepada Nabi Muhammad ﷺ",
    type = SectionType.Sholawat,
    blocks = listOf(
        ContentBlock(
                arabic = "إِلَى حَضْرَةِ النَّبِيِّ الْمُصْطَفَى سَيِّدِنَا مُحَمَّدٍ صَلَّى اللّٰهُ عَلَيْهِ وَسَلَّمَ وَاٰلِهِ وَأَزْوَاجِهِ وَأَوْلَادِهِ وَذُرِّيَّاتِهِ، شَيْءٌ لِلّٰهِ لَهُمُ الْفَاتِحَةُ",
                transliteration = "Ilâ hadhratin-nabiyyil-musthafâ sayyidinâ Muhammadin shallallâhu 'alaihi wa sallama wa âlihi wa azwâjihi wa aulâdihi wa dzurriyyâtihi, syai'un lillâhi lahumul-Fâtihah",
                translation = "Kepada yang terhormat Nabi terpilih, junjungan kami Muhammad ﷺ, segenap keluarga, istri-istri, anak-anak, dan keturunannya. Bacaan Al-Fatihah ini kami tujukan kepada Allah dan pahalanya untuk mereka semua. Al-Fatihah…",
        )
    ),
),

// 4 ──────────────────────────────────────────────────────────────────
Section(
    id = "surah-al-fatihah",
    number = 4,
    title = "Surah Al-Fatihah",
    subtitle = "Induk Al-Qur'an, dibaca untuk setiap tawassul",
    type = SectionType.Surah,
    blocks = listOf(
        ContentBlock(
                arabic = "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ • اَلْحَمْدُ لِلّٰهِ رَبِّ الْعٰلَمِيْنَ • الرَّحْمٰنِ الرَّحِيْمِ • مٰلِكِ يَوْمِ الدِّيْنِ • إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِيْنُ • اِهْدِنَا الصِّرَاطَ الْمُسْتَقِيْمَ • صِرَاطَ الَّذِيْنَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوْبِ عَلَيْهِمْ وَلَا الضَّاۤلِّيْنَ • رَبِّ اغْفِرْ لِيْ وَلِوَالِدَيَّ وَلِلْمُؤْمِنِيْنَ اٰمِيْنَ",
                transliteration = "Bismillâhir-rahmânir-rahîm. Alhamdu lillâhi rabbil-'âlamîn. Ar-rahmânir-rahîm. Mâliki yaumid-dîn. Iyyâka na'budu wa iyyâka nasta'în. Ihdinash-shirâthal-mustaqîm. Shirâthal-ladzîna an'amta 'alaihim, ghairil-maghdhûbi 'alaihim wa ladh-dhâllîn. Rabbighfir lî wa liwâlidayya wa lil-mu'minîna, âmîn",
                translation = "Dengan menyebut nama Allah Yang Maha Pengasih lagi Maha Penyayang. Segala puji bagi Allah, Tuhan semesta alam. Yang Maha Pengasih lagi Maha Penyayang. Yang menguasai hari pembalasan. Hanya kepada-Mu kami menyembah dan hanya kepada-Mu kami mohon pertolongan. Tunjukilah kami jalan yang lurus, yaitu jalan orang-orang yang telah Engkau beri nikmat, bukan jalan mereka yang dimurkai dan bukan pula jalan mereka yang sesat. Ya Tuhanku, ampunilah aku, kedua orang tuaku, dan orang-orang mukmin. Kabulkanlah.",
                note = "Al-Fatihah (Surat ke-1)",
        )
    ),
),

// 5 ──────────────────────────────────────────────────────────────────
Section(
    id = "tawassul-kedua",
    number = 5,
    title = "Tawassul Kedua",
    subtitle = "Kepada Para Nabi, Wali, dan Syekh Abdul Qadir Al-Jailani",
    type = SectionType.Tahlil,
    blocks = listOf(
        ContentBlock(
                arabic = "ثُمَّ إِلَى حَضْرَةِ إِخْوَانِهِ مِنَ الْأَنْبِيَاءِ وَالْمُرْسَلِيْنَ وَالْأَوْلِيَاءِ وَالشُّهَدَاءِ وَالصَّالِحِيْنَ وَالصَّحَابَةِ وَالتَّابِعِيْنَ وَالْعُلَمَاءِ الْعَامِلِيْنَ وَالْمُصَنِّفِيْنَ الْمُخْلِصِيْنَ وَجَمِيْعِ الْمُجَاهِدِيْنَ فِيْ سَبِيْلِ اللّٰهِ رَبِّ الْعٰلَمِيْنَ وَالْمَلَائِكَةِ الْمُقَرَّبِيْنَ، خُصُوْصًا إِلَى سَيِّدِنَا الشَّيْخِ عَبْدِ الْقَادِرِ الْجِيْلَانِيْ رَضِيَ اللّٰهُ عَنْهُ، شَيْءٌ لِلّٰهِ لَهُمُ الْفَاتِحَةُ",
                transliteration = "Tsumma ilâ hadhrati ikhwânihi minal-anbiyâ'i wal-mursalîn, wal-auliyâ'i wasy-syuhadâ'i wash-shâlihîn, wash-shahâbati wat-tâbi'în, wal-'ulamâ'il-'âmilîn, wal-mushannifînal-mukhlishîn, wa jamî'il-mujâhidîna fî sabîlillâhi rabbil-'âlamîn, wal-malâ'ikatil-muqarrabîn, khushûshan ilâ sayyidinâsy-syaikh 'Abdil-Qâdir Al-Jîlânî radhiyallâhu 'anhu, syai'un lillâhi lahumul-Fâtihah",
                translation = "Kemudian kepada segenap saudara beliau dari kalangan para nabi, rasul, wali, syuhada, orang-orang saleh, sahabat, tabi'in, ulama yang mengamalkan ilmunya, para pengarang yang ikhlas, semua pejuang di jalan Allah Tuhan semesta alam, dan para malaikat yang dekat dengan Allah, khususnya kepada junjungan kami Syekh Abdul Qadir Al-Jailani — semoga Allah meridhainya. Al-Fatihah…",
        )
    ),
),

// 6 ──────────────────────────────────────────────────────────────────
Section(
    id = "tawassul-khusus-arwah",
    number = 6,
    title = "Tawassul Khusus",
    subtitle = "Kepada Arwah yang Dikirimi Doa",
    type = SectionType.Doa,
    blocks = listOf(
        ContentBlock(
                arabic = "ثُمَّ خُصُوْصًا إِلَى رُوْحِ ...، وَرُوْحِ ...، وَرُوْحِ ...، اَللّٰهُمَّ اغْفِرْ لَهُمْ وَارْحَمْهُمْ وَعَافِهِمْ وَاعْفُ عَنْهُمْ، شَيْءٌ لِلّٰهِ لَهُمُ الْفَاتِحَةُ",
                transliteration = "Tsumma khushûshan ilâ rûhi ..., wa rûhi ..., wa rûhi ..., Allâhummaghfir lahum warhamhum wa 'âfihim wa'fu 'anhum, syai'un lillâhi lahumul-Fâtihah",
                translation = "Kemudian khususnya kepada roh ..., dan roh ..., dan roh ... (sebutkan nama arwah yang dituju pada bagian ...). Ya Allah, ampunilah mereka, rahmatilah mereka, sejahterakanlah mereka, dan maafkanlah mereka. Al-Fatihah…",
                note = "Sebutkan nama arwah yang dituju pada bagian ... (dapat diisi hingga tiga arwah).",
        )
    ),
),

// 7 ──────────────────────────────────────────────────────────────────
Section(
    id = "tawassul-ahli-kubur",
    number = 7,
    title = "Tawassul Ketiga",
    subtitle = "Kepada Semua Ahli Kubur",
    type = SectionType.Tahlil,
    blocks = listOf(
        ContentBlock(
                arabic = "ثُمَّ إِلَى جَمِيْعِ أَهْلِ الْقُبُوْرِ مِنَ الْمُسْلِمِيْنَ وَالْمُسْلِمَاتِ وَالْمُؤْمِنِيْنَ وَالْمُؤْمِنَاتِ مِنْ مَشَارِقِ الْأَرْضِ إِلَى مَغَارِبِهَا بَرِّهَا وَبَحْرِهَا، خُصُوْصًا إِلَى اٰبَائِنَا وَأُمَّهَاتِنَا وَأَجْدَادِنَا وَجَدَّاتِنَا وَمَشَايِخِنَا وَلِمَنِ اجْتَمَعْنَا هٰهُنَا بِسَبَبِهِ، الْفَاتِحَةُ",
                transliteration = "Tsumma ilâ jamî'i ahlil-qubûri minal-muslimîna wal-muslimâti wal-mu'minîna wal-mu'minâti min masyâriqil-ardhi ilâ maghâribihâ barrihâ wa bahrihâ, khushûshan ilâ âbâ'inâ wa ummahâtinâ wa ajdâdinâ wa jaddâtinâ wa masyâyikhinâ wa liman ijtama'nâ hâhunâ bisababihi, al-Fâtihah",
                translation = "Kemudian kepada semua ahli kubur dari kaum Muslimin, Muslimat, Mukminin, dan Mukminat dari timur bumi sampai baratnya, di darat maupun di laut, khususnya kepada bapak-bapak kami, ibu-ibu kami, kakek-kakek kami, nenek-nenek kami, guru-guru kami, dan kepada orang yang menjadi sebab kami berkumpul di sini. Al-Fatihah…",
        )
    ),
),

// 8 ──────────────────────────────────────────────────────────────────
Section(
    id = "surah-al-ikhlas",
    number = 8,
    title = "Surah Al-Ikhlas",
    subtitle = "Dibaca 3 kali",
    type = SectionType.Surah,
    blocks = listOf(
        ContentBlock(
                arabic = "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ • قُلْ هُوَ اللّٰهُ أَحَدٌ • اَللّٰهُ الصَّمَدُ • لَمْ يَلِدْ وَلَمْ يُوْلَدْ • وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ",
                transliteration = "Bismillâhir-rahmânir-rahîm. Qul huwallâhu ahad. Allâhush-shamad. Lam yalid wa lam yûlad. Wa lam yakul lahû kufuwan ahad",
                translation = "Dengan menyebut nama Allah Yang Maha Pengasih lagi Maha Penyayang. Katakanlah (Muhammad), \"Dialah Allah, Yang Maha Esa. Allah tempat meminta segala sesuatu. Dia tidak beranak dan tidak pula diperanakkan. Dan tidak ada sesuatu yang setara dengan Dia.\"",
                note = "Al-Ikhlas (Surat ke-112)",
                repeat = 3,
        ),
        ContentBlock(
                arabic = "لَا إِلٰهَ إِلَّا اللّٰهُ وَاللّٰهُ أَكْبَرُ وَلِلّٰهِ الْحَمْدُ",
                transliteration = "Lâ ilâha illallâhu wallâhu akbar wa lillâhil-hamd",
                translation = "Tiada Tuhan selain Allah, Allah Maha Besar, dan hanya bagi Allah segala puji.",
                note = "Dibaca setiap selesai Al-Ikhlas, Al-Falaq, dan An-Nas",
        )
    ),
),

// 9 ──────────────────────────────────────────────────────────────────
Section(
    id = "surah-al-falaq",
    number = 9,
    title = "Surah Al-Falaq",
    subtitle = "Berlindung dari kejahatan makhluk",
    type = SectionType.Surah,
    blocks = listOf(
        ContentBlock(
                arabic = "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ • قُلْ أَعُوْذُ بِرَبِّ الْفَلَقِ • مِنْ شَرِّ مَا خَلَقَ • وَمِنْ شَرِّ غَاسِقٍ إِذَا وَقَبَ • وَمِنْ شَرِّ النَّفّٰثٰتِ فِي الْعُقَدِ • وَمِنْ شَرِّ حَاسِدٍ إِذَا حَسَدَ",
                transliteration = "Bismillâhir-rahmânir-rahîm. Qul a'ûdzu bi rabbil-falaq. Min syarri mâ khalaq. Wa min syarri ghâsiqin idzâ waqab. Wa min syarrin-naffâtsâti fil-'uqad. Wa min syarri hâsidin idzâ hasad",
                translation = "Dengan menyebut nama Allah Yang Maha Pengasih lagi Maha Penyayang. Katakanlah, \"Aku berlindung kepada Tuhan yang menguasai subuh, dari kejahatan makhluk yang Dia ciptakan, dan dari kejahatan malam apabila telah gelap gulita, dan dari kejahatan perempuan-perempuan penyihir yang meniup pada buhul-buhul, dan dari kejahatan orang yang dengki apabila dia dengki.\"",
                note = "Al-Falaq (Surat ke-113)",
        )
    ),
),

// 10 ──────────────────────────────────────────────────────────────────
Section(
    id = "surah-an-nas",
    number = 10,
    title = "Surah An-Nas",
    subtitle = "Berlindung dari bisikan setan",
    type = SectionType.Surah,
    blocks = listOf(
        ContentBlock(
                arabic = "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ • قُلْ أَعُوْذُ بِرَبِّ النَّاسِ • مَلِكِ النَّاسِ • إِلٰهِ النَّاسِ • مِنْ شَرِّ الْوَسْوَاسِ الْخَنَّاسِ • الَّذِيْ يُوَسْوِسُ فِيْ صُدُوْرِ النَّاسِ • مِنَ الْجِنَّةِ وَالنَّاسِ",
                transliteration = "Bismillâhir-rahmânir-rahîm. Qul a'ûdzu bi rabbin-nâs. Malikin-nâs. Ilâhin-nâs. Min syarril-waswâsil-khannâs. Alladzî yuwaswisu fî shudûrin-nâs. Minal-jinnati wan-nâs",
                translation = "Dengan menyebut nama Allah Yang Maha Pengasih lagi Maha Penyayang. Katakanlah, \"Aku berlindung kepada Tuhan manusia, Raja manusia, Sembahan manusia, dari kejahatan bisikan setan yang bersembunyi, yang membisikkan kejahatan ke dalam dada manusia, dari golongan jin dan manusia.\"",
                note = "An-Nas (Surat ke-114)",
        )
    ),
),

// 11 ──────────────────────────────────────────────────────────────────
Section(
    id = "al-baqarah-1-5",
    number = 11,
    title = "Surah Al-Baqarah Ayat 1–5",
    subtitle = "Sifat orang-orang yang bertakwa",
    type = SectionType.Ayat,
    blocks = listOf(
        ContentBlock(
                arabic = "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ • الۤمّۤ • ذٰلِكَ الْكِتٰبُ لَا رَيْبَ فِيْهِ هُدًى لِلْمُتَّقِيْنَ • الَّذِيْنَ يُؤْمِنُوْنَ بِالْغَيْبِ وَيُقِيْمُوْنَ الصَّلٰوةَ وَمِمَّا رَزَقْنٰهُمْ يُنْفِقُوْنَ • وَالَّذِيْنَ يُؤْمِنُوْنَ بِمَا أُنْزِلَ إِلَيْكَ وَمَا أُنْزِلَ مِنْ قَبْلِكَ وَبِالْاٰخِرَةِ هُمْ يُوْقِنُوْنَ • أُولٰئِكَ عَلٰى هُدًى مِنْ رَبِّهِمْ وَأُولٰئِكَ هُمُ الْمُفْلِحُوْنَ",
                transliteration = "Bismillâhir-rahmânir-rahîm. Alif lâm mîm. Dzâlikal-kitâbu lâ raiba fîhi hudal lil-muttaqîn. Alladzîna yu'minûna bil-ghaibi wa yuqîmûnash-shalâta wa mimmâ razaqnâhum yunfiqûn. Walladzîna yu'minûna bimâ unzila ilaika wa mâ unzila min qablika wa bil-âkhirati hum yûqinûn. Ulâ'ika 'alâ hudam mir rabbihim wa ulâ'ika humul-muflihûn",
                translation = "Dengan menyebut nama Allah Yang Maha Pengasih lagi Maha Penyayang. Alif Lam Mim. Kitab (Al-Qur'an) ini tidak ada keraguan di dalamnya; petunjuk bagi orang-orang yang bertakwa, yaitu orang-orang yang beriman pada yang gaib, menegakkan salat, dan menginfakkan sebagian rezeki yang Kami anugerahkan kepada mereka, dan mereka yang beriman pada (Al-Qur'an) yang diturunkan kepadamu dan kitab-kitab yang diturunkan sebelum engkau, dan mereka yakin akan adanya akhirat. Merekalah yang mendapat petunjuk dari Tuhannya dan mereka itulah orang-orang yang beruntung.",
                note = "QS Al-Baqarah: 1–5",
        )
    ),
),

// 12 ──────────────────────────────────────────────────────────────────
Section(
    id = "al-baqarah-163",
    number = 12,
    title = "Surah Al-Baqarah Ayat 163",
    subtitle = "Tauhid — Allah Tuhan Yang Maha Esa",
    type = SectionType.Ayat,
    blocks = listOf(
        ContentBlock(
                arabic = "وَإِلٰهُكُمْ إِلٰهٌ وَاحِدٌ لَا إِلٰهَ إِلَّا هُوَ الرَّحْمٰنُ الرَّحِيْمُ",
                transliteration = "Wa ilâhukum ilâhun wâhid, lâ ilâha illâ huwar-rahmânur-rahîm",
                translation = "Dan Tuhanmu adalah Tuhan Yang Maha Esa. Tidak ada Tuhan selain Dia Yang Maha Pengasih lagi Maha Penyayang.",
                note = "QS Al-Baqarah: 163",
        )
    ),
),

// 13 ──────────────────────────────────────────────────────────────────
Section(
    id = "ayat-kursi",
    number = 13,
    title = "Ayat Kursi",
    subtitle = "Surah Al-Baqarah Ayat 255 — puncak keagungan Allah",
    type = SectionType.Ayat,
    blocks = listOf(
        ContentBlock(
                arabic = "اَللّٰهُ لَا إِلٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّوْمُ، لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ، لَهُ مَا فِي السَّمٰوٰتِ وَمَا فِي الْأَرْضِ، مَنْ ذَا الَّذِيْ يَشْفَعُ عِنْدَهُ إِلَّا بِإِذْنِهِ، يَعْلَمُ مَا بَيْنَ أَيْدِيْهِمْ وَمَا خَلْفَهُمْ، وَلَا يُحِيْطُوْنَ بِشَيْءٍ مِنْ عِلْمِهِ إِلَّا بِمَا شَاءَ، وَسِعَ كُرْسِيُّهُ السَّمٰوٰتِ وَالْأَرْضَ، وَلَا يَئُوْدُهُ حِفْظُهُمَا، وَهُوَ الْعَلِيُّ الْعَظِيْمُ",
                transliteration = "Allâhu lâ ilâha illâ huwal-hayyul-qayyûm. Lâ ta'khudzuhû sinatuw wa lâ naum. Lahû mâ fis-samâwâti wa mâ fil-ardh. Man dzal-ladzî yasyfa'u 'indahû illâ bi idznih. Ya'lamu mâ baina aidîhim wa mâ khalfahum. Wa lâ yuhîthûna bi syai'in min 'ilmihî illâ bimâ syâ'. Wasi'a kursiyyuhus-samâwâti wal-ardh. Wa lâ ya'ûduhû hifzhuhumâ. Wa huwal-'aliyyul-'azhîm",
                translation = "Allah, tidak ada Tuhan selain Dia, Yang Maha Hidup lagi terus-menerus mengurus makhluk-Nya. Dia tidak mengantuk dan tidak tidur. Milik-Nya apa yang ada di langit dan di bumi. Tidak ada yang dapat memberi syafaat di sisi-Nya tanpa izin-Nya. Dia mengetahui apa yang ada di hadapan mereka dan apa yang ada di belakang mereka, dan mereka tidak mengetahui sesuatu pun tentang ilmu-Nya melainkan apa yang Dia kehendaki. Kursi-Nya meliputi langit dan bumi, dan Dia tidak merasa berat memelihara keduanya. Dan Dia Maha Tinggi lagi Maha Agung.",
                note = "QS Al-Baqarah: 255",
        )
    ),
),

// 14 ──────────────────────────────────────────────────────────────────
Section(
    id = "al-baqarah-284-286",
    number = 14,
    title = "Surah Al-Baqarah Ayat 284–286",
    subtitle = "Penutup Al-Baqarah — doa beban dan ampunan",
    type = SectionType.Ayat,
    blocks = listOf(
        ContentBlock(
                arabic = "لِلّٰهِ مَا فِي السَّمٰوٰتِ وَمَا فِي الْأَرْضِ، وَإِنْ تُبْدُوْا مَا فِيْ أَنْفُسِكُمْ أَوْ تُخْفُوْهُ يُحَاسِبْكُمْ بِهِ اللّٰهُ، فَيَغْفِرُ لِمَنْ يَشَاءُ وَيُعَذِّبُ مَنْ يَشَاءُ، وَاللّٰهُ عَلٰى كُلِّ شَيْءٍ قَدِيْرٌ",
                transliteration = "Lillâhi mâ fis-samâwâti wa mâ fil-ardh. Wa in tubdû mâ fî anfusikum au tukhfûhu yuhâsibkum bihillâh. Fayaghfiru limay yasyâ'u wa yu'adzdzibu may yasyâ'. Wallâhu 'alâ kulli syai'in qadîr",
                translation = "Milik Allah apa yang ada di langit dan apa yang ada di bumi. Jika kamu menyatakan apa yang ada di dalam hatimu atau kamu menyembunyikannya, niscaya Allah memperhitungkannya bagimu. Dia mengampuni siapa yang Dia kehendaki dan mengazab siapa yang Dia kehendaki. Allah Maha Kuasa atas segala sesuatu.",
                note = "QS Al-Baqarah: 284",
        ),
        ContentBlock(
                arabic = "اٰمَنَ الرَّسُوْلُ بِمَا أُنْزِلَ إِلَيْهِ مِنْ رَبِّهِ وَالْمُؤْمِنُوْنَ، كُلٌّ اٰمَنَ بِاللّٰهِ وَمَلَائِكَتِهِ وَكُتُبِهِ وَرُسُلِهِ، لَا نُفَرِّقُ بَيْنَ أَحَدٍ مِنْ رُسُلِهِ، وَقَالُوْا سَمِعْنَا وَأَطَعْنَا غُفْرَانَكَ رَبَّنَا وَإِلَيْكَ الْمَصِيْرُ",
                transliteration = "Âmanar-rasûlu bimâ unzila ilaihi mir rabbihî wal-mu'minûn. Kullun âmana billâhi wa malâ'ikatihî wa kutubihî wa rusulih. Lâ nufarriqu baina ahadim mir rusulih. Wa qâlû sami'nâ wa atha'nâ, ghufrânaka rabbanâ wa ilaikal-mashîr",
                translation = "Rasul telah beriman pada apa yang diturunkan kepadanya dari Tuhannya, demikian pula orang-orang mukmin. Semuanya beriman kepada Allah, malaikat-malaikat-Nya, kitab-kitab-Nya, dan rasul-rasul-Nya. Kami tidak membeda-bedakan seorang pun dari rasul-rasul-Nya. Mereka berkata, \"Kami dengar dan kami taat. Ampunilah kami, ya Tuhan kami, dan hanya kepada-Mu tempat kembali.\"",
                note = "QS Al-Baqarah: 285",
        ),
        ContentBlock(
                arabic = "لَا يُكَلِّفُ اللّٰهُ نَفْسًا إِلَّا وُسْعَهَا، لَهَا مَا كَسَبَتْ وَعَلَيْهَا مَا اكْتَسَبَتْ، رَبَّنَا لَا تُؤَاخِذْنَا إِنْ نَسِيْنَا أَوْ أَخْطَأْنَا، رَبَّنَا وَلَا تَحْمِلْ عَلَيْنَا إِصْرًا كَمَا حَمَلْتَهُ عَلَى الَّذِيْنَ مِنْ قَبْلِنَا، رَبَّنَا وَلَا تُحَمِّلْنَا مَا لَا طَاقَةَ لَنَا بِهِ",
                transliteration = "Lâ yukallifullâhu nafsan illâ wus'ahâ. Lahâ mâ kasabat wa 'alaihâ maktasabat. Rabbanâ lâ tu'âkhidznâ in nasînâ au akhtha'nâ. Rabbanâ wa lâ tahmil 'alainâ ishran kamâ hamaltahû 'alal-ladzîna min qablinâ. Rabbanâ wa lâ tuhammilnâ mâ lâ thâqata lanâ bih",
                translation = "Allah tidak membebani seseorang melainkan sesuai kesanggupannya. Dia mendapat pahala dari kebajikan yang dikerjakannya dan mendapat siksa dari kejahatan yang diperbuatnya. Ya Tuhan kami, janganlah Engkau hukum kami jika kami lupa atau bersalah. Ya Tuhan kami, janganlah Engkau bebani kami beban yang berat sebagaimana Engkau bebani orang-orang sebelum kami. Ya Tuhan kami, janganlah Engkau pikulkan kepada kami apa yang tidak sanggup kami memikulnya.",
                note = "QS Al-Baqarah: 286 (awal)",
        ),
        ContentBlock(
                arabic = "وَاعْفُ عَنَّا وَاغْفِرْ لَنَا وَارْحَمْنَا • أَنْتَ مَوْلَانَا فَانْصُرْنَا عَلَى الْقَوْمِ الْكٰفِرِيْنَ",
                transliteration = "Wa'fu 'annâ waghfir lanâ warhamnâ. Anta maulânâ fanshurnâ 'alal-qaumil-kâfirîn, bi rahmatika yâ arhamar-râhimîn",
                translation = "Maafkanlah kami, ampunilah kami, dan rahmatilah kami. Engkaulah pelindung kami, maka tolonglah kami menghadapi kaum kafir, dengan rahmat-Mu wahai Zat Yang Paling Penyayang di antara para penyayang.",
                note = "QS Al-Baqarah: 286 (penutup) — 'wa'fu 'annâ waghfir lanâ warhamnâ' diulang 7 kali",
                repeat = 7,
        )
    ),
),

// 15 ──────────────────────────────────────────────────────────────────
Section(
    id = "irhamna-ya-arhamar-rahimin",
    number = 15,
    title = "Irhamna Ya Arhamar Rahimin",
    subtitle = "Memohon rahmat Allah, dibaca 7 kali",
    type = SectionType.Dzikir,
    blocks = listOf(
        ContentBlock(
                arabic = "اِرْحَمْنَا يَا أَرْحَمَ الرَّاحِمِيْنَ",
                transliteration = "Irhamnâ yâ arhamar-râhimîn",
                translation = "Rahmatilah kami, wahai Zat Yang Paling Penyayang di antara para penyayang.",
                repeat = 7,
        )
    ),
),

// 16 ──────────────────────────────────────────────────────────────────
Section(
    id = "rahmat-allah-ahlul-bait",
    number = 16,
    title = "Rahmat Allah bagi Ahlul Bait",
    subtitle = "QS Hud: 73 & QS Al-Ahzab: 33",
    type = SectionType.Ayat,
    blocks = listOf(
        ContentBlock(
                arabic = "رَحْمَةُ اللّٰهِ وَبَرَكَاتُهُ عَلَيْكُمْ أَهْلَ الْبَيْتِ، إِنَّهُ حَمِيْدٌ مَجِيْدٌ",
                transliteration = "Rahmatullâhi wa barakâtuhû 'alaikum ahlal-bait, innahû hamîdum majîd",
                translation = "Rahmat Allah dan berkah-Nya semoga tercurah atas kamu, wahai ahlul bait. Sesungguhnya Dia Maha Terpuji lagi Maha Mulia.",
                note = "QS Hud: 73",
        ),
        ContentBlock(
                arabic = "إِنَّمَا يُرِيْدُ اللّٰهُ لِيُذْهِبَ عَنْكُمُ الرِّجْسَ أَهْلَ الْبَيْتِ وَيُطَهِّرَكُمْ تَطْهِيْرًا",
                transliteration = "Innamâ yurîdullâhu liyudzhiba 'ankumur-rijsa ahlal-baiti wa yuthahhirakum tathhîrâ",
                translation = "Sesungguhnya Allah hendak menghilangkan dosa dari kamu, wahai ahlul bait, dan membersihkan kamu sebersih-bersihnya.",
                note = "QS Al-Ahzab: 33",
        )
    ),
),

// 17 ──────────────────────────────────────────────────────────────────
Section(
    id = "perintah-bersholawat",
    number = 17,
    title = "Perintah Bersholawat",
    subtitle = "Surah Al-Ahzab Ayat 56",
    type = SectionType.Ayat,
    blocks = listOf(
        ContentBlock(
                arabic = "إِنَّ اللّٰهَ وَمَلَائِكَتَهُ يُصَلُّوْنَ عَلَى النَّبِيِّ، يَا أَيُّهَا الَّذِيْنَ اٰمَنُوْا صَلُّوْا عَلَيْهِ وَسَلِّمُوْا تَسْلِيْمًا",
                transliteration = "Innallâha wa malâ'ikatahû yushallûna 'alan-nabiyy. Yâ ayyuhal-ladzîna âmanû shallû 'alaihi wa sallimû taslîmâ",
                translation = "Sesungguhnya Allah dan para malaikat-Nya bersholawat untuk Nabi. Wahai orang-orang yang beriman, bersholawatlah kamu untuk Nabi dan ucapkanlah salam penghormatan kepadanya.",
                note = "QS Al-Ahzab: 56",
        )
    ),
),

// 18 ──────────────────────────────────────────────────────────────────
Section(
    id = "sholawat-afdhal",
    number = 18,
    title = "Sholawat Afdhal",
    subtitle = "3 kali dengan 3 Sebutan untuk Nabi Muhammad ﷺ",
    type = SectionType.Sholawat,
    blocks = listOf(
        ContentBlock(
                arabic = "اَللّٰهُمَّ صَلِّ أَفْضَلَ الصَّلَاةِ عَلَى أَسْعَدِ مَخْلُوْقَاتِكَ نُوْرِ الْهُدَى سَيِّدِنَا مُحَمَّدٍ وَعَلَى اٰلِهِ وَصَحْبِهِ وَسَلِّمْ، عَدَدَ مَعْلُوْمَاتِكَ وَمِدَادَ كَلِمَاتِكَ كُلَّمَا ذَكَرَكَ الذَّاكِرُوْنَ وَغَفَلَ عَنْ ذِكْرِكَ الْغَافِلُوْنَ",
                transliteration = "Allâhumma shalli afdhalash-shalâti 'alâ as'adi makhlûqâtika nûril-hudâ sayyidinâ Muhammadin wa 'alâ âlihi wa shahbihi wa sallim, 'adada ma'lûmâtika wa midâda kalimâtika kullamâ dzakarakadz-dzâkirûna wa ghafala 'an dzikrikal-ghâfilûn",
                translation = "Ya Allah, limpahkanlah sholawat yang paling utama kepada makhluk-Mu yang paling bahagia, cahaya petunjuk, junjungan kami Nabi Muhammad, serta kepada keluarga dan sahabatnya, sebanyak pengetahuan-Mu dan sebanyak tinta kalimat-kalimat-Mu, setiap kali orang-orang yang berdzikir mengingat-Mu dan setiap kali orang-orang yang lalai lupa mengingat-Mu.",
                note = "Sebutan ke-1: Nûrul-Hudâ (Cahaya Petunjuk)",
                repeat = 3,
        ),
        ContentBlock(
                arabic = "اَللّٰهُمَّ صَلِّ أَفْضَلَ الصَّلَاةِ عَلَى أَسْعَدِ مَخْلُوْقَاتِكَ شَمْسِ الضُّحَى سَيِّدِنَا مُحَمَّدٍ وَعَلَى اٰلِهِ وَصَحْبِهِ وَسَلِّمْ، عَدَدَ مَعْلُوْمَاتِكَ وَمِدَادَ كَلِمَاتِكَ كُلَّمَا ذَكَرَكَ الذَّاكِرُوْنَ وَغَفَلَ عَنْ ذِكْرِكَ الْغَافِلُوْنَ",
                transliteration = "Allâhumma shalli afdhalash-shalâti 'alâ as'adi makhlûqâtika syamsidh-dhuhâ sayyidinâ Muhammadin wa 'alâ âlihi wa shahbihi wa sallim, 'adada ma'lûmâtika wa midâda kalimâtika kullamâ dzakarakadz-dzâkirûna wa ghafala 'an dzikrikal-ghâfilûn",
                translation = "Ya Allah, limpahkanlah sholawat yang paling utama kepada makhluk-Mu yang paling bahagia, matahari dhuha, junjungan kami Nabi Muhammad, serta kepada keluarga dan sahabatnya, sebanyak pengetahuan-Mu dan sebanyak tinta kalimat-kalimat-Mu, setiap kali orang-orang yang berdzikir mengingat-Mu dan setiap kali orang-orang yang lalai lupa mengingat-Mu.",
                note = "Sebutan ke-2: Syamsidh-Dhuhâ (Matahari Dhuha)",
                repeat = 3,
        ),
        ContentBlock(
                arabic = "اَللّٰهُمَّ صَلِّ أَفْضَلَ الصَّلَاةِ عَلَى أَسْعَدِ مَخْلُوْقَاتِكَ بَدْرِ الدُّجَى سَيِّدِنَا مُحَمَّدٍ وَعَلَى اٰلِهِ وَصَحْبِهِ وَسَلِّمْ، عَدَدَ مَعْلُوْمَاتِكَ وَمِدَادَ كَلِمَاتِكَ كُلَّمَا ذَكَرَكَ الذَّاكِرُوْنَ وَغَفَلَ عَنْ ذِكْرِكَ الْغَافِلُوْنَ",
                transliteration = "Allâhumma shalli afdhalash-shalâti 'alâ as'adi makhlûqâtika badrid-dujâ sayyidinâ Muhammadin wa 'alâ âlihi wa shahbihi wa sallim, 'adada ma'lûmâtika wa midâda kalimâtika kullamâ dzakarakadz-dzâkirûna wa ghafala 'an dzikrikal-ghâfilûn",
                translation = "Ya Allah, limpahkanlah sholawat yang paling utama kepada makhluk-Mu yang paling bahagia, purnama di kegelapan malam, junjungan kami Nabi Muhammad, serta kepada keluarga dan sahabatnya, sebanyak pengetahuan-Mu dan sebanyak tinta kalimat-kalimat-Mu, setiap kali orang-orang yang berdzikir mengingat-Mu dan setiap kali orang-orang yang lalai lupa mengingat-Mu.",
                note = "Sebutan ke-3: Badrid-Dujâ (Purnama di Kegelapan Malam)",
                repeat = 3,
        ),
        ContentBlock(
                arabic = "وَسَلِّمْ وَرَضِيَ اللّٰهُ تَبَارَكَ وَتَعَالَى عَنْ سَادَاتِنَا أَصْحَابِ رَسُوْلِ اللّٰهِ أَجْمَعِيْنَ",
                transliteration = "Wa sallim wa radhiyallâhu tabâraka wa ta'âlâ 'an sâdâtinâ ashhâbi rasûlillâhi ajma'în",
                translation = "Dan limpahkanlah keselamatan, dan semoga Allah Yang Maha Memberkahi lagi Maha Tinggi meridhai junjungan-junjungan kami, para sahabat Rasulullah seluruhnya.",
        )
    ),
),

// 19 ──────────────────────────────────────────────────────────────────
Section(
    id = "hasbunallah-la-hawla",
    number = 19,
    title = "Hasbunallah dan La Hawla",
    subtitle = "Pengakuan tawakkal dan kepasrahan kepada Allah",
    type = SectionType.Dzikir,
    blocks = listOf(
        ContentBlock(
                arabic = "حَسْبُنَا اللّٰهُ وَنِعْمَ الْوَكِيْلُ، نِعْمَ الْمَوْلَى وَنِعْمَ النَّصِيْرُ",
                transliteration = "Hasbunallâhu wa ni'mal-wakîl, ni'mal-maulâ wa ni'man-nashîr",
                translation = "Cukuplah Allah bagi kami, dan Dia adalah sebaik-baik pelindung. Dia adalah sebaik-baik pemimpin dan sebaik-baik penolong.",
        ),
        ContentBlock(
                arabic = "وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللّٰهِ الْعَلِيِّ الْعَظِيْمِ",
                transliteration = "Wa lâ hawla wa lâ quwwata illâ billâhil-'aliyyil-'azhîm",
                translation = "Dan tidak ada daya dan kekuatan kecuali dengan pertolongan Allah Yang Maha Tinggi lagi Maha Agung.",
        )
    ),
),

// 20 ──────────────────────────────────────────────────────────────────
Section(
    id = "istighfar-sholawat-pengantar-tahlil",
    number = 20,
    title = "Istighfar dan Sholawat",
    subtitle = "Pengantar Tahlil",
    type = SectionType.Sholawat,
    blocks = listOf(
        ContentBlock(
                arabic = "أَسْتَغْفِرُ اللّٰهَ الْعَظِيْمَ",
                transliteration = "Astaghfirullâhal-'azhîm",
                translation = "Saya mohon ampun kepada Allah Yang Maha Agung.",
                repeat = 3,
        ),
        ContentBlock(
                arabic = "اَللّٰهُمَّ صَلِّ عَلَى سَيِّدِنَا مُحَمَّدٍ، اَللّٰهُمَّ صَلِّ عَلَيْهِ وَسَلِّمْ",
                transliteration = "Allâhumma shalli 'alâ sayyidinâ Muhammad. Allâhumma shalli 'alaihi wa sallim",
                translation = "Ya Allah, limpahkanlah rahmat kepada junjungan kami Nabi Muhammad. Ya Allah, limpahkanlah rahmat dan keselamatan kepadanya.",
                repeat = 3,
        )
    ),
),

// 21 ──────────────────────────────────────────────────────────────────
Section(
    id = "afdhaludz-dzikr",
    number = 21,
    title = "Afdhaludz Dzikr",
    subtitle = "Sebaik-baik dzikir: La ilaha illallah",
    type = SectionType.Dzikir,
    blocks = listOf(
        ContentBlock(
                arabic = "أَفْضَلُ الذِّكْرِ فَاعْلَمْ أَنَّهُ لَا إِلٰهَ إِلَّا اللّٰهُ، حَيٌّ مَوْجُوْدٌ",
                transliteration = "Afdhaludz-dzikri fa'lam annahû lâ ilâha illallâh, hayyum maujûd",
                translation = "Sebaik-baik dzikir, ketahuilah, adalah \"Tiada Tuhan selain Allah\", Zat Yang Maha Hidup dan Wujud (ada).",
        ),
        ContentBlock(
                arabic = "لَا إِلٰهَ إِلَّا اللّٰهُ، حَيٌّ مَعْبُوْدٌ",
                transliteration = "Lâ ilâha illallâh, hayyum ma'bûd",
                translation = "Tiada Tuhan selain Allah, Zat Yang Maha Hidup dan disembah.",
        ),
        ContentBlock(
                arabic = "لَا إِلٰهَ إِلَّا اللّٰهُ، حَيٌّ بَاقٍ",
                transliteration = "Lâ ilâha illallâh, hayyum bâq",
                translation = "Tiada Tuhan selain Allah, Zat Yang Maha Hidup dan kekal.",
        ),
        ContentBlock(
                arabic = "مُحَمَّدٌ رَسُوْلُ اللّٰهِ صَلَّى اللّٰهُ عَلَيْهِ وَسَلَّمَ",
                transliteration = "Muhammadur rasûlullâh shallallâhu 'alaihi wa sallam",
                translation = "Nabi Muhammad adalah utusan Allah, semoga rahmat dan keselamatan Allah tercurah kepadanya.",
        )
    ),
),

// 22 ──────────────────────────────────────────────────────────────────
Section(
    id = "tahlil-la-ilaha-illallah",
    number = 22,
    title = "Tahlil — La Ilaha Illallah",
    subtitle = "Inti tahlil, dibaca sekitar 100 kali",
    type = SectionType.Tahlil,
    blocks = listOf(
        ContentBlock(
                arabic = "لَا إِلٰهَ إِلَّا اللّٰهُ",
                transliteration = "Lâ ilâha illallâh",
                translation = "Tiada Tuhan yang berhak disembah selain Allah.",
                note = "Diulang sekitar 100 kali. Pada beberapa bagian disambung: Muhammadur rasûlullâh shallallâhu 'alaihi wa sallam — Muhammad adalah utusan Allah, semoga rahmat dan keselamatan Allah tercurah kepadanya.",
                repeat = 100,
        )
    ),
),

// 23 ──────────────────────────────────────────────────────────────────
Section(
    id = "sholawat-pendek-setelah-tahlil",
    number = 23,
    title = "Sholawat Pendek",
    subtitle = "Setelah Tahlil, dibaca berulang",
    type = SectionType.Sholawat,
    blocks = listOf(
        ContentBlock(
                arabic = "اَللّٰهُمَّ صَلِّ عَلَى سَيِّدِنَا مُحَمَّدٍ، اَللّٰهُمَّ صَلِّ عَلَيْهِ وَسَلِّمْ",
                transliteration = "Allâhumma shalli 'alâ sayyidinâ Muhammad. Allâhumma shalli 'alaihi wa sallim",
                translation = "Ya Allah, limpahkanlah rahmat kepada junjungan kami Nabi Muhammad. Ya Allah, limpahkanlah rahmat dan keselamatan kepadanya.",
                note = "Dibaca berulang setelah tahlil.",
        )
    ),
),

// 24 ──────────────────────────────────────────────────────────────────
Section(
    id = "dua-kalimat-ar-rahman",
    number = 24,
    title = "Dua Kalimat yang Dicintai Ar-Rahman",
    subtitle = "Tasbih — hadits riwayat Bukhari & Muslim",
    type = SectionType.Dzikir,
    blocks = listOf(
        ContentBlock(
                arabic = "قَالَ رَسُوْلُ اللّٰهِ صَلَّى اللّٰهُ عَلَيْهِ وَسَلَّمَ: كَلِمَتَانِ حَبِيْبَتَانِ إِلَى الرَّحْمٰنِ، خَفِيْفَتَانِ عَلَى اللِّسَانِ، ثَقِيْلَتَانِ فِي الْمِيْزَانِ: سُبْحٰنَ اللّٰهِ وَبِحَمْدِهِ، سُبْحٰنَ اللّٰهِ الْعَظِيْمِ",
                transliteration = "Qâla rasûlullâhi shallallâhu 'alaihi wa sallam: kalimatâni habîbatâni ilar-rahmân, khafîfatâni 'alal-lisân, tsaqîlatâni fil-mîzân: subhânallâhi wa bi hamdih, subhânallâhil-'azhîm",
                translation = "Rasulullah ﷺ bersabda: \"Dua kalimat yang dicintai oleh Zat Yang Maha Pengasih, ringan diucapkan di lisan, dan berat dalam timbangan amal: Maha Suci Allah dan dengan memuji-Nya, Maha Suci Allah Yang Maha Agung.\"",
                note = "Tasbihnya kemudian diulang-ulang.",
        )
    ),
),

// 25 ──────────────────────────────────────────────────────────────────
Section(
    id = "sholawat-habib-rasul",
    number = 25,
    title = "Sholawat kepada Habib dan Rasul",
    subtitle = "Sholawat untuk Nabi sebagai kekasih dan utusan Allah",
    type = SectionType.Sholawat,
    blocks = listOf(
        ContentBlock(
                arabic = "اَللّٰهُمَّ صَلِّ عَلَى حَبِيْبِكَ سَيِّدِنَا مُحَمَّدٍ وَعَلَى اٰلِهِ وَصَحْبِهِ وَسَلِّمْ أَجْمَعِيْنَ",
                transliteration = "Allâhumma shalli 'alâ habîbika sayyidinâ Muhammadin wa 'alâ âlihi wa shahbihi wa sallim ajma'în",
                translation = "Ya Allah, limpahkanlah rahmat dan keselamatan kepada kekasih-Mu, junjungan kami Nabi Muhammad, serta kepada keluarga dan sahabatnya seluruhnya.",
        ),
        ContentBlock(
                arabic = "اَللّٰهُمَّ صَلِّ عَلَى رَسُوْلِكَ وَحَبِيْبِكَ سَيِّدِنَا مُحَمَّدٍ وَعَلَى اٰلِهِ وَصَحْبِهِ وَسَلِّمْ أَجْمَعِيْنَ",
                transliteration = "Allâhumma shalli 'alâ rasûlika wa habîbika sayyidinâ Muhammadin wa 'alâ âlihi wa shahbihi wa sallim ajma'în",
                translation = "Ya Allah, limpahkanlah rahmat dan keselamatan kepada utusan-Mu dan kekasih-Mu, junjungan kami Nabi Muhammad, serta kepada keluarga dan sahabatnya seluruhnya.",
        )
    ),
),

// 26 ──────────────────────────────────────────────────────────────────
Section(
    id = "al-fatihah-pengantar-doa-penutup",
    number = 26,
    title = "Al-Fatihah — Pengantar Doa Penutup",
    subtitle = "Dilanjutkan membaca Surah Al-Fatihah",
    type = SectionType.Surah,
    blocks = listOf(
        ContentBlock(
                arabic = "اَلْفَاتِحَةُ",
                transliteration = "Al-Fâtihah",
                translation = "Dilanjutkan membaca Surah Al-Fatihah (lihat teks lengkap di section 4).",
                note = "Lihat section 4 (Surah Al-Fatihah) untuk teks lengkap.",
        ),
        ContentBlock(
                arabic = "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ • اَلْحَمْدُ لِلّٰهِ رَبِّ الْعٰلَمِيْنَ • الرَّحْمٰنِ الرَّحِيْمِ • مٰلِكِ يَوْمِ الدِّيْنِ • إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِيْنُ • اِهْدِنَا الصِّرَاطَ الْمُسْتَقِيْمَ • صِرَاطَ الَّذِيْنَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوْبِ عَلَيْهِمْ وَلَا الضَّاۤلِّيْنَ • رَبِّ اغْفِرْ لِيْ وَلِوَالِدَيَّ وَلِلْمُؤْمِنِيْنَ اٰمِيْنَ",
                transliteration = "Bismillâhir-rahmânir-rahîm. Alhamdu lillâhi rabbil-'âlamîn. Ar-rahmânir-rahîm. Mâliki yaumid-dîn. Iyyâka na'budu wa iyyâka nasta'în. Ihdinash-shirâthal-mustaqîm. Shirâthal-ladzîna an'amta 'alaihim, ghairil-maghdhûbi 'alaihim wa ladh-dhâllîn. Rabbighfir lî wa liwâlidayya wa lil-mu'minîna, âmîn",
                translation = "Dengan menyebut nama Allah Yang Maha Pengasih lagi Maha Penyayang. Segala puji bagi Allah, Tuhan semesta alam. Yang Maha Pengasih lagi Maha Penyayang. Yang menguasai hari pembalasan. Hanya kepada-Mu kami menyembah dan hanya kepada-Mu kami mohon pertolongan. Tunjukilah kami jalan yang lurus, yaitu jalan orang-orang yang telah Engkau beri nikmat, bukan jalan mereka yang dimurkai dan bukan pula jalan mereka yang sesat. Ya Tuhanku, ampunilah aku, kedua orang tuaku, dan orang-orang mukmin. Kabulkanlah.",
                note = "Al-Fatihah (Surat ke-1)",
        )
    ),
),

// 27 ──────────────────────────────────────────────────────────────────
Section(
    id = "doa-tahlil-pembuka-doa",
    number = 27,
    title = "Doa Tahlil — Pembuka Doa",
    subtitle = "Hamdalah dan pujian kepada Allah",
    type = SectionType.Doa,
    blocks = listOf(
        ContentBlock(
                arabic = "أَعُوْذُ بِاللّٰهِ مِنَ الشَّيْطَانِ الرَّجِيْمِ، بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ، اَلْحَمْدُ لِلّٰهِ رَبِّ الْعٰلَمِيْنَ، حَمْدَ الشَّاكِرِيْنَ حَمْدَ النَّاعِمِيْنَ، حَمْدًا يُوَافِيْ نِعَمَهُ وَيُكَافِئُ مَزِيْدَهُ، يَا رَبَّنَا لَكَ الْحَمْدُ كَمَا يَنْبَغِيْ لِجَلَالِ وَجْهِكَ وَعَظِيْمِ سُلْطَانِكَ",
                transliteration = "A'ûdzu billâhi minasy-syaithânir-rajîm. Bismillâhir-rahmânir-rahîm. Alhamdu lillâhi rabbil-'âlamîn, hamdasy-syâkirîn hamdan-nâ'imîn, hamdan yuwâfî ni'amahû wa yukâfi'u mazîdah. Yâ rabbanâ lakal-hamdu kamâ yanbaghî li jalâli wajhika wa 'azhîmi sulthânik",
                translation = "Aku berlindung kepada Allah dari godaan setan yang terkutuk. Dengan menyebut nama Allah Yang Maha Pengasih lagi Maha Penyayang. Segala puji bagi Allah, Tuhan semesta alam, dengan pujian orang-orang yang bersyukur dan pujian orang-orang yang memperoleh nikmat, pujian yang sebanding dengan nikmat-nikmat-Nya dan menjamin tambahannya. Wahai Tuhan kami, hanya bagi-Mu segala puji, sebagaimana yang patut bagi keluhuran wajah-Mu dan keagungan kekuasaan-Mu.",
        )
    ),
),

// 28 ──────────────────────────────────────────────────────────────────
Section(
    id = "sholawat-munjiyat",
    number = 28,
    title = "Sholawat Munjiyat",
    subtitle = "Sholawat yang menyelamatkan dari segala bencana",
    type = SectionType.Sholawat,
    blocks = listOf(
        ContentBlock(
                arabic = "اَللّٰهُمَّ صَلِّ عَلَى سَيِّدِنَا مُحَمَّدٍ صَلَاةً تُنْجِيْنَا بِهَا مِنْ جَمِيْعِ الْأَهْوَالِ وَالْاٰفَاتِ، وَتَقْضِيْ لَنَا بِهَا جَمِيْعَ الْحَاجَاتِ، وَتُطَهِّرُنَا بِهَا مِنْ جَمِيْعِ السَّيِّئَاتِ، وَتَرْفَعُنَا بِهَا عِنْدَكَ أَعْلَى الدَّرَجَاتِ، وَتُبَلِّغُنَا بِهَا أَقْصَى الْغَايَاتِ مِنْ جَمِيْعِ الْخَيْرَاتِ فِي الْحَيَاةِ وَبَعْدَ الْمَمَاتِ، إِنَّكَ عَلَى كُلِّ شَيْءٍ قَدِيْرٌ",
                transliteration = "Allâhumma shalli 'alâ sayyidinâ Muhammadin shalâtan tunjînâ bihâ min jamî'il-ahwâli wal-âfât, wa taqdhî lanâ bihâ jamî'al-hâjât, wa tuthahhirunâ bihâ min jamî'is-sayyi'ât, wa tarfa'unâ bihâ 'indaka a'lad-darajât, wa tuballighunâ bihâ aqshal-ghâyâti min jamî'il-khairâti fil-hayâti wa ba'dal-mamât, innaka 'alâ kulli syai'in qadîr",
                translation = "Ya Allah, limpahkanlah rahmat kepada junjungan kami Nabi Muhammad, dengan rahmat yang menyelamatkan kami dari segala ketakutan dan bencana, yang dengan rahmat itu Engkau kabulkan segala hajat kami, Engkau sucikan kami dari segala keburukan, Engkau angkat kami ke derajat tertinggi di sisi-Mu, dan Engkau sampaikan kami ke puncak segala kebaikan dalam hidup dan setelah mati. Sesungguhnya Engkau Maha Kuasa atas segala sesuatu.",
        )
    ),
),

// 29 ──────────────────────────────────────────────────────────────────
Section(
    id = "doa-tahlil-pahala-bacaan",
    number = 29,
    title = "Doa Tahlil — Menyampaikan Pahala Bacaan",
    subtitle = "Menghadiahkan pahala bacaan kepada Nabi, para nabi, wali, dan arwah",
    type = SectionType.Doa,
    blocks = listOf(
        ContentBlock(
                arabic = "اَللّٰهُمَّ تَقَبَّلْ وَأَوْصِلْ ثَوَابَ مَا قَرَأْنَاهُ مِنَ الْقُرْآنِ الْعَظِيْمِ وَمَا هَلَّلْنَا وَمَا سَبَّحْنَا وَمَا صَلَّيْنَا عَلَى سَيِّدِنَا مُحَمَّدٍ صَلَّى اللّٰهُ عَلَيْهِ وَسَلَّمَ هَدِيَّةً وَاصِلَةً وَرَحْمَةً نَازِلَةً وَبَرَكَةً شَامِلَةً إِلَى حَضْرَةِ حَبِيْبِنَا وَشَفِيْعِنَا وَقُرَّةِ أَعْيُنِنَا سَيِّدِنَا وَمَوْلَانَا مُحَمَّدٍ صَلَّى اللّٰهُ عَلَيْهِ وَسَلَّمَ، وَإِلَى جَمِيْعِ إِخْوَانِهِ مِنَ الْأَنْبِيَاءِ وَالْمُرْسَلِيْنَ وَالْأَوْلِيَاءِ وَالشُّهَدَاءِ وَالصَّالِحِيْنَ وَالصَّحَابَةِ وَالتَّابِعِيْنَ وَالْعُلَمَاءِ الْعَامِلِيْنَ وَالْمُصَنِّفِيْنَ الْمُخْلِصِيْنَ وَجَمِيْعِ الْمُجَاهِدِيْنَ فِيْ سَبِيْلِ اللّٰهِ رَبِّ الْعٰلَمِيْنَ وَالْمَلَائِكَةِ الْمُقَرَّبِيْنَ",
                transliteration = "Allâhumma taqabbal wa aushil tsawâba mâ qara'nâhu minal-qur'ânil-'azhîm, wa mâ hallalnâ, wa mâ sabbahnâ, wa mâ shallainâ 'alâ sayyidinâ Muhammadin shallallâhu 'alaihi wa sallam, hadiyyatan wâshilatan wa rahmatan nâzilatan wa barakatan syâmilatan ilâ hadhrati habîbinâ wa syafî'inâ wa qurrati a'yuninâ sayyidinâ wa maulânâ Muhammadin shallallâhu 'alaihi wa sallam, wa ilâ jamî'i ikhwânihi minal-anbiyâ'i wal-mursalîn, wal-auliyâ'i wasy-syuhadâ'i wash-shâlihîn, wash-shahâbati wat-tâbi'în, wal-'ulamâ'il-'âmilîn, wal-mushannifînal-mukhlishîn, wa jamî'il-mujâhidîna fî sabîlillâhi rabbil-'âlamîn, wal-malâ'ikatil-muqarrabîn",
                translation = "Ya Allah, terimalah dan sampaikanlah pahala Al-Qur'an yang agung yang telah kami baca, pahala tahlil kami, tasbih kami, dan sholawat kami kepada junjungan kami Nabi Muhammad ﷺ, sebagai hadiah yang sampai, rahmat yang turun, dan berkah yang menyeluruh, kepada kekasih kami, pemberi syafaat kami, dan penyejuk mata kami, junjungan dan pemimpin kami Muhammad ﷺ, dan kepada semua saudara beliau dari kalangan para nabi, rasul, wali, syuhada, orang-orang saleh, sahabat, tabi'in, ulama yang mengamalkan ilmunya, para pengarang yang ikhlas, semua pejuang di jalan Allah Tuhan semesta alam, dan para malaikat yang dekat dengan Allah.",
        ),
        ContentBlock(
                arabic = "ثُمَّ خُصُوْصًا إِلَى رُوْحِ الْمَرْحُوْمِ / الْمَرْحُوْمَةِ … ثُمَّ إِلَى جَمِيْعِ أَهْلِ الْقُبُوْرِ مِنَ الْمُسْلِمِيْنَ وَالْمُسْلِمَاتِ وَالْمُؤْمِنِيْنَ وَالْمُؤْمِنَاتِ مِنْ مَشَارِقِ الْأَرْضِ إِلَى مَغَارِبِهَا بَرِّهَا وَبَحْرِهَا، خُصُوْصًا إِلَى اٰبَائِنَا وَأُمَّهَاتِنَا وَأَجْدَادِنَا وَجَدَّاتِنَا وَمَشَايِخِنَا وَلِمَنِ اجْتَمَعْنَا هٰهُنَا بِسَبَبِهِ وَلِأَجْلِهِ",
                transliteration = "Tsumma khushûshan ilâ rûhil-marhûm / al-marhûmah … Tsumma ilâ jamî'i ahlil-qubûri minal-muslimîna wal-muslimâti wal-mu'minîna wal-mu'minâti min masyâriqil-ardhi ilâ maghâribihâ barrihâ wa bahrihâ, khushûshan ilâ âbâ'inâ wa ummahâtinâ wa ajdâdinâ wa jaddâtinâ wa masyâyikhinâ wa liman ijtama'nâ hâhunâ bisababihi wa li ajlih",
                translation = "Kemudian khususnya kepada roh almarhum / almarhumah … Kemudian kepada semua ahli kubur dari kaum Muslimin, Muslimat, Mukminin, dan Mukminat dari timur bumi sampai baratnya, di darat maupun di laut, khususnya kepada bapak-bapak kami, ibu-ibu kami, kakek-kakek kami, nenek-nenek kami, guru-guru kami, dan kepada orang yang menjadi sebab kami berkumpul di sini dan untuk keperluannya.",
                note = "Sebutkan nama arwah yang dituju pada bagian …",
        )
    ),
),

// 30 ──────────────────────────────────────────────────────────────────
Section(
    id = "doa-tahlil-ampunan-ahli-kubur",
    number = 30,
    title = "Doa Tahlil — Ampunan dan Rahmat untuk Ahli Kubur",
    subtitle = "Doa penutup untuk arwah dan kaum muslimin",
    type = SectionType.Doa,
    blocks = listOf(
        ContentBlock(
                arabic = "اَللّٰهُمَّ اغْفِرْ لَهُمْ وَارْحَمْهُمْ وَعَافِهِمْ وَاعْفُ عَنْهُمْ، اَللّٰهُمَّ أَنْزِلِ الرَّحْمَةَ وَالْمَغْفِرَةَ عَلَى أَهْلِ الْقُبُوْرِ مِنْ أَهْلِ لَا إِلٰهَ إِلَّا اللّٰهُ مُحَمَّدٌ رَسُوْلُ اللّٰهِ",
                transliteration = "Allâhummaghfir lahum warhamhum wa 'âfihim wa'fu 'anhum. Allâhumma anzilir-rahmata wal-maghfirata 'alâ ahlil-qubûri min ahli lâ ilâha illallâhu Muhammadur rasûlullâh",
                translation = "Ya Allah, ampunilah mereka, rahmatilah mereka, sejahterakanlah mereka, dan maafkanlah mereka. Ya Allah, turunkanlah rahmat dan ampunan kepada para ahli kubur dari kalangan ahli kalimat \"Lâ ilâha illallâh, Muhammadur rasûlullâh\" (Tiada Tuhan selain Allah, Muhammad adalah utusan Allah).",
        ),
        ContentBlock(
                arabic = "يَا مُهَيْمِنُ يَا سَلَامُ، سَلِّمْنَا وَالْمُسْلِمِيْنَ فِي الدُّنْيَا وَالْاٰخِرَةِ بِرَحْمَتِكَ يَا أَرْحَمَ الرَّاحِمِيْنَ",
                transliteration = "Yâ muhaiminu yâ salâm, sallimnâ wal-muslimîna fid-dunyâ wal-âkhirah, bi rahmatika yâ arhamar-râhimîn",
                translation = "Wahai Zat Yang Maha Memelihara, wahai Zat Yang Maha Memberi Keselamatan, selamatkanlah kami dan kaum Muslimin di dunia dan akhirat, dengan rahmat-Mu wahai Zat Yang Paling Penyayang di antara para penyayang.",
        ),
        ContentBlock(
                arabic = "اَللّٰهُمَّ إِنَّا نَسْأَلُكَ رِضَاكَ وَالْجَنَّةَ وَنَعُوْذُ بِكَ مِنْ سَخَطِكَ وَالنَّارِ، رَبَّنَا اٰتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْاٰخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
                transliteration = "Allâhumma innâ nas'aluka ridhâka wal-jannah, wa na'ûdzu bika min sakhatika wan-nâr. Rabbanâ âtinâ fid-dunyâ hasanatan wa fil-âkhirati hasanatan wa qinâ 'adzâban-nâr",
                translation = "Ya Allah, sesungguhnya kami memohon ridha-Mu dan surga, dan kami berlindung kepada-Mu dari murka-Mu dan api neraka. Ya Tuhan kami, berilah kami kebaikan di dunia dan kebaikan di akhirat, dan peliharalah kami dari siksa api neraka.",
        ),
        ContentBlock(
                arabic = "سُبْحٰنَ رَبِّكَ رَبِّ الْعِزَّةِ عَمَّا يَصِفُوْنَ، وَسَلَامٌ عَلَى الْمُرْسَلِيْنَ، وَالْحَمْدُ لِلّٰهِ رَبِّ الْعٰلَمِيْنَ، اَلْفَاتِحَةُ",
                transliteration = "Subhâna rabbika rabbil-'izzati 'ammâ yashifûn, wa salâmun 'alal-mursalîn, wal-hamdu lillâhi rabbil-'âlamîn. Al-Fâtihah",
                translation = "Maha Suci Tuhanmu, Tuhan Yang Maha Perkasa, dari apa yang mereka sifatkan. Dan kesejahteraan dilimpahkan kepada para rasul. Dan segala puji bagi Allah, Tuhan semesta alam. (dilanjutkan Al-Fatihah penutup — lihat section 4)",
        )
    ),
),

// 31 ──────────────────────────────────────────────────────────────────
Section(
    id = "sholawat-penutup-maulaya-burdah",
    number = 31,
    title = "Sholawat Penutup — Maulaya dan Syair Burdah",
    subtitle = "Penutup dengan sholawat Maulaya dan syair Burdah",
    type = SectionType.Sholawat,
    blocks = listOf(
        ContentBlock(
                arabic = "مَوْلَايَ صَلِّ وَسَلِّمْ دَائِمًا أَبَدًا عَلَى حَبِيْبِكَ خَيْرِ الْخَلْقِ كُلِّهِمِ",
                transliteration = "Maulâya shalli wa sallim dâ'iman abadan 'alâ habîbika khairil-khalqi kullihimi",
                translation = "Wahai Tuhanku, limpahkanlah rahmat dan keselamatan selamanya kepada kekasih-Mu, sebaik-baik makhluk seluruhnya.",
        ),
        ContentBlock(
                arabic = "هُوَ الْحَبِيْبُ الَّذِيْ تُرْجَى شَفَاعَتُهُ لِكُلِّ هَوْلٍ مِنَ الْأَهْوَالِ مُقْتَحَمِ",
                transliteration = "Huwal-habîbul-ladzî turjâ syafâ'atuhû likulli haulin minal-ahwâli muqtahami",
                translation = "Dialah kekasih yang diharapkan syafaatnya pada setiap kejadian yang mengerikan dari semua kejadian yang menakutkan.",
        ),
        ContentBlock(
                arabic = "يَا رَبِّ بِالْمُصْطَفَى بَلِّغْ مَقَاصِدَنَا وَاغْفِرْ لَنَا مَا مَضَى يَا وَاسِعَ الْكَرَمِ",
                transliteration = "Yâ rabbi bil-musthafâ balligh maqâshidanâ, waghfir lanâ mâ madhâ yâ wâsi'al-karami",
                translation = "Wahai Tuhanku, dengan perantara Nabi terpilih, sampaikanlah kami kepada tujuan-tujuan kami, dan ampunilah dosa kami yang telah lalu, wahai Zat Yang Maha Luas kedermawanan-Nya.",
        ),
        ContentBlock(
                arabic = "يَا أَرْحَمَ الرَّاحِمِيْنَ يَا أَرْحَمَ الرَّاحِمِيْنَ يَا أَرْحَمَ الرَّاحِمِيْنَ، فَرِّجْ عَلَى الْمُسْلِمِيْنَ",
                transliteration = "Yâ arhamar-râhimîn, yâ arhamar-râhimîn, yâ arhamar-râhimîn, farrij 'alal-muslimîn",
                translation = "Wahai Zat Yang Paling Penyayang di antara para penyayang (3 kali), lapangkanlah / berilah jalan keluar bagi kaum Muslimin.",
        )
    ),
),

// 32 ──────────────────────────────────────────────────────────────────
Section(
    id = "salam-penutup",
    number = 32,
    title = "Salam Penutup",
    subtitle = "Doa penutup majelis tahlil",
    type = SectionType.Doa,
    blocks = listOf(
        ContentBlock(
                arabic = "تَقَبَّلَ اللّٰهُ مِنَّا وَمِنْكُمْ تَقَبَّلْ يَا كَرِيْمُ، اَلْحَمْدُ لِلّٰهِ رَبِّ الْعٰلَمِيْنَ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللّٰهِ الْعَلِيِّ الْعَظِيْمِ، وَالسَّلَامُ عَلَيْكُمْ وَرَحْمَةُ اللّٰهِ وَبَرَكَاتُهُ",
                transliteration = "Taqabbalallâhu minnâ wa minkum, taqabbal yâ karîm. Alhamdu lillâhi rabbil-'âlamîn. Wa lâ hawla wa lâ quwwata illâ billâhil-'aliyyil-'azhîm. Wassalâmu 'alaikum wa rahmatullâhi wa barakâtuh",
                translation = "Semoga Allah menerima amal dari kami dan dari kalian, terimalah wahai Zat Yang Maha Mulia. Segala puji bagi Allah, Tuhan semesta alam. Dan tidak ada daya dan kekuatan kecuali dengan pertolongan Allah Yang Maha Tinggi lagi Maha Agung. Dan semoga keselamatan, rahmat Allah, dan berkah-Nya tercurah atas kalian.",
        )
    ),
),
    )
}
