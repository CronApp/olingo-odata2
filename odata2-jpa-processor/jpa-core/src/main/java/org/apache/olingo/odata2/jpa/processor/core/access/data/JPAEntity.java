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
package org.apache.olingo.odata2.jpa.processor.core.access.data;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

import javax.persistence.Id;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.olingo.odata2.api.edm.*;
import org.apache.olingo.odata2.api.ep.entry.EntryMetadata;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.core.edm.AbstractSimpleType;
import org.apache.olingo.odata2.core.edm.provider.EdmSimplePropertyImplProv;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.OnJPAWriteContent;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmMapping;
import org.apache.olingo.odata2.jpa.processor.core.ODataJPAConfig;
import org.apache.olingo.odata2.jpa.processor.core.model.JPAEdmMappingImpl;

public class JPAEntity {

  private Object jpaEntity = null;
  private JPAEntity parentJPAEntity = null;
  private EdmEntityType oDataEntityType = null;
  private EdmEntitySet oDataEntitySet = null;
  private Class<?> jpaType = null;
  private HashMap<String, Method> accessModifiersWrite = null;
  private JPAEntityParser jpaEntityParser = null;
  private ODataJPAContext oDataJPAContext;
  private OnJPAWriteContent onJPAWriteContent = null;
  private List<String> relatedJPAEntityLink = new ArrayList<String>();
  public HashMap<String, List<Object>> relatedJPAEntityMap = null;
  private EdmNavigationProperty viaNavigationProperty;

  public JPAEntity(final EdmEntityType oDataEntityType, final EdmEntitySet oDataEntitySet,
      final ODataJPAContext context) {
    this.oDataEntityType = oDataEntityType;
    this.oDataEntitySet = oDataEntitySet;
    oDataJPAContext = context;
    try {
      JPAEdmMapping mapping = (JPAEdmMapping) oDataEntityType.getMapping();
      jpaType = mapping.getJPAType();
    } catch (EdmException e) {
      return;
    }
    jpaEntityParser = new JPAEntityParser(oDataJPAContext, null);
    onJPAWriteContent = oDataJPAContext.getODataContext().getServiceFactory().getCallback(OnJPAWriteContent.class);
  }

  public void setAccessModifersWrite(final HashMap<String, Method> accessModifiersWrite) {
    this.accessModifiersWrite = accessModifiersWrite;
  }

  public void setParentJPAEntity(final JPAEntity jpaEntity) {
    parentJPAEntity = jpaEntity;
  }

  public JPAEntity getParentJPAEntity() {
    return parentJPAEntity;
  }
  
  //TesteBuild5

  public Object getJPAEntity() {
    return jpaEntity;
  }

  public void setViaNavigationProperty(EdmNavigationProperty viaNavigationProperty) {
    this.viaNavigationProperty = viaNavigationProperty;
  }

  public EdmNavigationProperty getViaNavigationProperty() {
    return viaNavigationProperty;
  }

  public void create(final ODataEntry oDataEntry) throws ODataJPARuntimeException {

    if (oDataEntry == null) {
      throw ODataJPARuntimeException
          .throwException(ODataJPARuntimeException.GENERAL, null);
    }
    try {
      EntryMetadata entryMetadata = oDataEntry.getMetadata();
      Map<String, Object> oDataEntryProperties = oDataEntry.getProperties();
      if (oDataEntry.containsInlineEntry()) {
        normalizeInlineEntries(oDataEntryProperties);
      }

      if (oDataEntry.getProperties().size() > 0) {

        write(oDataEntryProperties, true);

        for (String navigationPropertyName : oDataEntityType.getNavigationPropertyNames()) {
          EdmNavigationProperty navProperty =
              (EdmNavigationProperty) oDataEntityType.getProperty(navigationPropertyName);
          if (relatedJPAEntityMap != null && relatedJPAEntityMap.containsKey(navigationPropertyName)) {
            oDataEntry.getProperties().get(navigationPropertyName);
            JPALink.linkJPAEntities(oDataJPAContext, relatedJPAEntityMap.get(navigationPropertyName), jpaEntity,
                navProperty);
            continue;
          }
          // The second condition is required to ensure that there is an explicit request to link
          // two entities. Else the third condition will always be true for cases where two navigations
          // point to same entity types.
          if (parentJPAEntity != null
              && navProperty.getRelationship().equals(getViaNavigationProperty().getRelationship())) {
            List<Object> targetJPAEntities = new ArrayList<Object>();
            targetJPAEntities.add(parentJPAEntity.getJPAEntity());
            JPALink.linkJPAEntities(oDataJPAContext, targetJPAEntities, jpaEntity, navProperty);
          } else if (!entryMetadata.getAssociationUris(navigationPropertyName).isEmpty()) {
            if (!relatedJPAEntityLink.contains(navigationPropertyName)) {
              relatedJPAEntityLink.add(navigationPropertyName);
            }
          }
        }
      }
      if (!relatedJPAEntityLink.isEmpty()) {
        JPALink link = new JPALink(oDataJPAContext);
        link.setSourceJPAEntity(jpaEntity);
        link.create(oDataEntitySet, oDataEntry, relatedJPAEntityLink);
      }
    } catch (EdmException e) {
      throw ODataJPARuntimeException
          .throwException(ODataJPARuntimeException.GENERAL
              .addContent(e.getMessage()), e);
    } catch (ODataJPAModelException e) {
      throw ODataJPARuntimeException
          .throwException(ODataJPARuntimeException.GENERAL
              .addContent(e.getMessage()), e);
    }
  }

  public EdmEntitySet getEdmEntitySet() {
    return oDataEntitySet;
  }

  public void create(final Map<String, Object> oDataEntryProperties) throws ODataJPARuntimeException {
    normalizeInlineEntries(oDataEntryProperties);
    write(oDataEntryProperties, true);
  }

  public void update(final ODataEntry oDataEntry) throws ODataJPARuntimeException {
    if (oDataEntry == null) {
      throw ODataJPARuntimeException
          .throwException(ODataJPARuntimeException.GENERAL, null);
    }
    Map<String, Object> oDataEntryProperties = oDataEntry.getProperties();
    if (oDataEntry.containsInlineEntry()) {
      normalizeInlineEntries(oDataEntryProperties);
    }
    write(oDataEntryProperties, false);
    //Removido código original do ODATApor:
    //1- Performance
    //2- Auto relacionamento não estava permitindo update, apenas insert
    /*
    JPALink link = new JPALink(oDataJPAContext);
    link.setSourceJPAEntity(jpaEntity);
    try {
      link.create(oDataEntitySet, oDataEntry, oDataEntityType.getNavigationPropertyNames());
    } catch (EdmException e) {
      throw ODataJPARuntimeException
          .throwException(ODataJPARuntimeException.GENERAL
              .addContent(e.getMessage()), e);
    } catch (ODataJPAModelException e) {
      throw ODataJPARuntimeException
          .throwException(ODataJPARuntimeException.GENERAL
              .addContent(e.getMessage()), e);
    }
    */
  }

  public void update(final Map<String, Object> oDataEntryProperties) throws ODataJPARuntimeException {
    normalizeInlineEntries(oDataEntryProperties);
    write(oDataEntryProperties, false);
  }

  public void setJPAEntity(final Object jpaEntity) {
    this.jpaEntity = jpaEntity;
  }

  protected void setComplexProperty(Method accessModifier, final Object jpaEntity,
      final EdmStructuralType edmComplexType, final HashMap<String, Object> propertyValue)
      throws EdmException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
      InstantiationException, ODataJPARuntimeException, NoSuchMethodException, SecurityException, SQLException {

    setComplexProperty(accessModifier, jpaEntity, edmComplexType, propertyValue, null);
  }

  protected void setProperty(final Method method, final Object entity, final Object entityPropertyValue,
      final EdmSimpleType type, boolean isNullable) throws
      IllegalAccessException, IllegalArgumentException, InvocationTargetException, ODataJPARuntimeException, 
      EdmException {

    setProperty(method, entity, entityPropertyValue, type, null, isNullable);
  }

  protected void setEmbeddableKeyProperty(final HashMap<String, String> embeddableKeys,
      final List<EdmProperty> oDataEntryKeyProperties,
      final Map<String, Object> oDataEntryProperties, final Object entity, final boolean isCreate)
      throws ODataJPARuntimeException, EdmException, IllegalAccessException, IllegalArgumentException,
      InvocationTargetException, InstantiationException {

    HashMap<String, Object> embeddableObjMap = new HashMap<String, Object>();
    List<EdmProperty> leftODataEntryKeyProperties = new ArrayList<EdmProperty>();
    HashMap<String, String> leftEmbeddableKeys = new HashMap<String, String>();
    final List<String> keyNames = oDataEntityType.getKeyPropertyNames();

    for (EdmProperty edmProperty : oDataEntryKeyProperties) {
      if (oDataEntryProperties.containsKey(edmProperty.getName()) == false) {
        continue;
      }

      if (isCreate == false) {
        if (keyNames.contains(edmProperty.getName())) {
          continue;
        }
      }

      String edmPropertyName = edmProperty.getName();
      String embeddableKeyNameComposite = embeddableKeys.get(edmPropertyName);
      if (embeddableKeyNameComposite == null) {
        continue;
      }
      String embeddableKeyNameSplit[] = embeddableKeyNameComposite.split("\\.");
      String methodPartName = null;
      Method method = null;
      Object embeddableObj = null;

      if (embeddableObjMap.containsKey(embeddableKeyNameSplit[0]) == false) {
        methodPartName = embeddableKeyNameSplit[0];
        method = jpaEntityParser.getAccessModifierSet(entity, methodPartName);
        embeddableObj = method.getParameterTypes()[0].newInstance();
        method.invoke(entity, embeddableObj);
        embeddableObjMap.put(embeddableKeyNameSplit[0], embeddableObj);
      } else {
        embeddableObj = embeddableObjMap.get(embeddableKeyNameSplit[0]);
      }

      if (embeddableKeyNameSplit.length == 2) {
        methodPartName = embeddableKeyNameSplit[1];
        method = jpaEntityParser.getAccessModifierSet(embeddableObj, methodPartName);
        Object simpleObj = oDataEntryProperties.get(edmProperty.getName());
        method.invoke(embeddableObj, simpleObj);
      } else if (embeddableKeyNameSplit.length > 2) { // Deeply nested
        leftODataEntryKeyProperties.add(edmProperty);
        leftEmbeddableKeys
            .put(edmPropertyName, embeddableKeyNameComposite.split(embeddableKeyNameSplit[0] + ".", 2)[1]);
      }
    }
  }

  protected Object instantiateJPAEntity() throws InstantiationException, IllegalAccessException {
    if (jpaType == null) {
      throw new InstantiationException();
    }

    return jpaType.newInstance();
  }

  private void normalizeInlineEntries(final Map<String, Object> oDataEntryProperties) throws ODataJPARuntimeException {
    List<ODataEntry> entries = null;
    try {
      for (String navigationPropertyName : oDataEntityType.getNavigationPropertyNames()) {
        Object inline = oDataEntryProperties.get(navigationPropertyName);
        if (inline instanceof ODataFeed) {
          entries = ((ODataFeed) inline).getEntries();
        } else if (inline instanceof ODataEntry) {
          entries = new ArrayList<ODataEntry>();
          entries.add((ODataEntry) inline);
        }
        if (entries != null) {
          oDataEntryProperties.put(navigationPropertyName, entries);
          entries = null;
        }
      }
    } catch (EdmException e) {
      throw ODataJPARuntimeException
          .throwException(ODataJPARuntimeException.GENERAL
              .addContent(e.getMessage()), e);
    }
  }

  @SuppressWarnings("unchecked")
  private void write(final Map<String, Object> oDataEntryProperties,
      final boolean isCreate)
      throws ODataJPARuntimeException {
    try {

      EdmStructuralType structuralType = null;
      final List<String> keyNames = oDataEntityType.getKeyPropertyNames();

      if (isCreate) {
        jpaEntity = instantiateJPAEntity();
      } else if (jpaEntity == null) {
        throw ODataJPARuntimeException
            .throwException(ODataJPARuntimeException.RESOURCE_NOT_FOUND, null);
      }

      if (accessModifiersWrite == null) {
        accessModifiersWrite =
            jpaEntityParser.getAccessModifiers(jpaEntity, oDataEntityType, JPAEntityParser.ACCESS_MODIFIER_SET);
      }

      if (oDataEntityType == null || oDataEntryProperties == null) {
        throw ODataJPARuntimeException
            .throwException(ODataJPARuntimeException.GENERAL, null);
      }

      final HashMap<String, String> embeddableKeys =
          jpaEntityParser.getJPAEmbeddableKeyMap(jpaEntity.getClass().getName());
      Set<String> propertyNames = null;
      if (embeddableKeys != null) {
        setEmbeddableKeyProperty(embeddableKeys, oDataEntityType.getKeyProperties(), oDataEntryProperties,
            jpaEntity, isCreate);

        propertyNames = new HashSet<String>();
        propertyNames.addAll(oDataEntryProperties.keySet());
        //for (String key : embeddableKeys.keySet()) {
        //propertyNames.remove(key);
        //}
      } else {
        propertyNames = oDataEntryProperties.keySet();
      }

      boolean isVirtual = false;
      Map<String, Object> created = new HashMap();

      for (String propertyName : propertyNames) {
        EdmTyped edmTyped = (EdmTyped) oDataEntityType.getProperty(propertyName);
        if (edmTyped instanceof EdmProperty) {
          isVirtual = ((JPAEdmMappingImpl)((EdmProperty) edmTyped).getMapping()).isVirtualAccess();
        } else {
          isVirtual = false;
        }
        Method accessModifier = null;

        switch (edmTyped.getType().getKind()) {
        case SIMPLE:
          EdmProperty edmProperty = (EdmProperty)oDataEntityType.getProperty(propertyName);
          if (isCreate == false) {
            if (keyNames.contains(edmTyped.getName()) || ((EdmSimplePropertyImplProv)edmProperty).getProperty().isOriginalId()) {
              continue;
            }
          }
          boolean isNullable = edmProperty.getFacets() == null ? (keyNames.contains(propertyName)? false : true)
              : edmProperty.getFacets().isNullable() == null ? true : edmProperty.getFacets().isNullable();
          if (((EdmSimplePropertyImplProv) edmProperty).getComposite() != null) {
            Map<String, Object> oDataEntryPropertiesComposite = new LinkedHashMap<String, Object>();
            String value = (String) oDataEntryProperties.get(propertyName);
            String[] values = value.split(ODataJPAConfig.COMPOSITE_SEPARATOR);
            int i = 0;
            for (EdmProperty p: ((EdmSimplePropertyImplProv)edmProperty).getComposite()) {
              if (isCreate == false && ((EdmSimplePropertyImplProv)p).getProperty().isOriginalId()) {
                i++;
                continue;
              }
              if (i < values.length) {
                Object valueObj = null;
                if (!((EdmSimplePropertyImplProv) p).getProperty().isForeignKey() && oDataEntryProperties.containsKey(p.getName())) {
                  valueObj = oDataEntryProperties.get(p.getName());
                } else {
                  if (!"null".equals(values[i])) {
                    valueObj = ((AbstractSimpleType) p.getType()).valueOfString(values[i], EdmLiteralKind.JSON, p.getFacets(), ((JPAEdmMappingImpl) p.getMapping()).getOriginaType());
                  }
                }
                oDataEntryPropertiesComposite.put(p.getName(), valueObj);
                setEntityValue(oDataEntryPropertiesComposite, created, p.getName(), p, null, isNullable, isCreate);
              }
              i++;
            }
            continue;
          }
          accessModifier = accessModifiersWrite.get(propertyName);
          if (isVirtual || accessModifier == null) {
            try {
              setProperty(accessModifier, jpaEntity, oDataEntryProperties.get(propertyName), (EdmSimpleType) edmTyped
                  .getType(), propertyName, isNullable);
            } catch(Exception e) {
              try {
                setProperty(accessModifier, jpaEntity, oDataEntryProperties.get(propertyName), (EdmSimpleType) edmTyped
                    .getType(), isNullable);
              } catch(Exception e3) {
                setEntityValue(oDataEntryProperties, created, propertyName, edmTyped, accessModifier, isNullable, isCreate);
              }
            }
          } else {
            setProperty(accessModifier, jpaEntity, oDataEntryProperties.get(propertyName), (EdmSimpleType) edmTyped
                .getType(), isNullable);
          }
          break;
        case COMPLEX:
          structuralType = (EdmStructuralType) edmTyped.getType();
          accessModifier = accessModifiersWrite.get(propertyName);
          if (isVirtual) {
            setComplexProperty(accessModifier, jpaEntity,
                structuralType,
                (HashMap<String, Object>) oDataEntryProperties.get(propertyName), propertyName);
          } else {
            setComplexProperty(accessModifier, jpaEntity,
                structuralType,
                (HashMap<String, Object>) oDataEntryProperties.get(propertyName));
          }
          break;
        case NAVIGATION:
        case ENTITY:
          if (isCreate) {
            structuralType = (EdmStructuralType) edmTyped.getType();
            EdmNavigationProperty navProperty = (EdmNavigationProperty) edmTyped;
            EdmEntitySet edmRelatedEntitySet = oDataEntitySet.getRelatedEntitySet(navProperty);
            List<ODataEntry> relatedEntries = (List<ODataEntry>) oDataEntryProperties.get(propertyName);
            if (relatedJPAEntityMap == null) {
              relatedJPAEntityMap = new HashMap<String, List<Object>>();
            }
            List<Object> relatedJPAEntities = new ArrayList<Object>();
            for (ODataEntry oDataEntry : relatedEntries) {
              JPAEntity relatedEntity =
                  new JPAEntity((EdmEntityType) structuralType, edmRelatedEntitySet, oDataJPAContext);
              relatedEntity.setParentJPAEntity(this);
              relatedEntity.setViaNavigationProperty(navProperty);
              relatedEntity.create(oDataEntry);
              if (oDataEntry.getProperties().size() == 0) {
                if (!oDataEntry.getMetadata().getUri().isEmpty()
                    && !relatedJPAEntityLink.contains(navProperty.getName())) {
                  relatedJPAEntityLink.add(navProperty.getName());
                }
              } else {
                relatedJPAEntities.add(relatedEntity.getJPAEntity());
              }
            }
            if (!relatedJPAEntities.isEmpty()) {
              relatedJPAEntityMap.put(navProperty.getName(), relatedJPAEntities);
            }
          }
        default:
          continue;
        }
      }
    } catch (Exception e) {
      if (e instanceof ODataJPARuntimeException) {
        throw (ODataJPARuntimeException) e;
      }
      throw ODataJPARuntimeException
          .throwException(ODataJPARuntimeException.GENERAL
              .addContent(e.getMessage()), e);
    }
  }

  private static int countStr(String someString, char someChar) {
    int count = 0;

    for (int i = 0; i < someString.length(); i++) {
      if (someString.charAt(i) == someChar) {
        count++;
      }
    }

    return count;
  }

  private void setEntityValue(Map<String, Object> oDataEntryProperties, Map<String, Object> created, String propertyName, EdmTyped edmTyped, Method accessModifier, boolean isNullable, boolean isCreate) throws EdmException {
    JPAEdmMappingImpl mapping = ((JPAEdmMappingImpl) ((EdmSimplePropertyImplProv) edmTyped).getMapping());

    String expression = mapping.getInternalExpression();
    int start = 1;
    if (expression == null && accessModifier == null) {
      expression = mapping.getInternalName();
      start = 0;
    }

    if (expression != null) {
      try {
        boolean changingMainType = true;
        if (countStr(expression, '.') > start) {
          changingMainType = false;
        }

        Object o = jpaEntity;
        Object current = o;
        Object lastObject = o;
        Method lastSet = null;
        Method mget = null;
        Method mset = null;
        String[] parts = expression.split("\\.");

        boolean canContinue = true;
        if (parts.length > 1) {
          Class clazz = jpaEntity.getClass();
          for (int i = start; i < parts.length; i++) {
            String p = parts[i];

            if (i == parts.length - 1) {
              Field f = ReflectionUtil.getField(clazz, p);
              if (f != null && !changingMainType) {
                canContinue = f.getAnnotation(Id.class) != null;
              }
            } else {
              mget = ReflectionUtil.getMethod(clazz, "get" + p);
              clazz = mget.getReturnType();
              current = mget.invoke(current);
            }
          }
        }

        if (canContinue) {
          String path = "";
          mset = null;
          boolean hasObject = false;
          for (int i = start; i < parts.length; i++) {
            String p = parts[i];

            if (!path.isEmpty()) {
              path += ".";
            }

            path += p;

            lastSet = mset;

            mget = ReflectionUtil.getMethod(o, "get" + p);
            mset = ReflectionUtil.getMethod(o, "set" + p);

            if (i < parts.length - 1) {
              lastObject = o;
              Object value = mget.invoke(o);

              if (value == null || (value != null && !created.containsKey(path))) {
                value = mget.getReturnType().newInstance();
                mset.invoke(o, value);
                created.put(path, value);
              }

              o = value;
              hasObject = true;
            }

            if (i == parts.length - 1) {
              Field f = ReflectionUtil.getField(o, p);
              if (oDataEntryProperties.get(propertyName) == null && f.getAnnotation(Id.class) != null && lastSet != null) {
                setProperty(lastSet, lastObject, null, (EdmSimpleType) edmTyped
                    .getType(), isNullable);
              } else {
                if (hasObject && f.getAnnotation(Id.class) == null && !changingMainType) {
                  continue;
                } else {
                  setProperty(mset, o, oDataEntryProperties.get(propertyName), (EdmSimpleType) edmTyped
                      .getType(), isNullable);
                }
              }
            }
          }
        }
      } catch(Exception e1) {

      }
    }


    //
  }

  @SuppressWarnings("unchecked")
  protected void setComplexProperty(Method accessModifier, final Object jpaEntity,
      final EdmStructuralType edmComplexType, final HashMap<String, Object> propertyValue, String propertyName)
      throws EdmException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
      InstantiationException, ODataJPARuntimeException, NoSuchMethodException, SecurityException, SQLException {

    JPAEdmMapping mapping = (JPAEdmMapping) edmComplexType.getMapping();
    Object embeddableObject = mapping.getJPAType().newInstance();
    if (propertyName != null) {
    	accessModifier.invoke(jpaEntity, propertyName, embeddableObject);
    } else {
    	accessModifier.invoke(jpaEntity, embeddableObject);
    }

    HashMap<String, Method> accessModifiers =
        jpaEntityParser.getAccessModifiers(embeddableObject, edmComplexType,
            JPAEntityParser.ACCESS_MODIFIER_SET);

    for (String edmPropertyName : edmComplexType.getPropertyNames()) {
      if (propertyValue != null) {
        EdmTyped edmTyped = edmComplexType.getProperty(edmPropertyName);
        accessModifier = accessModifiers.get(edmPropertyName);
        EdmType type = edmTyped.getType();
        if (type.getKind().toString().equals(EdmTypeKind.COMPLEX.toString())) {
          setComplexProperty(accessModifier, embeddableObject, (EdmStructuralType) type,
              (HashMap<String, Object>) propertyValue.get(edmPropertyName), propertyName);
        } else {
          EdmSimpleType simpleType = (EdmSimpleType) type;
          EdmProperty edmProperty = (EdmProperty)edmComplexType.getProperty(edmPropertyName);
          boolean isNullable = edmProperty.getFacets() == null ? true
              : edmProperty.getFacets().isNullable() == null ? true : edmProperty.getFacets().isNullable();
    		  if (propertyName != null) {
            setProperty(accessModifier, embeddableObject, propertyValue.get(edmPropertyName),
                simpleType, isNullable);
          } else {
            setProperty(accessModifier, embeddableObject, propertyValue.get(edmPropertyName),
                simpleType, isNullable);
          }
        }
      }
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected void setProperty(final Method method, final Object entity, final Object entityPropertyValue,
      final EdmSimpleType type, String propertyName, boolean isNullable) throws
      IllegalAccessException, IllegalArgumentException, InvocationTargetException, ODataJPARuntimeException, 
      EdmException {

    if (method == null) {
      throw new RuntimeException("Null");
    }

    if (entityPropertyValue != null || isNullable) {
      if (propertyName != null) {
        if (entityPropertyValue == null) {
          try {
            method.invoke(entity, propertyName, new Object[]{null});
          } catch(Exception e) {
            //abafa
          }
        }
        else {
          method.invoke(entity, propertyName, entityPropertyValue);
        }
        return;
      }
      Class<?> parameterType = method.getParameterTypes()[0];
      if (entityPropertyValue == null) {
        try {
          method.invoke(entity, new Object[]{null});
        } catch(Exception e) {
          //abafa
        }
      }
      else if (type != null && type.getDefaultType().equals(Short.class) && parameterType.equals(Byte.class)) {
        method.invoke(entity, ((Short)entityPropertyValue).byteValue());
      }
      else if (type != null && type.getDefaultType().equals(String.class)) {
        if (parameterType.equals(String.class)) {
          method.invoke(entity, entityPropertyValue);
        } else if (parameterType.equals(char[].class)) {
          char[] characters = entityPropertyValue != null ? ((String) entityPropertyValue).toCharArray() : null;
          method.invoke(entity, characters);
        } else if (parameterType.equals(char.class)) {
          char c = entityPropertyValue != null ? ((String) entityPropertyValue).charAt(0) : '\u0000';
          method.invoke(entity, c);
        } else if (parameterType.equals(Character[].class)) {
          Character[] characters = entityPropertyValue != null ? 
              JPAEntityParser.toCharacterArray((String) entityPropertyValue) : null;
          method.invoke(entity, (Object) characters);
        } else if (parameterType.equals(Character.class)) {
          Character c = entityPropertyValue != null ? 
              Character.valueOf(((String) entityPropertyValue).charAt(0)) : null;
          method.invoke(entity, c);
        } else if (parameterType.isEnum()) {
          Enum e = entityPropertyValue != null ?
              Enum.valueOf((Class<Enum>) parameterType, (String) entityPropertyValue) : null;
          method.invoke(entity, e);
        } else {
          String setterName = method.getName();
      	  String getterName = setterName.replace("set", "get");
      	  try {
            Method getMethod = entity.getClass().getDeclaredMethod(getterName);
            if(getMethod.isAnnotationPresent(XmlJavaTypeAdapter.class)) {
              XmlAdapter xmlAdapter = getMethod.getAnnotation(XmlJavaTypeAdapter.class)
                  .value().newInstance();
              method.invoke(entity, xmlAdapter.unmarshal(entityPropertyValue));
            }
          } catch (Exception e) {
            throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.GENERAL, e);
      	  }
        }
      } else if (parameterType.equals(Blob.class)) {
        if (onJPAWriteContent == null) {
          throw ODataJPARuntimeException
              .throwException(ODataJPARuntimeException.ERROR_JPA_BLOB_NULL, null);
        } else {
          method.invoke(entity, entityPropertyValue != null ? 
              onJPAWriteContent.getJPABlob((byte[]) entityPropertyValue) : null);
        }
      } else if (parameterType.equals(Clob.class)) {
        if (onJPAWriteContent == null) {
          throw ODataJPARuntimeException
              .throwException(ODataJPARuntimeException.ERROR_JPA_CLOB_NULL, null);
        } else {
          method.invoke(entity, entityPropertyValue != null ? 
              onJPAWriteContent.getJPAClob(((String) entityPropertyValue).toCharArray()) : null);
        }
      } else if (parameterType.equals(Timestamp.class)) {
        Timestamp ts = entityPropertyValue != null ? 
            new Timestamp(((Calendar) entityPropertyValue).getTimeInMillis()) : null;
        method.invoke(entity, ts);
      } else if (parameterType.equals(java.util.Date.class)) {
        if (entityPropertyValue instanceof Date) {
          method.invoke(entity, (Date) entityPropertyValue);
        } else {
          Date d = entityPropertyValue != null ? ((Calendar) entityPropertyValue).getTime() : null;
          method.invoke(entity, d);
        }
      } else if (parameterType.equals(java.sql.Date.class)) {
        java.sql.Date d = entityPropertyValue != null ? 
            new java.sql.Date(((Calendar) entityPropertyValue).getTimeInMillis()) : null;
        method.invoke(entity, d);
      } else if (parameterType.equals(java.sql.Time.class)) {
        java.sql.Time t = entityPropertyValue != null ? 
            new java.sql.Time(((Calendar) entityPropertyValue).getTimeInMillis()) : null;
        method.invoke(entity, t);
      } else {
        method.invoke(entity, entityPropertyValue);
      }
    }
  }
}
