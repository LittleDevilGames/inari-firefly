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
package com.inari.firefly.graphics.tile;

import java.util.Iterator;

import com.inari.commons.geom.Position;
import com.inari.commons.lang.aspect.Aspects;
import com.inari.commons.lang.list.DynArray;
import com.inari.firefly.FFInitException;
import com.inari.firefly.entity.EntityActivationEvent;
import com.inari.firefly.entity.EntityActivationListener;
import com.inari.firefly.entity.EntitySystem;
import com.inari.firefly.graphics.ETransform;
import com.inari.firefly.graphics.view.ViewEvent;
import com.inari.firefly.graphics.view.ViewEvent.Type;
import com.inari.firefly.graphics.view.ViewEventListener;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.RenderEvent;
import com.inari.firefly.system.component.ComponentSystem;
import com.inari.firefly.system.component.SystemBuilderAdapter;
import com.inari.firefly.system.component.SystemComponent.SystemComponentKey;
import com.inari.firefly.system.component.SystemComponentBuilder;

public final class TileGridSystem
    extends 
        ComponentSystem<TileGridSystem>
    implements
        ViewEventListener,
        EntityActivationListener {
    
    public static final FFSystemTypeKey<TileGridSystem> SYSTEM_KEY = FFSystemTypeKey.create( TileGridSystem.class ); 
    private static final SystemComponentKey<?>[] SUPPORTED_COMPONENT_TYPES = new SystemComponentKey[] {
        TileGrid.TYPE_KEY,
        TileGridRenderer.TYPE_KEY
    };

    private EntitySystem entitySystem;
    
    private final DynArray<TileGridRenderer> renderer;
    private final DynArray<TileGrid> tileGrids;
    private final DynArray<DynArray<TileGrid>> tileGridOfViewsPerLayer;
    
    public TileGridSystem() {
        super( SYSTEM_KEY );
        renderer = DynArray.create( TileGridRenderer.class, 5, 5 );
        tileGrids = DynArray.create( TileGrid.class, 20, 10 );
        tileGridOfViewsPerLayer = DynArray.createTyped( DynArray.class, 10, 10 );
    }
    
    @Override
    public void init( FFContext context ) {
        super.init( context );
        entitySystem = context.getSystem( EntitySystem.SYSTEM_KEY );
        
        // build and register default tile grid renderer
        getRendererBuilder( NormalFastTileGridRenderer.class )
            .set( TileGridRenderer.NAME, NormalFastTileGridRenderer.NAME )
            .build();
        getRendererBuilder( NormalFullTileGridRenderer.class )
            .set( TileGridRenderer.NAME, NormalFullTileGridRenderer.NAME )
            .build();

        context.registerListener( ViewEvent.TYPE_KEY, this );
        context.registerListener( EntityActivationEvent.TYPE_KEY, this );
        context.registerListener( TileSystemEvent.TYPE_KEY, this );
    }

    public final TileGridRendererBuilder getRendererBuilder( Class<? extends TileGridRenderer> componentType ) {
        return new TileGridRendererBuilder( componentType );
    }

    @Override
    public final void dispose( FFContext context ) {
        context.disposeListener( ViewEvent.TYPE_KEY, this );
        context.disposeListener( EntityActivationEvent.TYPE_KEY, this );
        context.disposeListener( TileSystemEvent.TYPE_KEY, this );
        
        for ( TileGridRenderer r : renderer ) {
            context.disposeListener( RenderEvent.TYPE_KEY, r );
            r.dispose();
        }
        
        clear();
    }

    final void removeMultiTilePosition( final int tileGridId, final int entityId, final int x, final int y ) {
        ETile tile = entitySystem.getComponent( entityId, ETile.TYPE_KEY );
        tile.getGridPositions().remove( new Position( x, y ) );
        getTileGrid( tileGridId ).reset( x, y );
    }

    final void addMultiTilePosition( final int tileGridId, final int entityId, final int x, final int y ) {
        ETile tile = entitySystem.getComponent( entityId, ETile.TYPE_KEY );
        tile.getGridPositions().add( new Position( x, y ) );
        getTileGrid( tileGridId ).set( entityId, x, y );
    }
    
    public final void entityActivated( int entityId, final Aspects aspects ) {
        final ETransform transform = entitySystem.getComponent( entityId, ETransform.TYPE_KEY );
        final ETile tile = entitySystem.getComponent( entityId, ETile.TYPE_KEY );
        final TileGrid tileGrid = getTileGrid( transform.getViewId(), transform.getLayerId() );
        final DynArray<Position> gridPositions = tile.getGridPositions();
        
        for ( int i = 0; i < gridPositions.capacity(); i++ ) {
            if ( !gridPositions.contains( i ) ) {
                continue;
            }

            tileGrid.set( entityId, gridPositions.get( i ) );
        }
    }
    
    public final void entityDeactivated( int entityId, final Aspects aspects ) {
        final ETransform transform = entitySystem.getComponent( entityId, ETransform.TYPE_KEY );
        final ETile tile = entitySystem.getComponent( entityId, ETile.TYPE_KEY );
        final TileGrid tileGrid = getTileGrid( transform.getViewId(), transform.getLayerId() );
        final DynArray<Position> gridPositions = tile.getGridPositions();
        
        for ( int i = 0; i < gridPositions.capacity(); i++ ) {
            if ( !gridPositions.contains( i ) ) {
                continue;
            }
            
            tileGrid.resetIfMatch( entityId, gridPositions.get( i ) );
        }
    }
    
    @Override
    public final void onViewEvent( ViewEvent event ) {
        if ( event.isOfType( Type.VIEW_DELETED ) ) {
            deleteAllTileGrid( event.getView().index() );
            return;
        }
    }
    
    @Override
    public final boolean match( Aspects aspects ) {
        return aspects.contains( ETile.TYPE_KEY );
    }
    
    public final TileGridRenderer getRenderer( int id ) {
        if ( renderer.contains( id ) ) {
            return renderer.get( id );
        }
        
        return null;
    }

    public final int getRendererId( String name ) {
        for ( TileGridRenderer r : renderer ) {
            if ( name.equals( r.getName() ) ) {
                return r.index();
            }
        }
        
        return -1;
    }

    public final void deleteRenderer( int id ) {
        TileGridRenderer r = renderer.remove( id );
        if ( r != null ) {
            context.disposeListener( RenderEvent.TYPE_KEY, r );
            r.dispose();
        }
    }
    
    public final boolean hasTileGrid( int viewId, int layerId ) {
        return getTileGrid( viewId, layerId ) != null;
    }
    
    public final TileGrid getTileGrid( final String tileGridName ) {
        for ( TileGrid tileGrid : tileGrids ) {
            if ( tileGrid != null && tileGrid.getName().equals(  tileGridName ) ) {
                return tileGrid;
            }
        }
        return null;
    }
    
    public final int getTileGridId( final String tileGridName ) {
        for ( TileGrid tileGrid : tileGrids ) {
            if ( tileGrid != null && tileGrid.getName().equals(  tileGridName ) ) {
                return tileGrid.index();
            }
        }
        return -1;
    }
    
    public final TileGrid getTileGrid( int tileGridId ) {
        for ( TileGrid tileGrid : tileGrids ) {
            if ( tileGrid != null && tileGrid.index() == tileGridId ) {
                return tileGrid;
            }
        }
        return null;
    }

    public final TileGrid getTileGrid( int viewId, int layerId ) {
        if ( !tileGridOfViewsPerLayer.contains( viewId ) ) {
            return null;
        }
        
        DynArray<TileGrid> tileGridsForView = tileGridOfViewsPerLayer.get( viewId );
        return tileGridsForView.get( layerId );
    }
    
    public final int getTile( int viewId, int layerId, final Position position ) {
        TileGrid tileGrid = getTileGrid( viewId, layerId );
        if ( tileGrid == null ) {
            return -1;
        }
        
        return tileGrid.getTileAt( position );
    }
    
    public final int getTile( int tileGridId, final Position position ) {
        TileGrid tileGrid = getTileGrid( tileGridId );
        if ( tileGrid == null ) {
            return -1;
        }
        
        return tileGrid.getTileAt( position );
    }

    public final void deleteAllTileGrid( int viewId ) {
        if ( tileGridOfViewsPerLayer.contains( viewId ) ) {
            DynArray<TileGrid> toRemove = tileGridOfViewsPerLayer.remove( viewId );
            for ( TileGrid tileGrid : toRemove ) {
                tileGrids.remove( tileGrid.index() );
                disposeSystemComponent( tileGrid );
            }
        }
    }
    
    public final void deleteTileGrid( int viewId, int layerId ) {
        if ( !tileGridOfViewsPerLayer.contains( viewId ) ) {
            return;
        }
        DynArray<TileGrid> tileGridsForView = tileGridOfViewsPerLayer.get( viewId );
        if ( tileGridsForView == null ) {
            return;
        }
        
        if ( !tileGridOfViewsPerLayer.contains( layerId ) ) {
            return;
        }
        
        TileGrid removed = tileGridsForView.remove( layerId );
        tileGrids.remove( removed.index() );
        disposeSystemComponent( removed );
    }
    
    public final void deleteTileGrid( int tileGridId ) {
        if ( !tileGrids.contains( tileGridId ) ) {
            return;
        }
        
        TileGrid removed = tileGrids.get( tileGridId );
        tileGridOfViewsPerLayer.get( removed.getViewId() ).remove( removed.getLayerId() );
    };

    public final SystemComponentBuilder getTileGridBuilder() {
        return new TileGridBuilder();
    }
    
    @Override
    public final SystemComponentKey<?>[] supportedComponentTypes() {
        return SUPPORTED_COMPONENT_TYPES;
    }

    @Override
    public final SystemBuilderAdapter<?>[] getSupportedBuilderAdapter() {
        return new SystemBuilderAdapter<?>[] {
            new TileGridBuilderAdapter(),
            new TileGridRendererBuilderAdapter()
        };
    }

    @Override
    public final void clear() {
        tileGridOfViewsPerLayer.clear();
        renderer.clear();
    }
    
    private final class TileGridBuilder extends SystemComponentBuilder {
        
        private TileGridBuilder() {
            super( context );
        }
        
        @Override
        public final SystemComponentKey<TileGrid> systemComponentKey() {
            return TileGrid.TYPE_KEY;
        }
        
        @Override
        public int doBuild( int componentId, Class<?> subType, boolean activate ) {
            TileGrid tileGrid = createSystemComponent( componentId, subType, context );

            int viewId = tileGrid.getViewId();
            int layerId = tileGrid.getLayerId();
            
            if ( viewId < 0 ) {
                throw new FFInitException( "ViewId is mandatory for TileGrid" );
            }
            
            if ( layerId < 0 ) {
                throw new FFInitException( "LayerId is mandatory for TileGrid" );
            }
            
            if ( !tileGridOfViewsPerLayer.contains( viewId ) ) {
                tileGridOfViewsPerLayer.set( viewId, DynArray.create( TileGrid.class, 20, 10 ) );
            }

            tileGrids.set( tileGrid.index(), tileGrid );
            tileGridOfViewsPerLayer
                .get( viewId )
                .set( layerId, tileGrid );

            return tileGrid.index();
        }
    }
    
    private final class TileGridRendererBuilder extends SystemComponentBuilder {
        
        private TileGridRendererBuilder( Class<? extends TileGridRenderer> componentType ) {
            super( context, componentType );
        }
        
        @Override
        public final SystemComponentKey<TileGridRenderer> systemComponentKey() {
            return TileGridRenderer.TYPE_KEY;
        }

        @Override
        public int doBuild( int componentId, Class<?> componentType, boolean activate ) {
            TileGridRenderer component = createSystemComponent( componentId, componentType, context );
            renderer.set( component.index(), component );
            return component.index();
        }
    }
    
    private final class TileGridBuilderAdapter extends SystemBuilderAdapter<TileGrid> {
        private TileGridBuilderAdapter() {
            super( TileGridSystem.this, TileGrid.TYPE_KEY );
        }
        @Override
        public final TileGrid get( int id ) {
            return getTileGrid( id );
        }
        @Override
        public final Iterator<TileGrid> getAll() {
            return tileGrids.iterator();
        }
        @Override
        public final void delete( int id ) {
            deleteTileGrid( id );
        }
        @Override
        public final int getId( String name ) {
            return getTileGridId( name );
        }
        @Override
        public final void activate( int id ) {
            throw new UnsupportedOperationException( componentTypeKey() + " is not activable" );
        }
        @Override
        public final void deactivate( int id ) {
            throw new UnsupportedOperationException( componentTypeKey() + " is not activable" );
        }
        @Override
        public final SystemComponentBuilder createComponentBuilder( Class<? extends TileGrid> componentType ) {
            return new TileGridBuilder();
        }
    }
    
    private final class TileGridRendererBuilderAdapter extends SystemBuilderAdapter<TileGridRenderer> {
        private TileGridRendererBuilderAdapter() {
            super( TileGridSystem.this, TileGridRenderer.TYPE_KEY );
        }
        @Override
        public final TileGridRenderer get( int id ) {
            return getRenderer( id );
        }
        @Override
        public final void delete( int id ) {
            deleteRenderer( id );
        }
        @Override
        public final Iterator<TileGridRenderer> getAll() {
            return renderer.iterator();
        }
        @Override
        public final int getId( String name ) {
            return getRendererId( name );
        }
        @Override
        public final void activate( int id ) {
            throw new UnsupportedOperationException( componentTypeKey() + " is not activable" );
        }
        @Override
        public final void deactivate( int id ) {
            throw new UnsupportedOperationException( componentTypeKey() + " is not activable" );
        }
        @Override
        public final SystemComponentBuilder createComponentBuilder( Class<? extends TileGridRenderer> componentType ) {
            if ( componentType == null ) {
                throw new IllegalArgumentException( "componentType is needed for SystemComponentBuilder for component: " + componentTypeKey().name() );
            }
            return new TileGridRendererBuilder( componentType );
        }
    }

}
