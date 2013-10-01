;****************************************************************************
;*
;*  Copyright (C) 1996 - 2000 Dallas Semiconductor Corporation. 
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
;*     Module Name: DS80c390 
;*
;*     Description:  
;*
;*        Filename: ds80c390.inc
;*
;* Dependant Files: none
;*
;*          Author: All 
;*
;*        Compiler: A390.EXE
;*
;*         Version: 0.01 
;*
;*         Created: 11/11/97
;*
;*   Modifications: 
;*
;****************************************************************************
;-------------------------------------------------------------------------------
; DALLAS Processor Declarations
; TO USE THIS INCLUDE FILE YOU MUST DECLARE $NOMOD51 IN THE SOURCE FILE
; WHERE THIS IS INCLUDED FROM
;-------------------------------------------------------------------------------
$NOMOD51

; New registers in the 83C390
AP      EQU     09CH
ESP     EQU     09BH
DPX     EQU     093H
DPX1    EQU     095H
ACON    EQU     09DH
MCON    EQU     0C6H
P4CNT   EQU     092H
P5CNT   EQU     0A2H
P5      EQU     0A1H
MXAX    EQU     0EAH
COR     EQU     0CEH            ; Clock output Register

; Registers on the 80C520
F1      EQU     PSW.1           ; Don't know why this one wasn't defined but F0 was.

; CAN Controller Registers
C0RMS0  EQU     096H            ; CAN 0 Receive Message Stored Register Zero (1-8)
C0RMS1  EQU     097H            ; CAN 0 Receive Message Stored Register One (9-15)
C0TMA0  EQU     09EH            ; CAN 0 Transmit Message Acknowledgement Register Zero (1-8)
C0TMA1  EQU     09FH            ; CAN 0 Transmit Message Acknowledgement Register One (9-15)
C0C     EQU     0A3H            ; CAN 0 Control Register
C0S     EQU     0A4H            ; CAN 0 Status Register
C0IR    EQU     0A5H            ; CAN 0 Interrupt Register
C0TE    EQU     0A6H            ; CAN 0 Transmit Error Register
C0RE    EQU     0A7H            ; CAN 0 Receive Error Register
C0M1C   EQU     0ABH            ; CAN 0 Message One Control Register
C0M2C   EQU     0ACH            ; CAN 0 Message Two Control Register
C0M3C   EQU     0ADH            ; CAN 0 Message Three Control Register
C0M4C   EQU     0AEH            ; CAN 0 Message Four Control Register
C0M5C   EQU     0AFH            ; CAN 0 Message Five Control Register
C0M6C   EQU     0B3H            ; CAN 0 Message Six Control Register
C0M7C   EQU     0B4H            ; CAN 0 Message Seven Control Register
C0M8C   EQU     0B5H            ; CAN 0 Message Eight Control Register
C0M9C   EQU     0B6H            ; CAN 0 Message Nine Control Register
C0M10C  EQU     0B7H            ; CAN 0 Message Ten Control Register
C0M11C  EQU     0BBH            ; CAN 0 Message Eleven Control Register
C0M12C  EQU     0BCH            ; CAN 0 Message Twelve Control Register
C0M13C  EQU     0BDH            ; CAN 0 Message Thirteen Control Register
C0M14C  EQU     0BEH            ; CAN 0 Message Fourteen Control Register
C0M15C  EQU     0BFH            ; CAN 0 Message Fifteen Control Register

C1RMS0  EQU     0D6H            ; CAN 1 Receive Message Stored Register Zero (1-8)
C1RMS1  EQU     0D7H            ; CAN 1 Receive Message Stored Register One (9-15)
C1TMA0  EQU     0DEH            ; CAN 1 Transmit Message Acknowledgement Register Zero (1-8)
C1TMA1  EQU     0DFH            ; CAN 1 Transmit Message Acknowledgement Register One (9-15)
C1C     EQU     0E3H            ; CAN 1 Control Register
C1S     EQU     0E4H            ; CAN 1 Status Register
C1IR    EQU     0E5H            ; CAN 1 Interrupt Register
C1TE    EQU     0E6H            ; CAN 1 Transmit Error Register
C1RE    EQU     0E7H            ; CAN 1 Receive Error Register
C1M1C   EQU     0EBH            ; CAN 1 Message One Control Register
C1M2C   EQU     0ECH            ; CAN 1 Message Two Control Register
C1M3C   EQU     0EDH            ; CAN 1 Message Three Control Register
C1M4C   EQU     0EEH            ; CAN 1 Message Four Control Register
C1M5C   EQU     0EFH            ; CAN 1 Message Five Control Register
C1M6C   EQU     0F3H            ; CAN 1 Message Six Control Register
C1M7C   EQU     0F4H            ; CAN 1 Message Seven Control Register
C1M8C   EQU     0F5H            ; CAN 1 Message Eight Control Register
C1M9C   EQU     0F6H            ; CAN 1 Message Nine Control Register
C1M10C  EQU     0F7H            ; CAN 1 Message Ten Control Register
C1M11C  EQU     0FBH            ; CAN 1 Message Eleven Control Register
C1M12C  EQU     0FCH            ; CAN 1 Message Twelve Control Register
C1M13C  EQU     0FDH            ; CAN 1 Message Thirteen Control Register
C1M14C  EQU     0FEH            ; CAN 1 Message Fourteen Control Register
C1M15C  EQU     0FFH            ; CAN 1 Message Fifteen Control Register

C1IE    EQU     EIE.5           ; CAN 1 Interrupt Enable
C0IE    EQU     EIE.6           ; CAN 0 Interrupt Enable
CANBIE  EQU     EIE.7           ; CAN Bus Activity Interrupt Enable

;  BYTE Registers
SBUF0   EQU    099H

;  DS80C320 Extensions
DPL1    EQU    084H
DPH1    EQU    085H
DPS     EQU    086H
CKCON   EQU    08EH
EXIF    EQU    091H
SADDR0  EQU    0A9H
SADDR1  EQU    0AAH
SADEN0  EQU    0B9H
SADEN1  EQU    0BAH
SBUF1   EQU    0C1H
TA      EQU    0C7H
T2MOD   EQU    0C9H
RCAP2L  EQU    0CAH
RCAP2H  EQU    0CBH
TL2     EQU    0CCH
TH2     EQU    0CDH
EIP     EQU    0F8H
ROMSIZE EQU    0C2H
PMR	EQU    0C4H
STATUS  EQU    0C5h


; Real Time Clock Registers
RTASS	EQU    0F2H 
RTAS	EQU    0F3H
RTAM    EQU    0F4H
RTAH    EQU    0F5H
RTCC    EQU    0F9H
RTCSS   EQU    0FAH
RTCS    EQU    0FBH
RTCM    EQU    0FCH
RTCH    EQU    0FDH
RTCD0   EQU    0FEH
RTCD1   EQU    0FFH

; Register Banks
R0_B0     equ 0                         ; Register bank 0 equates.
R1_B0     equ 1
R2_B0     equ 2
R3_B0     equ 3
R4_B0     equ 4
R5_B0     equ 5
R6_B0     equ 6
R7_B0     equ 7
;
R0_B1     equ 8                         ; Register bank 1 equates.
R1_B1     equ 9
R2_B1     equ 0AH 
R3_B1     equ 0BH 
R4_B1     equ 0CH 
R5_B1     equ 0DH 
R6_B1     equ 0EH 
R7_B1     equ 0FH 
;
R0_B2     equ 10H                       ; Register bank 2 equates.
R1_B2     equ 11H 
R2_B2     equ 12H 
R3_B2     equ 13H
R4_B2     equ 14H
R5_B2     equ 15H
R6_B2     equ 16H
R7_B2     equ 17H
;
R0_B3     equ 18H                       ; Register bank 3 equates.
R1_B3     equ 19H
R2_B3     equ 1AH
R3_B3     equ 1BH
R4_B3     equ 1CH
R5_B3     equ 1DH
R6_B3     equ 1EH
R7_B3     equ 1FH

;  EQU Registers

; *** IE ***
ES1     EQU     0AEH
ET2     EQU     0ADH
ES0     EQU     0ACH

; *** IP ***
PS1     EQU     0BEH
PT2     EQU     0BDH
PS0     EQU     0BCH

; *** P1 ***
INT5    EQU     097H
INT4    EQU     096H
INT3    EQU     095H
INT2    EQU     094H
TXD1    EQU     093H
RXD1    EQU     092H
T2EX    EQU     091H
T2      EQU     090H

; *** P3 ***
TXD0    EQU     0B1H
RXD0    EQU     0B0H

; *** SCON1 ***
SM01    EQU     0C7H
SM11    EQU     0C6H
SM21    EQU     0C5H
REN1    EQU     0C4H
TB81    EQU     0C3H
RB81    EQU     0C2H
TI1     EQU     0C1H
RI1     EQU     0C0H

; *** T2CON ***
TF2     EQU     0CFH
EXF2    EQU     0CEH
RCLK    EQU     0CDH
TCLK    EQU     0CCH
EXEN2   EQU     0CBH
TR2     EQU     0CAH
C_T2    EQU     0C9H
CP_RL2  EQU     0C8H

; *** WDCON ***
SMOD    EQU     0DFH
POR     EQU     0DEH
EPFI    EQU     0DDH
PFI     EQU     0DCH
WDIF    EQU     0DBH
WDRF    EQU     0DAH
EWT     EQU     0D9H
RWT     EQU     0D8H

; *** EIE ***
EWDI    EQU     0ECH
EX5     EQU     0EBH
EX4     EQU     0EAH
EX3     EQU     0E9H
EX2     EQU     0E8H

; *** EIP ***
PWDI    EQU     0FCH
PX5     EQU     0FBH
PX4     EQU     0FAH
PX3     EQU     0F9H
PX2     EQU     0F8H

; **** Math Accelerator ****
MCNT0   EQU     0D1H
MCNT1   EQU     0D2H
MA      EQU     0D3H
MB      EQU     0D4H
MC      EQU     0D5H

