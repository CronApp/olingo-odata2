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
package org.apache.olingo.odata2.core.ep.feed;

import java.util.List;

import org.apache.olingo.odata2.api.ep.entry.DeletedEntryMetadata;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.FeedMetadata;
import org.apache.olingo.odata2.api.ep.feed.ODataDeltaFeed;

public class ODataDeltaFeedImpl implements ODataDeltaFeed {

  private final List<ODataEntry> entries;
  private final FeedMetadata feedMetadata;
  private final List<DeletedEntryMetadata> deletedEntries;

  public ODataDeltaFeedImpl(final List<ODataEntry> entries, final FeedMetadata feedMetadata) {
    this(entries, feedMetadata, null);
  }

  public ODataDeltaFeedImpl(final List<ODataEntry> entries, final FeedMetadata feedMetadata,
      final List<DeletedEntryMetadata> deletedEntries) {
    this.entries = entries;
    this.feedMetadata = feedMetadata;
    this.deletedEntries = deletedEntries;
  }

  @Override
  public List<ODataEntry> getEntries() {
    return entries;
  }

  @Override
  public FeedMetadata getFeedMetadata() {
    return feedMetadata;
  }

  @Override
  public List<DeletedEntryMetadata> getDeletedEntries() {
    return deletedEntries;
  }

}
