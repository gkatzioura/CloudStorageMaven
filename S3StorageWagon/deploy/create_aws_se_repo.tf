provider "aws" {
}

variable "bucket-name" {}

resource "aws_s3_bucket" "b" {
  bucket = "${var.bucket-name}"
  acl    = "private"

  tags = {
    Name        = "cloud-storage-maven"
    Environment = "cloud-storage-maven-bucket"
  }
}

resource "aws_iam_policy" "policy" {
  name        = "${var.bucket-name}-rw-policy"
  path        = "/"
  description = "Policy for cloud storage maven repository"

  policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "ListObjectsInBucket",
            "Effect": "Allow",
            "Action": ["s3:ListBucket"],
            "Resource": ["arn:aws:s3:::${var.bucket-name}"]
        },
        {
            "Sid": "AllObjectActions",
            "Effect": "Allow",
            "Action": "s3:*Object",
            "Resource": ["arn:aws:s3:::${var.bucket-name}/*"]
        }
    ]
}
EOF

}

