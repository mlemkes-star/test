package com.example.cordova.progloveintent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Listens for the ProGlove "DISPLAY_BUTTON" broadcast intent and forwards
 * the button id + gesture (e.g. DOUBLE_CLICK, TRIPLE_CLICK) to JavaScript.
 *
 * Docs: https://docs.proglove.com/en/receive-button-events.html
 */
public class ProGloveIntentPlugin extends CordovaPlugin {

    private static final String ACTION_DISPLAY_BUTTON = "com.proglove.api.DISPLAY_BUTTON";
    private static final String EXTRA_BUTTON_ID = "com.proglove.api.extra.DISPLAY_BUTTON";
    private static final String EXTRA_BUTTON_GESTURE = "com.proglove.api.extra.BUTTON_GESTURE";

    // JS-side action name this plugin responds to
    private static final String JS_ACTION_REGISTER = "register";

    private CallbackContext listenerCallback;
    private BroadcastReceiver receiver;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null || !ACTION_DISPLAY_BUTTON.equals(intent.getAction())) {
                    return;
                }
                if (listenerCallback == null) {
                    // No JS listener registered (yet) - nothing to forward to.
                    return;
                }

                try {
                    JSONObject data = new JSONObject();
                    data.put("buttonId", intent.getStringExtra(EXTRA_BUTTON_ID));
                    data.put("gesture", intent.getStringExtra(EXTRA_BUTTON_GESTURE));

                    PluginResult result = new PluginResult(PluginResult.Status.OK, data);
                    // Keep the callback alive so JS keeps receiving future events
                    result.setKeepCallback(true);
                    listenerCallback.sendPluginResult(result);
                } catch (JSONException e) {
                    listenerCallback.error("Failed to parse ProGlove intent: " + e.getMessage());
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_DISPLAY_BUTTON);

        // Registered as soon as the WebView/plugin is initialized, i.e. app start.
        cordova.getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (JS_ACTION_REGISTER.equals(action)) {
            this.listenerCallback = callbackContext;

            // No immediate result - this callback stays open and fires
            // every time a DISPLAY_BUTTON intent comes in.
            PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
            return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            try {
                cordova.getActivity().unregisterReceiver(receiver);
            } catch (IllegalArgumentException ignored) {
                // receiver was already unregistered - safe to ignore
            }
        }
    }
}
