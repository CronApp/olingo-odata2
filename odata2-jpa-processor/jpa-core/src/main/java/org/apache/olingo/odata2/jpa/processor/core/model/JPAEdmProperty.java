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
package org.apache.olingo.odata2.jpa.processor.core.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.*;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.ComplexProperty;
import org.apache.olingo.odata2.api.edm.provider.ComplexType;
import org.apache.olingo.odata2.api.edm.provider.Facets;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAEdmBuilder;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAEdmMappingModelAccess;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmAssociationEndView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmAssociationView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmComplexPropertyView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmComplexTypeView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmEntityTypeView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmKeyView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmMapping;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmNavigationPropertyView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmPropertyView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmReferentialConstraintView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmSchemaView;
import org.apache.olingo.odata2.jpa.processor.core.ODataJPAConfig;
import org.apache.olingo.odata2.jpa.processor.core.access.model.JPAEdmNameBuilder;
import org.apache.olingo.odata2.jpa.processor.core.access.model.JPATypeConverter;

public class JPAEdmProperty extends JPAEdmBaseViewImpl implements
    JPAEdmPropertyView, JPAEdmComplexPropertyView {

  private JPAEdmSchemaView schemaView;
  private JPAEdmEntityTypeView entityTypeView;
  private JPAEdmComplexTypeView complexTypeView;
  private JPAEdmNavigationPropertyView navigationPropertyView = null;

  private JPAEdmKeyView keyView;
  private List<Property> properties;
  private SimpleProperty currentSimpleProperty = null;
  private ComplexProperty currentComplexProperty = null;
  private Attribute<?, ?> currentAttribute;
  private Attribute<?, ?> currentRefAttribute;
  private boolean isBuildModeComplexType;
  private Map<String, Integer> associationCount;
  private ArrayList<String[]> joinColumnNames = null;
  private List<SimpleProperty> joinProperties = null;
  private int totaJoinColumns = 0;
  private JPAEdmBuilder keyViewBuilder;

  public JPAEdmProperty(final JPAEdmSchemaView view) {
    super(view);
    schemaView = view;
    entityTypeView = schemaView.getJPAEdmEntityContainerView()
        .getJPAEdmEntitySetView().getJPAEdmEntityTypeView();
    complexTypeView = schemaView.getJPAEdmComplexTypeView();
    navigationPropertyView = new JPAEdmNavigationProperty(schemaView);
    isBuildModeComplexType = false;
    associationCount = new HashMap<String, Integer>();
  }

  public JPAEdmProperty(final JPAEdmSchemaView schemaView,
                        final JPAEdmComplexTypeView view) {
    super(view);
    this.schemaView = schemaView;
    complexTypeView = view;
    isBuildModeComplexType = true;
  }

  @Override
  public JPAEdmBuilder getBuilder() {
    if (builder == null) {
      builder = new JPAEdmPropertyBuilder();
    }

    return builder;
  }

  @Override
  public List<Property> getEdmPropertyList() {
    return properties;
  }

  @Override
  public JPAEdmKeyView getJPAEdmKeyView() {
    return keyView;
  }

  @Override
  public SimpleProperty getEdmSimpleProperty() {
    return currentSimpleProperty;
  }

  @Override
  public Attribute<?, ?> getJPAAttribute() {
    return currentAttribute;
  }

  @Override
  public Attribute<?, ?> getJPAReferencedAttribute() {
    return currentRefAttribute;
  }

  @Override
  public ComplexProperty getEdmComplexProperty() {
    return currentComplexProperty;
  }

  @Override
  public JPAEdmNavigationPropertyView getJPAEdmNavigationPropertyView() {
    return navigationPropertyView;
  }

  @Override
  public List<String[]> getJPAJoinColumns() {
    return joinColumnNames;
  }

  private class JPAEdmPropertyBuilder implements JPAEdmBuilder {
    /*
     *
     * Each call to build method creates a new EDM Property List.
     * The Property List can be created either by an Entity type or
     * ComplexType. The flag isBuildModeComplexType tells if the
     * Properties are built for complex type or for Entity Type.
     *
     * While Building Properties Associations are built. However
     * the associations thus built does not contain Referential
     * constraint. Associations thus built only contains
     * information about Referential constraints. Adding of
     * referential constraints to Associations is the taken care
     * by Schema.
     *
     * Building Properties is divided into four parts
     * A) Building Simple Properties
     * B) Building Complex Properties
     * C) Building Associations
     * D) Building Navigation Properties
     *
     * ************************************************************
     * Build EDM Schema - STEPS
     * ************************************************************
     * A) Building Simple Properties:
     *
     * 1) Fetch JPA Attribute List from
     * A) Complex Type
     * B) Entity Type
     * depending on isBuildModeComplexType.
     * B) Building Complex Properties
     * C) Building Associations
     * D) Building Navigation Properties
     *
     * ************************************************************
     * Build EDM Schema - STEPS
     * ************************************************************
     */
    @Override
    public void build() throws ODataJPAModelException, ODataJPARuntimeException {
      keyViewBuilder = null;
      properties = new ArrayList<Property>();

      List<Attribute<?, ?>> jpaAttributes = null;
      String currentEntityName = null;
      String targetEntityName = null;
      String entityTypeName = null;
      if (isBuildModeComplexType) {
        jpaAttributes = sortInAscendingOrder(complexTypeView.getJPAEmbeddableType().getAttributes(), complexTypeView.getJPAEmbeddableType().getJavaType());
        entityTypeName = complexTypeView.getJPAEmbeddableType().getJavaType().getSimpleName();
      } else {
        jpaAttributes = sortInAscendingOrder(entityTypeView.getJPAEntityType().getAttributes(), entityTypeView.getJPAEntityType().getJavaType());
        entityTypeName = entityTypeView.getJPAEntityType().getName();
      }

      List<SimpleProperty> objectKey = new LinkedList<SimpleProperty>();
      for (Object jpaAttribute : jpaAttributes) {
        currentAttribute = (Attribute<?, ?>) jpaAttribute;

        // Check for need to Exclude
        if (isExcluded((JPAEdmPropertyView) JPAEdmProperty.this, entityTypeName, currentAttribute.getName())) {
          continue;
        }

        PersistentAttributeType attributeType = currentAttribute.getPersistentAttributeType();

        switch (attributeType) {
          case BASIC:
            currentSimpleProperty = new SimpleProperty();
            properties.add(buildSimpleProperty(currentAttribute, currentSimpleProperty));
            break;
          case EMBEDDED:
            ComplexType complexType = complexTypeView.searchEdmComplexType(currentAttribute.getJavaType().getName());

            if (complexType == null) {
              JPAEdmComplexTypeView complexTypeViewLocal = new JPAEdmComplexType(schemaView, currentAttribute);
              complexTypeViewLocal.getBuilder().build();
              complexType = complexTypeViewLocal.getEdmComplexType();
              complexTypeView.addJPAEdmCompleTypeView(complexTypeViewLocal);
            }

            if (isBuildModeComplexType == false && entityTypeView.getJPAEntityType().getIdType().getJavaType()
                .equals(currentAttribute.getJavaType())) {

              if (keyView == null) {
                keyView = new JPAEdmKey(complexTypeView, JPAEdmProperty.this);
              }
              keyView.getBuilder().build();
              complexTypeView.expandEdmComplexType(complexType, properties, currentAttribute.getName());
            } else {
              currentComplexProperty = new ComplexProperty();
              if (isBuildModeComplexType) {
                JPAEdmNameBuilder.build((JPAEdmComplexPropertyView) JPAEdmProperty.this,
                    complexTypeView.getJPAEmbeddableType().getJavaType().getSimpleName());
              } else {
                JPAEdmNameBuilder
                    .build((JPAEdmComplexPropertyView) JPAEdmProperty.this, JPAEdmProperty.this, skipDefaultNaming);
              }
              currentComplexProperty
                  .setType(new FullQualifiedName(schemaView.getEdmSchema().getNamespace(), complexType.getName()));

              properties.add(currentComplexProperty);
              if (!complexTypeView.isReferencedInKey(currentComplexProperty.getType().getName())) {
                complexTypeView.setReferencedInKey(currentComplexProperty.getType().getName());
              }
            }

            break;
          case MANY_TO_MANY:
          case ONE_TO_MANY:
          case ONE_TO_ONE:
          case MANY_TO_ONE:

            if (attributeType.equals(PersistentAttributeType.MANY_TO_ONE) || attributeType
                .equals(PersistentAttributeType.ONE_TO_ONE)) {
              addForeignKey(currentAttribute);
            }

            JPAEdmAssociationEndView associationEndView = new JPAEdmAssociationEnd(entityTypeView, JPAEdmProperty.this);
            associationEndView.getBuilder().build();
            JPAEdmAssociationView associationView = schemaView.getJPAEdmAssociationView();
            if (associationView.searchAssociation(associationEndView) == null) {
              int count = associationView.getNumberOfAssociationsWithSimilarEndPoints(associationEndView);
              JPAEdmAssociationView associationViewLocal =
                  new JPAEdmAssociation(associationEndView, entityTypeView, JPAEdmProperty.this, count);
              associationViewLocal.getBuilder().build();
              associationView.addJPAEdmAssociationView(associationViewLocal, associationEndView);
            }

            if (attributeType.equals(PersistentAttributeType.MANY_TO_ONE) || attributeType
                .equals(PersistentAttributeType.ONE_TO_ONE)) {

              JPAEdmReferentialConstraintView refConstraintView =
                  new JPAEdmReferentialConstraint(associationView, entityTypeView, JPAEdmProperty.this);
              refConstraintView.getBuilder().build();

              if (refConstraintView.isExists()) {
                associationView.addJPAEdmRefConstraintView(refConstraintView);
              }
            }
            if (navigationPropertyView == null) {
              navigationPropertyView = new JPAEdmNavigationProperty(schemaView);
            }
            currentEntityName = entityTypeView.getJPAEntityType().getName();

            if (currentAttribute.isCollection()) {
              targetEntityName =
                  ((PluralAttribute<?, ?, ?>) currentAttribute).getElementType().getJavaType().getSimpleName();
            } else {
              targetEntityName = currentAttribute.getJavaType().getSimpleName();
            }
            Integer sequenceNumber = associationCount.get(currentEntityName + targetEntityName);
            if (sequenceNumber == null) {
              sequenceNumber = new Integer(1);
            } else {
              sequenceNumber = new Integer(sequenceNumber.intValue() + 1);
            }
            associationCount.put(currentEntityName + targetEntityName, sequenceNumber);
            JPAEdmNavigationPropertyView localNavigationPropertyView =
                new JPAEdmNavigationProperty(associationView, JPAEdmProperty.this, sequenceNumber.intValue());
            localNavigationPropertyView.getBuilder().build();
            navigationPropertyView.addJPAEdmNavigationPropertyView(localNavigationPropertyView);
            break;
          default:
            break;
        }

        if ((attributeType == PersistentAttributeType.BASIC
            || attributeType == PersistentAttributeType.MANY_TO_MANY
            || attributeType == PersistentAttributeType.ONE_TO_MANY
            || attributeType == PersistentAttributeType.ONE_TO_ONE
            || attributeType == PersistentAttributeType.MANY_TO_ONE)
            && (currentAttribute instanceof SingularAttribute && ((SingularAttribute<?, ?>) currentAttribute).isId())) {

          objectKey.add(currentSimpleProperty);
        }
      }

      SimpleProperty compositeProperty = new SimpleProperty();
      currentSimpleProperty = compositeProperty;
      JPAEdmNameBuilder.build(JPAEdmProperty.this, false, true, false);
      compositeProperty.setType(EdmSimpleTypeKind.String);
      Facets facets = new Facets();
      compositeProperty.setFacets(facets);
      compositeProperty.setName(ODataJPAConfig.COMPOSITE_KEY_NAME);
      ((JPAEdmMappingImpl) compositeProperty.getMapping()).setJPAType(String.class);
      for (SimpleProperty p : objectKey) {
        p.setOriginalId(true);
        if (p.getComposite() != null) {
          for (Property composite : p.getComposite()) {
            compositeProperty.addComposite(composite);
            composite.setForeignKey(true);
            composite.setOriginalId(true);
          }
        } else {
          compositeProperty.addComposite(p);
          p.setOriginalId(true);
          p.setForeignKey(false);
          p.setOriginalName(p.getName());
        }
      }

      properties.add(compositeProperty);

      if (keyView == null) {
        keyView = new JPAEdmKey(JPAEdmProperty.this);
        keyViewBuilder = keyView.getBuilder();
      }

      if (keyViewBuilder != null) {
        keyViewBuilder.build();
      }

    }

    private SimpleProperty buildSimpleProperty(final Attribute<?, ?> jpaAttribute, final SimpleProperty simpleProperty)
        throws ODataJPAModelException, ODataJPARuntimeException {
      return buildSimpleProperty(jpaAttribute, simpleProperty, null);
    }

    private SimpleProperty buildSimpleProperty(final Attribute<?, ?> jpaAttribute, final SimpleProperty simpleProperty,
                                               final JoinColumn joinColumn)
        throws ODataJPAModelException, ODataJPARuntimeException {

      boolean isForeignKey = joinColumn != null;
      JPAEdmNameBuilder.build(JPAEdmProperty.this, isBuildModeComplexType, skipDefaultNaming, isForeignKey);
      EdmSimpleTypeKind simpleTypeKind = JPATypeConverter
          .convertToEdmSimpleType(jpaAttribute
              .getJavaType(), jpaAttribute);
      simpleProperty.setType(simpleTypeKind);
      Facets facets = JPAEdmFacets.createAndSet(jpaAttribute, simpleProperty);
      if (isForeignKey) {
        facets.setNullable(joinColumn.nullable());
        String path = joinColumnNames.get(joinColumnNames.size() - 1)[1];
        if (totaJoinColumns > 1) {
          simpleProperty.setName(simpleProperty.getName() + "_" + path.replace(".", "_"));
        }
      }

      int total = 0;
      String name = simpleProperty.getName();
      for (Property property : properties) {
        if (property.getName().equals(name)) {
          total++;
          name = simpleProperty.getName() + "_" + total;
        }
      }

      if (total > 0) {
        simpleProperty.setName(name);
      }

      ((JPAEdmMapping) simpleProperty.getMapping()).setOriginalType(jpaAttribute.getJavaType());

      return simpleProperty;

    }

    private void addForeignKey(final Attribute<?, ?> jpaAttribute) throws ODataJPAModelException,
        ODataJPARuntimeException {

      AnnotatedElement annotatedElement = (AnnotatedElement) jpaAttribute.getJavaMember();
      joinColumnNames = null;
      totaJoinColumns = 0;
      int joinColumnIndex = -1;
      if (joinProperties == null) {
        joinProperties = new LinkedList<SimpleProperty>();
      }
      joinProperties.clear();
      if (annotatedElement == null) {
        return;
      }
      JoinColumn joinColumn = annotatedElement.getAnnotation(JoinColumn.class);
      if (joinColumn == null) {
        JoinColumns joinColumns = annotatedElement.getAnnotation(JoinColumns.class);
        if (joinColumns != null) {
          totaJoinColumns = joinColumns.value().length;
          SimpleProperty compositeProperty = null;
          if (joinColumns.value().length > 1) {
            compositeProperty = new SimpleProperty();
            currentSimpleProperty = compositeProperty;
            JPAEdmNameBuilder.build(JPAEdmProperty.this, false, true, false);
            EdmSimpleTypeKind simpleTypeKind = JPATypeConverter.convertToEdmSimpleType(String.class, jpaAttribute);
            compositeProperty.setType(simpleTypeKind);
            JPAEdmFacets.createAndSet(jpaAttribute, compositeProperty);
            compositeProperty.setName(jpaAttribute.getName());
            ((JPAEdmMappingImpl) compositeProperty.getMapping()).setJPAType(String.class);
            compositeProperty.setOriginalType(jpaAttribute.getJavaType());
            properties.add(compositeProperty);
          }
          for (JoinColumn jc : joinColumns.value()) {
            joinColumnIndex++;
            SimpleProperty p = buildForeignKey(jc, jpaAttribute, joinColumnIndex);
            if (ODataJPAConfig.EXPAND_COMPOSITE_KEYS) {
              properties.add(p);
            }
            if (compositeProperty != null) {
              compositeProperty.addComposite(p);
              currentSimpleProperty = compositeProperty;
            }
          }
        }
      } else {
        totaJoinColumns = 1;
        joinColumnIndex++;
        SimpleProperty p = buildForeignKey(joinColumn, jpaAttribute, joinColumnIndex);
        currentSimpleProperty.setOriginalType(jpaAttribute.getJavaType());
        properties.add(currentSimpleProperty);
      }
    }

    private SimpleProperty buildForeignKey(final JoinColumn joinColumn, final Attribute<?, ?> jpaAttribute, int joinColumnIndex)
        throws ODataJPAModelException,
        ODataJPARuntimeException {
      joinColumnNames = joinColumnNames == null ? new ArrayList<String[]>() : joinColumnNames;
      String[] name = {null, null};
      name[0] = "".equals(joinColumn.name()) == true ? jpaAttribute.getName() : joinColumn.name();

      EntityType<?> referencedEntityType = null;
      if (jpaAttribute.isCollection()) {
        referencedEntityType =
            metaModel.entity(((PluralAttribute<?, ?, ?>) currentAttribute).getElementType().getJavaType());
      } else {
        referencedEntityType = metaModel.entity(jpaAttribute.getJavaType());
      }

      if ("".equals(joinColumn.referencedColumnName())) {
        for (Attribute<?, ?> referencedAttribute : referencedEntityType.getAttributes()) {
          if (referencedAttribute.getPersistentAttributeType() == PersistentAttributeType.BASIC &&
              ((SingularAttribute<?, ?>) referencedAttribute).isId()) {
            AnnotatedElement annotatedElement = (AnnotatedElement) referencedAttribute.getJavaMember();
            Column referencedColumn = null;
            if (annotatedElement != null) {
              referencedColumn = annotatedElement.getAnnotation(Column.class);
            }
            if (referencedColumn != null) {
              name[1] = referencedColumn.name();
            } else {
              name[1] = referencedAttribute.getName();
            }
            joinColumnNames.add(name);
            currentRefAttribute = referencedAttribute;
            break;
          }
        }
      } else {
        List<Attribute<?, ?>> refPath = findRef(referencedEntityType, joinColumn);
        if (refPath.size() > 0) {
          name[1] = toString(refPath);
          joinColumnNames.add(name);
          currentRefAttribute = refPath.get(refPath.size() - 1);
        }
      }

      if (currentRefAttribute == null) {
        throw ODataJPAModelException.throwException(ODataJPAModelException.REF_ATTRIBUTE_NOT_FOUND
            .addContent(joinColumn.referencedColumnName() + " -> " + referencedEntityType.getName()), null);
      }
      currentSimpleProperty = new SimpleProperty();
      currentSimpleProperty.setOriginalName(jpaAttribute.getName());
      currentSimpleProperty.setIndex(joinColumnIndex);
      currentSimpleProperty.setForeignKey(true);
      buildSimpleProperty(currentRefAttribute, currentSimpleProperty, joinColumn);

      return currentSimpleProperty;
    }

    private String toString(List<Attribute<?, ?>> refPath) {
      String path = "";
      for (Attribute<?, ?> attr : refPath) {
        if (!path.isEmpty()) {
          path += ".";
        }
        path += attr.getName();
      }

      return path;
    }

    private List<Attribute<?, ?>> findRef(EntityType<?> referencedEntityType, JoinColumn joinColumn) {
      List<Attribute<?, ?>> refPath = new LinkedList<Attribute<?, ?>>();
      findPath(refPath, referencedEntityType, joinColumn.referencedColumnName());
      return refPath;
    }

    private void findPath(List<Attribute<?, ?>> refPath, EntityType<?> referencedEntityType, String name) {

      for (Attribute<?, ?> referencedAttribute : referencedEntityType.getAttributes()) {
        AnnotatedElement annotatedElement2 = (AnnotatedElement) referencedAttribute.getJavaMember();
        if (annotatedElement2 != null) {
          String refColName = getReferenceColumnName(annotatedElement2, referencedAttribute);
          if (refColName.equals((name))) {
            refPath.add(referencedAttribute);
            return;
          }
        }
      }

      for (Attribute<?, ?> referencedAttribute : referencedEntityType.getAttributes()) {

        AnnotatedElement annotatedElement = (AnnotatedElement) referencedAttribute.getJavaMember();
        if (annotatedElement == null) {
          continue;
        }

        JoinColumn joinColumn = annotatedElement.getAnnotation(JoinColumn.class);
        if (joinColumn == null) {
          JoinColumns joinColumns = annotatedElement.getAnnotation(JoinColumns.class);
          if (joinColumns != null) {
            for (JoinColumn jc : joinColumns.value()) {
              if (jc.name().equals(name)) {
                refPath.add(referencedAttribute);
                findPath(refPath, metaModel.entity(referencedAttribute.getJavaType()), jc.referencedColumnName());
                break;
              }
            }
          }
        } else {
          if (joinColumn.name().equals(name)) {
            refPath.add(referencedAttribute);
            findPath(refPath, metaModel.entity(referencedAttribute.getJavaType()), joinColumn.referencedColumnName());
          }
        }
      }
    }

    private Attribute<?, ?> findField(final Set<?> jpaAttributes, String name) {
      for (Object o : jpaAttributes) {
        if (((Attribute<?, ?>) o).getName().equals(name)) {
          return ((Attribute<?, ?>) o);
        }
      }

      return null;
    }

    @SuppressWarnings("rawtypes")
    private List<Attribute<?, ?>> sortInAscendingOrder(final Set<?> jpaAttributes, Class clazz) {
      List<Attribute<?, ?>> jpaAttributeList = new ArrayList<Attribute<?, ?>>();
      Field[] fields = clazz.getDeclaredFields();

      for (Field field : fields) {
        Attribute<?, ?> attr = findField(jpaAttributes, field.getName());
        if (attr != null) {
          jpaAttributeList.add(attr);
        }
      }

      return jpaAttributeList;
    }
  }

  private String getReferenceColumnName(AnnotatedElement annotatedElement2, Attribute<?, ?> referencedAttribute) {
    String refColName = null;
    Column c = annotatedElement2.getAnnotation(Column.class);
    if (c != null) {
      refColName = c.name();
    }
    return refColName == null || "".equals(refColName)
        ? referencedAttribute.getName()
        : refColName;
  }

  @Override
  public JPAEdmEntityTypeView getJPAEdmEntityTypeView() {
    return entityTypeView;
  }

  @Override
  public JPAEdmComplexTypeView getJPAEdmComplexTypeView() {
    return complexTypeView;
  }

  private boolean isExcluded(final JPAEdmPropertyView jpaEdmPropertyView, final String jpaEntityTypeName,
                             final String jpaAttributeName) {
    JPAEdmMappingModelAccess mappingModelAccess = jpaEdmPropertyView
        .getJPAEdmMappingModelAccess();
    boolean isExcluded = false;
    if (mappingModelAccess != null && mappingModelAccess.isMappingModelExists()) {
      // Exclusion of a simple property in a complex type
      if (isBuildModeComplexType
          && mappingModelAccess.checkExclusionOfJPAEmbeddableAttributeType(jpaEntityTypeName, jpaAttributeName)
          // Exclusion of a simple property of an Entity Type
          || (!isBuildModeComplexType && mappingModelAccess.checkExclusionOfJPAAttributeType(jpaEntityTypeName,
          jpaAttributeName))) {
        isExcluded = true;
      }
    }
    return isExcluded;
  }
}
