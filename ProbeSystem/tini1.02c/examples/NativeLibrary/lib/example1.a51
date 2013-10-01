;****************************************************************************
;*
;*  Copyright (C) 2000 Dallas Semiconductor Corporation. 
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
;*     Module Name: Example1
;*
;*     Description: Native library example
;*
;*        Filename: Example1.a51
;*
;* Dependant Files: tini.inc, ds80c390.inc, tinimacro.inc apiequ.inc
;*
;*          Author: Chenot 
;*
;*           Tools: macro + a390
;*
;*         Version: 0.01 
;*
;*         Created: 05/19/2000 
;*
;*   Modifications: 11/00 - changed LoadPointer calls to LoadJavaByteArray
;*                        - added support for "indirect" registers
;*                        - added process destroy function
;*                        - added sleep example 
;*                  
;*           Notes: Native library rules:
;*                  - No ACalls or AJmps -> this module can span segments.
;*                  - The module must be contiguous. No ORG statements
;*                    allowed. The module offsets are automatically
;*                    set to zero by the assembler.
;*                  - There are absolutely no directs that persist
;*                    across method calls. Certain ranges of directs are
;*                    reserved for global system variables. You can be
;*                    process swapped at any time. Touch them and you
;*                    scrog the system!!!
;*                    At the time this was written this includes 
;*                    20h-21h and 68h and above.
;*                    All other directs MUST be returned to the
;*                    state that they were in before the method was called.
;*                  - Indirects are reserved for system use only. Touch them
;*                    and you scrog the system!!! As of release 1.02 a handful
;*                    of "indirects" have been reserved for native libraries.
;*                    See Native_Methods.txt for details.
;*                  - Register banks 0, 1 and 3 are free for library use.
;*                    Register bank 2 can only be used after method
;*                    parameters are removed from the Java stack.
;*                  - The file tini.inc contains the address of all
;*                    exported systems functions. These are static linkages
;*                    that are tied to a specific firmware release. Your
;*                    module MUST be reassembled for other firmware revisions.
;*                  - The file apiequ.inc contains the numbers that represent
;*                    exceptions and class numbers. These are static linkages
;*                    tied to a specific firmware release. Your module MUST
;*                    be reassembled with this file for each release.
;*                  - Threads in native methods are not automatically
;*                    swapped by the thread scheduler. Details on swapping
;*                    from native methods are contained in Native_Methods.txt. 
;*                  - We reserve the right to change these rules in future
;*                    releases.
;*                  - See the native method Readme file in the distribution
;*                    for more details.
;*
;****************************************************************************
;
; $Workfile: example1.a51 $
; $Revision: 11 $ 
; $Date: 9/13/01 10:15a $ 
; $Author: Chenot $ 
; $Modtime: 9/13/01 10:05a $
;
;
$include(tini.inc)
$include(ds80c390.inc)
$include(tinimacro.inc)
$include(apiequ.inc)
;
;***************************************************************************
;*
;* Function Name: Example1_Init
;*
;*   Description: Initialization routine
;*
;*      Input(s): None
;*
;*    Outputs(s): A - 0 if success
;*                
;*         Notes: Every TINI library is required to have an initialization
;*                function and the initialization routine MUST be located
;*                at offset zero into the file. This function is called at the
;*                end of the Java load/loadLibrary method invocation. If
;*                this init routine returns with any non-zero value in ACC
;*                the load will fail with an UnsatisfiedLinkError. This 
;*                function should be used to malloc memory and/or initialize
;*                data/hardware.
;*                This function will be called each time load/loadLibrary
;*                is called. State should be checked in order to determine
;*                whether or not initialization is required.
;*
;*                The minimum init routine must include:
;*                  clr a
;*                  ret
;*
;* *** This routine must exist ***
;* *** This routine must exist ***
;* *** This routine must exist ***
;* *** This routine must be at offset 0 ***
;* *** This routine must be at offset 0 ***
;* *** This routine must be at offset 0 ***
;*
;****************************************************************************
Example1_Init:
  mov   a, #SYSTEM_MAX_INDIRECT  ; see if there are enough indirects
  cjne  a, #8, $+3               ;
  jnc   Init_HaveEnoughRegisters ;

  mov   a, #0ffh                 ; return error code
  ljmp  Init_Done                ;

Init_HaveEnoughRegisters:
  lcall System_AcquireIndirectSemaphore
  jz    Init_RegisterDestroyFunction

  mov   a, #0feh                 ; return error code
  ljmp  Init_Done                ;

Init_RegisterDestroyFunction:
  mov   dptr, #Process_Destroy_Function
  lcall System_RegisterProcessDestroyFunction
  jz    Init_GetEphemeralStateBlock

  lcall System_ReleaseIndirectSemaphore
  mov   a, #0fdh                 ; return error code
  ljmp  Init_Done                ;

Init_GetEphemeralStateBlock:
  mov   dptr, #LibraryID         ; see if we have an ESB installed
  lcall NatLib_GetEphemeralStateBlock

  jnz   Init_MallocEphemeral     ;
;
; an ESB exists - some process
; has already called load since
; boot time
;
Init_StoreRetrieveEphemeral:
  sjmp  Init_TestImmutable       ;

Init_MallocEphemeral:
  mov   R2, #8h                  ; request 8 bytes
  mov   R3, #0                   ;
  lcall mm_Malloc                ;
  jnz   Init_Done                ;

  mov   dpl1, dpl                ; copy address to
  mov   dph1, dph                ;  second dptr
  mov   dpx1, dpx                ; handle is already in R3:R2

  mov   dptr, #LibraryID         ; first dptr is our identifier
  lcall NatLib_InstallEphemeralStateBlock

Init_TestImmutable:
  mov   dptr, #LibraryID         ; see if there is an ISB installed
  lcall NatLib_GetImmutableStateBlock
  jnz   Init_PerformMalloc       ;
;
; an ISB exists - some process has
; called load since the heap was
; cleared
;
Init_StoreRetrieveImmutable:
  GETX                           ; just using this as a load counter
  add   a, #1                    ;
  PUTX                           ;
  inc   dptr                     ;
  GETX                           ;
  addc  a, #0                    ;
  PUTX                           ;
  clr   a                        ;
  sjmp  Init_ClearIndirects      ;

Init_PerformMalloc:
  mov   R2, #2h                  ; request 2 bytes
  mov   R3, #0                   ;  for a simple counter
  lcall mm_Malloc                ;
  jnz   Init_Done                ;

  mov   dpl1, dpl                ; copy address to
  mov   dph1, dph                ;  second dptr
  mov   dpx1, dpx                ; handle is already in R3:R2

  mov   dptr, #LibraryID         ; first dptr is our identifier
  lcall NatLib_InstallImmutableStateBlock

Init_ClearIndirects:
;
; init indirects
;
  clr   a                        ; indicate success
  mov   R0, #SYSTEM_INDIRECT_START
  mov   b, #8                    ;

Init_Loop: 
  mov   @R0, a                   ;
  inc   R0                       ;
  djnz  b, Init_Loop             ;

Init_Done:
  ret                            ;
;****************************************************************************
;*
;* This is the library identifier that is used to access library state 
;* blocks. These state blocks are malloced and freed by the library
;* and inserted into/removed from system state block tables using 
;* the InstallStateBlock/RemoveStateBlock functions. The GetStateBlock
;* functions can be used to retrieve both the pointer and handle to a 
;* library's state block. There are two types of state blocks: Ephemeral and
;* Immutable. Ephemeral state blocks exist from the time they are created
;* until the time the system reboots. They are cleaned up automatically
;* at boot time. Immutable state blocks exist until the heap is cleared
;* (B18, F0 at the loader prompt) or until the library removes the block
;* from the table and frees the memory.
;*
;* This identifier must be exactly 8 bytes long and can be any non-zero
;* 8 byte data.
;*****************
LibraryID:      ;*
db "DS"         ;*
db 0,0,0,0,0,1  ;*
;*****************
;****************************************************************************
;*
;* Function Name: Native_method1
;*
;*   Description: public static native int method1(byte[] b, long l, int i)
;*
;*      Input(s): Byte array reference, long and int on the Java stack.
;*
;*    Outputs(s): A - zero on success, exception number on failure.
;*                returns an int in R3:R2:R1:R0, msb:...:lsb.
;*                
;*         Notes: System calls that are documented to return a value
;*                return 0 on success and an exception number on failure.
;*                In general is is a good idea to exit your native method
;*                with this value unless you wish to handle the failure
;*                in your native method. If wish to generate your own
;*                exception in your native method, you may use the 
;*                exception equates in apiequ.inc. Simply assign the
;*                equate to A just prior to returning to Java.
;*                On entry to a native method the first DPTR (via DPS) 
;*                is selected and RB0 (via PSW) is selected.
;*
;****************************************************************************
Native_method1:
  clr    a                       ; request first parameter
  lcall  NatLib_LoadJavaByteArray; point to Java buffer
  jz     NM1_PointerIsValid      ;

  ljmp   NM1_Exit                ;

NM1_PointerIsValid:
  mov    a, R0                   ; check the array length
  orl    a, R1                   ;
  orl    a, R2                   ;
  orl    a, R3                   ;
  jz     NM1_GetNextParams       ; 

;
; pointing to data - let's just jam
; some bytes into this buffer
;
  mov    b, #'U'                 ;
  mov    a, R0                   ;
  jz     NM1_TestR1R0            ;

  inc    R1_B0                   ;
;
; we must adjust the loop variables first
;
NM1_TestR1R0:
  mov    a, R1                   ;
  jz     NM1_TestR2R1R0          ;

  inc    R2_B0                   ;

NM1_TestR2R1R0:
  mov    a, R2                   ;
  jz     NM1_BuffSetGo           ;

  inc    R3_B0                   ;

NM1_BuffSetGo:
  mov    a, b                    ;

NM1_Loop:
  PUTX                           ; iterate through the array
  inc    dptr                    ;  
  djnz   R0, NM1_Loop            ; currently Java array lengths are two bytes
  djnz   R1, NM1_Loop            ;  in length but this will be compatible
  djnz   R2, NM1_Loop            ;  for all future releases. R2 and R3 are
  djnz   R3, NM1_Loop            ;  currently set to zero by 
                                 ;  NatLib_LoadJavaByteArray
NM1_GetNextParams:
  mov    a, #1                   ;
  lcall  NatLib_LoadWidePrimitive; point to wide parameter

  mov   dptr, #LibraryID         ; save this param 
  push  R2_B0                    ; preserve registers
  push  R3_B0                    ; - we don't need the handle
  lcall NatLib_GetEphemeralStateBlock ; in the state block
  pop   R3_B0                    ; restore registers
  pop   R2_B0                    ;
  jz    NM1_HaveStateBlock       ;

  mov   a, #java_lang_NullPointerException
  sjmp  NM1_Exit                 ;

NM1_HaveStateBlock:
  mov   a, R0                    ; write it to our state buffer
  PUTX                           ;
  inc   dptr                     ;
  mov   a, R1                    ; 
  PUTX                           ;
  inc   dptr                     ;
  mov   a, R2                    ; 
  PUTX                           ;
  inc   dptr                     ;
  mov   a, R3                    ; 
  PUTX                           ;
  inc   dptr                     ;
  mov   a, R4                    ; 
  PUTX                           ;
  inc   dptr                     ;
  mov   a, R5                    ; 
  PUTX                           ;
  inc   dptr                     ;
  mov   a, R6                    ; 
  PUTX                           ;
  inc   dptr                     ;
  mov   a, R7                    ; 
  PUTX                           ;

  mov    a, #3                   ;
  lcall  NatLib_LoadPrimitive    ; point to parameter
;
; we just popped the param into
; R3:R0 - let's just return them
; as the result
;
  clr  a                         ; indicate "no exception"

NM1_Exit:
  ret                            ;
;****************************************************************************
;*
;* Function Name: Native_method2
;*
;*   Description: public native long method2(void)
;*
;*      Input(s): "this" reference on the Java stack
;*
;*    Outputs(s): A - zero on success, exception number on failure.
;*                returns a long in R7:R6:R5:R4:R3:R2:R1:R0, msb:...:lsb.
;*                
;*         Notes: System calls that are documented to return a value
;*                return 0 on success and an exception number on failure.
;*                In general is is a good idea to exit your native method
;*                with this value unless you wish to handle the failure
;*                in your native method. If wish to generate your own
;*                exception in your native method, you may use the 
;*                exception equates in apiequ.inc. Simply assign the
;*                equate to A just prior to returning to Java.
;*
;****************************************************************************
Native_method2:
;
; NOTE: this is a virtual method so we could
;       pop the "this" reference off of the
;       Java stack if we so desired.
;
  mov   dptr, #LibraryID         ; lets save this param 
  lcall NatLib_GetEphemeralStateBlock ; in the state block
  jz    NM2_HaveStateBlock       ;

  mov   a, #java_lang_NullPointerException
  sjmp  NM2_Exit                 ;

NM2_HaveStateBlock:
;
; get the return value from the buffer 
;
  GETX                           ;
  mov   R0, a                    ;
  inc   dptr                     ;
  GETX                           ;
  mov   R1, a                    ;
  inc   dptr                     ;
  GETX                           ;
  mov   R2, a                    ;
  inc   dptr                     ;
  GETX                           ;
  mov   R3, a                    ;
  inc   dptr                     ;
  GETX                           ;
  mov   R4, a                    ;
  inc   dptr                     ;
  GETX                           ;
  mov   R5, a                    ;
  inc   dptr                     ;
  GETX                           ;
  mov   R6, a                    ;
  inc   dptr                     ;
  GETX                           ;
  mov   R7, a                    ;
  
  clr   a                        ; indicate "no exceptions"

NM2_Exit:
  ret                            ;
;****************************************************************************
;*
;* Function Name: Native_method3
;*
;*   Description: public static native long method3(void)
;*
;*      Input(s): None 
;*
;*    Outputs(s): A - zero on success, exception number on failure.
;*                returns a long in R7:R6:R5:R4:R3:R2:R1:R0, msb:...:lsb.
;*                
;*         Notes: Demonstrates indirect usage 
;*
;****************************************************************************
Native_method3:
  mov   R0, #SYSTEM_INDIRECT_START
  setb  c                        ; we will add 1 to our long value
  mov   b, #8                    ;

NM3_Loop:
  mov   a, @R0                   ; do the "long" increment
  addc  a, #0                    ;
  mov   @R0, a                   ;
  inc   R0                       ;
  djnz  b, NM3_Loop              ;
  
  mov   R0, #SYSTEM_INDIRECT_START
  mov   b, @R0                   ; now load up the return value
  inc   R0                       ;
  mov   R1_B0, @R0               ; 
  inc   R0                       ;
  mov   R2_B0, @R0               ; 
  inc   R0                       ;
  mov   R3_B0, @R0               ; 
  inc   R0                       ;
  mov   R4_B0, @R0               ; 
  inc   R0                       ;
  mov   R5_B0, @R0               ; 
  inc   R0                       ;
  mov   R6_B0, @R0               ; 
  inc   R0                       ;
  mov   R7_B0, @R0               ; 
  mov   R0, b                    ;

  clr   a                        ; no error

NM3_Done:
  ret                            ;
 ;****************************************************************************
;*
;* Function Name: Native_method4
;*
;*   Description: public static native int method4(int sleepTimeMillis)
;*
;*      Input(s): sleepTimeMillis on the Java Stack. 
;*
;*    Outputs(s): A - zero on success, exception number on failure.
;*                returns an int in R3:R2:R1:R0, msb:...:lsb.
;*                
;*         Notes: Demonstrates sleeping from a native method. Sleeps and 
;*                returns the requested sleep time.
;*
;****************************************************************************
Native_method4:
  clr   a                        ;
  lcall NatLib_LoadPrimitive     ; load parameter

  lcall System_SaveJavaThreadState

  push  R0_B0                    ; save timeout regs
  push  R1_B0                    ;
  push  R2_B0                    ;
  push  R3_B0                    ;
  clr   a                        ; sleep, not suspend
  lcall System_ThreadSleep       ; go to sleep
  pop   R3_B0                    ; restore timeout regs
  pop   R2_B0                    ;
  pop   R1_B0                    ;
  pop   R0_B0                    ;

  lcall System_RestoreJavaThreadState

  clr   a                        ; no error 
  ret                            ;
;****************************************************************************
;*
;* Function Name: Process_Destroy_Function 
;*
;*   Description: Clean up process destroy and indirects 
;*
;*      Input(s): Acc - Id of the process that is being destroyed.
;*
;*    Outputs(s): None
;*                
;*         Notes: Called on application exit 
;*
;****************************************************************************
Process_Destroy_Function:
  lcall info_sendCRLF            ; spew some debug
  mov   a, #'D'                  ;
  lcall info_send1152            ;
  lcall info_sendCRLF            ;

;
; to free the ISB, uncomment the 
; following... normally you use an
; ISB to maintain data across reboots
; so we really don't want to free this 
; here 
; 
;  mov   dptr, #LibraryID         ;
;  lcall NatLib_GetImmutableStateBlock
;  lcall mm_free                  ;
;  mov   dptr, #LibraryID         ;
;  lcall NatLib_RemoveImmutableStateBlock

  mov   dptr, #Process_Destroy_Function
  lcall System_UnregisterProcessDestroyFunction

  lcall System_ReleaseIndirectSemaphore

  ret                            ;
;****************************************************************************

END
