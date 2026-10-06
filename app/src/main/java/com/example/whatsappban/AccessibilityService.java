package com.example.whatsappban;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

/**
 * Legacy compatibility class kept only to avoid stale source conflicts.
 * The real service implementation lives in WhatsAppAccessibilityService.java.
 */
class AccessibilityServiceCompat extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // no-op for compatibility build; actual logic is handled by WhatsAppAccessibilityService
    }

    @Override
    public void onInterrupt() {
        // no-op for compatibility build; actual logic is handled by WhatsAppAccessibilityService
    }
}
