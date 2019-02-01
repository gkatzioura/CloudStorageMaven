# S3StorageWagon

## Upload/download maven artifacts using s3

The S3StorageWagon project enables you to upload your artifacts to a google cloud storage bucket. 

```xml
<build>
    <extensions>
        <extension>
            <groupId>com.gkatzioura.maven.cloud</groupId>
            <artifactId>s3-storage-wagon</artifactId>
            <version>1.2</version>
        </extension>
    </extensions>
</build>
```
Full guide on [wagon](https://egkatzioura.com/2018/04/09/host-your-maven-artifacts-using-amazon-s3/)

### Public repos

You can specify your artifacts to be public and thus getting downloaded without the need for authorised access to your bucket.

To specify a repo as public you can do it through the settings.xml

```xml
<server>
  <id>bucket-repo</id>
  <username>access_key</username>
  <password>access_secret</password>
  <configuration>
    <region>eu-west-1</region>
    <publicRepository>true</publicRepository>
  </configuration>
</server>
``` 

You can also use system properties with the mvn command

```bash
mvn deploy -DpublicRepository=true
```

Or through environmental variables

```bash
PUBLIC_REPOSITORY=true mvn deploy
```

Then you can use the artifact without any authorised access

```bash
    <repositories>
        <repository>
            <id>bucket-repo</id>
            <url>https://s3-eu-west-1.amazonaws.com/whatever/snapshot</url>
        </repository>
    </repositories>
```

## Upload/download files for ci/cd purposes

Apart from giving a solution to use s3 a maven repository the storage s3-storage-wagon can be used as a plugin in order to
upload and download items from s3. 

###### Upload files

```xml
<build>
        <plugins>
            <plugin>
                <groupId>com.gkatzioura.maven.cloud</groupId>
                <artifactId>s3-storage-wagon</artifactId>
                <version>1.5-SNAPSHOT</version>
                <executions>
                    <execution>
                        <id>upload-single-file</id>
                        <phase>package</phase>
                        <goals>
                            <goal>s3-upload</goal>
                        </goals>
                        <configuration>
                            <bucket>yourbucketname</bucket>
                            <path>/file/path/test.txt</path>
                            <key>test.txt</key>
                        </configuration>
                    </execution>
                    <execution>
                        <id>upload-multiple-files</id>
                        <phase>package</phase>
                        <goals>
                            <goal>s3-upload</goal>
                        </goals>
                        <configuration>
                            <bucket>yourbucketname</bucket>
                            <path>/path/to/directory/with/files</path>
                            <key>prefixforfiles</key>
                        </configuration>
                    </execution>
                    <execution>
                        <id>upload-single-file-no-key</id>
                        <phase>package</phase>
                        <goals>
                            <goal>s3-upload</goal>
                        </goals>
                        <configuration>
                            <bucket>yourbucketname</bucket>
                            <path>/file/path/test.txt</path>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```

###### Download files

```xml
 <build>
        <plugins>
            <plugin>
                <groupId>com.gkatzioura.maven.cloud</groupId>
                <artifactId>s3-storage-wagon</artifactId>
                <version>1.5-SNAPSHOT</version>
                <executions>
                    <execution>
                        <id>download-multiple-files-to-one-directory</id>
                        <phase>package</phase>
                        <goals>
                            <goal>s3-download</goal>
                        </goals>
                        <configuration>
                            <bucket>yourbucketname</bucket>
                            <downloadPath>/path/to/directory</downloadPath>
                            <keys>file1.txt,file2.jpg</keys>
                        </configuration>
                    </execution>
                    <execution>
                        <id>download-files-and-files-starting-with-prefix</id>
                        <phase>package</phase>
                        <goals>
                            <goal>s3-download</goal>
                        </goals>
                        <configuration>
                            <bucket>yourbucketname</bucket>
                            <downloadPath>/path/to/directory</downloadPath>
                            <keys>prefix,file1.txt,file2.txt</keys>
                        </configuration>
                    </execution>
                    <execution>
                        <id>download-single-file</id>
                        <phase>package</phase>
                        <goals>
                            <goal>s3-download</goal>
                        </goals>
                        <configuration>
                            <bucket>yourbucketname</bucket>
                            <downloadPath>/path/to/directory/file.txt</downloadPath>
                            <keys>file-to-download.txt</keys>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
 </build>   
```

Full guide on upload [download](https://egkatzioura.com/2019/01/22/upload-and-download-files-to-s3-using-maven/)


