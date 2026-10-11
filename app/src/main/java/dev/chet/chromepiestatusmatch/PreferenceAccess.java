package dev.chet.chromepiestatusmatch;
import android.content.Context;
/** Private preferences; Pack snapshot provider handles cross-process reads. */
public final class PreferenceAccess { private PreferenceAccess() {} public static int mode(Context c) { return Context.MODE_PRIVATE; } }
