/*******************************************************************************
 * Copyright (c) 2015 - 2016, Andreas Hefti, inarisoft@yahoo.de 
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
package com.inari.firefly.entity;

import com.inari.commons.lang.aspect.Aspects;
import com.inari.firefly.FFInitException;
import com.inari.firefly.control.Controller;
import com.inari.firefly.system.external.FFTimer;

// TODO think about add and remove entityIds directly in the EntitySystem instead of listening to EntityActivationListener
public abstract class EntityController extends Controller implements EntityActivationListener {

    protected EntityController( int id ) {
        super( id );
    }
    @Override
    public void init() throws FFInitException {
        super.init();
        
        context.registerListener( EntityActivationEvent.TYPE_KEY, this );
    }

    @Override
    public void dispose() {
        context.disposeListener( EntityActivationEvent.TYPE_KEY, this );
        
        super.dispose();
    }
    
    @Override
    public final boolean match( Aspects aspects ) {
        return aspects.contains( EEntity.TYPE_KEY );
    }

    @Override
    public final void onEntityActivationEvent( EntityActivationEvent event ) {
        switch ( event.eventType ) {
            case ENTITY_ACTIVATED: {
                if ( hasControllerId( event.entityId ) ) {
                    componentIds.add( event.entityId );
                }
                break;
            } 
            case ENTITY_DEACTIVATED: {
                componentIds.remove( event.entityId );
                break;
            }
            default: {}
        }
    }

    @Override
    public void update( final FFTimer timer ) {
        for ( int i = 0; i < componentIds.length(); i++ ) {
            int entityId = componentIds.get( i );
            if ( entityId >= 0 ) {
                update( timer, entityId );
            }
        }
    }

    protected abstract void update( final FFTimer timer, int entityId );
    
    private final boolean hasControllerId( int entityId ) {
        EEntity controllerComponent = context.getEntityComponent( entityId, EEntity.TYPE_KEY );
        if ( controllerComponent == null ) {
            return false;
        }
        
        return controllerComponent.controlledBy( index );
    }

    public void initEntity( EntityAttributeMap attributes ) {
        // NOOP for default
    }
}
