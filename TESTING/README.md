# Bộ integration test tách biệt

Thư mục này gom các test có liên quan đến integration/system/API để có thể tách khỏi project sau khi hoàn tất kiểm thử. Mã nguồn ứng dụng trong `backend` và `frontend` không phụ thuộc vào thư mục này khi chạy bình thường.

Hướng dẫn chi tiết theo từng module, lệnh chạy, cách cấu hình và vị trí report: [`MODULE_USAGE_GUIDE.md`](./MODULE_USAGE_GUIDE.md).

## Thành phần

- `backend-integration`: JUnit 5 + Spring Boot Test + MockMvc + Testcontainers. SQL Server và Redis thật được khởi tạo trong container; email được mock để không gửi ra ngoài.
- `system-tests`: Playwright kiểm tra UI và API trên hệ thống đã triển khai bằng Docker Compose.
- `api-tests/http`: các request `.http` dùng trong IntelliJ IDEA hoặc VS Code REST Client.
- `run-backend-integration.ps1`: build backend thành JAR nội bộ rồi chạy Failsafe trên test `*IT.java`.
- `run-system-tests.ps1`: bật hệ thống, cài Chromium và chạy smoke test cùng Report 5.3.

## 1. Chạy backend integration test

Yêu cầu: JDK 21, Maven và Docker Desktop đang chạy.

Nếu `JAVA_HOME` chưa hợp lệ, script sẽ tự tìm bản JDK cao nhất trong `C:\Program Files\Java`; nếu không tìm thấy, script dừng với thông báo yêu cầu cấu hình JDK 21.

Script sẽ build backend trước để tạo cả executable JAR và JAR class gốc (`.jar.original`), rồi cài JAR class gốc vào Maven local với classifier `classes`. Harness tách biệt dùng classifier này để biên dịch test, đồng thời dùng dependency backend chính để lấy đầy đủ thư viện liên quan.

Từ thư mục gốc repository:

```powershell
powershell -ExecutionPolicy Bypass -File .\TESTING\run-backend-integration.ps1
```

Chạy riêng lớp Report 5.3:

```powershell
mvn -f .\backend\pom.xml -DskipTests install
mvn install:install-file -Dfile=.\backend\target\backend-0.0.1-SNAPSHOT.jar.original -DgroupId=com.fpt -DartifactId=backend -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar -Dclassifier=classes -DpomFile=.\backend\pom.xml
mvn -f .\TESTING\backend-integration\pom.xml -Dit.test=Report53IntegrationIT verify
```

Kết quả Failsafe nằm ở `TESTING/backend-integration/target/failsafe-reports`.

## 2. Chạy Playwright system test

Sao chép `TESTING/system-tests/.env.example` thành `.env`, điền tài khoản và ID chỉ dùng cho môi trường test. Các case thay đổi dữ liệu yêu cầu `E2E_ALLOW_DESTRUCTIVE=true` và chỉ được chạy trên database dùng một lần.

```powershell
Copy-Item .\TESTING\system-tests\.env.example .\TESTING\system-tests\.env
powershell -ExecutionPolicy Bypass -File .\TESTING\run-system-tests.ps1
```

Các lệnh hữu ích khi làm việc trực tiếp trong `TESTING/system-tests`:

```powershell
npm run test:smoke
npm run test:report53
npm run test:headed
npm run test:ui
npm run report
```

## 3. Chạy request `.http`

1. Bật hệ thống: `docker compose -f .\deploy\docker-compose.yml up -d --build`.
2. Mở file trong `TESTING/api-tests/http` bằng IntelliJ IDEA hoặc VS Code REST Client.
3. Chạy request login trước, lấy JWT và đặt vào header `Authorization: Bearer <token>` cho các request cần xác thực.
4. Đối chiếu status, JSON response và dữ liệu SQL Server sau mỗi request thay đổi dữ liệu.

## 4. Ý nghĩa báo cáo 500 case

Workbook Report 5.2 chứa đúng 500 kịch bản theo 40 chức năng của form. Tất cả được để `Pending` vì chưa chạy trong môi trường của bạn. Các test tự động hiện có là nguồn khởi đầu và bằng chứng traceability, không được ghi nhận giả là 500 lần chạy thành công.

Sau khi test xong, có thể di chuyển hoặc xóa toàn bộ thư mục `TESTING`; chỉ cần giữ lại workbook và các report kết quả mà bạn muốn nộp.
