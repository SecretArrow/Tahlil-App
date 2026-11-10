export type ContentBlock = {
  arabic?: string;
  transliteration: string;
  translation: string;
  note?: string;
  repeat?: number;
};

export type Section = {
  id: string;
  number: number;
  title: string;
  subtitle?: string;
  type: "pembukaan" | "sholawat" | "surah" | "tahlil" | "ayat" | "doa" | "dzikir";
  blocks: ContentBlock[];
};

export const tahlilSections: Section[] = [
  {
    id: "pembukaan",
    number: 1,
    title: "Pembukaan",
    subtitle: "Ta'awudz & Basmalah",
    type: "pembukaan",
    blocks: [
      {
        arabic: "أَعُوْذُ بِاللَّهِ مِنَ الشَّيْطٰنِ الرَّجِيْمِ. بِسْمِ اللَّهِ الرَّحْمٰنِ الرَّحِيْمِ.",
        transliteration:
          "A'uudzubillaahi minasy-syaithaanir-rajiim. Bismillaahirrahmaanir-rahiim.",
        translation:
          "Aku berlindung kepada Allah dari godaan syetan yang terkutuk. Dengan menyebut nama Allah Yang Maha Pemurah lagi Maha Penyayang.",
      },
    ],
  },
  {
    id: "sholawat-nabi",
    number: 2,
    title: "Shalawat & Al-Fatihah untuk Nabi",
    subtitle: "Hadhrat Nabiyil Mushtofa Muhammad SAW",
    type: "sholawat",
    blocks: [
      {
        arabic:
          "إِلَىٰ حَضْرَةِ النَّبِيِّ الْمُصْطَفَىٰ مُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمْ وَعَلَى آلِهِ وَأَصْحَابِهِ وَأَزْوَاجِهِ وَذُرِّيَّتِهِ وَأَهْلِ بَيْتِهِ أَجْمَعِيْنَ، شَيْءٌ لِلَّهِ لَهُمُ الْفَاتِحَةُ.",
        transliteration:
          "Ilaa hadhrotin-nabiyyil mushtofaa Muhammadin shallallaahu 'alaihi wasallam, wa 'alaa aalihi wa ash-haabihi wa azwaajihi wa dzurriyyatihi wa ahli baitihi ajma'iin. Syai-un lillaahi lahumul-faatihah.",
        translation:
          "Yaa Allah semoga sampai (rahmat Allah) ke hadapan Nabi Besar Muhammad SAW. serta keselamatan kepada keluarga, sahabat, istri, anak cucu dan Ahli Baitnya (semuanya). Segala perkara kepunyaan Allah dan tetaplah kepada-Nya. Al-Fatihah.",
      },
      {
        arabic:
          "بِسْمِ اللَّهِ الرَّحْمٰنِ الرَّحِيْمِ ۝١ الْحَمْدُ لِلَّهِ رَبِّ الْعٰلَمِيْنَ ۝٢ الرَّحْمٰنِ الرَّحِيْمِ ۝٣ مٰلِكِ يَوْمِ الدِّيْنِ ۝٤ إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِيْنُ ۝٥ اِهْدِنَا الصِّرَاطَ الْمُسْتَقِيْمَ ۝٦ صِرَاطَ الَّذِيْنَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوْبِ عَلَيْهِمْ وَلَا الضَّآلِّيْنَ ۝٧.",
        transliteration:
          "Bismillaahirrahmaanir-rahiim. Alhamdu lillaahi rabbil-'aalamiin. Ar-rahmaanir-rahiim. Maaliki yaumid-diin. Iyyaaka na'budu wa iyyaaka nasta'iin. Ihdinash-shiraathal-mustaqiim. Shiraathalladziina an'amta 'alaihim ghairil-maghdluubi 'alaihim waladh-dhaalliin.",
        translation:
          "Dengan menyebut nama Allah Yang Maha Pengasih lagi Maha Penyayang. Segala puji bagi Allah, Tuhan seluruh alam. Yang Maha Pengasih lagi Maha Penyayang. Pemilik hari pembalasan. Hanya kepada-Mu kami menyembah dan hanya kepada-Mu kami memohon pertolongan. Tunjukkan kami ke jalan yang lurus. (yaitu) jalan orang-orang yang telah Engkau beri nikmat, bukan (jalan) mereka yang dimurkai dan bukan (pula) mereka yang sesat.",
        note: "Al-Fatihah (Surat ke-1)",
      },
    ],
  },
  {
    id: "tahlil-ahli-kubur",
    number: 3,
    title: "Tahlil untuk Ahli Kubur",
    subtitle: "Doa kepada seluruh penghuni kubur kaum muslimin",
    type: "tahlil",
    blocks: [
      {
        arabic:
          "ثُمَّ إِلَى حَضْرَةِ إِخْوَانِهِ مِنَ الْأَنْبِيَاءِ وَالْمُرْسَلِيْنَ وَالْأَوْلِيَاءِ وَالْشُّهَدَاءِ وَالْصَّالِحِيْنَ وَالْصَّحَابَةِ وَالْتَّابِعِيْنَ وَالْعُلَمَاءِ الْعَامِلِيْنَ وَالْمُصَنِّفِيْنَ الْمُخْلِصِيْنَ وَجَمِيْعِ الْمَلَائِكَةِ الْمُقَرَّبِيْنَ، خُصُوْصًا إِلَى سَيِّدِنَا الشَّيْخِ عَبْدِ الْقَادِرِ الْجِيْلَانِي، الْفَاتِحَةُ.",
        transliteration:
          "Tsumma ilaa hadhrati ikhwaanihi minal-anbiya'i wal-mursaliin wal-auliya'i wasy-syuhadaa'i wash-shaalihiin wash-shahaabati wat-taabi'iin wal-'ulamaa'il-'aamiliin wal-mushannifiinal-mukhlishiin wa jamii'il-malaaikatil-muqarrabiin, khushushon ilaa sayyidinasy-syaikh 'abdil-qaadir al-jilaani, al-faatihah.",
        translation:
          "Kemudian kepada hadirat saudara-saudaranya dari kalangan para nabi, para rasul, para wali, para syuhada, orang-orang saleh, para sahabat, para tabi'in, ulama yang mengamalkan ilmunya, para penulis yang ikhlas, dan seluruh malaikat muqarrabin. Khususnya kepada syaikh kita, Syekh Abdul Qadir al-Jilani. Al-Fatihah.",
      },
      {
        arabic:
          "بِسْمِ اللَّهِ الرَّحْمٰنِ الرَّحِيْمِ ۝١ الْحَمْدُ لِلَّهِ رَبِّ الْعٰلَمِيْنَ ۝٢ الرَّحْمٰنِ الرَّحِيْمِ ۝٣ مٰلِكِ يَوْمِ الدِّيْنِ ۝٤ إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِيْنُ ۝٥ اِهْدِنَا الصِّرَاطَ الْمُسْتَقِيْمَ ۝٦ صِرَاطَ الَّذِيْنَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوْبِ عَلَيْهِمْ وَلَا الضَّآلِّيْنَ ۝٧.",
        transliteration:
          "Bismillaahirrahmaanir-rahiim. Alhamdu lillaahi rabbil-'aalamiin. Ar-rahmaanir-rahiim. Maaliki yaumid-diin. Iyyaaka na'budu wa iyyaaka nasta'iin. Ihdinash-shiraathal-mustaqiim. Shiraathalladziina an'amta 'alaihim ghairil-maghdluubi 'alaihim waladh-dhaalliin.",
        translation:
          "Dengan menyebut nama Allah Yang Maha Pengasih lagi Maha Penyayang. Segala puji bagi Allah, Tuhan seluruh alam. Yang Maha Pengasih lagi Maha Penyayang. Pemilik hari pembalasan. Hanya kepada-Mu kami menyembah dan hanya kepada-Mu kami memohon pertolongan. Tunjukkan kami ke jalan yang lurus. (yaitu) jalan orang-orang yang telah Engkau beri nikmat, bukan (jalan) mereka yang dimurkai dan bukan (pula) mereka yang sesat.",
        note: "Al-Fatihah",
      },
      {
        arabic:
          "ثُمَّ إِلَىٰ جَمِيْعِ أَهْلِ الْقُبُوْرِ مِنَ الْمُسْلِمِيْنَ وَالْمُسْلِمَاتِ وَالْمُؤْمِنِيْنَ وَالْمُؤْمِنَاتِ مِنْ مَشَارِقِ الْأَرْضِ وَمَغَارِبِهَا فِيْ بَرِّهَا وَبَحْرِهَا وَمِنْ يَمِيْنِهَا إِلَىٰ شِمَالِهَا خُصُوْصًا إِلَىٰ آبَآئِنَا وَأُمَّهَاتِنَا وَأَجْدَادِنَا وَجَدَّاتِنَا.",
        transliteration:
          "Tsumma ilaa jamii'i ahlil-qubuur minal-muslimiina wal-muslimaati wal-mu'miniina wal-mu'minaati min masyaariqil-ardhi wa maghaaribihaa fii barrihaa wa bahrihaa wa min yamiinihaa ilaa syimaalihaa khushushon ilaa aabaainaa wa ummahaatinaa wa ajdaadinaa wa jaddaatinaa.",
        translation:
          "Kepada segenap ahli kubur kaum muslimin laki-laki dan perempuan, kaum mukminin laki-laki dan perempuan dari timur dan barat, baik yang ada di darat maupun di laut, di kanan dan kirinya, kemudian terutama kepada para bapak dan ibu kami, kakek dan nenek kami,",
      },
      {
        arabic:
          "وَمَشَايِخِنَا وَمَشَايِخِ مَشَايِخِنَا وَأَسَاتِذَتِنَا وَأَسَاتِذَةِ أَسَاتِذَتِنَا وَلِمَنِ اجْتَمَعْنَا هٰهُنَا بِسَبَبِهِ، شَيْءٌ لِلَّهِ لَهُمُ الْفَاتِحَةُ.",
        transliteration:
          "Wa masyaaikhinaa wa masyaaikhi masyaaikhinaa wa asaatidzatinaa wa asaatidzati asaatidzatinaa wa limanijtama'naa hahunaa bisababihi. Syai-un lillaahi lahum, al-faatihah.",
        translation:
          "kepada syaikh kami, dan syaikhnya syaikh kami, kepada guru-guru kami, dan gurunya guru-guru kami, dan bagi ahli kubur/arwah yang menjadi sebab kami berkumpul di sini. Segala perkara kepunyaan Allah dan tetaplah kepada-Nya. Al-Fatihah.",
        
      },
      {
        arabic:
          "بِسْمِ اللَّهِ الرَّحْمٰنِ الرَّحِيْمِ ۝١ الْحَمْدُ لِلَّهِ رَبِّ الْعٰلَمِيْنَ ۝٢ الرَّحْمٰنِ الرَّحِيْمِ ۝٣ مٰلِكِ يَوْمِ الدِّيْنِ ۝٤ إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِيْنُ ۝٥ اِهْدِنَا الصِّرَاطَ الْمُسْتَقِيْمَ ۝٦ صِرَاطَ الَّذِيْنَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوْبِ عَلَيْهِمْ وَلَا الضَّآلِّيْنَ ۝٧.",
        transliteration:
          "Bismillaahirrahmaanir-rahiim. Alhamdu lillaahi rabbil-'aalamiin. Ar-rahmaanir-rahiim. Maaliki yaumid-diin. Iyyaaka na'budu wa iyyaaka nasta'iin. Ihdinash-shiraathal-mustaqiim. Shiraathalladziina an'amta 'alaihim ghairil-maghdluubi 'alaihim waladh-dhaalliin.",
        translation:
          "Dengan menyebut nama Allah Yang Maha Pengasih lagi Maha Penyayang. Segala puji bagi Allah, Tuhan seluruh alam. Yang Maha Pengasih lagi Maha Penyayang. Pemilik hari pembalasan. Hanya kepada-Mu kami menyembah dan hanya kepada-Mu kami memohon pertolongan. Tunjukkan kami ke jalan yang lurus. (yaitu) jalan orang-orang yang telah Engkau beri nikmat, bukan (jalan) mereka yang dimurkai dan bukan (pula) mereka yang sesat.",
        note: "Al-Fatihah",
      },
      {
        arabic:
          "ثُمَّ إِلَى جَمِيْعِ أهْلِ الْقُبُوْرِ مِمَّنْ ذُكِرَتْ أَسْمَاؤُهُ فِيْ هٰذِهِ الرِّسَالَةِ حَضْرَةِ رُوْحِ … وَحَضْرَةِ رُوْحِ … وَحَضْرَةِ رُوْحِ … رَحِمَهُمُ اللَّهُ وَغَفَرَهُمْ، الْفَاتِحَةُ.",
        transliteration:
          "Tsumma ilaa jamii'i ahlil-qubuur mimman dzukirot asmaa'uhu fii haadzihir-risaalati, hadhrati ruuhi…, wa hadhrati ruuhi…, wa hadhrati ruuhi…, rahimahumullaahu wa ghafarahum, al-faatihah.",
        translation:
          "Kemudian kepada semua ahli kubur, yang namanya disebutkan dalam risalah ini. Kepada…, dan kepada…, dan kepada…. Semoga Allah merahmati dan mengampuni mereka. Bacaan Al-Fatihah ini kami tujukan kepada Allah dan pahalanya untuk mereka semua. Al-Fatihah.",
        note: "Sebutkan nama almarhum/almarhumah pada bagian …",
      },
      {
        arabic:
          "بِسْمِ اللَّهِ الرَّحْمٰنِ الرَّحِيْمِ ۝١ الْحَمْدُ لِلَّهِ رَبِّ الْعٰلَمِيْنَ ۝٢ الرَّحْمٰنِ الرَّحِيْمِ ۝٣ مٰلِكِ يَوْمِ الدِّيْنِ ۝٤ إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِيْنُ ۝٥ اِهْدِنَا الصِّرَاطَ الْمُسْتَقِيْمَ ۝٦ صِرَاطَ الَّذِيْنَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوْبِ عَلَيْهِمْ وَلَا الضَّآلِّيْنَ ۝٧.",
        transliteration:
          "Bismillaahirrahmaanir-rahiim. Alhamdu lillaahi rabbil-'aalamiin. Ar-rahmaanir-rahiim. Maaliki yaumid-diin. Iyyaaka na'budu wa iyyaaka nasta'iin. Ihdinash-shiraathal-mustaqiim. Shiraathalladziina an'amta 'alaihim ghairil-maghdluubi 'alaihim waladh-dhaalliin.",
        translation:
          "Dengan menyebut nama Allah Yang Maha Pengasih lagi Maha Penyayang. Segala puji bagi Allah, Tuhan seluruh alam. Yang Maha Pengasih lagi Maha Penyayang. Pemilik hari pembalasan. Hanya kepada-Mu kami menyembah dan hanya kepada-Mu kami memohon pertolongan. Tunjukkan kami ke jalan yang lurus. (yaitu) jalan orang-orang yang telah Engkau beri nikmat, bukan (jalan) mereka yang dimurkai dan bukan (pula) mereka yang sesat.",
        note: "Al-Fatihah",
      },
    ],
  },
  {
    id: "tahlil-1",
    number: 4,
    title: "Tahlil & Surat-Surat",
    subtitle: "Tahlil, Al-Ikhlas, Al-Falaq, An-Nas",
    type: "tahlil",
    blocks: [
      {
        arabic: "لَآ إِلٰهَ إِلَّا اللَّهُ وَاللَّهُ أَكْبَرُ وَلِلَّهِ الْحَمْدُ.",
        transliteration: "Laa ilaaha illallaah, wallaahu akbar, wa lillaahil-hamd.",
        translation:
          "Tiada Tuhan selain Allah, Dia-lah Yang Maha Besar dan hanya bagi-Nya lah segala puji.",
      },
      {
        arabic:
          "بِسْمِ اللَّهِ الرَّحْمٰنِ الرَّحِيْمِ ۝١ قُلْ هُوَ اللَّهُ أَحَدٌ ۝٢ اللَّهُ الصَّمَدُ ۝٣ لَمْ يَلِدْ وَلَمْ يُوْلَدْ ۝٤ وَلَمْ يَكُنْ لَهٗ كُفُوًا أَحَدٌ ۝٥.",
        transliteration:
          "Bismillaahirrahmaanir-rahiim. Qul huwallaahu ahad. Allaahush-shamad. Lam yalid walam yuulad. Wa lam yakun lahuu kufuwan ahad.",
        translation:
          "Dengan menyebut Nama Allah yang Maha Pengasih lagi Maha Penyayang. Katakanlah (Ya Muhammad) Allah itu Esa. Allah adalah Tuhan yang bergantung kepada-Nya segala sesuatu. Dia tiada beranak dan tiada pula diperanakkan. Dan tidak ada seorangpun yang setara dengan Dia.",
        note: "Surat Al-Ikhlas",
        repeat: 3,
      },
      {
        arabic: "لَآ إِلٰهَ إِلَّا اللَّهُ وَاللَّهُ أَكْبَرُ وَلِلَّهِ الْحَمْدُ.",
        transliteration: "Laa ilaaha illallaah, wallaahu akbar, wa lillaahil-hamd.",
        translation:
          "Tiada Tuhan selain Allah, Dia-lah Yang Maha Besar dan hanya bagi-Nya lah segala puji.",
      },
      {
        arabic:
          "بِسْمِ اللَّهِ الرَّحْمٰنِ الرَّحِيْمِ ۝١ قُلْ أَعُوْذُ بِرَبِّ الْفَلَقِ ۝٢ مِنْ شَرِّ مَا خَلَقَ ۝٣ وَمِنْ شَرِّ غَاسِقٍ إِذَا وَقَبَ ۝٤ وَمِنْ شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ۝٥ وَمِنْ شَرِّ حَاسِدٍ إِذَا حَسَدَ ۝٦.",
        transliteration:
          "Bismillaahirrahmaanir-rahiim. Qul a'uudzu birabbil-falaq. Min syarri maa khalaq. Wa min syarri ghaasiqin idzaa waqab. Wa min syarrin-naffaatsaati fil-'uqad. Wa min syarri haasidin idzaa hasad.",
        translation:
          "Katakanlah: Aku berlindung kepada Tuhan Yang Menguasai subuh, dari kejahatan makhluk-Nya. Dan dari kejahatan malam apabila telah gelap gulita. Dan dari kejahatan wanita-wanita tukang sihir yang menghembus pada buhul-buhul. Dan dari kejahatan orang yang dengki apabila ia dengki.",
        note: "Surat Al-Falaq",
      },
      {
        arabic: "لَآ إِلٰهَ إِلَّا اللَّهُ وَاللَّهُ أَكْبَرُ وَلِلَّهِ الْحَمْدُ.",
        transliteration: "Laa ilaaha illallaah, wallaahu akbar, wa lillaahil-hamd.",
        translation:
          "Tiada Tuhan selain Allah, Dia-lah Yang Maha Besar dan hanya bagi-Nya lah segala puji.",
      },
      {
        arabic:
          "بِسْمِ اللَّهِ الرَّحْمٰنِ الرَّحِيْمِ ۝١ قُلْ أَعُوْذُ بِرَبِّ النَّاسِ ۝٢ مَلِكِ النَّاسِ ۝٣ إِلٰهِ النَّاسِ ۝٤ مِنْ شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ۝٥ الَّذِيْ يُوَسْوِسُ فِيْ صُدُوْرِ النَّاسِ ۝٦ مِنَ الْجِنَّةِ وَالْنَّاسِ ۝٧.",
        transliteration:
          "Bismillaahirrahmaanir-rahiim. Qul a'uudzu birabbin-naas. Malikin-naas. Ilaahin-naas. Min syarril-waswaasil-khannaas. Alladzii yuwaswisu fii shuduurin-naas. Minal-jinnati wan-naas.",
        translation:
          "Katakanlah: Aku berlindung kepada Tuhan manusia. Raja manusia. Sembahan manusia. Dari kejahatan (bisikan) syetan yang biasa bersembunyi. Yang membisikan (kejahatan) ke dalam dada manusia. Dari jin dan manusia.",
        note: "Surat An-Nas",
      },
      {
        arabic: "لَآ إِلٰهَ إِلَّا اللَّهُ وَاللَّهُ أَكْبَرُ وَلِلَّهِ الْحَمْدُ.",
        transliteration: "Laa ilaaha illallaah, wallaahu akbar, wa lillaahil-hamd.",
        translation:
          "Tiada Tuhan selain Allah, Dia-lah Yang Maha Besar dan hanya bagi-Nya lah segala puji.",
      },
    ],
  },
  {
    id: "al-fatihah-2",
    number: 5,
    title: "Al-Fatihah",
    subtitle: "Setelah tahlil & surat-surat",
    type: "surah",
    blocks: [
      {
        arabic:
          "بِسْمِ اللَّهِ الرَّحْمٰنِ الرَّحِيْمِ ۝١ الْحَمْدُ لِلَّهِ رَبِّ الْعٰلَمِيْنَ ۝٢ الرَّحْمٰنِ الرَّحِيْمِ ۝٣ مٰلِكِ يَوْمِ الدِّيْنِ ۝٤ إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِيْنُ ۝٥ اِهْدِنَا الصِّرَاطَ الْمُسْتَقِيْمَ ۝٦ صِرَاطَ الَّذِيْنَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوْبِ عَلَيْهِمْ وَلَا الضَّآلِّيْنَ ۝٧.",
        transliteration:
          "Bismillaahirrahmaanir-rahiim. Alhamdu lillaahi rabbil-'aalamiin. Ar-rahmaanir-rahiim. Maaliki yaumid-diin. Iyyaaka na'budu wa iyyaaka nasta'iin. Ihdinash-shiraathal-mustaqiim. Shiraathalladziina an'amta 'alaihim ghairil-maghdluubi 'alaihim waladh-dhaalliin.",
        translation:
          "Dengan menyebut nama Allah Yang Maha Pengasih lagi Maha Penyayang. Segala puji bagi Allah, Tuhan seluruh alam. Yang Maha Pengasih lagi Maha Penyayang. Pemilik hari pembalasan. Hanya kepada-Mu kami menyembah dan hanya kepada-Mu kami memohon pertolongan. Tunjukkan kami ke jalan yang lurus. (yaitu) jalan orang-orang yang telah Engkau beri nikmat, bukan (jalan) mereka yang dimurkai dan bukan (pula) mereka yang sesat.",
        note: "Al-Fatihah",
      },
    ],
  },
  {
    id: "ayat-kursi",
    number: 6,
    title: "Ayat Kursi",
    subtitle: "Surat Al-Baqarah ayat 1-5 & 255",
    type: "ayat",
    blocks: [
      {
        arabic:
          "الٓمٓ ۝١ ذٰلِكَ الْكِتٰبُ لَا رَيْبَ فِيْهِ هُدًى لِلْمُتَّقِيْنَ ۝٢ الَّذِيْنَ يُؤْمِنُوْنَ بِالْغَيْبِ وَيُقِيْمُوْنَ الصَّلٰوةَ وَمِمَّا رَزَقْنٰهُمْ يُنْفِقُوْنَ ۝٣.",
        transliteration:
          "Aliif laam miiim. Dzaalikal-kitaabu laa raiba fiih, hudan lil-muttaqiin. Alladziina yu'minuuna bil-ghaibi wa yuqiimuunash-shalaata wa mimmaa razaqnaahum yunfiquun.",
        translation:
          "Alif, laam miim. Kitab (Al-Qur'an) ini tidak ada keraguan padanya; petunjuk bagi mereka yang bertakwa. (yaitu) mereka yang beriman kepada yang ghaib, yang mendirikan sholat, dan menafkahkan sebagian rezeki yang Kami anugerahkan kepada mereka.",
        note: "Surat Al-Baqarah ayat 1-3",
      },
      {
        arabic:
          "وَالَّذِيْنَ يُؤْمِنُوْنَ بِمَآ أُنْزِلَ إِلَيْكَ وَمَآ أُنْزِلَ مِنْ قَبْلِكَ وَبِالْأٰخِرَةِ هُمْ يُوْقِنُوْنَ ۝٤ أُولٰئِكَ عَلَىٰ هُدًى مِنْ رَبِّهِمْ وَأُولٰئِكَ هُمُ الْمُفْلِحُوْنَ ۝٥.",
        transliteration:
          "Walladziina yu'minuuna bimaa unzila ilaika wa maa unzila min qablika wa bil-aakhirati hum yuuqinuun. Ulaaika 'alaa hudan min rabbihim wa ulaaika humul-muflihuun.",
        translation:
          "Dan mereka beriman kepada Kitab (Al-Qur'an) yang telah diturunkan kepadamu dan kitab-kitab yang telah diturunkan sebelummu, serta mereka yakin akan adanya (kehidupan) akhirat. Mereka itulah yang tetap mendapat petunjuk dari Tuhannya. Dan merekalah orang-orang yang beruntung.",
        note: "Surat Al-Baqarah ayat 4-5",
      },
      {
        arabic:
          "وَإِلٰهُكُمْ إِلٰهٌ وَاحِدٌ، لَآ إِلٰهَ إِلَّا هُوَ الرَّحْمٰنُ الرَّحِيْمُ. اللَّهُ لَآ إِلٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّوْمُ، لَا تَأْخُذُهٗ سِنَةٌ وَلَا نَوْمٌ، لَهٗ مَا فِي السَّمٰوَاتِ وَمَا فِي الْأَرْضِ.",
        transliteration:
          "Wa ilaahukum ilaahun waahid, laa ilaaha illaa huwar-rahmaanur-rahiim. Allaahu laa ilaaha illaa huwal-hayyul-qayyuum, laa ta'khudzuhuu sinatun walaa naum, lahuu maa fis-samaawaati wa maa fil-ardh.",
        translation:
          "Dan Tuhan-Mu Tuhan yang satu. Tiada Tuhan selain Dia, yang Maha Pengasih lagi Maha Penyayang. Allah tidak ada tuhan melainkan Dia yang hidup kekal lagi terus menerus mengurus (makhluk-Nya); tidak mengantuk dan tidak tidur. Kepunyaan-Nya apa yang di langit dan apa yang di bumi.",
        note: "Awal Ayat Kursi",
      },
      {
        arabic:
          "مَنْ ذَا الَّذِيْ يَشْفَعُ عِنْدَهٗ إِلَّا بِإِذْنِهٖ، يَعْلَمُ مَا بَيْنَ أَيْدِيْهِمْ وَمَا خَلْفَهُمْ، وَلَا يُحِيْطُوْنَ بِشَيْءٍ مِنْ عِلْمِهٖ إِلَّا بِمَا شَآءَ، وَسِعَ كُرْسِيُّهُ السَّمٰوَاتِ وَالْأَرْضَ، وَلَا يَؤُوْدُهٗ حِفْظُهُمَا وَهُوَ الْعَلِيُّ الْعَظِيْمُ ۝٢٥٥.",
        transliteration:
          "Man dzaalladzii yasyfa'u 'indahuuu illaa bi-idznihi, ya'lamu maa bainu aidiihim wa maa khalfahum, wa laa yuhiithuuna bisyai-in min 'ilmihii illaa bimaa syaa-a, wasi'a kursiyyuhus-samaawaati wal-ardh, wa laa yauuduhuu hifzhuhumaa wa huwal-'aliyyul-'azhiim.",
        translation:
          "Siapakah yang dapat memberi syafa'at di sisi Allah tanpa izin-Nya? Allah mengetahui apa-apa yang di hadapan mereka, dan mereka tidak mengetahui apa-apa dari ilmu Allah melainkan apa yang dikehendaki-Nya. Kursi Allah meliputi langit dan bumi. Dan Allah tidak merasa berat memelihara keduanya, dan Allah Maha Tinggi lagi Maha Besar.",
        note: "Ayat Kursi (Surat Al-Baqarah ayat 255)",
      },
    ],
  },
  {
    id: "dzikir",
    number: 7,
    title: "Dzikir",
    subtitle: "Dzikir yang paling utama",
    type: "dzikir",
    blocks: [
      {
        arabic: "أَفْضَلُ الذِّكْرِ فَاعْلَمْ أَنَّهٗ.",
        transliteration: "Afdlaludz-dzikri fa'lam annahuu.",
        translation: "Ketahuilah bahwa dzikir yang paling utama ialah:",
      },
      {
        arabic: "لَآ إِلٰهَ إِلَّا اللَّهُ.",
        transliteration: "Laa ilaaha illallaah.",
        translation: "Tiada Tuhan selain Allah.",
        repeat: 11,
        note: "Dibaca 11 kali (atau lebih banyak lebih baik)",
      },
      {
        arabic: "سَيِّدِنَا مُحَمَّدٌ رَسُوْلُ اللَّهِ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ.",
        transliteration:
          "Sayyidinaa Muhammadur-rasuulullaah. Shallallaahu 'alaihi wasallam.",
        translation:
          "Junjungan Kita Nabi Muhammad adalah utusan Allah. Semoga Allah limpahkan rahmat dan salam kepada beliau.",
      },
    ],
  },
  {
    id: "doa-pembuka",
    number: 8,
    title: "Doa Pembuka",
    subtitle: "Ta'awudz, Basmalah & pujian kepada Allah",
    type: "doa",
    blocks: [
      {
        arabic:
          "أَعُوْذُ بِاللَّهِ مِنَ الشَّيْطٰنِ الرَّجِيْمِ. بِسْمِ اللَّهِ الرَّحْمٰنِ الرَّحِيْمِ.",
        transliteration:
          "A'uudzubillaahi minasy-syaithaanir-rajiim. Bismillaahirrahmaanir-rahiim.",
        translation:
          "Aku berlindung kepada Allah dari godaan syetan yang terkutuk. Dengan menyebut nama Allah Yang Maha Pemurah lagi Maha Penyayang.",
      },
      {
        arabic:
          "الْحَمْدُ لِلَّهِ رَبِّ الْعٰلَمِيْنَ، حَمْدَ الشَّاكِرِيْنَ، حَمْدَ النَّاعِمِيْنَ، حَمْدًا يُوَافِيْ نِعْمَتَهٗ وَيُكَافِئُ مَزِيْدَهٗ.",
        transliteration:
          "Alhamdu lillaahi rabbil-'aalamiin, hamdan syaakiriin, hamdan naa'imiin, hamdan yuwaafii ni'amatahuu wa yukaafi-u maziidahuu.",
        translation:
          "Segala puji bagi Allah penguasa alam semesta, sebagaimana orang-orang yang bersyukur dan orang-orang yang mendapat banyak kenikmatan memuji-Nya. Dengan pujian yang sepadan dan nikmat-Nya dan memungkinkan pertambahannya.",
      },
      {
        arabic:
          "يَا رَبَّنَا لَكَ الْحَمْدُ كَمَا يَنْبَغِيْ لِجَلَالِ وَجْهِكَ وَعَظِيْمِ سُلْطَانِكَ. اَللّٰهُمَّ صَلِّ وَسَلِّمْ عَلَىٰ سَيِّدِنَا مُحَمَّدٍ وَعَلَىٰ آلِ سَيِّدِنَا مُحَمَّدٍ.",
        transliteration:
          "Yaa rabbanaa lakal-hamdu kamaa yanbaghii lijalaali wajhika wa 'azhiimi sulthaanik. Allahumma shalli wa sallim 'alaa sayyidinaa Muhammadin wa 'alaa aali sayyidinaa Muhammadin.",
        translation:
          "Wahai Tuhan kami, pujian hanyalah untuk-Mu, sebagaimana yang layak akan kemuliaan Dzat-Mu dan keagungan kekuasaan-Mu. Ya Allah, limpahkanlah kesejahteraan dan keselamatan kepada Nabi Muhammad junjungan kami dan kepada keluarga beliau.",
      },
    ],
  },
  {
    id: "doa-tahmid",
    number: 9,
    title: "Doa Penyampaian Pahala",
    subtitle: "Doa agar pahala sampai kepada Nabi & ahli kubur",
    type: "doa",
    blocks: [
      {
        arabic:
          "اَللّٰهُمَّ تَقَبَّلْ وَأَوْصِلْ ثَوَابَ مَا قَرَأْنَاهُ مِنَ الْقُرْآنِ الْعَظِيْمِ وَمَا هَلَّلْنَا وَمَا سَبَّحْنَا وَمَا اسْتَغْفَرْنَا وَمَا صَلَّيْنَا عَلَىٰ سَيِّدِنَا مُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ.",
        transliteration:
          "Allahumma taqabbal wa aushil tsawaaba maa qara'naahu minal-qur'aanil-'azhiimi wa maa hallalnaa wa maa sabbahnaa wa mastaghfarnaa wa maa shallainaa 'alaa sayyidinaa Muhammadin shallallaahu 'alaihi wasallam.",
        translation:
          "Ya Allah, terimalah dan sampaikanlah pahala Al-Qur'an yang kami baca, tahlil kami, tasbih kami, istighfar kami dan shalawat kami kepada Nabi Muhammad SAW.",
      },
      {
        arabic:
          "هَدِيَّةً وَاصِلَةً وَرَحْمَةً نَازِلَةً وَبَرَكَةً شَامِلَةً إِلَىٰ حَضْرَةِ حَبِيْبِنَا وَشَفِيْعِنَا وَقُرَّةِ أَعْيُنِنَا سَيِّدِنَا وَمَوْلَنَا مُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ.",
        transliteration:
          "Hadiyyatan waashilatan wa rahmatan naazilah wa barokatan syaamilah ilaa hadhrati habiibinaa wa syafii'inaa wa qurrati a'yuninaa sayyidinaa wa maulanaa Muhammadin shallallaahu 'alaihi wasallam.",
        translation:
          "Sebagai hadiah yang menjadi penyambung, sebagai rahmat yang turun dan sebagai berkah yang menyebar kepada kekasih kami, penolong kami dan buah hati kami, pemuka dan pemimpin kami, yaitu Nabi Muhammad SAW.",
      },
      {
        arabic:
          "وَإِلَى جَمِيْعِ إِخْوَانِهِ مِنَ الْأَنْبِيَاءِ وَالْمُرْسَلِيْنَ وَالْأَوْلِيَاءِ وَالْشُّهَدَاءِ وَالْصَّالِحِيْنَ وَالْصَّحَابَةِ وَالْتَّابِعِيْنَ وَالْعُلَمَاءِ الْعَامِلِيْنَ وَالْمُصَنِّفِيْنَ الْمُخْلِصِيْنَ وَجَمِيْعِ الْمَلَائِكَةِ الْمُقَرَّبِيْنَ، خُصُوْصًا إِلَى سَيِّدِنَا الشَّيْخِ عَبْدِ الْقَادِرِ الْجِيْلَانِي.",
        transliteration:
          "Wa ilaa jamii'i ikhwaanihi minal-anbiya'i wal-mursaliin wal-auliya'i wasy-syuhadaa'i wash-shaalihiin wash-shahaabati wat-taabi'iin wal-'ulamaa'il-'aamiliin wal-mushannifiinal-mukhlishiin wa jamii'il-malaaikatil-muqarrabiin, khushushon ilaa sayyidinasy-syaikh 'abdil-qaadir al-jilaani.",
        translation:
          "Dan kepada seluruh saudara-saudaranya dari kalangan para nabi, para rasul, para wali, para syuhada, orang-orang saleh, para sahabat, para tabi'in, ulama yang mengamalkan ilmunya, para penulis yang ikhlas, dan seluruh malaikat muqarrabin, khususnya kepada syaikh kita, Syekh Abdul Qadir al-Jilani.",
      },
      {
        arabic:
          "ثُمَّ إِلَى أَرْوَاحِ جَمِيْعِ أَهْلِ الْقُبُوْرِ مِنَ الْمُسْلِمِيْنَ وَالْمُسْلِمَاتِ وَالْمُؤْمِنِيْنَ وَالْمُؤْمِنَاتِ مِنْ مَشَارِقِ الْأَرْضِ وَمَغَارِبِهَا بَرِّهَا وَبَحْرِهَا، خُصُوْصًا إِلَى آبَاءِ أُسْرَةِ الْمُضِيْفِ وَأُمَّهَاتِهِمْ وَأَجْدَادِهِمْ وَجَدَّاتِهِمْ.",
        transliteration:
          "Tsumma ilaa arwaahi jamii'i ahlil-qubuur minal-muslimiina wal-muslimaati wal-mu'miniina wal-mu'minaati min masyaariqil-ardhi wa maghaaribihaa barrihaa wa bahrihaa, khushushon ilaa aabaa'i usratil-mudifi wa ummahaatihim wa ajdaadihim wa jaddaatihim.",
        translation:
          "Kemudian kepada seluruh arwah ahli kubur dari kalangan muslimin laki-laki dan perempuan, mukminin laki-laki dan perempuan, dari timur dan barat, di darat dan di laut, khususnya kepada bapak-bapak keluarga tuan rumah beserta ibu-ibu mereka, kakek dan nenek mereka.",
      },
      {
        arabic:
          "ثُمَّ خُصُوْصًا إِلَى حَضْرَةِ رُوْحِ ……، وَرُوْحِ ……، وَرُوْحِ ……، وَرُوْحِ ……، وَرُوْحِ ……، وَبَعْدَ ذَلِكَ إِلَى آبَائِنَا وَأُمَّهَاتِنَا وَأَجْدَادِنَا وَجَدَّاتِنَا، وَجَمِيْعِ الْمُسْلِمِيْنَ وَالْمُسْلِمَاتِ وَالْمُؤْمِنِيْنَ وَالْمُؤْمِنَاتِ الْأَحْيَاءِ مِنْهُمْ وَالْأَمْوَاتِ، وَلِمَنِ اجْتَمَعْنَا هٰهُنَا بِسَبَبِهِ وَلِأَجْلِهِ.",
        transliteration:
          "Tsumma khushushon ilaa hadhrati ruuhi……, wa ruuhi……, wa ruuhi……, wa ruuhi……, wa ruuhi……, wa ba'da dzaalika ilaa aabaainaa wa ummahaatinaa wa ajdaadinaa wa jaddaatinaa, wa jamii'il-muslimiina wal-muslimaati wal-mu'miniina wal-mu'minaati al-ahyaa'i minhum wal-amwaat, wa limanijtama'naa hahunaa bisababihi wa liajlihi.",
        translation:
          "Kemudian terkhusus kepada ahli kubur/arwah yang menjadi sebab kami berkumpul di sini dan untuk keperluannya, yaitu kepada hadirat ruh……, dan ruh……, dan ruh……, dan ruh……, dan ruh……, serta setelah itu kepada bapak-bapak dan ibu-ibu kami, kakek dan nenek kami, hingga kemudian kepada seluruh arwah ahli kubur dari kalangan muslimin laki-laki dan perempuan, mukminin laki-laki dan perempuan, baik yang masih hidup maupun yang telah meninggal dunia.",
        note: "Sebutkan nama almarhum/almarhumah pada bagian ……",
      },
    ],
  },
  {
    id: "doa-ahli-kubur",
    number: 10,
    title: "Doa untuk Ahli Kubur",
    subtitle: "Permohonan ampunan & rahmat",
    type: "doa",
    blocks: [
      {
        arabic:
          "اَللّٰهُمَّ اغْفِرْ لَهُمْ وَارْحَمْهُمْ وَعَافِهِمْ وَاعْفُ عَنْهُمْ. اَللّٰهُمَّ أَنْزِلِ الرَّحْمَةَ وَالْمَغْفِرَةَ عَلَىٰ أَهْلِ الْقُبُوْرِ مِنْ أَهْلِ لَآ إِلٰهَ إِلَّا اللَّهُ مُحَمَّدٌ رَسُوْلُ اللَّهِ.",
        transliteration:
          "Allahummaghfir lahum warhamhum wa 'aafihim wa'fu 'anhum. Allahumma anzilir-rahmata wal-maghfirota 'alaa ahlil-qubuuri min ahli laa ilaaha illallaah Muhammadur-rasuulullaah.",
        translation:
          "Ya Allah, ampunilah mereka, kasihanilah mereka, berilah mereka kesejahteraan dan maafkanlah mereka. Ya Allah, turunkanlah rahmat dan ampunan kepada ahli kubur yang selalu mengucapkan 'Laailaaha illallaah muhammadur rasuulullaah' (Tidak ada Tuhan selain Allah, Muhammad adalah utusan Allah).",
      },
      {
        arabic:
          "اَللّٰهُمَّ إِنَّا نَسْأَلُكَ سَلَامَةً فِي الدِّيْنِ وَعَافِيَةً فِي الْجَسَدِ وَزِيَادَةً فِي الْعِلْمِ وَبَرَكَةً فِي الرِّزْقِ وَتَوْبَةً قَبْلَ الْمَوْتِ وَرَحْمَةً عِنْدَ الْمَوْتِ.",
        transliteration:
          "Allahumma innaa nas-aluka salaamatan fid-diin wa 'aafiyatan fil-jasad wa ziyaadatan fil-'ilmi wa barokatan fir-rizqi wa taubatan qablal-maut wa rahmatan 'indal-maut.",
        translation:
          "Ya Allah ya Tuhan kami, kami mohon keselamatan agama, kesehatan jasmani, bertambahnya ilmu dan berkah rizki, dapat bertaubat sebelum mati, mendapat rahmat ketika mati,",
      },
      {
        arabic:
          "وَمَغْفِرَةً بَعْدَ الْمَوْتِ. اَللّٰهُمَّ هَوِّنْ عَلَيْنَا فِيْ سَكَرَاتِ الْمَوْتِ وَالْنَّجَاةَ مِنَ النَّارِ وَالْعَفْوَ عِنْدَ الْحِسَابِ.",
        transliteration:
          "Wa maghfiratan ba'dal-maut. Allahumma hawwin 'alainaa fii sakaraatil-maut wan-najaata minan-naari wal-'afwa 'indal-hisaab.",
        translation:
          "dan memperoleh ampunan setelah mati. Ya Allah, mudhkanlah kami pada gelombang sakaratul maut. Bebaskanlah kami dari adzab neraka-Mu dan memperoleh ampunan ketika kami dihisab.",
      },
      {
        arabic:
          "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْأَخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ. وَالْحَمْدُ لِلَّهِ رَبِّ الْعٰلَمِيْنَ. الْفَاتِحَةْ.",
        transliteration:
          "Rabbanaa aatinaa fid-dunyaa hasanah, wa fil-aakhirati hasanah, wa qinaa 'adzaaban-naar. Wal-hamdu lillaahi rabbil-'aalamiin. Al-faatihah.",
        translation:
          "Wahai Tuhan kami, berikanlah kami kebaikan di dunia dan kebaikan di akhirat, serta jauhkanlah kami dari siksa api neraka. Dan segala puji bagi Allah penguasa alam semesta. Al-Fatihah.",
        note: "Doa penutup",
      },
    ],
  },
  {
    id: "al-fatihah-penutup",
    number: 11,
    title: "Al-Fatihah Penutup",
    subtitle: "Penutup rangkaian tahlil",
    type: "surah",
    blocks: [
      {
        arabic:
          "بِسْمِ اللَّهِ الرَّحْمٰنِ الرَّحِيْمِ ۝١ الْحَمْدُ لِلَّهِ رَبِّ الْعٰلَمِيْنَ ۝٢ الرَّحْمٰنِ الرَّحِيْمِ ۝٣ مٰلِكِ يَوْمِ الدِّيْنِ ۝٤ إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِيْنُ ۝٥ اِهْدِنَا الصِّرَاطَ الْمُسْتَقِيْمَ ۝٦ صِرَاطَ الَّذِيْنَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوْبِ عَلَيْهِمْ وَلَا الضَّآلِّيْنَ ۝٧.",
        transliteration:
          "Bismillaahirrahmaanir-rahiim. Alhamdu lillaahi rabbil-'aalamiin. Ar-rahmaanir-rahiim. Maaliki yaumid-diin. Iyyaaka na'budu wa iyyaaka nasta'iin. Ihdinash-shiraathal-mustaqiim. Shiraathalladziina an'amta 'alaihim ghairil-maghdluubi 'alaihim waladh-dhaalliin.",
        translation:
          "Dengan menyebut nama Allah Yang Maha Pengasih lagi Maha Penyayang. Segala puji bagi Allah, Tuhan seluruh alam. Yang Maha Pengasih lagi Maha Penyayang. Pemilik hari pembalasan. Hanya kepada-Mu kami menyembah dan hanya kepada-Mu kami memohon pertolongan. Tunjukkan kami ke jalan yang lurus. (yaitu) jalan orang-orang yang telah Engkau beri nikmat, bukan (jalan) mereka yang dimurkai dan bukan (pula) mereka yang sesat.",
        note: "Al-Fatihah",
      },
    ],
  },
];

export const heroInfo = {
  title: "Tahlil Singkat",
  subtitle: "Majelis Tahlil & Doa",
  description:
    "Rangkaian zikir, tahlil, dan doa untuk mendoakan para arwah kaum muslimin. Disusun dari Al-Qur'an, hadits, dan amaliah ulama.",
  arabicBismillah: "بِسْمِ اللَّهِ الرَّحْمٰنِ الرَّحِيْمِ",
  source: "Salik Channel",
};
