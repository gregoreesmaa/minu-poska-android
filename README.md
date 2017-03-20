# Minu Poska Androidi äpp

Androidi äpp, mis ühendab nutikalt õppeinfosüsteemi ja tunniplaanid. / Android app, that intelligently combines school's info system and timetables.
Esialgselt ProgeTiigri konkursi jaoks valminud äpi edasiarendus. Minu Poska äpp on GitHubis, et inspireerida noori ning pakkuda lihtsasti muudetavat skeletti kooliäppidele. 

<img src="/graphics/screenshots/Screenshot_1490025950_framed.png?raw=true" width="250"><img src="/graphics/screenshots/Screenshot_1490025943_framed.png?raw=true" width="250"><img src="/graphics/screenshots/Screenshot_1490025837_framed.png?raw=true" width="250">

## Miks peaks minu kool seda kasutama?
  - Stuudiumi integratsioon ([Stuudiumi API](https://github.com/stuudium/API))
    - Sündmused
    - Kodused tööd ja nende tehtuks märkimine
    - Tera ja suhtluse veebivaade
  - aSc tunniplaanid (XML eksportandmetest)
    - Perioodi (vmt) valik
    - Filtreering klasside, ainete, õpetajate ja klassiruumide järgi
  - "Minu tunniplaan"
    - Kombineerib õpilase Stuudiumi päevikud ja tunniplaaniinfo, näidates õpilasele vaid neid aineid, kus ta osaleb.
  - Teavitused
    - Uued sündmused
    - Järgmise koolipäeva ülesanded
    - Järgmine tund
  - Muu
    - Sobib ka vahetusõpilastele - Eesti-, inglise- ja võrukeelne täistõlge (sh aine nimetused)
    - Moodne väljanägemine - järgib Material Design'i juhtnööre
    - Kooli päris oma äpp - Lihtne kohandada mistahes koolile
    - Hea viis Androidile progemist õppida

### Lihtne lisada
  - Koolispetsiifilised lehed, näiteks söökla menüü või spordipäeva punktitabel
  - Tõlge teistesse keeltesse ja/või murretesse
  - Teiste tunniplaanisüsteemide tugi
  
## Kohandamine

### Stuudium

Stuudiumi andme-API kasutamiseks on vaja personaalset võtit, mille saab võttes ühendust Stuudiumi arendajatega. Rohkem infot: <https://github.com/stuudium/API>.

Kui võti olemas, pane see faili **_app/src/main/java/.../PoskaApplication.java_** väljale `STUUDIUM_CLIENT_ID`. Samuti muuda ära kooli alamdomeen väljal `STUUDIUM_SUBDOMAIN`.
```java
    private static String STUUDIUM_CLIENT_ID = ""; // TODO Your Stuudium API key here
    private static String STUUDIUM_SUBDOMAIN = "jpg"; // TODO Your xxx.ope.ee subdomain here
```

### aSc tunniplaanid

Tunniplaanide hankimiseks kasutab äpp lihtsat PHP serverit (<http://jpg.tartu.ee/tunniplaan/xml/index.php>):
```php
<?php
    function endsWith($haystack, $needle) {
        return substr($haystack, -strlen($needle)) === $needle;
    }
    
    $currenttime = round(microtime(true));
    echo $currenttime."\n";

    $files = scandir ("./");
    foreach ($files as $file) {
        if (endsWith($file, ".xml")) {
            $mtime = filemtime($file);
            $ctime = filectime($file);
            $time = max($mtime, $ctime);
            echo $time." ".$file."\n";
        }
    }
?>
```
aSc's ekspordi tunniplaan vormingus Oman XML - 10 kirje piirangu teadet võib ignoreerida. Seejärel lae see fail üles PHP skriptiga samasse kausta. Äpp leiab muutuse automaatselt. 

PHP skripti aadress pane faili **_app/src/main/java/.../PoskaApplication.java_** väljale `TIMETABLE_SCRIPT_URL`
```java
    public static final String TIMETABLE_SCRIPT_URL = "http://jpg.tartu.ee/tunniplaan/xml/index.php"; // TODO Your timetable script address here
``` 

### Värvid ja graafika

Sinist värviskeemi saab muuta failis **_app/src/main/res/values/colors.xml_**. Soovituslik on kasutada Material Design'i paletti (guugelda).

Äpp kasutab järgnevaid Poskale vastavaid pilte:
Menüü päis **_app/src/main/res/drawable-nodpi/default_cover.jpg_**

<img src="/app/src/main/res/drawable-nodpi/default_cover.jpg?raw=true" >

EULA/Kasutustingimuste kohal olev logo **_app/src/main/res/drawable-xxhdpi/minuposka_logo.png_**

<img src="/graphics/untransparent_eula_logo.png?raw=true" >

Äpi ikoon **_app/src/main/res/mipmap-X/ic_launcher.png_** (X - mdpi, hdpi, xdpi, xxhdpi, xxxhdpi)

<img src="/app/src/main/res/mipmap-xxhdpi/ic_launcher.png?raw=true" >
