<div align="center">

<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="HP LaserJet P1005 app icon" width="192"/>

# HP LaserJet P1005 for Android

An Android app that prints documents and photos straight to an HP LaserJet P1005 over USB OTG. No computer and no network needed.

</div>

> **Status:** this is a reconstruction of the shipped HP P1005 app 1.1 (version code 217), built from the released APK. The original source repository was lost, so decompiled code was repaired by hand and checked against the shipped app. It builds and has been compared with the original (see *Verification* below). It has not been tested on a real printer yet. Arabic README: [README.ar.md](README.ar.md).

---

## Features

| Feature | Description |
| --- | --- |
| Print documents | Print PDF files directly on the printer |
| Print photos | Convert photos and print them in black and white |
| Receive from other apps | Accept files from any app through the Share menu |
| Print queue | Follow running and waiting jobs, plus a print history |
| Print options | Copies, page range, paper size, page layout, quality, and manual two-sided printing |
| Automatic setup | The app prepares the printer by itself as soon as it is connected, with a clear do-not-unplug message, and loads the printer software when needed. A manual setup wizard and test page stay available |
| Printer maintenance | Check the firmware version (FWVER) and load it when needed |
| Diagnostics log | USB and print log for troubleshooting |
| Arabic interface | Full Arabic UI in a One UI style, dark and light, for phone and tablet |

## Requirements

- An Android device with USB OTG support, and an OTG cable or adapter.
- An HP LaserJet P1005 connected directly to the device.

## How to use

1. Turn the printer on and connect it to the device over USB OTG.
2. Open the app and connect the printer. The app checks and loads the printer software automatically and tells you when the printer is ready. **Printer setup** is still there if you want a test page.
3. Choose **Print document** or **Print photos**, or share a file to the app from anywhere.
4. Set the print options, then tap **Print**.

> **Important:** the P1005 loses its firmware every time it loses power. The app reloads it automatically when the printer connects, so just keep the printer plugged in for a moment after switching it on. Do not reload it by hand without a reason.

## Known limits

- Only the HP LaserJet P1005 is supported, in monochrome only.
- Printing works over wired USB OTG only. There is no network or wireless printing.
- Two-sided printing is manual (flip the paper and feed it again).

## Build

JDK 17, Gradle 8.7 (Android Gradle Plugin 8.5.2), compileSdk 35:

```bash
gradle :app:assembleRelease
```

Signing keys are read from environment variables (`MORPHE_KEYSTORE_FILE`, `MORPHE_STORE_PASSWORD`, `MORPHE_KEY_ALIAS`, `MORPHE_KEY_PASSWORD`). Nothing secret is stored in the repository. Without them the build is unsigned or debug-signed.

## Verification (original vs reconstructed)

Decompiled code loses casts, branches and string whitespace, so a clean build proves little. These checks were run against the shipped APK 1.1:

- **Opcode comparison** of every class: arithmetic and conversion instructions and call order. Fixed the int/float and long casts that decompiling had dropped (blank page preview, poster geometry, PBM output).
- **Same-input comparison** of the original classes and the reconstructed classes on the JVM: page rendering for PNG, JPG, TXT, HTML, RTF, CSV, DOCX, XLSX, PPTX, ODT and unsupported files; every layout mode, paper size and scale mode; poster tiles; booklet order; the PBM bytes sent to the printer converter. Output is identical pixel for pixel and byte for byte.
- **Resources:** the string pool and all 445 app strings (English and Arabic) match the original exactly, after restoring 156 strings whose leading and trailing spaces had been lost.
- **Emulator run** (Android 14 emulator, original and reconstructed APK side by side): home screen, all tabs, the file picker, and the print preview for an image, a text file and a PDF, plus the first controls on each. Screens and UI trees are identical.

**Not tested here (needs a phone and the printer):** USB OTG connection and permission, firmware load, real printing and print output, the system print service in the Android print dialog, tablet layout.

Hand-rebuilt from bytecode because decompilers failed: `HpPrintService.processJob`, `MainActivity.startPrintPass`, the built-in test page and firmware transfer routines, `showCapsuleNav`, `resolvePageRange`, `UsbPrinterIO.sendXqx`, the end of `SheetComposer.composeSheet` and `posterTileMap`, and several `PrintFlow` methods. Resource names differ from the shipped APK (it was resource-shrunk).

The prebuilt `libfoo2xqx.so` files are copied unchanged from the shipped APK.

## Protocol and licence

The printer uses the proprietary XQX protocol, so plain text or raw images cannot be sent directly. The app converts pages internally with the `foo2xqx` generator from the OpenPrinting project. Check its source licence notices before redistributing any part of it: https://github.com/OpenPrinting/foo2zjs

---

<div align="center">

brought to you by <b>Instinct</b>

</div>
