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

'use client'

import { useEffect } from 'react'

import { Box } from '@mui/material'

import { useAppDispatch, useAppSelector } from '@/lib/hooks/useStore'
import { useSearchParams } from 'next/navigation'
import MetalakePageLeftBar from './MetalakePageLeftBar'
import RightContent from './rightContent/RightContent'

import {
  fetchCatalogs,
  fetchSchemas,
  fetchTables,
  fetchFilesets,
  fetchTopics,
  fetchModels,
  fetchModelVersions,
  getMetalakeDetails,
  getCatalogDetails,
  getSchemaDetails,
  getTableDetails,
  getFilesetDetails,
  getTopicDetails,
  getModelDetails,
  getVersionDetails,
  setSelectedNodes
} from '@/lib/store/metalakes'

const MetalakeView = () => {
  const dispatch = useAppDispatch()
  const searchParams = useSearchParams()
  const paramsSize = [...searchParams.keys()].length
  const store = useAppSelector(state => state.metalakes)

  const buildNodePath = routeParams => {
    const keys = ['metalake', 'catalog', 'type', 'schema', 'table', 'fileset', 'topic', 'model']

    return keys.map(key => (routeParams[key] ? `{{${routeParams[key]}}}` : '')).join('')
  }

  useEffect(() => {
    const routeParams = {
      metalake: searchParams.get('metalake'),
      catalog: searchParams.get('catalog'),
      type: searchParams.get('type'),
      schema: searchParams.get('schema'),
      table: searchParams.get('table'),
      fileset: searchParams.get('fileset'),
      topic: searchParams.get('topic'),
      model: searchParams.get('model'),
      version: searchParams.get('version')
    }
    async function fetchDependsData() {
      if ([...searchParams.keys()].length) {
        const { metalake, catalog, type, schema, table, fileset, topic, model, version } = routeParams

        if (paramsSize === 1 && metalake) {
          dispatch(fetchCatalogs({ init: true, page: 'metalakes', metalake }))
          dispatch(getMetalakeDetails({ metalake }))
        }

        if (paramsSize === 3 && catalog) {
          if (!store.catalogs.length) {
            await dispatch(fetchCatalogs({ metalake }))
          }
          dispatch(fetchSchemas({ init: true, page: 'catalogs', metalake, catalog, type }))
          dispatch(getCatalogDetails({ metalake, catalog, type }))
        }

        if (paramsSize === 4 && catalog && type && schema) {
          if (!store.catalogs.length) {
            await dispatch(fetchCatalogs({ metalake }))
            await dispatch(fetchSchemas({ metalake, catalog, type }))
          }
          switch (type) {
            case 'relational':
              dispatch(fetchTables({ init: true, page: 'schemas', metalake, catalog, schema }))
              break
            case 'fileset':
              dispatch(fetchFilesets({ init: true, page: 'schemas', metalake, catalog, schema }))
              break
            case 'messaging':
              dispatch(fetchTopics({ init: true, page: 'schemas', metalake, catalog, schema }))
              break
            case 'model':
              dispatch(fetchModels({ init: true, page: 'schemas', metalake, catalog, schema }))
              break
            default:
              break
          }
          dispatch(getSchemaDetails({ metalake, catalog, schema }))
        }

        if (paramsSize === 5 && catalog && type && schema && (table || fileset || topic || model)) {
          if (!store.catalogs.length) {
            await dispatch(fetchCatalogs({ metalake }))
            await dispatch(fetchSchemas({ metalake, catalog, type }))
          }
          switch (type) {
            case 'relational':
              await dispatch(fetchTables({ init: true, page: 'schemas', metalake, catalog, schema }))
              await dispatch(getTableDetails({ init: true, metalake, catalog, schema, table }))
              break
            case 'fileset':
              await dispatch(fetchFilesets({ init: true, page: 'schemas', metalake, catalog, schema }))
              await dispatch(getFilesetDetails({ init: true, metalake, catalog, schema, fileset }))
              break
            case 'messaging':
              await dispatch(fetchTopics({ init: true, page: 'schemas', metalake, catalog, schema }))
              await dispatch(getTopicDetails({ init: true, metalake, catalog, schema, topic }))
              break
            case 'model':
              await dispatch(fetchModels({ init: true, page: 'schemas', metalake, catalog, schema }))
              await dispatch(fetchModelVersions({ init: true, metalake, catalog, schema, model }))
              await dispatch(getModelDetails({ init: true, metalake, catalog, schema, model }))
              break
            default:
              break
          }
        }
        if (paramsSize === 6 && version) {
          if (!store.catalogs.length) {
            await dispatch(fetchCatalogs({ metalake }))
            await dispatch(fetchSchemas({ metalake, catalog, type }))
            await dispatch(fetchModels({ init: true, page: 'schemas', metalake, catalog, schema }))
          }
          dispatch(getVersionDetails({ init: true, metalake, catalog, schema, model, version }))
        }
      }
    }
    fetchDependsData()

    dispatch(setSelectedNodes(routeParams.catalog ? [buildNodePath(routeParams)] : []))

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchParams])

  return (
    <Box className={'metalake-template'} style={{ height: 'calc(100vh - 11rem)' }}>
      <Box className='app-metalake twc-w-full twc-h-full twc-flex twc-rounded-lg twc-overflow-auto twc-relative shadow-md'>
        <Box className={`twc-bg-customs-white`} sx={{ borderRight: theme => `1px solid ${theme.palette.divider}` }}>
          <Box className={`twc-w-[340px] twc-h-full round-tl-md round-bl-md twc-overflow-hidden`}>
            <Box className={'twc-px-5 twc-py-3 twc-flex twc-items-center'}></Box>
            <MetalakePageLeftBar />
          </Box>
        </Box>
        <RightContent />
      </Box>
    </Box>
  )
}

export default MetalakeView
