import com.dalsemi.system.BitPort;

class Blinky {
   public static void main(String[] args) {
      BitPort bp = new BitPort(BitPort.Port3Bit5);
      for (;;) {
         // Turn on LED
         bp.clear();
         // Leave it on for 1/4 second
         try {
            Thread.sleep(250);
         } catch (InterruptedException ie) {}
         // Turn off LED
         bp.set();
         // Leave it off for 1/4 second
         try {
            Thread.sleep(250);
         } catch (InterruptedException ie) {}
      }
   }
}
