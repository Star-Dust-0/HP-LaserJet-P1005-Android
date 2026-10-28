#!/usr/bin/env bash
# Scratch inspection of the user-supplied reference APK (Samsung Memory
# Guardian 8.5.17) UI: install, launch home activity, capture screenshots of
# bottom nav / top settings / menus / buttons. Isolated emulator, no accounts,
# no network interaction beyond the app's own (app may crash without Samsung
# framework - logcat captures why).
set -uxo pipefail
mkdir -p shots
adb install -r refapk/memory-guardian-8.5.17.apk || true
sleep 2
adb shell cmd uimode night no; sleep 1
adb shell am start -n com.samsung.android.memoryguardian/.presentation.home.MemoryGuardianHomeActivity > shots/start-day.txt 2>&1 || true
sleep 8
adb exec-out screencap -p > shots/ref-day-home.png || true
sleep 3
adb exec-out screencap -p > shots/ref-day-home2.png || true
# try tapping the bottom nav items (right-most, left-most thirds) and settings icon area
H=$(adb shell wm size | grep -oE '[0-9]+x[0-9]+' | tail -1)
W=${H%x*}; HH=${H#*x}
y_nav=$(( HH * 96 / 100 )); y_top=$(( HH * 7 / 100 ))
# Motion evidence: tab-switch clip, then settings/menu appearance clip.
adb shell "screenrecord --time-limit 10 /sdcard/ref-nav.mp4" >/dev/null 2>&1 &
rec=$!; sleep 1
for x_frac in 20 50 80; do
  x=$(( W * x_frac / 100 ))
  adb shell input tap $x $y_nav; sleep 2
  adb exec-out screencap -p > "shots/ref-day-nav${x_frac}.png" || true
done
sleep 3; wait $rec || true
adb pull /sdcard/ref-nav.mp4 shots/ref-day-nav.mp4 >/dev/null 2>&1 || true
adb shell rm /sdcard/ref-nav.mp4 || true
adb shell "screenrecord --time-limit 6 /sdcard/ref-settings.mp4" >/dev/null 2>&1 &
rec=$!; sleep 1
adb shell input tap $(( W * 88 / 100 )) $y_top
for i in 1 2 3 4 5 6; do
  adb shell screencap -p /sdcard/ref-burst$i.png || true; sleep 0.4
done
sleep 2; wait $rec || true
adb pull /sdcard/ref-settings.mp4 shots/ref-day-settings.mp4 >/dev/null 2>&1 || true
for i in 1 2 3 4 5 6; do adb pull /sdcard/ref-burst$i.png shots/ref-day-settings-burst$i.png >/dev/null 2>&1 || true; done
adb shell rm /sdcard/ref-settings.mp4 /sdcard/ref-burst*.png || true
adb exec-out screencap -p > shots/ref-day-topright.png || true
adb shell cmd uimode night yes; sleep 2
adb shell am start -n com.samsung.android.memoryguardian/.presentation.home.MemoryGuardianHomeActivity >> shots/start-night.txt 2>&1 || true
sleep 8
adb exec-out screencap -p > shots/ref-night-home.png || true
adb shell dumpsys window | grep -iE 'mCurrentFocus|mFocusedApp' > shots/focus.txt || true
adb logcat -d 2>/dev/null | grep -iE 'memoryguardian|AndroidRuntime|FATAL' | tail -60 > shots/logcat-tail.txt || true
ls -la shots
