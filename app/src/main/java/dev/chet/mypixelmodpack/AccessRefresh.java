package dev.chet.mypixelmodpack;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.atomic.AtomicBoolean;

/** Same grants as 2.0.2, shared by category screens with one worker at a time. */
public final class AccessRefresh {
    private static final AtomicBoolean busy=new AtomicBoolean();
    private AccessRefresh() {}
    public static boolean isRunning() { return busy.get(); }
    public static boolean start(Context context,Runnable finished) {
        if(!busy.compareAndSet(false,true)) return false;
        Context app=context.getApplicationContext();
        new Thread(() -> {
            try {
                String result;
                try { result=ProviderVisibility.refresh(app).summary(); }
                catch(RuntimeException error) { result="App access refresh failed: "+error; }
                app.getSharedPreferences("access_diagnostics_v2",0).edit()
                    .putString("result",System.currentTimeMillis()+" | "+result).commit();
            } finally {
                busy.set(false);
                if(finished!=null) new Handler(Looper.getMainLooper()).post(finished);
            }
        },"PackAppAccess").start();
        return true;
    }
}
