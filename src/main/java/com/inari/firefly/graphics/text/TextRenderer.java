package com.inari.firefly.graphics.text;

import com.inari.commons.lang.indexed.IndexedTypeKey;
import com.inari.firefly.asset.AssetSystem;
import com.inari.firefly.graphics.BaseRenderer;
import com.inari.firefly.system.FFContext;

public abstract class TextRenderer extends BaseRenderer {
    
    public static final SystemComponentKey<TextRenderer> TYPE_KEY = SystemComponentKey.create( TextRenderer.class );
    
    protected TextSystem textSystem;
    protected AssetSystem assetSystem;
    
    TextRenderer( int id, FFContext context ) {
        super( id, context );
        
        textSystem = context.getSystem( TextSystem.SYSTEM_KEY );
        assetSystem = context.getSystem( AssetSystem.SYSTEM_KEY );
    }
    
    @Override
    public final IndexedTypeKey indexedTypeKey() {
        return TYPE_KEY;
    }

}