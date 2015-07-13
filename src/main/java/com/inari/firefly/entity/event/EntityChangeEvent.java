/*******************************************************************************
 * Copyright (c) 2015, Andreas Hefti, inarisoft@yahoo.de 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/ 
package com.inari.firefly.entity.event;

import com.inari.commons.event.AspectedEvent;
import com.inari.commons.lang.aspect.Aspect;

@Deprecated // check if this is really useful
public abstract class EntityChangeEvent extends AspectedEvent<EntityChangeListener> {
    
    public enum Type {
        COMPONENT_ADDED,
        COMPONENT_CHANGED,
        COMPONENT_REMOVED
    }
    
    public final int entityId;
    public final int componentId;
    public final Aspect aspect;
    public final Type type;
    
    public EntityChangeEvent( int entityId, int componentId, Aspect aspect, Type type ) {
        this.entityId = entityId;
        this.componentId = componentId;
        this.aspect = aspect;
        this.type = type;
    }

    @Override
    public Aspect getAspect() {
        return aspect;
    }

}
