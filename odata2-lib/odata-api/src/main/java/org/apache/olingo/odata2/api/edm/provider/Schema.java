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

import java.util.List;

/**
 * Objects of this class represent a schema
 * 
 */
public class Schema {

  private String namespace;
  private String alias;
  private List<Using> usings;
  private List<EntityType> entityTypes;
  private List<ComplexType> complexTypes;
  private List<Association> associations;
  private List<EntityContainer> entityContainers;
  private List<AnnotationAttribute> annotationAttributes;
  private List<AnnotationElement> annotationElements;

  /**
   * Sets the namespace for this {@link Schema}
   * @param namespace
   * @return {@link Schema} for method chaining
   */
  public Schema setNamespace(final String namespace) {
    this.namespace = namespace;
    return this;
  }

  /**
   * Sets the alias for this {@link Schema}
   * @param alias
   * @return {@link Schema} for method chaining
   */
  public Schema setAlias(final String alias) {
    this.alias = alias;
    return this;
  }

  /**
   * Sets the {@link Using} for this {@link Schema}
   * @param usings
   * @return {@link Schema} for method chaining
   */
  public Schema setUsings(final List<Using> usings) {
    this.usings = usings;
    return this;
  }

  /**
   * Sets the {@link EntityType}s for this {@link Schema}
   * @param entityTypes
   * @return {@link Schema} for method chaining
   */
  public Schema setEntityTypes(final List<EntityType> entityTypes) {
    this.entityTypes = entityTypes;
    return this;
  }

  /**
   * Sets the {@link ComplexType}s for this {@link Schema}
   * @param complexTypes
   * @return {@link Schema} for method chaining
   */
  public Schema setComplexTypes(final List<ComplexType> complexTypes) {
    this.complexTypes = complexTypes;
    return this;
  }

  /**
   * Sets the {@link Association}s for this {@link Schema}
   * @param associations
   * @return {@link Schema} for method chaining
   */
  public Schema setAssociations(final List<Association> associations) {
    this.associations = associations;
    return this;
  }

  /**
   * Sets the {@link EntityContainer}s for this {@link Schema}
   * @param entityContainers
   * @return {@link Schema} for method chaining
   */
  public Schema setEntityContainers(final List<EntityContainer> entityContainers) {
    this.entityContainers = entityContainers;
    return this;
  }

  /**
   * Sets the List of {@link AnnotationAttribute} for this {@link Schema}
   * @param annotationAttributes
   * @return {@link Schema} for method chaining
   */
  public Schema setAnnotationAttributes(final List<AnnotationAttribute> annotationAttributes) {
    this.annotationAttributes = annotationAttributes;
    return this;
  }

  /**
   * Sets the List of {@link AnnotationElement} for this {@link Schema}
   * @param annotationElements
   * @return {@link Schema} for method chaining
   */
  public Schema setAnnotationElements(final List<AnnotationElement> annotationElements) {
    this.annotationElements = annotationElements;
    return this;
  }

  /**
   * @return <b>String</b> namespace of this {@link Schema}
   */
  public String getNamespace() {
    return namespace;
  }

  /**
   * @return <b>String</b> alias of this {@link Schema}
   */
  public String getAlias() {
    return alias;
  }

  /**
   * @return List<{@link Using}> of this {@link Schema}
   */
  public List<Using> getUsings() {
    return usings;
  }

  /**
   * @return List<{@link EntityType}> of this {@link Schema}
   */
  public List<EntityType> getEntityTypes() {
    return entityTypes;
  }

  public EntityType getEntityType(String name) {
    for (EntityType type: entityTypes) {
      if (name.equals(type.getName())) {
        return type;
      }
    }
    return null;
  }

  /**
   * @return List<{@link ComplexType}> of this {@link Schema}
   */
  public List<ComplexType> getComplexTypes() {
    return complexTypes;
  }

  /**
   * @return List<{@link Association}> of this {@link Schema}
   */
  public List<Association> getAssociations() {
    return associations;
  }

  /**
   * @return List<{@link EntityContainer}> of this {@link Schema}
   */
  public List<EntityContainer> getEntityContainers() {
    return entityContainers;
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
}
