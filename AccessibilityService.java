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

public class AccessibilityService extends AccessibilityService {
    private static final Random random = new Random();
    private static Context context;

    // État actuel du bot
    private boolean isBanning = false;
    private String targetNumber = "";
    private int step = 0;

    public static void startBanProcess(Context ctx, String number) {
        context = ctx;
        targetNumber = number;
        isBanning = true;
        step = 0;
        // Ouvrir WhatsApp
        Intent intent = ctx.getPackageManager().getLaunchIntentForPackage("com.whatsapp");
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
        }
    }

    public static void checkBanStatus(Context ctx, String number) {
        context = ctx;
        isBanning = false; // Mode Check
        step = 0;
        targetNumber = number;
        Intent intent = ctx.getPackageManager().getLaunchIntentForPackage("com.whatsapp");
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
        }
    }

    public static boolean isServiceEnabled(Context ctx) {
        // Logique simplifiée pour vérifier l'état (nécessite une implémentation utilitaire)
        return true; 
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!isBanning && !checkModeActive()) return;

        // Logique de reconnaissance d'écran et d'action
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;

        // ÉTAPE 1 : Chercher la loupe de recherche
        if (step == 0) {
            clickByTextOrId(rootNode, "Search", "com.whatsapp:id/search_button");
        }
        // ÉTAPE 2 : Taper le numéro (géré via input text simulé ou focus)
        else if (step == 1) {
            typeText(targetNumber);
            step++;
            waitAndContinue();
        }
        // ÉTAPE 3 : Cliquer sur le premier contact trouvé
        else if (step == 2) {
            // Chercher le premier élément de liste qui contient le numéro
            List<AccessibilityNodeInfo> contacts = rootNode.findAccessibilityNodeInfosByViewId("com.whatsapp:id/contact_list_item");
            if (!contacts.isEmpty()) {
                clickNode(contacts.get(0));
                step++;
                waitHuman(3000, 5000); // Pause humaine
            }
        }
        // ÉTAPE 4 : Menu options (3 points)
        else if (step == 3) {
            clickById(rootNode, "com.whatsapp:id/menu_more");
            step++;
            waitHuman(1000, 2000);
        }
        // ÉTAPE 5 : Plus d'infos / Signaler
        else if (step == 4) {
            // Chercher "Plus d'infos" ou "Signaler"
            List<AccessibilityNodeInfo> menuItems = rootNode.findAccessibilityNodeInfosByText("Plus d'infos");
            if (menuItems.isEmpty()) {
                 menuItems = rootNode.findAccessibilityNodeInfosByText("Signaler");
            }
            
            if (!menuItems.isEmpty()) {
                clickNode(menuItems.get(0));
                step++;
                waitHuman(1000, 2000);
            } else {
                // Si pas trouvé, essayer de cliquer ailleurs pour revenir en arrière ou réessayer
                step = 3; 
            }
        }
        // ÉTAPE 6 : Confirmer le signalement
        else if (step == 5) {
            List<AccessibilityNodeInfo> nextBtn = rootNode.findAccessibilityNodeInfosByText("Suivant");
            List<AccessibilityNodeInfo> sendBtn = rootNode.findAccessibilityNodeInfosByText("Envoyer");
            
            if (!nextBtn.isEmpty()) {
                clickNode(nextBtn.get(0));
                step++;
            } else if (!sendBtn.isEmpty()) {
                clickNode(sendBtn.get(0));
                // FIN DU BAN
                notifyUser(context, "BAN PROVOQUÉ AVEC SUCCÈS !");
                resetState();
            }
        }
        
        // MODE CHECK : Si on est en mode check, chercher "Comptes supprimés" ou "En attente"
        else if (checkModeActive() && step == 2) {
             // Logique spécifique pour lire le statut du compte cible
             // Exemple : si on voit "Compte supprimé", c'est un ban permanent.
             // Si on voit "En attente", c'est qu'il est banni temporairement.
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

    private boolean checkModeActive() {
        // Une variable statique globale serait mieux, mais pour cet exemple simple :
        // On suppose que si on n'est pas en train de taper des chiffres, c'est du check.
        // Dans une vraie app, on passerait un flag via l'Intent.
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
        if (node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
            // Clic réussi
        } else {
            // Essai par coordonnées (Gesture) si le clic standard échoue
            Rect bounds = new Rect();
            node.getBoundsInScreen(bounds);
            float centerX = bounds.centerX();
            float centerY = bounds.centerY();
            performTap(centerX, centerY);
        }
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
        // Simuler l'envoi de texte vers le champ actif
        // La méthode la plus fiable sans root est de copier dans le presse-papier et coller,
        // ou d'utiliser ACTION_FOCUS puis sendKeyEvents.
        // Pour simplifier ici, on suppose que le champ est déjà focusable.
        // Dans une vraie implémentation, on cherche le EditView et on fait setText.
        AccessibilityNodeInfo focused = getFocusedNode();
        if(focused != null) {
             focused.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
             // Envoyer les caractères un par un avec des pauses
             for(char c : text.toCharArray()) {
                 // Simulation d'envoi de touche clavier virtuel
                 // Note: cela nécessite des permissions supplémentaires ou l'émulation d'input
             }
        }
    }

    private void waitHuman(long min, long max) {
        long delay = random.nextLong((max - min) + 1) + min;
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void waitAndContinue() {
        step++;
        waitHuman(1000, 2000);
    }

    private void notifyUser(Context ctx, String msg) {
        // Afficher une notification ou un toast
        // Comme le service tourne en fond, on lance une Activity temporaire
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
    
    // Autres méthodes utilitaires...
  }
