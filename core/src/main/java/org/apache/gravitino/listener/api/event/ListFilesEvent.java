/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.gravitino.listener.api.event;

import org.apache.gravitino.NameIdentifier;
import org.apache.gravitino.Namespace;
import org.apache.gravitino.annotation.DeveloperApi;

/**
 * Represents an event that is triggered upon the successful listing of filesets within a system.
 */
@DeveloperApi
public final class ListFilesEvent extends FilesetEvent {
  private final String listFiles;
  /**
   * Constructs a new {@code ListFilesetEvent}.
   *
   * @param user The user who initiated the listing of filesets.
   * @param identifier The identifier contextual information, identifying the scope and boundaries
   *     of the listing operation.
   */
  public ListFilesEvent(String user, NameIdentifier identifier, String listFiles) {
    super(user, identifier);
    this.listFiles = listFiles;
  }

  /**
   * Retrieves the namespace associated with the failed listing event.
   *
   * @return The {@link Namespace} that was targeted during the failed listing operation.
   */
  public String listFiles() {
    return listFiles;
  }
}
