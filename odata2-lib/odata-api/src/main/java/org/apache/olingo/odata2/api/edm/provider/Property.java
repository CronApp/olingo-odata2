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

import java.util.LinkedList;
import java.util.List;

import org.apache.olingo.odata2.api.edm.EdmFacets;

/**
 * Objects of this class represent a property of an entity type
 * 
 */
public abstract class Property {

  private String name;
  private String originalName;
  private int index = 0;
  private EdmFacets facets;
  private CustomizableFeedMappings customizableFeedMappings;
  private String mimeType;
  private Mapping mapping;
  private Documentation documentation;
  private List<AnnotationAttribute> annotationAttributes;
  private List<AnnotationElement> annotationElements;
  private List<Property> composite;

  /**
   * @return <b>String</b> name of this property
   */
  public String getName() {
    return name;
  }

  /**
   * @return {@link EdmFacets} of this property
   */
  public EdmFacets getFacets() {
    return facets;
  }

  /**
   * @return {@link CustomizableFeedMappings} of this property
   */
  public CustomizableFeedMappings getCustomizableFeedMappings() {
    return customizableFeedMappings;
  }

  /**
   * @return <b>String</b> mime type of this property
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * @return {@link Mapping} of this property
   */
  public Mapping getMapping() {
    return mapping;
  }

  /**
   * @return {@link Documentation} of this property
   */
  public Documentation getDocumentation() {
    return documentation;
  }

  /**
   * @return List of {@link AnnotationAttribute} annotation attributes
   */
  public List<AnnotationAttribute> getAnnotationAttributes() {
    return annotationAttributes;
  }

  /**
   * @return List of {@link AnnotationElement} annotation elements
   */
  public List<AnnotationElement> getAnnotationElements() {
    return annotationElements;
  }

  /**
   * Sets the name for this {@link Property}
   * @param name
   * @return {@link Property} for method chaining
   */
  public Property setName(final String name) {
    this.name = name;
    return this;
  }

  /**
   * Sets the {@link Facets} for this {@link Property}
   * @param facets
   * @return {@link Property} for method chaining
   */
  public Property setFacets(final EdmFacets facets) {
    this.facets = facets;
    return this;
  }

  /**
   * Sets the {@link CustomizableFeedMappings} for this {@link Property}
   * @param customizableFeedMappings
   * @return {@link Property} for method chaining
   */
  public Property setCustomizableFeedMappings(final CustomizableFeedMappings customizableFeedMappings) {
    this.customizableFeedMappings = customizableFeedMappings;
    return this;
  }

  /**
   * Sets the mime type for this {@link Property}
   * @param mimeType
   * @return {@link Property} for method chaining
   */
  public Property setMimeType(final String mimeType) {
    this.mimeType = mimeType;
    return this;
  }

  /**
   * Sets the {@link Mapping} for this {@link Property}
   * @param mapping
   * @return {@link Property} for method chaining
   */
  public Property setMapping(final Mapping mapping) {
    this.mapping = mapping;
    return this;
  }

  /**
   * Sets the {@link Documentation} for this {@link Property}
   * @param documentation
   * @return {@link Property} for method chaining
   */
  public Property setDocumentation(final Documentation documentation) {
    this.documentation = documentation;
    return this;
  }

  /**
   * Sets the List of {@link AnnotationAttribute} for this {@link Property}
   * @param annotationAttributes
   * @return {@link Property} for method chaining
   */
  public Property setAnnotationAttributes(final List<AnnotationAttribute> annotationAttributes) {
    this.annotationAttributes = annotationAttributes;
    return this;
  }

  /**
   * Sets the List of {@link AnnotationElement} for this {@link Property}
   * @param annotationElements
   * @return {@link Property} for method chaining
   */
  public Property setAnnotationElements(final List<AnnotationElement> annotationElements) {
    this.annotationElements = annotationElements;
    return this;
  }

  public String getOriginalName() {
    return originalName;
  }

  public void setOriginalName(String originalName) {
    this.originalName = originalName;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public void addComposite(Property property) {
    if (this.composite == null) {
      this.composite = new LinkedList<Property>();
    }
    this.composite.add(property);
  }

  public List<Property> getComposite() {
    return this.composite;
  }
}
