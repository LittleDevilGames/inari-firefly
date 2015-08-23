package com.inari.firefly.renderer.sprite.tile;

import com.inari.commons.event.IEventDispatcher;
import com.inari.commons.geom.Vector2f;
import com.inari.firefly.entity.ETransform;
import com.inari.firefly.renderer.BaseRenderer;
import com.inari.firefly.renderer.sprite.tile.TileGrid.TileGridIterator;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.FFInitException;
import com.inari.firefly.system.RenderEvent;
import com.inari.firefly.system.RenderEventListener;

public class TileGridRenderer extends BaseRenderer implements RenderEventListener {
    
    private TileGridSystem tileGridSystem;

    @Override
    public final void init( FFContext context ) throws FFInitException {
        super.init( context );
        tileGridSystem = context.getComponent( FFContext.Systems.TILE_GRID_SYSTEM );
        
        IEventDispatcher eventDispatcher = context.getComponent( FFContext.EVENT_DISPATCHER );
        eventDispatcher.register( RenderEvent.class, this );
    }
    
    @Override
    public final void dispose( FFContext context ) {
        IEventDispatcher eventDispatcher = context.getComponent( FFContext.EVENT_DISPATCHER );
        eventDispatcher.unregister( RenderEvent.class, this );
    }

    @Override
    public final void render( RenderEvent event ) {
        int viewId = event.getViewId();
        int layerId = event.getLayerId();
        TileGridIterator iterator = tileGridSystem.iterator( viewId, layerId, event.getClip() );
        if ( iterator == null ) {
            return;
        }
        
        switch ( tileGridSystem.getRenderMode( viewId, layerId ) ) {
            case FAST_RENDERING: {
                renderTileGrid( iterator );
                break;
            }
            case FULL_RENDERING: {
                renderTileGridAllData( iterator );
                break;
            }
            default: {}
        }
    }
    
    protected void renderTileGridAllData( TileGridIterator iterator ) {
        Vector2f actualWorldPosition  = iterator.getWorldPosition();
        while( iterator.hasNext() ) {
            int entityId = iterator.next();
            ETile cTile = entitySystem.getComponent( entityId, ETile.COMPONENT_TYPE );
            ETransform transform = entitySystem.getComponent( entityId, ETransform.COMPONENT_TYPE );
            
            transformCollector.set( transform );
            transformCollector.xpos += actualWorldPosition.dx;
            transformCollector.ypos += actualWorldPosition.dy;
            
            render( cTile, transform );
        }
    }

    protected void renderTileGrid( TileGridIterator iterator ) {
        Vector2f actualWorldPosition  = iterator.getWorldPosition();
        while( iterator.hasNext() ) {
            ETile cTile = entitySystem.getComponent( iterator.next(), ETile.COMPONENT_TYPE );
            lowerSystemFacade.renderSprite( cTile, actualWorldPosition.dx, actualWorldPosition.dy );
        }
    }

}
