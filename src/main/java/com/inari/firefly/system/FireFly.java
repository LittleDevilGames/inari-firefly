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
package com.inari.firefly.system;

import java.util.Iterator;
import java.util.Random;

import com.inari.commons.event.IEventDispatcher;
import com.inari.commons.geom.Position;
import com.inari.commons.geom.Rectangle;
import com.inari.firefly.animation.AnimationSystem;
import com.inari.firefly.asset.AssetSystem;
import com.inari.firefly.audio.AudioSystem;
import com.inari.firefly.control.ControllerSystem;
import com.inari.firefly.entity.EntityPrefabSystem;
import com.inari.firefly.entity.EntitySystem;
import com.inari.firefly.graphics.sprite.SpriteViewSystem;
import com.inari.firefly.graphics.tile.TileGridSystem;
import com.inari.firefly.state.StateSystem;
import com.inari.firefly.system.external.FFAudio;
import com.inari.firefly.system.external.FFGraphics;
import com.inari.firefly.system.external.FFTimer;
import com.inari.firefly.system.external.FFInput;
import com.inari.firefly.system.view.View;
import com.inari.firefly.system.view.ViewSystem;
import com.inari.firefly.task.TaskSystem;

public abstract class FireFly {
    
    public static final Random RANDOM = new Random();
    
    protected final FFContext context;
    
    protected FFGraphics lowerSystemFacade;
    protected ViewSystem viewSystem;

    private final UpdateEvent updateEvent;
    private final RenderEvent renderEvent;
    
    private boolean disposed = false;

    protected FireFly( 
            IEventDispatcher eventDispatcher, 
            FFGraphics graphics,
            FFAudio audio,
            FFTimer timer,
            FFInput input 
    ) {
        context = new FFContext( eventDispatcher, graphics, audio, timer, input );
        
        lowerSystemFacade = context.getGraphics();
        viewSystem = context.getSystem( ViewSystem.SYSTEM_KEY );
        
        context.loadSystem( AssetSystem.SYSTEM_KEY );
        context.loadSystem( StateSystem.SYSTEM_KEY );
        context.loadSystem( EntitySystem.SYSTEM_KEY );
        context.loadSystem( EntityPrefabSystem.SYSTEM_KEY );
        context.loadSystem( ControllerSystem.SYSTEM_KEY );
        context.loadSystem( AnimationSystem.SYSTEM_KEY );
        context.loadSystem( AudioSystem.SYSTEM_KEY );
        context.loadSystem( SpriteViewSystem.SYSTEM_KEY );
        context.loadSystem( TileGridSystem.SYSTEM_KEY );
        context.loadSystem( TaskSystem.SYSTEM_KEY );
        
        updateEvent = new UpdateEvent( timer );
        renderEvent = new RenderEvent();
    }
    
    public final boolean exit() {
        return context.exit;
    }

    public final void dispose() {
        context.dispose();
        disposed = true;
    }

    public final FFContext getContext() {
        return context;
    }
    
    public final void update() {
        updateEvent.timer.tick();
        context.notify( updateEvent );
    }
    
    public final void render() {
        if ( disposed ) {
            return;
        }
        // NOTE: for now there is no renderer that works with approximationTime so I skip the calculation so far.
        // TODO: implements the calculation of approximationTime and set it to the event.
        if ( viewSystem.hasActiveViewports() ) {
            Iterator<View> virtualViewIterator = viewSystem.activeViewportIterator();
            while ( virtualViewIterator.hasNext() ) {
                View virtualView = virtualViewIterator.next();
                if ( !virtualView.isActive() ) {
                    continue;
                }

                int viewId = virtualView.index();
                Rectangle bounds = virtualView.getBounds();
                Position worldPosition = virtualView.getWorldPosition();
                renderEvent.viewId = viewId;
                renderEvent.clip.x = worldPosition.x;
                renderEvent.clip.y = worldPosition.y;
                renderEvent.clip.width = bounds.width;
                renderEvent.clip.height = bounds.height;

                lowerSystemFacade.startRendering( virtualView );
                context.notify( renderEvent );
                lowerSystemFacade.endRendering( virtualView );
            }
            
            lowerSystemFacade.flush( viewSystem.activeViewportIterator() );
        } else {
            View baseView = viewSystem.getView( ViewSystem.BASE_VIEW_ID );
            
            Rectangle bounds = baseView.getBounds();
            Position worldPosition = baseView.getWorldPosition();
            renderEvent.viewId = ViewSystem.BASE_VIEW_ID;
            renderEvent.clip.x = worldPosition.x;
            renderEvent.clip.y = worldPosition.y;
            renderEvent.clip.width = bounds.width;
            renderEvent.clip.height = bounds.height;
            
            lowerSystemFacade.startRendering( baseView );
            context.notify( renderEvent );
            lowerSystemFacade.endRendering( baseView );
            
            lowerSystemFacade.flush( null );
        }
    }

}
