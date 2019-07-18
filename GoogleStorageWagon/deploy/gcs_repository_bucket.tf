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
  permissions = ["storage.objects.create","storage.objects.get","storage.objects.list"]
}

resource "google_service_account" "cloud_storage_service_account" {
  account_id    = "${var.bucket_name}-sa"
  display_name  = "${var.bucket_name}ServiceAccount"
}

resource "google_service_account_iam_binding" "cloud_storage_maven_service_account_iam_policy" {
  service_account_id = "${google_service_account.cloud_storage_service_account.id}"
  role = "${google_project_iam_custom_role.cloud_storage_maven_role.id}"
  members = [
    "serviceAccount:${google_service_account.cloud_storage_service_account.email}"
  ]
}