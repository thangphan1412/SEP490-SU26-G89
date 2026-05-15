# SEP490-SU26-G89
1.Project title :

2.Description:

# 3.Prerequisites:
- Dùng jdk25 
- React :19.2.6
- React-dom: 19.2.6

# 4.Installation:
//// tôi push hết lên docker rồi
- Ae tải docker đăng nhập vào
- Mở terminal gõ lệnh:"cd deploy" để chuyển sang file deploy
- Gõ lệnh : "docker-compose up --build" nó ra 3 file backend, frontend, deploy started là ok
- Không cần setup môi trường gì đâu
- lên trình duyệt gõ host của be, fe la dc
- Host be: 8080
- Host fe :3000
//// cho ae nào chạy local
- Ae naò gặp vấn đề kết nối check theo thứ tự:
  (BE)
+ Check xem đã tạo database trong mysql workbench chưa 
+ Vào file application xem đã đặt url đúng chưa
+ check username
+ check password
- FE 
+ Ae tải react trước
+ Rồi chạy fe dùng lênhj:npm run dev
- Docker:
+ build Be: mvn clean package
5.Usage:

6.Contributing:

7.License: