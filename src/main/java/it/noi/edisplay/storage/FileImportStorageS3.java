package it.noi.edisplay.storage;

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

@Service
public class FileImportStorageS3 {

    private final String bucket;
    private final S3Client s3Client;

    public FileImportStorageS3(@Value("${aws.bucket.fileImport}") String bucket, S3Client s3Client) {
        this.bucket = bucket;
        this.s3Client = s3Client;
    }

    public void upload(byte[] bytes, String s3FileKey) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucket).key(s3FileKey).build();
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));
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
