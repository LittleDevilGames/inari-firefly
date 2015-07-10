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
package com.inari.firefly.control;

import com.inari.commons.lang.list.IntBag;
import com.inari.firefly.Disposable;
import com.inari.firefly.component.NamedIndexedComponent;

public abstract class Controller extends NamedIndexedComponent implements Disposable {
   
    // TODO check if this is a proper init
    protected IntBag componentIds = new IntBag( 10, -1 );
    
    protected Controller( int id ) {
        super( id );
    }

    @Override
    public final Class<Controller> indexedObjectType() {
        return Controller.class;
    }
    
    @Override
    public final Class<Controller> getComponentType() {
        return Controller.class;
    }
    
    public final void addComponentId( int componentId ) {
        componentIds.add( componentId );
    }
    
    public final void removeComponentId( int componentId ) {
        componentIds.remove( componentId );
    }
    
    public abstract void update( long time );

}
