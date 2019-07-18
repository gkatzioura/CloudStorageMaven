variable "bucket_name" {
}

resource "aws_s3_bucket" "cloud_storage_maven_repo" {
  bucket = "${var.bucket_name}"
  acl    = "private"

  tags = {
    CloudStorageMaven = ""
  }
}

resource "aws_iam_policy" "cloud_storage_maven_repo_policy" {
  name        = "${var.bucket_name}_policy"
  path        = "/"
  description = "Cloud Storage Maven Repository Bucket Policy"

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject",
        "s3:ListBucket",
        "s3:DeleteObject"
      ],
      "Resource": "arn:aws:s3:::${var.bucket_name}/*"
    }
  ]
}
EOF
}

resource "aws_iam_role" "cloud_storage_maven_role" {
  name        = "${var.bucket_name}_role"
  path        = "/"
  description = "Cloud Storage Maven Repository Bucket Role"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "ec2.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF

}

resource "aws_iam_role_policy_attachment" "cloud_storage_maven_role_attach_policy" {
  role       = "${aws_iam_role.cloud_storage_maven_role.name}"
  policy_arn = "${aws_iam_policy.cloud_storage_maven_repo_policy.arn}"
}

resource "aws_iam_group" "cloud_storage_maven_group" {
  name = "${var.bucket_name}_group"
}

resource "aws_iam_group_policy_attachment" "cloud_storage_maven_group_attach_policy" {
  group      = "${aws_iam_group.cloud_storage_maven_group.name}"
  policy_arn = "${aws_iam_policy.cloud_storage_maven_repo_policy.arn}"
}