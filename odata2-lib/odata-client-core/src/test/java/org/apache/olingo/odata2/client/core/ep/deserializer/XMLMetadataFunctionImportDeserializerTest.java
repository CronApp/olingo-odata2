package org.apache.olingo.odata2.client.core.ep.deserializer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmAnnotationAttribute;
import org.apache.olingo.odata2.api.edm.EdmAnnotationElement;
import org.apache.olingo.odata2.api.edm.EdmAnnotations;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmParameter;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.client.api.edm.EdmDataServices;
import org.apache.olingo.odata2.client.api.edm.EdmSchema;
import org.junit.Test;

public class XMLMetadataFunctionImportDeserializerTest {
  private static final String NAMESPACE = "RefScenario";
  private static final String NAMESPACE2 = "RefScenario2";
  
  private final String[] propertyNames = { "EmployeeId", "EmployeeName", "Location" };
  
  @Test
  public void testFunctionImport() throws XMLStreamException, 
  EntityProviderException, EdmException, UnsupportedEncodingException {
    final String xmWithEntityContainer =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\""
            + Edm.NAMESPACE_EDMX_2007_06
            + "\">"
            + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\""
            + Edm.NAMESPACE_M_2007_08
            + "\">"
            + "<Schema Namespace=\""
            + NAMESPACE
            + "\" xmlns=\""
            + Edm.NAMESPACE_EDM_2008_09
            + "\">"
            + "<EntityType Name= \"Employee\" m:HasStream=\"true\">"
            + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>"
            + "<Property Name=\""
            + propertyNames[0]
            + "\" Type=\"Edm.String\" Nullable=\"false\"/>"
            + "<Property Name=\""
            + propertyNames[1]
            + "\" Type=\"Edm.String\" m:FC_TargetPath=\"SyndicationTitle\"/>"
            + "</EntityType>"
            + "<EntityContainer Name=\"Container1\" m:IsDefaultEntityContainer=\"true\">"
            + "<EntitySet Name=\"Employees\" EntityType=\"RefScenario.Employee\"/>"
            + "<FunctionImport Name=\"EmployeeSearch\" ReturnType=\"Collection(RefScenario.Employee)\" " +
            "EntitySet=\"Employees\" m:HttpMethod=\"GET\">"
            + "<Parameter Name=\"q1\" Type=\"Edm.String\" Nullable=\"true\" />"
            + "<Parameter Name=\"q2\" Type=\"Edm.Int32\" Nullable=\"false\" />"
            + "</FunctionImport>"
            + "<FunctionImport Name=\"RoomSearch\" ReturnType=\"Collection(RefScenario.Employee)\" " +
            "EntitySet=\"Employees\" m:HttpMethod=\"GET\">"
            + "<Parameter Name=\"q1\" Type=\"Edm.String\" Nullable=\"true\" Mode=\"In\"/>"
            + "<Parameter Name=\"q2\" Type=\"Edm.Int32\" Nullable=\"false\" />"
            + "</FunctionImport>"
            + "<FunctionImport Name=\"NoParamters\" ReturnType=\"Collection(RefScenario.Employee)\" " +
            "EntitySet=\"Employees\" m:HttpMethod=\"GET\">"
            + "</FunctionImport>"
            + "<FunctionImport Name=\"NoReturn\" " +
            "EntitySet=\"Rooms\" m:HttpMethod=\"GET\"/>"
            + "<FunctionImport Name=\"SingleRoomReturnType\" ReturnType=\"RefScenario.Employee\" " +
            "EntitySet=\"Employees\" m:HttpMethod=\"GET\">"
            + "</FunctionImport>"
            + "</EntityContainer>" + "</Schema>" + "</edmx:DataServices>" + "</edmx:Edmx>";
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmWithEntityContainer);
    EdmDataServices result = parser.readMetadata(reader, true);
    for (EdmSchema schema : result.getEdm().getSchemas()) {
      for (EdmEntityContainer container : schema.getEntityContainers()) {
        assertEquals("Container1", container.getName());
        assertEquals(Boolean.TRUE, container.isDefaultEntityContainer());

        EdmFunctionImport functionImport1 = container.getFunctionImport("EmployeeSearch");

        assertEquals("EmployeeSearch", functionImport1.getName());
        checkParameters1(functionImport1);

        EdmFunctionImport functionImport2 = container.getFunctionImport("RoomSearch");
        assertEquals("RoomSearch", functionImport2.getName());
        assertEquals("Employees", functionImport2.getEntitySet().getName());
        assertEquals(NAMESPACE, functionImport2.getReturnType().getType().getNamespace());
        assertEquals("Employee", functionImport2.getReturnType().getName());
        assertEquals(EdmMultiplicity.MANY, functionImport2.getReturnType().getMultiplicity());
        assertEquals("GET", functionImport2.getHttpMethod());
        List<String> parameterNames = (List<String>) functionImport2.getParameterNames();
        
        assertEquals(2, parameterNames.size());

        EdmParameter edmParam = functionImport2.getParameter(parameterNames.get(0));
        assertEquals("q1", parameterNames.get(0));
        assertEquals("String", edmParam.getType().getName());
        assertEquals(Boolean.TRUE, edmParam.getFacets().isNullable());
        
        edmParam = functionImport2.getParameter(parameterNames.get(1));
        assertEquals("q2", parameterNames.get(1));
        assertEquals("Int32", edmParam.getType().getName());
        assertEquals(Boolean.FALSE, edmParam.getFacets().isNullable());
        
        EdmFunctionImport functionImport3 = container.getFunctionImport("NoParamters");
        assertEquals("NoParamters", functionImport3.getName());
        parameterNames = (List<String>) functionImport3.getParameterNames();
        assertNotNull(parameterNames);
        assertEquals(0, parameterNames.size());

        EdmFunctionImport functionImport4 = container.getFunctionImport("NoReturn");
        assertEquals("NoReturn", functionImport4.getName());
        parameterNames = (List<String>) functionImport4.getParameterNames();
        assertNotNull(parameterNames);
        assertEquals(0, parameterNames.size());
        assertNull(functionImport4.getReturnType());

        EdmFunctionImport functionImport5 = container.getFunctionImport("SingleRoomReturnType");
        assertEquals("SingleRoomReturnType", functionImport5.getName());
        parameterNames = (List<String>) functionImport4.getParameterNames();
        assertNotNull(parameterNames);
        assertEquals(0, parameterNames.size());
        assertEquals("Employee",
            functionImport5.getReturnType().getType().getName());
        assertEquals(EdmMultiplicity.ONE, functionImport5.getReturnType().getMultiplicity());
      }
    }
  }
  
  private InputStream createStreamReader(final String xml) throws 
  XMLStreamException, UnsupportedEncodingException {
    return new ByteArrayInputStream(xml.getBytes("UTF-8"));
  }
  
  @Test
  public void testFunctionImportIn2Containers() throws XMLStreamException, 
  EntityProviderException, EdmException, UnsupportedEncodingException {
    final String xmWithEntityContainer =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\""
            + Edm.NAMESPACE_EDMX_2007_06
            + "\">"
            + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\""
            + Edm.NAMESPACE_M_2007_08
            + "\">"
            + "<Schema Namespace=\""
            + NAMESPACE
            + "\" xmlns=\""
            + Edm.NAMESPACE_EDM_2008_09
            + "\">"
            + "<EntityType Name= \"Employee\" m:HasStream=\"true\">"
            + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>"
            + "<Property Name=\""
            + propertyNames[0]
            + "\" Type=\"Edm.String\" Nullable=\"false\"/>"
            + "<Property Name=\""
            + propertyNames[1]
            + "\" Type=\"Edm.String\" m:FC_TargetPath=\"SyndicationTitle\"/>"
            + "</EntityType>"
            + "<EntityContainer Name=\"Container1\" m:IsDefaultEntityContainer=\"true\">"
            + "<EntitySet Name=\"Employees\" EntityType=\"RefScenario.Employee\"/>"
            + "<FunctionImport Name=\"EmployeeSearch\" ReturnType=\"Collection(RefScenario.Employee)\" " +
            "EntitySet=\"Employees\" m:HttpMethod=\"GET\">"
            + "<Parameter Name=\"q1\" Type=\"Edm.String\" Nullable=\"true\" />"
            + "<Parameter Name=\"q2\" Type=\"Edm.Int32\" Nullable=\"false\" />"
            + "</FunctionImport>"
            + "<FunctionImport Name=\"RoomSearch\" ReturnType=\"Collection(RefScenario.Employee)\" " +
            "EntitySet=\"Employees\" m:HttpMethod=\"GET\">"
            + "<Parameter Name=\"q1\" Type=\"Edm.String\" Nullable=\"true\" Mode=\"In\"/>"
            + "<Parameter Name=\"q2\" Type=\"Edm.Int32\" Nullable=\"false\" />"
            + "</FunctionImport>"
            + "</EntityContainer>" 
            + "<EntityContainer Name=\"Container2\" m:IsDefaultEntityContainer=\"false\">"
            + "<EntitySet Name=\"Employees\" EntityType=\"RefScenario.Employee\"/>"
            + "<FunctionImport Name=\"RoomSearch\" ReturnType=\"Collection(RefScenario.Employee)\" " +
            "EntitySet=\"Employees\" m:HttpMethod=\"GET\">"
            + "<Parameter Name=\"q1\" Type=\"Edm.String\" Nullable=\"true\" Mode=\"In\"/>"
            + "<Parameter Name=\"q2\" Type=\"Edm.Int32\" Nullable=\"false\" />"
            + "</FunctionImport>"
            + "</EntityContainer>" + 
            "</Schema>" + "</edmx:DataServices>" + "</edmx:Edmx>";
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmWithEntityContainer);
    EdmDataServices result = parser.readMetadata(reader, true);
    for (EdmSchema schema : result.getEdm().getSchemas()) {
      int i = 0;
      for (EdmEntityContainer container : schema.getEntityContainers()) {
        i++;
        assertEquals("Container" + i, container.getName());
        EdmFunctionImport functionImport = null;
        if (container.getName().equals("Container1")) {
          assertEquals(Boolean.TRUE, container.isDefaultEntityContainer());
          functionImport = container.getFunctionImport("EmployeeSearch");
          assertEquals("EmployeeSearch", functionImport.getName());
          checkParameters1(functionImport);
          assertEquals(Boolean.TRUE, container.isDefaultEntityContainer());
          functionImport = container.getFunctionImport("RoomSearch");
          assertEquals("RoomSearch", functionImport.getName());
          checkParameters1(functionImport);
        } else {
          assertEquals(Boolean.FALSE, container.isDefaultEntityContainer());
          functionImport = container.getFunctionImport("RoomSearch");
          assertEquals("RoomSearch", functionImport.getName());
        }
        
      }
    }
  }

  /**
   * @param functionImport
   * @throws EdmException
   */
  private void checkParameters1(EdmFunctionImport functionImport) throws EdmException {
    assertEquals("Employees", functionImport.getEntitySet().getName());
    assertEquals("Employee", functionImport.getReturnType().getName());
    assertEquals(EdmMultiplicity.MANY, functionImport.getReturnType().getMultiplicity());
    assertEquals(NAMESPACE, functionImport.getReturnType().getType().getNamespace());
    assertEquals("GET", functionImport.getHttpMethod());
    List<String> parameterNames = (List<String>) functionImport.getParameterNames();
    assertEquals(2, parameterNames.size());

    assertEquals("q1", parameterNames.get(0));
    EdmParameter edmParam = functionImport.getParameter(parameterNames.get(0)); 
    assertEquals("String", edmParam.getType().getName());
    assertEquals(Boolean.TRUE, edmParam.getFacets().isNullable());

    assertEquals("q2", parameterNames.get(1));
    edmParam = functionImport.getParameter(parameterNames.get(1));
    assertEquals("Int32", edmParam.getType().getName());
    assertEquals(Boolean.FALSE, edmParam.getFacets().isNullable());
  }
  
  @Test
  public void testFunctionImportIn2Schemas() throws XMLStreamException,
  EntityProviderException, EdmException, UnsupportedEncodingException {
    final String xmWithEntityContainer =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\""
            + Edm.NAMESPACE_EDMX_2007_06
            + "\">"
            + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\""
            + Edm.NAMESPACE_M_2007_08
            + "\">"
            + "<Schema Namespace=\""
            + NAMESPACE + "1"
            + "\" xmlns=\""
            + Edm.NAMESPACE_EDM_2008_09
            + "\">"
            + "<EntityType Name= \"Employee\" m:HasStream=\"true\">"
            + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>"
            + "<Property Name=\""
            + propertyNames[0]
            + "\" Type=\"Edm.String\" Nullable=\"false\"/>"
            + "<Property Name=\""
            + propertyNames[1]
            + "\" Type=\"Edm.String\" m:FC_TargetPath=\"SyndicationTitle\"/>"
            + "</EntityType>"
            + "<EntityContainer Name=\"Container1\" m:IsDefaultEntityContainer=\"true\">"
            + "<EntitySet Name=\"Employees\" EntityType=\"RefScenario1.Employee\"/>"
            + "<FunctionImport Name=\"EmployeeSearch\" ReturnType=\"Collection(RefScenario1.Employee)\" " +
            "EntitySet=\"Employees\" m:HttpMethod=\"GET\">"
            + "<Parameter Name=\"q1\" Type=\"Edm.String\" Nullable=\"true\" />"
            + "<Parameter Name=\"q2\" Type=\"Edm.Int32\" Nullable=\"false\" />"
            + "</FunctionImport>"
            + "<FunctionImport Name=\"RoomSearch\" ReturnType=\"Collection(RefScenario1.Employee)\" " +
            "EntitySet=\"Employees\" m:HttpMethod=\"GET\">"
            + "<Parameter Name=\"q1\" Type=\"Edm.String\" Nullable=\"true\" Mode=\"In\"/>"
            + "<Parameter Name=\"q2\" Type=\"Edm.Int32\" Nullable=\"false\" />"
            + "</FunctionImport>"
            + "</EntityContainer>" 
            + "<EntityContainer Name=\"Container2\" m:IsDefaultEntityContainer=\"false\">"
            + "<EntitySet Name=\"Employees\" EntityType=\"RefScenario1.Employee\"/>"
            + "<FunctionImport Name=\"RoomSearch\" ReturnType=\"Collection(RefScenario1.Employee)\" " +
            "EntitySet=\"Employees\" m:HttpMethod=\"GET\">"
            + "<Parameter Name=\"q1\" Type=\"Edm.String\" Nullable=\"true\" Mode=\"In\"/>"
            + "<Parameter Name=\"q2\" Type=\"Edm.Int32\" Nullable=\"false\" />"
            + "</FunctionImport>"
            + "</EntityContainer>" + 
            "</Schema>" 
            + "<Schema Namespace=\""
            + NAMESPACE2
            + "\" xmlns=\""
            + Edm.NAMESPACE_EDM_2008_09
            + "\">"
            + "<EntityType Name= \"Employee\" m:HasStream=\"true\">"
            + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>"
            + "<Property Name=\""
            + propertyNames[0]
            + "\" Type=\"Edm.String\" Nullable=\"false\"/>"
            + "<Property Name=\""
            + propertyNames[1]
            + "\" Type=\"Edm.String\" m:FC_TargetPath=\"SyndicationTitle\"/>"
            + "</EntityType>"
            + "<EntityContainer Name=\"Container1\" m:IsDefaultEntityContainer=\"true\">"
            + "<EntitySet Name=\"Employees\" EntityType=\"RefScenario2.Employee\"/>"
            + "<FunctionImport Name=\"EmployeeSearch\" ReturnType=\"Collection(RefScenario2.Employee)\" " +
            "EntitySet=\"Employees\" m:HttpMethod=\"GET\">"
            + "<Parameter Name=\"q1\" Type=\"Edm.String\" Nullable=\"true\" />"
            + "<Parameter Name=\"q2\" Type=\"Edm.Int32\" Nullable=\"false\" />"
            + "</FunctionImport>"
            + "<FunctionImport Name=\"RoomSearch\" ReturnType=\"Collection(RefScenario2.Employee)\" " +
            "EntitySet=\"Employees\" m:HttpMethod=\"GET\">"
            + "<Parameter Name=\"q1\" Type=\"Edm.String\" Nullable=\"true\" Mode=\"In\"/>"
            + "<Parameter Name=\"q2\" Type=\"Edm.Int32\" Nullable=\"false\" />"
            + "</FunctionImport>"
            + "</EntityContainer>" 
            + "</Schema>"
            + "</edmx:DataServices>" + "</edmx:Edmx>";
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmWithEntityContainer);
    EdmDataServices result = parser.readMetadata(reader, true);
    int i = 0;
    for (EdmSchema schema : result.getEdm().getSchemas()) {
      i++;
      assertEquals("RefScenario" + i, schema.getNamespace());
      int j = 0;
      for (EdmEntityContainer container : schema.getEntityContainers()) {
        j++;
        assertEquals("Container" + j, container.getName());
        EdmFunctionImport functionImport = null;
        if (container.getName().equals("Container1") && schema.getNamespace().equalsIgnoreCase("RefScenario" + i)) {
          assertEquals(Boolean.TRUE, container.isDefaultEntityContainer());
          functionImport = container.getFunctionImport("EmployeeSearch");
          assertEquals("EmployeeSearch", functionImport.getName());
          checkParameters(i, functionImport);
        } else if (container.getName().equals("Container2") && 
            schema.getNamespace().equalsIgnoreCase("RefScenario" + i)) {
          assertEquals(Boolean.FALSE, container.isDefaultEntityContainer());
          functionImport = container.getFunctionImport("RoomSearch");
          assertEquals("RoomSearch", functionImport.getName());
          checkParameters(i, functionImport);
        }
      }
    }
  }

  /**
   * @param i
   * @param functionImport
   * @throws EdmException
   */
  private void checkParameters(int i, EdmFunctionImport functionImport) throws EdmException {
    assertEquals("Employees", functionImport.getEntitySet().getName());
    assertEquals("Employee", functionImport.getReturnType().getName());
    assertEquals(EdmMultiplicity.MANY, functionImport.getReturnType().getMultiplicity());
    assertEquals(NAMESPACE + i, functionImport.getReturnType().getType().getNamespace());
    assertEquals("GET", functionImport.getHttpMethod());
    List<String> parameterNames = (List<String>) functionImport.getParameterNames();
    assertEquals(2, parameterNames.size());

    assertEquals("q1", parameterNames.get(0));
    EdmParameter edmParam = functionImport.getParameter(parameterNames.get(0)); 
    assertEquals("String", edmParam.getType().getName());
    assertEquals(Boolean.TRUE, edmParam.getFacets().isNullable());

    assertEquals("q2", parameterNames.get(1));
    edmParam = functionImport.getParameter(parameterNames.get(1));
    assertEquals("Int32", edmParam.getType().getName());
    assertEquals(Boolean.FALSE, edmParam.getFacets().isNullable());
  }
  
  @Test(expected = EntityProviderException.class)
  public void testMissingTypeAtFunctionImport() throws Exception {
    final String xml =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\""
            + Edm.NAMESPACE_EDMX_2007_06
            + "\">"
            + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\""
            + Edm.NAMESPACE_M_2007_08
            + "\">"
            + "<Schema Namespace=\""
            + NAMESPACE
            + "\" xmlns=\""
            + Edm.NAMESPACE_EDM_2008_09
            + "\">"
            + "<EntityType Name= \"Employee\" m:HasStream=\"true\">"
            + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>"
            + "<Property Name=\""
            + propertyNames[0]
            + "\" Type=\"Edm.String\" Nullable=\"false\"/>"
            + "<Property Name=\""
            + propertyNames[1]
            + "\" Type=\"Edm.String\" m:FC_TargetPath=\"SyndicationTitle\"/>"
            + "</EntityType>"
            + "<EntityContainer Name=\"Container1\" m:IsDefaultEntityContainer=\"true\">"
            + "<EntitySet Name=\"Employees\" EntityType=\"RefScenario.Employee\"/>"
            + "<FunctionImport Name=\"EmployeeSearch\" ReturnType=\"Collection(RefScenario.Employee)\" " +
            "EntitySet=\"Employees\" m:HttpMethod=\"GET\">"
            + "<Parameter Name=\"q\" Nullable=\"true\" />" + "</FunctionImport>"
            + "</EntityContainer></Schema></edmx:DataServices></edmx:Edmx>";
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xml);
    try {
      parser.readMetadata(reader, true);
    } catch (EntityProviderException e) {
      assertEquals(EntityProviderException.MISSING_ATTRIBUTE.getKey(), e.getMessageReference().getKey());
      assertEquals(2, e.getMessageReference().getContent().size());
      assertEquals("Type", e.getMessageReference().getContent().get(0));
      assertEquals("Parameter", e.getMessageReference().getContent().get(1));
      throw e;
    }
  }
  
  @Test
  public void testFunctionImportWithAnnotations() throws XMLStreamException, 
  EntityProviderException, EdmException, UnsupportedEncodingException {
    final String xmWithEntityContainer =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\""
            + Edm.NAMESPACE_EDMX_2007_06 
            + "\" xmlns:sap=\"http://www.sap.com/Protocols/SAPData\">"
            + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\""
            + Edm.NAMESPACE_M_2007_08
            + "\">"
            + "<Schema Namespace=\""
            + NAMESPACE
            + "\" xmlns=\""
            + Edm.NAMESPACE_EDM_2008_09
            + "\">"
            + "<EntityType Name= \"Employee\" m:HasStream=\"true\">"
            + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>"
            + "<Property Name=\""
            + propertyNames[0]
            + "\" Type=\"Edm.String\" Nullable=\"false\"/>"
            + "<Property Name=\""
            + propertyNames[1]
            + "\" Type=\"Edm.String\" m:FC_TargetPath=\"SyndicationTitle\"/>"
            + "</EntityType>"
            + "<EntityContainer Name=\"Container1\" m:IsDefaultEntityContainer=\"true\">"
            + "<EntitySet Name=\"Employees\" EntityType=\"RefScenario.Employee\"/>"
            + "<FunctionImport Name=\"EmployeeSearch\" ReturnType=\"Collection(RefScenario.Employee)\" " 
            + "EntitySet=\"Employees\" m:HttpMethod=\"GET\" sap:label=\"Approve\" "
            + "sap:action-for=\"RefScenario.LeaveRequest\" sap:applicable-path=\"ControlData/NeedsApproval\">"
            + "<Parameter Name=\"q1\" Type=\"Edm.String\" Nullable=\"true\" />"
            + "<Parameter Name=\"q2\" Type=\"Edm.Int32\" Nullable=\"false\" >"
            + "<sap:value-constraint set=\"Employees\">"
            + "<sap:parameter-ref name=\"EmployeeId\" />"
            + "</sap:value-constraint>"
            + "</Parameter>"
            + "</FunctionImport>"
            + "</EntityContainer>" + "</Schema>" + "</edmx:DataServices>" + "</edmx:Edmx>";
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmWithEntityContainer);
    EdmDataServices result = parser.readMetadata(reader, true);
    for (EdmSchema schema : result.getEdm().getSchemas()) {
      for (EdmEntityContainer container : schema.getEntityContainers()) {
        assertEquals("Container1", container.getName());
        assertEquals(Boolean.TRUE, container.isDefaultEntityContainer());

        EdmFunctionImport functionImport1 = container.getFunctionImport("EmployeeSearch");

        assertEquals("EmployeeSearch", functionImport1.getName());
        checkParametersWithAnnotations(functionImport1);
      }
    }
  }
  
  /**
   * @param functionImport
   * @throws EdmException
   */
  private void checkParametersWithAnnotations(EdmFunctionImport functionImport) throws EdmException {
    assertEquals("Employees", functionImport.getEntitySet().getName());
    assertEquals("Employee", functionImport.getReturnType().getName());
    assertEquals(EdmMultiplicity.MANY, functionImport.getReturnType().getMultiplicity());
    assertEquals(NAMESPACE, functionImport.getReturnType().getType().getNamespace());
    assertEquals("GET", functionImport.getHttpMethod());
    List<EdmAnnotationAttribute> annotationAttrs = functionImport.getAnnotations().getAnnotationAttributes();
    assertEquals(3, annotationAttrs.size());
    assertEquals("label", annotationAttrs.get(0).getName());
    assertEquals("Approve", annotationAttrs.get(0).getText());
    assertEquals("sap", annotationAttrs.get(0).getPrefix());
    assertEquals("http://www.sap.com/Protocols/SAPData", annotationAttrs.get(0).getNamespace());
    assertEquals("action-for", annotationAttrs.get(1).getName());
    assertEquals("RefScenario.LeaveRequest", annotationAttrs.get(1).getText());
    assertEquals("sap", annotationAttrs.get(1).getPrefix());
    assertEquals("http://www.sap.com/Protocols/SAPData", annotationAttrs.get(1).getNamespace());
    assertEquals("applicable-path", annotationAttrs.get(2).getName());
    assertEquals("ControlData/NeedsApproval", annotationAttrs.get(2).getText());
    assertEquals("sap", annotationAttrs.get(2).getPrefix());
    assertEquals("http://www.sap.com/Protocols/SAPData", annotationAttrs.get(2).getNamespace());
      
    List<String> parameterNames = (List<String>) functionImport.getParameterNames();
    assertEquals(2, parameterNames.size());

    assertEquals("q1", parameterNames.get(0));
    EdmParameter edmParam = functionImport.getParameter(parameterNames.get(0)); 
    assertEquals("String", edmParam.getType().getName());
    assertEquals(Boolean.TRUE, edmParam.getFacets().isNullable());

    assertEquals("q2", parameterNames.get(1));
    edmParam = functionImport.getParameter(parameterNames.get(1));
    EdmAnnotations edmParamAnnotations = edmParam.getAnnotations();
    if (edmParamAnnotations != null) {
      for (EdmAnnotationElement annotationEle : edmParamAnnotations.getAnnotationElements()) {
        assertEquals("value-constraint", annotationEle.getName());
        assertEquals("http://www.sap.com/Protocols/SAPData", annotationEle.getNamespace());
        assertEquals("sap", annotationEle.getPrefix());
        for (EdmAnnotationAttribute annotationAttr : annotationEle.getAttributes()) {
          assertEquals("set", annotationAttr.getName());
          assertEquals("Employees", annotationAttr.getText());
        }
        for (EdmAnnotationElement childAnnotationEle : annotationEle.getChildElements()) {
          assertEquals("parameter-ref", childAnnotationEle.getName());
          assertEquals("http://www.sap.com/Protocols/SAPData", childAnnotationEle.getNamespace());
          assertEquals("sap", childAnnotationEle.getPrefix());
          for (EdmAnnotationAttribute childAnnotationAttr : childAnnotationEle.getAttributes()) {
            assertEquals("name", childAnnotationAttr.getName());
            assertEquals("EmployeeId", childAnnotationAttr.getText());
          }
        }
      }
    }
    assertEquals("Int32", edmParam.getType().getName());
    assertEquals(Boolean.FALSE, edmParam.getFacets().isNullable());
  }
  
  @Test
  public void testSameFunctionImportIn2Containers() throws XMLStreamException, 
  EntityProviderException, EdmException, UnsupportedEncodingException {
    final String xmWithEntityContainer =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\""
            + Edm.NAMESPACE_EDMX_2007_06
            + "\">"
            + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\""
            + Edm.NAMESPACE_M_2007_08
            + "\">"
            + "<Schema Namespace=\""
            + NAMESPACE
            + "\" xmlns=\""
            + Edm.NAMESPACE_EDM_2008_09
            + "\">"
            + "<EntityType Name= \"Employee\" m:HasStream=\"true\">"
            + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>"
            + "<Property Name=\""
            + propertyNames[0]
            + "\" Type=\"Edm.String\" Nullable=\"false\"/>"
            + "<Property Name=\""
            + propertyNames[1]
            + "\" Type=\"Edm.String\" m:FC_TargetPath=\"SyndicationTitle\"/>"
            + "</EntityType>"
            + "<EntityContainer Name=\"Container1\" m:IsDefaultEntityContainer=\"true\">"
            + "<EntitySet Name=\"Employees\" EntityType=\"RefScenario.Employee\"/>"
            + "<FunctionImport Name=\"EmployeeSearch\" ReturnType=\"Collection(RefScenario.Employee)\" " +
            "EntitySet=\"Employees\" m:HttpMethod=\"GET\">"
            + "<Parameter Name=\"q1\" Type=\"Edm.String\" Nullable=\"true\" />"
            + "<Parameter Name=\"q2\" Type=\"Edm.Int32\" Nullable=\"false\" />"
            + "</FunctionImport>"
            + "</EntityContainer>" + "<EntityContainer Name=\"Container2\" m:IsDefaultEntityContainer=\"false\">"
            + "<EntitySet Name=\"Employees\" EntityType=\"RefScenario.Employee\"/>"
            + "<FunctionImport Name=\"EmployeeSearch\" ReturnType=\"Collection(RefScenario.Employee)\" " +
            "EntitySet=\"Employees\" m:HttpMethod=\"GET\">"
            + "<Parameter Name=\"q1\" Type=\"Edm.String\" Nullable=\"true\" />"
            + "<Parameter Name=\"q2\" Type=\"Edm.Int32\" Nullable=\"false\" />"
            + "</FunctionImport>"
            + "</EntityContainer>" 
            + "</Schema>" + "</edmx:DataServices>" + "</edmx:Edmx>";
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmWithEntityContainer);
    EdmDataServices result = parser.readMetadata(reader, true);
    for (EdmSchema schema : result.getEdm().getSchemas()) {
      for (EdmEntityContainer container : schema.getEntityContainers()) {
        if (container.getName().equalsIgnoreCase("Container1")) {
          assertEquals(Boolean.TRUE, container.isDefaultEntityContainer());
          EdmFunctionImport functionImport1 = container.getFunctionImport("EmployeeSearch");
          assertEquals("EmployeeSearch", functionImport1.getName());
          checkParameters1(functionImport1);
        } else if (container.getName().equalsIgnoreCase("Container2")) {
          assertEquals(Boolean.FALSE, container.isDefaultEntityContainer());
          EdmFunctionImport functionImport1 = container.getFunctionImport("EmployeeSearch");
          assertEquals("EmployeeSearch", functionImport1.getName());
          checkParameters1(functionImport1);
        }
      }
    }
  }
}
