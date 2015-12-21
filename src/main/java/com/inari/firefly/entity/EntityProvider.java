package com.inari.firefly.entity;

import java.util.ArrayDeque;
import java.util.Set;

import com.inari.commons.lang.indexed.IndexedTypeKey;
import com.inari.commons.lang.indexed.IndexedTypeSet;
import com.inari.commons.lang.indexed.Indexer;
import com.inari.commons.lang.list.DynArray;
import com.inari.firefly.FFInitException;
import com.inari.firefly.component.build.ComponentCreationException;
import com.inari.firefly.entity.EntityComponent.EntityComponentTypeKey;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.FFContextInitiable;
import com.inari.firefly.system.FFSystem;

public final class EntityProvider implements FFSystem, FFContextInitiable  {
    
    public static final FFSystemTypeKey<EntityProvider> SYSTEM_KEY = FFSystemTypeKey.create( EntityProvider.class );

    final ArrayDeque<IndexedTypeSet> disposedComponentSets;
    final DynArray<ArrayDeque<EntityComponent>> disposedComponents;

    private int componentSetCapacity = 20;

    EntityProvider() {
        disposedComponentSets = new ArrayDeque<IndexedTypeSet>();
        disposedComponents = new DynArray<ArrayDeque<EntityComponent>>();
    }
    
    @Override
    public final IndexedTypeKey indexedTypeKey() {
        return SYSTEM_KEY;
    }

    @Override
    public final FFSystemTypeKey<EntityProvider> systemTypeKey() {
        return SYSTEM_KEY;
    }

    @Override
    public final void init( FFContext context ) throws FFInitException {
        Integer compSetCap = context.getProperty( FFContext.Properties.ENTITY_COMPONENT_SET_CAPACITY );
        if ( compSetCap != null ) {
            componentSetCapacity = compSetCap;
        }
        
        int size = Indexer.getIndexedObjectSize( EntityComponentTypeKey.class );
        if ( size > componentSetCapacity ) {
            componentSetCapacity = size;
        }

        disposedComponents.ensureCapacity( componentSetCapacity );

        for ( int i = 0; i < disposedComponents.capacity(); i++ ) {
            disposedComponents.add( new ArrayDeque<EntityComponent>() );
        }

        Integer cacheSize = context.getProperty( FFContext.Properties.ENTITY_BEANS_CACHE_SIZE );
        if ( cacheSize != null ) {
            createComponentSetsForLaterUse( cacheSize );
            for ( int i = 0; i < size; i++ ) {
                EntityComponentTypeKey<?> indexedTypeKey = Indexer.getIndexedTypeKeyForIndex( EntityComponentTypeKey.class, i );
                @SuppressWarnings( "unchecked" )
                Class<? extends EntityComponent> componentType = (Class<? extends EntityComponent>) indexedTypeKey.indexedType;
                createComponentsForLaterUse( cacheSize, componentType );
            }
        }
    }

    @Override
    public final void dispose( FFContext context ) {
        disposedComponentSets.clear();
        disposedComponents.clear();
    }
    
    public <T extends EntityComponent> T getComponent( Class<T> componentType ) {
        int componentTypeId = Indexer.getIndexedTypeKey( EntityComponentTypeKey.class, componentType ).index();
        ArrayDeque<EntityComponent> componentsOfType = disposedComponents.get( componentTypeId );
        T component;
        if ( componentsOfType.isEmpty() ) {
            component = newComponent( componentType );
        } else {
            component = componentType.cast( componentsOfType.pop() );
        }

        return component;
    }
    
    public final void createComponentSetsForLaterUse( int number ) {
        for ( int i = 0; i < number; i++ ) {
            disposedComponentSets.add( 
                new IndexedTypeSet( 
                    EntityComponentTypeKey.class, 
                    componentSetCapacity 
                ) 
            );
        }
    }
    
    public final void createComponentsForLaterUse( int number, Class<? extends EntityComponent> componentType ) {
        int componentTypeId = Indexer.getIndexedTypeKey( EntityComponentTypeKey.class, componentType ).index();
        ArrayDeque<EntityComponent> componentsOfType = disposedComponents.get( componentTypeId );
        if ( componentsOfType == null ) {
            componentsOfType = new ArrayDeque<EntityComponent>();
            disposedComponents.set( componentTypeId, componentsOfType );
        }
        
        for ( int i = 0; i < number; i++ ) {
            componentsOfType.add( newComponent( componentType ) );
        }
    }

    public IndexedTypeSet getComponentTypeSet() {
        if ( disposedComponentSets.isEmpty() ) {
            return new IndexedTypeSet( EntityComponentTypeKey.class, componentSetCapacity );
        }
        
        IndexedTypeSet result = disposedComponentSets.pop();
        if ( result.size() != 0 ) {
            throw new IllegalStateException( "NOTE: this can happen but never should happen. It seem that there is a other reference to this IndexedTypeSet: " + result );
        }
        return result;
    }

    void disposeComponentSet( IndexedTypeSet components ) {
        for ( int i = 0; i < components.length(); i++ ) {
            EntityComponent component = components.get( i );
            if ( component != null ) {
                component.resetAttributes();
                ArrayDeque<EntityComponent> componentsOfType;
                if ( !disposedComponents.contains( i ) ) {
                    componentsOfType = new ArrayDeque<EntityComponent>();
                    disposedComponents.set( i, componentsOfType );
                } else {
                    componentsOfType = disposedComponents.get( i );
                }
                componentsOfType.add( component );
            }
        }

        components.clear();
        disposedComponentSets.add( components );
    }

    void createComponents( IndexedTypeSet components, EntityAttributeMap attributes ) {
        Set<Class<? extends EntityComponent>> componentTypes = attributes.getEntityComponentTypes();
        for ( Class<? extends EntityComponent> componentType : componentTypes ) {
            EntityComponent component = getComponent( componentType );
            component.fromAttributes( attributes );
            components.set( component );
        }
    }

    private <C extends EntityComponent> C newComponent( Class<C> componentType ) {
        try {
            return componentType.newInstance();
        } catch ( Exception e ) {
            throw new ComponentCreationException( "Unknwon error while Component creation: " + componentType, e );
        }
    }

}
