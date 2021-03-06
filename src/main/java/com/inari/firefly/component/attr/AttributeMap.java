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
package com.inari.firefly.component.attr;

import com.inari.commons.lang.list.DynArray;
import com.inari.commons.lang.list.IntBag;
import com.inari.firefly.component.Component;
import com.inari.firefly.component.ComponentId;
import com.inari.firefly.system.component.SystemComponent.SystemComponentKey;

public interface AttributeMap {
    
    ComponentId getComponentId();
    
    void setComponentId( ComponentId componentId );

    boolean isEmpty();

    <A> A getValue( AttributeKey<A> key );

    <A> AttributeMap put( AttributeKey<A> key, A value );
    
    AttributeMap putAll( AttributeMap attributes );
    
    AttributeMap putUntyped( AttributeKey<?> key, Object value );

    int getValue( AttributeKey<Integer> key, int defaultValue );

    float getValue( AttributeKey<Float> key, float defaultValue );

    boolean getValue( AttributeKey<Boolean> key, boolean defaultValue );

    long getValue( AttributeKey<Long> key, long defaultValue );

    <A> A getValue( AttributeKey<A> key, A defaultValue );

    Object getUntypedValue( AttributeKey<?> key, Object defaultValue );
    
    int getIdForName( 
        AttributeKey<String> nameAttribute, 
        AttributeKey<Integer> idAttribute, 
        SystemComponentKey<? extends Component> typeKey, 
        int defaultValue 
    );
    
    int getAssetInstanceId( 
        AttributeKey<String> nameAttribute, 
        AttributeKey<Integer> idAttribute, 
        int defaultValue 
    );
    
    IntBag getIdsForNames( 
        AttributeKey<DynArray<String>> namesAttribute, 
        AttributeKey<IntBag> idsAttribute,
        SystemComponentKey<? extends Component> typeKey, 
        IntBag defaultValue 
    );

    void clear();

    boolean contains( AttributeKey<?> key );

    

}