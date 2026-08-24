# YouTube TV Remote 2.0 — Android TV

این پروژه نسخهٔ ارتقایافتهٔ YouTubeTVRemote برای تلویزیون‌های Android TV است که اپ رسمی YouTube روی آن‌ها در دسترس/قابل نصب نیست.

## تغییرات نسخهٔ 2.0
- ناوبری TV محور با D-pad و OK با الگوریتم فوکوس هندسی بهتر.
- فوکوس دیداری واضح روی کارت‌ها، دکمه‌ها، تب‌ها و عناصر قابل انتخاب YouTube.
- حفظ Cookie / Web Storage برای session وب و کمک به حفظ ورود حساب.
- کنترل Play/Pause با کلیدهای Media و Space.
- Seek واقعی ۱۰ ثانیه‌ای با ← و → وقتی ویدیو واقعاً در حال پخش است؛ در غیر این صورت حرکت فوکوس انجام می‌شود.
- نمایش HUD هنگام جلو/عقب رفتن.
- پشتیبانی بهتر از Fullscreen HTML5 / YouTube video.
- Back: خروج از fullscreen، سپس برگشت در history و در نهایت خروج از برنامه.
- اجرای Landscape و سخت‌افزار-accelerated WebView.
- جلوگیری از باز شدن لینک‌های غیرمرتبط داخل WebView.
- Target Android TV API 34 و compileSdk 35.

## معماری
این برنامه «کل محتوای YouTube» را از طریق تجربهٔ وب YouTube نمایش می‌دهد تا لازم نباشد یک کلاینت محدود با دیتابیس جداگانه ساخته شود. در نتیجه صفحات Home، Search، Channel، Playlist و پیشنهادهای شخصی YouTube از خود سرویس می‌آیند.

## محدودیت‌های واقعی
YouTube می‌تواند DOM/صفحه وب را تغییر دهد. همچنین بعضی قابلیت‌های رسمی YouTube TV، DRMهای خاص، Cast و بعضی جریان‌های احراز هویت ممکن است در WebView محدود باشند. برای ورود حساب، نگه‌داری session وب فعال است، اما این جایگزین رسمی OAuth/YouTube TV client نیست.

## Build
پروژه را با Android Studio باز کنید و Gradle Sync سپس Build APK را بزنید. محیط تولیدشده در این workspace Android SDK ندارد، بنابراین APK اینجا compile نشده است.
