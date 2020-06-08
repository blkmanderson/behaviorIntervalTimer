package com.example.intervaltimer;

import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.accessory.SA;
import com.samsung.android.sdk.accessory.SAAgent;
import com.samsung.android.sdk.accessory.SAAgentV2;
import com.samsung.android.sdk.accessory.SAAuthenticationToken;
import com.samsung.android.sdk.accessory.SAMessage;
import com.samsung.android.sdk.accessory.SAPeerAgent;
import com.samsung.android.sdk.accessory.SASocket;
import com.samsung.android.sdk.accessoryfiletransfer.SAFileTransfer;
import com.samsung.android.sdk.accessoryfiletransfer.SAft;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class ProviderService extends SAAgentV2 {
    private static final String TAG = "HelloMessage(P)";
    private SAMessage mMessage = null;
    private Toast mToast;
    private Context mContext;
    private SAPeerAgent[] mPeer;

    public ProviderService(Context context) {
        super(TAG, context);
        mContext = context;
        SA mAccessory = new SA();
        try {
            mAccessory.initialize(mContext);
        } catch (SsdkUnsupportedException e) {
            // try to handle SsdkUnsupportedException
            if (processUnsupportedException(e) == true) {
                return;
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            /*
             * Your application can not use Samsung Accessory SDK. Your application should work smoothly
             * without using this SDK, or you may want to notify user and close your application gracefully
             * (release resources, stop Service threads, close UI thread, etc.)
             */
        }

        mMessage = new SAMessage(this) {

            @Override
            protected void onSent(SAPeerAgent peerAgent, int id) {
                Log.d(TAG, "onSent(), id: " + id + ", ToAgent: " + peerAgent.getPeerId());
            }

            @Override
            protected void onError(SAPeerAgent peerAgent, int id, int errorCode) {
                Log.d(TAG, "onError(), id: " + id + ", ToAgent: " + peerAgent.getPeerId() + ", errorCode: " + errorCode);
            }

            @Override
            protected void onReceive(final SAPeerAgent peerAgent, final byte[] message) {
                Log.d(TAG, "onReceive(), FromAgent : " + peerAgent.getPeerId() + " Message : " + new String(message));
            }
        };

        findPeerAgents();
    }

    @Override
    protected void onFindPeerAgentsResponse(final SAPeerAgent[] peerAgents, int result) {
        Log.d(TAG, "onFindPeerAgentResponse : result =" + result);
        mPeer = peerAgents;
        if(mPeer != null) {
            if (mPeer.length != 0) {
                send("Ready");
                displayToast("Watch Ready", Toast.LENGTH_SHORT);
            }
        }
    }

    @Override
    protected void onAuthenticationResponse(SAPeerAgent peerAgent, SAAuthenticationToken authToken, int error) {
        /*
         * The authenticatePeerAgent(peerAgent) API may not be working properly depending on the firmware
         * version of accessory device. Please refer to another sample application for Security.
         */
    }

    @Override
    protected void onError(SAPeerAgent peerAgent, String errorMessage, int errorCode) {
        super.onError(peerAgent, errorMessage, errorCode);
    }

    public void send(final String message) {

        if(mPeer != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mPeer.length > 0) {
                        for (SAPeerAgent agent : mPeer) {
                            int result = sendData(agent, message);
                        }
                    }
                }
            }).run();
        }
    }

    private int sendData(SAPeerAgent peerAgent, String message) {
        int tid;
        if (mMessage != null) {
            try {
                tid = mMessage.send(peerAgent, message.getBytes());
                return tid;
            } catch (IOException e) {
                e.printStackTrace();
                displayToast(e.getMessage(), Toast.LENGTH_SHORT);
                return -1;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                displayToast(e.getMessage(), Toast.LENGTH_SHORT);
                return -1;
            }
        }
        return -1;
    }

    private boolean processUnsupportedException(SsdkUnsupportedException e) {
        e.printStackTrace();
        int errType = e.getType();
        if (errType == SsdkUnsupportedException.VENDOR_NOT_SUPPORTED
                || errType == SsdkUnsupportedException.DEVICE_NOT_SUPPORTED) {
            /*
             * Your application can not use Samsung Accessory SDK. You application should work smoothly
             * without using this SDK, or you may want to notify user and close your app gracefully (release
             * resources, stop Service threads, close UI thread, etc.)
             */
        } else if (errType == SsdkUnsupportedException.LIBRARY_NOT_INSTALLED) {
            Log.e(TAG, "You need to install Samsung Accessory SDK to use this application.");
        } else if (errType == SsdkUnsupportedException.LIBRARY_UPDATE_IS_REQUIRED) {
            Log.e(TAG, "You need to update Samsung Accessory SDK to use this application.");
        } else if (errType == SsdkUnsupportedException.LIBRARY_UPDATE_IS_RECOMMENDED) {
            Log.e(TAG, "We recommend that you update your Samsung Accessory SDK before using this application.");
            return false;
        }
        return true;
    }

    private void displayToast(String str, int duration) {
        if(mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getApplicationContext(), str, duration);
        mToast.show();
    }
}