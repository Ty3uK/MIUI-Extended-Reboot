package tk.ty3uk.miuiextendedreboot;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import android.app.AndroidAppHelper;
import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.os.Build;
import android.os.PowerManager;

public class Main implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("android")) {
            final Class<?> MiuiGlobalActions;
            final Class<?> MiuiGlobalActions$1;
            final Class<?> MiuiGlobalActions$1$1;

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) { //Marshmallow
                MiuiGlobalActions = XposedHelpers.findClass("com.android.server.policy.MiuiGlobalActions", lpparam.classLoader);
                MiuiGlobalActions$1 = XposedHelpers.findClass("com.android.server.policy.MiuiGlobalActions$1", lpparam.classLoader);
                MiuiGlobalActions$1$1 = XposedHelpers.findClass("com.android.server.policy.MiuiGlobalActions$1$1", lpparam.classLoader);
            } else { //KitKat and Lollipop
                MiuiGlobalActions = XposedHelpers.findClass("com.android.internal.policy.impl.MiuiGlobalActions", lpparam.classLoader);
                MiuiGlobalActions$1 = XposedHelpers.findClass("com.android.internal.policy.impl.MiuiGlobalActions$1", lpparam.classLoader);
                MiuiGlobalActions$1$1 = XposedHelpers.findClass("com.android.internal.policy.impl.MiuiGlobalActions$1$1", lpparam.classLoader);
            }

            final Class<?> WindowManagerFuncs = XposedHelpers.findClass("android.view.WindowManagerPolicy$WindowManagerFuncs", lpparam.classLoader);
            final Class<?> ResourceManager = XposedHelpers.findClass("miui.maml.ResourceManager", lpparam.classLoader);
            final Class<?> ZipResourceLoader = XposedHelpers.findClass("miui.maml.util.ZipResourceLoader", lpparam.classLoader);
            final Class<?> ScreenContext = XposedHelpers.findClass("miui.maml.ScreenContext", lpparam.classLoader);
            final Class<?> ScreenElementRoot = XposedHelpers.findClass("miui.maml.ScreenElementRoot", lpparam.classLoader);

            XposedHelpers.findAndHookConstructor(MiuiGlobalActions, Context.class, WindowManagerFuncs, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Context contextImpl = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
//
                    File powermenu = new File("/cache/powermenu_patched");
                    InputStream inputStream;
                    FileOutputStream outputStream;
                    byte[] fileBytes;

                    if (!powermenu.exists()) {
                        Context context = contextImpl.createPackageContext("tk.ty3uk.miuiextendedreboot", Context.CONTEXT_IGNORE_SECURITY);
                        inputStream = context.getResources().openRawResource(
                                context.getResources().getIdentifier("powermenu_patched", "raw", "tk.ty3uk.miuiextendedreboot")
                        );
                        fileBytes = new byte[inputStream.available()];
                        inputStream.read(fileBytes);
                        outputStream = new FileOutputStream(powermenu);
                        outputStream.write(fileBytes);
                        outputStream.close();
                        inputStream.close();
                    }

                    Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                    XposedHelpers.setObjectField(param.thisObject, "mResourceManager", XposedHelpers.newInstance(
                                    ResourceManager,
                                    XposedHelpers.newInstance(ZipResourceLoader, powermenu.getPath()))
                    );
                    Object mResourceManager = XposedHelpers.getObjectField(param.thisObject, "mResourceManager");
                    XposedHelpers.setObjectField(param.thisObject, "mScreenElementRoot", XposedHelpers.newInstance(
                                    ScreenElementRoot,
                                    XposedHelpers.newInstance(ScreenContext, mContext, mResourceManager))
                    );
                    Object mScreenElementRoot = XposedHelpers.getObjectField(param.thisObject, "mScreenElementRoot");
                    XposedHelpers.callMethod(mScreenElementRoot, "setOnExternCommandListener", XposedHelpers.getObjectField(param.thisObject, "mCommandListener"));
                    XposedHelpers.callMethod(mScreenElementRoot, "setKeepResource", true);
                    XposedHelpers.callMethod(mScreenElementRoot, "load");
                    XposedHelpers.callMethod(mScreenElementRoot, "init");
                }
            });

            XposedHelpers.findAndHookMethod(MiuiGlobalActions$1, "onCommand", String.class, Double.class, String.class, new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    Object this$0 = XposedHelpers.getObjectField(param.thisObject, "this$0");
                    String paramString1 = (String) param.args[0];

                    if ("airplane".equals(paramString1)) {
                        XposedHelpers.callStaticMethod(MiuiGlobalActions, "access$000", this$0, 9);
                        return null;
                    }

                    do {
                        if ("silent".equals(paramString1)) {
                            XposedHelpers.callStaticMethod(MiuiGlobalActions, "access$000", this$0, 5);
                            return null;
                        }

                        if ("reboot".equals(paramString1)) {
                            PowerManager pm = (PowerManager) AndroidAppHelper.currentApplication().getSystemService(Context.POWER_SERVICE);
                            pm.reboot(null);
                            return null;
                        }

                        if ("recovery".equals(paramString1)) {
                            PowerManager pm = (PowerManager) AndroidAppHelper.currentApplication().getSystemService(Context.POWER_SERVICE);
                            pm.reboot("recovery");
                            return null;
                        }

                        if ("bootloader".equals(paramString1)) {
                            PowerManager pm = (PowerManager) AndroidAppHelper.currentApplication().getSystemService(Context.POWER_SERVICE);
                            pm.reboot("bootloader");
                            return null;
                        }

                        if ("shutdown".equals(paramString1)) {
                            Object instanse = XposedHelpers.newInstance(MiuiGlobalActions$1$1, param.thisObject, "ShutdownThread");
                            XposedHelpers.callMethod(instanse, "start");
                            return null;
                        }
                    } while (!"dismiss".equals(paramString1));

                    Object mHandler = XposedHelpers.callStaticMethod(MiuiGlobalActions, "access$200", this$0);
                    XposedHelpers.callMethod(mHandler, "sendEmptyMessage", 0);

                    return null;
                }
            });
        }
    }
}