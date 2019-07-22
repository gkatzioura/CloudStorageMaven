/*
 * Copyright 2018 Emmanouil Gkatziouras
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

variable "storage_account_name" {
}

variable "location" {
  default = "westeurope"
}

variable "resource_group_name" {
  default = "CloudStorageMaven"
}

resource "azurerm_resource_group" "cloud_storage_maven_resource_group" {
  name     = "${var.resource_group_name}"
  location = "${var.location}"
}

resource "azurerm_storage_account" "cloud_storage_maven_storage_account" {
  name                     = "${var.storage_account_name}"
  resource_group_name      = "${azurerm_resource_group.cloud_storage_maven_resource_group.name}"
  location                 = "${var.location}"
  account_tier             = "Standard"
  account_replication_type = "LRS"
}

resource "azurerm_storage_container" "cloud_storage_maven_storage_account_snapshot_container" {
  name                  = "snapshot"
  resource_group_name   = "${var.resource_group_name}"
  storage_account_name  = "${azurerm_storage_account.cloud_storage_maven_storage_account.name}"
  container_access_type = "private"
}

resource "azurerm_storage_container" "cloud_storage_maven_storage_account_release_container" {
  name                  = "release"
  resource_group_name   = "${var.resource_group_name}"
  storage_account_name  = "${azurerm_storage_account.cloud_storage_maven_storage_account.name}"
  container_access_type = "private"
}
