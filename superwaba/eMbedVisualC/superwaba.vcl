<html>
<body>
<pre>
<h1>Build Log</h1>
<h3>
--------------------Configuration: superwaba - Win32 (WCE x86em) Release--------------------
</h3>
<h3>Command Lines</h3>
Creating temporary file "C:\DOCUME~1\shari\LOCALS~1\Temp\RSP48C.tmp" with contents
[
/nologo /W3 /D _WIN32_WCE=300 /D "WIN32" /D "STRICT" /D "_WIN32_WCE_EMULATION" /D "INTERNATIONAL" /D "USA" /D "INTLMSG_CODEPAGE" /D "WIN32_PLATFORM_PSPC" /D "i486" /D UNDER_CE=300 /D "UNICODE" /D "_UNICODE" /D "_X86_" /D "x86" /D "NDEBUG" /Fp"X86EMRel/superwaba.pch" /YX /Fo"X86EMRel/" /Gz /Oxs /c 
"C:\scytacki\superwaba\vmsrc\vm\waba.c"
]
Creating command line "cl.exe @C:\DOCUME~1\shari\LOCALS~1\Temp\RSP48C.tmp" 
Creating temporary file "C:\DOCUME~1\shari\LOCALS~1\Temp\RSP48D.tmp" with contents
[
corelibc.lib commctrl.lib coredll.lib winsock.lib aygshell.lib /nologo /stack:0x10000,0x1000 /subsystem:windows /incremental:no /pdb:"waba.pdb" /nodefaultlib:"OLDNAMES.lib" /nodefaultlib:libc.lib /nodefaultlib:libcd.lib /nodefaultlib:libcmt.lib /nodefaultlib:libcmtd.lib /nodefaultlib:msvcrt.lib /nodefaultlib:msvcrtd.lib /nodefaultlib:oldnames.lib /out:"waba.exe" /windowsce:emulation /MACHINE:IX86 
.\X86EMRel\waba.obj
]
Creating command line "link.exe @C:\DOCUME~1\shari\LOCALS~1\Temp\RSP48D.tmp"
<h3>Output Window</h3>
Compiling...
waba.c
C:\scytacki\superwaba\vmsrc\vm\nmwin32_b.c(395) : warning C4101: 'cr' : unreferenced local variable
C:\scytacki\superwaba\vmsrc\vm\nmwin32_b.c(837) : warning C4101: 'hWnd' : unreferenced local variable
Linking...



<h3>Results</h3>
waba.exe - 0 error(s), 2 warning(s)
</pre>
</body>
</html>
