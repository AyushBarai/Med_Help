package com.medhelp.common.tenant;

import java.util.UUID;

public class TenantContext {
    private static final ThreadLocal<UUID> CURRENT_LAB = new ThreadLocal<>();

    public static void set(UUID labid){
        CURRENT_LAB.set(labid);
    }

    public static UUID get(){
        UUID labId = CURRENT_LAB.get();
        if (labId == null) {
            throw new IllegalStateException("No Tenant in context. Is this request Authenticated?");
        }
        return labId;
    }

    public static UUID getOrNull(){
        return CURRENT_LAB.get();
    }

    public static void clear(){
        CURRENT_LAB.remove();
    }
}
