# Tamir Kiti Eklentisi (Spigot 1.8.8+)

Bu eklenti, sunuculara gelişmiş ve tamamen yapılandırılabilir bir tamir kiti sistemi ekler. Oyuncular, belirli bekleme süreleriyle komut kullanarak tamir kitleri alabilir ve bu kitleri hasarlı alet, zırh ve silahlarını anında tamir etmek için kullanabilirler.

## 📖 Kullanım (Usage)

Tamir kitini kullanmak oldukça basittir. İşlem, oyuncunun kendi envanteri açıkken yapılır.

1.  Envanterinizi açın (`E` tuşu).
2.  🖱️ Farenizin sol tuşuyla `Tamir Kiti`'ne tıklayarak onu elinize (imlecinize) alın.
3.  Elinizdeki tamir kitini, tamir etmek istediğiniz **hasarlı** bir eşyanın (örneğin bir kılıç veya zırh) üzerine getirin.
4.  🖱️ Hasarlı eşyanın üzerine bir kez daha sol tıklayın.

### İşlem Sonuçları

Bu işlemi yaptığınızda üç farklı sonuçla karşılaşabilirsiniz:

* **✅ Başarılı Tamir:** Eğer üzerine tıkladığınız eşya hasarlı ve tamir edilebilir ise, anında tamir edilecek ve `config.yml`'de ayarladığınız başarı sesi çalacaktır. Bir adet tamir kiti kullanılır ve artan kitler (varsa) envanterinizdeki ilk boş slota yerleştirilir.

* **⚠️ Zaten Tamir Edilmiş:** Eğer kiti, tamir edilebilir bir eşya (kılıç, zırh vb.) üzerinde kullanmaya çalışırsanız ama o eşyanın canı zaten tam doluysa, bir uyarı mesajı alır ve başarısızlık sesini duyarsınız. Kitiniz harcanmaz.

* **Envanter Düzenleme:** Eğer kiti, bir toprak blok veya meşale gibi tamir edilemeyen bir eşyanın üzerine getirip tıklarsanız, eklenti bunu normal bir envanter düzenleme işlemi olarak görür. Herhangi bir mesaj veya ses olmadan, elinizdeki kit ile o eşya normal bir şekilde yer değiştirir.

## ✨ Özellikler

-   **Ayarlanabilir Tamir Kiti:** Kitin hangi eşya olacağını (`material`), adını, açıklamasını ve parlama efektini `config.yml` üzerinden tamamen değiştirebilirsiniz.
-   **Yetkiye Dayalı Bekleme Süreleri:** Farklı oyuncu gruplarına (rank'lara) farklı bekleme süreleri atayabilirsiniz. (`/tamirkiti`)
-   **Esnek Veritabanı Desteği:** Oyuncu verileri, sunucuda yüklü olan ve JDBC sürücüsü sağlayan diğer eklentiler veya kütüphane aracılığıyla SQLite veya MySQL gibi veritabanlarında saklanabilir. Veri kaybını önlemek için sağlam bir yapı sunar.
-   **Admin Komutları:** Oyunculara kit vermek (`/tamirkitiver`) ve eklentiyi yeniden yüklemek (`/tamirkitireload`) için özel komutlar.
-   **Özel İzinler:** Belirli oyunculara bekleme süresini atlama (`bypass`) izni verebilirsiniz.
-   **Kötüye Kullanım Koruması:** Tamir kitlerinin bir blok olarak yere konulması veya örste isminin değiştirilmesi engellenmiştir.
-   **Akıllı Kit Yönetimi:** Tamir sonrası artan kitler, envanterdeki ilk boş slota döner. Envanter doluysa oyuncunun imlecinde kalır.
-   **Ayarlanabilir Sesler ve Mesajlar:** Başarılı ve başarısız tamir işlemleri için ses efektleri ve tüm oyuncu mesajları `config.yml` üzerinden düzenlenebilir.
-   **Yüksek Performans:** Eklenti, veritabanı işlemlerini arka planda (asenkron) yapar ve verileri hafızada önbelleğe alarak sunucu performansını (TPS) etkilemez.

## ⚙️ Kurulum (Sunucu Sahipleri İçin)

1.  GitHub sayfasının sağ tarafındaki **"Releases"** bölümüne gidin.
2.  En son sürümün altından `TamirKiti-1.0.jar` dosyasını indirin.
3.  İndirdiğiniz bu `.jar` dosyasını, sunucunuzun `plugins` klasörüne atın.
4.  Sunucuyu başlatın veya yeniden yükleyin.
5.  İlk başlatmada `plugins/TamirKiti/` klasörü ve içinde `config.yml` ile `cooldowns.db` (SQLite kullanılıyorsa) dosyaları otomatik olarak oluşacaktır.
6.  `config.yml` dosyasını kendi isteğinize göre düzenleyip `/tamirkitireload` komutunu kullanabilirsiniz. Özellikle `database` bölümünü sunucunuzdaki yapılandırmaya göre ayarlamayı unutmayın.

## 📋 Komutlar ve Yetkiler

| Komut | Açıklama | Yetki (Permission) |
| :--- | :--- | :--- |
| `/tamirkiti` | Oyuncunun kendisine tamir kiti verir. | `tamirkiti.command.use` |
| `/tamirkitiver <oyuncu> [miktar]` | Belirtilen oyuncuya kit verir. | `tamirkiti.command.give` |
| `/tamirkitireload` | `config.yml` dosyasını yeniden yükler. | `tamirkiti.command.reload` |

**Diğer Yetkiler:**
-   `tamirkiti.bypass.cooldown`: `/tamirkiti` komutunu bekleme süresi olmadan kullanmayı sağlar.
-   `tamirkiti.rank.<rank_adı>`: `config.yml` dosyasında belirlediğiniz ranklara özel bekleme süreleri için.

## 🛠️ Geliştiriciler İçin (Derleme)

Projeyi klonlamak ve kendiniz derlemek için:

1.  **Projeyi Klonla:**
    ```bash
    git clone https://github.com/iamxmatti/TamirKiti.git
    ```
2.  **Maven ile Derle:**
    ```bash
    mvn clean package
    ```
    Oluşturulan `.jar` dosyası `target` klasöründe bulunacaktır.
