package com.inari.firefly.entity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.inari.commons.lang.aspect.Aspect;
import com.inari.commons.lang.aspect.AspectGroup;
import com.inari.commons.lang.aspect.Aspects;
import com.inari.commons.lang.list.DynArray;
import com.inari.commons.lang.list.IntBag;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.control.Controller;

public class EEntity extends EntityComponent {
    
    public static final EntityComponentTypeKey<EEntity> TYPE_KEY = EntityComponentTypeKey.create( EEntity.class );
    public static final AspectGroup ENTITY_ASPECT_GROUP = new AspectGroup( "ENTITY_ASPECT_GROUP" );
    
    public static final AttributeKey<String> ENTITY_NAME = AttributeKey.createString( "entityName", EEntity.class );
    public static final AttributeKey<DynArray<String>> CONTROLLER_NAMES = AttributeKey.createDynArray( "controllerNames", EEntity.class );
    public static final AttributeKey<IntBag> CONTROLLER_IDS = AttributeKey.createIntBag( "controllerIds", EEntity.class );
    public static final AttributeKey<DynArray<Aspect>> ASPECTS = AttributeKey.createDynArray( "aspects", EEntity.class );
    private static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] { 
        ENTITY_NAME,
        CONTROLLER_IDS,
        ASPECTS
    };
    
    private String entityName;
    private final IntBag controllerIds;
    
    final Aspects aspects;
    
    public EEntity() {
        super( TYPE_KEY );
        aspects = ENTITY_ASPECT_GROUP.createAspects();
        controllerIds = new IntBag( 10, -1, 10 );
        resetAttributes();
    }

    @Override
    public final void resetAttributes() {
        controllerIds.clear();
        entityName = null;
        aspects.clear();
    }

    public final String getEntityName() {
        return entityName;
    }

    public final void setEntityName( String entityName ) {
        this.entityName = entityName;
    }

    public final IntBag getControllerIds() {
        return controllerIds;
    }

    public final void setControllerIds( IntBag controllerIds ) {
        this.controllerIds.clear();
        if ( controllerIds != null ) {
            this.controllerIds.addAll( controllerIds );
        }
    }
    
    public final boolean controlledBy( int controllerId ) {
        if ( controllerIds == null ) {
            return false;
        }
        
        return controllerIds.contains( controllerId );
    }
    
    public final Aspects getAspects() {
        return aspects;
    }
    
    public final void setAspect( Aspect aspect ) {
        aspects.set( aspect );
    }
    
    public final void resetAspect( Aspect aspect ) {
        aspects.reset( aspect );
    }
    
    public final void resetAspects( Aspects aspects ) {
        this.aspects.reset( aspects );
    }

    public final void setAspects( Aspects aspects ) {
        this.aspects.set( aspects );
    }
    
    public final boolean hasAspect( Aspect aspect ) {
        return aspects.contains( aspect );
    }
    
    public final void resetAspects() {
        aspects.clear();
    }

    @Override
    public final Set<AttributeKey<?>> attributeKeys() {
        return new HashSet<AttributeKey<?>>( Arrays.asList( ATTRIBUTE_KEYS ) );
    }

    @Override
    public void fromAttributes( AttributeMap attributes ) {
        entityName = attributes.getValue( ENTITY_NAME, entityName );
        setControllerIds( attributes.getIdsForNames( CONTROLLER_NAMES, CONTROLLER_IDS, Controller.TYPE_KEY, controllerIds ) );
        if ( attributes.contains( ASPECTS ) ) {
            DynArray<Aspect> aspects = attributes.getValue( ASPECTS );
            this.aspects.clear();
            for ( Aspect aspect : aspects ) {
                setAspect( aspect );
            }
        }
    }

    @Override
    public void toAttributes( AttributeMap attributes ) {
        attributes.put( ENTITY_NAME, entityName );
        attributes.put( CONTROLLER_IDS, controllerIds );
        attributes.put( ASPECTS, getAspectsAsDynArray() );
    }
    
    private DynArray<Aspect> getAspectsAsDynArray() {
        DynArray<Aspect> result = DynArray.create( Aspect.class, 20, 10 );
        for ( Aspect aspect : aspects ) {
            result.add( aspect );
        }
        return result;
    }

}
