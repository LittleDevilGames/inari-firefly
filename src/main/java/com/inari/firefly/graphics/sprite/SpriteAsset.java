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
package com.inari.firefly.graphics.sprite;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.inari.commons.geom.Rectangle;
import com.inari.commons.lang.list.IntBag;
import com.inari.firefly.asset.Asset;
import com.inari.firefly.asset.AssetSystem;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.external.SpriteData;
import com.inari.firefly.system.utils.Disposable;

public final class SpriteAsset extends Asset implements SpriteData {
    
    public static final AttributeKey<String> TEXTURE_ASSET_NAME = new AttributeKey<String>( "textureAssetName", String.class, SpriteAsset.class );
    public static final AttributeKey<Integer> TEXTURE_ASSET_ID = new AttributeKey<Integer>( "textureAssetId", Integer.class, SpriteAsset.class );
    public static final AttributeKey<Rectangle> TEXTURE_REGION  = new AttributeKey<Rectangle>( "textureRegion", Rectangle.class, SpriteAsset.class );
    public static final AttributeKey<Boolean> HORIZONTAL_FLIP = new AttributeKey<Boolean>( "horizontalFlip", Boolean.class, ESprite.class );
    public static final AttributeKey<Boolean> VERTICAL_FLIP = new AttributeKey<Boolean>( "verticalFlip", Boolean.class, ESprite.class );
    private static final Set<AttributeKey<?>> ATTRIBUTE_KEYS = new HashSet<AttributeKey<?>>( Arrays.<AttributeKey<?>>asList( new AttributeKey[] { 
        TEXTURE_ASSET_ID,
        TEXTURE_REGION,
        HORIZONTAL_FLIP,
        VERTICAL_FLIP
    } ) );
    
    private int textureAssetId;
    private final Rectangle textureRegion;
    
    private int textureId = -1;
    private int spriteId = -1;
    
    private boolean horizontalFlip = false;
    private boolean verticalFlip = false;
    
    SpriteAsset( int assetId ) {
        super( assetId );
        textureRegion = new Rectangle();
    }

    public final int getTextureAssetId() {
        return textureAssetId;
    }

    public final void setTextureAssetId( int textureAssetId ) {
        this.textureAssetId = textureAssetId;
    }

    @Override
    public final int getInstanceId( int index ) {
        return spriteId;
    }
    
    public final int getSpriteId() {
        return spriteId;
    }

    @Override
    protected final IntBag dependsOn() {
        IntBag result = new IntBag( 1, -1 );
        result.add( textureAssetId );
        return result;
    }
    
    public final int getTextureId() {
        return textureId;
    }

    public final Rectangle getTextureRegion() {
        return textureRegion;
    }

    public final void setTextureRegion( Rectangle textureRegion ) {
        checkNotAlreadyLoaded();
        this.textureRegion.x = textureRegion.x;
        this.textureRegion.y = textureRegion.y;
        this.textureRegion.width = textureRegion.width;
        this.textureRegion.height = textureRegion.height;
    }
    
    public final boolean isHorizontalFlip() {
        return horizontalFlip;
    }

    public final void setHorizontalFlip( boolean horizontalFlip ) {
        this.horizontalFlip = horizontalFlip;
    }

    public final boolean isVerticalFlip() {
        return verticalFlip;
    }

    public final void setVerticalFlip( boolean verticalFlip ) {
        this.verticalFlip = verticalFlip;
    }

    @Override
    public final Set<AttributeKey<?>> attributeKeys() {
        Set<AttributeKey<?>> attributeKeys = super.attributeKeys();
        attributeKeys.addAll( ATTRIBUTE_KEYS );
        return super.attributeKeys( attributeKeys );
    }

    @Override
    public final void fromAttributes( AttributeMap attributes ) {
        checkNotAlreadyLoaded();
        super.fromAttributes( attributes );
        
        textureAssetId = attributes.getIdForName( TEXTURE_ASSET_NAME, TEXTURE_ASSET_ID, Asset.TYPE_KEY, textureAssetId );
        Rectangle textureRegion = attributes.getValue( TEXTURE_REGION );
        if ( textureRegion != null ) {
            setTextureRegion( textureRegion );
        }
        
        horizontalFlip = attributes.getValue( HORIZONTAL_FLIP, horizontalFlip );
        verticalFlip = attributes.getValue( VERTICAL_FLIP, verticalFlip );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        attributes.put( TEXTURE_ASSET_ID, textureAssetId );
        attributes.put( TEXTURE_REGION, new Rectangle( textureRegion ) );
        attributes.put( HORIZONTAL_FLIP, horizontalFlip );
        attributes.put( VERTICAL_FLIP, verticalFlip );
    }

    @Override
    public final Disposable load( FFContext context ) {
        if ( loaded ) {
            return this;
        }
        
        textureId = context.getSystem( AssetSystem.SYSTEM_KEY ).getAssetInstanceId( textureAssetId );
        spriteId = context.getGraphics().createSprite( this );
        
        return this;
    }

    @Override
    public final void dispose( FFContext context ) {
        if ( !loaded ) {
            return;
        }
        
        context.getGraphics().disposeSprite( spriteId );
        spriteId = -1;
        textureId = -1;
    }

}
