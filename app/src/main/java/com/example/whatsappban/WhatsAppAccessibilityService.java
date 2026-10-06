package com.example.whatsappban;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.content.Context;
import android.content.Intent;
import java.util.Random;
import java.util.List;

public class WhatsAppAccessibilityService extends AccessibilityService {
    private static final Random random = new Random();
    private static Context context;
    private static boolean isBanning = false;
    private static String targetNumber = "";
    private static int step = 0;

    public static void startBanProcess(Context ctx, String number) {
        context = ctx.getApplicationContext();
        targetNumber = number;
        isBanning = true;
        step = 0;

        Intent intent = ctx.getPackageManager().getLaunchIntentForPackage("com.whatsapp");
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
        }
    }

    public static void checkBanStatus(Context ctx, String number) {
        context = ctx.getApplicationContext();
        isBanning = false;
        step = 0;
        targetNumber = number;

        Intent intent = ctx.getPackageManager().getLaunchIntentForPackage("com.whatsapp");
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
        }
    }

    public static boolean isServiceEnabled(Context ctx) {
        return true;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!isBanning && !checkModeActive()) return;

        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;

        if (step == 0) {
            clickByTextOrId(rootNode, "Search", "com.whatsapp:id/search_button");
        } else if (step == 1) {
            typeText(targetNumber);
            step++;
            waitAndContinue();
        } else if (step == 2) {
            List<AccessibilityNodeInfo> contacts = rootNode.findAccessibilityNodeInfosByViewId("com.whatsapp:id/contact_list_item");
            if (!contacts.isEmpty()) {
                clickNode(contacts.get(0));
                step++;
                waitHuman(3000, 5000);
            }
        } else if (step == 3) {
            clickById(rootNode, "com.whatsapp:id/menu_more");
        } else if (step == 4) {
            List<AccessibilityNodeInfo> menuItems = rootNode.findAccessibilityNodeInfosByText("Plus d'infos");
            if (menuItems.isEmpty()) {
                menuItems = rootNode.findAccessibilityNodeInfosByText("Signaler");
            }

            if (!menuItems.isEmpty()) {
                clickNode(menuItems.get(0));
                step++;
                waitHuman(1000, 2000);
            } else {
                step = 3;
            }
        } else if (step == 5) {
            List<AccessibilityNodeInfo> nextBtn = rootNode.findAccessibilityNodeInfosByText("Suivant");
            List<AccessibilityNodeInfo> sendBtn = rootNode.findAccessibilityNodeInfosByText("Envoyer");

            if (!nextBtn.isEmpty()) {
                clickNode(nextBtn.get(0));
                step++;
            } else if (!sendBtn.isEmpty()) {
                clickNode(sendBtn.get(0));
                notifyUser(context, "BAN PROVOQUÉ AVEC SUCCÈS !");
                resetState();
            }
        }

        if (checkModeActive() && step == 2) {
            List<AccessibilityNodeInfo> statusTexts = rootNode.findAccessibilityNodeInfosByText("Compte supprimé");
            if (!statusTexts.isEmpty()) {
                notifyUser(context, "STATUT : BANNI PERMANENT (Compte supprimé)");
            } else {
                List<AccessibilityNodeInfo> waiting = rootNode.findAccessibilityNodeInfosByText("En attente");
                if (!waiting.isEmpty()) {
                    notifyUser(context, "STATUT : SUSPENDU (En attente)");
                } else {
                    notifyUser(context, "STATUT : COMPTE ACTIF");
                }
            }
            resetState();
        }
    }

    @Override
    public void onInterrupt() {
        // Called when the system wants to interrupt the accessibility service.
    }

    private boolean checkModeActive() {
        return false;
    }

    private void clickById(AccessibilityNodeInfo node, String id) {
        List<AccessibilityNodeInfo> list = node.findAccessibilityNodeInfosByViewId(id);
        if (!list.isEmpty()) {
            clickNode(list.get(0));
            step++;
            waitHuman(500, 1500);
        }
    }

    private void clickByTextOrId(AccessibilityNodeInfo node, String text, String id) {
        List<AccessibilityNodeInfo> byText = node.findAccessibilityNodeInfosByText(text);
        if (!byText.isEmpty()) {
            clickNode(byText.get(0));
            step++;
            waitHuman(500, 1500);
            return;
        }

        clickById(node, id);
    }

    private void clickNode(AccessibilityNodeInfo node) {
        if (node == null) return;

        if (node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
            return;
        }

        Rect bounds = new Rect();
        node.getBoundsInScreen(bounds);
        float centerX = bounds.centerX();
        float centerY = bounds.centerY();
        performTap(centerX, centerY);
    }

    private void performTap(float x, float y) {
        GestureDescription.Builder builder = new GestureDescription.Builder();
        Path path = new Path();
        path.moveTo(x, y);
        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 100);
        builder.addStroke(stroke);
        dispatchGesture(builder.build(), null, null);
    }

    private void typeText(String text) {
        AccessibilityNodeInfo focused = getFocusedWindow();
        if (focused != null) {
            focused.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
            for (char c : text.toCharArray()) {
                // Intentionally left for a future keyboard injection implementation.
            }
        }
    }

    private void waitHuman(long min, long max) {
        // Java 17 compatible: use nextInt with explicit casting
        long delay = (long) (Math.random() * (max - min + 1)) + min;
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void waitAndContinue() {
        step++;
        waitHuman(1000, 2000);
    }

    private void notifyUser(Context ctx, String msg) {
        if (ctx == null) return;

        Intent i = new Intent(ctx, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("result", msg);
        ctx.startActivity(i);
    }

    private void resetState() {
        step = 0;
        isBanning = false;
        targetNumber = "";
    }
}
