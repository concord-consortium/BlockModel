package waba.fx;

/*
Copyright (c) 1998, 1999, 2000 Wabasoft  All rights reserved.

This software is furnished under a license and may be used only in accordance
with the terms of that license. This software and documentation, and its
copyrights are owned by Wabasoft and are protected by copyright law.

THIS SOFTWARE AND REFERENCE MATERIALS ARE PROVIDED "AS IS" WITHOUT WARRANTY
AS TO THEIR PERFORMANCE, MERCHANTABILITY, FITNESS FOR ANY PARTICULAR PURPOSE,
OR AGAINST INFRINGEMENT. WABASOFT ASSUMES NO RESPONSIBILITY FOR THE USE OR
INABILITY TO USE THIS SOFTWARE. WABASOFT SHALL NOT BE LIABLE FOR INDIRECT,
SPECIAL OR CONSEQUENTIAL DAMAGES RESULTING FROM THE USE OF THIS PRODUCT.

WABASOFT SHALL HAVE NO LIABILITY OR RESPONSIBILITY FOR SOFTWARE ALTERED,
MODIFIED, OR CONVERTED BY YOU OR A THIRD PARTY, DAMAGES RESULTING FROM
ACCIDENT, ABUSE OR MISAPPLICATION, OR FOR PROBLEMS DUE TO THE MALFUNCTION OF
YOUR EQUIPMENT OR SOFTWARE NOT SUPPLIED BY WABASOFT.
*/

/**
 * Sound is used to play sounds such as beeps and tones.
 * <p>
 * Playing beeps is supported under all platforms but tones are only supported
 * where the underlying platform supports generating tones. Tones aren't supported
 * under Java or Windows CE.
 * <p>
 * Here is an example that beeps the speaker and plays a tone:
 *
 * <pre>
 * Sound.beep();
 * Sound.tone(4000, 300);
 * </pre>
 */
public class Sound
{
/**
 * Plays a tone of the specified frequency for the specified
 * duration. Tones will only play under Win32 and PalmOS, they won't
 * play under Java or Windows CE due to underlying platform limitations.
 * @param freq frequency in hertz from 32 to 32767
 * @param duration duration in milliseconds
 */

static boolean toneErrDisplayed = false;
/** Plays the device's default beep sound. */

public static void beep()
	{
	java.awt.Toolkit.getDefaultToolkit().beep();
	}
public static void tone(int freq, int duration)
	{
	if (!toneErrDisplayed)
		{
		System.out.println("NOTICE: tones aren't supported under Java");
		toneErrDisplayed = true;
		}
	}
}