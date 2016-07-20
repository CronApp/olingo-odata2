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
package org.apache.olingo.odata2.core.ep.consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.entry.DeletedEntryMetadata;
import org.apache.olingo.odata2.api.ep.entry.MediaMetadata;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.FeedMetadata;
import org.apache.olingo.odata2.api.ep.feed.ODataDeltaFeed;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.junit.Test;

/**
 *  
 */
public class JsonFeedConsumerTest extends AbstractConsumerTest {

  @Test
  public void teamsFeed() throws Exception {
    ODataFeed feed = prepareAndExecuteFeed("JsonTeams.json", "Teams", DEFAULT_PROPERTIES);

    List<ODataEntry> entries = feed.getEntries();
    assertNotNull(entries);
    assertEquals(2, entries.size());

    // Team1
    ODataEntry entry = entries.get(0);
    Map<String, Object> properties = entry.getProperties();
    assertNotNull(properties);
    assertEquals("1", properties.get("Id"));
    assertEquals("Team 1", properties.get("Name"));
    assertEquals(Boolean.FALSE, properties.get("isScrumTeam"));
    assertNull(properties.get("nt_Employees"));

    List<String> associationUris = entry.getMetadata().getAssociationUris("nt_Employees");
    assertEquals(1, associationUris.size());
    assertEquals("http://localhost:8080/ReferenceScenario.svc/Teams('1')/nt_Employees", associationUris.get(0));

    checkMediaDataInitial(entry.getMediaMetadata());

    // Team2
    entry = entries.get(1);
    properties = entry.getProperties();
    assertNotNull(properties);
    assertEquals("2", properties.get("Id"));
    assertEquals("Team 2", properties.get("Name"));
    assertEquals(Boolean.TRUE, properties.get("isScrumTeam"));
    assertNull(properties.get("nt_Employees"));

    associationUris = entry.getMetadata().getAssociationUris("nt_Employees");
    assertEquals(1, associationUris.size());
    assertEquals("http://localhost:8080/ReferenceScenario.svc/Teams('2')/nt_Employees", associationUris.get(0));

    checkMediaDataInitial(entry.getMediaMetadata());

    // Check FeedMetadata
    FeedMetadata feedMetadata = feed.getFeedMetadata();
    assertNotNull(feedMetadata);
    assertNull(feedMetadata.getInlineCount());
    assertNull(feedMetadata.getNextLink());
  }

  @Test
  public void teamsFeedWithoutD() throws Exception {
    ODataFeed feed = prepareAndExecuteFeed("JsonTeamsWithoutD.json", "Teams", DEFAULT_PROPERTIES);

    List<ODataEntry> entries = feed.getEntries();
    assertNotNull(entries);
    assertEquals(2, entries.size());

    // Team1
    ODataEntry entry = entries.get(0);
    Map<String, Object> properties = entry.getProperties();
    assertNotNull(properties);
    assertEquals("1", properties.get("Id"));
    assertEquals("Team 1", properties.get("Name"));
    assertEquals(Boolean.FALSE, properties.get("isScrumTeam"));
    assertNull(properties.get("nt_Employees"));

    List<String> associationUris = entry.getMetadata().getAssociationUris("nt_Employees");
    assertEquals(1, associationUris.size());
    assertEquals("http://localhost:8080/ReferenceScenario.svc/Teams('1')/nt_Employees", associationUris.get(0));

    checkMediaDataInitial(entry.getMediaMetadata());

    // Team2
    entry = entries.get(1);
    properties = entry.getProperties();
    assertNotNull(properties);
    assertEquals("2", properties.get("Id"));
    assertEquals("Team 2", properties.get("Name"));
    assertEquals(Boolean.TRUE, properties.get("isScrumTeam"));
    assertNull(properties.get("nt_Employees"));

    associationUris = entry.getMetadata().getAssociationUris("nt_Employees");
    assertEquals(1, associationUris.size());
    assertEquals("http://localhost:8080/ReferenceScenario.svc/Teams('2')/nt_Employees", associationUris.get(0));

    checkMediaDataInitial(entry.getMediaMetadata());

    // Check FeedMetadata
    FeedMetadata feedMetadata = feed.getFeedMetadata();
    assertNotNull(feedMetadata);
    assertNull(feedMetadata.getInlineCount());
    assertNull(feedMetadata.getNextLink());
  }

  @Test(expected = EntityProviderException.class)
  public void invalidDoubleClosingBrackets() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    String content = "{\"d\":{\"results\":[]}}}";
    InputStream contentBody = createContentAsStream(content);

    // execute
    JsonEntityConsumer xec = new JsonEntityConsumer();
    xec.readFeed(entitySet, contentBody, DEFAULT_PROPERTIES);
  }

  @Test
  public void emptyFeed() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    String content = "{\"d\":{\"results\":[]}}";
    InputStream contentBody = createContentAsStream(content);

    // execute
    JsonEntityConsumer xec = new JsonEntityConsumer();
    ODataFeed feed = xec.readFeed(entitySet, contentBody, DEFAULT_PROPERTIES);
    assertNotNull(feed);

    List<ODataEntry> entries = feed.getEntries();
    assertNotNull(entries);
    assertEquals(0, entries.size());

    FeedMetadata feedMetadata = feed.getFeedMetadata();
    assertNotNull(feedMetadata);
    assertNull(feedMetadata.getInlineCount());
    assertNull(feedMetadata.getNextLink());
  }

  @Test
  public void emptyFeedWithoutDAndResults() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    InputStream contentBody = createContentAsStream("[]");
    final ODataFeed feed = new JsonEntityConsumer().readFeed(entitySet, contentBody, DEFAULT_PROPERTIES);
    assertNotNull(feed);
    final List<ODataEntry> entries = feed.getEntries();
    assertNotNull(entries);
    assertEquals(0, entries.size());
    final FeedMetadata feedMetadata = feed.getFeedMetadata();
    assertNotNull(feedMetadata);
    assertNull(feedMetadata.getInlineCount());
    assertNull(feedMetadata.getNextLink());
  }

  @Test
  public void emptyFeedWithoutResults() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    InputStream contentBody = createContentAsStream("{\"d\":[]}");
    final ODataFeed feed = new JsonEntityConsumer().readFeed(entitySet, contentBody, DEFAULT_PROPERTIES);
    assertNotNull(feed);
    final List<ODataEntry> entries = feed.getEntries();
    assertNotNull(entries);
    assertEquals(0, entries.size());
    final FeedMetadata feedMetadata = feed.getFeedMetadata();
    assertNotNull(feedMetadata);
    assertNull(feedMetadata.getInlineCount());
    assertNull(feedMetadata.getNextLink());
  }

  @Test(expected = EntityProviderException.class)
  public void resultsNotPresent() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    InputStream contentBody = createContentAsStream("{\"d\":{}}");
    new JsonEntityConsumer().readFeed(entitySet, contentBody, DEFAULT_PROPERTIES);
  }

  @Test(expected = EntityProviderException.class)
  public void countButNoResults() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    InputStream contentBody = createContentAsStream("{\"d\":{\"__count\":\"1\"}}");
    new JsonEntityConsumer().readFeed(entitySet, contentBody, DEFAULT_PROPERTIES);
  }

  @Test(expected = EntityProviderException.class)
  public void wrongCountType() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    InputStream contentBody = createContentAsStream("{\"d\":{\"__count\":1,\"results\":[]}}");
    new JsonEntityConsumer().readFeed(entitySet, contentBody, DEFAULT_PROPERTIES);
  }

  @Test(expected = EntityProviderException.class)
  public void wrongCountContent() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    InputStream contentBody = createContentAsStream("{\"d\":{\"__count\":\"one\",\"results\":[]}}");
    new JsonEntityConsumer().readFeed(entitySet, contentBody, DEFAULT_PROPERTIES);
  }

  @Test(expected = EntityProviderException.class)
  public void negativeCount() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    InputStream contentBody = createContentAsStream("{\"d\":{\"__count\":\"-1\",\"results\":[]}}");
    new JsonEntityConsumer().readFeed(entitySet, contentBody, DEFAULT_PROPERTIES);
  }

  @Test(expected = EntityProviderException.class)
  public void wrongNextType() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    InputStream contentBody = createContentAsStream("{\"d\":{\"results\":[],\"__next\":false}}");
    new JsonEntityConsumer().readFeed(entitySet, contentBody, DEFAULT_PROPERTIES);
  }

  @Test(expected = EntityProviderException.class)
  public void wrongTag() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    InputStream contentBody = createContentAsStream("{\"d\":{\"__results\":null}}");
    new JsonEntityConsumer().readFeed(entitySet, contentBody, DEFAULT_PROPERTIES);
  }

  @Test(expected = EntityProviderException.class)
  public void doubleCount() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    InputStream contentBody = createContentAsStream("{\"d\":{\"__count\":\"1\",\"__count\":\"2\",\"results\":[]}}");
    new JsonEntityConsumer().readFeed(entitySet, contentBody, DEFAULT_PROPERTIES);
  }

  @Test(expected = EntityProviderException.class)
  public void doubleNext() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    InputStream contentBody = createContentAsStream("{\"d\":{\"results\":[],\"__next\":\"a\",\"__next\":\"b\"}}");
    new JsonEntityConsumer().readFeed(entitySet, contentBody, DEFAULT_PROPERTIES);
  }

  @Test(expected = EntityProviderException.class)
  public void doubleResults() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    InputStream contentBody = createContentAsStream("{\"results\":{\"results\":[]}}");
    new JsonEntityConsumer().readFeed(entitySet, contentBody, DEFAULT_PROPERTIES);
  }

  @Test(expected = EntityProviderException.class)
  public void doubleD() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    InputStream contentBody = createContentAsStream("{\"d\":{\"d\":[]}}");
    new JsonEntityConsumer().readFeed(entitySet, contentBody, DEFAULT_PROPERTIES);
  }

  @Test
  public void teamsFeedWithCount() throws Exception {
    ODataFeed feed = prepareAndExecuteFeed("JsonTeamsWithCount.json", "Teams", DEFAULT_PROPERTIES);

    List<ODataEntry> entries = feed.getEntries();
    assertNotNull(entries);
    assertEquals(2, entries.size());

    // Check FeedMetadata
    FeedMetadata feedMetadata = feed.getFeedMetadata();
    assertNotNull(feedMetadata);
    assertEquals(Integer.valueOf(3), feedMetadata.getInlineCount());
    assertNull(feedMetadata.getNextLink());
  }

  @Test
  public void teamsFeedWithCountWithoutD() throws Exception {
    ODataFeed feed = prepareAndExecuteFeed("JsonTeamsWithCountWithoutD.json", "Teams", DEFAULT_PROPERTIES);

    List<ODataEntry> entries = feed.getEntries();
    assertNotNull(entries);
    assertEquals(2, entries.size());

    // Check FeedMetadata
    FeedMetadata feedMetadata = feed.getFeedMetadata();
    assertNotNull(feedMetadata);
    assertEquals(Integer.valueOf(3), feedMetadata.getInlineCount());
    assertNull(feedMetadata.getNextLink());
  }

  @Test
  public void feedWithInlineCountAndNextAndDelta() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    String content =
        "{\"d\":{\"__count\":\"3\",\"results\":[{" +
            "\"__metadata\":{\"id\":\"http://localhost:8080/ReferenceScenario.svc/Teams('1')\"," +
            "\"uri\":\"http://localhost:8080/ReferenceScenario.svc/Teams('1')\",\"type\":\"RefScenario.Team\"}," +
            "\"Id\":\"1\",\"Name\":\"Team 1\",\"isScrumTeam\":false,\"nt_Employees\":{\"__deferred\":{" +
            "\"uri\":\"http://localhost:8080/ReferenceScenario.svc/Teams('1')/nt_Employees\"}}}]," +
            "\"__next\":\"Rooms?$skiptoken=98&$inlinecount=allpages\",\"__delta\":\"deltalink\"}}";
    assertNotNull(content);
    InputStream contentBody = createContentAsStream(content);

    // execute
    JsonEntityConsumer xec = new JsonEntityConsumer();
    ODataFeed feed = xec.readFeed(entitySet, contentBody, DEFAULT_PROPERTIES);
    assertNotNull(feed);

    List<ODataEntry> entries = feed.getEntries();
    assertNotNull(entries);
    assertEquals(1, entries.size());

    FeedMetadata feedMetadata = feed.getFeedMetadata();
    assertNotNull(feedMetadata);
    assertEquals(Integer.valueOf(3), feedMetadata.getInlineCount());
    assertEquals("Rooms?$skiptoken=98&$inlinecount=allpages", feedMetadata.getNextLink());
    assertEquals("deltalink", feedMetadata.getDeltaLink());
  }

  @Test
  public void feedWithTeamAndNextAndDelta() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    String content =
        "{\"d\":{\"results\":[{" +
            "\"__metadata\":{\"id\":\"http://localhost:8080/ReferenceScenario.svc/Teams('1')\"," +
            "\"uri\":\"http://localhost:8080/ReferenceScenario.svc/Teams('1')\",\"type\":\"RefScenario.Team\"}," +
            "\"Id\":\"1\",\"Name\":\"Team 1\",\"isScrumTeam\":false,\"nt_Employees\":{\"__deferred\":{" +
            "\"uri\":\"http://localhost:8080/ReferenceScenario.svc/Teams('1')/nt_Employees\"}}}]," +
            "\"__next\":\"Rooms?$skiptoken=98\"," +
            "\"__delta\":\"http://localhost:8080/ReferenceScenario.svc/Teams?!deltatoken=4711\"}}";
    assertNotNull(content);
    InputStream contentBody = createContentAsStream(content);

    // execute
    JsonEntityConsumer xec = new JsonEntityConsumer();
    ODataFeed feed = xec.readFeed(entitySet, contentBody, DEFAULT_PROPERTIES);
    assertNotNull(feed);

    List<ODataEntry> entries = feed.getEntries();
    assertNotNull(entries);
    assertEquals(1, entries.size());

    FeedMetadata feedMetadata = feed.getFeedMetadata();
    assertNotNull(feedMetadata);
    assertEquals("Rooms?$skiptoken=98", feedMetadata.getNextLink());
    assertEquals("http://localhost:8080/ReferenceScenario.svc/Teams?!deltatoken=4711", feedMetadata.getDeltaLink());
  }

  @Test
  public void feedWithTeamAndDeltaAndDeletedEntriesWithoutWhen() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    String content =
        "{\"d\":{\"results\":[{" +
            "\"__metadata\":{\"id\":\"http://localhost:8080/ReferenceScenario.svc/Teams('1')\"," +
            "\"uri\":\"http://localhost:8080/ReferenceScenario.svc/Teams('1')\",\"type\":\"RefScenario.Team\"}," +
            "\"Id\":\"1\",\"Name\":\"Team 1\",\"isScrumTeam\":false,\"nt_Employees\":{\"__deferred\":{" +
            "\"uri\":\"http://localhost:8080/ReferenceScenario.svc/Teams('1')/nt_Employees\"}}}" +
            ",{ \"@odata.context\":\"$metadata#Teams/$deletedEntity\",\"id\":\"/Teams('2')\"}" +
            "]," +
            "\"__delta\":\"http://localhost:8080/ReferenceScenario.svc/Teams?!deltatoken=4711\"}}";
    assertNotNull(content);
    InputStream contentBody = createContentAsStream(content);

    // execute
    JsonEntityConsumer xec = new JsonEntityConsumer();
    ODataDeltaFeed feed = xec.readDeltaFeed(entitySet, contentBody, DEFAULT_PROPERTIES);
    assertNotNull(feed);

    List<ODataEntry> entries = feed.getEntries();
    assertNotNull(entries);
    assertEquals(1, entries.size());

    FeedMetadata feedMetadata = feed.getFeedMetadata();
    assertNotNull(feedMetadata);
    assertEquals("http://localhost:8080/ReferenceScenario.svc/Teams?!deltatoken=4711", feedMetadata.getDeltaLink());

    List<DeletedEntryMetadata> deletedEntries = feed.getDeletedEntries();
    assertEquals(1, deletedEntries.size());
    assertEquals("/Teams('2')", deletedEntries.get(0).getUri());
    assertNull(deletedEntries.get(0).getWhen());
  }

  @Test
  public void feedWithTeamAndDeltaAndDeletedEntries() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    String content =
        "{\"d\":{\"results\":[{" +
            "\"__metadata\":{\"id\":\"http://localhost:8080/ReferenceScenario.svc/Teams('1')\"," +
            "\"uri\":\"http://localhost:8080/ReferenceScenario.svc/Teams('1')\",\"type\":\"RefScenario.Team\"}," +
            "\"Id\":\"1\",\"Name\":\"Team 1\",\"isScrumTeam\":false,\"nt_Employees\":{\"__deferred\":{" +
            "\"uri\":\"http://localhost:8080/ReferenceScenario.svc/Teams('1')/nt_Employees\"}}}" +
            ",{ \"@odata.context\":\"$metadata#Teams/$deletedEntity\"," +
            "\"id\":\"/Teams('2')\"," +
            "\"when\":\"\\/Date(1297187419617)\\/\" }" +
            "]," +
            "\"__delta\":\"http://localhost:8080/ReferenceScenario.svc/Teams?!deltatoken=4711\"}}";
    assertNotNull(content);
    InputStream contentBody = createContentAsStream(content);

    // execute
    JsonEntityConsumer xec = new JsonEntityConsumer();
    ODataDeltaFeed feed = xec.readDeltaFeed(entitySet, contentBody, DEFAULT_PROPERTIES);
    assertNotNull(feed);

    List<ODataEntry> entries = feed.getEntries();
    assertNotNull(entries);
    assertEquals(1, entries.size());

    FeedMetadata feedMetadata = feed.getFeedMetadata();
    assertNotNull(feedMetadata);
    assertEquals("http://localhost:8080/ReferenceScenario.svc/Teams?!deltatoken=4711", feedMetadata.getDeltaLink());

    List<DeletedEntryMetadata> deletedEntries = feed.getDeletedEntries();
    assertEquals(1, deletedEntries.size());
    assertEquals("/Teams('2')", deletedEntries.get(0).getUri());
    assertEquals(new Date(1297187419617l), deletedEntries.get(0).getWhen());
  }

  @Test
  public void feedWithOnlyDeletedEntries() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    String content =
        "{\"d\":{\"results\":[" +
            "{ \"@odata.context\":\"$metadata#Teams/$deletedEntity\"," +
            "\"id\":\"/Teams('2')\"," +
            "\"when\":\"\\/Date(1297187419617)\\/\" }" +
            "]," +
            "\"__delta\":\"http://localhost:8080/ReferenceScenario.svc/Teams?!deltatoken=4711\"}}";
    assertNotNull(content);
    InputStream contentBody = createContentAsStream(content);

    // execute
    JsonEntityConsumer xec = new JsonEntityConsumer();
    ODataDeltaFeed feed = xec.readDeltaFeed(entitySet, contentBody, DEFAULT_PROPERTIES);
    assertNotNull(feed);

    List<ODataEntry> entries = feed.getEntries();
    assertNotNull(entries);
    assertEquals(0, entries.size());

    FeedMetadata feedMetadata = feed.getFeedMetadata();
    assertNotNull(feedMetadata);
    assertEquals("http://localhost:8080/ReferenceScenario.svc/Teams?!deltatoken=4711", feedMetadata.getDeltaLink());

    List<DeletedEntryMetadata> deletedEntries = feed.getDeletedEntries();
    assertEquals(1, deletedEntries.size());
    assertEquals("/Teams('2')", deletedEntries.get(0).getUri());
    assertEquals(new Date(1297187419617l), deletedEntries.get(0).getWhen());
  }

  @Test(expected = EntityProviderException.class)
  public void feedWithInvalidDeletedEntryWhenValue() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    String content =
        "{\"d\":{\"results\":[" +
            "{ \"@odata.context\":\"$metadata#Teams/$deletedEntity\"," +
            "\"id\":\"/Teams('2')\"," +
            "\"when\":\"1297187419617\" }" +
            "]," +
            "\"__delta\":\"http://localhost:8080/ReferenceScenario.svc/Teams?!deltatoken=4711\"}}";
    assertNotNull(content);
    InputStream contentBody = createContentAsStream(content);

    // execute
    JsonEntityConsumer xec = new JsonEntityConsumer();
    try {
      xec.readDeltaFeed(entitySet, contentBody, DEFAULT_PROPERTIES);
    } catch (EntityProviderException e) {
      assertEquals(EntityProviderException.INVALID_DELETED_ENTRY_METADATA, e.getMessageReference());
      throw e;
    }
  }

  @Test
  public void feedWithRoomAndDeltaAndDeletedEntries() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    String content = readFile("JsonWithDeletedEntries.json");
    assertNotNull(content);
    InputStream contentBody = createContentAsStream(content);

    // execute
    JsonEntityConsumer xec = new JsonEntityConsumer();
    ODataDeltaFeed feed = xec.readDeltaFeed(entitySet, contentBody, DEFAULT_PROPERTIES);
    assertNotNull(feed);

    List<ODataEntry> entries = feed.getEntries();
    assertNotNull(entries);
    assertEquals(1, entries.size());

    FeedMetadata feedMetadata = feed.getFeedMetadata();
    assertNotNull(feedMetadata);
    assertEquals("http://localhost:8080/ReferenceScenario.svc/Rooms?!deltatoken=4711", feedMetadata.getDeltaLink());

    assertEquals("W/\"2\"", entries.get(0).getMetadata().getEtag());

    List<DeletedEntryMetadata> deletedEntries = feed.getDeletedEntries();
    assertEquals(2, deletedEntries.size());
    for (DeletedEntryMetadata deletedEntry : deletedEntries) {
      String uri = deletedEntry.getUri();
      if (uri.contains("Rooms('4')")) {
        assertEquals("http://host:80/service/Rooms('4')", deletedEntry.getUri());
        assertEquals(new Date(3509636760000l), deletedEntry.getWhen());
      } else if (uri.contains("Rooms('3')")) {
        assertEquals("http://host:80/service/Rooms('3')", deletedEntry.getUri());
        assertEquals(new Date(1300561560000l), deletedEntry.getWhen());
      } else {
        Assert.fail("Found unknown DeletedEntry with value: " + deletedEntry);
      }
    }
  }

  private void checkMediaDataInitial(final MediaMetadata mediaMetadata) {
    assertNull(mediaMetadata.getContentType());
    assertNull(mediaMetadata.getEditLink());
    assertNull(mediaMetadata.getEtag());
    assertNull(mediaMetadata.getSourceLink());
  }
}
