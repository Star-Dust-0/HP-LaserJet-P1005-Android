#!/usr/bin/env python3
"""Real Activity dialogs + real PDF composition/conversion, synthetic USB sink only."""
import subprocess as sp, time, re, pathlib, xml.etree.ElementTree as ET
PKG='com.hpp1005.printer'; CMP=PKG+'/com.mohamedelnaggar.hpp1005.MainActivity'
OUT=pathlib.Path('shots');OUT.mkdir(exist_ok=True)
def adb(*a):return sp.check_output(['adb',*a],text=True).strip()
def dump():
 adb('shell','uiautomator','dump','/sdcard/flow.xml')
 return ET.fromstring(adb('exec-out','cat','/sdcard/flow.xml'))
def find(text):
 for n in dump().iter('node'):
  if text in (n.get('text',''),n.get('content-desc','')):
   x1,y1,x2,y2=map(int,re.findall(r'\d+',n.get('bounds')));return ((x1+x2)//2,(y1+y2)//2)
def tap(text):
 end=time.time()+40
 while time.time()<end:
  xy=find(text)
  if xy:adb('shell','input','tap',*map(str,xy));return
  # Phone preview is taller than one viewport. Scroll the real page, not a fixed tap.
  size=adb('shell','wm','size');w,h=map(int,re.findall(r'(\d+)x(\d+)',size)[-1])
  adb('shell','input','swipe',str(w//2),str(h*4//5),str(w//2),str(h//3),'350')
 raise AssertionError('missing button '+text)
def cap(name):
 (OUT/(name+'-log.txt')).write_text(logs())
 with open(OUT/(name+'.png'),'wb') as f:sp.run(['adb','exec-out','screencap','-p'],stdout=f,check=True)
def logs():return adb('logcat','-d','-s','HPPrintFlow:I','HpPrintService:I','AndroidRuntime:E','*:S')
def config(**kw):
 args=['shell','am','broadcast','-a','com.hpp1005.printer.DEBUG_PRINT_FLOW','-p',PKG]
 for k,v in kw.items():args+=['--ez' if isinstance(v,bool) else '--ei' if isinstance(v,int) else '--es',k,str(v).lower() if isinstance(v,bool) else str(v)]
 before=logs().count('fixture configured')
 deadline=time.time()+30
 while True:
  result=adb(*args)
  time.sleep(1)
  if logs().count('fixture configured')>before:break
  if time.time()>deadline:raise AssertionError('fixture receiver not ready: '+result)
def reset(name, **kw):
 adb('shell','am','force-stop',PKG);adb('shell','am','start','-n',CMP)
 time.sleep(2);adb('logcat','-c')
 config(file='/data/local/tmp/flow.pdf',layoutMode='booklet',bookletBinding='left',pageRange='all',copies=1,enabled=True,**kw)
 tap('طباعة');tap('طباعة الوجه الأول')
def wait_count(count,seconds=90):
 end=time.time()+seconds
 while time.time()<end:
  l=logs()
  if l.count('fixture accepted stream')>=count:return
  if 'FATAL EXCEPTION' in l:raise AssertionError(l)
  time.sleep(1)
 raise AssertionError('stream count '+str(count)+' '+logs())
def assert_absent(text):assert not find(text), 'unexpected '+text
import atexit
atexit.register(lambda: cap('flow-final-state'))
adb('wait-for-device')
# First UI proof is the exact reported one-page scenario.
reset('one-page');wait_count(1);time.sleep(2);assert_absent('طباعة الوجه الثاني');cap('flow-one-page-complete')
# Multiple copies must advance without asking for a blank back.
reset('copies',copies=2) if False else None
# reset uses defaults merged here to keep each scenario independent.
def scenario(file='flow.pdf', **kw):
 adb('shell','am','force-stop',PKG);adb('shell','am','start','-n',CMP);time.sleep(2);adb('logcat','-c')
 opts=dict(file='/data/local/tmp/'+file,layoutMode='booklet',bookletBinding='left',pageRange='all',copies=1,enabled=True);opts.update(kw);config(**opts)
 tap('طباعة');tap('طباعة الوجه الأول')
scenario(copies=2);wait_count(1);tap('طباعة النسخة 2');wait_count(2);time.sleep(2);assert_absent('طباعة الوجه الثاني');cap('flow-one-page-two-copies')
# An original page selected from a multi-page PDF has the same blank-back behavior.
scenario('flow4.pdf',pageRange='3,3',bookletBinding='right');wait_count(1);time.sleep(2);assert_absent('طباعة الوجه الثاني');cap('flow-single-range-right')
for binding in ('left','right'):
 scenario('flow4.pdf',bookletBinding=binding);wait_count(1);tap('طباعة الوجه الثاني');wait_count(2);time.sleep(2);cap('flow-four-pages-'+binding)
# Transport failures stop the flow. Clearing the fault then retrying must work.
for fault in ('disconnect','permission','transfer'):
 scenario(fault=fault);time.sleep(3);assert_absent('طباعة الوجه الثاني');assert 'fixture accepted stream' not in logs();cap('flow-failure-'+fault)
 config(fault='none');tap('طباعة');tap('طباعة الوجه الأول');wait_count(1);time.sleep(2);cap('flow-retry-'+fault)
# Cancel after first conversion starts: no reinsert dialog or next copy is allowed.
scenario('flow8.pdf',delay=3000,copies=2)
time.sleep(1);config(cancel=True,delay=3000);time.sleep(7);assert_absent('طباعة الوجه الثاني');assert_absent('طباعة النسخة 2');cap('flow-cancel-no-followup')
config(cancel=False,delay=0);tap('طباعة');tap('طباعة الوجه الأول');wait_count(logs().count('fixture accepted stream')+2);tap('التوقف هنا');cap('flow-cancel-retry-front')
# Non-booklet layouts and manual duplex use the same production pass executor.
for mode in ('size','multiple','poster'):
 scenario_file='flow4.pdf' if mode!='poster' else 'flow.pdf'
 adb('shell','am','force-stop',PKG);adb('shell','am','start','-n',CMP);time.sleep(2);adb('logcat','-c')
 config(file='/data/local/tmp/'+scenario_file,layoutMode=mode,duplexMode='off',pageRange='all',copies=1,enabled=True)
 tap('طباعة');wait_count(4 if mode=='size' else 2 if mode=='multiple' else 1);time.sleep(3);cap('flow-layout-'+mode)
for edge in ('manual-long','manual-short'):
 adb('shell','am','force-stop',PKG);adb('shell','am','start','-n',CMP);time.sleep(2);adb('logcat','-c')
 config(file='/data/local/tmp/flow4.pdf',layoutMode='multiple',pagesPerSheet=2,duplexMode=edge,pageRange='all',copies=1,enabled=True)
 tap('طباعة');tap('طباعة الصفحات الفردية');wait_count(1);tap('طباعة الوجه الثاني');wait_count(2);time.sleep(2);cap('flow-duplex-'+edge)
(OUT/'print-flow-logcat.txt').write_text(logs())
print('PRINT FLOW ACTIVITY SMOKE PASS')
