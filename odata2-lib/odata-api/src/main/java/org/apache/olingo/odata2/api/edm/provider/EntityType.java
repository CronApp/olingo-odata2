/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ******************************************************************************/
package org.apache.olingo.odata2.api.edm.provider;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.FullQualifiedName;

/**
 * Objects of this class represent an entity type
 * 
 */
public class EntityType extends ComplexType {

  private boolean hasStream;
  private CustomizableFeedMappings customizableFeedMappings;
  private Key key;
  private List<NavigationProperty> navigationProperties;
  private boolean showMetadata = true;

  /**
   * @return <b>boolean</b> if this EntityType is a media resource
   */
  public boolean isHasStream() {
    return hasStream;
  }

  /**
   * @return {@link CustomizableFeedMappings} of this entity type
   */
  public CustomizableFeedMappings getCustomizableFeedMappings() {
    return customizableFeedMappings;
  }

  /**
   * @return {@link Key} of this entity type
   */
  public Key getKey() {
    return key;
  }

  /**
   * @return List<{@link NavigationProperty}> of this entity type
   */
  public List<NavigationProperty> getNavigationProperties() {
    return navigationProperties;
  }

  /**
   * Sets if this {@link EntityType} is a media resource
   * @param hasStream
   * @return {@link EntityType} for method chaining,
   */
  public EntityType setHasStream(final boolean hasStream) {
    this.hasStream = hasStream;
    return this;
  }

  /**
   * Sets the {@link CustomizableFeedMappings} for this {@link EntityType}
   * @param customizableFeedMappings
   * @return {@link EntityType} for method chaining
   */
  public EntityType setCustomizableFeedMappings(final CustomizableFeedMappings customizableFeedMappings) {
    this.customizableFeedMappings = customizableFeedMappings;
    return this;
  }

  /**
   * Sets the {@link Key} for this {@link EntityType}
   * @param key
   * @return {@link EntityType} for method chaining
   */
  public EntityType setKey(final Key key) {
    this.key = key;
    return this;
  }

  /**
   * Sets the {@link NavigationProperty}s for this {@link EntityType}
   * @param navigationProperties
   * @return {@link EntityType} for method chaining
   */
  public EntityType setNavigationProperties(final List<NavigationProperty> navigationProperties) {
    this.navigationProperties = navigationProperties;
    return this;
  }

  /**
   * @param name
   * @return {@link EntityType} for method chaining
   */
  @Override
  public EntityType setName(final String name) {
    super.setName(name);
    return this;
  }

  /**
   * @param baseType
   * @return {@link EntityType} for method chaining
   */
  @Override
  public EntityType setBaseType(final FullQualifiedName baseType) {
    super.setBaseType(baseType);
    return this;
  }

  /**
   * @param isAbstract
   * @return {@link EntityType} for method chaining
   */
  @Override
  public EntityType setAbstract(final boolean isAbstract) {
    super.setAbstract(isAbstract);
    return this;
  }

  /**
   * @param properties
   * @return {@link EntityType} for method chaining
   */
  @Override
  public EntityType setProperties(final List<Property> properties) {
    super.setProperties(properties);
    return this;
  }

  /**
   * @param mapping
   * @return {@link EntityType} for method chaining
   */
  @Override
  public EntityType setMapping(final Mapping mapping) {
    super.setMapping(mapping);
    return this;
  }

  /**
   * @param documentation
   * @return {@link EntityType} for method chaining
   */
  @Override
  public EntityType setDocumentation(final Documentation documentation) {
    super.setDocumentation(documentation);
    return this;
  }

  /**
   * Sets the collection of {@link AnnotationAttribute} for this {@link EntityType}
   * @param annotationAttributes
   * @return {@link EntityType} for method chaining
   */
  @Override
  public EntityType setAnnotationAttributes(final List<AnnotationAttribute> annotationAttributes) {
    super.setAnnotationAttributes(annotationAttributes);
    return this;
  }

  /**
   * Sets the collection of {@link AnnotationElement} for this {@link EntityType}
   * @param annotationElements
   * @return {@link EntityType} for method chaining
   */
  @Override
  public EntityType setAnnotationElements(final List<AnnotationElement> annotationElements) {
    super.setAnnotationElements(annotationElements);
    return this;
  }

  private Object clone(Object obj) {

    if (obj == null)
      return null;

    Object newObj = null;

    if (obj instanceof Map) {
      newObj = new HashMap();
      ((Map)newObj).putAll((Map) obj);
    }

    else if (obj instanceof List) {
      newObj = new ArrayList();
      ((List)newObj).addAll((List) obj);
    } else {
      newObj = obj;
    }

    return newObj;

  }

  private Object getFieldValue(String name) throws IllegalAccessException, NoSuchFieldException {
    Field f = this.getClass().getDeclaredField(name);
    return f.get(this);
  }

  public EntityType getClone()  {
    EntityType newObj = new EntityType();

    for (Field f : this.getClass().getDeclaredFields()) {
      try {
        f.set(newObj, clone(getFieldValue(f.getName())));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return newObj;
  }

  public boolean showMetadata() {
    return showMetadata;
  }

  public void setShowMetadata(boolean showMetadata) {
    this.showMetadata = showMetadata;
  }
}
