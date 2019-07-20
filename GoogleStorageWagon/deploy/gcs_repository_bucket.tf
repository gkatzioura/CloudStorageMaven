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

variable "bucket_name" {
}

variable "location" {
  default = "europe-west2"
}

variable "project" {
}

provider "google" {
  project = "${var.project}"
}

resource "google_storage_bucket" "cloud_storage_maven_repo" {
  name     = "${var.bucket_name}"
  location = "${var.location}"

  project = "${var.project}"
}

resource "google_project_iam_custom_role" "cloud_storage_maven_role" {
  role_id     = "${replace(var.bucket_name,"-","_")}Role"
  title       = "${var.bucket_name}Role"
  description = "Cloud Storage Maven Repository Bucket Role"
  permissions = ["storage.objects.create","storage.objects.get","storage.objects.list","storage.objects.delete"]
}

resource "google_service_account" "cloud_storage_service_account" {
  account_id    = "${var.bucket_name}-sa"
  display_name  = "${var.bucket_name}ServiceAccount"
}

resource "google_storage_bucket_iam_binding" "cloud_storage_bucket_service_account_iam_binding" {
  bucket = "${google_storage_bucket.cloud_storage_maven_repo.id}"
  members = [
    "serviceAccount:${google_service_account.cloud_storage_service_account.email}"
  ]
  role = "${google_project_iam_custom_role.cloud_storage_maven_role.id}"
}
