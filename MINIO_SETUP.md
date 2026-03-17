# MinIO Setup Guide

## What Changed
Replaced LocalStack with MinIO for S3-compatible object storage.

## Configuration

### Docker Compose
- **API Port**: 9000 (S3 API)
- **Console Port**: 9001 (Web UI)
- **Credentials**: minioadmin / minioadmin

### Environment Variables (.env)
```properties
MINIO_PORT_EXPOSE=9000
MINIO_WEB_CONSOLE_PORT=9001
AWS_ENDPOINT=http://localhost:9000
AWS_ACCESS_KEY_ID=minioadmin
AWS_SECRET_ACCESS_KEY=minioadmin
AWS_S3_BUCKET_NAME=blog-images
```

## Usage

### 1. Start Services
```bash
docker-compose up -d
```

### 2. Access MinIO Console
Open browser: http://localhost:9001
- Username: `minioadmin`
- Password: `minioadmin`

### 3. Create Bucket
In MinIO console:
1. Click "Buckets" → "Create Bucket"
2. Name: `blog-images`
3. Click "Create Bucket"

### 4. Test Upload
Your Spring Boot app will automatically use MinIO for file uploads.

## Advantages over LocalStack
- ✅ Lighter weight
- ✅ Better web console
- ✅ Faster startup
- ✅ True S3 compatibility
- ✅ Persistent storage with volumes

## Troubleshooting

### Bucket doesn't exist error
Create the bucket manually in MinIO console or via CLI:
```bash
docker exec minio-blogapp-dev mc alias set local http://localhost:9000 minioadmin minioadmin
docker exec minio-blogapp-dev mc mb local/blog-images
```

### Connection refused
Check if MinIO is running:
```bash
docker ps | grep minio
```
