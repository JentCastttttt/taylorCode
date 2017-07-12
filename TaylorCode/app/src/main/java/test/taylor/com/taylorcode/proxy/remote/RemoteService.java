package test.taylor.com.taylorcode.proxy.remote;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import test.taylor.com.taylorcode.IRemoteService;


/**
 * represents Service in another process,which is different from the default process of application
 * Created on 17/5/2.
 */

public class RemoteService extends Service {


    /**
     * [story IPC]2.server implement aidl interface
     */
    private IRemoteService.Stub binder = new IRemoteService.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {
            //do nothing
        }

        @Override
        public void sail() throws RemoteException {
            Log.v("taylor servicePid", "RemoteService.sail() " + " pid=" + android.os.Process.myPid());
        }

        @Override
        public boolean isEngineOk() throws RemoteException {
            boolean isEngineOk = false;
            Log.v("taylor tamperService", "RemoteService.isEngineOk() " + " isEngineOk = " + isEngineOk);
            return isEngineOk;
        }
    };

    /**
     * [story IPC]3.server wrap implemented aidl interface with IBinder and return it to client
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("taylor servicePid", "RemoteService.onCreate() " + " pid=" + android.os.Process.myPid());
    }
}
