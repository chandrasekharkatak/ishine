# Large File Handling Implementation Plan

## Current State Analysis
- **Current Max File Size**: 20MB (configured in `application-dev.properties`)
- **Frontend Max File Size**: 20MB (from sessionStorage)
- **Current Implementation**: Single request upload/download (entire file in memory)
- **Issue**: Large PPT files (>20MB) fail due to memory constraints and timeout issues

## Proposed Solution Architecture

### Phase 1: Backend Configuration & Infrastructure

#### 1.1 Increase File Size Limits
**Files to Modify:**
- `resources/application-dev.properties`
- `resources/application-prod.properties` (if exists)

**Changes:**
```properties
# Increase limits for large files
spring.servlet.multipart.max-file-size=200MB
spring.servlet.multipart.max-request-size=200MB
spring.servlet.multipart.enabled=true
spring.servlet.multipart.file-size-threshold=2MB
```

#### 1.2 Add Chunked Upload Endpoints
**New Endpoints:**
- `POST /api/training/uploadChunk` - Upload a single chunk
- `POST /api/training/completeChunkedUpload` - Finalize chunked upload
- `GET /api/training/getUploadStatus/{uploadId}` - Check upload progress

**Files to Create/Modify:**
- `TrainingController.java` - Add chunked upload endpoints
- `TrainingService.java` - Add chunked upload service methods
- `TrainingServiceImpl.java` - Implement chunked upload logic
- New: `ChunkedUploadDTO.java` - DTO for chunk metadata
- New: `ChunkedUploadService.java` - Service to manage chunked uploads

#### 1.3 Streaming Download Endpoint
**Modify Existing:**
- `GET /api/training/downloadContent/{contentId}` - Add streaming support
- Use `StreamingResponseBody` for large files instead of loading entire file

**Files to Modify:**
- `TrainingController.java` - Update downloadContent to use StreamingResponseBody
- `TrainingServiceImpl.java` - Implement streaming download

#### 1.4 Compression (Optional)
**Approach:**
- Compress files on upload (gzip/deflate)
- Decompress on download
- Store compressed files on disk
- Add compression flag in content metadata

**Files to Modify:**
- `TrainingServiceImpl.java` - Add compression/decompression utilities
- `TrainingContent.java` - Add `isCompressed` field (optional)

---

### Phase 2: Frontend Implementation

#### 2.1 Chunked Upload Service
**New File:** `frontend/src/app/services/chunked-upload.service.ts`

**Features:**
- Split file into chunks (configurable chunk size: 5MB default)
- Upload chunks sequentially or in parallel (configurable)
- Track upload progress
- Retry failed chunks
- Resume interrupted uploads

**Methods:**
```typescript
uploadFileInChunks(file: File, trainingId: number, contentDTO: any): Observable<UploadProgress>
uploadChunk(chunk: Blob, chunkIndex: number, totalChunks: number, uploadId: string): Observable<any>
completeChunkedUpload(uploadId: string, metadata: any): Observable<any>
getUploadStatus(uploadId: string): Observable<UploadStatus>
```

#### 2.2 Update Training Config Component
**Files to Modify:**
- `training-config.component.ts` - Add chunked upload logic
- `training-config.component.html` - Add upload progress UI

**Changes:**
- Detect large files (>20MB) and use chunked upload
- Show upload progress bar
- Handle chunk upload errors and retries
- Update file size limit display

#### 2.3 Streaming Download
**Files to Modify:**
- `training.service.ts` - Add streaming download method
- `training-config.component.ts` - Handle streaming download for large files

**Approach:**
- Use `responseType: 'blob'` with `observe: 'response'`
- Stream chunks and reconstruct file
- Show download progress
- Handle partial downloads

#### 2.4 Progress Tracking UI
**New Components/Features:**
- Upload progress bar component
- Download progress indicator
- Chunk status display (optional)
- Error retry UI

---

### Phase 3: Database & Storage

#### 3.1 Chunked Upload Metadata Table (Optional)
**New Table:** `chunked_upload_metadata`
```sql
- upload_id (VARCHAR, PRIMARY KEY)
- training_id (INT)
- content_name (VARCHAR)
- total_chunks (INT)
- uploaded_chunks (INT)
- status (VARCHAR) - 'IN_PROGRESS', 'COMPLETED', 'FAILED'
- created_by (BIGINT)
- created_on (TIMESTAMP)
- expires_on (TIMESTAMP) - Cleanup old incomplete uploads
```

#### 3.2 Temporary Chunk Storage
**Approach:**
- Store chunks temporarily in `{trainingFileLocation}/temp/{uploadId}/`
- Merge chunks after all uploaded
- Cleanup temp files after merge or on expiry

---

### Phase 4: Error Handling & Resilience

#### 4.1 Retry Logic
- Automatic retry for failed chunks (max 3 retries)
- Exponential backoff for retries
- Manual retry option for users

#### 4.2 Resume Capability
- Track uploaded chunks
- Allow resuming from last successful chunk
- Cleanup incomplete uploads after 24 hours

#### 4.3 Validation
- Verify chunk integrity (checksum/MD5)
- Validate file after merge
- Handle corrupted chunks

---

## Implementation Details

### Chunked Upload Flow:
1. **Initiate Upload:**
   - Frontend: Split file into chunks (5MB each)
   - Backend: Create upload session, return uploadId
   
2. **Upload Chunks:**
   - Frontend: Upload chunks sequentially/parallel
   - Backend: Store chunks temporarily, track progress
   - Show progress: `(uploadedChunks / totalChunks) * 100`
   
3. **Complete Upload:**
   - Frontend: Send completion request with metadata
   - Backend: Merge chunks, validate file, save to final location
   - Return contentId

### Streaming Download Flow:
1. **Request Download:**
   - Frontend: Request file with range headers (if supported)
   - Backend: Stream file in chunks using `StreamingResponseBody`
   
2. **Receive Chunks:**
   - Frontend: Receive chunks, reconstruct blob
   - Show progress: `(receivedBytes / totalBytes) * 100`
   
3. **Complete:**
   - Frontend: Create blob URL for preview
   - Handle PPTX parsing if needed

### Compression Strategy (Optional):
- **Upload:** Compress file before chunking (if >50MB)
- **Storage:** Store compressed file
- **Download:** Decompress on-the-fly during streaming
- **Metadata:** Store compression flag in database

---

## Configuration Changes

### Backend (`application-dev.properties`):
```properties
# File Upload Limits
spring.servlet.multipart.max-file-size=200MB
spring.servlet.multipart.max-request-size=200MB
spring.servlet.multipart.enabled=true
spring.servlet.multipart.file-size-threshold=2MB

# Chunked Upload Configuration
training.chunk.size=5242880  # 5MB in bytes
training.chunk.parallel.uploads=3  # Max parallel chunk uploads
training.chunk.retry.max=3  # Max retries per chunk
training.chunk.temp.cleanup.hours=24  # Cleanup incomplete uploads after 24 hours

# Large File Threshold
training.large.file.threshold=20971520  # 20MB - use chunked upload above this
```

### Frontend (`training-config.component.ts`):
```typescript
// Configuration
CHUNK_SIZE = 5 * 1024 * 1024; // 5MB
LARGE_FILE_THRESHOLD = 20 * 1024 * 1024; // 20MB
MAX_PARALLEL_CHUNKS = 3;
MAX_RETRIES = 3;
```

---

## File Structure Changes

### New Backend Files:
```
java/com/apmosys/employeeportal/
├── dto/
│   └── ChunkedUploadDTO.java
├── service/
│   └── ChunkedUploadService.java
└── controller/
    └── TrainingController.java (modify - add chunked endpoints)
```

### New Frontend Files:
```
frontend/src/app/
├── services/
│   └── chunked-upload.service.ts
└── components/
    └── upload-progress/
        ├── upload-progress.component.ts
        ├── upload-progress.component.html
        └── upload-progress.component.css
```

---

## Testing Strategy

### Test Cases:
1. **Small Files (<20MB):**
   - Should use regular upload (no chunking)
   - Should work as before

2. **Large Files (20-100MB):**
   - Should use chunked upload
   - Should show progress
   - Should handle network interruptions

3. **Very Large Files (>100MB):**
   - Should use chunked upload with compression
   - Should handle timeout issues
   - Should resume on failure

4. **Download:**
   - Small files: Regular download
   - Large files: Streaming download with progress

5. **Error Scenarios:**
   - Network failure during upload
   - Server restart during upload
   - Corrupted chunks
   - Disk space issues

---

## Performance Considerations

### Memory Management:
- **Upload:** Process chunks one at a time, don't load entire file
- **Download:** Stream response, don't load entire file into memory
- **Compression:** Use streaming compression (if implemented)

### Network Optimization:
- Parallel chunk uploads (configurable, default 3)
- Chunk size optimization (5MB balance between speed and reliability)
- Compression for very large files (>50MB)

### Storage:
- Temporary chunk storage with cleanup
- Final file storage after merge
- Compression to save disk space

---

## Rollout Plan

### Phase 1: Backend Infrastructure (Week 1)
1. Increase file size limits
2. Implement chunked upload endpoints
3. Implement streaming download
4. Add chunked upload service

### Phase 2: Frontend Implementation (Week 1-2)
1. Create chunked upload service
2. Update training config component
3. Add progress UI
4. Implement streaming download

### Phase 3: Testing & Optimization (Week 2)
1. Test with various file sizes
2. Optimize chunk size
3. Test error scenarios
4. Performance tuning

### Phase 4: Compression (Optional - Week 3)
1. Implement compression/decompression
2. Add compression flag
3. Test with very large files

---

## Risk Mitigation

### Risks:
1. **Backward Compatibility:** Old files should still work
   - **Mitigation:** Check file size, use chunked only for large files

2. **Storage Space:** Chunks + final files use more space temporarily
   - **Mitigation:** Automatic cleanup of temp files

3. **Complexity:** More code to maintain
   - **Mitigation:** Well-documented, modular design

4. **Browser Compatibility:** Chunked upload might not work in old browsers
   - **Mitigation:** Feature detection, fallback to regular upload

---

## Alternative Approaches Considered

### Option 1: Simple Size Increase Only
- **Pros:** Simple, quick
- **Cons:** Still has memory issues, timeout problems

### Option 2: Compression Only
- **Pros:** Reduces file size
- **Cons:** Doesn't solve memory/timeout issues for very large files

### Option 3: Chunked Upload + Streaming Download (Recommended)
- **Pros:** Handles large files efficiently, scalable
- **Cons:** More complex implementation

### Option 4: External Storage (S3, etc.)
- **Pros:** Offloads server, better scalability
- **Cons:** Additional cost, infrastructure changes

---

## Recommended Approach

**Primary:** Chunked Upload + Streaming Download
- Implement chunked upload for files >20MB
- Implement streaming download for all files
- Add compression as optional enhancement later

**Fallback:** If chunked upload fails, show error with option to retry or use regular upload (with warning)

---

## Questions for Review

1. **Chunk Size:** 5MB per chunk - is this optimal? (Can be configurable)
2. **Parallel Uploads:** Upload 3 chunks in parallel - acceptable?
3. **Compression:** Implement now or later?
4. **Max File Size:** What should be the new limit? (Proposed: 200MB)
5. **Backward Compatibility:** Should we support both chunked and regular upload?
6. **Progress UI:** How detailed should progress be? (Overall vs per-chunk)

---

## Estimated Effort

- **Backend:** 2-3 days
- **Frontend:** 2-3 days  
- **Testing:** 1-2 days
- **Total:** 5-8 days

---

## Next Steps After Approval

1. Implement backend chunked upload endpoints
2. Implement streaming download
3. Create frontend chunked upload service
4. Update UI with progress indicators
5. Test with various file sizes
6. Deploy and monitor
