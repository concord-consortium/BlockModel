;
; Dallas Semiconductor TINI System Equate File
; TINI Version 1.02c
; Generated Fri Sep 14 13:55:35 2001
;
;                                            -- directs --
                                   WOS_CRIT_COUNT EQU 068h
;                                          -- end directs --
;                                          --- indirects ---
                            SYSTEM_INDIRECT_START EQU 0f0h
                              SYSTEM_MAX_INDIRECT EQU 010h
;                                          - end indirects -
                                       DEC_DPTR1  EQU 01ca60H ;
                      SYSTEM_GETCURRENTPROCESSID  EQU 010e2aH ;
                                       MEM_CLEAR  EQU 01c5c3H ;
           SYSTEM_REGISTERPROCESSDESTROYFUNCTION  EQU 011a28H ;
                          SYSTEM_REGISTER_DRIVER  EQU 01a177H ;
                          SYSTEM_SETRTCREGISTERS  EQU 027b0dH ;
                                   MEM_COPY_16_0  EQU 01c6e7H ;
                                    SUB_DPTR1_16  EQU 01c9f0H ;
              SYSTEM_UNREGISTEREXTERNALINTERRUPT  EQU 033316H ;
                            SYSTEM_PROCESSRESUME  EQU 011352H ;
                                MEM_COPY_16_ND_0  EQU 01c8e8H ;
                              NATLIB_LOADPOINTER  EQU 03bad0H ;
                                     MEM_COPY_16  EQU 01c6dcH ;
                          DEBUG_DUMPDATAPOINTER1  EQU 02fbf3H ;
                          DEBUG_DUMPDATAPOINTER2  EQU 02fc37H ;
                 SYSTEM_RELEASEINDIRECTSEMAPHORE  EQU 03bb4aH ;
                                       MM_MALLOC  EQU 01b122H ;
                        NATLIB_LOADJAVABYTEARRAY  EQU 03bb22H ;
                        SYSTEM_UNREGISTER_DRIVER  EQU 01a133H ;
                          SYSTEM_EXECJAVAPROCESS  EQU 01c517H ;
                           SYSTEM_PROCESSSUSPEND  EQU 01130fH ;
                            MM_APPLICATIONMALLOC  EQU 01b11cH ;
                                    SUB_DPTR2_16  EQU 01ca44H ;
                           SYSTEM_WRITEFLASHBYTE  EQU 027273H ;
               NATLIB_INSTALLEPHEMERALSTATEBLOCK  EQU 03b83dH ;
                               MEM_COMPARE_XDATA  EQU 01c904H ;
                                       SUB_DPTR1  EQU 01ca0cH ;
                                       SUB_DPTR2  EQU 01ca28H ;
                             SYSTEM_REGISTERPOLL  EQU 01ade0H ;
                                 INFO_SENDTWOHEX  EQU 017c1eH ;
                                    ADD_DPTR1_16  EQU 01c990H ;
                                    MEM_CLEAR_16  EQU 01c5eaH ;
                                    ADD_DPTR1_24  EQU 01c9c0H ;
                              SYSTEM_THREADSLEEP  EQU 031cd1H ;
                NATLIB_REMOVEEPHEMERALSTATEBLOCK  EQU 03b8f8H ;
               NATLIB_INSTALLIMMUTABLESTATEBLOCK  EQU 03b614H ;
                                         MM_FREE  EQU 01b32fH ;
                             SYSTEM_PROCESSYIELD  EQU 010f9cH ;
                      SYSTEM_SAVEJAVATHREADSTATE  EQU 031381H ;
             SYSTEM_GETDESTROYORCURRENTPROCESSID  EQU 010e2dH ;
                                           CRC16  EQU 01ca81H ;
                         DEBUG_DUMPREGISTERBANK0  EQU 02fd38H ;
                         DEBUG_DUMPREGISTERBANK1  EQU 02fda9H ;
                         DEBUG_DUMPREGISTERBANK2  EQU 02fe1aH ;
                         DEBUG_DUMPREGISTERBANK3  EQU 02fe8bH ;
                                    ADD_DPTR2_16  EQU 01c9a8H ;
                                COMPARE_DPTR1_24  EQU 01ca69H ;
                                    ADD_DPTR2_24  EQU 01c9d8H ;
                                 INFO_SENDSTRING  EQU 017c04H ;
                          SYSTEM_GETUPTIMEMILLIS  EQU 010e34H ;
                                    DEBUG_DUMPBA  EQU 02fc9dH ;
         SYSTEM_UNREGISTERPROCESSDESTROYFUNCTION  EQU 011a83H ;
                                        MEM_COPY  EQU 01c691H ;
                                   INFO_SENDCRLF  EQU 017c2eH ;
                             SYSTEM_PROCESSSLEEP  EQU 010fd5H ;
          SYSTEM_THREADSLEEP_EXITCRITICALSECTION  EQU 031cd3H ;
                                     MEM_CLEAR_0  EQU 01c5d5H ;
                   NATLIB_GETIMMUTABLESTATEBLOCK  EQU 03b736H ;
       SYSTEM_PROCESSSUSPEND_EXITCRITICALSECTION  EQU 011311H ;
                                  MEM_CLEAR_16_0  EQU 01c5fcH ;
                                      MM_XMALLOC  EQU 01b110H ;
         SYSTEM_PROCESSSLEEP_EXITCRITICALSECTION  EQU 010fd7H ;
                                   INFO_SEND1152  EQU 017c9fH ;
                                  MEM_COPY_16_ND  EQU 01c8d6H ;
                                        MM_DEREF  EQU 01b48dH ;
                          MM_UNRESTRICTEDPERSIST  EQU 01b5ffH ;
                 SYSTEM_ACQUIREINDIRECTSEMAPHORE  EQU 03bb32H ;
                         SYSTEM_INSTALLINTERRUPT  EQU 01af70H ;
                            NATLIB_LOADPRIMITIVE  EQU 03ba66H ;
                   NATLIB_GETEPHEMERALSTATEBLOCK  EQU 03b95fH ;
                            NATLIB_LOADJAVAARRAY  EQU 03bb22H ;
                       SYSTEM_GETCURRENTTHREADID  EQU 030771H ;
                             MEM_COMPARE_XDATA_0  EQU 01c916H ;
                NATLIB_REMOVEIMMUTABLESTATEBLOCK  EQU 03b6cfH ;
                   SYSTEM_RESTOREJAVATHREADSTATE  EQU 0313c9H ;
                          SYSTEM_GETRTCREGISTERS  EQU 027a90H ;
                                      MEM_COPY_0  EQU 01c69cH ;
                                       ADD_DPTR1  EQU 01c962H ;
                                       ADD_DPTR2  EQU 01c979H ;
                SYSTEM_REGISTEREXTERNALINTERRUPT  EQU 033416H ;
                                       MM_APPTAG  EQU 01b507H ;
                        NATLIB_LOADWIDEPRIMITIVE  EQU 03ba95H ;
                               SYSTEM_REMOVEPOLL  EQU 01ae1fH ;
                                        WOS_INIT  EQU 0105d7H ;
                             SYSTEM_THREADRESUME  EQU 030f1cH ;
