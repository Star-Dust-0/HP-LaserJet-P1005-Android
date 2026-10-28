<div align="center">

<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="HP LaserJet P1005 app icon" width="192"/>

# طابعة HP LaserJet P1005 لأندرويد

تطبيق أندرويد لطباعة المستندات والصور مباشرة على طابعة HP LaserJet P1005 عبر USB OTG، دون حاسوب ودون شبكة.

</div>

---

## المزايا

| الميزة | الوصف |
| --- | --- |
| طباعة المستندات | طباعة ملفات PDF مباشرة على الطابعة |
| طباعة الصور | تحويل الصور وطباعتها بالأبيض والأسود |
| الاستقبال من التطبيقات | استقبال الملفات من أي تطبيق عبر قائمة «مشاركة» |
| قائمة الطباعة | متابعة المهام الجارية والمنتظرة وسجل الطباعة |
| خيارات الطباعة | عدد النسخ، نطاق الصفحات، حجم الورق، تخطيط الصفحة، الجودة، والطباعة على الوجهين يدويًا |
| تهيئة الطابعة | معالج إعداد يفحص البرنامج الثابت ويطبع صفحة تجريبية |
| صيانة الطابعة | فحص إصدار البرنامج الثابت (FWVER) وتحميله عند الحاجة |
| سجل التشخيص | سجل USB والطباعة لاستكشاف الأخطاء |
| واجهة عربية | تصميم عربي كامل بأسلوب One UI، داكن وفاتح، للهاتف والجهاز اللوحي |

## المتطلبات

- جهاز أندرويد يدعم USB OTG، وكابل أو محوّل OTG.
- طابعة HP LaserJet P1005 متصلة مباشرة بالجهاز.

## طريقة الاستخدام

1. شغّل الطابعة ووصّلها بالجهاز عبر USB OTG.
2. افتح التطبيق واضغط **إعداد الطابعة** واتبع خطوات التهيئة حتى طباعة الصفحة التجريبية.
3. اختر **طباعة مستند** أو **طباعة الصور**، أو شارك ملفًا إلى التطبيق من أي مكان.
4. اضبط خيارات الطباعة ثم اضغط **طباعة**.

> **ملاحظة مهمة:** طابعة P1005 تفقد برنامجها الثابت بعد كل فصل للكهرباء. عند تشغيلها من جديد، حمّل البرنامج الثابت من صفحة الصيانة مرة واحدة قبل الطباعة، ولا تُكرر التحميل دون حاجة.

## القيود المعروفة

- يدعم التطبيق طابعة HP LaserJet P1005 فقط، وطباعة أحادية اللون فقط.
- الطباعة عبر USB OTG السلكية فقط؛ لا توجد طباعة شبكية أو لاسلكية.
- الطباعة على الوجهين يدوية (تقليب الورق وإعادة إدخاله).

## البناء

يبنى التطبيق عبر GitHub Actions عند كل دفع إلى `main`، وتُنشر حزمة APK موقّعة في صفحة الإصدارات. للبناء محليًا:

```bash
./gradlew assembleRelease
```

## البروتوكول والترخيص

تستخدم الطابعة بروتوكول XQX المملوك، لذا لا يمكن إرسال نص أو صورة خام مباشرة. يحوّل التطبيق الصفحات داخليًا اعتمادًا على مولّد `foo2xqx` من مشروع OpenPrinting؛ راجع إشعارات ترخيص المصدر قبل إعادة توزيع أي جزء منه: https://github.com/OpenPrinting/foo2zjs

<details>
<summary>English summary</summary>

An Arabic-first Android app that prints PDF documents and images directly to an HP LaserJet P1005 over USB OTG: print queue with history, copies/page-range/paper-size/layout/quality options, manual duplex, printer setup with firmware check and test page, firmware maintenance, and a diagnostics log. P1005 only, monochrome, wired USB OTG only; firmware must be reloaded after every printer power cycle. Built by GitHub Actions; signed APKs are attached to Releases.

</details>

---

<div align="center">
بُني بواسطة <b>Instinct</b> · brought to you by Instinct
</div>

