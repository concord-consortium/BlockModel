Native_ReadMeNOW.txt (DC, 09.14.01)

-=---------------=-
TINI Firmware 1.02c
-=---------------=-
--------------------------------------------------------------------
Go to http://www.ibutton.com/TINI/book.html for an online version of 
"The TINI Specification and Developer's Guide".
--------------------------------------------------------------------

In all previous firmware revisions the Mem_Copy*, Mem_Clear* and Mem_Compare* 
functions were documented to use a specific data pointer for target and source.
The implementations of these functions, however, did not set DPS to a known 
value so the role of target and source could be reversed if DPS was not zero 
when these functions were called. 

The TINI 1.02 firmware now sets DPS on entry to these functions.

Existing native libraries may have been written to make use of this bug and the
fix could break these libraries. If your native library was written to expect 
a certain behavior when DPS was non-zero, when calling these functions, you
still have access to the old entry points to these functions. The original entry
points have the same name as in previous releases with a "_0" appended. You can
either change your calls in your native library to use the "_0" functions or
you can hack the equates in tini.inc.

Example:
                                      ; these numbers may not match
                                      ; the address in the actual release
                                       ;MEM_CLEAR  EQU 01c4f5H ;
                                   MEM_CLEAR_NEW  EQU 01c4f5H ;
                                      ;...
                                     ;MEM_CLEAR_0  EQU 01c507H ;
                                     MEM_CLEAR    EQU 01c507H ;
