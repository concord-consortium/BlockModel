;****************************************************************************
;*
;*  Copyright (C) 1996 - 2000  Dallas Semiconductor Corporation. 
;*  All rights Reserved. Printed in U.S.A.
;*  This software is protected by copyright laws of
;*  the United States and of foreign countries.
;*  This material may also be protected by patent laws of the United States 
;*  and of foreign countries.
;*  This software is furnished under a license agreement and/or a
;*  nondisclosure agreement and may only be used or copied in accordance
;*  with the terms of those agreements.
;*  The mere transfer of this software does not imply any licenses
;*  of trade secrets, proprietary technology, copyrights, patents,
;*  trademarks, maskwork rights, or any other form of intellectual
;*  property whatsoever. Dallas Semiconductor retains all ownership rights.
;*
;*     Module Name: TINIMacro
;*
;*     Description: Jellybean macros for data pointer manulation, movx access, 
;*                  etc.
;*
;*        Filename: tinimacro.inc
;*
;* Dependant Files: none
;*
;*          Author: All 
;*
;*        Compiler: a390
;*
;*         Version: 0.01 
;*
;*         Created: 11/11/97
;*
;*   Modifications: 
;*
;****************************************************************************
;
PUSH_DPTR1  MACRO
        push  dpl
        push  dph
        push  dpx
        ENDM
;****************************************************************************
;
POP_DPTR1  MACRO
        pop   dpx
        pop   dph
        pop   dpl
        ENDM
;****************************************************************************
;
PUSH_DPTR2  MACRO
        push  dpl1
        push  dph1
        push  dpx1
        ENDM
;****************************************************************************
;
POP_DPTR2  MACRO
        pop   dpx1
        pop   dph1
        pop   dpl1
        ENDM
;****************************************************************************
; push R1:R0
PUSH_R1R0  MACRO
        push  0
        push  1
        ENDM
;****************************************************************************
;
POP_R1R0  MACRO
        pop   1
        pop   0
        ENDM
;****************************************************************************
;
PUSH_R3R2  MACRO
        push  2
        push  3
        ENDM
;****************************************************************************
;
POP_R3R2  MACRO
        pop   3
        pop   2
        ENDM
;****************************************************************************
;
PUSH_R5R4  MACRO
        push  4
        push  5
        ENDM
;****************************************************************************
;
POP_R5R4  MACRO
        pop   5
        pop   4
        ENDM
;****************************************************************************
;
PUSH_R7R6  MACRO
        push  6
        push  7
        ENDM
;****************************************************************************
;
POP_R7R6  MACRO
        pop   7
        pop   6
        ENDM
;****************************************************************************
;
PUSH_BANK_0  MACRO
        push  R0_B0
        push  R1_B0
        push  R2_B0
        push  R3_B0
        push  R4_B0
        push  R5_B0
        push  R6_B0
        push  R7_B0
        ENDM
;****************************************************************************
;
POP_BANK_0  MACRO
        pop   R7_B0                  
        pop   R6_B0
        pop   R5_B0                  
        pop   R4_B0
        pop   R3_B0
        pop   R2_B0
        pop   R1_B0
        pop   R0_B0
        ENDM
;****************************************************************************
;
PUSH_BANK_1  MACRO
        push  R0_B1
        push  R1_B1
        push  R2_B1
        push  R3_B1
        push  R4_B1
        push  R5_B1
        push  R6_B1
        push  R7_B1
        ENDM
;****************************************************************************
;
POP_BANK_1  MACRO
        pop   R7_B1                  
        pop   R6_B1
        pop   R5_B1                  
        pop   R4_B1
        pop   R3_B1
        pop   R2_B1
        pop   R1_B1
        pop   R0_B1
        ENDM
;****************************************************************************
;
PUSH_BANK_2  MACRO
        push  R0_B2
        push  R1_B2
        push  R2_B2
        push  R3_B2
        push  R4_B2
        push  R5_B2
        push  R6_B2
        push  R7_B2
        ENDM
;****************************************************************************
;
POP_BANK_2  MACRO
        pop   R7_B2                  
        pop   R6_B2
        pop   R5_B2                  
        pop   R4_B2
        pop   R3_B2
        pop   R2_B2
        pop   R1_B2
        pop   R0_B2
        ENDM
;****************************************************************************
;
PUSH_BANK_3  MACRO
        push  R0_B3
        push  R1_B3
        push  R2_B3
        push  R3_B3
        push  R4_B3
        push  R5_B3
        push  R6_B3
        push  R7_B3
        ENDM
;****************************************************************************
;
POP_BANK_3  MACRO
        pop   R7_B3                  
        pop   R6_B3
        pop   R5_B3                  
        pop   R4_B3
        pop   R3_B3
        pop   R2_B3
        pop   R1_B3
        pop   R0_B3
        ENDM
;****************************************************************************
;
GETX  MACRO
        movx  a, @dptr
        ENDM
;****************************************************************************
;
PUTX  MACRO
        movx  @dptr, a
        ENDM
;****************************************************************************
;
GETC_AT_0  MACRO
        clr   a
        movc  a, @a+dptr
        ENDM
;****************************************************************************
;
GETX_AT_R0  MACRO
        movx  a, @r0
        ENDM
;****************************************************************************
;
PUTX_AT_R0  MACRO
        movx  @r0, a
        ENDM
;****************************************************************************
;
USE_REGISTER_BANK_0  MACRO 
        anl   psw, #0E7H
        ENDM
;*********** Deprecated ***********
USE_BANK_0  MACRO
        USE_REGISTER_BANK_0
        ENDM
;****************************************************************************
;
USE_REGISTER_BANK_1  MACRO 
        anl   psw, #0E7H
        orl   psw, #008H
        ENDM
;****************************************************************************
;
USE_REGISTER_BANK_2  MACRO 
        anl   psw, #0E7H
        orl   psw, #010H
        ENDM
;****************************************************************************
;
USE_REGISTER_BANK_3  MACRO 
        anl   psw, #0E7H
        orl   psw, #018H
        ENDM
;****************************************************************************
;
; NOTE: a and psw are destroyed.
;
ADD_DPTR_0_BA  MACRO
        add   a, dpl
        mov   dpl, a
        mov   a, dph
        addc  a, b
        mov   dph, a
        clr   a
        addc  a, dpx
        mov   dpx, a
        ENDM
;****************************************************************************
;
; NOTE: a and psw are destroyed.
;
ADD_DPTR_0_A  MACRO
        add   a, dpl
        mov   dpl, a
        mov   a, dph
        addc  a, #0 
        mov   dph, a 
        clr   a
        addc  a, dpx
        mov   dpx, a
        ENDM
;****************************************************************************
;
; NOTE: a and psw are destroyed
;
ADD_DPTR_1_A  MACRO
        add   a, dpl1
        mov   dpl1, a
        mov   a, dph1
        addc  a, #0 
        mov   dph1, a 
        clr   a
        addc  a, dpx1
        mov   dpx1, a
        ENDM
;****************************************************************************
;
; NOTE: Assumes dps is already 0 (ALL BITS 0)
;
DEC_DPTR_0  MACRO
        mov   dps, #40H              
        inc   dptr
        mov   dps, #0H
        ENDM
;****************************************************************************
;
; NOTE: Assumes dps is already 1 (00000001)
;
; This is intentionally bogus. Want to avoid confusion when converting all
; functions/macros/docs to dptr0, dptr1!
;
DEC_DPTR_2  MACRO
        mov   dps, #81H              
        inc   dptr
        mov   dps, #1H
        ENDM
;****************************************************************************
;
TINIOS_ENTER_CRITICAL_SECTION   MACRO
        inc   wos_crit_count
        ENDM
;*********** Deprecated ***********
WOS_ENTER_CRITICAL_SECTION  MACRO
        TINIOS_ENTER_CRITICAL_SECTION
        ENDM
;****************************************************************************
;
TINIOS_EXIT_CRITICAL_SECTION  MACRO
        dec     wos_crit_count
        ENDM
;*********** Deprecated ***********
WOS_EXIT_CRITICAL_SECTION  MACRO
        TINIOS_EXIT_CRITICAL_SECTION
        ENDM
;****************************************************************************
;
TINIOS_DISABLE_INTERRUPTS_AND_PUSH_INTERRUPT_STATE  MACRO
        push  ie
        clr   ea
        ENDM
;*********** Deprecated ***********
WOS_DISABLE_INTERRUPTS  MACRO
        TINIOS_DISABLE_INTERRUPTS_AND_PUSH_INTERRUPT_STATE
        ENDM
;****************************************************************************
;
TINIOS_POP_INTERRUPT_STATE  MACRO
        pop   ie
        ENDM
;*********** Deprecated ***********
WOS_ENABLE_INTERRUPTS  MACRO
        TINIOS_POP_INTERRUPT_STATE
        ENDM
;****************************************************************************
;
TINIOS_REBOOT  MACRO
        clr     EA
        ljmp    WOS_Init
        ENDM
;*********** Deprecated ***********
WOS_REBOOT  MACRO
        TINIOS_REBOOT
        ENDM
;****************************************************************************
;
TIMEDACCESS  MACRO
        mov     TA,#0AAH
        mov     TA,#055H
        ENDM
;****************************************************************************
;
ENTER_PCE_SPACE_AND_PUSH_STATE  MACRO  PARAM  PCE_VALUE
        TINIOS_DISABLE_INTERRUPTS_AND_PUSH_INTERRUPT_STATE
        push    MCON
        mov     a, MCON
        anl     a, #0F0h   ; mask out PCE bits
        orl     a, PCE_VALUE ; or in our pce
        TIMEDACCESS
        mov     MCON, a
        ENDM
;****************************************************************************
;
EXIT_PCE_SPACE_AND_POP_STATE  MACRO
        TIMEDACCESS
        pop     MCON
        TINIOS_POP_INTERRUPT_STATE
        ENDM
;****************************************************************************
;
;
FEEDDOG  MACRO
        TIMEDACCESS
        orl     WDCON,#001H
        ENDM
;****************************************************************************
;
ENABLE_ONE_WIRE  MACRO
        ENDM
;****************************************************************************
;
ENABLE_EXTERNAL_BUS  MACRO
        ENDM
;****************************************************************************
;
DISABLE_EXTERNAL_BUS  MACRO
        TIMEDACCESS
        orl     MCON,#00001110B
        ENABLE_RAM
        ENDM
;****************************************************************************
;
SAVE_P1_STATE  MACRO
        ENDM
;****************************************************************************
;
RESTORE_P1_STATE  MACRO
        ENDM

PUSH_CODEBANK  MACRO
        push    AP
        ENDM
;****************************************************************************
;
HALT   MACRO
        clr   ea                     ; No fooling man. Really halt       
        clr   p3.5                   ; Turn on status LED.
        sjmp  $
        ENDM
;****************************************************************************
;
BLINK_HALT  MACRO
LOCAL blink_again, blink_wait_low, blink_wait_high
        clr   ea

blink_again:
        clr   p3.5
        mov   r0, #0
        mov   r1, #0
        mov   r2, #10H

blink_wait_low:
        djnz  r0, blink_wait_low
        djnz  r1, blink_wait_low
        djnz  r2, blink_wait_low
        setb  p3.5

        mov   r2, #10H

blink_wait_high:
        djnz  r0, blink_wait_high
        djnz  r1, blink_wait_high
        djnz  r2, blink_wait_high
        sjmp  blink_again
        ENDM
;****************************************************************************
;
BLINK       MACRO
LOCAL blink_wait_low_1, blink_wait_high_1
        push  R0_B0
        push  R1_B0
        push  R2_B0
;
        clr   p3.5
        mov   r0, #0
        mov   r1, #0
        mov   r2, #10H

blink_wait_low_1:
        djnz  r0, blink_wait_low_1
        djnz  r1, blink_wait_low_1
        djnz  r2, blink_wait_low_1
        setb  p3.5

        mov   r2, #10H

blink_wait_high_1:
        djnz  r0, blink_wait_high_1
        djnz  r1, blink_wait_high_1
        djnz  r2, blink_wait_high_1
;
        pop   R2_B0
        pop   R1_B0
        pop   R0_B0
        ENDM
;****************************************************************************
;
; Store the handle in r2, r3 and physical pointer in dpx1:dpl1:dph1 as a
; handle pair at the location pointed to by dptr. Assuming dps = 0 ???
;
STORE_HPP   MACRO
        PUSH_DPTR1
        push  acc
        mov   a, r2
        PUTX
        inc   dptr
        mov   a, r3
        PUTX
        inc   dptr
        mov   a, dpl1
        PUTX
        inc   dptr
        mov   a, dph1
        PUTX
        inc   dptr
        mov   a, dpx1
        PUTX
        pop   acc
        POP_DPTR1
        ENDM 
;****************************************************************************
;
; Load the HPP pointed to by dptr1 into r3:r2 and dptr1 as if just returned 
; from malloc.
;
LOAD_HPP   MACRO
        push  acc
        GETX
        mov   r2, a
        inc   dptr
        GETX
        mov   r3, a
        inc   dptr
        GETX
        push  acc
        inc   dptr
        GETX
        push  acc
        inc   dptr
        GETX
        mov   dpx, a
        pop   dph
        pop   dpl
        pop   acc
        ENDM
;****************************************************************************
;
; Extract the physical pointer from a handle pair pointed to by dptr. 
;
LOAD_PHYS_FROM_HPP  MACRO
        push  acc
        inc   dptr
        inc   dptr
        GETX
        push  acc
        inc   dptr
        GETX
        push  acc
        inc   dptr
        GETX
        xch   a, dpx 
        pop   dph
        pop   dpl
        pop   acc
        ENDM
;****************************************************************************
;
; Extract the handle from a handle pair pointed to by dptr. 
;
LOAD_HANDLE_FROM_HPP  MACRO
        push  acc
        GETX
        xch   a, r2
        inc   dptr
        GETX
        xch   a, r3 
        pop   acc
        ENDM
;****************************************************************************
;
; Zero the 5 bytes consumed by a handle pair. 
;
CLEAR_HPP   MACRO
        push  acc
        clr   a
        PUTX
        inc   dptr
        PUTX
        inc   dptr
        PUTX
        inc   dptr
        PUTX
        inc   dptr
        PUTX
        pop   acc
        ENDM
;****************************************************************************
;
DEBUG_DUMP_STRING  MACRO PARAM TEXT_PTR
        PUSH_DPTR1
        mov   dptr, #TEXT_PTR
        call  Info_SendString
        call  Info_SendCRLF
        POP_DPTR1
        ENDM
;****************************************************************************

