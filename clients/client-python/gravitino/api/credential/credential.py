# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

from abc import ABC, abstractmethod
from typing import Dict


class Credential(ABC):
    """Represents the credential in Gravitino."""

    # The HTTP header used to get the credential from fileset location
    HTTP_HEADER_CURRENT_LOCATION_NAME = "Current-Location-Name"

    @abstractmethod
    def credential_type(self) -> str:
        """The type of the credential.

        Returns:
             the type of the credential.
        """
        pass

    @abstractmethod
    def expire_time_in_ms(self) -> int:
        """Returns the expiration time of the credential in milliseconds since
        the epoch, 0 means it will never expire.

        Returns:
             The expiration time of the credential.
        """
        pass

    @abstractmethod
    def credential_info(self) -> Dict[str, str]:
        """The credential information.

        Returns:
             The credential information.
        """
        pass
