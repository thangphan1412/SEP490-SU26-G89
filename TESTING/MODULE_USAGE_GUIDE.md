# Hướng dẫn sử dụng các module kiểm thử

Tài liệu này phân biệt rõ các lớp kiểm thử đang có trong project. Chạy lệnh từ thư mục gốc repository, trừ khi phần hướng dẫn yêu cầu chuyển thư mục.

## 1. Bản đồ module

| Module | Công cụ | Mục đích | Cần Docker | Kết quả |
| --- | --- | --- | --- | --- |
| `backend/src/test/java` | Maven, JUnit 5, Mockito, MockMvc, H2 | Unit test, service test, controller test nhanh | Không | `backend/target/surefire-reports` |
| `TESTING/backend-integration` | Spring Boot Test, MockMvc, Testcontainers, SQL Server, Redis | Kiểm tra tích hợp qua HTTP, security, service và database thật | Có | `TESTING/backend-integration/target/failsafe-reports` |
| `TESTING/api-tests/http` | IntelliJ HTTP Client hoặc VS Code REST Client | Gọi và kiểm tra API thủ công | Có, hoặc backend chạy cục bộ | Cửa sổ response của IDE |
| `TESTING/system-tests` | Playwright | Kiểm tra UI và API trên hệ thống đã triển khai | Có | `playwright-report`, `test-results` |
| `deploy/docker-compose.yml` | Docker Compose | Khởi động SQL Server, Redis, backend và frontend | Có | Container logs |

Frontend hiện chưa cấu hình Vitest/Jest và không có script `npm test`. `npm run lint` và `npm run build` chỉ kiểm tra mã nguồn/build; kiểm thử tích hợp giao diện được thực hiện bằng Playwright trong `TESTING/system-tests`.

## 2. Chuẩn bị môi trường

Cần có:

- JDK 21 và Maven.
- Docker Desktop đang chạy.
- Node.js và npm cho Playwright.
- Các cổng `3000`, `8080`, `1434` và `6379` không bị ứng dụng khác chiếm khi dùng Docker Compose.

Kiểm tra nhanh:

```powershell
java -version
mvn -version
node --version
npm --version
docker version
```

Script backend sẽ tự tìm JDK trong `C:\Program Files\Java` nếu `JAVA_HOME` chưa hợp lệ.

## 3. Module unit/service/controller của backend

Module này chạy nhanh và dùng H2 in-memory theo `backend/src/test/resources/application.properties`. Nó phù hợp để kiểm tra logic riêng lẻ trước khi chạy integration test.

Chạy toàn bộ test backend:

```powershell
mvn -f .\backend\pom.xml test
```

Chạy một lớp:

```powershell
mvn -f .\backend\pom.xml -Dtest=ProjectServiceImplTest test
```

Chạy một phương thức:

```powershell
mvn -f .\backend\pom.xml -Dtest=ProjectServiceImplTest#tenPhuongThucTest test
```

Đọc kết quả trong `backend/target/surefire-reports`. Dòng tổng kết Maven cho biết số test đã chạy, failed, error và skipped. Một số lớp cũ trong project đang comment annotation hoặc nội dung test; không tính các lớp đó là đã chạy nếu Maven không ghi nhận trong summary.

## 4. Module backend integration với Testcontainers

Module `TESTING/backend-integration` khởi động SQL Server 2022 và Redis 7.2 trong container riêng, nạp toàn bộ Spring context và gọi endpoint bằng MockMvc. Email được mock để test không gửi thư ra ngoài. Container dùng cổng ngẫu nhiên và được dọn sau phiên test.

Chạy toàn bộ quy trình:

```powershell
powershell -ExecutionPolicy Bypass -File .\TESTING\run-backend-integration.ps1
```

Script thực hiện ba việc:

1. Build backend và cài dependency vào Maven local.
2. Cài JAR class gốc với classifier `classes` để harness tách biệt có thể biên dịch.
3. Chạy Maven Failsafe cho các lớp có tên `*IT.java`.

Sau khi đã chạy script ít nhất một lần, có thể chạy riêng lớp hoặc phương thức:

```powershell
mvn -f .\TESTING\backend-integration\pom.xml -Dit.test=Report53IntegrationIT verify
mvn -f .\TESTING\backend-integration\pom.xml -Dit.test=Report53IntegrationIT#e2e01_loginWithValidCredentials_returnsJwtAndRole verify
```

Kết quả nằm ở `TESTING/backend-integration/target/failsafe-reports`. Lần chạy đầu có thể lâu vì Docker phải tải image SQL Server và Redis. Nếu báo không kết nối được Docker daemon, mở Docker Desktop rồi chạy lại.

## 5. Module API `.http`

Module này dùng khi cần xem trực tiếp status code, header, JSON response và kiểm tra dữ liệu sau từng request.

1. Khởi động hệ thống theo mục 7.
2. Cài extension **REST Client** nếu dùng VS Code; IntelliJ IDEA có HTTP Client sẵn.
3. Mở `TESTING/api-tests/http/authentication.http`.
4. Thay `@email` và `@password` bằng tài khoản của môi trường test, rồi bấm **Send Request** ở `AUTH-01`.
5. Sao chép giá trị `data.token` từ response.
6. Mở file API cần test và thay `@token = replace-with-valid-jwt` bằng token vừa lấy.
7. Bấm **Send Request** trên từng request, so sánh response với expected trong tiêu đề case.

Các file hiện có:

- `authentication.http`: đăng nhập và kiểm tra sai mật khẩu.
- `user-management.http`: danh sách, chi tiết, tạo và cập nhật người dùng.
- `user-profile-management.http`: xem/cập nhật hồ sơ cá nhân.
- `company-profile-management.http`: xem/cập nhật hồ sơ công ty.
- `dashboard.http`: overview, statistical reports và pending signatures.

Các request `POST`, `PUT` hoặc `DELETE` có thể thay đổi dữ liệu. Chỉ chạy chúng trên database test và ghi lại ID bản ghi đã tạo để dọn dữ liệu khi cần.

## 6. Module Playwright system test

Playwright kiểm tra hai nhóm:

- `report53.ui.spec.js`: đăng nhập, điều hướng và quyền truy cập giao diện.
- `report53.api.spec.js`: trạng thái cuối của project/contract, dashboard và các nhánh thay đổi dữ liệu.

Chuẩn bị một lần:

```powershell
Copy-Item .\TESTING\system-tests\.env.example .\TESTING\system-tests\.env
Set-Location .\TESTING\system-tests
npm ci
npx playwright install chromium
```

Điền `.env` bằng tài khoản và ID từ database test. Giữ `E2E_ALLOW_DESTRUCTIVE=false` khi chỉ muốn chạy case an toàn. Các case xoá, từ chối, huỷ, ký lỗi hoặc reset password chỉ chạy khi biến này là `true`.

Các lệnh thường dùng trong `TESTING/system-tests`:

```powershell
npm run test:smoke       # Hai smoke test đăng nhập
npm run test:report53    # Các test gắn tag @report53
npm test                 # Toàn bộ Playwright suite
npm run test:headed      # Mở trình duyệt để quan sát
npm run test:ui          # Chế độ Playwright UI để chọn/debug case
npm run report           # Mở HTML report gần nhất
```

Chạy một file hoặc một case:

```powershell
npx playwright test tests/report53.api.spec.js
npx playwright test --grep "ALT-04"
```

Muốn chạy trọn quy trình gồm Docker Compose, cài dependency, smoke và Report 5.3:

```powershell
powershell -ExecutionPolicy Bypass -File .\TESTING\run-system-tests.ps1
```

HTML report nằm ở `TESTING/system-tests/playwright-report`. Screenshot, video và trace khi lỗi nằm trong `TESTING/system-tests/test-results`.

## 7. Module hạ tầng Docker Compose

Khởi động hệ thống:

```powershell
docker compose -f .\deploy\docker-compose.yml up -d --build
docker compose -f .\deploy\docker-compose.yml ps
```

Xem log:

```powershell
docker compose -f .\deploy\docker-compose.yml logs -f backend
docker compose -f .\deploy\docker-compose.yml logs -f frontend
```

Dừng container nhưng giữ database volume:

```powershell
docker compose -f .\deploy\docker-compose.yml down
```

Không thêm `-v` nếu còn cần dữ liệu test, vì tùy chọn đó xoá volume SQL Server.

## 8. Thứ tự chạy khuyến nghị

1. Chạy `mvn test` để phát hiện lỗi logic nhanh.
2. Chạy `run-backend-integration.ps1` để kiểm tra Spring, security, SQL Server và Redis.
3. Khởi động hệ thống bằng Docker Compose.
4. Chạy `npm run test:smoke`.
5. Chạy `npm run test:report53` với `E2E_ALLOW_DESTRUCTIVE=false`.
6. Sao lưu hoặc dùng database dùng một lần, chuyển biến thành `true`, rồi chạy các case thay đổi dữ liệu.
7. Đọc Surefire, Failsafe và Playwright reports; chỉ cập nhật `Passed`/`Failed` trong workbook cho case có bằng chứng tương ứng.

Workbook 500 case hiện là test plan, không tự động đổi trạng thái sau khi chạy. Việc cập nhật Round 1/2/3 phải dựa trên kết quả thật từ từng module ở trên.

## 9. Tách khỏi project sau kiểm thử

Có thể di chuyển toàn bộ thư mục `TESTING` sau khi hoàn thành. Trước khi tách, nên giữ lại `failsafe-reports`, `playwright-report`, `test-results` và workbook làm bằng chứng. Các script hiện dùng đường dẫn tương đối đến `backend`, `frontend` và `deploy`, nên muốn chạy lại sau khi tách vẫn cần đặt `TESTING` cạnh các thư mục đó hoặc sửa `$repositoryRoot`.
