# aOtoMod

aOtoMod, Paper 1.21.4 sunucuları için geliştirilmiş gelişmiş bir otomatik sohbet moderasyon eklentisidir. Küfür, hakaret, spam, reklam ve filtre atlatma denemelerini algılar; oyunculara otomatik ceza uygular ve yetkililere Discord üzerinden detaylı log gönderir.

## Özellikler

- 3 seviyeli küfür ve hakaret filtresi
- Türkçe karakter, leetspeak ve sembol atlatma algılama
- Benzerlik tabanlı yerel filtre sistemi
- Spam koruması
- Reklam, IP, domain ve Discord daveti koruması
- Kalıcı mute sistemi
- Oyuncu mesaj geçmişi
- İhlal geçmişi kaydı
- Discord webhook logları
- Discord bot butonları
- Modal ile özel mute süresi uzatma
- Oyun içi moderasyon paneli
- Tamamen düzenlenebilir config.yml ve messages.yml

## Algılanabilen Örnekler

text
a.m.k
a m k
a-m-k
aaamkkk
s!k
s1k
p!ç
gel gel gel gel
!!!!!!!!!!!!
play.example.com
discord.gg/test
play . example . com


Whitelist sistemi sayesinde normal kelimelerin yanlışlıkla cezalandırılması engellenebilir.

## Discord Sistemi

Discord logları temiz ve detaylı embed olarak gönderilir. Loglarda oyuncu adı, UUID, ihlal türü, mesaj, ceza, süre, son mesajlar ve tarih bulunur.

Discord bot aktifse loglara şu butonlar eklenir:

- Yanlış İşlem / Mute Kaldır
- Mute Süresini Uzat
- Oyuncuyu Banla
- Geçmişi Göster
- İncelendi

Mute Süresini Uzat butonu Discord modal açar. Yetkili özel süre girebilir:

text
30s, 10m, 1h, 2d, 1w


Butonları sadece config içindeki yetkili Discord rol IDlerine sahip kişiler kullanabilir.

## Oyun İçi Panel

text
/automod panel


Panel ile:

- Aktif susturmalar görülebilir
- Son ihlaller incelenebilir
- Oyuncu geçmişi görüntülenebilir
- İstatistikler kontrol edilebilir
- Spam, reklam ve Discord logları açılıp kapatılabilir
- Reload, unmute, mute uzatma ve ban işlemleri yapılabilir

## Komutlar

text
/automod reload
/automod unmute <oyuncu>
/automod panel
/automod history <oyuncu>
/automod violations <oyuncu>
/automod stats


## İzinler

text
automod.bypass
automod.reload
automod.unmute
automod.panel
automod.history
automod.stats
automod.spam.bypass
automod.advertisement.bypass


## Kurulum

1. Jar dosyasını plugins klasörüne atın.
2. Sunucuyu başlatın.
3. config.yml ve messages.yml dosyalarını düzenleyin.
4. Discord bot kullanacaksanız bot token, guild ID, channel ID ve rol IDlerini girin.
5. /automod reload ile ayarları yenileyin.

## Dosyalar

text
config.yml              Tüm ayarlar
messages.yml            Tüm mesajlar
data.yml                Aktif mute verileri
violations.yml          İhlal geçmişi
discord-actions.yml     Discord buton/modal kayıtları


## Gereksinimler

- Paper 1.21.4
- Java 21

## Kısa Tanıtım

aOtoMod, sunucunuzdaki sohbeti otomatik olarak denetleyen modern bir moderasyon eklentisidir. Küfür, spam ve reklamları algılar; cezaları uygular; Discord ve oyun içi panel ile yetkililerin hızlı işlem yapmasını sağlar.
