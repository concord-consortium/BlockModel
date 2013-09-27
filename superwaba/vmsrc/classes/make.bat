@echo off
\waba\bin\warp.exe c /c WABA waba waba\fx\*.class waba\io\*.class waba\lang\*.class waba\sys\*.class waba\ui\*.class waba\util\*.class
copy *.pdb c:\palm\guich\install
copy *.pdb h:\wabavm
copy *.pdb J:\waba\vmsrc\vm\palm\
if '%1' == '' copy ..\vmaux\project\waba.prc c:\palm\guich\install\
if '%1' == '' copy ..\vmaux\project\waba.prc h:\wabavm\
regedit /s callHotSync.reg
del /y/s/q/z/e h:\wabavm\classes
copy /s/q/e waba h:\wabavm\classes\waba
dir *.pdb