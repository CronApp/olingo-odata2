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
package org.apache.olingo.odata2.client.core.edm.Impl;

import org.apache.olingo.odata2.api.edm.EdmContentKind;
import org.apache.olingo.odata2.api.edm.EdmCustomizableFeedMappings;

/**
 * Objects of this class represent customizable feed mappings.
 * 
 */
public class EdmCustomizableFeedMappingsImpl implements EdmCustomizableFeedMappings {

  private Boolean fcKeepInContent;
  private EdmContentKind fcContentKind;
  private String fcNsPrefix;
  private String fcNsUri;
  private String fcSourcePath;
  private String fcTargetPath;

  @Override
  public Boolean isFcKeepInContent() {
    return fcKeepInContent;
  }

  @Override
  public EdmContentKind getFcContentKind() {
    return fcContentKind;
  }

  @Override
  public String getFcNsPrefix() {
    return fcNsPrefix;
  }

  @Override
  public String getFcNsUri() {
    return fcNsUri;
  }

  @Override
  public String getFcSourcePath() {
    return fcSourcePath;
  }

  @Override
  public String getFcTargetPath() {
    return fcTargetPath;
  }

  /**
   * @return <b>boolean</b>
   */
  public Boolean getFcKeepInContent() {
    return fcKeepInContent;
  }

  /**
   * Sets if this is kept in content.
   * @param fcKeepInContent
   * @return {@link EdmCustomizableFeedMappingsImpl} for method chaining
   */
  public EdmCustomizableFeedMappingsImpl setFcKeepInContent(final Boolean fcKeepInContent) {
    this.fcKeepInContent = fcKeepInContent;
    return this;
  }

  /**
   * Sets the {@link EdmContentKind}.
   * @param fcContentKind
   * @return {@link EdmCustomizableFeedMappingsImpl} for method chaining
   */
  public EdmCustomizableFeedMappingsImpl setFcContentKind(final EdmContentKind fcContentKind) {
    this.fcContentKind = fcContentKind;
    return this;
  }

  /**
   * Sets the prefix.
   * @param fcNsPrefix
   * @return {@link EdmCustomizableFeedMappingsImpl} for method chaining
   */
  public EdmCustomizableFeedMappingsImpl setFcNsPrefix(final String fcNsPrefix) {
    this.fcNsPrefix = fcNsPrefix;
    return this;
  }

  /**
   * Sets the Uri.
   * @param fcNsUri
   * @return {@link EdmCustomizableFeedMappingsImpl} for method chaining
   */
  public EdmCustomizableFeedMappingsImpl setFcNsUri(final String fcNsUri) {
    this.fcNsUri = fcNsUri;
    return this;
  }

  /**
   * Sets the source path.
   * @param fcSourcePath
   * @return {@link EdmCustomizableFeedMappingsImpl} for method chaining
   */
  public EdmCustomizableFeedMappingsImpl setFcSourcePath(final String fcSourcePath) {
    this.fcSourcePath = fcSourcePath;
    return this;
  }

  /**
   * <p>Sets the target path.</p>
   * <p>For standard Atom elements, constants are available in {@link org.apache.olingo.odata2.api.edm.EdmTargetPath
   * EdmTargetPath}.</p>
   * @param fcTargetPath
   * @return {@link EdmCustomizableFeedMappingsImpl} for method chaining
   */
  public EdmCustomizableFeedMappingsImpl setFcTargetPath(final String fcTargetPath) {
    this.fcTargetPath = fcTargetPath;
    return this;
  }
  
  @Override
  public String toString() {
      return String.format(fcNsPrefix + " " + fcNsUri);
  }
}
