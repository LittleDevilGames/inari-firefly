package com.inari.firefly.control.behavior;

import com.inari.firefly.control.behavior.Action.ActionState;
import com.inari.firefly.system.FFContext;

public interface BCondition {

    ActionState check( int entityId, final EBehavoir behavior, final FFContext context );
    
}
