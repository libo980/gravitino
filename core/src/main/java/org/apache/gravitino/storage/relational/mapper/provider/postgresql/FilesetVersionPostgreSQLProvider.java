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
package org.apache.gravitino.storage.relational.mapper.provider.postgresql;

import static org.apache.gravitino.storage.relational.mapper.FilesetVersionMapper.VERSION_TABLE_NAME;

import java.util.List;
import org.apache.gravitino.storage.relational.mapper.provider.base.FilesetVersionBaseSQLProvider;
import org.apache.gravitino.storage.relational.po.FilesetVersionPO;
import org.apache.ibatis.annotations.Param;

public class FilesetVersionPostgreSQLProvider extends FilesetVersionBaseSQLProvider {
  @Override
  public String softDeleteFilesetVersionsByMetalakeId(Long metalakeId) {
    return "UPDATE "
        + VERSION_TABLE_NAME
        + " SET deleted_at = floor(extract(epoch from((current_timestamp -"
        + " timestamp '1970-01-01 00:00:00')*1000)))"
        + " WHERE metalake_id = #{metalakeId} AND deleted_at = 0";
  }

  @Override
  public String softDeleteFilesetVersionsByCatalogId(Long catalogId) {
    return "UPDATE "
        + VERSION_TABLE_NAME
        + " SET deleted_at = floor(extract(epoch from((current_timestamp -"
        + " timestamp '1970-01-01 00:00:00')*1000)))"
        + " WHERE catalog_id = #{catalogId} AND deleted_at = 0";
  }

  @Override
  public String softDeleteFilesetVersionsBySchemaId(Long schemaId) {
    return "UPDATE "
        + VERSION_TABLE_NAME
        + " SET deleted_at = floor(extract(epoch from((current_timestamp -"
        + " timestamp '1970-01-01 00:00:00')*1000)))"
        + " WHERE schema_id = #{schemaId} AND deleted_at = 0";
  }

  @Override
  public String softDeleteFilesetVersionsByFilesetId(Long filesetId) {
    return "UPDATE "
        + VERSION_TABLE_NAME
        + " SET deleted_at = floor(extract(epoch from((current_timestamp -"
        + " timestamp '1970-01-01 00:00:00')*1000)))"
        + " WHERE fileset_id = #{filesetId} AND deleted_at = 0";
  }

  @Override
  public String deleteFilesetVersionsByLegacyTimeline(
      @Param("legacyTimeline") Long legacyTimeline, @Param("limit") int limit) {
    return "DELETE FROM "
        + VERSION_TABLE_NAME
        + " WHERE id IN (SELECT id FROM "
        + VERSION_TABLE_NAME
        + " WHERE deleted_at > 0 AND deleted_at < #{legacyTimeline} LIMIT #{limit})";
  }

  @Override
  public String softDeleteFilesetVersionsByRetentionLine(
      Long filesetId, long versionRetentionLine, int limit) {
    return "UPDATE "
        + VERSION_TABLE_NAME
        + " SET deleted_at = floor(extract(epoch from((current_timestamp -"
        + " timestamp '1970-01-01 00:00:00')*1000)))"
        + " WHERE fileset_id = #{filesetId} AND version <= #{versionRetentionLine} AND deleted_at = 0 LIMIT #{limit}";
  }

  @Override
  public String insertFilesetVersionsOnDuplicateKeyUpdate(
      List<FilesetVersionPO> filesetVersionPOs) {
    return "<script>"
        + "INSERT INTO "
        + VERSION_TABLE_NAME
        + " (metalake_id, catalog_id, schema_id, fileset_id,"
        + " version, fileset_comment, properties, storage_location_name, storage_location,"
        + " deleted_at)"
        + " VALUES "
        + "<foreach collection='filesetVersions' item='version' separator=','>"
        + " (#{version.metalakeId}, #{version.catalogId}, #{version.schemaId}, #{version.filesetId},"
        + " #{version.version}, #{version.filesetComment}, #{version.properties},"
        + " #{version.locationName}, #{version.storageLocation}, #{version.deletedAt})"
        + "</foreach>"
        + " ON CONFLICT(fileset_id, version, storage_location_name, deleted_at) DO UPDATE SET"
        + " metalake_id = EXCLUDED.metalake_id,"
        + " catalog_id = EXCLUDED.catalog_id,"
        + " schema_id = EXCLUDED.schema_id,"
        + " fileset_id = EXCLUDED.fileset_id,"
        + " version = EXCLUDED.version,"
        + " fileset_comment = EXCLUDED.fileset_comment,"
        + " properties = EXCLUDED.properties,"
        + " storage_location_name = EXCLUDED.storage_location_name,"
        + " storage_location = EXCLUDED.storage_location,"
        + " deleted_at = EXCLUDED.deleted_at"
        + "</script>";
  }
}
