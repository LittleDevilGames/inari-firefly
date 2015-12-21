package com.inari.firefly.state;

import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.external.FFTimer;

public interface StateChangeCondition {
    
    boolean check( FFContext context, Workflow workflow, FFTimer timer );

}
