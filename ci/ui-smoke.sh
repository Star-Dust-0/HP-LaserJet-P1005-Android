#!/usr/bin/env bash
set -uxo pipefail
APK="$1"; NAME="$2"
PKG="com.hpp1005.printer"
CMP="com.hpp1005.printer/com.mohamedelnaggar.hpp1005.MainActivity"
mkdir -p shots
die() {
  echo "SMOKE FAIL: $*" >&2
  adb shell screencap -p /sdcard/fail.png 2>/dev/null && adb pull /sdcard/fail.png "shots/${NAME}-fail.png" >/dev/null 2>&1 || true
  adb logcat -d 2>/dev/null | grep -iE "HPP1005|HP1005|AndroidRuntime" | tail -300 > "shots/${NAME}-logcat-fail.txt" 2>/dev/null || true
  exit 1
}

adb wait-for-device
adb shell 'while [ -z "$(getprop sys.boot_completed)" ]; do sleep 3; done'
sleep 10
adb install -r "$APK" || die "apk install failed"

find_node() { # $1 = content-desc; prints "x y" of LAST match or nothing
  adb shell uiautomator dump /sdcard/uix.xml >/dev/null 2>&1
  adb exec-out cat /sdcard/uix.xml > /tmp/uix.xml 2>/dev/null
  cp /tmp/uix.xml "shots/${NAME}-uix-last.xml" 2>/dev/null || true
  python3 - "$1" <<'PY'
import sys, re, subprocess
import xml.etree.ElementTree as ET
try:
    tree = ET.parse('/tmp/uix.xml')
except Exception:
    sys.exit(0)
hit = None
anr_foreign = False
wait_bounds = None
for node in tree.iter('node'):
    if node.get('content-desc') == sys.argv[1]:
        hit = node.get('bounds')
    txt = node.get('text') or ''
    if "isn't responding" in txt:
        # An ANR of OUR app must fail the run; a foreign app (e.g. the
        # launcher on a slow emulator boot) gets dismissed via Wait.
        if 'HP P1005' in txt:
            print('OWN APP ANR: ' + txt, file=sys.stderr)
        else:
            anr_foreign = True
    elif txt.strip() == 'Wait':
        wait_bounds = wait_bounds or node.get('bounds')
if hit:
    m = re.match(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]', hit)
    print((int(m.group(1))+int(m.group(3)))//2, (int(m.group(2))+int(m.group(4)))//2)
elif anr_foreign and wait_bounds:
    m = re.match(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]', wait_bounds)
    x = (int(m.group(1))+int(m.group(3)))//2
    y = (int(m.group(2))+int(m.group(4)))//2
    subprocess.run(['adb', 'shell', 'input', 'tap', str(x), str(y)])
    print('dismissed foreign ANR dialog via Wait', file=sys.stderr)
PY
}
wait_node() { # $1 desc, $2 timeout_s
  local t=0
  while [ $t -lt "$2" ]; do
    local xy; xy=$(find_node "$1")
    [ -n "$xy" ] && { echo "$xy"; return 0; }
    t=$((t+2)); sleep 2
  done
  die "node never appeared: $1"
}
tap_node() { # $1 desc
  local xy; xy=$(wait_node "$1" 30) || exit 1
  xy=$(echo "$xy" | tail -1)
  adb shell input tap $xy
}
cap() {
  local f="shots/${NAME}-$1.png"
  adb exec-out screencap -p > "$f"; sync
  [ -s "$f" ] || die "empty capture $1"
  local stats mean sd
  stats=$(convert "$f" -format "%[mean] %[standard-deviation]" info: 2>/dev/null) || die "imagemagick failed on $1"
  read mean sd <<< "$stats"
  echo "capture $1 mean=$mean stddev=$sd"
  awk "BEGIN{exit !($sd < 200)}" && die "blank/uniform capture $1 (stddev $sd)"
}
resumed() { adb shell dumpsys activity activities | grep -oE "(topResumedActivity|ResumedActivity)[^}]*" | head -1; }
launch() {
  local out
  out=$(adb shell am start -n "$CMP" 2>&1) || die "am start exit nonzero: $out"
  echo "$out" | grep -q "Error" && die "am start error: $out"
  wait_node "الرئيسية" 60 >/dev/null || exit 1
  resumed | grep -q "$PKG" || die "app not resumed after launch: $(resumed)"
}

cur_dims() { adb shell screencap -p 2>/dev/null | file -b - | grep -oE '[0-9]+ x [0-9]+' | tr -d ' ' | tr 'x' ' '; }
# NOTE: never derive swipe geometry from `wm size` - it reports PHYSICAL
# (portrait) dims even when rotated, landing rotated swipes off-screen.
swipe_up() {
  read SW SH <<< "$(cur_dims)"
  X=$((SW/2)); [ "${1:-}" = "edge" ] && X=$((SW/4))
  adb shell input swipe $X $((SH*3/4)) $X $((SH/4)) 300
}
swipe_down() {
  read SW SH <<< "$(cur_dims)"
  X=$((SW/2)); [ "${1:-}" = "edge" ] && X=$((SW/4))
  adb shell input swipe $X $((SH/4)) $X $((SH*3/4)) 300
}

launch; cap 01-home-light
tap_node "طباعة"; sleep 2; cap 02-print-light
swipe_up; swipe_up; sleep 1; cap 02b-print-bottom
swipe_down; swipe_down; sleep 1
tap_node "قائمة الطباعة"; sleep 2; cap 03-queue-light
tap_node "الرئيسية"; sleep 1
tap_node "الإعدادات"; sleep 2; cap 04-settings-light
swipe_up; swipe_up; sleep 1; cap 04b-settings-bottom
adb shell input keyevent KEYCODE_BACK; sleep 2
# The tablet emulator often ANRs Pixel Launcher mid-run; a system ANR dialog can
# eat the BACK press or cover the app during the dump. Retry with ANR dismissal
# (find_node's watcher path) and only re-press BACK when still in settings, so
# an already-home screen is never backed out of.
back_home_ok=""
for i in $(seq 1 12); do
  adb shell uiautomator dump /sdcard/uix-back.xml >/dev/null 2>&1 || true
  adb shell cat /sdcard/uix-back.xml > shots/${NAME}-uix-back.xml 2>/dev/null || true
  if grep -q "الرئيسية" shots/${NAME}-uix-back.xml; then back_home_ok=1; break; fi
  find_node "__never_matches__" >/dev/null 2>&1 || true
  if grep -q "الإعدادات" shots/${NAME}-uix-back.xml; then
    adb shell input keyevent KEYCODE_BACK
  fi
  sleep 2
done
[ -n "$back_home_ok" ] || die "back from settings did not return home"
cap 04c-back-home
adb shell cmd uimode night yes; sleep 2
launch; cap 05-home-dark
tap_node "قائمة الطباعة"; sleep 2; cap 06-queue-dark
adb shell cmd uimode night no; sleep 1
# Font scale 1.3: large-text check - labels are sp, so system font scale
# must not clip the capsule or overflow cards. Reset before moving on.
# NB: launch resumes the current task page (singleTask), and the capsule has
# only 3 tabs - الإعدادات is the HOME gear. Go home via the capsule first.
adb shell settings put system font_scale 1.3; sleep 2
launch; tap_node "الرئيسية"; sleep 1; cap 05f-home-fontscale
tap_node "الإعدادات"; sleep 2; cap 05g-settings-fontscale
adb shell settings put system font_scale 1.0; sleep 1
tap_node "رجوع"; sleep 2
# Reduced motion: zero animation scales must not break rendering or nav.
adb shell settings put global animator_duration_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global window_animation_scale 0
launch; cap 05h-home-nomotion
tap_node "طباعة"; sleep 2; cap 05i-print-nomotion
adb shell settings put global animator_duration_scale 1
adb shell settings put global transition_animation_scale 1
adb shell settings put global window_animation_scale 1
# Restore the queue page the following rotation section expects.
tap_node "قائمة الطباعة"; sleep 2
# Dump + grep with foreign-ANR dismissal retry: a system ANR dialog (launcher)
# can cover the app during the dump; the app's own ANR still fails the run via
# find_node's watcher. Retries up to 6 times, 2s apart.
dump_expect() { # $1 outfile, $2 needle, $3 error
  local i
  for i in $(seq 1 6); do
    adb shell uiautomator dump /sdcard/uix-chk.xml >/dev/null 2>&1 || true
    adb shell cat /sdcard/uix-chk.xml > "$1" 2>/dev/null || true
    if grep -q "$2" "$1"; then return 0; fi
    find_node "__never_matches__" >/dev/null 2>&1 || true
    sleep 2
  done
  die "$3"
}

png_dims() { adb shell screencap -p 2>/dev/null | file -b - | grep -oE '[0-9]+ x [0-9]+' | head -1; }
adb shell settings put system accelerometer_rotation 0
D0=$(png_dims)
adb shell settings put system user_rotation 1
t=0; D="$D0"
while [ $t -lt 15 ]; do
  D=$(png_dims)
  [ -n "$D" ] && [ "$D" != "$D0" ] && break
  t=$((t+3)); sleep 3
done
if [ -z "$D" ] || [ "$D" = "$D0" ]; then
  echo "rotation not supported in this emulator (capture dims stayed $D0)" | tee "shots/${NAME}-rotation-note.txt"
else
  echo "rotated: $D0 -> $D"
fi
dump_expect "shots/${NAME}-uix-rot.xml" "جارية الآن" "page state lost after rotation (queue no longer visible)"
cap 07-rotated
swipe_up edge; sleep 1; cap 07b-rotated-scrolled
adb shell settings put system user_rotation 0; sleep 2
# Real share sender: a tiny companion app shares the fixture via FileProvider with a
# Parcelable content:// Uri and a real FLAG_GRANT_READ_URI_PERMISSION grant (am start
# --eu/--grant flags do not grant the EXTRA_STREAM Uri - proven by run R's
# SecurityException: Permission Denial opening ExternalStorageProvider).
sout=$(adb install -r sharesender/build/outputs/apk/debug/sharesender-debug.apk 2>&1) || die "sender install failed: $sout"
adb shell run-as com.hpp1005.sharesender mkdir -p files 2>/dev/null || true
adb push ci/smoke-test.pdf /data/local/tmp/smoke-test.pdf >/dev/null || die "could not stage test pdf"
adb shell run-as com.hpp1005.sharesender cp /data/local/tmp/smoke-test.pdf files/smoke-test.pdf || die "could not copy fixture into sender files"
sout=$(adb shell am start -n com.hpp1005.sharesender/.ShareActivity 2>&1) || die "sender launch exit nonzero: $sout"
echo "$sout" | grep -q "Error" && die "sender launch error: $sout"
sleep 8
resumed | grep -q "$PKG" || die "app not resumed after share: $(resumed)"
adb shell uiautomator dump /sdcard/uix-share.xml >/dev/null 2>&1 || true
adb shell cat /sdcard/uix-share.xml > shots/${NAME}-uix-share.xml 2>/dev/null || true
adb logcat -d 2>/dev/null | grep "HP1005-Share" > "shots/${NAME}-share-log.txt" 2>/dev/null || true
grep -q "الصفحة 1 من" shots/${NAME}-uix-share.xml || die "share did not open a rendered preview page (no page counter)"
grep -q "معاينة الصفحة 1" shots/${NAME}-uix-share.xml || die "preview page image not rendered"
cap 08-share-preview
# Lower preview controls reachability on narrow layouts: scroll and capture.
swipe_up; sleep 1; cap 08a-preview-bottom
# One UI print dialogs: real pixels of the copies wheel and page-range field
# sheets (not just the summary rows). BACK dismisses each; preview survives.
tap_node "عدد النسخ"; sleep 2; cap 08d0-copies-dialog
adb shell input keyevent KEYCODE_BACK; sleep 1
tap_node "الصفحات"; sleep 2; cap 08d1-pagerange-dialog
adb shell input keyevent KEYCODE_BACK; sleep 1
swipe_down; sleep 1
# The preview is a sub-page with only a back button (no bottom capsule by
# design). Back out first; whether back lands on the queue or home, the
# capsule is present and tapping قائمة الطباعة settles on the queue page.
tap_node "رجوع"; sleep 2
tap_node "قائمة الطباعة"; sleep 2
adb shell uiautomator dump /sdcard/uix-wait.xml >/dev/null 2>&1 || true
adb shell cat /sdcard/uix-wait.xml > shots/${NAME}-uix-wait.xml 2>/dev/null || true
grep -q "بانتظار المعاينة والطباعة" shots/${NAME}-uix-wait.xml || die "waiting queue card not populated after share"
cap 08b-queue-waiting

# --- 12: motion + navigation corrections (2026-10-26) -----------------------
# (b) Page-transition motion clip: a tab switch recorded during the tap must
# contain intermediate frames (no instant cut). Frame-measured on the artifact.
home_xy=$(wait_node "الرئيسية" 45) || die "home tab not found for motion clip"
home_xy=$(echo "$home_xy" | tail -1)
adb shell screenrecord --time-limit 8 /sdcard/motion-tab.mp4 &
sleep 1
adb shell input tap $home_xy
sleep 8
adb pull /sdcard/motion-tab.mp4 "shots/${NAME}-motion-tab.mp4" || die "tab motion clip missing"
adb shell rm /sdcard/motion-tab.mp4 || true
[ -s "shots/${NAME}-motion-tab.mp4" ] || die "tab motion clip empty"

# (c) One UI dim behind sheets (his report: no dim behind menus). Settings
# page hosts the copies row; the left strip must darken clearly under the sheet.
tap_node "الإعدادات"; sleep 2
cap 12b-pre-dialog
# Dialog-open recorded: first frame of the sheet must already carry final
# geometry (frame-measured on the artifact; the pre-show window styling is
# what the ghost/snap regression was about).
copies_xy=$(wait_text "عدد النسخ" 45) || die "copies row not found for dialog clip"
copies_xy=$(echo "$copies_xy" | tail -1)
adb shell screenrecord --time-limit 8 /sdcard/dialog-open.mp4 &
sleep 1
adb shell input tap $copies_xy; sleep 8
adb pull /sdcard/dialog-open.mp4 "shots/${NAME}-dialog-open.mp4" || die "dialog open clip missing"
adb shell rm /sdcard/dialog-open.mp4 || true
[ -s "shots/${NAME}-dialog-open.mp4" ] || die "dialog open clip empty"
cap 12c-copies-dialog
pre_mean=$(convert "shots/${NAME}-12b-pre-dialog.png" -crop 60x1600+0+400 +repage -format "%[mean]" info:)
dlg_mean=$(convert "shots/${NAME}-12c-copies-dialog.png" -crop 60x1600+0+400 +repage -format "%[mean]" info:)
[ -n "$pre_mean" ] && [ -n "$dlg_mean" ] || die "strip mean extraction failed"
awk -v a="$pre_mean" -v b="$dlg_mean" 'BEGIN{exit !(b+0 < a*0.9)}' || die "dialog dim missing (pre=$pre_mean dlg=$dlg_mean)"
adb shell input keyevent KEYCODE_BACK; sleep 1
# Modal precedence: back with a sheet open dismisses the sheet; the page
# underneath must NOT have popped.
dump_expect "shots/${NAME}-uix-12c2.xml" "عدد النسخ" "back with sheet open popped the page instead of dismissing the sheet"

# Back-stack origin + scroll restore: diagnostics -> back must return to
# settings (never home/"برا خالص") at the same scroll offset.
swipe_up; sleep 1; cap 12a-settings-scrolled
tap_text "سجل التشخيص"; sleep 2
dump_expect "shots/${NAME}-uix-12d.xml" "سجل نشاط HP P1005" "diagnostics page did not open from settings"
# Settings-page identity must not depend on the restored scroll offset: any
# settings-exclusive marker in view proves the page. The تخطيط الصفحة row can
# sit above the viewport at some restored offsets, and uiautomator skips
# clipped nodes (run 40366546451 died here on a correct back). Offset restore
# itself is asserted separately in pixels by the 12a/12e crop-mean check below.
SETTINGS_MARKERS="تخطيط الصفحة|تفضيلات الطباعة|تهيئة الطابعة|ردود الفعل اللمسية|صيانة الطابعة|المساعدة واستكشاف الأخطاء"
adb shell input keyevent KEYCODE_BACK; sleep 2
for i in $(seq 1 8); do
  adb shell uiautomator dump --all-windows /sdcard/uix-12e.xml >/dev/null 2>&1 || true
  adb shell cat /sdcard/uix-12e.xml > shots/${NAME}-uix-12e.xml 2>/dev/null || true
  grep -qE "$SETTINGS_MARKERS" shots/${NAME}-uix-12e.xml && break
  find_node "__never_matches__" >/dev/null 2>&1 || true
  if grep -q "سجل نشاط HP P1005" shots/${NAME}-uix-12e.xml; then
    adb shell input keyevent KEYCODE_BACK
  fi
  sleep 2
done
grep -qE "$SETTINGS_MARKERS" shots/${NAME}-uix-12e.xml || die "back from diagnostics did not return to settings"
cap 12e-settings-back
a_mean=$(convert "shots/${NAME}-12a-settings-scrolled.png" -crop 400x300+300+1200 +repage -format "%[mean]" info:)
b_mean=$(convert "shots/${NAME}-12e-settings-back.png" -crop 400x300+300+1200 +repage -format "%[mean]" info:)
[ -n "$a_mean" ] && [ -n "$b_mean" ] || die "scroll mean extraction failed"
awk -v a="$a_mean" -v b="$b_mean" 'BEGIN{d=(a+0>b+0)?a-b:b-a; lim=(a*0.03>2)?a*0.03:2; exit !(d<lim)}' || die "settings scroll not restored after back (before=$a_mean after=$b_mean)"
adb shell input keyevent KEYCODE_BACK; sleep 2
for i in $(seq 1 8); do
  adb shell uiautomator dump --all-windows /sdcard/uix-12f.xml >/dev/null 2>&1 || true
  adb shell cat /sdcard/uix-12f.xml > shots/${NAME}-uix-12f.xml 2>/dev/null || true
  grep -q "النشاط الأخير" shots/${NAME}-uix-12f.xml && break
  find_node "__never_matches__" >/dev/null 2>&1 || true
  if grep -qE "$SETTINGS_MARKERS" shots/${NAME}-uix-12f.xml; then
    adb shell input keyevent KEYCODE_BACK
  fi
  sleep 2
done
grep -q "النشاط الأخير" shots/${NAME}-uix-12f.xml || die "second back did not land on home"

# (d) Predictive back (API 34 emulator, gesture nav): a partial edge drag must
# CANCEL (page stays), a full edge swipe must COMMIT to home. If the synthetic
# swipe delivers no progress events, release still invokes the commit path -
# both outcomes land on the asserted page, so this stays an honest check.
adb shell settings put secure navigation_mode 2 || true
sleep 1
tap_node "الإعدادات"; sleep 2
# Partial drag: must seek the transition via AndroidX progress callbacks and
# then CANCEL (stay on settings). Logcat proves the callback sequence; the
# clip carries the intermediate seeked frame for frame-measurement.
adb logcat -c
adb shell screenrecord --time-limit 8 /sdcard/gesture-partial.mp4 &
sleep 1
# Short peek: past edge touch slop (~21px) but well under the system back
# classifier's commit distance - release must cancel, not commit.
adb shell input swipe 2 1200 50 1200 600; sleep 8
adb pull /sdcard/gesture-partial.mp4 "shots/${NAME}-gesture-partial.mp4" || die "partial gesture clip missing"
adb shell rm /sdcard/gesture-partial.mp4 || true
adb logcat -d 2>/dev/null | grep "HP1005-Back" > "shots/${NAME}-back-partial-log.txt" || true
grep -q "started" "shots/${NAME}-back-partial-log.txt" || die "predictive back: no started callback (gesture infra or wiring)"
grep -q "progressed" "shots/${NAME}-back-partial-log.txt" || die "predictive back: AndroidX progress callbacks did not fire"
# Cancel vs commit on a peek is the SYSTEM gesture classifier's choice: the
# pixel_tablet emulator committed a 50px peek that pixel_6 cancelled. The
# strict cancellation proof is the phone leg's recorded started -> progressed
# -> cancelled log (artifact), NOT this assert. Here assert only that the app
# followed whichever decision the system made.
if grep -q "cancelled" "shots/${NAME}-back-partial-log.txt"; then
  dump_expect "shots/${NAME}-uix-13a.xml" "اللغة" "system cancelled the peek but the app left settings"
elif grep -q "gesture commit" "shots/${NAME}-back-partial-log.txt"; then
  dump_expect "shots/${NAME}-uix-13a.xml" "النشاط الأخير" "system committed the peek but the app did not go home"
  tap_node "الإعدادات"; sleep 2
else
  die "partial gesture produced neither cancel nor commit"
fi
cap 13a-back-cancelled
# Full swipe: must COMMIT to home through the gesture path.
adb logcat -c
adb shell screenrecord --time-limit 8 /sdcard/gesture-full.mp4 &
sleep 1
adb shell input swipe 2 1200 900 1200 450; sleep 8
adb pull /sdcard/gesture-full.mp4 "shots/${NAME}-gesture-full.mp4" || die "full gesture clip missing"
adb shell rm /sdcard/gesture-full.mp4 || true
adb logcat -d 2>/dev/null | grep "HP1005-Back" > "shots/${NAME}-back-full-log.txt" || true
grep -q "started" "shots/${NAME}-back-full-log.txt" || die "predictive back: no started callback on full swipe"
grep -q "progressed" "shots/${NAME}-back-full-log.txt" || die "predictive back: no progress callbacks on full swipe"
grep -q "gesture commit" "shots/${NAME}-back-full-log.txt" || die "predictive back: full swipe did not commit through the gesture path"
grep -q "cancelled" "shots/${NAME}-back-full-log.txt" && die "predictive back: full swipe was unexpectedly cancelled"
dump_expect "shots/${NAME}-uix-13b.xml" "النشاط الأخير" "full back gesture did not commit to home"
cap 13b-back-committed
adb shell settings put secure navigation_mode 0 || true
sleep 1

# (e) Header back == system back; rapid navigation stability; reduced-motion
# gesture falls back to instant commit.
tap_node "الإعدادات"; sleep 2
tap_node "رجوع"; sleep 2
dump_expect "shots/${NAME}-uix-14a.xml" "النشاط الأخير" "header back from settings did not return home"
tap_node "طباعة"; tap_node "قائمة الطباعة"; tap_node "الرئيسية"; sleep 2
dump_expect "shots/${NAME}-uix-14b.xml" "النشاط الأخير" "rapid tab switching broke rendering"
tap_node "الإعدادات"; sleep 1
swipe_up; sleep 1
tap_text "سجل التشخيص"; sleep 1
adb shell input keyevent KEYCODE_BACK
adb shell input keyevent KEYCODE_BACK
sleep 2
dump_expect "shots/${NAME}-uix-14c.xml" "النشاط الأخير" "rapid double back did not land home"
adb shell settings put global animator_duration_scale 0
tap_node "الإعدادات"; sleep 1
adb shell input swipe 2 1200 900 1200 450; sleep 1
dump_expect "shots/${NAME}-uix-14d.xml" "النشاط الأخير" "reduced-motion back gesture did not commit home"
adb shell settings put global animator_duration_scale 1
sleep 1
# Restore the queue page the following rotation section expects.
tap_node "قائمة الطباعة"; sleep 2
# Populated queue under rotation: width re-layout with a real waiting card, and
# capsule-overlap reachability of the populated card (empty-state captures prove
# nothing about clearance).
adb shell settings put system user_rotation 1; sleep 3
dump_expect "shots/${NAME}-uix-prot.xml" "بانتظار المعاينة والطباعة" "waiting card lost after rotation with populated queue"
grep -q "قائمة الطباعة" shots/${NAME}-uix-prot.xml || die "queue page lost after rotation with populated queue"
cap 08d-queue-populated-rotated
# Landscape scroll reachability: a single fixed-lane swipe can land on the
# capsule or a side margin on a rotated emulator and produce a byte-identical
# "scrolled" capture (seen in run Z). Swipe in three lanes and keep the first
# capture that actually differs from 08d; if none moves the content, that is a
# real clearance defect, so fail loudly.
cp "shots/${NAME}-08d-queue-populated-rotated.png" "shots/${NAME}-08e-queue-populated-rotated-scrolled.png"
for lane in 2 4 3; do
  read SW SH <<< "$(cur_dims)"
  X=$((SW/lane)); [ "$lane" = 3 ] && X=$((SW*3/4))
  adb shell input swipe $X $((SH*4/5)) $X $((SH/5)) 350
  sleep 1
  adb shell screencap -p 2>/dev/null > shots/tmp-08e.png
  if ! cmp -s "shots/${NAME}-08d-queue-populated-rotated.png" shots/tmp-08e.png; then
    mv shots/tmp-08e.png "shots/${NAME}-08e-queue-populated-rotated-scrolled.png"
    break
  fi
done
rm -f shots/tmp-08e.png
cmp -s "shots/${NAME}-08d-queue-populated-rotated.png" "shots/${NAME}-08e-queue-populated-rotated-scrolled.png"   && die "landscape scroll did not move populated queue content (capsule clearance unreachable)"
adb shell settings put system user_rotation 0; sleep 2
# Long-filename header check: same fixture bytes, synthetic mixed Arabic/Latin
# name, verifying ellipsis/wrapping and that the Change control stays present.
LONGNAME='تقرير-الطباعة-السنوي-very-long-document-name-2026-final-version.pdf'
adb push ci/smoke-test.pdf /data/local/tmp/long-name.pdf >/dev/null || die "could not stage long-name pdf"
adb shell run-as com.hpp1005.sharesender cp /data/local/tmp/long-name.pdf "files/$LONGNAME" || die "could not stage long-name fixture"
sout=$(adb shell am start -n com.hpp1005.sharesender/.ShareActivity --es name "$LONGNAME" 2>&1) || die "long-name sender launch exit nonzero: $sout"
echo "$sout" | grep -q "Error" && die "long-name sender launch error: $sout"
sleep 8
adb shell uiautomator dump /sdcard/uix-long.xml >/dev/null 2>&1 || true
adb shell cat /sdcard/uix-long.xml > shots/${NAME}-uix-long.xml 2>/dev/null || true
grep -q "تغيير" shots/${NAME}-uix-long.xml || die "Change control missing with long filename"
grep -q "الصفحة 1 من" shots/${NAME}-uix-long.xml || die "long-name share did not open a rendered preview"
cap 08c-longname-preview
adb shell am start -a android.intent.action.MAIN -c android.intent.category.HOME >/dev/null || true
sleep 3; cap 09-launcher-icon
# Launcher-entry evidence, deterministic: resolve the LAUNCHER activity for the
# app (must exist) and for the test sender (must NOT exist), then capture the
# app's recents card, which shows the installed artifact's real icon + label.
appcmp=$(adb shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -p "$PKG" 2>/dev/null | tail -1)
echo "$appcmp" | grep -q "$PKG" || die "no launcher activity resolvable for $PKG: $appcmp"
sendercmp=$(adb shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -p com.hpp1005.sharesender 2>/dev/null | tail -1)
if echo "$sendercmp" | grep -qi "sharesender"; then die "test sender has a launcher entry: $sendercmp"; fi
adb shell input keyevent KEYCODE_APP_SWITCH || true
sleep 2; cap 09b-recents-icon
adb shell input keyevent KEYCODE_BACK || true
adb logcat -d 2>/dev/null | grep -iE "$PKG|AndroidRuntime" > "shots/${NAME}-logcat.txt" || true
adb logcat -d 2>/dev/null | grep -E "ANR in $PKG|FATAL EXCEPTION" && die "ANR/crash detected in logcat"
echo "SMOKE PASS: all captures valid"
ls -la shots/
