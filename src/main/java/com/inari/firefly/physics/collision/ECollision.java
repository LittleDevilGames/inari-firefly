package com.inari.firefly.physics.collision;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.inari.commons.geom.BitMask;
import com.inari.commons.geom.Rectangle;
import com.inari.commons.lang.aspect.Aspect;
import com.inari.commons.lang.list.DynArray;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.entity.EntityComponent;

public final class ECollision extends EntityComponent {
    
    public static final EntityComponentTypeKey<ECollision> TYPE_KEY = EntityComponentTypeKey.create( ECollision.class );
    
    public static final AttributeKey<Rectangle> COLLISION_BOUNDS = new AttributeKey<Rectangle>( "collisionBounds", Rectangle.class, ECollision.class );
    public static final AttributeKey<BitMask> COLLISION_MASK = new AttributeKey<BitMask>( "collisionMask", BitMask.class, ECollision.class );
    public static final AttributeKey<String> COLLISION_RESOLVER_NAME = new AttributeKey<String>( "collisionResolverName", String.class, ECollision.class );
    public static final AttributeKey<Integer> COLLISION_RESOLVER_ID = new AttributeKey<Integer>( "collisionResolverId", Integer.class, ECollision.class );
    public static final AttributeKey<Aspect> MATERIAL_TYPE = new AttributeKey<Aspect>( "materialType", Aspect.class, ECollision.class );
    public static final AttributeKey<Aspect> CONTACT_TYPE = new AttributeKey<Aspect>( "contactType", Aspect.class, ECollision.class );
    public static final AttributeKey<DynArray<ContactConstraint>> CONTACT_CONSTRAINTS = AttributeKey.createDynArray( "contactConstraints", ECollision.class );
    private static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] { 
        COLLISION_BOUNDS,
        COLLISION_MASK,
        COLLISION_RESOLVER_ID,
        MATERIAL_TYPE,
        CONTACT_TYPE,
        CONTACT_CONSTRAINTS
    };
    
    private final Rectangle collisionBounds;
    private BitMask collisionMask;
    private int collisionResolverId;
    private Aspect materialType;
    private Aspect contactType;
    
    private ContactScan contactScan;

    ECollision() {
        super( TYPE_KEY );
        collisionBounds = new Rectangle();
        contactScan = new ContactScan();
        resetAttributes();
    }
    
    @Override
    public final void resetAttributes() {
        collisionMask = null;
        collisionResolverId = -1;
        contactType = null;
        materialType = null;
        contactScan = new ContactScan();
    }

    public final Rectangle getCollisionBounds() {
        return collisionBounds;
    }

    public final void setCollisionBounds( Rectangle collisionBounds ) {
        if ( collisionBounds == null ) {
            this.collisionBounds.x = 0;
            this.collisionBounds.y = 0;
            this.collisionBounds.width = 0;
            this.collisionBounds.height = 0;
            return;
        }
        this.collisionBounds.setFrom( collisionBounds );
    }

    public final BitMask getCollisionMask() {
        return collisionMask;
    }

    public final void setCollisionMask( BitMask collisionMask ) {
        this.collisionMask = collisionMask;
    }

    public final int getCollisionResolverId() {
        return collisionResolverId;
    }

    public final void setCollisionResolverId( int collisionResolverId ) {
        this.collisionResolverId = collisionResolverId;
    }

    public final ContactScan getContactScan() {
        return contactScan;
    }

    public final Aspect getMaterialType() {
        return materialType;
    }

    public final void setMaterialType( Aspect materialType ) {
        this.materialType = materialType;
    }

    public final Aspect getContactType() {
        return contactType;
    }

    public final void setContactType( Aspect contactType ) {
        this.contactType = contactType;
    }
    
    public final void addContactConstraint( ContactConstraint constraint ) {
        contactScan.addContactContstraint( constraint );
    }

    @Override
    public final Set<AttributeKey<?>> attributeKeys() {
        return new HashSet<AttributeKey<?>>( Arrays.asList( ATTRIBUTE_KEYS ) );
    }

    @Override
    public final void fromAttributes( AttributeMap attributes ) {
        setCollisionBounds( attributes.getValue( COLLISION_BOUNDS, collisionBounds ) );
        collisionMask = attributes.getValue( COLLISION_MASK, collisionMask );
        collisionResolverId = attributes.getIdForName( COLLISION_RESOLVER_NAME, COLLISION_RESOLVER_ID, CollisionResolver.TYPE_KEY, collisionResolverId );
        materialType = attributes.getValue( MATERIAL_TYPE, materialType );
        contactType = attributes.getValue( CONTACT_TYPE, contactType );
        
        if ( attributes.contains( CONTACT_CONSTRAINTS ) ) {
            DynArray<ContactConstraint> constraints = attributes.getValue( CONTACT_CONSTRAINTS );
            for ( ContactConstraint constraint : constraints ) {
                addContactConstraint( constraint );
            }
        }
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        attributes.put( COLLISION_BOUNDS, collisionBounds );
        attributes.put( COLLISION_MASK, collisionMask );
        attributes.put( COLLISION_RESOLVER_ID, collisionResolverId );
        attributes.put( MATERIAL_TYPE, materialType );
        attributes.put( CONTACT_TYPE, contactType );
    }

}
