// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Service
public class FileImportStorageS3 {

    private static final Logger logger = LoggerFactory.getLogger(FileImportStorageS3.class);

    private final String bucket;
    private final S3Client s3Client;

    public FileImportStorageS3(@Value("${aws.bucket.fileImport}") String bucket, S3Client s3Client) {
        this.bucket = bucket;
        this.s3Client = s3Client;
    }

    public void upload(byte[] bytes, String s3FileKey) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucket).key(s3FileKey).build();
        PutObjectResponse response = s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));
        if (response.eTag() == null || response.eTag().trim().isEmpty()) {
            throw new RuntimeException("S3 upload returned no ETag for key: " + s3FileKey);
        }
        logger.debug("Uploaded {} to S3, ETag: {}", s3FileKey, response.eTag());
    }

    public byte[] download(String s3FileKey) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucket).key(s3FileKey).build();
        final ResponseBytes<GetObjectResponse> object = s3Client.getObject(getObjectRequest,
                ResponseTransformer.toBytes());
        return object.asByteArray();
    }

    public void copy(String oldS3FileKey, String newS3FileKey) {
        CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder().sourceBucket(bucket).sourceKey(oldS3FileKey)
                .destinationBucket(bucket).destinationKey(newS3FileKey).build();
        s3Client.copyObject(copyObjectRequest);
    }
}
