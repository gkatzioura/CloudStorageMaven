#!/usr/bin/env bash
sudo yum remove java-1.7.0-openjdk -y
sudo yum install java-1.8.0 -y
sudo yum install git -y
sudo yum install maven -y
git clone https://github.com/gkatzioura/CloudStorageMaven.git
cd CloudStorageMaven/
aws s3 cp s3://$gpgbucket/gpg_secret_key.txt gpg_secret_key.txt
aws s3 cp s3://$gpgbucket/gpg_ownertrust.txt gpg_ownertrust.txt
cat gpg_secret_key.txt|base64 --decode| gpg --import
cat gpg_ownertrust.txt| base64 --decode| gpg --import-ownertrust

mvn clean install deploy -Dgpg.passphrase=$passphrase --settings ./deploy/settings.xml -Dnexus.user=$nexususer -Dnexus.password=nexuspassword
